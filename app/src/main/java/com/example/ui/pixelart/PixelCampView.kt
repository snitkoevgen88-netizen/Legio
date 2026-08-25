package com.example.ui.pixelart

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import kotlin.random.Random

/**
 * High-Fidelity Retro-Tactical Canvas Roman Military Camp (Castra Legionis).
 * Multi-layer rendering with aqueducts, animated legionaries, forge sparks, glowing braziers,
 * and seasonal weather particle simulation.
 */
@Composable
fun PixelCampView(
    seasonYear: SeasonYear,
    buildings: List<Building>,
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    onBuildingClick: (BuildingType) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "camp_master_anim")
    
    // Animation clocks
    val marchPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "march"
    )
    val sparPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spar"
    )
    val flagPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flag"
    )
    val fireFlicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire"
    )
    val particleClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )
    val gleamPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gleam"
    )

    val principiaLevel = buildings.find { it.type == BuildingType.PRINCIPIA }?.level ?: 1
    val campusLevel = buildings.find { it.type == BuildingType.CAMPUS_MARTIUS }?.level ?: 1
    val speculaLevel = buildings.find { it.type == BuildingType.SPECULA }?.level ?: 1
    val horreumLevel = buildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
    val fabricaLevel = buildings.find { it.type == BuildingType.FABRICA }?.level ?: 1
    val valetudinariumLevel = buildings.find { it.type == BuildingType.VALETUDINARIUM }?.level ?: 1
    val aquilaLevel = buildings.find { it.type == BuildingType.AQUILA_SHRINE }?.level ?: 1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, RomanGold, RoundedCornerShape(12.dp))
            .background(RomanDarkSurface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Multi-Layer Background: Sky, Mountains, and Roman Aqueduct
            drawSkyAndMountains(w, h, seasonYear.season, gleamPhase)
            drawRomanAqueduct(w, h * 0.22f)

            // 2. Ground Terrain & Camp Fortifications (Vallum & Fossa)
            drawCampGround(w, h, seasonYear.season)
            drawFortifiedWalls(w, h, principiaLevel, flagPhase)

            // 3. Central Praetorium / Principia HQ & Aquila Shrine
            drawPrincipiaHQ(
                x = w * 0.38f,
                y = h * 0.30f,
                pw = w * 0.28f,
                ph = h * 0.35f,
                level = principiaLevel,
                aquilaLevel = aquilaLevel,
                flagPhase = flagPhase,
                gleamPhase = gleamPhase
            )

            // 4. Left Flank: Campus Martius Training Field
            drawCampusMartius(
                x = w * 0.05f,
                y = h * 0.44f,
                cw = w * 0.32f,
                ch = h * 0.38f,
                level = campusLevel,
                sparPhase = sparPhase,
                marchPhase = marchPhase
            )

            // 5. Right Flank: Specula Watchtower
            drawSpeculaWatchtower(
                x = w * 0.76f,
                y = h * 0.16f,
                tw = w * 0.18f,
                th = h * 0.44f,
                level = speculaLevel,
                flagPhase = flagPhase,
                fireFlicker = fireFlicker
            )

            // 6. Lower Right: Fabrica (Forge) with Sparks & Horreum (Granary)
            drawFabricaAndHorreum(
                x = w * 0.68f,
                y = h * 0.58f,
                fw = w * 0.28f,
                fh = h * 0.36f,
                fabricaLevel = fabricaLevel,
                horreumLevel = horreumLevel,
                fireFlicker = fireFlicker,
                particleClock = particleClock
            )

            // 7. Lower Center: Valetudinarium Medic Tent & Contubernium Tents
            drawValetudinariumAndTents(
                x = w * 0.36f,
                y = h * 0.68f,
                tw = w * 0.26f,
                th = h * 0.28f,
                level = valetudinariumLevel
            )

            // 8. Dynamic Braziers / Campfire Torches with Light Halos
            drawBrazierTorch(w * 0.34f, h * 0.48f, fireFlicker)
            drawBrazierTorch(w * 0.68f, h * 0.48f, fireFlicker)
            drawBrazierTorch(w * 0.12f, h * 0.82f, fireFlicker)

            // 9. Centurion Patrol with Red Plume
            val patrolX = (w * 0.25f + sin(marchPhase * Math.PI.toFloat()) * 40f)
            drawCrestedCenturion(patrolX, h * 0.36f, isFacingRight = marchPhase > 0.5f)

            // 10. Seasonal Weather Simulation (Snow, Autumn Leaves, Spring Petals, Summer Rays)
            drawSeasonalAtmosphere(w, h, seasonYear.season, particleClock)
        }

        // Interactive Tap Targets
        BuildingHotspotsOverlay(onBuildingClick = onBuildingClick)

        // Camp Header HUD Tag
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xE6140D0A), RoundedCornerShape(6.dp))
                .border(1.dp, RomanGold, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏛️ Castra Legio IV «Invicta»",
                color = RomanGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ${seasonYear.season.titleRu}",
                color = RomanTextGold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun BoxScope.BuildingHotspotsOverlay(
    onBuildingClick: (BuildingType) -> Unit
) {
    // Left: Campus Martius
    Box(
        modifier = Modifier
            .fillMaxHeight(0.55f)
            .fillMaxWidth(0.35f)
            .align(Alignment.BottomStart)
            .clickable { onBuildingClick(BuildingType.CAMPUS_MARTIUS) }
    )

    // Center: Principia
    Box(
        modifier = Modifier
            .fillMaxHeight(0.55f)
            .fillMaxWidth(0.35f)
            .align(Alignment.Center)
            .clickable { onBuildingClick(BuildingType.PRINCIPIA) }
    )

    // Top Right: Specula
    Box(
        modifier = Modifier
            .fillMaxHeight(0.48f)
            .fillMaxWidth(0.28f)
            .align(Alignment.TopEnd)
            .clickable { onBuildingClick(BuildingType.SPECULA) }
    )

    // Bottom Right: Horreum & Fabrica
    Box(
        modifier = Modifier
            .fillMaxHeight(0.48f)
            .fillMaxWidth(0.32f)
            .align(Alignment.BottomEnd)
            .clickable { onBuildingClick(BuildingType.FABRICA) }
    )
}

// ---------------------------------------------------------------------------------
// DETAILED DRAW SCOPE RENDER METHODS
// ---------------------------------------------------------------------------------

private fun DrawScope.drawSkyAndMountains(w: Float, h: Float, season: Season, gleamPhase: Float) {
    // Sky Gradient
    val skyColors = when (season) {
        Season.SPRING -> listOf(Color(0xFF3E6B89), Color(0xFF81C784))
        Season.SUMMER -> listOf(Color(0xFFD35400), Color(0xFFF39C12), Color(0xFFFFE082))
        Season.AUTUMN -> listOf(Color(0xFF6C3483), Color(0xFFBA4A00), Color(0xFFF5B041))
        Season.WINTER -> listOf(Color(0xFF2C3E50), Color(0xFF7F8C8D), Color(0xFFBDC3C7))
    }
    drawRect(
        brush = Brush.verticalGradient(skyColors, startY = 0f, endY = h * 0.45f),
        topLeft = Offset.Zero,
        size = Size(w, h * 0.45f)
    )

    // Distant Apennine Mountains
    val mountainPath = Path().apply {
        moveTo(0f, h * 0.32f)
        lineTo(w * 0.18f, h * 0.15f)
        lineTo(w * 0.35f, h * 0.28f)
        lineTo(w * 0.55f, h * 0.12f)
        lineTo(w * 0.78f, h * 0.26f)
        lineTo(w, h * 0.18f)
        lineTo(w, h * 0.45f)
        lineTo(0f, h * 0.45f)
        close()
    }
    val mountainColor = when (season) {
        Season.WINTER -> Color(0xFF515A5A)
        Season.SUMMER -> Color(0xFF5D4037)
        else -> Color(0xFF3E2723)
    }
    drawPath(mountainPath, color = mountainColor.copy(alpha = 0.75f))

    // Snow caps on peaks
    if (season == Season.WINTER) {
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 10f, center = Offset(w * 0.18f, h * 0.15f))
        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 12f, center = Offset(w * 0.55f, h * 0.12f))
    }
}

