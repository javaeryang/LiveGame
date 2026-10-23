package com.`fun`.live.get

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

    // Spirit Stone State (Level 1 to 100)
    var spiritStoneCount by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }

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
            // Only accumulate spirit stone count continuously without deducting 100!
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
                    count = spiritStoneCount,
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
                                .padding(top = 40.dp)
                        )

                        // B. Floating Spirit Stone (悬空灵石 / 至尊水晶) hovering above turntable
                        SpiritStoneView(
                            spiritStoneCount = if (level >= 100) 100 else spiritStoneCount,
                            level = level,
                            modifier = Modifier
                                .size(280.dp)
                                .padding(bottom = 60.dp)
                        )

                        // C. Explosive Level Up Particle Burst Canvas
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

                        // D. Floating "+2" Particles
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
 * Prominent Spirit Stone Count Display positioned directly below the Five Elements Turntable (转盘正下方)
 */
@Composable
private fun SpiritStoneCountDisplay(
    count: Int,
    level: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CountPulse")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CountBorderGlow"
    )

    Box(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xBB310052),
                        Color(0xDD4A148C),
                        Color(0xBB310052)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = borderGlow),
                        Color(0xFFE040FB).copy(alpha = borderGlow),
                        Color(0xFF00E5FF).copy(alpha = borderGlow),
                        Color(0xFFFFD700).copy(alpha = borderGlow)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (level >= 100) "✨ 至尊灵石点数：" else "💎 当前灵石点数：",
                color = if (level >= 100) Color(0xFFFFD700) else Color(0xFFE040FB),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "$count",
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Self-animating "+2" Floating Particle Text
 */
@Composable
private fun FloatingPlusTwoText(
    particle: FloatingParticle,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
        onDismiss()
    }

    val progress = anim.value
    val alpha = (1f - progress).coerceIn(0f, 1f)
    val yOffsetDp = -progress * 110f
    val scale = 1f + progress * 0.35f

    Text(
        text = "+2",
        color = particle.color.copy(alpha = alpha),
        fontSize = (26 * scale).sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier.offset(
            x = particle.xOffsetDp.dp,
            y = yOffsetDp.dp
        )
    )
}

/**
 * Top Header displaying Realm, Tier Name, Level, Progress and Action Buttons (💎 图鉴 / 📜 日志)
 */
