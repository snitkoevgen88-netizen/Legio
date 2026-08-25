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
import com.example.model.Cohort
import com.example.model.Commander
import com.example.model.LegionResources
import com.example.model.SeasonalPlan
import com.example.ui.theme.*

@Composable
fun CohortsScreen(
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    resources: LegionResources,
    seasonalPlan: SeasonalPlan,
    onSetTrainingCohort: (String?) -> Unit,
    onReplenishCohort: (String) -> Unit,
    onReplenishAllCohorts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMissingSoldiers = cohorts.sumOf { it.maxSoldiers - it.soldiers }
    val totalReplenishCost = totalMissingSoldiers * 1
    val canReplenishAll = totalMissingSoldiers > 0 && resources.denarii >= totalReplenishCost

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⚔️ Манипулы и когорты Legio IV",
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (totalMissingSoldiers > 0) "Не хватает: $totalMissingSoldiers воинов (🪙 $totalReplenishCost)" else "Все когорты полностью укомплектованы",
                                color = if (totalMissingSoldiers > 0) DefeatRed else RomanGreenLight,
                                fontSize = 11.sp
                            )
                        }

                        if (totalMissingSoldiers > 0) {
                            Button(
                                onClick = onReplenishAllCohorts,
                                enabled = canReplenishAll,
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("replenish_all_cohorts_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RomanGreenLight,
                                    contentColor = RomanDarkSurface,
                                    disabledContainerColor = RomanBronzeDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ Пополнить всех",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        items(cohorts) { cohort ->
            val isSelectedForTraining = seasonalPlan.trainCohortId == cohort.id
            val assignedCommander = commanders.find { it.id == cohort.assignedCommanderId }
            val missingSoldiers = cohort.maxSoldiers - cohort.soldiers
            val replenishCost = missingSoldiers * 1

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cohort_card_${cohort.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelectedForTraining) 2.dp else 1.dp,
                    if (isSelectedForTraining) RomanGold else RomanBronzeDark
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Title & Level badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cohort.name,
                                color = RomanGoldLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cohort.rankLabel,
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RomanCrimson)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Уровень ${cohort.level}",
                                color = RomanGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // XP Progress bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Опыт (XP)", color = RomanTextMuted, fontSize = 10.sp)
                            Text(text = "${cohort.xp} / ${cohort.maxXp}", color = RomanGoldLight, fontSize = 10.sp)
                        }
                        LinearProgressIndicator(
                            progress = { cohort.xp.toFloat() / cohort.maxXp.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = RomanGold,
                            trackColor = RomanDarkSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stats grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanDarkSurface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Численность", color = RomanTextMuted, fontSize = 10.sp)
                            Text(
                                text = "${cohort.soldiers}/${cohort.maxSoldiers}",
                                color = if (cohort.soldiers < 50) DefeatRed else RomanGreenLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ветераны", color = RomanTextMuted, fontSize = 10.sp)
                            Text(text = "🎖️ ${cohort.veteransCount}", color = RomanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Атака / Защита", color = RomanTextMuted, fontSize = 10.sp)
                            Text(text = "⚔️ ${cohort.attackPower} / 🛡️ ${cohort.defensePower}", color = RomanParchment, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Дисциплина", color = RomanTextMuted, fontSize = 10.sp)
                            Text(text = "${cohort.discipline}", color = RomanTextGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Combat Record history
                    Text(
                        text = "📜 История: ${cohort.expeditionsCount} походов • 🏆 ${cohort.greatVictoriesCount} триумфов • ⚔️ ${cohort.victoriesCount} побед • 🚩 ${cohort.defeatsCount} поражений",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )

                    // Traditions
                    if (cohort.traditions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cohort.traditions.forEach { trad ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF3E2723))
                                        .border(1.dp, RomanBronze, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "🛡️ $trad", color = RomanGoldLight, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions: Training Drill & Replenishment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Training toggle button
                        Button(
                            onClick = {
                                if (isSelectedForTraining) onSetTrainingCohort(null) else onSetTrainingCohort(cohort.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("train_cohort_btn_${cohort.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelectedForTraining) RomanGold else RomanDarkSurface,
                                contentColor = if (isSelectedForTraining) RomanDarkSurface else RomanGoldLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
                        ) {
                            Text(
                                text = if (isSelectedForTraining) "✓ Назначена тренировка" else "⚔️ Тренировать в сезоне",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Replenish button if soldiers missing
                        if (missingSoldiers > 0) {
                            OutlinedButton(
                                onClick = { onReplenishCohort(cohort.id) },
                                enabled = resources.denarii >= replenishCost,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RomanGreenLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGreenLight)
                            ) {
                                Text(
                                    text = "Пополнить ($replenishCost 💰)",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
