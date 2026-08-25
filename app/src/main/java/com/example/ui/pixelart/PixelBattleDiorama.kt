package com.example.ui.pixelart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * Animated Visual Battle Diorama depicting Roman Legion vs Enemy in Real Tactical Phases.
 */
@Composable
fun PixelBattleDiorama(
    result: ExpeditionResult,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "battle_anim")

    val clashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clash"
    )

    val spearFlight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spear"
    )

    val dustClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dust"
    )

    val sparksClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparks"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, RomanGold, RoundedCornerShape(12.dp))
            .background(RomanDarkSurface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Dynamic Dramatic Battlefield Sky
            drawBattleSky(w, h, result.outcome)

            // 2. Battlefield Ground & Dust Clouds
            drawBattlefieldGround(w, h, dustClock)

            // 3. Roman Legion Line (Left to Center)
            drawRomanLegionFormation(
                w = w,
                h = h,
                tactics = result.tactics,
                clashPhase = clashPhase,
                isVictor = result.outcome.isSuccess
            )

            // 4. Enemy Host / Barbarian Line (Right to Center)
            drawEnemyHostFormation(
                w = w,
                h = h,
                expedition = result.expedition,
                clashPhase = clashPhase,
                isRouted = result.outcome == ExpeditionOutcome.GREAT_VICTORY || result.outcome == ExpeditionOutcome.VICTORY
            )

            // 5. Flying Projectiles (Pilum Barrage & Arrows)
            drawProjectilesInFlight(w, h, spearFlight)

            // 6. Center Clash Sparks & Impact Smoke
            drawClashImpactSparks(w * 0.50f, h * 0.65f, sparksClock, clashPhase)

            // 7. Tactical Phase & Outcome Banner
            drawBattleMoraleIndicators(w, h, result)
        }

        // Tactical Overlay HUD
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .background(Color(0xE6140D0A), RoundedCornerShape(6.dp))
                .border(1.dp, RomanGold, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${result.outcome.icon} ${result.outcome.titleRu}",
                color = if (result.outcome.isSuccess) RomanGoldLight else RomanCrimsonLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• ${result.tactics.titleRu}",
                color = RomanTextGold,
                fontSize = 11.sp
            )
        }
    }
}

private fun DrawScope.drawBattleSky(w: Float, h: Float, outcome: ExpeditionOutcome) {
    val skyColors = when (outcome) {
        ExpeditionOutcome.GREAT_VICTORY, ExpeditionOutcome.VICTORY -> listOf(
            Color(0xFF8E2800),
            Color(0xFFE65100),
            Color(0xFFFFB300)
        )
        ExpeditionOutcome.PARTIAL_SUCCESS -> listOf(
            Color(0xFF37474F),
            Color(0xFF607D8B),
            Color(0xFF90A4AE)
        )
        ExpeditionOutcome.DEFEAT, ExpeditionOutcome.DISASTER -> listOf(
            Color(0xFF1A1A1D),
            Color(0xFF4A148C),
            Color(0xFF311B92)
        )
    }

    drawRect(
        brush = Brush.verticalGradient(skyColors, startY = 0f, endY = h * 0.50f),
        topLeft = Offset.Zero,
        size = Size(w, h * 0.50f)
    )
}

private fun DrawScope.drawBattlefieldGround(w: Float, h: Float, dustClock: Float) {
    drawRect(
        color = Color(0xFF3E2723),
        topLeft = Offset(0f, h * 0.48f),
        size = Size(w, h * 0.52f)
    )

    // Swirling Battlefield Dust Motes
    for (i in 0..15) {
        val dx = (i * 35f + dustClock * 2f) % w
        val dy = h * 0.55f + (sin((dustClock + i * 10f) * 0.1f) * 18f)
        drawCircle(
            color = Color(0x33D7CCC8),
            radius = 6f + (i % 3) * 2f,
            center = Offset(dx, dy)
        )
    }
}

