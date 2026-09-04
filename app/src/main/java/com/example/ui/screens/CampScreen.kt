package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.BuildingUpgradeDialog
import com.example.ui.pixelart.PixelCampTopDownView
import com.example.ui.theme.*

@Composable
fun CampScreen(
    seasonYear: SeasonYear,
    resources: LegionResources,
    campRank: CampRank,
    republicRank: RepublicRank,
    buildings: List<Building>,
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    seasonalPlan: SeasonalPlan,
    activeBlessing: ActiveBlessing?,
    senateQuests: List<SenateQuest> = emptyList(),
    doctrines: List<MilitaryDoctrine> = emptyList(),
    aquilaState: LegionAquilaState = LegionAquilaState(),
    totalVictories: Int = 0,
    totalGreatVictories: Int = 0,
    totalDefeats: Int = 0,
    onOpenSeasonPlan: () -> Unit,
    onAutoPlanSeason: (AutoPlanPriority) -> Unit,
    onBuildingUpgrade: (BuildingType) -> Unit,
    onReplenishAllCohorts: () -> Unit,
    onAutoEquipAll: () -> Unit,
    onNavigateToCohorts: () -> Unit,
    onNavigateToExpeditions: () -> Unit,
    onNavigateToSenate: () -> Unit,
    onNavigateToCommanders: () -> Unit,
    onNavigateToAltar: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToChronicles: () -> Unit,
    onClaimQuest: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBuildingForModal by remember { mutableStateOf<Building?>(null) }
    var selectedAutoPriority by remember { mutableStateOf(AutoPlanPriority.BALANCED) }

    // Aggregate Legion Metrics
    val totalSoldiers = cohorts.sumOf { it.soldiers }
    val maxSoldiers = cohorts.sumOf { it.maxSoldiers }
    val strengthPct = if (maxSoldiers > 0) ((totalSoldiers.toFloat() / maxSoldiers) * 100).toInt() else 0
    val avgMorale = if (cohorts.isNotEmpty()) cohorts.sumOf { it.morale } / cohorts.size else 80
    val avgDiscipline = if (cohorts.isNotEmpty()) cohorts.sumOf { it.discipline } / cohorts.size else 80
    val totalVeterans = cohorts.sumOf { it.veteransCount }

    // Dynamic Legion Title
    val legionEpithet = when {
        resources.glory >= 160 -> "Pia Fidelis Invicta"
        totalVictories >= 10 -> "Victrix"
        totalGreatVictories >= 3 -> "Triumphalis"
        resources.senateFavor >= 75 -> "Vindex Senatus"
        else -> "Martia"
    }
    val fullLegionName = "Legio IV $legionEpithet"

    // Primary Active Senate Quest or Campaign target
    val activeQuest = senateQuests.find { it.status == QuestStatus.ACTIVE || it.isFinished }

    // Evaluate Active Problems / Actionable Alerts
    val alerts = remember(cohorts, resources, senateQuests, commanders, activeBlessing) {
        val list = mutableListOf<LegionAlert>()

        // 1. Casualties alert
        val damagedCohorts = cohorts.filter { it.soldiers < (it.maxSoldiers * 0.65f) }
        if (damagedCohorts.isNotEmpty()) {
            val names = damagedCohorts.joinToString(", ") { it.name }
            list.add(
                LegionAlert(
                    id = "alert_casualties",
                    severity = AlertSeverity.CRITICAL,
                    icon = "🩸",
                    titleRu = "Потери в когортах ($names)",
                    descriptionRu = "Боеспособность снижена: $totalSoldiers/$maxSoldiers бойцов в строю.",
                    actionLabelRu = "Пополнить всех",
                    targetScreen = "COHORTS",
                    actionType = "REPLENISH_ALL"
                )
            )
        }

        // 2. Low denarii
        if (resources.denarii < 35) {
            list.add(
                LegionAlert(
                    id = "alert_low_denarii",
                    severity = AlertSeverity.CRITICAL,
                    icon = "🪙",
                    titleRu = "Казна легиона истощена",
                    descriptionRu = "В сундуках осталось ${resources.denarii} денариев. Необходим заём или военная добыча.",
                    actionLabelRu = "Казна / Рынок",
                    targetScreen = "MARKET"
                )
            )
        }

        // 3. Low grain
        if (resources.provisions < 30) {
            list.add(
                LegionAlert(
                    id = "alert_low_provisions",
                    severity = AlertSeverity.WARNING,
                    icon = "🌾",
                    titleRu = "Критический запас зерна",
                    descriptionRu = "Осталось ${resources.provisions} мер зерна. Когортам грозит голодный мор.",
                    actionLabelRu = "Купить зерно",
                    targetScreen = "MARKET"
                )
            )
        }

        // 4. Finished senate quest ready to claim
        val claimable = senateQuests.find { it.isFinished && !it.isClaimed }
        if (claimable != null) {
            list.add(
                LegionAlert(
                    id = "alert_claimable",
                    severity = AlertSeverity.OPPORTUNITY,
                    icon = "📜",
                    titleRu = "Награда Сената за эдикт",
                    descriptionRu = "Исполнен декрет: «${claimable.titleRu}» (+${claimable.rewardDenarii} 🪙).",
                    actionLabelRu = "Получить награду",
                    targetScreen = "SENATE",
                    actionType = "CLAIM_QUEST"
                )
            )
        }

        // 5. Commander promotion
        val promotable = commanders.find { it.isAlive && it.xp >= it.maxXp }
        if (promotable != null) {
            list.add(
                LegionAlert(
                    id = "alert_commander_xp",
                    severity = AlertSeverity.OPPORTUNITY,
                    icon = "🎖️",
                    titleRu = "Офицер готов к повышению",
                    descriptionRu = "${promotable.name} накопил опыт для повышения в звании.",
                    actionLabelRu = "К офицерам",
                    targetScreen = "COMMANDERS"
                )
            )
        }

        // 6. Divine favor absent
        if (activeBlessing == null) {
            list.add(
                LegionAlert(
                    id = "alert_altar",
                    severity = AlertSeverity.INFO,
                    icon = "🕊️",
                    titleRu = "Нет покровительства богов",
                    descriptionRu = "Освятите поход на алтаре Марса или Юпитера для победных ауспиций.",
                    actionLabelRu = "К Алтарю",
                    targetScreen = "ALTAR"
                )
            )
        }

        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TOP CASTRUM COMMAND HUB (Identity & 4 Vitals)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Legion Name and Rank Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🦅", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = fullLegionName,
                                    color = RomanGoldLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${campRank.titleRu} • ${republicRank.titleRu}",
                                    color = RomanParchment,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RomanDarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TriumphGold)
                        ) {
                            Text(
                                text = "🏆 ${totalVictories}В / ${totalDefeats}П",
                                color = TriumphGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = RomanGoldDark.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Core Vitals Grid: Strength | Morale | Discipline | Glory
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Strength
                        VitalStatBox(
                            modifier = Modifier.weight(1f),
                            icon = "🛡️",
                            label = "Численность",
                            valueText = "$totalSoldiers/$maxSoldiers",
                            subText = "$strengthPct% строй",
                            color = if (strengthPct < 70) DefeatRed else RomanGreenLight
                        )

                        // 2. Morale
                        VitalStatBox(
                            modifier = Modifier.weight(1f),
                            icon = "🔥",
                            label = "Боевой дух",
                            valueText = "$avgMorale%",
                            subText = if (avgMorale >= 80) "Непоколебим" else if (avgMorale >= 50) "Стойкий" else "Подавлен",
                            color = if (avgMorale < 50) DefeatRed else RomanGoldLight
                        )

                        // 3. Discipline
                        VitalStatBox(
                            modifier = Modifier.weight(1f),
                            icon = "📐",
                            label = "Дисциплина",
                            valueText = "$avgDiscipline",
                            subText = "$totalVeterans ветеранов",
                            color = RomanParchment
                        )

                        // 4. Glory
                        VitalStatBox(
                            modifier = Modifier.weight(1f),
                            icon = "👑",
                            label = "Слава SPQR",
                            valueText = "${resources.glory}",
                            subText = "Сенат: ${resources.senateFavor}%",
                            color = TriumphGold
                        )
                    }
                }
            }
        }

        // 2. SEASON & ACTIVE OPERATIONAL BRIEFING
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
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = seasonYear.season.icon, fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${seasonYear.season.titleRu}, ${seasonYear.yearBc} г. до н.э.",
                                color = RomanGoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Сезон ${seasonYear.seasonNumber}",
                                color = RomanTextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = seasonYear.season.effectDescRu,
                            color = RomanParchment,
                            fontSize = 11.sp
                        )

                        if (activeQuest != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🎯 Приказ Сената: «${activeQuest.titleRu}» (${activeQuest.currentProgress}/${activeQuest.targetCount})",
                                color = TriumphGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 3. ACTIONABLE ALERTS & PROBLEMS OF THE LEGION (if any)
        if (alerts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "⚡ Важные проблемы и события легиона",
                        color = RomanGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    alerts.forEach { alert ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(alert.severity.colorHex)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = alert.icon, fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = alert.titleRu,
                                            color = Color(alert.severity.colorHex),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = alert.descriptionRu,
                                            color = RomanTextLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        when (alert.actionType) {
                                            "REPLENISH_ALL" -> onReplenishAllCohorts()
                                            "CLAIM_QUEST" -> {
                                                val claimable = senateQuests.find { it.isFinished && !it.isClaimed }
                                                if (claimable != null) onClaimQuest(claimable.id) else onNavigateToSenate()
                                            }
                                            else -> {
                                                when (alert.targetScreen) {
                                                    "COHORTS" -> onNavigateToCohorts()
                                                    "SENATE" -> onNavigateToSenate()
                                                    "COMMANDERS" -> onNavigateToCommanders()
                                                    "MARKET" -> onNavigateToMarket()
                                                    "ALTAR" -> onNavigateToAltar()
                                                    "EXPEDITIONS" -> onNavigateToExpeditions()
                                                    else -> onNavigateToCohorts()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .height(34.dp)
                                        .padding(start = 6.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(alert.severity.colorHex)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = alert.actionLabelRu,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. STRATEGIC PRIORITIES & AUTO-PLAN COMMAND CENTER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 Приоритет стратегии легиона",
                            color = RomanGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedAutoPriority.titleRu,
                            color = TriumphGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Priority Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AutoPlanPriority.entries) { prio ->
                            val isSelected = prio == selectedAutoPriority
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) RomanCrimson else RomanDarkSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) RomanGold else RomanBronzeDark
                                ),
                                modifier = Modifier.clickable { selectedAutoPriority = prio }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = prio.icon, fontSize = 12.sp)
                                    Text(
                                        text = prio.titleRu,
                                        color = if (isSelected) RomanGoldLight else RomanTextLight,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = selectedAutoPriority.descRu,
                        color = RomanTextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Execution buttons: Auto-plan with priority & Season Plan Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAutoPlanSeason(selectedAutoPriority) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("quick_auto_plan_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RomanGoldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚡ Авто-план",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onOpenSeasonPlan,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("end_season_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (seasonalPlan.hasAnyAction()) "📜 Завершить сезон" else "📜 План сезона",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 5. ROUTINE AUTOMATION QUICK-ACTIONS BAR (Reduce micromanagement)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Replenish All
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    icon = "🛡️",
                    title = "Пополнить всех",
                    subtitle = "Лечение потерь",
                    onClick = onReplenishAllCohorts
                )

                // 2. Auto Equip All
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    icon = "🗡️",
                    title = "Авто-снаряжение",
                    subtitle = "Лучшее оружие",
                    onClick = onAutoEquipAll
                )

                // 3. Drill
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    icon = "🏋️",
                    title = "Марсово поле",
                    subtitle = "Муштра когорт",
                    onClick = onNavigateToTraining
                )
            }
        }

        // 6. PIXEL ART TOP-DOWN TACTICAL CASTRUM
        item {
            PixelCampTopDownView(
                seasonYear = seasonYear,
                buildings = buildings,
                cohorts = cohorts,
                commanders = commanders,
                onBuildingClick = { bType ->
                    selectedBuildingForModal = buildings.find { it.type == bType }
                }
            )
        }

        // 7. DIRECT DISTRICT NAVIGATION SHORTCUTS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DistrictShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏛️",
                    title = "Сенат SPQR",
                    subtitle = "Эдикты & Казна",
                    onClick = onNavigateToSenate
                )
                DistrictShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = "⚔️",
                    title = "Походы",
                    subtitle = "Кампании Рима",
                    onClick = onNavigateToExpeditions
                )
                DistrictShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = "🕊️",
                    title = "Алтарь Богов",
                    subtitle = "Авгурии & Обеты",
                    onClick = onNavigateToAltar
                )
                DistrictShortcutCard(
                    modifier = Modifier.weight(1f),
                    icon = "📖",
                    title = "Анналы",
                    subtitle = "Летопись побед",
                    onClick = onNavigateToChronicles
                )
            }
        }

        // 8. COHORT READINESS & ROSTER PREVIEW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚔️ Боеготовность манипул",
                    color = RomanGoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Все когорты →",
                    color = RomanGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToCohorts() }
                )
            }
        }

        items(cohorts) { cohort ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCohorts() },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cohort.name,
                            color = RomanParchment,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cohort.rankLabel} • Ур. ${cohort.level} • Мораль: ${cohort.morale}%",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ ${cohort.soldiers}/${cohort.maxSoldiers}",
                            color = if (cohort.soldiers < 50) DefeatRed else RomanGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "🎖️ ${cohort.veteransCount} вет.",
                            color = RomanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal if building clicked on map
    selectedBuildingForModal?.let { building ->
        BuildingUpgradeDialog(
            building = building,
            resources = resources,
            onUpgrade = {
                onBuildingUpgrade(building.type)
                selectedBuildingForModal = null
            },
            onDismiss = { selectedBuildingForModal = null }
        )
    }
}

@Composable
private fun VitalStatBox(
    icon: String,
    label: String,
    valueText: String,
    subText: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = RomanDarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, RomanBronzeDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = valueText,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = RomanTextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subText,
                color = RomanGoldLight.copy(alpha = 0.8f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionPill(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = RomanDarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = title,
                color = RomanGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = RomanTextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DistrictShortcutCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = RomanDarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, RomanGoldDark)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = title,
                color = RomanGoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = RomanTextMuted,
                fontSize = 8.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
