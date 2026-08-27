package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LegionResources
import com.example.model.SeasonYear
import com.example.ui.theme.*

@Composable
fun TopResourceBar(
    seasonYear: SeasonYear,
    resources: LegionResources,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RomanDarkSurfaceHeader,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = RomanGoldDark.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Top row: Season & Year banner + Sound toggle + Golden Age tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = seasonYear.formatted,
                        color = RomanGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (resources.isGoldenAge) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RomanGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✨ Золотой Век",
                                color = RomanDarkSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Звук",
                        tint = if (isSoundEnabled) RomanGold else RomanBronze,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom row: Resources 💰 Denarii | 🌾 Provisions | ⭐ Glory | 🏛️ Senate Favor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResourceChip(
                    icon = "💰",
                    value = "${resources.denarii}",
                    label = "Казна",
                    color = RomanGoldLight,
                    testTag = "resource_denarii"
                )
                ResourceChip(
                    icon = "🌾",
                    value = "${resources.provisions}",
                    label = "Провизия",
                    color = Color(0xFFC5E1A5),
                    testTag = "resource_provisions"
                )
                ResourceChip(
                    icon = "⭐",
                    value = "${resources.glory}",
                    label = "Слава",
                    color = TriumphGold,
                    testTag = "resource_glory"
                )
                ResourceChip(
                    icon = "🏛️",
                    value = "${resources.senateFavor}%",
                    label = "Сенат",
                    color = RomanParchment,
                    testTag = "resource_senate"
                )
            }
        }
    }
}

@Composable
private fun ResourceChip(
    icon: String,
    value: String,
    label: String,
    color: Color,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(RomanDarkSurfaceCard)
            .border(1.dp, RomanBronzeDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 13.sp)
            Column {
                Text(
                    text = value,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
