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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Top-Down Roman Military Camp Plan (Forma Castrorum / Castra Stativa).
 * Features classical playing-card geometry, Fossa, Agger, Vallum, 4 Cardinal Gates,
 * Via Principalis, Via Praetoria, Intervallum, Principia HQ, Horreum, Fabrica,
 * Valetudinarium, Contubernia tent rows, and animated patrolling legionaries.
 */
@Composable
fun PixelCampTopDownView(
    seasonYear: SeasonYear,
    buildings: List<Building>,
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    onBuildingClick: (BuildingType) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "castra_topdown_anim")

    // Animation clocks
    val patrolClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "patrol"
    )

    val fireFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire"
    )

    val eagleGleam by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eagle_gleam"
    )

    val smokeClock by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smoke"
    )

    val principiaLevel = buildings.find { it.type == BuildingType.PRINCIPIA }?.level ?: 1
    val campusLevel = buildings.find { it.type == BuildingType.CAMPUS_MARTIUS }?.level ?: 1
    val speculaLevel = buildings.find { it.type == BuildingType.SPECULA }?.level ?: 1
    val horreumLevel = buildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
    val fabricaLevel = buildings.find { it.type == BuildingType.FABRICA }?.level ?: 1
    val valetudinariumLevel = buildings.find { it.type == BuildingType.VALETUDINARIUM }?.level ?: 1
    val aquilaLevel = buildings.find { it.type == BuildingType.AQUILA_SHRINE }?.level ?: 1

    var activeHoverBuilding by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, RomanGold, RoundedCornerShape(12.dp))
            .background(RomanDarkSurface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Map Parchment & Surrounding Italian Terrain
            drawParchmentTerrain(w, h, seasonYear.season)

            // 2. Fortifications: Fossa (Ditch), Agger (Rampart), and Vallum (Palisade / Walls)
            val campLeft = w * 0.08f
            val campTop = h * 0.10f
            val campW = w * 0.84f
            val campH = h * 0.80f
            val cornerRadius = 24f

            drawFortifiedPerimeter(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                cornerRadius = cornerRadius,
                principiaLevel = principiaLevel,
                season = seasonYear.season
            )

            // 3. The 4 Cardinal Roman Gates (Portae)
            drawRomanGates(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                fireFlicker = fireFlicker,
                speculaLevel = speculaLevel
            )

            // 4. Roman Military Street Grid: Intervallum, Via Principalis, Via Praetoria, Via Decumana
            drawStreetGrid(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                cornerRadius = cornerRadius
            )

            // 5. Central Headquarters: PRINCIPIA & AEDES AQUILAE (Shrine of the Golden Eagle)
            drawPrincipiaComplex(
                w = w,
                h = h,
                principiaLevel = principiaLevel,
                aquilaLevel = aquilaLevel,
                eagleGleam = eagleGleam,
                fireFlicker = fireFlicker
            )

            // 6. Western Wing (Left): CAMPUS MARTIUS & TENT ROWS (Contubernia)
            drawCampusMartiusAndBarracks(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                campusLevel = campusLevel,
                season = seasonYear.season
            )

            // 7. Eastern Wing (Right): HORREUM (Granaries), FABRICA (Forge), VALETUDINARIUM (Hospital)
            drawWorkshopsAndLogistics(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                horreumLevel = horreumLevel,
                fabricaLevel = fabricaLevel,
                valetudinariumLevel = valetudinariumLevel,
                fireFlicker = fireFlicker,
                smokeClock = smokeClock
            )

            // 8. 4 Corner Watchtowers (Specula / Turres)
            drawCornerWatchtowers(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                speculaLevel = speculaLevel,
                fireFlicker = fireFlicker
            )

            // 9. Animated Patrolling Legionaries along Via Principalis and Intervallum
            drawPatrols(
                campLeft = campLeft,
                campTop = campTop,
                campW = campW,
                campH = campH,
                patrolClock = patrolClock
            )

            // 10. Authentic Roman Compass Rose & Cartographic Accents
            drawRomanCompassRose(w * 0.91f, h * 0.18f)
        }

        // Interactive Building Tap Overlay
        TopDownHotspotsOverlay(
            onBuildingClick = onBuildingClick,
            onHover = { activeHoverBuilding = it }
        )

        // Top Header Banner with Latin Motto
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xF0150D09), RoundedCornerShape(6.dp))
                .border(1.dp, RomanGold, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🦅 CASTRA STATIVA • FORMA",
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

        // Bottom Street Labels Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
                .background(Color(0xE6140D0A), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🏛️ Нажмите на здание для развития", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BoxScope.TopDownHotspotsOverlay(
    onBuildingClick: (BuildingType) -> Unit,
    onHover: (String?) -> Unit
) {
    // 1. Principia (Center)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.32f)
            .fillMaxHeight(0.38f)
            .align(Alignment.Center)
            .clickable { onBuildingClick(BuildingType.PRINCIPIA) }
    )

    // 2. Campus Martius / Drill Ground (Left Center)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.28f)
            .fillMaxHeight(0.34f)
            .align(Alignment.CenterStart)
            .offset(x = 24.dp)
            .clickable { onBuildingClick(BuildingType.CAMPUS_MARTIUS) }
    )

    // 3. Fabrica (Bottom Right)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.28f)
            .fillMaxHeight(0.32f)
            .align(Alignment.BottomEnd)
            .offset(x = (-20).dp, y = (-20).dp)
            .clickable { onBuildingClick(BuildingType.FABRICA) }
    )

    // 4. Horreum (Top Right)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.28f)
            .fillMaxHeight(0.30f)
            .align(Alignment.TopEnd)
            .offset(x = (-20).dp, y = 24.dp)
            .clickable { onBuildingClick(BuildingType.HORREUM) }
    )

    // 5. Specula (Top Left / Watchtowers)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.20f)
            .fillMaxHeight(0.24f)
            .align(Alignment.TopStart)
            .offset(x = 16.dp, y = 16.dp)
            .clickable { onBuildingClick(BuildingType.SPECULA) }
    )
}

