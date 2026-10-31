package com.`fun`.live.get

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

/**
 * Remote game save as returned by backend `/game/data`.
 */
data class RemoteGameData(
    val count: Int,
    val level: Int,
    val totalClicks: Int,
    val clientUpdatedAt: Long
)

/** HTTP error from backend. 4xx (except 408/429) are not retried. */
class GameApiException(val httpCode: Int, message: String) : IOException(message) {
    val retryable: Boolean get() = httpCode >= 500 || httpCode == 408 || httpCode == 429
}

/**
 * Syncs Spirit Stone game data with backend:
 *  - [submit]     提交指定数据 (PUT, 服务端不存在则创建)
 *  - [pull]       拉取服务端存档并写入本地数据库
 *  - [pushFromDb] 读取本地数据库，有未同步修改时推送到后端
 * All network calls automatically retry up to [MAX_RETRIES] times with exponential backoff.
 */
class GameSyncRepository(context: Context, private val db: SpiritStoneDatabase) {

    companion object {
        private const val TAG = "GameSync"

        //我的账号
        //deviceId=415cf26e-47f3-45c7-be86-5e5e40cc40d4

        /** Backend base URL; requests go to $BASE_URL/game/data/{deviceId}. */
        const val BASE_URL = "https://pay.javaer.fun/pay"
        const val MAX_RETRIES = 5
        private const val INITIAL_BACKOFF_MS = 1000L
        private const val TIMEOUT_MS = 10_000
    }

    /** Stable per-install player id. */
    val deviceId: String = context.getSharedPreferences("game_sync", Context.MODE_PRIVATE).let { sp ->
        sp.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            sp.edit().putString("device_id", it).apply()
        }
    }

    /** 提交数据到后端(新增或更新)。 */
    suspend fun submit(count: Int, level: Int, totalClicks: Int, clientUpdatedAt: Long): RemoteGameData {
        Log.i(TAG, "submit start: deviceId=$deviceId count=$count level=$level totalClicks=$totalClicks clientUpdatedAt=$clientUpdatedAt")
        val result = withRetry("submit") {
            val body = JSONObject()
                .put("count", count)
                .put("level", level)
                .put("totalClicks", totalClicks)
                .put("clientUpdatedAt", clientUpdatedAt)
            parseGame(request("PUT", "/game/data/${enc(deviceId)}", body))
        }
        Log.i(TAG, "submit success: server=$result")
        return result
    }

    /** 从后端拉取存档，并写入本地数据库。服务端无存档时返回 null。 */
    suspend fun pull(): RemoteGameData? {
        Log.i(TAG, "pull start: deviceId=$deviceId")
        val remote = try {
            withRetry("pull") { parseGame(request("GET", "/game/data/${enc(deviceId)}", null)) }
        } catch (e: GameApiException) {
            if (e.httpCode == 404) {
                Log.i(TAG, "pull: no save on server (404)")
                return null
            }
            throw e
        }
        val local = db.getSyncSnapshot()
        // 本地有更新的未同步数据时不覆盖，交给 pushFromDb 上传
        if (!(local.dirty && local.updatedAt > remote.clientUpdatedAt)) {
            db.applyRemoteState(remote.count, remote.level, remote.clientUpdatedAt)
            Log.i(TAG, "pull success: applied remote to local DB, remote=$remote (local was $local)")
        } else {
            Log.i(TAG, "pull success: local is newer & unsynced, keep local=$local, remote=$remote")
        }
        return remote
    }

    /** 将本地数据库中的数据推送到后端；无待同步数据时返回 false。 */
    suspend fun pushFromDb(force: Boolean = false): Boolean {
        val snapshot = db.getSyncSnapshot()
        Log.i(TAG, "pushFromDb: local=$snapshot force=$force")
        if (!snapshot.dirty && !force) {
            Log.i(TAG, "pushFromDb: nothing to push")
            return false
        }
        submit(snapshot.count, snapshot.level, snapshot.totalClicks, snapshot.updatedAt)
        db.markSynced(snapshot.updatedAt)
        Log.i(TAG, "pushFromDb success: marked synced at ${snapshot.updatedAt}")
        return true
    }

    /** 启动时同步：先推送本地未同步数据，再拉取最新存档。失败不抛出。 */
    suspend fun syncOnStart(): RemoteGameData? = try {
        Log.i(TAG, "syncOnStart begin: baseUrl=$BASE_URL deviceId=$deviceId")
        pushFromDb()
        pull().also { Log.i(TAG, "syncOnStart done: remote=$it") }
    } catch (e: CancellationException) {
        Log.i(TAG, "syncOnStart cancelled")
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "syncOnStart failed: ${e.message}", e)
        null
    }

    private suspend fun <T> withRetry(name: String, block: suspend () -> T): T {
        var backoff = INITIAL_BACKOFF_MS
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: IOException) {
                val retryable = (e as? GameApiException)?.retryable ?: true
                if (!retryable) {
                    Log.e(TAG, "$name failed, not retryable: ${e.javaClass.simpleName}: ${e.message}")
                    throw e
                }
                if (attempt >= MAX_RETRIES) {
                    Log.e(TAG, "$name failed after $MAX_RETRIES retries: ${e.javaClass.simpleName}: ${e.message}")
                    throw e
                }
                attempt++
                Log.w(TAG, "$name failed (${e.javaClass.simpleName}: ${e.message}), retry $attempt/$MAX_RETRIES in ${backoff}ms")
                delay(backoff)
                backoff *= 2
            }
        }
    }

    private suspend fun request(method: String, path: String, body: JSONObject?): JSONObject =
        withContext(Dispatchers.IO) {
            val url = BASE_URL + path
            val start = System.currentTimeMillis()
            Log.d(TAG, "--> $method $url ${body ?: ""}")
            val conn = URL(url).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = TIMEOUT_MS
                conn.readTimeout = TIMEOUT_MS
                conn.setRequestProperty("Accept", "application/json")
                if (body != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                Log.d(TAG, "<-- $code $method $url (${System.currentTimeMillis() - start}ms) $text")
                if (code !in 200..299) {
                    val msg = runCatching { JSONObject(text).optString("message") }.getOrNull()
                    throw GameApiException(code, msg?.ifBlank { null } ?: "HTTP $code")
                }
                JSONObject(text)
            } catch (e: IOException) {
                if (e !is GameApiException) {
                    Log.w(TAG, "<-- FAILED $method $url (${System.currentTimeMillis() - start}ms): ${e.javaClass.simpleName}: ${e.message}")
                }
                throw e
            } finally {
                conn.disconnect()
            }
        }

    private fun parseGame(json: JSONObject): RemoteGameData {
        val g = json.getJSONObject("data").getJSONObject("game")
        return RemoteGameData(
            count = g.getInt("count"),
            level = g.getInt("level"),
            totalClicks = g.optInt("totalClicks"),
            clientUpdatedAt = g.optLong("clientUpdatedAt")
        )
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