private fun DrawScope.drawRomanLegionFormation(
    w: Float,
    h: Float,
    tactics: Tactics,
    clashPhase: Float,
    isVictor: Boolean
) {
    val baseX = w * 0.10f
    val baseY = h * 0.52f
    val advance = if (tactics == Tactics.AGGRESSIVE) clashPhase * 18f else clashPhase * 8f

    // 1. Centurion with Aquila / Vexillum
    drawRomanLeaderWithBanner(baseX + advance + 50f, baseY - 6f, isVictor)

    // 2. Frontline Hastati / Principes with Scutums
    for (i in 0..4) {
        val rx = baseX + advance + (i * 14f)
        val ry = baseY + (i * 6f)
        drawBattleLegionary(rx, ry, isAttacking = clashPhase > 0.5f, isTestudo = tactics == Tactics.TESTUDO)
    }

    // 3. Second Line Triarii with Spears
    for (i in 0..3) {
        val rx = baseX + advance - 16f + (i * 14f)
        val ry = baseY + 8f + (i * 6f)
        drawBattleLegionary(rx, ry, isAttacking = false, hasPilum = true, isTestudo = false)
    }
}

private fun DrawScope.drawEnemyHostFormation(
    w: Float,
    h: Float,
    expedition: Expedition,
    clashPhase: Float,
    isRouted: Boolean
) {
    val baseX = w * 0.85f
    val baseY = h * 0.52f
    val charge = if (isRouted) -clashPhase * 25f else -clashPhase * 12f

    // Enemy Chieftain / Horn Blower
    drawEnemyChieftain(baseX + charge - 40f, baseY - 4f, isRouted)

    // Enemy Warriors
    for (i in 0..4) {
        val ex = baseX + charge - (i * 14f)
        val ey = baseY + (i * 6f)
        drawBarbarianWarrior(ex, ey, isAttacking = clashPhase <= 0.5f, isRouted = isRouted)
    }
}

private fun DrawScope.drawBattleLegionary(
    x: Float,
    y: Float,
    isAttacking: Boolean,
    hasPilum: Boolean = false,
    isTestudo: Boolean = false
) {
    // Montefortino Helmet
    drawRect(color = Color(0xFFD4AC0D), topLeft = Offset(x + 2f, y), size = Size(8f, 5f))
    // Armor
    drawRect(color = RomanCrimson, topLeft = Offset(x + 1f, y + 6f), size = Size(10f, 9f))
    // Scutum Red Shield
    val shieldY = if (isTestudo) y - 2f else y + 4f
    drawRect(color = RomanCrimsonLight, topLeft = Offset(x + 6f, shieldY), size = Size(6f, 15f))
    drawCircle(color = RomanGold, radius = 2.5f, center = Offset(x + 9f, shieldY + 7f))

    // Thrusting Gladius or Raised Pilum
    if (hasPilum) {
        drawLine(color = Color(0xFFB0BEC5), start = Offset(x + 2f, y + 15f), end = Offset(x + 18f, y - 8f), strokeWidth = 2f)
    } else if (isAttacking) {
        drawRect(color = Color(0xFFECEFF1), topLeft = Offset(x + 12f, y + 8f), size = Size(8f, 2f))
    }
}

private fun DrawScope.drawRomanLeaderWithBanner(x: Float, y: Float, isVictor: Boolean) {
    // Red Crest Helmet
    drawRect(color = Color(0xFFC0392B), topLeft = Offset(x + 2f, y - 4f), size = Size(10f, 3f))
    drawRect(color = RomanGold, topLeft = Offset(x + 3f, y - 1f), size = Size(8f, 5f))
    // Purple Cloak
    drawRect(color = Color(0xFF4A235A), topLeft = Offset(x, y + 5f), size = Size(12f, 12f))

    // Aquila Standard Pole
    val poleX = x + 14f
    drawLine(color = RomanGoldDark, start = Offset(poleX, y - 18f), end = Offset(poleX, y + 16f), strokeWidth = 3f)
    // Golden Eagle
    drawCircle(color = RomanGoldLight, radius = 4f, center = Offset(poleX, y - 18f))
}

