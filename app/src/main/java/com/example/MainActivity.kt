package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

enum class NavigationTab(val titleRu: String, val icon: String) {
    CAMP("Лагерь", "🏛️"),
    COHORTS("Когорты", "⚔️"),
    ARMORY("Кузница", "🗡️"),
    DOCTRINES("Доктрины", "🛡️"),
    EXPEDITIONS("Походы", "🗺️"),
    SENATE("Сенат", "📜"),
    COMMANDERS("Офицеры", "🎖️"),
    BUILDINGS("Стройка", "🏗️"),
    CHRONICLES("Хроники", "📖"),
    RANKING("Рейтинг", "👑"),
    ACHIEVEMENTS("Слава", "🏆")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LegioInvictaTheme {
                LegioInvictaApp()
            }
        }
    }
}

@Composable
fun LegioInvictaApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavigationTab.CAMP) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = RomanDarkSurface,
        topBar = {
            TopResourceBar(
                seasonYear = uiState.seasonYear,
                resources = uiState.resources,
                isSoundEnabled = uiState.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() }
            )
        },
        bottomBar = {
            RomanBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.CAMP -> CampScreen(
                    seasonYear = uiState.seasonYear,
                    resources = uiState.resources,
                    campRank = uiState.campRank,
                    republicRank = uiState.republicRank,
                    buildings = uiState.buildings,
                    cohorts = uiState.cohorts,
                    commanders = uiState.commanders,
                    seasonalPlan = uiState.seasonalPlan,
                    onOpenSeasonPlan = { viewModel.openSeasonPlanDialog() },
                    onAutoPlanSeason = { viewModel.autoPlanSeason() },
                    onBuildingUpgrade = { bType -> viewModel.setPlanUpgradeBuilding(bType) },
                    onNavigateToCohorts = { currentTab = NavigationTab.COHORTS },
                    onNavigateToExpeditions = { currentTab = NavigationTab.EXPEDITIONS }
                )
                NavigationTab.COHORTS -> CohortsScreen(
                    cohorts = uiState.cohorts,
                    commanders = uiState.commanders,
                    resources = uiState.resources,
                    seasonalPlan = uiState.seasonalPlan,
                    onSetTrainingCohort = { cohortId -> viewModel.setPlanTrainingCohort(cohortId) },
                    onReplenishCohort = { cohortId -> viewModel.replenishCohort(cohortId) },
                    onReplenishAllCohorts = { viewModel.replenishAllCohorts() }
                )
                NavigationTab.ARMORY -> ArmoryScreen(
                    equipment = uiState.equipment,
                    cohorts = uiState.cohorts,
                    resources = uiState.resources,
                    onCraftItem = { itemId -> viewModel.craftEquipment(itemId) },
                    onEquipItem = { itemId, cohortId -> viewModel.equipItem(itemId, cohortId) },
                    onAutoEquipAll = { viewModel.autoEquipAll() }
                )
                NavigationTab.DOCTRINES -> DoctrinesScreen(
                    doctrines = uiState.doctrines,
                    resources = uiState.resources,
                    onUnlockDoctrine = { doctrineId -> viewModel.unlockDoctrine(doctrineId) }
                )
                NavigationTab.SENATE -> SenateScreen(
                    senateQuests = uiState.senateQuests,
                    resources = uiState.resources,
                    onClaimQuest = { questId -> viewModel.claimSenateQuest(questId) }
                )
                NavigationTab.BUILDINGS -> BuildingsScreen(
                    buildings = uiState.buildings,
                    resources = uiState.resources,
                    seasonalPlan = uiState.seasonalPlan,
                    onSetUpgradeBuilding = { bType -> viewModel.setPlanUpgradeBuilding(bType) }
                )
                NavigationTab.EXPEDITIONS -> ExpeditionsScreen(
                    availableExpeditions = uiState.availableExpeditions,
                    commanders = uiState.commanders,
                    cohorts = uiState.cohorts,
                    campLevel = uiState.campLevel,
                    resources = uiState.resources,
                    seasonalPlan = uiState.seasonalPlan,
                    onCalculateOdds = { exp, cmd, coh, tac -> viewModel.calculateBattleOdds(exp, cmd, coh, tac) },
                    onSetExpeditionPlan = { expId, cmdId, cohId, tac -> viewModel.setPlanExpedition(expId, cmdId, cohId, tac) },
                    onSetTactics = { tac -> viewModel.setPlanTactics(tac) },
                    onAutoSelectSquad = { expId -> viewModel.autoSelectExpeditionSquad(expId) }
                )
                NavigationTab.COMMANDERS -> CommandersScreen(
                    commanders = uiState.commanders,
                    resources = uiState.resources,
                    onRecruitCommander = { viewModel.recruitNewCommander() }
                )
                NavigationTab.CHRONICLES -> ChronicleScreen(
                    chronicles = uiState.chronicles
                )
                NavigationTab.RANKING -> RankingScreen(
                    competingLegions = uiState.competingLegions
                )
                NavigationTab.ACHIEVEMENTS -> AchievementsScreen(
                    achievements = uiState.achievements,
                    totalVictories = uiState.totalVictories,
                    totalGreatVictories = uiState.totalGreatVictories,
                    totalDefeats = uiState.totalDefeats,
                    currentStreak = uiState.currentWinStreak
                )
            }

            // MODALS & DIALOGS
            if (uiState.showSeasonPlanDialog) {
                SeasonPlanDialog(
                    seasonalPlan = uiState.seasonalPlan,
                    buildings = uiState.buildings,
                    cohorts = uiState.cohorts,
                    commanders = uiState.commanders,
                    availableExpeditions = uiState.availableExpeditions,
                    resources = uiState.resources,
                    seasonYear = uiState.seasonYear,
                    onAutoPlan = { viewModel.autoPlanSeason() },
                    onConfirm = { viewModel.confirmSeasonPlan() },
                    onDismiss = { viewModel.dismissSeasonPlanDialog() }
                )
            }

            if (uiState.showBattleResultDialog && uiState.lastExpeditionResult != null) {
                BattleResultDialog(
                    result = uiState.lastExpeditionResult!!,
                    onDismiss = { viewModel.dismissBattleResultDialog() }
                )
            }

            if (uiState.showEventDialog && uiState.activeEvent != null) {
                CampEventDialog(
                    event = uiState.activeEvent!!,
                    onChoiceSelected = { choice -> viewModel.resolveEventChoice(choice) }
                )
            }
        }
    }
}

@Composable
fun RomanBottomNavigationBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RomanDarkSurfaceHeader,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = RomanGoldDark.copy(alpha = 0.6f))
    ) {
        ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = RomanGoldLight,
            edgePadding = 6.dp,
            indicator = {},
            divider = {}
        ) {
            NavigationTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RomanCrimson else Color.Transparent)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = tab.icon, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = tab.titleRu,
                                color = if (isSelected) RomanGoldLight else RomanTextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
    }
}
