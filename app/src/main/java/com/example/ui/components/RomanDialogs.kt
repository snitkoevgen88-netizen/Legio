package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.pixelart.PixelBattleDiorama
import com.example.ui.pixelart.PixelCommanderPortrait
import com.example.ui.theme.*

/**
 * Seasonal Plan Modal ("План сезона")
 * Summarizes the planned training, building upgrades, expeditions, and budget before turn execution.
 */
@Composable
fun SeasonPlanDialog(
    seasonalPlan: SeasonalPlan,
    buildings: List<Building>,
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    availableExpeditions: List<Expedition>,
    resources: LegionResources,
    seasonYear: SeasonYear,
    onAutoPlan: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val trainingCohort = cohorts.find { it.id == seasonalPlan.trainCohortId }
    val upgradeBuilding = buildings.find { it.type == seasonalPlan.upgradeBuildingType }
    val expedition = availableExpeditions.find { it.id == seasonalPlan.launchedExpeditionId }
    val commander = commanders.find { it.id == seasonalPlan.selectedCommanderId }

    var totalCostDenarii = 0
    var totalCostProvisions = 0

    if (trainingCohort != null) {
        totalCostDenarii += if (seasonYear.season == Season.SPRING) 24 else 30
        totalCostProvisions += 15
    }
    if (upgradeBuilding != null) {
        totalCostDenarii += upgradeBuilding.upgradeCostDenarii
        totalCostProvisions += upgradeBuilding.upgradeCostProvisions
    }
    if (expedition != null) {
        totalCostDenarii += expedition.denariiCost
        totalCostProvisions += expedition.provisionsCost
    }

    val canAfford = resources.denarii >= totalCostDenarii && resources.provisions >= totalCostProvisions

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📜 План сезона: ${seasonYear.formatted}",
                    color = RomanGoldLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Training
                PlanSectionItem(
                    title = "⚔️ Тренировка отрядов",
                    content = if (trainingCohort != null) "${trainingCohort.name} — усиленная строевая подготовка" else "Тренировки не назначены"
                )

                // Section 2: Construction
                PlanSectionItem(
                    title = "🏗️ Строительство в лагере",
                    content = if (upgradeBuilding != null) "Улучшение: ${upgradeBuilding.type.titleRu} (Ранг ${upgradeBuilding.level} ➔ ${upgradeBuilding.level + 1})" else "Строительные работы не запланированы"
                )

                // Section 3: Expedition
                PlanSectionItem(
                    title = "🗺️ Военная экспедиция",
                    content = if (expedition != null && commander != null)
                        "Поход: ${expedition.titleRu}\nКомандующий: ${commander.name} (${commander.trait.titleRu})\nТактика: ${seasonalPlan.selectedTactics.titleRu}"
                    else "Экспедиция не выбрана (войска остаются в лагере)"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expenses summary box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RomanDarkSurface)
                        .border(1.dp, RomanBronzeDark, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Расходы за сезон:",
                            color = RomanGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "💰 Денарии: -$totalCostDenarii", color = if (resources.denarii >= totalCostDenarii) RomanGoldLight else DefeatRed, fontSize = 13.sp)
                            Text(text = "🌾 Провизия: -$totalCostProvisions", color = if (resources.provisions >= totalCostProvisions) Color(0xFFC5E1A5) else DefeatRed, fontSize = 13.sp)
                        }
                    }
                }

                if (!canAfford) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ Недостаточно ресурсов в казне или зернохранилище!",
                        color = DefeatRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Plan recommendation button
                OutlinedButton(
                    onClick = onAutoPlan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("dialog_auto_plan_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RomanGoldLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⚡ Автоматически составить сбалансированный план", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plan_back_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RomanTextLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronze)
                    ) {
                        Text("Вернуться")
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = canAfford,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("plan_confirm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RomanCrimson,
                            contentColor = RomanGoldLight,
                            disabledContainerColor = RomanIron,
                            disabledContentColor = RomanTextMuted
                        )
                    ) {
                        Text("Подтвердить", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanSectionItem(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = title, color = RomanGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(text = content, color = RomanTextLight, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = RomanBronzeDark.copy(alpha = 0.5f), thickness = 0.8.dp)
    }
}

/**
 * Battle Result Dialog with Pixel Art Battle Diorama and spoils breakdown.
 */
@Composable
fun BattleResultDialog(
    result: ExpeditionResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (result.outcome.isSuccess) RomanGold else DefeatRed
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Headline
                Text(
                    text = "${result.outcome.icon} ${result.outcome.titleRu}",
                    color = if (result.outcome.isSuccess) RomanGoldLight else DefeatRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = result.expedition.titleRu,
                    color = RomanTextGold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pixel Battle Diorama
                PixelBattleDiorama(
                    result = result
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Narrative Story Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RomanDarkSurface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = result.storyNarrativeRu,
                        color = RomanTextLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🩸 Потери", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "-${result.casualties}", color = if (result.casualties > 0) DefeatRed else RomanGreenLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "💰 Трофеи", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "+${result.lootDenarii}", color = RomanGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⭐ Слава", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "${if (result.gloryDelta >= 0) "+" else ""}${result.gloryDelta}", color = TriumphGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⚔️ Опыт", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "+${result.xpEarned} XP", color = RomanParchment, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (result.commanderPromoted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎖️ ${result.commander.name} повышен в звании!",
                        color = RomanGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (result.commanderKilled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💀 ${result.commander.name} погиб с честью на поле боя!",
                        color = DefeatRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battle_result_continue_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight)
                ) {
                    Text("Принять итоги похода", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Historical Random Camp Event Dialog
 */
@Composable
fun CampEventDialog(
    event: CampEvent,
    onChoiceSelected: (CampEventChoice) -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${event.icon} ${event.titleRu}",
                    color = RomanGoldLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = event.descRu,
                    color = RomanTextLight,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                event.choices.forEachIndexed { index, choice ->
                    OutlinedButton(
                        onClick = { onChoiceSelected(choice) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("event_choice_$index"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = RomanDarkSurface,
                            contentColor = RomanGoldLight
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = choice.textRu, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = choice.effectDescRu, color = RomanTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Building Upgrade Dialog
 */
@Composable
fun BuildingUpgradeDialog(
    building: Building,
    resources: LegionResources,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val canAfford = resources.denarii >= building.upgradeCostDenarii && resources.provisions >= building.upgradeCostProvisions && building.level < building.maxLevel

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${building.type.icon} ${building.type.titleRu}",
                    color = RomanGoldLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Текущий ранг: ${building.level} / ${building.maxLevel}",
                    color = RomanTextGold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = building.type.roleDescRu,
                    color = RomanTextLight,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RomanDarkSurface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Эффект текущего уровня:", color = RomanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = building.currentPerkRu, color = RomanParchment, fontSize = 12.sp)
                    }
                }

                if (building.level < building.maxLevel) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Стоимость улучшения: 💰 ${building.upgradeCostDenarii} | 🌾 ${building.upgradeCostProvisions}",
                        color = if (canAfford) RomanGoldLight else DefeatRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✨ Здание улучшено до максимума!",
                        color = RomanGreenLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Закрыть")
                    }

                    if (building.level < building.maxLevel) {
                        Button(
                            onClick = onUpgrade,
                            enabled = canAfford,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight)
                        ) {
                            Text("Улучшить")
                        }
                    }
                }
            }
        }
    }
}
