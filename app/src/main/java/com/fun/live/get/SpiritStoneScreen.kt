package com.`fun`.live.get

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import android.util.Log
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data class for Spirit Stone Level Tier Preview Info (灵石等阶图鉴数据)
 */
data class LevelTierInfo(
    val level: Int,
    val name: String,
    val description: String,
    val features: String
)

/**
 * Helper to get cultivation tier names for levels up to Level 100 (至尊水晶)
 */
private fun getLevelTierName(level: Int): String {
    return when (level) {
        100 -> "至尊水晶"
        99 -> "无上帝尊晶"
        in 90..98 -> "寰宇至尊晶 (${level - 89}阶)"
        in 80..89 -> "荒古尊晶 (${level - 79}阶)"
        in 70..79 -> "不朽道晶 (${level - 69}阶)"
        in 60..69 -> "创世神晶 (${level - 59}阶)"
        in 50..59 -> "太初天晶 (${level - 49}阶)"
        in 40..49 -> "混沌仙晶 (${level - 39}阶)"
        in 30..39 -> "鸿蒙尊晶 (${level - 29}阶)"
        in 20..29 -> "帝品魔晶 (${level - 19}阶)"
        in 15..19 -> "圣品灵晶 (${level - 14}阶)"
        in 10..14 -> "神品灵石 (${level - 9}阶)"
        in 7..9 -> "仙品灵石 (${level - 6}阶)"
        in 5..6 -> "极品灵石 (${level - 4}阶)"
        4 -> "上品灵石"
        3 -> "中品灵石"
        2 -> "下品灵石"
        1 -> "凡品灵石"
        else -> "万界至尊晶 ($level 阶)"
    }
}

/**
 * Dynamically generated 100 level tier list for Level Preview Dialog
 */
val LEVEL_TIERS_LIST = List(100) { i ->
    val lvl = i + 1
    val name = getLevelTierName(lvl)
    val desc = when {
        lvl == 100 -> "至尊之道，万界永恒！独创至尊水晶耀世光华"
        lvl >= 90 -> "凝聚寰宇至尊威能，星河交辉，至高无上"
        lvl >= 80 -> "不朽道痕缠绕，掌万物生灭之玄机"
        lvl >= 70 -> "创世神威凝聚，诞生无上神光法阵"
        lvl >= 60 -> "太初无上天力，超脱生死六道之外"
        lvl >= 50 -> "混沌初开，鸿蒙精粹汇聚于晶体核心"
        lvl >= 40 -> "至尊帝威喷薄而出，威压诸天万界"
        lvl >= 30 -> "圣品精华凝练，晶体通透澄澈如琉璃"
        lvl >= 20 -> "仙道法则附着，诞生双重交错星轨"
        lvl >= 10 -> "精纯灵力化液，散发璀璨深紫灵光"
        else -> "天地初具灵气，凝聚基础紫晶形态"
    }
    val feature = when {
        lvl == 100 -> "🌟 终极解锁：至尊水晶七彩耀世法阵"
        lvl >= 90 -> "解锁三重星轨与全彩尊晶特效"
        lvl >= 80 -> "解锁不朽紫金道光辉映"
        lvl >= 70 -> "解锁创世神光莲花聚气法阵"
        lvl >= 60 -> "解锁太初天辉流光扫掠"
        lvl >= 50 -> "解锁混沌九面精雕紫金神晶"
        lvl >= 40 -> "解锁帝威紫光暴击特效"
        lvl >= 30 -> "解锁圣品双重交错星轨"
        lvl >= 20 -> "解锁仙品紫云星轨环绕"
        lvl >= 10 -> "解锁七面棱镜精雕紫晶"
        else -> "基础六面紫晶形态"
    }
    LevelTierInfo(lvl, name, desc, feature)
}

/**
 * Main Screen for Floating Spirit Stone (灵石悬空主界面)
 */
