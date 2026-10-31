package com.`fun`.live.get

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a recorded click log in the database.
 */
data class ClickRecord(
    val id: Long = 0,
    val timestamp: Long,
    val formattedTime: String,
    val points: Int,
    val totalCount: Int,
    val level: Int
)

/**
 * Data class representing saved Spirit Stone persistent state.
 */
data class SavedState(
    val count: Int,
    val level: Int
)

/**
 * Local state snapshot used for backend sync. [dirty] = has unsynced local changes.
 */
data class SyncSnapshot(
    val count: Int,
    val level: Int,
    val totalClicks: Int,
    val updatedAt: Long,
    val dirty: Boolean
)

/**
 * SQLite Persistence Database Helper for Spirit Stone.
 * Table 1: `click_records` - Stores every click event with timestamp and points.
 * Table 2: `spirit_stone_state` - Stores total count and level state across app restarts.
 */
class SpiritStoneDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "spirit_stone.db"
        private const val DATABASE_VERSION = 2

        // Table 1: Click Records
        private const val TABLE_RECORDS = "click_records"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_FORMATTED_TIME = "formatted_time"
        private const val COLUMN_POINTS = "points"
        private const val COLUMN_TOTAL_COUNT = "total_count"
        private const val COLUMN_LEVEL = "level"

        // Table 2: State
        private const val TABLE_STATE = "spirit_stone_state"
        private const val COLUMN_STATE_ID = "id"
        private const val COLUMN_STATE_COUNT = "count"
        private const val COLUMN_STATE_LEVEL = "level"
        private const val COLUMN_STATE_UPDATED_AT = "updated_at"
        private const val COLUMN_STATE_DIRTY = "dirty"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Click Records Table
        val createRecordsTable = """
            CREATE TABLE $TABLE_RECORDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_FORMATTED_TIME TEXT NOT NULL,
                $COLUMN_POINTS INTEGER NOT NULL,
                $COLUMN_TOTAL_COUNT INTEGER NOT NULL,
                $COLUMN_LEVEL INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createRecordsTable)

        // Create State Table
        val createStateTable = """
            CREATE TABLE $TABLE_STATE (
                $COLUMN_STATE_ID INTEGER PRIMARY KEY,
                $COLUMN_STATE_COUNT INTEGER NOT NULL,
                $COLUMN_STATE_LEVEL INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createStateTable)

        // Insert default initial state
        val initialValues = ContentValues().apply {
            put(COLUMN_STATE_ID, 1)
            put(COLUMN_STATE_COUNT, 0)
            put(COLUMN_STATE_LEVEL, 1)
        }
        db.insert(TABLE_STATE, null, initialValues)

        addSyncColumns(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // v1 -> v2: keep player data, only add sync columns
        if (oldVersion < 2) {
            addSyncColumns(db)
        }
    }

    private fun addSyncColumns(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE $TABLE_STATE ADD COLUMN $COLUMN_STATE_UPDATED_AT INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE $TABLE_STATE ADD COLUMN $COLUMN_STATE_DIRTY INTEGER NOT NULL DEFAULT 1")
    }

    /**
     * Full local snapshot used for syncing with backend.
     */
    fun getSyncSnapshot(): SyncSnapshot {
        val cursor = readableDatabase.query(
            TABLE_STATE,
            arrayOf(COLUMN_STATE_COUNT, COLUMN_STATE_LEVEL, COLUMN_STATE_UPDATED_AT, COLUMN_STATE_DIRTY),
            "$COLUMN_STATE_ID = ?", arrayOf("1"), null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return SyncSnapshot(
                    count = it.getInt(0),
                    level = it.getInt(1),
                    totalClicks = getTotalClickCount(),
                    updatedAt = it.getLong(2),
                    dirty = it.getInt(3) == 1
                )
            }
        }
        return SyncSnapshot(0, 1, 0, 0L, false)
    }

    /**
     * Marks local state as synced, only if it hasn't changed since [updatedAt] (avoids losing newer clicks).
     */
    fun markSynced(updatedAt: Long) {
        val values = ContentValues().apply { put(COLUMN_STATE_DIRTY, 0) }
        writableDatabase.update(
            TABLE_STATE, values,
            "$COLUMN_STATE_ID = ? AND $COLUMN_STATE_UPDATED_AT = ?",
            arrayOf("1", updatedAt.toString())
        )
    }

    /**
     * Overwrites local state with data pulled from backend (marked as clean).
     */
    fun applyRemoteState(count: Int, level: Int, updatedAt: Long) {
        val values = ContentValues().apply {
            put(COLUMN_STATE_COUNT, count)
            put(COLUMN_STATE_LEVEL, level)
            put(COLUMN_STATE_UPDATED_AT, updatedAt)
            put(COLUMN_STATE_DIRTY, 0)
        }
        writableDatabase.update(TABLE_STATE, values, "$COLUMN_STATE_ID = ?", arrayOf("1"))
    }

    /**
     * Records a click event into SQLite database and updates current persistent state.
     */
    fun recordClick(points: Int, totalCount: Int, level: Int): ClickRecord {
        val db = writableDatabase
        val nowMs = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(nowMs))

        // 1. Insert Click Record
        val recordValues = ContentValues().apply {
            put(COLUMN_TIMESTAMP, nowMs)
            put(COLUMN_FORMATTED_TIME, formattedTime)
            put(COLUMN_POINTS, points)
            put(COLUMN_TOTAL_COUNT, totalCount)
            put(COLUMN_LEVEL, level)
        }
        val insertedId = db.insert(TABLE_RECORDS, null, recordValues)

        // 2. Update Spirit Stone State
        val stateValues = ContentValues().apply {
            put(COLUMN_STATE_COUNT, totalCount)
            put(COLUMN_STATE_LEVEL, level)
            put(COLUMN_STATE_UPDATED_AT, nowMs)
            put(COLUMN_STATE_DIRTY, 1)
        }
        db.update(TABLE_STATE, stateValues, "$COLUMN_STATE_ID = ?", arrayOf("1"))

        return ClickRecord(
            id = insertedId,
            timestamp = nowMs,
            formattedTime = formattedTime,
            points = points,
            totalCount = totalCount,
            level = level
        )
    }

    /**
     * Loads saved Spirit Stone persistent state.
     */
    fun getSavedState(): SavedState {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STATE,
            arrayOf(COLUMN_STATE_COUNT, COLUMN_STATE_LEVEL),
            "$COLUMN_STATE_ID = ?",
            arrayOf("1"),
            null, null, null
        )

        var count = 0
        var level = 1
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE_COUNT))
            level = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATE_LEVEL))
        }
        cursor.close()
        return SavedState(count, level)
    }

    /**
     * Fetches recent click records from database (newest first).
     */
    fun getRecentClickRecords(limit: Int = 30): List<ClickRecord> {
        val db = readableDatabase
        val list = mutableListOf<ClickRecord>()
        val cursor = db.query(
            TABLE_RECORDS,
            arrayOf(COLUMN_ID, COLUMN_TIMESTAMP, COLUMN_FORMATTED_TIME, COLUMN_POINTS, COLUMN_TOTAL_COUNT, COLUMN_LEVEL),
            null, null, null, null,
            "$COLUMN_TIMESTAMP DESC",
            limit.toString()
        )

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            val timeStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FORMATTED_TIME))
            val pts = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_POINTS))
            val total = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_COUNT))
            val lvl = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LEVEL))

            list.add(
                ClickRecord(
                    id = id,
                    timestamp = timestamp,
                    formattedTime = timeStr,
                    points = pts,
                    totalCount = total,
                    level = lvl
                )
            )
        }
        cursor.close()
        return list
    }

    /**
     * Gets total count of clicks recorded in database.
     */
    fun getTotalClickCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_RECORDS", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }
}