private fun DrawScope.drawRomanAqueduct(w: Float, y: Float) {
    val archColor = Color(0x668D6E63)
    val span = 36f
    var ax = 10f
    while (ax < w) {
        // Pillar
        drawRect(color = archColor, topLeft = Offset(ax, y), size = Size(6f, 22f))
        // Arch bridge top
        drawRect(color = archColor, topLeft = Offset(ax, y), size = Size(span, 5f))
        // Rounded arch
        drawCircle(color = archColor, radius = 6f, center = Offset(ax + span / 2f, y + 10f), style = Stroke(2f))
        ax += span
    }
}

private fun DrawScope.drawCampGround(w: Float, h: Float, season: Season) {
    val groundColor = when (season) {
        Season.SPRING -> Color(0xFF2E5A1E)
        Season.SUMMER -> Color(0xFF6E561E)
        Season.AUTUMN -> Color(0xFF543818)
        Season.WINTER -> Color(0xFFB0BEC5)
    }
    drawRect(color = groundColor, topLeft = Offset(0f, h * 0.32f), size = Size(w, h * 0.68f))

    // Via Principalis & Via Praetoria (Stone & Dirt Roads)
    val roadColor = Color(0xFF4A3525)
    val roadBorder = Color(0xFF332215)
    // Horizontal main road
    drawRect(color = roadColor, topLeft = Offset(0f, h * 0.52f), size = Size(w, h * 0.10f))
    drawLine(color = roadBorder, start = Offset(0f, h * 0.52f), end = Offset(w, h * 0.52f), strokeWidth = 2f)
    drawLine(color = roadBorder, start = Offset(0f, h * 0.62f), end = Offset(w, h * 0.62f), strokeWidth = 2f)

    // Vertical cardo road
    drawRect(color = roadColor, topLeft = Offset(w * 0.46f, h * 0.32f), size = Size(w * 0.08f, h * 0.68f))
}