@Composable
fun SpiritStoneScreen() {
    val context = LocalContext.current
    val db = remember { SpiritStoneDatabase(context) }
    val syncRepo = remember { GameSyncRepository(context.applicationContext, db) }
    val syncScope = rememberCoroutineScope()
    var pushJob by remember { mutableStateOf<Job?>(null) }

    // Spirit Stone State (Level 1 to 100)
    var spiritStoneCount by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }

    // Active Turntable Element Index for Pulse (-1 = none, 0..4 = 金木水火土)
    var activeElementIndex by remember { mutableIntStateOf(-1) }

    // Converging Elemental Light Particles List (五行光点汇聚动画)
    val convergingParticles = remember { mutableStateListOf<ConvergingElementalParticle>() }

    // Frame Ticker for 60FPS smooth particle updates
    var particleFrameTicker by remember { mutableLongStateOf(0L) }

    LaunchedEffect(convergingParticles.isNotEmpty()) {
        while (convergingParticles.isNotEmpty()) {
            withFrameNanos { particleFrameTicker = it }
        }
    }

    // SoundPool Player setup for "凝练灵石" crystal sound effect
    val soundPool = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(8, AudioManager.STREAM_MUSIC, 0)
        }
    }
    var soundId by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        soundId = soundPool.load(context, R.raw.spirit_condense, 1)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }

    fun playCondenseSound() {
        if (soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    // History Log Dialog State
    var showHistoryDialog by remember { mutableStateOf(false) }
    val historyLogs = remember { mutableStateListOf<ClickRecord>() }

    // Level Preview Dialog State (等阶图鉴)
    var showLevelPreviewDialog by remember { mutableStateOf(false) }

    // Level Up Celebration Banner State
    var showLevelUpBanner by remember { mutableStateOf(false) }
    var levelUpTierName by remember { mutableStateOf("凡品灵石") }

    // Floating "+2" Particles List
    val floatingParticles = remember { mutableStateListOf<FloatingParticle>() }

    // Burst Animation for Level Up
    val burstAnim = remember { Animatable(0f) }

    // Load initial state & history from SQLite Database on startup
    LaunchedEffect(Unit) {
        val saved = db.getSavedState()
        spiritStoneCount = saved.count
        level = saved.level

        val logs = db.getRecentClickRecords(30)
        historyLogs.clear()
        historyLogs.addAll(logs)
        Log.i("GameSync", "local load: count=${saved.count} level=${saved.level} history=${logs.size}")

        // Sync with backend: push unsynced local data, then pull latest save
        if (syncRepo.syncOnStart() != null) {
            val synced = db.getSavedState()
            spiritStoneCount = synced.count
            level = synced.level
            Log.i("GameSync", "UI refreshed from synced DB: count=${synced.count} level=${synced.level}")
        }
    }

    // Trigger burst animation and level up banner when level changes
    LaunchedEffect(level) {
        if (level > 1) {
            burstAnim.snapTo(0f)
            burstAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
        }
    }

    // Hide level up banner after 2.5 seconds
    LaunchedEffect(showLevelUpBanner) {
        if (showLevelUpBanner) {
            delay(2500)
            showLevelUpBanner = false
        }
    }

    // Background Stars
    val stars = remember { List(40) { StarParticle() } }

    // Handle Spirit Stone gather action (+2)
    fun gatherSpiritStone() {
        // 1. Play crystal spirit stone sound effect
        playCondenseSound()

        // 2. Advance active element node index on turntable
        activeElementIndex = (activeElementIndex + 1) % FIVE_ELEMENTS.size

        // 3. Trigger Five Elements light particles starting from 金, 木, 水, 火, 土 nodes to Spirit Stone center
        val now = System.currentTimeMillis()
        FIVE_ELEMENTS.forEachIndexed { i, node ->
            val rad = Math.toRadians(node.angleDegrees.toDouble()).toFloat()
            convergingParticles.add(
                ConvergingElementalParticle(
                    elementName = node.name,
                    color = node.color,
                    startAngleRad = rad,
                    startRadiusRatio = 0.38f,
                    targetYOffsetDp = -25f,
                    durationMs = 420f + i * 25f,
                    curveOffsetDp = if (i % 2 == 0) 25f else -25f,
                    creationTimeMs = now
                )
            )
        }

        // 4. Update Spirit Stone count
        spiritStoneCount += 2

        if (level < 100) {
            // Normal level progression before 100
            if (spiritStoneCount >= 100) {
                spiritStoneCount -= 100
                level += 1
                levelUpTierName = getLevelTierName(level)
                showLevelUpBanner = true
            }
        } else {
            // Reached Level 100 (至尊水晶)!
            // Accumulate spirit stone count continuously
        }

        // Record click to SQLite Database with timestamp and points
        val record = db.recordClick(
            points = 2,
            totalCount = spiritStoneCount,
            level = level
        )

        // Add to history log list
        historyLogs.add(0, record)
        if (historyLogs.size > 50) {
            historyLogs.removeAt(historyLogs.lastIndex)
        }

        // Debounced push of local DB state to backend (auto-retries up to 5 times)
        Log.d("GameSync", "click recorded: id=${record.id} count=${record.totalCount} level=${record.level}, push scheduled")
        pushJob?.cancel()
        pushJob = syncScope.launch {
            delay(1500)
            runCatching { syncRepo.pushFromDb() }
                .onFailure { if (it !is CancellationException) Log.e("GameSync", "push failed: ${it.message}", it) }
        }

        // Add floating "+2" particle safely
        floatingParticles.add(FloatingParticle())
        if (floatingParticles.size > 15) {
            floatingParticles.removeAt(0)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D001A) // Deep cosmic violet background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- 1. Ambient Cosmic Background & Star Dust Particles ---
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A004D),
                            Color(0xFF140026),
                            Color(0xFF0A0014)
                        ),
                        center = Offset(size.width / 2f, size.height * 0.45f),
                        radius = size.width * 0.95f
                    )
                )

                stars.forEach { star ->
                    drawCircle(
                        color = star.color.copy(alpha = star.baseAlpha),
                        radius = star.radius,
                        center = Offset(star.xRatio * size.width, star.yRatio * size.height)
                    )
                }
            }

            // --- 2. Main Layout Content ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // --- Top Header: Spirit Stone Realm & Progress Bar ---
                HeaderSection(
                    level = level,
                    tierName = getLevelTierName(level),
                    onOpenHistory = { showHistoryDialog = true },
                    onOpenLevelPreview = { showLevelPreviewDialog = true }
                )

                // --- Center Column: Turntable & Spirit Stone + Points Display below Turntable ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // A. Five Elements Turntable (转盘，金木水火土) positioned below stone
                        ElementalTurntable(
                            modifier = Modifier
                                .size(340.dp)
                                .padding(top = 40.dp),
                            activeElementIndex = activeElementIndex
                        )

                        // B. Floating Spirit Stone (悬空灵石 / 至尊水晶) hovering above turntable
                        SpiritStoneView(
                            spiritStoneCount = if (level >= 100) 100 else spiritStoneCount,
                            level = level,
                            onGather = { gatherSpiritStone() },
                            modifier = Modifier
                                .size(280.dp)
                                .padding(bottom = 60.dp)
                        )

                        // C. Converging Elemental Light Particles Overlay (五行灵石光点汇聚动画)
                        if (convergingParticles.isNotEmpty()) {
                            if (particleFrameTicker >= 0L) {
                                val currentTime = System.currentTimeMillis()

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val turntableRadius = minOf(size.width, size.height) * 0.42f
                                    val startRadius = turntableRadius * 0.72f
                                    val targetPos = Offset(center.x, center.y - 25.dp.toPx()) // Spirit Stone Center

                                    val toRemove = mutableListOf<ConvergingElementalParticle>()

                                    convergingParticles.forEach { particle ->
                                        val elapsed = (currentTime - particle.creationTimeMs).toFloat()
                                        val progress = (elapsed / particle.durationMs).coerceIn(0f, 1f)

                                        // Start position on turntable element node
                                        val startX = center.x + (startRadius * cos(particle.startAngleRad.toDouble())).toFloat()
                                        val startY = center.y + (startRadius * sin(particle.startAngleRad.toDouble())).toFloat()
                                        val startPos = Offset(startX, startY)

                                        // Control point for curved arc
                                        val midX = (startX + targetPos.x) / 2f + particle.curveOffsetDp.dp.toPx()
                                        val midY = (startY + targetPos.y) / 2f - 15.dp.toPx()
                                        val controlPos = Offset(midX, midY)

                                        // Quadratic Bezier interpolation
                                        fun bezierPoint(t: Float): Offset {
                                            val invT = 1f - t
                                            val x = invT * invT * startPos.x + 2f * invT * t * controlPos.x + t * t * targetPos.x
                                            val y = invT * invT * startPos.y + 2f * invT * t * controlPos.y + t * t * targetPos.y
                                            return Offset(x, y)
                                        }

                                        val currentPos = bezierPoint(progress)

                                        // Draw Tail Trailing Line
                                        if (progress > 0.05f) {
                                            val trailPos = bezierPoint((progress - 0.12f).coerceAtLeast(0f))
                                            drawLine(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        particle.color.copy(alpha = (1f - progress) * 0.85f),
                                                        particle.color.copy(alpha = 0f)
                                                    ),
                                                    start = currentPos,
                                                    end = trailPos
                                                ),
                                                start = currentPos,
                                                end = trailPos,
                                                strokeWidth = (1f - progress * 0.4f) * 6.dp.toPx()
                                            )
                                        }

                                        // Draw Glowing Particle Head
                                        val pRadius = (1f - progress * 0.25f) * 7.dp.toPx()
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    particle.color,
                                                    Color.Transparent
                                                ),
                                                center = currentPos,
                                                radius = pRadius * 2.2f
                                            ),
                                            radius = pRadius * 2.2f,
                                            center = currentPos
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = pRadius * 0.7f,
                                            center = currentPos
                                        )

                                        // Center Impact Ripple Flash when particle converges
                                        if (progress >= 0.85f) {
                                            val impactAlpha = (1f - (progress - 0.85f) / 0.15f).coerceIn(0f, 1f)
                                            drawCircle(
                                                color = particle.color.copy(alpha = impactAlpha * 0.5f),
                                                radius = (progress - 0.85f) * 100.dp.toPx(),
                                                center = targetPos,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                            )
                                        }

                                        if (progress >= 1f) {
                                            toRemove.add(particle)
                                        }
                                    }

                                    if (toRemove.isNotEmpty()) {
                                        convergingParticles.removeAll(toRemove)
                                    }
                                }
                            }
                        }

                        // D. Explosive Level Up Particle Burst Canvas
                        if (burstAnim.value in 0.001f..0.999f) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val p = burstAnim.value
                                val alpha = (1f - p).coerceIn(0f, 1f)
                                val maxDist = size.width * 0.38f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                repeat(28) { i ->
                                    val angle = (i * 2.0 * Math.PI / 28)
                                    val dist = p * maxDist * (0.6f + (i % 4) * 0.12f)
                                    val px = center.x + (dist * cos(angle)).toFloat()
                                    val py = center.y + (dist * sin(angle)).toFloat()

                                    val particleColor = when (i % 4) {
                                        0 -> Color(0xFFFFD700)
                                        1 -> Color(0xFFE040FB)
                                        2 -> Color(0xFF00E5FF)
                                        else -> Color.White
                                    }

                                    drawCircle(
                                        color = particleColor.copy(alpha = alpha),
                                        radius = (1f - p * 0.4f) * 6.dp.toPx(),
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }

                        // E. Floating "+2" Particles
                        floatingParticles.forEach { particle ->
                            key(particle.id) {
                                FloatingPlusTwoText(
                                    particle = particle,
                                    onDismiss = { floatingParticles.remove(particle) },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    // --- Spirit Stone Points Display Below Turntable (转盘正下方灵石点数显示) ---
                    SpiritStoneCountDisplay(
                        count = spiritStoneCount,
                        level = level
                    )
                }

                // --- Bottom Action Button: Streamer Quality Button (+2) ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = if (level >= 100) "已至终极【100 阶 · 至尊水晶】，不断凝聚无限灵石点数！" else "点击凝聚天地灵气，每满100灵石升级至更高阶位",
                        color = Color(0xAAEA80FC),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    StreamerButton(
                        text = "凝 炼 灵 石 (+2)",
                        onClick = { gatherSpiritStone() }
                    )
                }
            }

            // --- 3. Level Up Celebration Overlay Banner (灵石进阶) ---
            AnimatedVisibility(
                visible = showLevelUpBanner,
                enter = fadeIn() + scaleIn(initialScale = 0.6f),
                exit = fadeOut() + scaleOut(targetScale = 1.2f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                LevelUpCelebrationBanner(
                    level = level,
                    tierName = levelUpTierName
                )
            }

            // --- 4. Database Click Records History Dialog (修仙炼石日志) ---
            AnimatedVisibility(
                visible = showHistoryDialog,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                HistoryLogsDialog(
                    logs = historyLogs,
                    totalClickCount = db.getTotalClickCount(),
                    onClose = { showHistoryDialog = false }
                )
            }

            // --- 5. Spirit Stone Level List Preview Dialog (1-100 阶灵石等阶图鉴预览) ---
            AnimatedVisibility(
                visible = showLevelPreviewDialog,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            ) {
                LevelPreviewDialog(
                    currentLevel = level,
                    onClose = { showLevelPreviewDialog = false }
                )
            }
        }
    }
}