// ---------------------------------------------------------------------------------
// DRAW METHODS FOR AUTHENTIC TOP-DOWN CASTRA
// ---------------------------------------------------------------------------------

private fun DrawScope.drawParchmentTerrain(w: Float, h: Float, season: Season) {
    val bgColors = when (season) {
        Season.SPRING -> listOf(Color(0xFF2E4018), Color(0xFF1E2B10))
        Season.SUMMER -> listOf(Color(0xFF422F18), Color(0xFF2E1F10))
        Season.AUTUMN -> listOf(Color(0xFF382314), Color(0xFF26160C))
        Season.WINTER -> listOf(Color(0xFF37474F), Color(0xFF263238))
    }

    drawRect(
        brush = Brush.radialGradient(bgColors, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.7f),
        topLeft = Offset.Zero,
        size = Size(w, h)
    )

    // Parchment grid lines / Surveying cardo
    val gridColor = RomanGoldDark.copy(alpha = 0.08f)
    var x = 0f
    while (x < w) {
        drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
        x += 20f
    }
    var y = 0f
    while (y < h) {
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
        y += 20f
    }
}

private fun DrawScope.drawFortifiedPerimeter(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    cornerRadius: Float,
    principiaLevel: Int,
    season: Season
) {
    // 1. Fossa (Defensive Double Ditch outside)
    val ditchOffset = 10f
    val fossaRect = RoundRect(
        rect = Rect(
            left = campLeft - ditchOffset,
            top = campTop - ditchOffset,
            right = campLeft + campW + ditchOffset,
            bottom = campTop + campH + ditchOffset
        ),
        cornerRadius = CornerRadius(cornerRadius + 6f, cornerRadius + 6f)
    )
    val fossaPath = Path().apply { addRoundRect(fossaRect) }
    val ditchColor = if (season == Season.WINTER) Color(0xFF455A64) else Color(0xFF1B120C)
    drawPath(fossaPath, color = ditchColor, style = Stroke(width = 8f))

    // 2. Agger (Earth Rampart Mound)
    val aggerRect = RoundRect(
        rect = Rect(
            left = campLeft - 3f,
            top = campTop - 3f,
            right = campLeft + campW + 3f,
            bottom = campTop + campH + 3f
        ),
        cornerRadius = CornerRadius(cornerRadius + 2f, cornerRadius + 2f)
    )
    val aggerPath = Path().apply { addRoundRect(aggerRect) }
    drawPath(aggerPath, color = Color(0xFF5D4037), style = Stroke(width = 6f))

    // 3. Camp Interior Ground (Castra Sand / Clay)
    val interiorRect = RoundRect(
        rect = Rect(left = campLeft, top = campTop, right = campLeft + campW, bottom = campTop + campH),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
    val interiorPath = Path().apply { addRoundRect(interiorRect) }
    val groundColor = when (season) {
        Season.WINTER -> Color(0xFF546E7A)
        Season.SPRING -> Color(0xFF3E2D1F)
        else -> Color(0xFF3E2723)
    }
    drawPath(interiorPath, color = groundColor)

    // 4. Vallum (Timber / Stone Palisade with Merlons)
    val wallColor = if (principiaLevel >= 3) Color(0xFFCFD8DC) else Color(0xFF8D6E63)
    drawPath(interiorPath, color = wallColor, style = Stroke(width = 3.5f))
}

private fun DrawScope.drawRomanGates(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    fireFlicker: Float,
    speculaLevel: Int
) {
    val gateTimber = Color(0xFF3E2723)
    val gateTowerColor = if (speculaLevel >= 3) Color(0xFF90A4AE) else Color(0xFF6D4C41)
    val gateWidth = 32f

    // 1. PORTA PRAETORIA (Top / North Gate)
    val topGateX = campLeft + (campW * 0.5f) - (gateWidth / 2f)
    val topGateY = campTop - 4f
    // Breach opening in wall
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(topGateX, topGateY), size = Size(gateWidth, 8f))
    // Twin Gate Towers
    drawRect(color = gateTowerColor, topLeft = Offset(topGateX - 10f, topGateY - 4f), size = Size(10f, 16f))
    drawRect(color = gateTowerColor, topLeft = Offset(topGateX + gateWidth, topGateY - 4f), size = Size(10f, 16f))
    drawCircle(color = Color(0xFFFF5722), radius = 2.5f * fireFlicker, center = Offset(topGateX - 5f, topGateY + 4f))
    drawCircle(color = Color(0xFFFF5722), radius = 2.5f * fireFlicker, center = Offset(topGateX + gateWidth + 5f, topGateY + 4f))

    // 2. PORTA DECUMANA (Bottom / South Gate)
    val botGateX = campLeft + (campW * 0.5f) - (gateWidth / 2f)
    val botGateY = campTop + campH - 4f
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(botGateX, botGateY), size = Size(gateWidth, 8f))
    drawRect(color = gateTowerColor, topLeft = Offset(botGateX - 10f, botGateY - 6f), size = Size(10f, 16f))
    drawRect(color = gateTowerColor, topLeft = Offset(botGateX + gateWidth, botGateY - 6f), size = Size(10f, 16f))

    // 3. PORTA PRINCIPALIS SINISTRA (Left / West Gate)
    val leftGateX = campLeft - 4f
    val leftGateY = campTop + (campH * 0.42f) - (gateWidth / 2f)
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(leftGateX, leftGateY), size = Size(8f, gateWidth))
    drawRect(color = gateTowerColor, topLeft = Offset(leftGateX - 6f, leftGateY - 8f), size = Size(16f, 8f))
    drawRect(color = gateTowerColor, topLeft = Offset(leftGateX - 6f, leftGateY + gateWidth), size = Size(16f, 8f))

    // 4. PORTA PRINCIPALIS DEXTRA (Right / East Gate)
    val rightGateX = campLeft + campW - 4f
    val rightGateY = campTop + (campH * 0.42f) - (gateWidth / 2f)
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(rightGateX, rightGateY), size = Size(8f, gateWidth))
    drawRect(color = gateTowerColor, topLeft = Offset(rightGateX - 6f, rightGateY - 8f), size = Size(16f, 8f))
    drawRect(color = gateTowerColor, topLeft = Offset(rightGateX - 6f, rightGateY + gateWidth), size = Size(16f, 8f))
}