private fun DrawScope.drawFortifiedWalls(w: Float, h: Float, level: Int, flagPhase: Float) {
    val wallColor = if (level >= 3) Color(0xFF78909C) else Color(0xFF5D4037)
    val stakeColor = if (level >= 3) Color(0xFFB0BEC5) else Color(0xFF8D6E63)

    // Vallum rampart line
    drawRect(color = wallColor, topLeft = Offset(0f, h * 0.28f), size = Size(w, h * 0.05f))

    // Palisade Stakes / Battlements
    var x = 4f
    while (x < w) {
        drawRect(color = stakeColor, topLeft = Offset(x, h * 0.24f), size = Size(7f, h * 0.05f))
        x += 14f
    }
}

private fun DrawScope.drawPrincipiaHQ(
    x: Float,
    y: Float,
    pw: Float,
    ph: Float,
    level: Int,
    aquilaLevel: Int,
    flagPhase: Float,
    gleamPhase: Float
) {
    // Praetorium HQ Foundation
    val wallColor = if (level >= 3) Color(0xFFECEFF1) else Color(0xFFEFEBE9)
    val roofColor = if (level >= 3) RomanCrimsonDark else RomanCrimson

    // Pediment Roof
    val pediment = Path().apply {
        moveTo(x + pw * 0.5f, y)
        lineTo(x + pw + 6f, y + ph * 0.40f)
        lineTo(x - 6f, y + ph * 0.40f)
        close()
    }
    drawPath(pediment, color = roofColor)
    drawPath(pediment, color = RomanGold, style = Stroke(2f))

    // Marble Colonnade
    drawRect(color = wallColor, topLeft = Offset(x, y + ph * 0.40f), size = Size(pw, ph * 0.60f))

    // 4 Golden Pillars
    val pillarCount = 4
    val pSpacing = pw / (pillarCount + 1)
    for (i in 1..pillarCount) {
        val px = x + (i * pSpacing) - 3f
        drawRect(color = RomanGold, topLeft = Offset(px, y + ph * 0.40f), size = Size(6f, ph * 0.60f))
    }

    // Portal / Doorway
    drawRect(color = Color(0xFF1B120E), topLeft = Offset(x + pw * 0.38f, y + ph * 0.55f), size = Size(pw * 0.24f, ph * 0.45f))

    // Golden Eagle Standard (Aquila) above HQ
    val poleX = x + pw * 0.5f
    val poleTopY = y - 28f
    drawRect(color = RomanGoldDark, topLeft = Offset(poleX - 2f, poleTopY), size = Size(4f, 28f))
    
    // Golden Aquila Eagle with dynamic gleam
    drawCircle(color = RomanGoldLight, radius = 6f, center = Offset(poleX, poleTopY))
    if (gleamPhase > 0.6f) {
        drawCircle(color = Color.White, radius = 3f, center = Offset(poleX, poleTopY))
    }

    // Vexillum Crimson Banner waving
    val wave = flagPhase * 6f
    val bannerPath = Path().apply {
        moveTo(poleX + 2f, poleTopY + 2f)
        lineTo(poleX + 26f + wave, poleTopY + 2f)
        lineTo(poleX + 22f + wave, poleTopY + 18f)
        lineTo(poleX + 2f, poleTopY + 18f)
        close()
    }
    drawPath(bannerPath, color = RomanCrimsonLight)
    // Gold SPQR letters
    drawRect(color = RomanGold, topLeft = Offset(poleX + 6f + wave * 0.5f, poleTopY + 6f), size = Size(10f, 6f))
}