/**
 * Top Header Bar (顶栏状态展示)
 */
@Composable
private fun HeaderSection(
    level: Int,
    tierName: String,
    onOpenHistory: () -> Unit,
    onOpenLevelPreview: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Realm Tier Title Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4A148C),
                            Color(0xFF8E24AA)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFE040FB)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onOpenLevelPreview() }
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "第 $level 阶 · $tierName 📖",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // History Log Button
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF311B92))
                .border(1.dp, Color(0xFFBA68C8), CircleShape)
                .clickable { onOpenHistory() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "📜 炼石日志",
                color = Color(0xFFFFE082),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Spirit Stone Points Display Below Turntable (转盘下方灵石数量组件)
 */
@Composable
private fun SpiritStoneCountDisplay(
    count: Int,
    level: Int
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xBB1A0033))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE040FB),
                        Color(0xFFFFD700),
                        Color(0xFF00E5FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (level >= 100) "至尊水晶灵力: $count 灵石" else "已凝炼灵石: $count / 100",
            color = Color(0xFFFFD700),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Floating "+2" Text Particle
 */
@Composable
private fun FloatingPlusTwoText(
    particle: FloatingParticle,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(particle.id) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        onDismiss()
    }

    val yOffsetDp = (-animProgress.value * 120f).dp
    val alpha = (1f - animProgress.value).coerceIn(0f, 1f)

    Text(
        text = "+2 灵石",
        color = particle.color.copy(alpha = alpha),
        fontSize = (20 + animProgress.value * 6).sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
            .offset(x = particle.xOffsetDp.dp, y = yOffsetDp)
    )
}

