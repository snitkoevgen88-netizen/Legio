package com.example.ui.pixelart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.Commander
import com.example.ui.theme.*

/**
 * Pixel-Art Commander Portrait generator in Jetpack Compose.
 * Renders retro 16x16 styled pixel grid with authentic Roman features.
 */
@Composable
fun PixelCommanderPortrait(
    commander: Commander,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(RomanDarkSurfaceCard)
            .border(2.dp, if (commander.isAlive) RomanGold else DefeatRed, RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val gridCount = 16
            val pixelSize = this.size.width / gridCount

            // Colors based on commander properties
            val skinTone = when (commander.avatarSkinTone) {
                0 -> Color(0xFFF5CBA7) // Light Mediterranean
                1 -> Color(0xFFE59866) // Tanned Roman
                2 -> Color(0xFFBA4A00) // Deep sunbaked
                else -> Color(0xFFEDBB99)
            }
            val skinShadow = when (commander.avatarSkinTone) {
                0 -> Color(0xFFDC7633)
                1 -> Color(0xFFA04000)
                else -> Color(0xFF873600)
            }

            val hairColor = when (commander.hairStyle) {
                0 -> Color(0xFF2C1810) // Dark brown
                1 -> Color(0xFF1B120C) // Black
                2 -> Color(0xFF873600) // Auburn
                else -> Color(0xFF7F8C8D) // Veteran Grey
            }

            val cloakColor = when (commander.cloakColorIndex) {
                0 -> RomanCrimsonLight
                1 -> Color(0xFF6A1B9A) // Patrician Purple
                2 -> RomanGoldDark
                else -> RomanIron
            }

            val helmetBronze = Color(0xFFD4AC0D)
            val helmetIron = Color(0xFF7F8C8D)
            val crestRed = Color(0xFFC0392B)
            val crestGold = Color(0xFFF1C40F)

            fun drawPx(x: Int, y: Int, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(x * pixelSize, y * pixelSize),
                    size = Size(pixelSize, pixelSize)
                )
            }

            // 1. Background gradient / ambient
            for (x in 0 until gridCount) {
                for (y in 0 until gridCount) {
                    val shade = if ((x + y) % 2 == 0) Color(0xFF2A1C16) else Color(0xFF231611)
                    drawPx(x, y, shade)
                }
            }

            // 2. Cloak & Shoulders (Rows 12..15)
            for (x in 3..12) {
                for (y in 12..15) {
                    drawPx(x, y, cloakColor)
                }
            }
            // Fibula golden brooch
            drawPx(4, 12, RomanGold)
            drawPx(4, 13, RomanGoldLight)

            // 3. Neck & Chin (Rows 10..11)
            for (x in 6..9) {
                drawPx(x, 10, skinTone)
                drawPx(x, 11, skinShadow)
            }

            // 4. Face Base (Rows 6..10)
            for (x in 5..10) {
                for (y in 6..10) {
                    drawPx(x, y, skinTone)
                }
            }

            // Beard / Stubble
            if (commander.beardStyle == 1) {
                // Stubble
                drawPx(6, 10, skinShadow)
                drawPx(7, 10, skinShadow)
                drawPx(8, 10, skinShadow)
                drawPx(9, 10, skinShadow)
            } else if (commander.beardStyle == 2) {
                // Full beard
                for (x in 5..10) {
                    drawPx(x, 10, hairColor)
                    drawPx(x, 11, hairColor)
                }
            }

            // Eyes & Brows (Rows 7..8)
            drawPx(6, 7, Color.White)
            drawPx(6, 8, Color(0xFF1B120C)) // Pupil
            drawPx(9, 7, Color.White)
            drawPx(9, 8, Color(0xFF1B120C)) // Pupil
            // Eyebrows
            drawPx(6, 6, hairColor)
            drawPx(9, 6, hairColor)

            // Nose
            drawPx(7, 8, skinShadow)
            drawPx(8, 8, skinShadow)

            // Mouth
            val mouthColor = if (commander.isAlive) Color(0xFF900C3F) else Color(0xFF581845)
            drawPx(7, 10, mouthColor)
            drawPx(8, 10, mouthColor)

            // 5. Helmet / Headgear (Rows 1..6)
            when (commander.helmetType) {
                1 -> {
                    // Transverse Crest (Crested Centurion)
                    for (x in 4..11) {
                        drawPx(x, 2, crestRed)
                        drawPx(x, 3, crestRed)
                    }
                    drawPx(7, 1, crestGold)
                    drawPx(8, 1, crestGold)
                    // Helmet Bowl
                    for (x in 4..11) {
                        drawPx(x, 4, helmetBronze)
                        drawPx(x, 5, helmetBronze)
                    }
                    // Cheek guards
                    drawPx(4, 6, helmetBronze)
                    drawPx(4, 7, helmetBronze)
                    drawPx(11, 6, helmetBronze)
                    drawPx(11, 7, helmetBronze)
                }
                2 -> {
                    // Golden Laurel Wreath (Legate / Triumphator)
                    for (x in 5..10) {
                        drawPx(x, 4, hairColor)
                        drawPx(x, 5, hairColor)
                    }
                    // Golden laurel leaves
                    drawPx(4, 4, RomanGold)
                    drawPx(5, 3, RomanGoldLight)
                    drawPx(10, 3, RomanGoldLight)
                    drawPx(11, 4, RomanGold)
                    drawPx(7, 3, RomanGold)
                    drawPx(8, 3, RomanGold)
                }
                else -> {
                    // Standard Montefortino Helmet
                    drawPx(7, 2, helmetIron)
                    drawPx(8, 2, helmetIron)
                    for (x in 4..11) {
                        drawPx(x, 3, helmetIron)
                        drawPx(x, 4, helmetIron)
                        drawPx(x, 5, helmetBronze)
                    }
                    // Cheek guards
                    drawPx(4, 6, helmetIron)
                    drawPx(11, 6, helmetIron)
                }
            }

            // Skull overlay if fallen
            if (!commander.isAlive) {
                drawPx(7, 7, DefeatRed)
                drawPx(8, 7, DefeatRed)
                drawPx(7, 8, Color.Black)
                drawPx(8, 8, Color.Black)
            }
        }
    }
}
