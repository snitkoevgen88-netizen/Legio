package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.pixelart.PixelCommanderPortrait
import com.example.ui.theme.*

@Composable
fun ExpeditionsScreen(
    availableExpeditions: List<Expedition>,
    commanders: List<Commander>,
    cohorts: List<Cohort>,
    campLevel: Int,
    resources: LegionResources,
    seasonalPlan: SeasonalPlan,
    selectedProvince: StrategicProvince = StrategicProvince.LATIUM,
    strategicRoads: List<StrategicRoadUpgrade> = emptyList(),
    onSelectProvince: (StrategicProvince) -> Unit = {},
    onPaveRoad: (String) -> Unit = {},
    onCalculateOdds: (Expedition, Commander, Cohort, Tactics) -> BattleOddsPreview,
    onSetExpeditionPlan: (String?, String?, String?, Tactics) -> Unit,
    onSetTactics: (Tactics) -> Unit,
    onAutoSelectSquad: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val livingCommanders = commanders.filter { it.isAlive }
    var selectedExpeditionId by remember { mutableStateOf(seasonalPlan.launchedExpeditionId ?: availableExpeditions.firstOrNull()?.id) }
    var selectedCommanderId by remember { mutableStateOf(seasonalPlan.selectedCommanderId ?: livingCommanders.firstOrNull()?.id) }
    var selectedCohortId by remember { mutableStateOf(seasonalPlan.selectedCohortId ?: cohorts.firstOrNull()?.id) }
    var selectedTactics by remember { mutableStateOf(seasonalPlan.selectedTactics) }

    // Sync with seasonalPlan if changed externally (e.g. from Auto-Plan)
    LaunchedEffect(seasonalPlan) {
        if (seasonalPlan.launchedExpeditionId != null) {
            selectedExpeditionId = seasonalPlan.launchedExpeditionId
        }
        if (seasonalPlan.selectedCommanderId != null) {
            selectedCommanderId = seasonalPlan.selectedCommanderId
        }
        if (seasonalPlan.selectedCohortId != null) {
            selectedCohortId = seasonalPlan.selectedCohortId
        }
        selectedTactics = seasonalPlan.selectedTactics
    }

    val currentExpedition = availableExpeditions.find { it.id == selectedExpeditionId } ?: availableExpeditions.first()
    val currentCommander = livingCommanders.find { it.id == selectedCommanderId } ?: livingCommanders.first()
    val currentCohort = cohorts.find { it.id == selectedCohortId } ?: cohorts.first()

    val battleOdds = remember(currentExpedition, currentCommander, currentCohort, selectedTactics, seasonalPlan) {
        onCalculateOdds(currentExpedition, currentCommander, currentCohort, selectedTactics)
    }

    val isThisExpeditionInPlan = seasonalPlan.launchedExpeditionId == currentExpedition.id

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🗺️ Военные кампании и стратегическая карта Республики",
                color = RomanGoldLight,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Изучайте разведданные, выбирайте опытного командира, мостите военные дороги и подбирайте тактику под вражеский строй.",
                color = RomanTextMuted,
                fontSize = 12.sp
            )
        }

        // Strategic Interactive Map of Italy
        item {
            StrategicMapCanvas(
                selectedProvince = selectedProvince,
                roads = strategicRoads,
                onSelectProvince = onSelectProvince
            )
        }

        // Selected province summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedProvince.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Провинция: ${selectedProvince.nameRu} (${selectedProvince.latinNameRu})",
                            color = RomanGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Статус: ${selectedProvince.controlStatus} • Доход: ${selectedProvince.resourceYieldRu}",
                            color = RomanTextGold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedProvince.descriptionRu,
                            color = RomanTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Strategic Roads Upgrade Section
        if (strategicRoads.isNotEmpty()) {
            item {
                StrategicRoadsSection(
                    roads = strategicRoads,
                    denarii = resources.denarii,
                    onPaveRoad = onPaveRoad
                )
            }
        }

        // 1. Horizontal list of campaigns
        item {
            Text(text = "Доступные театры военных действий:", color = RomanGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableExpeditions, key = { it.id }) { exp ->
                    val isSelected = exp.id == selectedExpeditionId
                    val isSenate = exp.isSenateTrial
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { selectedExpeditionId = exp.id }
                            .testTag("expedition_tab_${exp.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) RomanCrimsonDark else RomanDarkSurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSenate) RomanGold else if (isSelected) RomanGoldLight else RomanBronzeDark
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (isSenate) {
                                Text(text = "⚡ Приказ Сената", color = RomanGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = exp.titleRu,
                                color = if (isSelected) RomanGoldLight else RomanParchment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Сложность: ${"★".repeat(exp.difficulty)}${"☆".repeat(5 - exp.difficulty)}",
                                color = TriumphGold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "💰 -${exp.denariiCost} | 🌾 -${exp.provisionsCost}",
                                color = RomanTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Selected Campaign Details & Scout Intel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentExpedition.titleRu,
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Регион: ${currentExpedition.regionRu}",
                                color = RomanTextGold,
                                fontSize = 12.sp
                            )
                        }

                        // Auto-select squad button
                        Button(
                            onClick = { onAutoSelectSquad(currentExpedition.id) },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("auto_select_squad_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ Авто-подбор",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentExpedition.historicalContextRu,
                        color = RomanTextLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scout Intel Box (Specula report)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanDarkSurface)
                            .border(1.dp, RomanBronze, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "👁️ Разведданные (Specula):", color = RomanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (currentExpedition.scoutIntel.intelClarity >= 3) "Точная разведка" else "Частичный туман войны",
                                    color = RomanTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "• Сила врага: ${currentExpedition.scoutIntel.estimatedEnemyStrengthRu}", color = RomanParchment, fontSize = 11.sp)
                            Text(text = "• Уровень угрозы: ${currentExpedition.scoutIntel.dangerLevelRu}", color = if (currentExpedition.difficulty >= 4) DefeatRed else RomanGreenLight, fontSize = 11.sp)
                            Text(text = "• Тактика противника: ${currentExpedition.scoutIntel.enemyTacticRu}", color = RomanTextGold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rewards row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Награда: 💰 +${currentExpedition.rewardDenarii} • 🌾 +${currentExpedition.rewardProvisions} • ⭐ +${currentExpedition.rewardGlory} Славы", color = RomanGoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 3. Commander & Cohort Assignment
        item {
            Text(text = "Назначение командующего и когорты:", color = RomanGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            // Commander Picker
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(livingCommanders, key = { it.id }) { cmd ->
                    val isCmdSelected = cmd.id == selectedCommanderId
                    Card(
                        modifier = Modifier
                            .clickable { selectedCommanderId = cmd.id }
                            .testTag("select_cmd_${cmd.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCmdSelected) RomanCrimsonDark else RomanDarkSurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isCmdSelected) 2.dp else 1.dp,
                            if (isCmdSelected) RomanGold else RomanBronzeDark
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PixelCommanderPortrait(commander = cmd, size = 36.dp)
                            Column {
                                Text(text = cmd.name, color = RomanGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${cmd.trait.icon} ${cmd.trait.titleRu}", color = RomanTextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cohort Picker
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cohorts, key = { it.id }) { coh ->
                    val isCohSelected = coh.id == selectedCohortId
                    Card(
                        modifier = Modifier
                            .clickable { selectedCohortId = coh.id }
                            .testTag("select_coh_${coh.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCohSelected) RomanCrimsonDark else RomanDarkSurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isCohSelected) 2.dp else 1.dp,
                            if (isCohSelected) RomanGold else RomanBronzeDark
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = coh.name, color = RomanGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "🛡️ ${coh.soldiers}/${coh.maxSoldiers} • 🎖️ ${coh.veteransCount} вет.", color = RomanTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. Tactical Choice Selector
        item {
            Text(text = "Выбор тактики манипул:", color = RomanGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Tactics.entries.forEach { tactics ->
                    val isTacticSelected = tactics == selectedTactics
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTactics = tactics
                                onSetTactics(tactics)
                            }
                            .testTag("tactics_option_${tactics.name}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTacticSelected) RomanCrimsonDark else RomanDarkSurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isTacticSelected) 2.dp else 1.dp,
                            if (isTacticSelected) RomanGold else RomanBronzeDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = tactics.icon, fontSize = 20.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tactics.titleRu,
                                    color = if (isTacticSelected) RomanGoldLight else RomanParchment,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tactics.descRu,
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isTacticSelected) {
                                Text(text = "✓", color = RomanGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Real-Time Probability Odds Preview & Tactical Advice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📊 Прогноз исхода битвы (с учетом командира и тактики):",
                        color = RomanGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Visual Progress Split Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        if (battleOdds.greatVictoryPct > 0) {
                            Box(modifier = Modifier.weight(battleOdds.greatVictoryPct.toFloat()).fillMaxHeight().background(TriumphGold))
                        }
                        if (battleOdds.victoryPct > 0) {
                            Box(modifier = Modifier.weight(battleOdds.victoryPct.toFloat()).fillMaxHeight().background(RomanGreenLight))
                        }
                        if (battleOdds.partialPct > 0) {
                            Box(modifier = Modifier.weight(battleOdds.partialPct.toFloat()).fillMaxHeight().background(RomanParchment))
                        }
                        if (battleOdds.defeatPct > 0) {
                            Box(modifier = Modifier.weight(battleOdds.defeatPct.toFloat()).fillMaxHeight().background(DefeatRed))
                        }
                        if (battleOdds.disasterPct > 0) {
                            Box(modifier = Modifier.weight(battleOdds.disasterPct.toFloat()).fillMaxHeight().background(Color(0xFFBA68C8)))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "🏆 ${battleOdds.greatVictoryPct}%", color = TriumphGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "⚔️ ${battleOdds.victoryPct}%", color = RomanGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "⚖️ ${battleOdds.partialPct}%", color = RomanParchment, fontSize = 11.sp)
                        Text(text = "🚩 ${battleOdds.defeatPct}%", color = DefeatRed, fontSize = 11.sp)
                        Text(text = "💀 ${battleOdds.disasterPct}%", color = Color(0xFFBA68C8), fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = battleOdds.adviceRu,
                        color = RomanTextGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 6. Action Button: Assign or Cancel Campaign in Season Plan
        item {
            Button(
                onClick = {
                    if (isThisExpeditionInPlan) {
                        onSetExpeditionPlan(null, null, null, selectedTactics)
                    } else {
                        onSetExpeditionPlan(currentExpedition.id, currentCommander.id, currentCohort.id, selectedTactics)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("plan_expedition_submit_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isThisExpeditionInPlan) RomanGold else RomanCrimson,
                    contentColor = if (isThisExpeditionInPlan) RomanDarkSurface else RomanGoldLight
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isThisExpeditionInPlan) "✓ Поход утвержден в плане сезона (Отменить)" else "🗺️ Назначить экспедицию в план сезона",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