/**
 * Level Up Celebration Banner
 */
@Composable
private fun LevelUpCelebrationBanner(
    level: Int,
    tierName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A1B9A),
                        Color(0xFF4A148C),
                        Color(0xFF311B92)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFFFFE082),
                        Color(0xFFFFD700)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨ 灵 石 进 阶 ✨",
                color = Color(0xFFFFD700),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "恭喜突破至第 $level 阶",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "【$tierName】",
                color = Color(0xFFE040FB),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * SQLite Click Logs History Dialog
 */
@Composable
private fun HistoryLogsDialog(
    logs: List<ClickRecord>,
    totalClickCount: Int,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.75f)
            .clip(RoundedCornerShape(24.dp)),
        color = Color(0xFF1B0033)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color(0xFFBA68C8), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 炼石修仙日志",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✖ 关闭",
                    color = Color(0xAAFFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "累计凝炼次数: $totalClickCount 次 (SQLite持久化存储)",
                color = Color(0xFFE040FB),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无凝炼记录，快去点击按钮吧！",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(logs) { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A004D))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "凝炼 +${log.points} 灵石 (第 ${log.level} 阶)",
                                    color = Color(0xFFFFE082),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.formattedTime,
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "蓄积: ${log.totalCount}",
                                color = Color(0xFF00E5FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Spirit Stone Level List Preview Dialog (1 - 100 阶灵石等阶图鉴预览)
 */
@Composable
private fun LevelPreviewDialog(
    currentLevel: Int,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()
    var selectedLevel by remember { mutableIntStateOf(currentLevel) }
    val selectedTier = LEVEL_TIERS_LIST.getOrNull((selectedLevel - 1).coerceIn(0, 99)) ?: LEVEL_TIERS_LIST[0]

    // Auto scroll to current active level in tier list
    LaunchedEffect(currentLevel) {
        if (currentLevel in 1..100) {
            listState.scrollToItem((currentLevel - 1).coerceIn(0, 99))
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .fillMaxHeight(0.86f)
            .clip(RoundedCornerShape(24.dp)),
        color = Color(0xFF16002A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 灵石百阶全景图鉴",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✖ 关闭",
                    color = Color(0xAAFFFFFF),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击列表项预览任意阶位灵石 3D 特效 · 当前位阶: 第 $currentLevel 阶",
                color = Color(0xFFE040FB),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // --- A. Top Spotlight Preview Card (选中阶位 3D 悬浮灵石效果全景预览) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38006B),
                                Color(0xFF1F003D)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFE040FB),
                                Color(0xFF00E5FF)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Interactive 3D Spirit Stone View Spotlight
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0D001A)),
                        contentAlignment = Alignment.Center
                    ) {
                        SpiritStoneView(
                            spiritStoneCount = 100,
                            level = selectedLevel,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right: Selected Tier Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "第 $selectedLevel 阶 · ${selectedTier.name}",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val isSelectedUnlocked = selectedLevel <= currentLevel
                            Text(
                                text = if (selectedLevel == currentLevel) "★ 当前" else if (isSelectedUnlocked) "已解锁" else "未解锁",
                                color = if (selectedLevel == currentLevel) Color(0xFF00E5FF) else if (isSelectedUnlocked) Color(0xFF00E676) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTier.description,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTier.features,
                            color = Color(0xFFEA80FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- B. Tier List with Live Spirit Stone Thumbnails (百阶图鉴列表) ---
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(LEVEL_TIERS_LIST) { tier ->
                    val isCurrent = tier.level == currentLevel
                    val isSelected = tier.level == selectedLevel
                    val isUnlocked = tier.level <= currentLevel

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF4A148C)
                                else if (isUnlocked) Color(0xFF26004D)
                                else Color(0xFF1A0033)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else if (isCurrent) 1.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFFFFD700)
                                else if (isCurrent) Color(0xFF00E5FF)
                                else if (isUnlocked) Color(0xFF8E24AA)
                                else Color(0x338E24AA),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedLevel = tier.level }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Spirit Stone Live Miniature Preview (左侧灵石 3D 模型预览)
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF10002B))
                                    .border(
                                        width = 0.8.dp,
                                        color = if (isSelected) Color(0xFFFFD700) else Color(0x44BA68C8),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                SpiritStoneView(
                                    spiritStoneCount = 60,
                                    level = tier.level,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Tier Text Information
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "第 ${tier.level} 阶 · ${tier.name}",
                                        color = if (isUnlocked) Color(0xFFFFD700) else Color.Gray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isCurrent) "★ 当前" else if (isUnlocked) "已解锁" else "未解锁",
                                        color = if (isCurrent) Color(0xFF00E5FF) else if (isUnlocked) Color(0xFF00E676) else Color.DarkGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tier.description,
                                    color = if (isUnlocked) Color.White else Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tier.features,
                                    color = if (isUnlocked) Color(0xFFEA80FC) else Color.DarkGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
