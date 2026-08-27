package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

enum class MainCategory(val titleRu: String, val icon: String) {
    CASTRUM("Каструм", "🏛️"),
    LEGION("Легион", "⚔️"),
    EXPEDITIONS("Походы", "🗺️"),
    SENATE("Сенат & Казна", "📜"),
    TRIUMPHS("Триумфы", "🏆")
}

enum class CastrumSubTab(val titleRu: String, val icon: String) {
    CAMP("Лагерь", "🏛️"),
    BUILDINGS("Стройка", "🏗️"),
    TRAINING("Муштра", "🏋️"),
    ALTAR("Алтарь", "🕊️")
}

enum class LegionSubTab(val titleRu: String, val icon: String) {
    COHORTS("Когорты", "⚔️"),
    ARMORY("Кузница", "🗡️"),
    COMMANDERS("Офицеры", "🎖️"),
    DOCTRINES("Доктрины", "🛡️")
}

enum class TriumphsSubTab(val titleRu: String, val icon: String) {
    COUNCIL("Совет & Трофеи", "🦅"),
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
    var mainCategory by remember { mutableStateOf(MainCategory.CASTRUM) }
    var castrumSubTab by remember { mutableStateOf(CastrumSubTab.CAMP) }
    var legionSubTab by remember { mutableStateOf(LegionSubTab.COHORTS) }
    var triumphsSubTab by remember { mutableStateOf(TriumphsSubTab.COUNCIL) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = RomanDarkSurface,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopResourceBar(
                    seasonYear = uiState.seasonYear,
                    resources = uiState.resources,
                    isSoundEnabled = uiState.isSoundEnabled,
                    onToggleSound = { viewModel.toggleSound() }
                )

                // Secondary Category Sub-tabs
                when (mainCategory) {
                    MainCategory.CASTRUM -> {
                        SubTabRow(
                            tabs = CastrumSubTab.entries.map { it.titleRu to it.icon },
                            selectedIndex = castrumSubTab.ordinal,
                            onTabSelected = { castrumSubTab = CastrumSubTab.entries[it] }
                        )
                    }
                    MainCategory.LEGION -> {
                        SubTabRow(
                            tabs = LegionSubTab.entries.map { it.titleRu to it.icon },
                            selectedIndex = legionSubTab.ordinal,
                            onTabSelected = { legionSubTab = LegionSubTab.entries[it] }
                        )
                    }
                    MainCategory.TRIUMPHS -> {
                        SubTabRow(
                            tabs = TriumphsSubTab.entries.map { it.titleRu to it.icon },
                            selectedIndex = triumphsSubTab.ordinal,
                            onTabSelected = { triumphsSubTab = TriumphsSubTab.entries[it] }
                        )
                    }
                    else -> Unit
                }
            }
        },
        bottomBar = {
            RomanBottomNavigationBar(
                currentCategory = mainCategory,
                onCategorySelected = { mainCategory = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (mainCategory) {
                MainCategory.CASTRUM -> {
                    when (castrumSubTab) {
                        CastrumSubTab.CAMP -> CampScreen(
                            seasonYear = uiState.seasonYear,
                            resources = uiState.resources,
                            campRank = uiState.campRank,
                            republicRank = uiState.republicRank,
                            buildings = uiState.buildings,
                            cohorts = uiState.cohorts,
                            commanders = uiState.commanders,
                            seasonalPlan = uiState.seasonalPlan,
                            activeBlessing = uiState.activeBlessing,
                            onOpenSeasonPlan = { viewModel.openSeasonPlanDialog() },
                            onAutoPlanSeason = { viewModel.autoPlanSeason() },
                            onBuildingUpgrade = { bType -> viewModel.setPlanUpgradeBuilding(bType) },
                            onNavigateToCohorts = {
                                mainCategory = MainCategory.LEGION
                                legionSubTab = LegionSubTab.COHORTS
                            },
                            onNavigateToExpeditions = {
                                mainCategory = MainCategory.EXPEDITIONS
                            },
                            onNavigateToAltar = {
                                castrumSubTab = CastrumSubTab.ALTAR
                            },
                            onNavigateToCouncil = {
                                mainCategory = MainCategory.TRIUMPHS
                                triumphsSubTab = TriumphsSubTab.COUNCIL
                            },
                            onNavigateToTraining = {
                                castrumSubTab = CastrumSubTab.TRAINING
                            }
                        )
                        CastrumSubTab.BUILDINGS -> BuildingsScreen(
                            buildings = uiState.buildings,
                            resources = uiState.resources,
                            seasonalPlan = uiState.seasonalPlan,
                            onSetUpgradeBuilding = { bType -> viewModel.setPlanUpgradeBuilding(bType) }
                        )
                        CastrumSubTab.TRAINING -> TrainingScreen(
                            unitAllocations = uiState.unitAllocations,
                            cohorts = uiState.cohorts,
                            resources = uiState.resources,
                            onUpdateCount = { unitType, count -> viewModel.updateUnitAllocationCount(unitType, count) },
                            onUpdateDrillIntensity = { unitType, intensity -> viewModel.updateUnitDrillIntensity(unitType, intensity) },
                            onUpdateTargetCohort = { unitType, cohortId -> viewModel.updateUnitTargetCohort(unitType, cohortId) },
                            onStartTraining = { unitType -> viewModel.startUnitTraining(unitType) },
                            onCancelTraining = { unitType -> viewModel.cancelUnitTraining(unitType) },
                            onInstantComplete = { unitType -> viewModel.instantCompleteUnitTraining(unitType) },
                            onAutoAllocateBalanced = { viewModel.autoAllocateBalancedTraining() }
                        )
                        CastrumSubTab.ALTAR -> AltarScreen(
                            rituals = uiState.rituals,
                            activeBlessing = uiState.activeBlessing,
                            resources = uiState.resources,
                            onPerformRitual = { ritualId -> viewModel.performDivineRitual(ritualId) }
                        )
                    }
                }

                MainCategory.LEGION -> {
                    when (legionSubTab) {
                        LegionSubTab.COHORTS -> CohortsScreen(
                            cohorts = uiState.cohorts,
                            commanders = uiState.commanders,
                            resources = uiState.resources,
                            seasonalPlan = uiState.seasonalPlan,
                            onSetTrainingCohort = { cohortId -> viewModel.setPlanTrainingCohort(cohortId) },
                            onReplenishCohort = { cohortId -> viewModel.replenishCohort(cohortId) },
                            onReplenishAllCohorts = { viewModel.replenishAllCohorts() },
                            onNavigateToTraining = {
                                mainCategory = MainCategory.CASTRUM
                                castrumSubTab = CastrumSubTab.TRAINING
                            }
                        )
                        LegionSubTab.ARMORY -> ArmoryScreen(
                            equipment = uiState.equipment,
                            cohorts = uiState.cohorts,
                            resources = uiState.resources,
                            onCraftItem = { itemId -> viewModel.craftEquipment(itemId) },
                            onTemperItem = { itemId -> viewModel.temperEquipmentItem(itemId) },
                            onSalvageItem = { itemId -> viewModel.salvageEquipmentItem(itemId) },
                            onEquipItem = { itemId, cohortId -> viewModel.equipItem(itemId, cohortId) },
                            onAutoEquipAll = { viewModel.autoEquipAll() }
                        )
                        LegionSubTab.COMMANDERS -> CommandersScreen(
                            commanders = uiState.commanders,
                            resources = uiState.resources,
                            onRecruitCommander = { viewModel.recruitNewCommander() }
                        )
                        LegionSubTab.DOCTRINES -> DoctrinesScreen(
                            doctrines = uiState.doctrines,
                            resources = uiState.resources,
                            onUnlockDoctrine = { doctrineId -> viewModel.unlockDoctrine(doctrineId) }
                        )
                    }
                }

                MainCategory.EXPEDITIONS -> ExpeditionsScreen(
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

                MainCategory.SENATE -> SenateScreen(
                    senateQuests = uiState.senateQuests,
                    senatePetitions = uiState.senatePetitions,
                    resources = uiState.resources,
                    investments = uiState.investments,
                    bankingState = uiState.bankingState,
                    marketState = uiState.marketState,
                    campLevel = uiState.campLevel,
                    totalSoldiers = uiState.cohorts.sumOf { it.soldiers },
                    onClaimQuest = { questId -> viewModel.claimSenateQuest(questId) },
                    onResolvePetition = { petitionId -> viewModel.resolveSenatePetition(petitionId) },
                    onHoldSpeech = { viewModel.holdCommanderSpeech() },
                    onDonativum = { viewModel.payDonativum() },
                    onLustratio = { viewModel.performLustratio() },
                    onTradeProvisions = { prov, den -> viewModel.tradeProvisions(prov, den) },
                    onDispatchCaravan = { viewModel.dispatchTradeCaravan() },
                    onCollectVectigal = { viewModel.collectProvincialVectigal() },
                    onUpgradeInvestment = { invId -> viewModel.upgradeInvestment(invId) },
                    onDepositBank = { amount -> viewModel.depositDenariiToBank(amount) },
                    onWithdrawBank = { amount -> viewModel.withdrawDenariiFromBank(amount) },
                    onTakeWarLoan = { amount -> viewModel.takeSenateWarLoan(amount) },
                    onRepayWarLoan = { viewModel.repaySenateWarLoanFull() },
                    onMintCoins = { viewModel.mintLegionCoins() },
                    onTradeProvisionsDynamic = { prov, den, isBuy -> viewModel.tradeProvisionsDynamic(prov, den, isBuy) },
                    onSellSpoils = { viewModel.sellWarSpoils() }
                )

                MainCategory.TRIUMPHS -> {
                    when (triumphsSubTab) {
                        TriumphsSubTab.COUNCIL -> CouncilScreen(
                            trophies = uiState.trophies,
                            resources = uiState.resources,
                            onHoldSpeech = { viewModel.holdCommanderSpeech() },
                            onPayDonativum = { viewModel.payDonativum() },
                            onPerformLustratio = { viewModel.performLustratio() }
                        )
                        TriumphsSubTab.CHRONICLES -> ChronicleScreen(
                            chronicles = uiState.chronicles
                        )
                        TriumphsSubTab.RANKING -> RankingScreen(
                            competingLegions = uiState.competingLegions
                        )
                        TriumphsSubTab.ACHIEVEMENTS -> AchievementsScreen(
                            achievements = uiState.achievements,
                            totalVictories = uiState.totalVictories,
                            totalGreatVictories = uiState.totalGreatVictories,
                            totalDefeats = uiState.totalDefeats,
                            currentStreak = uiState.currentWinStreak
                        )
                    }
                }
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
private fun SubTabRow(
    tabs: List<Pair<String, String>>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = Color(0xFF1E1712),
        modifier = Modifier.fillMaxWidth().border(width = 0.5.dp, color = RomanGoldDark.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                val isSelected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onTabSelected(index) }
                        .testTag("subtab_${index}"),
                    color = if (isSelected) RomanCrimson else Color(0xFF281E17),
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) RomanGoldLight else RomanGoldDark.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            color = if (isSelected) RomanGoldLight else RomanTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RomanBottomNavigationBar(
    currentCategory: MainCategory,
    onCategorySelected: (MainCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RomanDarkSurfaceHeader,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = RomanGoldDark.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainCategory.entries.forEach { category ->
                val isSelected = category == currentCategory
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCategorySelected(category) }
                        .testTag("main_nav_${category.name.lowercase()}"),
                    color = if (isSelected) RomanCrimson else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = category.icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = category.titleRu,
                            color = if (isSelected) RomanGoldLight else RomanTextMuted,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