@Composable
private fun HeaderSection(
    level: Int,
    count: Int,
    tierName: String,
    onOpenHistory: () -> Unit,
    onOpenLevelPreview: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeaderGlowPulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x551A0033))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = glowPulse),
                        Color(0xFFE040FB).copy(alpha = glowPulse),
                        Color(0xFFFFD700).copy(alpha = glowPulse)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Level Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFF8E24AA))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "等阶 $level",
                    color = Color(0xFF1A0033),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            // Tier Name
            Text(
                text = "【$tierName】",
                color = Color(0xFFFFD700),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            // Action Buttons Row (💎 图鉴 & 📜 日志)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // 图鉴按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xAA4A148C))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                        .clickable { onOpenLevelPreview() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "💎 图鉴",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 日志按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xAA310052))
                        .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(12.dp))
                        .clickable { onOpenHistory() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "📜 日志",
                        color = Color(0xFFE040FB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (level >= 100) "破阶状态" else "灵石进阶进度",
                color = Color(0xFFE040FB),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = if (level >= 100) "已登顶至尊 ($count 点)" else "$count / 100",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Streamer Progress Bar
        val progressFraction = if (level >= 100) 1.0f else (count / 100f).coerceIn(0.001f, 1.0f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF140026))
                .border(1.dp, Color(0x66BA68C8), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF7B1FA2),
                                Color(0xFFE040FB),
                                Color(0xFFFFD700)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 1-100 Spirit Stone Level List Preview Dialog (100 阶灵石等阶图鉴预览弹窗)
 */
@Composable
private fun LevelPreviewDialog(
    currentLevel: Int,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A004D),
                        Color(0xFF140026),
                        Color(0xFF0D001A)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFE040FB), Color(0xFF00E5FF), Color(0xFFFFD700))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💎 1-100 阶灵石等阶图鉴",
                    color = Color(0xFFFFD700),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x66E040FB))
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(text = "关闭", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Level Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x554A148C))
                    .border(1.dp, Color(0xFFE040FB), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "当前等阶：第 $currentLevel 阶 · ${getLevelTierName(currentLevel)}",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Jump Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val jumpTargets = listOf(
                    "第 1 阶" to 0,
                    "第 30 阶" to 29,
                    "第 50 阶" to 49,
                    "第 100 阶 至尊水晶" to 99
                )

                jumpTargets.forEach { (label, index) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (index == 99) Color(0xAAFFD700) else Color(0x444A148C))
                            .clickable {
                                coroutineScope.launch {
                                    listState.scrollToItem(index)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (index == 99) Color(0xFF1A0033) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 100 Level Tiers LazyColumn List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(LEVEL_TIERS_LIST, key = { it.level }) { tier ->
                    val isCurrent = tier.level == currentLevel
                    val isUnlocked = tier.level <= currentLevel

                    val cardBg = when {
                        isCurrent -> Color(0xAA4A148C)
                        tier.level == 100 -> Color(0x666A1B9A)
                        isUnlocked -> Color(0x443B0066)
                        else -> Color(0x22140026)
                    }

                    val borderColor = when {
                        isCurrent || tier.level == 100 -> Color(0xFFFFD700)
                        isUnlocked -> Color(0xFFE040FB)
                        else -> Color(0x44BA68C8)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini Spirit Stone / Supreme Crystal Visual Preview
                        SpiritStoneView(
                            spiritStoneCount = 50,
                            level = tier.level,
                            modifier = Modifier.size(54.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "第 ${tier.level} 阶 · ${tier.name}",
                                    color = if (tier.level == 100) Color(0xFFFFD700) else if (isUnlocked) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Status Tag
                                val (statusText, statusBg, statusTextColor) = when {
                                    isCurrent -> Triple("🌟 当前", Color(0xFFFFD700), Color(0xFF1A0033))
                                    tier.level == 100 && isUnlocked -> Triple("✨ 满阶至尊", Color(0xFFFFD700), Color(0xFF1A0033))
                                    isUnlocked -> Triple("✓ 已解锁", Color(0x4400E676), Color(0xFF00E676))
                                    else -> Triple("🔒 未解锁", Color(0x33FFFFFF), Color.Gray)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusTextColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = tier.description,
                                color = if (isUnlocked) Color.White.copy(alpha = 0.85f) else Color.Gray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "特性: ${tier.features}",
                                color = if (tier.level == 100) Color(0xFFFFD700) else Color(0xFFE040FB),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog displaying SQLite Database Click History Records
 */
@Composable
private fun HistoryLogsDialog(
    logs: List<ClickRecord>,
    totalClickCount: Int,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A004D),
                        Color(0xFF140026),
                        Color(0xFF0D001A)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFE040FB), Color(0xFFFFD700))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 数据库炼石记录 (共 $totalClickCount 次)",
                    color = Color(0xFFFFD700),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x66E040FB))
                        .clickable { onClose() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "关闭", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无数据库炼石记录", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x443B0066))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.formattedTime,
                                    color = Color(0xFFBA68C8),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "累积: ${record.totalCount} 灵石 | 第 ${record.level} 阶",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "+${record.points}",
                                color = Color(0xFFFFD700),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Level Up Celebration Banner (灵石破阶升级)
 */
@Composable
private fun LevelUpCelebrationBanner(
    level: Int,
    tierName: String
) {
    Box(
        modifier = Modifier
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF3B0066),
                        Color(0xFF1A0033),
                        Color(0xFF0D001A)
                    )
                )
            )
            .border(
                width = 2.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFD700),
                        Color(0xFFE040FB),
                        Color(0xFF00E5FF),
                        Color(0xFFFFD700)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (level == 100) "👑 至 尊 登 顶 👑" else "✨ 灵 石 进 阶 ✨",
                color = Color(0xFFFFD700),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "突破至 第 $level 阶 · $tierName",
                color = Color(0xFFE040FB),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (level == 100) "已解锁终极最高等阶【至尊水晶】！" else "数据已存入数据库，品质大增！",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}