private fun DrawScope.drawStreetGrid(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    cornerRadius: Float
) {
    val roadColor = Color(0xFF4A3425)
    val roadBorder = Color(0xFF332215)
    val intervallumWidth = 14f

    // 1. Intervallum (Perimeter Road around entire camp)
    val intervallumRect = RoundRect(
        rect = Rect(
            left = campLeft + intervallumWidth * 0.5f,
            top = campTop + intervallumWidth * 0.5f,
            right = campLeft + campW - intervallumWidth * 0.5f,
            bottom = campTop + campH - intervallumWidth * 0.5f
        ),
        cornerRadius = CornerRadius(cornerRadius - 6f, cornerRadius - 6f)
    )
    val intervallumPath = Path().apply { addRoundRect(intervallumRect) }
    drawPath(intervallumPath, color = roadColor.copy(alpha = 0.65f), style = Stroke(width = intervallumWidth))

    // 2. Via Principalis (Main Transverse Avenue - West to East)
    val viaPrinY = campTop + (campH * 0.42f) - 10f
    drawRect(
        color = roadColor,
        topLeft = Offset(campLeft + intervallumWidth, viaPrinY),
        size = Size(campW - (intervallumWidth * 2f), 20f)
    )
    drawLine(color = roadBorder, start = Offset(campLeft, viaPrinY), end = Offset(campLeft + campW, viaPrinY), strokeWidth = 1.5f)
    drawLine(color = roadBorder, start = Offset(campLeft, viaPrinY + 20f), end = Offset(campLeft + campW, viaPrinY + 20f), strokeWidth = 1.5f)

    // 3. Via Praetoria (North Gate to Principia)
    val viaPraetX = campLeft + (campW * 0.5f) - 10f
    drawRect(
        color = roadColor,
        topLeft = Offset(viaPraetX, campTop + intervallumWidth),
        size = Size(20f, (campH * 0.42f) - intervallumWidth)
    )

    // 4. Via Decumana (Principia to South Gate)
    drawRect(
        color = roadColor,
        topLeft = Offset(viaPraetX, campTop + (campH * 0.70f)),
        size = Size(20f, (campH * 0.30f) - intervallumWidth)
    )

    // 5. Groma Surveying Center Marker
    val gromaX = campLeft + (campW * 0.5f)
    val gromaY = campTop + (campH * 0.42f)
    drawCircle(color = RomanGold, radius = 5f, center = Offset(gromaX, gromaY))
    drawLine(color = RomanCrimson, start = Offset(gromaX - 8f, gromaY), end = Offset(gromaX + 8f, gromaY), strokeWidth = 2f)
    drawLine(color = RomanCrimson, start = Offset(gromaX, gromaY - 8f), end = Offset(gromaX, gromaY + 8f), strokeWidth = 2f)
}