private fun DrawScope.drawCampusMartius(
    x: Float,
    y: Float,
    cw: Float,
    ch: Float,
    level: Int,
    sparPhase: Float,
    marchPhase: Float
) {
    // Training ground arena
    drawRect(color = Color(0xFF4E3629), topLeft = Offset(x, y), size = Size(cw, ch))
    drawRect(color = RomanGoldDark.copy(alpha = 0.6f), topLeft = Offset(x, y), size = Size(cw, ch), style = Stroke(2f))

    // Wooden Quintain Dummies
    drawRect(color = Color(0xFF6D4C41), topLeft = Offset(x + cw * 0.15f, y + ch * 0.15f), size = Size(6f, 22f))
    drawCircle(color = Color(0xFFD7CCC8), radius = 5f, center = Offset(x + cw * 0.15f + 3f, y + ch * 0.15f))
    // Shield on dummy
    drawRect(color = Color(0xFF8D6E63), topLeft = Offset(x + cw * 0.15f - 4f, y + ch * 0.22f), size = Size(14f, 10f))

    // Animated Sparring Legionaries!
    val sparX1 = x + cw * 0.42f + (sparPhase * 8f)
    val sparX2 = x + cw * 0.72f - (sparPhase * 6f)
    val sparY = y + ch * 0.45f

    drawPixelLegionary(sparX1, sparY, isAttacking = sparPhase > 0.5f, hasShield = true, isRightFacing = true)
    drawPixelLegionary(sparX2, sparY, isAttacking = sparPhase <= 0.5f, hasShield = true, isRightFacing = false)

    // Formed Maniple Drilling in the Back
    for (i in 0..2) {
        val mx = x + cw * 0.35f + (i * 18f)
        val my = y + ch * 0.18f + (if (i % 2 == 0) marchPhase * 3f else -marchPhase * 3f)
        drawPixelLegionary(mx, my, isAttacking = false, hasShield = true, isRightFacing = true, scale = 0.75f)
    }
}

private fun DrawScope.drawPixelLegionary(
    x: Float,
    y: Float,
    isAttacking: Boolean,
    hasShield: Boolean,
    isRightFacing: Boolean,
    scale: Float = 1.0f
) {
    val s = scale
    // Bronze Montefortino Helmet
    drawRect(color = Color(0xFFD4AC0D), topLeft = Offset(x + 2f * s, y), size = Size(8f * s, 6f * s))
    // Red Tunic & Armor
    drawRect(color = RomanCrimson, topLeft = Offset(x + 1f * s, y + 8f * s), size = Size(10f * s, 10f * s))
    // Face
    drawRect(color = Color(0xFFF5CBA7), topLeft = Offset(x + 3f * s, y + 5f * s), size = Size(6f * s, 4f * s))
    // Legs & Caligae
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(x + 2f * s, y + 18f * s), size = Size(3f * s, 6f * s))
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(x + 7f * s, y + 18f * s), size = Size(3f * s, 6f * s))

    // Scutum Red Shield
    if (hasShield) {
        val shieldX = if (isRightFacing) x + 8f * s else x - 5f * s
        drawRect(color = RomanCrimsonLight, topLeft = Offset(shieldX, y + 6f * s), size = Size(6f * s, 16f * s))
        drawCircle(color = RomanGold, radius = 2.5f * s, center = Offset(shieldX + 3f * s, y + 14f * s))
    }

    // Gladius Thrust
    if (isAttacking) {
        val swordX = if (isRightFacing) x + 14f * s else x - 10f * s
        drawRect(color = Color(0xFFECEFF1), topLeft = Offset(swordX, y + 10f * s), size = Size(8f * s, 2.5f * s))
    }
}

private fun DrawScope.drawCrestedCenturion(x: Float, y: Float, isFacingRight: Boolean) {
    // Red Transverse Crest
    drawRect(color = Color(0xFFC0392B), topLeft = Offset(x + 1f, y - 4f), size = Size(12f, 4f))
    // Helmet
    drawRect(color = RomanGold, topLeft = Offset(x + 3f, y), size = Size(8f, 6f))
    // Face
    drawRect(color = Color(0xFFF5CBA7), topLeft = Offset(x + 4f, y + 5f), size = Size(6f, 4f))
    // Lorica Segmentata & Purple Cloak
    drawRect(color = Color(0xFF4A235A), topLeft = Offset(x, y + 8f), size = Size(14f, 12f))
    // Gladius at hip
    drawRect(color = RomanGoldLight, topLeft = Offset(if (isFacingRight) x + 12f else x - 4f, y + 10f), size = Size(4f, 10f))
}

