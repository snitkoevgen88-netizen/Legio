package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.*
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class GameUiState(
    val seasonYear: SeasonYear = SeasonYear(seasonIndex = 0, seasonNumber = 1, yearBc = 315),
    val resources: LegionResources = LegionResources(denarii = 250, provisions = 180, glory = 25, senateFavor = 55),
    val commanders: List<Commander> = GameDefaults.createInitialCommanders(),
    val cohorts: List<Cohort> = GameDefaults.createInitialCohorts(),
    val buildings: List<Building> = GameDefaults.createInitialBuildings(),
    val availableExpeditions: List<Expedition> = GameDefaults.getAllExpeditions(),
    val competingLegions: List<CompetingLegion> = GameDefaults.createInitialCompetingLegions(),
    val chronicles: List<ChronicleEntry> = GameDefaults.createInitialChronicle(),
    val achievements: List<Achievement> = GameDefaults.createInitialAchievements(),
    val doctrines: List<MilitaryDoctrine> = GameDefaults.createInitialDoctrines(),
    val equipment: List<EquipmentItem> = GameDefaults.createInitialEquipment(),
    val senateQuests: List<SenateQuest> = GameDefaults.createInitialSenateQuests(),
    val seasonalPlan: SeasonalPlan = SeasonalPlan(),
    val activeEvent: CampEvent? = null,
    val lastExpeditionResult: ExpeditionResult? = null,
    val showSeasonPlanDialog: Boolean = false,
    val showBattleResultDialog: Boolean = false,
    val showEventDialog: Boolean = false,
    val showGoldenAgeDialog: Boolean = false,
    val totalVictories: Int = 3,
    val totalGreatVictories: Int = 1,
    val totalDefeats: Int = 0,
    val longestWinStreak: Int = 3,
    val currentWinStreak: Int = 3,
    val isSoundEnabled: Boolean = true
) {
    val campLevel: Int get() = buildings.sumOf { it.level }
    val campRank: CampRank get() = when {
        campLevel >= 14 -> CampRank.GRAND_CITADEL
        campLevel >= 9 -> CampRank.CASTRA_LEGIONIS
        campLevel >= 5 -> CampRank.FORTIFIED_OUTPOST
        else -> CampRank.FIELD_BIVOUAC
    }
    val republicRank: RepublicRank get() = when {
        resources.glory >= 160 -> RepublicRank.INVICTA
        resources.glory >= 90 -> RepublicRank.RENOWNED
        resources.glory >= 40 -> RepublicRank.RECOGNIZED
        else -> RepublicRank.PROVINCIAL
    }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val dao = database.legionDao()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadSavedGame()
    }

    private fun loadSavedGame() {
        viewModelScope.launch(Dispatchers.IO) {
            val stateEntity = dao.getGameState().firstOrNull()
            if (stateEntity != null) {
                val cmdEntities = dao.getCommanders().firstOrNull() ?: emptyList()
                val cohEntities = dao.getCohorts().firstOrNull() ?: emptyList()
                val bldEntities = dao.getBuildings().firstOrNull() ?: emptyList()
                val legEntities = dao.getCompetingLegions().firstOrNull() ?: emptyList()
                val chrEntities = dao.getChronicles().firstOrNull() ?: emptyList()
                val achEntities = dao.getAchievements().firstOrNull() ?: emptyList()

                val loadedCommanders = if (cmdEntities.isNotEmpty()) {
                    cmdEntities.map { entity ->
                        Commander(
                            id = entity.id,
                            name = entity.name,
                            level = entity.level,
                            xp = entity.xp,
                            maxXp = entity.maxXp,
                            trait = CommanderTrait.entries.find { it.name == entity.traitName } ?: CommanderTrait.BRAVE,
                            avatarSkinTone = entity.avatarSkinTone,
                            hairStyle = entity.hairStyle,
                            helmetType = entity.helmetType,
                            beardStyle = entity.beardStyle,
                            cloakColorIndex = entity.cloakColorIndex,
                            expeditionsLed = entity.expeditionsLed,
                            victoriesCount = entity.victoriesCount,
                            greatVictoriesCount = entity.greatVictoriesCount,
                            defeatsCount = entity.defeatsCount,
                            isAlive = entity.isAlive,
                            moodStatus = entity.moodStatus
                        )
                    }
                } else GameDefaults.createInitialCommanders()

                val loadedCohorts = if (cohEntities.isNotEmpty()) {
                    cohEntities.map { entity ->
                        Cohort(
                            id = entity.id,
                            name = entity.name,
                            level = entity.level,
                            xp = entity.xp,
                            maxXp = entity.maxXp,
                            soldiers = entity.soldiers,
                            maxSoldiers = entity.maxSoldiers,
                            veteransCount = entity.veteransCount,
                            morale = entity.morale,
                            attackPower = entity.attackPower,
                            defensePower = entity.defensePower,
                            discipline = entity.discipline,
                            expeditionsCount = entity.expeditionsCount,
                            victoriesCount = entity.victoriesCount,
                            greatVictoriesCount = entity.greatVictoriesCount,
                            defeatsCount = entity.defeatsCount,
                            casualtiesSuffered = entity.casualtiesSuffered,
                            assignedCommanderId = entity.assignedCommanderId,
                            traditions = entity.traditionsJoined.split(",").filter { it.isNotBlank() }
                        )
                    }
                } else GameDefaults.createInitialCohorts()

                val loadedBuildings = if (bldEntities.isNotEmpty()) {
                    bldEntities.mapNotNull { entity ->
                        val bType = BuildingType.entries.find { it.name == entity.typeName } ?: return@mapNotNull null
                        Building(type = bType, level = entity.level)
                    }
                } else GameDefaults.createInitialBuildings()

                val loadedLegions = if (legEntities.isNotEmpty()) {
                    legEntities.map {
                        CompetingLegion(
                            id = it.id,
                            name = it.name,
                            ratingScore = it.ratingScore,
                            victories = it.victories,
                            defeats = it.defeats,
                            currentActivityRu = it.currentActivityRu,
                            badgeSymbol = it.badgeSymbol
                        )
                    }
                } else GameDefaults.createInitialCompetingLegions()

                val loadedChronicles = if (chrEntities.isNotEmpty()) {
                    chrEntities.map {
                        ChronicleEntry(
                            id = it.id,
                            seasonFormatted = it.seasonFormatted,
                            yearBc = it.yearBc,
                            headlineRu = it.headlineRu,
                            textRu = it.textRu,
                            outcome = it.outcomeName?.let { name -> ExpeditionOutcome.entries.find { o -> o.name == name } },
                            commanderName = it.commanderName,
                            cohortName = it.cohortName,
                            casualties = it.casualties,
                            lootDenarii = it.lootDenarii,
                            lootProvisions = it.lootProvisions,
                            gloryEarned = it.gloryEarned,
                            traditionUnlocked = it.traditionUnlocked
                        )
                    }
                } else GameDefaults.createInitialChronicle()

                val loadedAchievements = if (achEntities.isNotEmpty()) {
                    achEntities.map {
                        Achievement(
                            id = it.id,
                            titleRu = it.titleRu,
                            descRu = it.descRu,
                            icon = it.icon,
                            bonusPerkRu = it.bonusPerkRu,
                            isUnlocked = it.isUnlocked
                        )
                    }
                } else GameDefaults.createInitialAchievements()

                _uiState.update {
                    it.copy(
                        seasonYear = SeasonYear(
                            seasonIndex = stateEntity.seasonIndex,
                            seasonNumber = stateEntity.seasonNumber,
                            yearBc = stateEntity.yearBc
                        ),
                        resources = LegionResources(
                            denarii = stateEntity.denarii,
                            provisions = stateEntity.provisions,
                            glory = stateEntity.glory,
                            senateFavor = stateEntity.senateFavor
                        ),
                        commanders = loadedCommanders,
                        cohorts = loadedCohorts,
                        buildings = loadedBuildings,
                        competingLegions = loadedLegions,
                        chronicles = loadedChronicles,
                        achievements = loadedAchievements,
                        totalVictories = stateEntity.totalVictories,
                        totalGreatVictories = stateEntity.totalGreatVictories,
                        totalDefeats = stateEntity.totalDefeats,
                        longestWinStreak = stateEntity.longestWinStreak,
                        currentWinStreak = stateEntity.currentWinStreak
                    )
                }
            }
        }
    }

    private fun persistGameState() {
        val s = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            dao.saveGameState(
                GameStateEntity(
                    seasonIndex = s.seasonYear.seasonIndex,
                    seasonNumber = s.seasonYear.seasonNumber,
                    yearBc = s.seasonYear.yearBc,
                    denarii = s.resources.denarii,
                    provisions = s.resources.provisions,
                    glory = s.resources.glory,
                    senateFavor = s.resources.senateFavor,
                    campLevel = s.campLevel,
                    totalVictories = s.totalVictories,
                    totalGreatVictories = s.totalGreatVictories,
                    totalDefeats = s.totalDefeats,
                    longestWinStreak = s.longestWinStreak,
                    currentWinStreak = s.currentWinStreak
                )
            )

            dao.saveCommanders(s.commanders.map {
                CommanderEntity(
                    id = it.id,
                    name = it.name,
                    level = it.level,
                    xp = it.xp,
                    maxXp = it.maxXp,
                    traitName = it.trait.name,
                    avatarSkinTone = it.avatarSkinTone,
                    hairStyle = it.hairStyle,
                    helmetType = it.helmetType,
                    beardStyle = it.beardStyle,
                    cloakColorIndex = it.cloakColorIndex,
                    expeditionsLed = it.expeditionsLed,
                    victoriesCount = it.victoriesCount,
                    greatVictoriesCount = it.greatVictoriesCount,
                    defeatsCount = it.defeatsCount,
                    isAlive = it.isAlive,
                    moodStatus = it.moodStatus
                )
            })

            dao.saveCohorts(s.cohorts.map {
                CohortEntity(
                    id = it.id,
                    name = it.name,
                    level = it.level,
                    xp = it.xp,
                    maxXp = it.maxXp,
                    soldiers = it.soldiers,
                    maxSoldiers = it.maxSoldiers,
                    veteransCount = it.veteransCount,
                    morale = it.morale,
                    attackPower = it.attackPower,
                    defensePower = it.defensePower,
                    discipline = it.discipline,
                    expeditionsCount = it.expeditionsCount,
                    victoriesCount = it.victoriesCount,
                    greatVictoriesCount = it.greatVictoriesCount,
                    defeatsCount = it.defeatsCount,
                    casualtiesSuffered = it.casualtiesSuffered,
                    assignedCommanderId = it.assignedCommanderId,
                    traditionsJoined = it.traditions.joinToString(",")
                )
            })

            dao.saveBuildings(s.buildings.map {
                BuildingEntity(typeName = it.type.name, level = it.level)
            })

            dao.saveCompetingLegions(s.competingLegions.map {
                CompetingLegionEntity(
                    id = it.id,
                    name = it.name,
                    ratingScore = it.ratingScore,
                    victories = it.victories,
                    defeats = it.defeats,
                    currentActivityRu = it.currentActivityRu,
                    badgeSymbol = it.badgeSymbol
                )
            })

            dao.saveAchievements(s.achievements.map {
                AchievementEntity(
                    id = it.id,
                    titleRu = it.titleRu,
                    descRu = it.descRu,
                    icon = it.icon,
                    bonusPerkRu = it.bonusPerkRu,
                    isUnlocked = it.isUnlocked
                )
            })
        }
    }

    // Toggle Sound
    fun toggleSound() {
        val newValue = !_uiState.value.isSoundEnabled
        SoundManager.isSoundEnabled = newValue
        _uiState.update { it.copy(isSoundEnabled = newValue) }
    }

    // Update Seasonal Plan
    fun setPlanTrainingCohort(cohortId: String?) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(trainCohortId = cohortId))
        }
        SoundManager.playDrumBeat()
    }

    fun setPlanUpgradeBuilding(type: BuildingType?) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(upgradeBuildingType = type))
        }
        SoundManager.playDrumBeat()
    }

    fun setPlanExpedition(expeditionId: String?, commanderId: String?, cohortId: String?, tactics: Tactics = Tactics.BALANCED) {
        _uiState.update {
            it.copy(
                seasonalPlan = it.seasonalPlan.copy(
                    launchedExpeditionId = expeditionId,
                    selectedCommanderId = commanderId,
                    selectedCohortId = cohortId,
                    selectedTactics = tactics
                )
            )
        }
        SoundManager.playWarHorn()
    }

    fun setPlanTactics(tactics: Tactics) {
        _uiState.update {
            it.copy(seasonalPlan = it.seasonalPlan.copy(selectedTactics = tactics))
        }
        SoundManager.playSwordClash()
    }

    fun openSeasonPlanDialog() {
        _uiState.update { it.copy(showSeasonPlanDialog = true) }
    }

    fun dismissSeasonPlanDialog() {
        _uiState.update { it.copy(showSeasonPlanDialog = false) }
    }

    fun dismissBattleResultDialog() {
        _uiState.update { it.copy(showBattleResultDialog = false) }
        // After battle, check if there's an event or golden age
        checkPendingEvents()
    }

    fun dismissGoldenAgeDialog() {
        _uiState.update { it.copy(showGoldenAgeDialog = false) }
    }

    // Calculate dynamic battle odds for UI
    fun calculateBattleOdds(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics
    ): BattleOddsPreview {
        var greatVictoryBase = 15
        var victoryBase = 45
        var partialBase = 25
        var defeatBase = 12
        var disasterBase = 3

        val state = _uiState.value
        val unlockedDoctrines = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

        // Equipped items on this cohort
        val equippedItems = state.equipment.filter { it.isCrafted && it.equippedCohortId == cohort.id }
        val eqAttack = equippedItems.sumOf { it.attackBonus }
        val eqDefense = equippedItems.sumOf { it.defenseBonus }
        val eqMorale = equippedItems.sumOf { it.moraleBonus }

        // Commander trait adjustments
        greatVictoryBase += commander.trait.victoryBonusChance
        disasterBase += commander.trait.disasterRiskChange
        victoryBase += (commander.level * 3)

        // Cohort experience, equipment & veterans
        victoryBase += (cohort.level * 4) + (cohort.veteransCount / 2) + (eqAttack / 2) + (eqDefense / 2)
        defeatBase -= (cohort.discipline / 5) + (eqDefense / 2)
        greatVictoryBase += (eqMorale / 10)

        // DOCTRINES SYNERGIES
        if (unlockedDoctrines.contains("doc_disciplina_ferrea")) {
            victoryBase += 6
            defeatBase -= 4
            disasterBase = max(1, disasterBase - 3)
        }
        if (unlockedDoctrines.contains("doc_pilum_volley")) {
            greatVictoryBase += 15
            victoryBase += 5
        }
        if (unlockedDoctrines.contains("doc_testudo")) {
            disasterBase = max(0, disasterBase - 8)
            defeatBase = max(2, defeatBase - 6)
            partialBase += 8
        }
        if (unlockedDoctrines.contains("doc_equites")) {
            greatVictoryBase += 10
            victoryBase += 6
        }

        // Tactics vs Enemy Intel synergy
        when (tactics) {
            Tactics.AGGRESSIVE -> {
                greatVictoryBase += 20
                disasterBase += 6
                defeatBase += 4
            }
            Tactics.CAUTIOUS -> {
                greatVictoryBase -= 10
                disasterBase -= 8
                partialBase += 15
            }
            Tactics.BALANCED -> {
                victoryBase += 6
            }
            Tactics.TESTUDO -> {
                disasterBase -= 12
                defeatBase -= 8
                partialBase += 12
            }
            Tactics.FLANK_AMBUSH -> {
                greatVictoryBase += 25
                disasterBase += 4
                victoryBase -= 4
            }
        }

        // Season effect (Summer boosts campaigns)
        if (state.seasonYear.season == Season.SUMMER) {
            victoryBase += 15
            greatVictoryBase += 5
        } else if (state.seasonYear.season == Season.WINTER) {
            disasterBase = max(1, disasterBase - 4)
        }

        // Difficulty scaling
        val diffPenalty = (expedition.difficulty - 1) * 8
        victoryBase -= diffPenalty
        greatVictoryBase -= (diffPenalty / 2)
        defeatBase += (diffPenalty / 2)
        disasterBase += (diffPenalty / 3)

        // Clamp
        greatVictoryBase = max(5, min(75, greatVictoryBase))
        disasterBase = max(0, min(35, disasterBase))
        val total = greatVictoryBase + victoryBase + partialBase + defeatBase + disasterBase
        val gvPct = (greatVictoryBase * 100) / total
        val vPct = (victoryBase * 100) / total
        val pPct = (partialBase * 100) / total
        val dPct = (defeatBase * 100) / total
        val disPct = max(0, 100 - (gvPct + vPct + pPct + dPct))

        val advice = when {
            unlockedDoctrines.contains("doc_pilum_volley") && tactics == Tactics.AGGRESSIVE ->
                "⚡ Залп пилумов сокрушит авангард врага при первой же атаке!"
            unlockedDoctrines.contains("doc_testudo") && tactics == Tactics.TESTUDO ->
                "🛡️ Стена щитов «Черепаха» отразит любые стрелы и сведет риск к минимуму."
            commander.trait == CommanderTrait.BRAVE && tactics == Tactics.AGGRESSIVE ->
                "⚔️ Марк Фабий пылает отвагой! Штурм принесет максимальный шанс триумфа."
            commander.trait == CommanderTrait.CAUTIOUS && tactics == Tactics.CAUTIOUS ->
                "🛡️ Гай Корнелий сомкнет щиты — риск гибели ветеранов сведен к минимуму."
            expedition.isSenateTrial ->
                "⚡ Приказ Сената чрезвычайно опасен! Требуется наивысшая дисциплина."
            else ->
                "⚖️ Манипулярный строй готов выполнить любой приказ центуриона."
        }

        return BattleOddsPreview(
            greatVictoryPct = gvPct,
            victoryPct = vPct,
            partialPct = pPct,
            defeatPct = dPct,
            disasterPct = disPct,
            adviceRu = advice
        )
    }

    // AUTO-ASSIST: Auto-select optimal squad and tactics for an expedition
    fun autoSelectExpeditionSquad(expeditionId: String) {
        val state = _uiState.value
        val expedition = state.availableExpeditions.find { it.id == expeditionId } ?: return
        val livingCommanders = state.commanders.filter { it.isAlive }
        if (livingCommanders.isEmpty()) return

        // 1. Pick best commander
        val bestCommander = livingCommanders.maxByOrNull { cmd ->
            var score = cmd.level * 10
            if (expedition.difficulty >= 4 && cmd.trait == CommanderTrait.CAUTIOUS) score += 20
            if (expedition.difficulty <= 2 && cmd.trait == CommanderTrait.BRAVE) score += 20
            if (expedition.rewardDenarii >= 120 && cmd.trait == CommanderTrait.GREEDY) score += 15
            score
        } ?: livingCommanders.first()

        // 2. Pick best ready cohort
        val bestCohort = state.cohorts.maxByOrNull { coh ->
            coh.soldiers * 2 + coh.discipline * 3 + coh.veteransCount * 5
        } ?: state.cohorts.first()

        // 3. Pick optimal counter-tactics
        val optimalTactics = when {
            expedition.difficulty >= 4 -> Tactics.TESTUDO
            expedition.scoutIntel.enemyTacticRu.contains("Засада", ignoreCase = true) -> Tactics.FLANK_AMBUSH
            expedition.scoutIntel.enemyTacticRu.contains("Конный", ignoreCase = true) -> Tactics.TESTUDO
            bestCommander.trait == CommanderTrait.BRAVE -> Tactics.AGGRESSIVE
            bestCommander.trait == CommanderTrait.CAUTIOUS -> Tactics.CAUTIOUS
            else -> Tactics.BALANCED
        }

        _uiState.update {
            it.copy(
                seasonalPlan = it.seasonalPlan.copy(
                    launchedExpeditionId = expedition.id,
                    selectedCommanderId = bestCommander.id,
                    selectedCohortId = bestCohort.id,
                    selectedTactics = optimalTactics
                )
            )
        }
        SoundManager.playWarHorn()
    }

    // AUTO-ASSIST: Auto-plan season (Building + Training + Expedition)
    fun autoPlanSeason() {
        val state = _uiState.value
        val res = state.resources
        var plannedDenarii = res.denarii
        var plannedProvisions = res.provisions

        // 1. Auto-select building upgrade
        val upgradableBuilding = state.buildings
            .filter { it.level < it.maxLevel && plannedDenarii >= it.upgradeCostDenarii && plannedProvisions >= it.upgradeCostProvisions }
            .minByOrNull { it.upgradeCostDenarii }

        if (upgradableBuilding != null) {
            plannedDenarii -= upgradableBuilding.upgradeCostDenarii
            plannedProvisions -= upgradableBuilding.upgradeCostProvisions
        }

        // 2. Auto-select training cohort
        val trainCohort = state.cohorts
            .filter { it.soldiers >= 40 }
            .minByOrNull { it.level }

        val trainCost = if (state.seasonYear.season == Season.SPRING) 24 else 30
        val hasTrainFunds = plannedDenarii >= trainCost && plannedProvisions >= 15
        val selectedTrainCohortId = if (hasTrainFunds && trainCohort != null) {
            plannedDenarii -= trainCost
            plannedProvisions -= 15
            trainCohort.id
        } else null

        // 3. Auto-select best expedition
        val affordableExpeditions = state.availableExpeditions.filter {
            plannedDenarii >= it.denariiCost && plannedProvisions >= it.provisionsCost
        }
        val bestExp = affordableExpeditions.maxByOrNull { it.rewardGlory }
        val livingCommanders = state.commanders.filter { it.isAlive }
        val bestCmd = livingCommanders.maxByOrNull { it.level }
        val bestCoh = state.cohorts.maxByOrNull { it.soldiers }

        _uiState.update {
            it.copy(
                seasonalPlan = SeasonalPlan(
                    trainCohortId = selectedTrainCohortId,
                    upgradeBuildingType = upgradableBuilding?.type,
                    launchedExpeditionId = bestExp?.id,
                    selectedCommanderId = bestCmd?.id,
                    selectedCohortId = bestCoh?.id,
                    selectedTactics = if (bestExp != null && bestExp.difficulty >= 3) Tactics.TESTUDO else Tactics.BALANCED
                )
            )
        }
        SoundManager.playWarHorn()
    }

    // AUTO-ASSIST: Auto-equip all cohorts with optimal crafted gear
    fun autoEquipAll() {
        val state = _uiState.value
        val craftedItems = state.equipment.filter { it.isCrafted }
        if (craftedItems.isEmpty() || state.cohorts.isEmpty()) return

        val cohorts = state.cohorts
        val updatedEquipment = state.equipment.map { item ->
            if (!item.isCrafted) item
            else {
                // Distribute strategically by type
                val assignedCohort = when (item.type) {
                    EquipmentType.WEAPON -> cohorts.maxByOrNull { it.attackPower }
                    EquipmentType.ARMOR, EquipmentType.HELMET -> cohorts.minByOrNull { it.defensePower }
                    EquipmentType.STANDARD -> cohorts.maxByOrNull { it.veteransCount }
                }
                item.copy(equippedCohortId = assignedCohort?.id)
            }
        }

        SoundManager.playGladiusClash()
        _uiState.update { it.copy(equipment = updatedEquipment) }
        persistGameState()
    }

    // AUTO-ASSIST: Replenish all cohorts at once
    fun replenishAllCohorts() {
        val state = _uiState.value
        var curDenarii = state.resources.denarii
        val updatedCohorts = state.cohorts.map { cohort ->
            val missing = cohort.maxSoldiers - cohort.soldiers
            if (missing > 0 && curDenarii >= missing) {
                curDenarii -= missing
                cohort.copy(soldiers = cohort.maxSoldiers, morale = 95)
            } else {
                cohort
            }
        }

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = curDenarii),
                cohorts = updatedCohorts
            )
        }
        persistGameState()
    }

    // MAIN SEASON CONFIRMATION LOOP
    fun confirmSeasonPlan() {
        val state = _uiState.value
        val plan = state.seasonalPlan

        var curDenarii = state.resources.denarii
        var curProvisions = state.resources.provisions
        var curGlory = state.resources.glory
        var curSenate = state.resources.senateFavor

        var updatedCommanders = state.commanders.toMutableList()
        var updatedCohorts = state.cohorts.toMutableList()
        var updatedBuildings = state.buildings.toMutableList()
        var updatedChronicles = state.chronicles.toMutableList()
        var updatedAchievements = state.achievements.toMutableList()

        var battleResult: ExpeditionResult? = null

        // 1. Process Building Upgrade
        if (plan.upgradeBuildingType != null) {
            val bIndex = updatedBuildings.indexOfFirst { it.type == plan.upgradeBuildingType }
            if (bIndex != -1) {
                val b = updatedBuildings[bIndex]
                if (curDenarii >= b.upgradeCostDenarii && curProvisions >= b.upgradeCostProvisions && b.level < b.maxLevel) {
                    curDenarii -= b.upgradeCostDenarii
                    curProvisions -= b.upgradeCostProvisions
                    updatedBuildings[bIndex] = b.copy(level = b.level + 1)
                    curGlory += 3
                }
            }
        }

        // 2. Process Training
        if (plan.trainCohortId != null) {
            val cIndex = updatedCohorts.indexOfFirst { it.id == plan.trainCohortId }
            if (cIndex != -1) {
                val cohort = updatedCohorts[cIndex]
                val trainingCostDenarii = if (state.seasonYear.season == Season.SPRING) 24 else 30
                val trainingCostProvisions = 15
                if (curDenarii >= trainingCostDenarii && curProvisions >= trainingCostProvisions) {
                    curDenarii -= trainingCostDenarii
                    curProvisions -= trainingCostProvisions
                    val campusLevel = updatedBuildings.find { it.type == BuildingType.CAMPUS_MARTIUS }?.level ?: 1
                    val xpGain = campusLevel * 35
                    val newXp = cohort.xp + xpGain
                    val newLevel = if (newXp >= cohort.maxXp) cohort.level + 1 else cohort.level
                    val finalXp = if (newXp >= cohort.maxXp) newXp - cohort.maxXp else newXp
                    val newVeterans = cohort.veteransCount + (if (cohort.level >= 3) 2 else 1)
                    updatedCohorts[cIndex] = cohort.copy(
                        level = newLevel,
                        xp = finalXp,
                        veteransCount = min(cohort.maxSoldiers, newVeterans),
                        attackPower = cohort.attackPower + 2,
                        discipline = cohort.discipline + 2
                    )
                }
            }
        }

        // 3. Process Expedition / Campaign
        if (plan.launchedExpeditionId != null) {
            val expedition = state.availableExpeditions.find { it.id == plan.launchedExpeditionId }
            val commander = updatedCommanders.find { it.id == plan.selectedCommanderId } ?: updatedCommanders.first()
            val cohort = updatedCohorts.find { it.id == plan.selectedCohortId } ?: updatedCohorts.first()
            val tactics = plan.selectedTactics

            if (expedition != null && curDenarii >= expedition.denariiCost && curProvisions >= expedition.provisionsCost) {
                curDenarii -= expedition.denariiCost
                curProvisions -= expedition.provisionsCost

                val odds = calculateBattleOdds(expedition, commander, cohort, tactics)
                val roll = Random.nextInt(100)

                val outcome = when {
                    roll < odds.greatVictoryPct -> ExpeditionOutcome.GREAT_VICTORY
                    roll < (odds.greatVictoryPct + odds.victoryPct) -> ExpeditionOutcome.VICTORY
                    roll < (odds.greatVictoryPct + odds.victoryPct + odds.partialPct) -> ExpeditionOutcome.PARTIAL_SUCCESS
                    roll < (odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct) -> ExpeditionOutcome.DEFEAT
                    else -> ExpeditionOutcome.DISASTER
                }

                // Calculate spoils & casualties
                val fabricaLevel = updatedBuildings.find { it.type == BuildingType.FABRICA }?.level ?: 1
                val valetudinariumLevel = updatedBuildings.find { it.type == BuildingType.VALETUDINARIUM }?.level ?: 1
                val unlockedDoctrines = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

                // Equipped gear mitigation
                val cohortEquipped = state.equipment.filter { it.isCrafted && it.equippedCohortId == cohort.id }
                val gearCasReductionPct = cohortEquipped.sumOf { it.casualtyReductionPct }

                val baseCasualties = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> Random.nextInt(0, 4)
                    ExpeditionOutcome.VICTORY -> Random.nextInt(4, 12)
                    ExpeditionOutcome.PARTIAL_SUCCESS -> Random.nextInt(10, 20)
                    ExpeditionOutcome.DEFEAT -> Random.nextInt(18, 32)
                    ExpeditionOutcome.DISASTER -> Random.nextInt(35, 55)
                }

                // Fabrica armor mitigation + Iron discipline doctrine + Gear
                var mitigatedCas = max(0, baseCasualties - (fabricaLevel * 2))
                if (unlockedDoctrines.contains("doc_disciplina_ferrea")) {
                    mitigatedCas = (mitigatedCas * 0.8f).toInt()
                }
                if (gearCasReductionPct > 0) {
                    mitigatedCas = max(0, (mitigatedCas * (1f - gearCasReductionPct / 100f)).toInt())
                }

                // Valetudinarium wounded treated + Field medics doctrine
                val healRatio = (valetudinariumLevel * 0.2f) + (if (unlockedDoctrines.contains("doc_medici_castrorum")) 0.35f else 0f)
                val woundedTreated = (mitigatedCas * healRatio).toInt()
                val netCasualties = max(0, mitigatedCas - woundedTreated)
                val veteransSaved = if (valetudinariumLevel >= 2 || unlockedDoctrines.contains("doc_medici_castrorum")) 2 else 0

                var lootMultiplier = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> 1.5f + (if (commander.trait == CommanderTrait.AMBITIOUS) 0.3f else 0f)
                    ExpeditionOutcome.VICTORY -> 1.1f + (if (commander.trait == CommanderTrait.GREEDY) 0.4f else 0f)
                    ExpeditionOutcome.PARTIAL_SUCCESS -> 0.6f
                    ExpeditionOutcome.DEFEAT -> 0.15f
                    ExpeditionOutcome.DISASTER -> 0.0f
                }
                if (unlockedDoctrines.contains("doc_equites") && outcome.isSuccess) {
                    lootMultiplier += 0.2f
                }
                if (unlockedDoctrines.contains("doc_art_tormentorum") && expedition.difficulty >= 3 && outcome.isSuccess) {
                    lootMultiplier += 0.35f
                }

                val lootDenarii = (expedition.rewardDenarii * lootMultiplier).toInt()
                val lootProvisions = (expedition.rewardProvisions * lootMultiplier).toInt()
                val gloryDelta = outcome.gloryDelta + (if (expedition.isSenateTrial && outcome.isSuccess) 15 else 0)

                curDenarii += lootDenarii
                curProvisions += lootProvisions
                curGlory = max(0, curGlory + gloryDelta)
                if (outcome.isSuccess) curSenate = min(100, curSenate + 6) else curSenate = max(0, curSenate - 8)

                val xpEarned = when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> 60
                    ExpeditionOutcome.VICTORY -> 40
                    ExpeditionOutcome.PARTIAL_SUCCESS -> 20
                    ExpeditionOutcome.DEFEAT -> 10
                    ExpeditionOutcome.DISASTER -> 5
                }

                // Update Cohort stats
                val cIndex = updatedCohorts.indexOfFirst { it.id == cohort.id }
                if (cIndex != -1) {
                    val currentSoldiers = max(15, cohort.soldiers - netCasualties)
                    val newTotalVictories = if (outcome.isSuccess) cohort.victoriesCount + 1 else cohort.victoriesCount
                    val newGreatVictories = if (outcome == ExpeditionOutcome.GREAT_VICTORY) cohort.greatVictoriesCount + 1 else cohort.greatVictoriesCount
                    val newDefeats = if (!outcome.isSuccess) cohort.defeatsCount + 1 else cohort.defeatsCount

                    var newTradition: String? = null
                    val tradList = cohort.traditions.toMutableList()
                    if (outcome == ExpeditionOutcome.GREAT_VICTORY && !tradList.contains("Победители ${expedition.regionRu}")) {
                        newTradition = "Победители ${expedition.regionRu}"
                        tradList.add(newTradition)
                    } else if (newTotalVictories >= 5 && !tradList.contains("Железный строй")) {
                        newTradition = "Железный строй"
                        tradList.add(newTradition)
                    }

                    updatedCohorts[cIndex] = cohort.copy(
                        soldiers = currentSoldiers,
                        xp = (cohort.xp + xpEarned) % cohort.maxXp,
                        level = cohort.level + ((cohort.xp + xpEarned) / cohort.maxXp),
                        expeditionsCount = cohort.expeditionsCount + 1,
                        victoriesCount = newTotalVictories,
                        greatVictoriesCount = newGreatVictories,
                        defeatsCount = newDefeats,
                        casualtiesSuffered = cohort.casualtiesSuffered + netCasualties,
                        traditions = tradList
                    )
                }

                // Update Commander stats
                val cmdIndex = updatedCommanders.indexOfFirst { it.id == commander.id }
                var commanderPromoted = false
                val commanderKilled = outcome == ExpeditionOutcome.DISASTER && Random.nextInt(100) < 25

                if (cmdIndex != -1) {
                    val cmd = updatedCommanders[cmdIndex]
                    val newCmdXp = cmd.xp + xpEarned
                    val newCmdLevel = if (newCmdXp >= cmd.maxXp) cmd.level + 1 else cmd.level
                    if (newCmdLevel > cmd.level && (newCmdLevel == 4 || newCmdLevel == 7)) {
                        commanderPromoted = true
                    }
                    updatedCommanders[cmdIndex] = cmd.copy(
                        level = newCmdLevel,
                        xp = newCmdXp % cmd.maxXp,
                        expeditionsLed = cmd.expeditionsLed + 1,
                        victoriesCount = if (outcome.isSuccess) cmd.victoriesCount + 1 else cmd.victoriesCount,
                        greatVictoriesCount = if (outcome == ExpeditionOutcome.GREAT_VICTORY) cmd.greatVictoriesCount + 1 else cmd.greatVictoriesCount,
                        defeatsCount = if (!outcome.isSuccess) cmd.defeatsCount + 1 else cmd.defeatsCount,
                        isAlive = !commanderKilled,
                        moodStatus = if (commanderKilled) "Погиб с честью на поле боя" else if (outcome.isSuccess) "Празднует победу в претории" else "Хмуро разбирает ошибки похода"
                    )
                }

                // Sound triggers
                when (outcome) {
                    ExpeditionOutcome.GREAT_VICTORY -> {
                        SoundManager.playTriumphFanfare()
                        SoundManager.playCoins()
                    }
                    ExpeditionOutcome.VICTORY -> {
                        SoundManager.playWarHorn()
                        SoundManager.playCoins()
                    }
                    ExpeditionOutcome.PARTIAL_SUCCESS -> SoundManager.playSwordClash()
                    ExpeditionOutcome.DEFEAT, ExpeditionOutcome.DISASTER -> SoundManager.playDrumBeat()
                }

                val narrative = generateBattleNarrative(expedition, commander, cohort, tactics, outcome, netCasualties, lootDenarii)

                battleResult = ExpeditionResult(
                    expedition = expedition,
                    commander = commander,
                    cohort = cohort,
                    tactics = tactics,
                    outcome = outcome,
                    casualties = netCasualties,
                    veteransSaved = veteransSaved,
                    woundedTreated = woundedTreated,
                    lootDenarii = lootDenarii,
                    lootProvisions = lootProvisions,
                    gloryDelta = gloryDelta,
                    xpEarned = xpEarned,
                    commanderPromoted = commanderPromoted,
                    newTradition = null,
                    commanderKilled = commanderKilled,
                    storyNarrativeRu = narrative
                )

                // Add to Chronicles
                val newChr = ChronicleEntry(
                    id = "chr_${System.currentTimeMillis()}",
                    seasonFormatted = state.seasonYear.formatted,
                    yearBc = state.seasonYear.yearBc,
                    headlineRu = "${outcome.icon} ${outcome.titleRu}: ${expedition.titleRu}",
                    textRu = narrative,
                    outcome = outcome,
                    commanderName = commander.name,
                    cohortName = cohort.name,
                    casualties = netCasualties,
                    lootDenarii = lootDenarii,
                    lootProvisions = lootProvisions,
                    gloryEarned = gloryDelta
                )
                updatedChronicles.add(0, newChr)
                viewModelScope.launch(Dispatchers.IO) { dao.saveChronicle(
                    ChronicleEntity(
                        id = newChr.id,
                        timestamp = System.currentTimeMillis(),
                        seasonFormatted = newChr.seasonFormatted,
                        yearBc = newChr.yearBc,
                        headlineRu = newChr.headlineRu,
                        textRu = newChr.textRu,
                        outcomeName = newChr.outcome?.name,
                        commanderName = newChr.commanderName,
                        cohortName = newChr.cohortName,
                        casualties = newChr.casualties,
                        lootDenarii = newChr.lootDenarii,
                        lootProvisions = newChr.lootProvisions,
                        gloryEarned = newChr.gloryEarned,
                        traditionUnlocked = newChr.traditionUnlocked
                    )
                ) }
            }
        }

        // 4. Autumn Harvest & Seasonal Passive upkeep & Imperium Tribute
        val unlockedDocs = state.doctrines.filter { it.isUnlocked }.map { it.id }.toSet()
        if (unlockedDocs.contains("doc_imperium")) {
            curDenarii += 45 // Dan' provintsiy tribute
        }

        if (state.seasonYear.season == Season.AUTUMN) {
            val horreumLevel = updatedBuildings.find { it.type == BuildingType.HORREUM }?.level ?: 1
            val harvestBonus = 50 + (horreumLevel * 30)
            curProvisions += harvestBonus
        }

        // Army Upkeep
        val totalSoldiers = updatedCohorts.sumOf { it.soldiers }
        val foodUpkeep = (totalSoldiers * 0.12f).toInt()
        curProvisions = max(0, curProvisions - foodUpkeep)

        // 5. Advance Season
        val nextSeasonIndex = (state.seasonYear.seasonIndex + 1)
        val nextSeasonNumber = state.seasonYear.seasonNumber + 1
        val nextYearBc = if (nextSeasonIndex % 4 == 0) state.seasonYear.yearBc - 1 else state.seasonYear.yearBc
        val newSeasonYear = SeasonYear(seasonIndex = nextSeasonIndex % 4, seasonNumber = nextSeasonNumber, yearBc = nextYearBc)

        // 6. Simulate Competing Legions
        val updatedLegions = simulateCompetingLegions(state.competingLegions, curGlory)

        // 7. Check Achievements & Senate Quests Progression
        val totalV = state.totalVictories + (if (battleResult?.outcome?.isSuccess == true) 1 else 0)
        val totalGV = state.totalGreatVictories + (if (battleResult?.outcome == ExpeditionOutcome.GREAT_VICTORY) 1 else 0)
        val totalD = state.totalDefeats + (if (battleResult?.outcome?.isSuccess == false) 1 else 0)
        val isFirst = updatedLegions.maxByOrNull { it.ratingScore }?.id == "legio_4_player"

        updatedAchievements = updatedAchievements.map { ach ->
            when (ach.id) {
                "ach_great_victory" -> if (totalGV >= 1) ach.copy(isUnlocked = true) else ach
                "ach_ten_victories" -> if (totalV >= 10) ach.copy(isUnlocked = true) else ach
                "ach_veteran_wall" -> if (updatedCohorts.any { it.veteransCount >= 20 }) ach.copy(isUnlocked = true) else ach
                "ach_best_legion" -> if (isFirst) ach.copy(isUnlocked = true) else ach
                "ach_pyrrhic_conqueror" -> if (battleResult?.expedition?.isSenateTrial == true && battleResult.outcome.isSuccess) ach.copy(isUnlocked = true) else ach
                "ach_grand_camp" -> if (updatedBuildings.all { it.level >= 3 }) ach.copy(isUnlocked = true) else ach
                else -> ach
            }
        }.toMutableList()

        val totalVeteransInLegion = updatedCohorts.sumOf { it.veteransCount }
        val isSamniumWon = battleResult?.outcome?.isSuccess == true && (battleResult.expedition.id == "exp_samnium_pacify" || battleResult.expedition.regionRu.contains("Самний", ignoreCase = true))

        val updatedQuests = state.senateQuests.map { quest ->
            when (quest.id) {
                "quest_appian_way" -> {
                    val newProg = min(quest.targetCount, totalV)
                    quest.copy(currentProgress = newProg, isCompleted = newProg >= quest.targetCount)
                }
                "quest_grain_reserve" -> {
                    val newProg = min(quest.targetCount, curProvisions)
                    quest.copy(currentProgress = newProg, isCompleted = newProg >= quest.targetCount)
                }
                "quest_veteran_reserve" -> {
                    val newProg = min(quest.targetCount, totalVeteransInLegion)
                    quest.copy(currentProgress = newProg, isCompleted = newProg >= quest.targetCount)
                }
                "quest_samnium_pacify" -> {
                    val newProg = if (isSamniumWon) quest.targetCount else quest.currentProgress
                    quest.copy(currentProgress = newProg, isCompleted = newProg >= quest.targetCount)
                }
                else -> quest
            }
        }

        // 8. Roll Random Event
        val rolledEvent = if (Random.nextInt(100) < 65) {
            GameDefaults.getRandomEvents().random()
        } else null

        val isGoldenAgeNow = curGlory >= 150 && curSenate >= 75
        val showGoldenAge = isGoldenAgeNow && !state.resources.isGoldenAge

        _uiState.update {
            it.copy(
                seasonYear = newSeasonYear,
                resources = LegionResources(
                    denarii = curDenarii,
                    provisions = curProvisions,
                    glory = curGlory,
                    senateFavor = curSenate
                ),
                commanders = updatedCommanders,
                cohorts = updatedCohorts,
                buildings = updatedBuildings,
                competingLegions = updatedLegions,
                chronicles = updatedChronicles,
                achievements = updatedAchievements,
                senateQuests = updatedQuests,
                seasonalPlan = SeasonalPlan(), // reset for new season
                activeEvent = rolledEvent,
                lastExpeditionResult = battleResult,
                showSeasonPlanDialog = false,
                showBattleResultDialog = battleResult != null,
                showEventDialog = (battleResult == null && rolledEvent != null),
                showGoldenAgeDialog = showGoldenAge,
                totalVictories = totalV,
                totalGreatVictories = totalGV,
                totalDefeats = totalD
            )
        }

        persistGameState()
    }

    private fun checkPendingEvents() {
        if (_uiState.value.activeEvent != null) {
            _uiState.update { it.copy(showEventDialog = true) }
        }
    }

    fun resolveEventChoice(choice: CampEventChoice) {
        val state = _uiState.value
        val res = state.resources
        val newDenarii = max(0, res.denarii + choice.denariiDelta)
        val newProvisions = max(0, res.provisions + choice.provisionsDelta)
        val newGlory = max(0, res.glory + choice.gloryDelta)
        val newSenate = max(0, min(100, res.senateFavor + choice.senateFavorDelta))

        // Update cohorts if XP or Morale delta
        val updatedCohorts = state.cohorts.map { coh ->
            coh.copy(
                xp = min(coh.maxXp, coh.xp + choice.cohortXpDelta),
                morale = max(20, min(100, coh.morale + choice.moraleDelta))
            )
        }

        // Add Chronicle note for the decision
        val eventChr = ChronicleEntry(
            id = "chr_evt_${System.currentTimeMillis()}",
            seasonFormatted = state.seasonYear.formatted,
            yearBc = state.seasonYear.yearBc,
            headlineRu = "🏛️ Решение легиона: ${state.activeEvent?.titleRu}",
            textRu = "${choice.textRu}. ${choice.resultLogRu}",
            outcome = null,
            commanderName = state.commanders.firstOrNull()?.name ?: "Совет легиона",
            cohortName = "Весь легион",
            casualties = 0,
            lootDenarii = choice.denariiDelta,
            lootProvisions = choice.provisionsDelta,
            gloryEarned = choice.gloryDelta
        )

        SoundManager.playCoins()

        _uiState.update {
            it.copy(
                resources = LegionResources(
                    denarii = newDenarii,
                    provisions = newProvisions,
                    glory = newGlory,
                    senateFavor = newSenate
                ),
                cohorts = updatedCohorts,
                chronicles = listOf(eventChr) + it.chronicles,
                activeEvent = null,
                showEventDialog = false
            )
        }

        persistGameState()
    }

    // Replenish Cohort Soldiers
    fun replenishCohort(cohortId: String) {
        val state = _uiState.value
        val cohort = state.cohorts.find { it.id == cohortId } ?: return
        val missing = cohort.maxSoldiers - cohort.soldiers
        if (missing <= 0) return

        val cost = missing * 1 // 1 denarius per recruit
        if (state.resources.denarii >= cost) {
            val updated = state.cohorts.map {
                if (it.id == cohortId) it.copy(soldiers = it.maxSoldiers, morale = 90) else it
            }
            SoundManager.playCoins()
            _uiState.update {
                it.copy(
                    resources = it.resources.copy(denarii = it.resources.denarii - cost),
                    cohorts = updated
                )
            }
            persistGameState()
        }
    }

    // Recruit New Promising Commander
    fun recruitNewCommander() {
        val state = _uiState.value
        val cost = 90
        if (state.resources.denarii < cost) return

        val romanNames = listOf("Тит Манлий", "Аппий Клавдий", "Гней Помпей", "Публий Деций", "Луций Эмилий", "Квинт Фабий")
        val randomName = romanNames.random()
        val randomTrait = CommanderTrait.entries.random()

        val newCommander = Commander(
            id = "cmd_${System.currentTimeMillis()}",
            name = randomName,
            level = 1,
            xp = 0,
            maxXp = 100,
            trait = randomTrait,
            avatarSkinTone = Random.nextInt(4),
            hairStyle = Random.nextInt(4),
            helmetType = Random.nextInt(4),
            beardStyle = Random.nextInt(3),
            cloakColorIndex = Random.nextInt(4),
            moodStatus = "Прибыл из Рима на службу"
        )

        SoundManager.playWarHorn()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii - cost),
                commanders = it.commanders + newCommander
            )
        }
        persistGameState()
    }

    private fun simulateCompetingLegions(legions: List<CompetingLegion>, playerGlory: Int): List<CompetingLegion> {
        val activities = listOf(
            "Штурмует крепость этрусков",
            "Охраняет долину реки По",
            "Пополняет новобранцев в Риме",
            "Патрулирует Аппиеву дорогу",
            "Празднует триумф на Капитолии",
            "Отражает набег галльских конников",
            "Разбивает лагерь у мыса Мизенум"
        )

        return legions.map { leg ->
            if (leg.id == "legio_4_player") {
                leg.copy(
                    ratingScore = min(99, playerGlory / 2 + 30),
                    victories = _uiState.value.totalVictories,
                    defeats = _uiState.value.totalDefeats
                )
            } else {
                val scoreChange = Random.nextInt(-2, 4)
                val newScore = max(30, min(98, leg.ratingScore + scoreChange))
                val isWin = Random.nextBoolean()
                leg.copy(
                    ratingScore = newScore,
                    victories = if (isWin) leg.victories + 1 else leg.victories,
                    defeats = if (!isWin) leg.defeats + 1 else leg.defeats,
                    currentActivityRu = activities.random()
                )
            }
        }.sortedByDescending { it.ratingScore }
    }

    fun unlockDoctrine(doctrineId: String) {
        val state = _uiState.value
        val doctrine = state.doctrines.find { it.id == doctrineId } ?: return
        if (doctrine.isUnlocked) return
        if (state.resources.glory < doctrine.costGlory) return

        val updatedDoctrines = state.doctrines.map {
            if (it.id == doctrineId) it.copy(isUnlocked = true) else it
        }

        SoundManager.playVictoryFanfare()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(glory = it.resources.glory - doctrine.costGlory),
                doctrines = updatedDoctrines
            )
        }
        persistGameState()
    }

    fun craftEquipment(itemId: String) {
        val state = _uiState.value
        val item = state.equipment.find { it.id == itemId } ?: return
        if (item.isCrafted) return
        if (state.resources.denarii < item.costDenarii) return

        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(isCrafted = true) else it
        }

        SoundManager.playGladiusClash()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(denarii = it.resources.denarii - item.costDenarii),
                equipment = updatedEquipment
            )
        }
        persistGameState()
    }

    fun equipItem(itemId: String, cohortId: String?) {
        val state = _uiState.value
        val updatedEquipment = state.equipment.map {
            if (it.id == itemId) it.copy(equippedCohortId = cohortId) else it
        }
        SoundManager.playMarchDrums()
        _uiState.update {
            it.copy(equipment = updatedEquipment)
        }
        persistGameState()
    }

    fun claimSenateQuest(questId: String) {
        val state = _uiState.value
        val quest = state.senateQuests.find { it.id == questId } ?: return
        if (!quest.isFinished || quest.isClaimed) return

        val updatedQuests = state.senateQuests.map {
            if (it.id == questId) it.copy(isClaimed = true) else it
        }

        SoundManager.playCoins()
        _uiState.update {
            it.copy(
                resources = it.resources.copy(
                    denarii = it.resources.denarii + quest.rewardDenarii,
                    senateFavor = min(100, it.resources.senateFavor + quest.rewardSenateFavor),
                    glory = it.resources.glory + quest.rewardGlory
                ),
                senateQuests = updatedQuests
            )
        }
        persistGameState()
    }

    private fun generateBattleNarrative(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        outcome: ExpeditionOutcome,
        casualties: Int,
        loot: Int
    ): String {
        return when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY ->
                "${commander.name} применил тактику «${tactics.titleRu}». Когорта ${cohort.name} сомкнула ряды и опрокинула врага мощным ударом пилумов! Вражеский лагерь захвачен. Триумф Рима! Потери: $casualties, добыча: +$loot золота."
            ExpeditionOutcome.VICTORY ->
                "В тяжелом полевом бою в регионе ${expedition.regionRu} ${commander.name} сломил сопротивление врага. ${cohort.name} держала строй до конца. Победа за нами! Потери: $casualties, трофеи: +$loot монет."
            ExpeditionOutcome.PARTIAL_SUCCESS ->
                "Столкновение завершилось упорным отходом противника на укрепленные высоты. Часть обоза захвачена, но основные силы врага избежали окружения. Потери: $casualties."
            ExpeditionOutcome.DEFEAT ->
                "Разведка оказалась неполной: превосходящие силы противника зажали манипулы ${cohort.name} в теснине. ${commander.name} сумел сохранить знамя и организованно отойти в лагерь. Потери: $casualties легионеров."
            ExpeditionOutcome.DISASTER ->
                "Катастрофа в горах! Внезапный конный фланговый налет врага разорвал когорту. Оставшиеся ветераны пробились сквозь кольцо окружения. Черный день для легиона! Потери: $casualties легионеров."
        }
    }
}