private fun DrawScope.drawPrincipiaComplex(
    w: Float,
    h: Float,
    principiaLevel: Int,
    aquilaLevel: Int,
    eagleGleam: Float,
    fireFlicker: Float
) {
    val px = w * 0.38f
    val py = h * 0.45f
    val pw = w * 0.24f
    val ph = h * 0.24f

    // 1. Principia Compound Base (Marble & Stone)
    val marbleColor = if (principiaLevel >= 3) Color(0xFFECEFF1) else Color(0xFFE0D8D0)
    drawRect(color = marbleColor, topLeft = Offset(px, py), size = Size(pw, ph))
    drawRect(color = RomanGold, topLeft = Offset(px, py), size = Size(pw, ph), style = Stroke(2f))

    // 2. Central Peristyle Courtyard (Atrium with Porticus Colonnade)
    val courtyardX = px + (pw * 0.15f)
    val courtyardY = py + (ph * 0.15f)
    val courtyardW = pw * 0.70f
    val courtyardH = ph * 0.50f
    drawRect(color = Color(0xFF3E2723), topLeft = Offset(courtyardX, courtyardY), size = Size(courtyardW, courtyardH))
    
    // Golden Columns
    for (i in 0..4) {
        val cx = courtyardX + (i * (courtyardW / 4f))
        drawCircle(color = RomanGold, radius = 2.5f, center = Offset(cx, courtyardY + 2f))
        drawCircle(color = RomanGold, radius = 2.5f, center = Offset(cx, courtyardY + courtyardH - 2f))
    }

    // 3. Aedes Principiorum (Sacred Shrine of the Legion Standard / Aquila)
    val aedesX = px + (pw * 0.35f)
    val aedesY = py + (ph * 0.68f)
    val aedesW = pw * 0.30f
    val aedesH = ph * 0.30f
    drawRect(color = RomanCrimsonDark, topLeft = Offset(aedesX, aedesY), size = Size(aedesW, aedesH))
    drawRect(color = RomanGold, topLeft = Offset(aedesX, aedesY), size = Size(aedesW, aedesH), style = Stroke(1.5f))

    // Golden Eagle (Aquila) Standard with Gleam
    val eagleCenter = Offset(aedesX + (aedesW * 0.5f), aedesY + (aedesH * 0.5f))
    drawCircle(color = RomanGoldLight, radius = 4f, center = eagleCenter)
    if (eagleGleam > 0.65f) {
        drawCircle(color = Color.White, radius = 2.5f, center = eagleCenter)
    }

    // Braziers at Principia entrance
    drawCircle(color = Color(0xFFFF5722), radius = 3f * fireFlicker, center = Offset(px + 6f, py + 6f))
    drawCircle(color = Color(0xFFFF5722), radius = 3f * fireFlicker, center = Offset(px + pw - 6f, py + 6f))

    // HQ Text Tag
    drawRect(color = RomanCrimson, topLeft = Offset(px + pw * 0.20f, py + 4f), size = Size(pw * 0.60f, 7f))
    drawRect(color = RomanGold, topLeft = Offset(px + pw * 0.25f, py + 5.5f), size = Size(pw * 0.50f, 4f))
}