private fun DrawScope.drawSpeculaWatchtower(
    x: Float,
    y: Float,
    tw: Float,
    th: Float,
    level: Int,
    flagPhase: Float,
    fireFlicker: Float
) {
    val timberColor = if (level >= 3) Color(0xFF78909C) else Color(0xFF5D4037)
    // Tower 4 Pillars
    drawRect(color = timberColor, topLeft = Offset(x + tw * 0.15f, y + th * 0.35f), size = Size(6f, th * 0.65f))
    drawRect(color = timberColor, topLeft = Offset(x + tw * 0.75f, y + th * 0.35f), size = Size(6f, th * 0.65f))
    // Cross beams
    drawLine(color = timberColor, start = Offset(x + tw * 0.15f, y + th * 0.75f), end = Offset(x + tw * 0.75f, y + th * 0.45f), strokeWidth = 3f)
    drawLine(color = timberColor, start = Offset(x + tw * 0.15f, y + th * 0.45f), end = Offset(x + tw * 0.75f, y + th * 0.75f), strokeWidth = 3f)

    // Platform & Lookout House
    drawRect(color = Color(0xFF795548), topLeft = Offset(x, y + th * 0.20f), size = Size(tw, th * 0.18f))
    
    // Roof
    val roof = Path().apply {
        moveTo(x + tw * 0.5f, y)
        lineTo(x + tw + 4f, y + th * 0.20f)
        lineTo(x - 4f, y + th * 0.20f)
        close()
    }
    drawPath(roof, color = RomanCrimsonDark)

    // Sentry on Lookout
    drawCircle(color = Color(0xFFF5CBA7), radius = 3.5f, center = Offset(x + tw * 0.45f, y + th * 0.18f))
    drawRect(color = RomanGold, topLeft = Offset(x + tw * 0.42f, y + th * 0.14f), size = Size(6f, 3f))

    // Warning Fire Beacon
    drawCircle(color = Color(0xFFFF5722).copy(alpha = 0.8f), radius = 4f * fireFlicker, center = Offset(x + tw * 0.85f, y + th * 0.18f))
}

private fun DrawScope.drawFabricaAndHorreum(
    x: Float,
    y: Float,
    fw: Float,
    fh: Float,
    fabricaLevel: Int,
    horreumLevel: Int,
    fireFlicker: Float,
    particleClock: Float
) {
    // Horreum Granary on Left
    val granaryW = fw * 0.45f
    drawRect(color = Color(0xFF8D6E63), topLeft = Offset(x, y + fh * 0.25f), size = Size(granaryW, fh * 0.75f))
    // Sacks & Amphorae
    drawCircle(color = Color(0xFFD7CCC8), radius = 5f, center = Offset(x + 6f, y + fh - 6f))
    drawCircle(color = Color(0xFFD7CCC8), radius = 5f, center = Offset(x + 14f, y + fh - 6f))
    drawRect(color = Color(0xFFBCAAA4), topLeft = Offset(x + 20f, y + fh - 12f), size = Size(6f, 12f))

    // Fabrica Forge on Right
    val forgeX = x + fw * 0.52f
    val forgeW = fw * 0.48f
    drawRect(color = Color(0xFF37474F), topLeft = Offset(forgeX, y + fh * 0.30f), size = Size(forgeW, fh * 0.70f))
    
    // Chimney & Smoke
    val chimneyX = forgeX + 6f
    drawRect(color = Color(0xFF263238), topLeft = Offset(chimneyX, y + fh * 0.05f), size = Size(10f, fh * 0.30f))
    
    // Animated Smoke Particles
    for (i in 0..4) {
        val sOffset = (particleClock * 0.5f + i * 25f) % 60f
        val smokeY = y + fh * 0.05f - sOffset
        val smokeX = chimneyX + 5f + sin(smokeY * 0.1f) * 6f
        drawCircle(
            color = Color.White.copy(alpha = (1f - sOffset / 60f) * 0.45f),
            radius = 3f + (sOffset * 0.15f),
            center = Offset(smokeX, smokeY)
        )
    }

    // Glowing Hearth Fire & Anvil
    val anvilX = forgeX + forgeW * 0.45f
    drawRect(color = Color(0xFF212121), topLeft = Offset(anvilX, y + fh * 0.65f), size = Size(12f, 10f))
    drawCircle(color = Color(0xFFFF5722), radius = 4f * fireFlicker, center = Offset(anvilX + 6f, y + fh * 0.65f))
    // Forge Sparks flying
    val sparkOffset = (particleClock * 1.5f) % 20f
    drawCircle(color = RomanGoldLight, radius = 2f, center = Offset(anvilX + 4f - sparkOffset * 0.3f, y + fh * 0.60f - sparkOffset))
    drawCircle(color = Color(0xFFFFAB00), radius = 1.5f, center = Offset(anvilX + 8f + sparkOffset * 0.4f, y + fh * 0.62f - sparkOffset * 0.8f))
}