private fun DrawScope.drawBarbarianWarrior(x: Float, y: Float, isAttacking: Boolean, isRouted: Boolean) {
    val skinColor = Color(0xFFEDBB99)
    val hairColor = Color(0xFFD35400)

    // Wild Hair / Horns
    drawCircle(color = hairColor, radius = 4f, center = Offset(x + 4f, y + 2f))
    // Torso / Leather Armor
    drawRect(color = Color(0xFF4E342E), topLeft = Offset(x + 1f, y + 6f), size = Size(9f, 9f))

    // Oval Green / Blue Shield
    drawRect(color = Color(0xFF1E8449), topLeft = Offset(x - 4f, y + 5f), size = Size(5f, 14f))

    // Raised Axe / Sword
    if (!isRouted && isAttacking) {
        drawLine(color = Color(0xFFCFD8DC), start = Offset(x - 2f, y + 6f), end = Offset(x - 12f, y - 2f), strokeWidth = 2.5f)
    }
}

private fun DrawScope.drawEnemyChieftain(x: Float, y: Float, isRouted: Boolean) {
    // Winged Helm
    drawRect(color = Color(0xFF78909C), topLeft = Offset(x + 2f, y), size = Size(8f, 5f))
    drawLine(color = Color.White, start = Offset(x, y - 3f), end = Offset(x + 4f, y + 1f), strokeWidth = 2f)
    drawLine(color = Color.White, start = Offset(x + 12f, y - 3f), end = Offset(x + 8f, y + 1f), strokeWidth = 2f)

    // Bear Fur Cloak
    drawRect(color = Color(0xFF3E2723), topLeft = Offset(x, y + 5f), size = Size(12f, 12f))
}

private fun DrawScope.drawProjectilesInFlight(w: Float, h: Float, flight: Float) {
    // Flying Roman Pila (from Left to Right)
    val px = w * 0.25f + flight * (w * 0.35f)
    val py = h * 0.35f + sin(flight * Math.PI.toFloat()) * -25f
    drawLine(color = Color(0xFFB0BEC5), start = Offset(px, py), end = Offset(px + 12f, py + 4f), strokeWidth = 2f)

    // Flying Barbarian Javelin (from Right to Left)
    val bx = w * 0.75f - flight * (w * 0.30f)
    val by = h * 0.38f + sin(flight * Math.PI.toFloat()) * -20f
    drawLine(color = Color(0xFF8D6E63), start = Offset(bx, by), end = Offset(bx - 10f, by + 3f), strokeWidth = 2f)
}

private fun DrawScope.drawClashImpactSparks(cx: Float, cy: Float, sparksClock: Float, clashPhase: Float) {
    if (clashPhase > 0.4f && clashPhase < 0.8f) {
        // Bright Yellow/Orange Impact Sparks
        drawCircle(color = Color(0xFFFFF176), radius = 5f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFFFF5722), radius = 3f, center = Offset(cx + 6f, cy - 4f))
        drawCircle(color = Color(0xFFFFC107), radius = 2.5f, center = Offset(cx - 5f, cy - 6f))
    }
}

private fun DrawScope.drawBattleMoraleIndicators(w: Float, h: Float, result: ExpeditionResult) {
    val barY = h * 0.90f
    val barH = 6f
    val barW = w * 0.38f

    // Roman Army Bar (Left)
    drawRect(color = Color(0x66000000), topLeft = Offset(12f, barY), size = Size(barW, barH))
    val romanPct = if (result.outcome.isSuccess) 0.90f else 0.40f
    drawRect(color = RomanGold, topLeft = Offset(12f, barY), size = Size(barW * romanPct, barH))

    // Enemy Force Bar (Right)
    val enemyStartX = w - barW - 12f
    drawRect(color = Color(0x66000000), topLeft = Offset(enemyStartX, barY), size = Size(barW, barH))
    val enemyPct = if (result.outcome.isSuccess) 0.25f else 0.85f
    drawRect(color = Color(0xFFE53935), topLeft = Offset(enemyStartX + barW * (1f - enemyPct), barY), size = Size(barW * enemyPct, barH))
}