private fun DrawScope.drawCampusMartiusAndBarracks(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    campusLevel: Int,
    season: Season
) {
    val cmX = campLeft + 24f
    val cmY = campTop + 24f
    val cmW = campW * 0.32f
    val cmH = campH * 0.30f

    // 1. Campus Martius Drill Field Ground
    drawRect(color = Color(0xFF4A3525), topLeft = Offset(cmX, cmY), size = Size(cmW, cmH))
    drawRect(color = RomanGoldDark, topLeft = Offset(cmX, cmY), size = Size(cmW, cmH), style = Stroke(1.5f))

    // Quintain Dummies & Target Hurdles
    drawCircle(color = Color(0xFFD7CCC8), radius = 3f, center = Offset(cmX + 16f, cmY + 16f))
    drawCircle(color = Color(0xFFD7CCC8), radius = 3f, center = Offset(cmX + 16f, cmY + 32f))
    drawRect(color = Color(0xFF8D6E63), topLeft = Offset(cmX + 32f, cmY + 12f), size = Size(14f, 4f))

    // Drilling Maniple (Tiny Cohort Soldiers in formation)
    for (row in 0..2) {
        for (col in 0..4) {
            val sx = cmX + 60f + (col * 8f)
            val sy = cmY + 12f + (row * 8f)
            // Bronze helm & Red scutum
            drawCircle(color = RomanGold, radius = 2f, center = Offset(sx, sy))
            drawRect(color = RomanCrimson, topLeft = Offset(sx - 1f, sy + 1f), size = Size(4f, 2f))
        }
    }

    // 2. Contubernia (Leather Barrack Tents - Strigae) in South-West Wing
    val tentBlockX = campLeft + 24f
    val tentBlockY = campTop + (campH * 0.48f)
    val tentW = 16f
    val tentH = 12f

    val tentColor = when (season) {
        Season.WINTER -> Color(0xFF8D8D8D)
        else -> Color(0xFF8D6E63)
    }

    for (row in 0..3) {
        for (col in 0..3) {
            val tx = tentBlockX + (col * (tentW + 8f))
            val ty = tentBlockY + (row * (tentH + 6f))

            // Leather Papilio Tent
            drawRect(color = tentColor, topLeft = Offset(tx, ty), size = Size(tentW, tentH))
            // Ridge seam
            drawLine(color = Color(0xFF5D4037), start = Offset(tx, ty + tentH * 0.5f), end = Offset(tx + tentW, ty + tentH * 0.5f), strokeWidth = 1.5f)
            // Red Scutum leaning outside
            drawRect(color = RomanCrimsonLight, topLeft = Offset(tx + tentW - 2f, ty + 2f), size = Size(3f, 8f))
        }
    }
}