private fun DrawScope.drawValetudinariumAndTents(
    x: Float,
    y: Float,
    tw: Float,
    th: Float,
    level: Int
) {
    // Large Medic Tent
    val tent = Path().apply {
        moveTo(x + tw * 0.45f, y)
        lineTo(x + tw * 0.90f, y + th)
        lineTo(x, y + th)
        close()
    }
    drawPath(tent, color = Color(0xFFF5EBE1))
    drawPath(tent, color = RomanGoldDark, style = Stroke(1.5f))
    
    // Crimson Rod / Cross Symbol
    drawRect(color = RomanCrimsonLight, topLeft = Offset(x + tw * 0.42f, y + th * 0.35f), size = Size(tw * 0.08f, th * 0.40f))
    drawRect(color = RomanCrimsonLight, topLeft = Offset(x + tw * 0.32f, y + th * 0.50f), size = Size(tw * 0.28f, th * 0.12f))
}

private fun DrawScope.drawBrazierTorch(x: Float, y: Float, fireFlicker: Float) {
    // Bronze stand
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(x - 2f, y), size = Size(4f, 14f))
    drawRect(color = Color(0xFF8D6E63), topLeft = Offset(x - 6f, y - 2f), size = Size(12f, 4f))

    // Radial Light Halo on Ground
    drawCircle(
        color = Color(0x33FF9800),
        radius = 24f * fireFlicker,
        center = Offset(x, y - 4f)
    )
    // Fire core
    drawCircle(color = Color(0xFFFF5722), radius = 5f * fireFlicker, center = Offset(x, y - 4f))
    drawCircle(color = Color(0xFFFFEB3B), radius = 2.5f * fireFlicker, center = Offset(x, y - 4f))
}

private fun DrawScope.drawSeasonalAtmosphere(w: Float, h: Float, season: Season, clock: Float) {
    val random = Random(123)
    when (season) {
        Season.WINTER -> {
            // Snowstorm with wind angle
            for (i in 0..35) {
                val speed = 1f + (i % 3) * 0.5f
                val sx = (random.nextFloat() * w + clock * speed * 0.8f) % w
                val sy = (random.nextFloat() * h + clock * speed * 1.6f) % h
                drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 2.5f + (i % 2), center = Offset(sx, sy))
            }
        }
        Season.AUTUMN -> {
            // Golden & Crimson Leaves fluttering
            val colors = listOf(Color(0xFFE67E22), Color(0xFFD35400), Color(0xFFC0392B), Color(0xFFF39C12))
            for (i in 0..24) {
                val lx = (random.nextFloat() * w + clock * 0.6f + sin((clock + i * 20) * 0.05f) * 15f) % w
                val ly = (random.nextFloat() * h + clock * 0.9f) % h
                drawCircle(color = colors[i % colors.size], radius = 3f, center = Offset(lx, ly))
            }
        }
        Season.SPRING -> {
            // Cherry / Laurel blossom petals
            for (i in 0..18) {
                val px = (random.nextFloat() * w + clock * 0.4f + sin((clock + i * 15) * 0.04f) * 12f) % w
                val py = (random.nextFloat() * h + clock * 0.7f) % h
                drawCircle(color = Color(0xFFFF80AB).copy(alpha = 0.75f), radius = 2.8f, center = Offset(px, py))
            }
        }
        Season.SUMMER -> {
            // Sun Rays / Lens flare
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x55FFF59D), Color(0x00FFF59D)),
                    center = Offset(w * 0.85f, h * 0.15f),
                    radius = 120f
                ),
                radius = 120f,
                center = Offset(w * 0.85f, h * 0.15f)
            )
        }
    }
}
