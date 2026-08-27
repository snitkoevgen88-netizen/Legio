package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.Commander
import com.example.model.LegionResources
import com.example.ui.pixelart.PixelCommanderPortrait
import com.example.ui.theme.*

@Composable
fun CommandersScreen(
    commanders: List<Commander>,
    resources: LegionResources,
    onRecruitCommander: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recruitCost = 90
    val canRecruit = resources.denarii >= recruitCost

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎖️ Офицеры и трибуны Legio IV",
                        color = RomanGoldLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Каждый командир обладает уникальным характером и боевым стилем.",
                        color = RomanTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Recruit button banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Призвать знатного трибуна из Рима", color = RomanParchment, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Стоимость: 💰 $recruitCost денариев", color = RomanGold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onRecruitCommander,
                        enabled = canRecruit,
                        colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("recruit_commander_btn")
                    ) {
                        Text("Призвать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(commanders, key = { it.id }) { commander ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("commander_card_${commander.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (commander.isAlive) RomanBronzeDark else DefeatRed
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelCommanderPortrait(commander = commander, size = 56.dp)

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = commander.name,
                                    color = if (commander.isAlive) RomanGoldLight else DefeatRed,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (commander.isAlive) RomanCrimson else Color(0xFF424242))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = commander.rankLabel,
                                        color = RomanGoldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Характер: ${commander.trait.icon} ${commander.trait.titleRu}",
                                color = RomanTextGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = commander.trait.descRu,
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // XP Progress bar
                    if (commander.isAlive) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Боевой опыт: ${commander.xp} / ${commander.maxXp} XP", color = RomanTextMuted, fontSize = 10.sp)
                                Text(text = "Уровень ${commander.level}", color = RomanGoldLight, fontSize = 10.sp)
                            }
                            LinearProgressIndicator(
                                progress = { commander.xp.toFloat() / commander.maxXp.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RomanGold,
                                trackColor = RomanDarkSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Battle Record
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(RomanDarkSurface)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Походов: ${commander.expeditionsLed}", color = RomanParchment, fontSize = 11.sp)
                            Text(text = "🏆 Триумфов: ${commander.greatVictoriesCount}", color = TriumphGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "⚔️ Побед: ${commander.victoriesCount}", color = RomanGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "🚩 Поражений: ${commander.defeatsCount}", color = DefeatRed, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "💬 Настроение: «${commander.moodStatus}»",
                        color = RomanTextLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