private fun DrawScope.drawWorkshopsAndLogistics(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    horreumLevel: Int,
    fabricaLevel: Int,
    valetudinariumLevel: Int,
    fireFlicker: Float,
    smokeClock: Float
) {
    val eastX = campLeft + (campW * 0.65f)
    
    // 1. HORREUM (Twin Granary Warehouses - Top East)
    val hY = campTop + 24f
    val hW = campW * 0.28f
    val hH = campH * 0.28f

    // Twin Barns with external buttresses
    val barnW = (hW - 8f) / 2f
    for (i in 0..1) {
        val bx = eastX + (i * (barnW + 8f))
        drawRect(color = Color(0xFF8D6E63), topLeft = Offset(bx, hY), size = Size(barnW, hH))
        // Terracotta Tile Roof lines
        drawRect(color = Color(0xFFA04000), topLeft = Offset(bx + 2f, hY + 2f), size = Size(barnW - 4f, hH - 4f))
        // Buttresses
        for (step in 1..3) {
            val by = hY + (step * (hH / 4f))
            drawRect(color = Color(0xFF5D4037), topLeft = Offset(bx - 3f, by), size = Size(3f, 6f))
            drawRect(color = Color(0xFF5D4037), topLeft = Offset(bx + barnW, by), size = Size(3f, 6f))
        }
    }
    // Grain sacks outside
    drawCircle(color = Color(0xFFD7CCC8), radius = 2.5f, center = Offset(eastX + hW * 0.5f, hY + hH - 4f))

    // 2. FABRICA (Forge & Armory - Bottom East)
    val fY = campTop + (campH * 0.48f)
    val fW = campW * 0.28f
    val fH = campH * 0.22f

    drawRect(color = Color(0xFF37474F), topLeft = Offset(eastX, fY), size = Size(fW, fH))
    drawRect(color = RomanGoldDark, topLeft = Offset(eastX, fY), size = Size(fW, fH), style = Stroke(1f))

    // Glowing Forge Furnace Hearth
    val forgeCenter = Offset(eastX + (fW * 0.35f), fY + (fH * 0.50f))
    drawCircle(color = Color(0xFFFF5722), radius = 6f * fireFlicker, center = forgeCenter)
    drawCircle(color = Color(0xFFFFEB3B), radius = 3f * fireFlicker, center = forgeCenter)

    // Animated Forge Smoke Particles
    for (i in 0..3) {
        val sOffset = (smokeClock * 0.6f + i * 20f) % 40f
        val smY = forgeCenter.y - sOffset
        val smX = forgeCenter.x + sin(smY * 0.15f) * 6f
        drawCircle(
            color = Color.White.copy(alpha = (1f - sOffset / 40f) * 0.5f),
            radius = 2.5f + (sOffset * 0.1f),
            center = Offset(smX, smY)
        )
    }

    // Anvils & Weapon Stacks
    drawRect(color = Color(0xFF212121), topLeft = Offset(eastX + fW * 0.70f, fY + 6f), size = Size(8f, 6f))
    drawLine(color = Color(0xFFECEFF1), start = Offset(eastX + fW * 0.60f, fY + fH - 6f), end = Offset(eastX + fW * 0.85f, fY + fH - 6f), strokeWidth = 2f)

    // 3. VALETUDINARIUM (Hospital Cloister - Bottom Center East)
    val vY = campTop + (campH * 0.72f)
    val vH = campH * 0.20f
    drawRect(color = Color(0xFFEFEBE9), topLeft = Offset(eastX, vY), size = Size(fW, vH))
    drawRect(color = RomanCrimson, topLeft = Offset(eastX + (fW * 0.45f), vY + 4f), size = Size(8f, 14f))
    drawRect(color = RomanCrimson, topLeft = Offset(eastX + (fW * 0.35f), vY + 7f), size = Size(16f, 8f))
}

private fun DrawScope.drawCornerWatchtowers(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    speculaLevel: Int,
    fireFlicker: Float
) {
    val towerSize = 16f
    val towerColor = if (speculaLevel >= 3) Color(0xFF78909C) else Color(0xFF5D4037)

    val corners = listOf(
        Offset(campLeft - 2f, campTop - 2f),
        Offset(campLeft + campW - towerSize + 2f, campTop - 2f),
        Offset(campLeft - 2f, campTop + campH - towerSize + 2f),
        Offset(campLeft + campW - towerSize + 2f, campTop + campH - towerSize + 2f)
    )

    corners.forEach { corner ->
        drawRect(color = towerColor, topLeft = corner, size = Size(towerSize, towerSize))
        drawRect(color = RomanGold, topLeft = corner, size = Size(towerSize, towerSize), style = Stroke(1f))
        // Turret Beacon Fire
        drawCircle(
            color = Color(0xFFFF9800),
            radius = 2.5f * fireFlicker,
            center = Offset(corner.x + towerSize * 0.5f, corner.y + towerSize * 0.5f)
        )
    }
}

private fun DrawScope.drawPatrols(
    campLeft: Float,
    campTop: Float,
    campW: Float,
    campH: Float,
    patrolClock: Float
) {
    // 1. Patrol on Via Principalis (Walking West to East)
    val prinY = campTop + (campH * 0.42f)
    val prinX = campLeft + 30f + (patrolClock * (campW - 60f))
    drawMiniPatrolLegionary(prinX, prinY - 2f)
    drawMiniPatrolLegionary(prinX - 10f, prinY - 2f)

    // 2. Centurion Patrol on Intervallum (Circulating the wall)
    val perimeterLength = (campW + campH) * 2f
    val dist = (patrolClock * perimeterLength) % perimeterLength
    val ix: Float
    val iy: Float
    val margin = 12f

    when {
        dist < campW -> {
            ix = campLeft + margin + dist
            iy = campTop + margin
        }
        dist < campW + campH -> {
            ix = campLeft + campW - margin
            iy = campTop + margin + (dist - campW)
        }
        dist < (campW * 2f) + campH -> {
            ix = campLeft + campW - margin - (dist - (campW + campH))
            iy = campTop + campH - margin
        }
        else -> {
            ix = campLeft + margin
            iy = campTop + campH - margin - (dist - ((campW * 2f) + campH))
        }
    }
    drawMiniCenturion(ix, iy)
}

private fun DrawScope.drawMiniPatrolLegionary(x: Float, y: Float) {
    // Bronze Helmet
    drawCircle(color = RomanGold, radius = 2.5f, center = Offset(x, y))
    // Red Tunic
    drawRect(color = RomanCrimson, topLeft = Offset(x - 2f, y + 2f), size = Size(4f, 4f))
    // Scutum Shield
    drawRect(color = RomanCrimsonLight, topLeft = Offset(x + 2f, y), size = Size(2f, 6f))
}

private fun DrawScope.drawMiniCenturion(x: Float, y: Float) {
    // Red Crest
    drawRect(color = Color(0xFFC0392B), topLeft = Offset(x - 3f, y - 4f), size = Size(6f, 2f))
    // Helmet
    drawCircle(color = RomanGold, radius = 2.5f, center = Offset(x, y))
    // Purple Cloak
    drawRect(color = Color(0xFF4A148C), topLeft = Offset(x - 2.5f, y + 2f), size = Size(5f, 4f))
}

private fun DrawScope.drawRomanCompassRose(cx: Float, cy: Float) {
    val r = 14f
    // Compass Circle
    drawCircle(color = Color(0x66000000), radius = r, center = Offset(cx, cy))
    drawCircle(color = RomanGoldDark, radius = r, center = Offset(cx, cy), style = Stroke(1f))

    // North (Septentrio) Arrow in Red
    val northPath = Path().apply {
        moveTo(cx, cy - r + 2f)
        lineTo(cx + 3f, cy)
        lineTo(cx - 3f, cy)
        close()
    }
    drawPath(northPath, color = RomanCrimsonLight)

    // South / East / West needles
    val southPath = Path().apply {
        moveTo(cx, cy + r - 2f)
        lineTo(cx + 3f, cy)
        lineTo(cx - 3f, cy)
        close()
    }
    drawPath(southPath, color = RomanGoldLight)

    // 'N' letter marker
    drawLine(color = RomanGoldLight, start = Offset(cx - 2f, cy - r - 4f), end = Offset(cx - 2f, cy - r - 9f), strokeWidth = 1f)
    drawLine(color = RomanGoldLight, start = Offset(cx - 2f, cy - r - 9f), end = Offset(cx + 2f, cy - r - 4f), strokeWidth = 1f)
    drawLine(color = RomanGoldLight, start = Offset(cx + 2f, cy - r - 9f), end = Offset(cx + 2f, cy - r - 4f), strokeWidth = 1f)
}
