package com.example.data

import androidx.room.withTransaction
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class GameRepository(
    private val database: AppDatabase,
    private val dao: LegionDao = database.legionDao()
) {
    private val saveMutex = Mutex()

    suspend fun loadSnapshot(): GameStateSnapshot? = withContext(Dispatchers.IO) {
        val stateEntity = dao.getGameStateDirect() ?: return@withContext null
        GameStateSnapshot(
            gameState = stateEntity,
            commanders = dao.getCommandersDirect(),
            cohorts = dao.getCohortsDirect(),
            buildings = dao.getBuildingsDirect(),
            competingLegions = dao.getCompetingLegionsDirect(),
            chronicles = dao.getChroniclesDirect(),
            achievements = dao.getAchievementsDirect(),
            doctrines = dao.getDoctrinesDirect(),
            equipment = dao.getEquipmentDirect(),
            senateQuests = dao.getSenateQuestsDirect(),
            senatePetitions = dao.getSenatePetitionsDirect(),
            unitAllocations = dao.getTrainingAllocationsDirect(),
            investments = dao.getInvestmentsDirect(),
            strategicRoads = dao.getStrategicRoadsDirect(),
            trophies = dao.getLegionTrophiesDirect()
        )
    }

    suspend fun saveSnapshot(snapshot: GameStateSnapshot) = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            database.withTransaction {
                dao.saveGameState(snapshot.gameState)
                dao.saveCommanders(snapshot.commanders)
                dao.saveCohorts(snapshot.cohorts)
                dao.saveBuildings(snapshot.buildings)
                dao.saveCompetingLegions(snapshot.competingLegions)
                dao.saveAchievements(snapshot.achievements)
                dao.saveDoctrines(snapshot.doctrines)
                dao.saveEquipment(snapshot.equipment)
                dao.saveSenateQuests(snapshot.senateQuests)
                dao.saveSenatePetitions(snapshot.senatePetitions)
                dao.saveTrainingAllocations(snapshot.unitAllocations)
                dao.saveInvestments(snapshot.investments)
                dao.saveStrategicRoads(snapshot.strategicRoads)
                dao.saveLegionTrophies(snapshot.trophies)
                dao.saveChronicles(snapshot.chronicles)
            }
        }
    }

    suspend fun appendChronicle(entry: ChronicleEntity) = withContext(Dispatchers.IO) {
        dao.saveChronicle(entry)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        saveMutex.withLock {
            database.withTransaction {
                dao.clearGameState()
                dao.clearCommanders()
                dao.clearCohorts()
                dao.clearBuildings()
                dao.clearChronicles()
                dao.clearDoctrines()
                dao.clearEquipment()
                dao.clearSenateQuests()
                dao.clearSenatePetitions()
                dao.clearTrainingAllocations()
                dao.clearInvestments()
                dao.clearStrategicRoads()
                dao.clearLegionTrophies()
            }
        }
    }
}

/**
 * Extension mapper to convert a complete GameUiState to GameStateSnapshot
 */
fun GameUiState.toSnapshot(): GameStateSnapshot {
    val stateEntity = GameStateEntity(
        id = 1,
        seasonIndex = seasonYear.seasonIndex,
        seasonNumber = seasonYear.seasonNumber,
        yearBc = seasonYear.yearBc,
        denarii = resources.denarii,
        provisions = resources.provisions,
        glory = resources.glory,
        senateFavor = resources.senateFavor,
        campLevel = campLevel,
        totalVictories = totalVictories,
        totalGreatVictories = totalGreatVictories,
        totalDefeats = totalDefeats,
        longestWinStreak = longestWinStreak,
        currentWinStreak = currentWinStreak,
        isSoundEnabled = isSoundEnabled,
        bankingDepositDenarii = bankingState.depositDenarii,
        bankingActiveLoanDenarii = bankingState.activeLoanDenarii,
        bankingLoanDueSeasons = bankingState.loanSeasonsRemaining,
        bankingCreditRating = 100,
        marketGrainPriceBuy = marketState.grainPriceBuy,
        marketGrainPriceSell = marketState.grainPriceSell,
        marketTariffsUnlocked = false,
        marketGrainStock = 100,
        magistracyRankName = magistracyRank.name,
        campaignTargetRankName = electionCampaign.targetRank.name,
        campaignPlebeianSupportPct = electionCampaign.plebeianSupportPct,
        campaignPatricianSupportPct = electionCampaign.patricianSupportPct,
        campaignBribedVotes = electionCampaign.briberyBudgetSpent,
        campaignFunds = electionCampaign.briberyBudgetSpent,
        aquilaNameRu = aquilaState.aquilaNameRu,
        aquilaCustomVexillumMotto = aquilaState.customVexillumMotto,
        aquilaSacredEagleLevel = aquilaState.eagleUpgradeLevel,
        aquilaStandardBearersCount = 2,
        aquilaRelicBonusesJoined = "",
        aquilaDecorationsJoined = "",
        selectedProvinceName = selectedProvince.name,
        activeBlessingGodName = activeBlessing?.god?.name,
        activeBlessingRitualNameRu = activeBlessing?.ritualNameRu,
        activeBlessingSeasonsRemaining = activeBlessing?.seasonsRemaining ?: 0,
        seasonalPlanTrainCohortId = seasonalPlan.trainCohortId,
        seasonalPlanUpgradeBuildingTypeName = seasonalPlan.upgradeBuildingType?.name,
        seasonalPlanLaunchedExpeditionId = seasonalPlan.launchedExpeditionId,
        seasonalPlanSelectedCommanderId = seasonalPlan.selectedCommanderId,
        seasonalPlanSelectedCohortId = seasonalPlan.selectedCohortId,
        seasonalPlanSelectedTacticsName = seasonalPlan.selectedTactics.name,
        activeEventId = activeEvent?.id
    )

    val cmdEntities = commanders.map {
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
            moodStatus = it.moodStatus,
            unlockedTalentsJoined = it.unlockedTalents.joinToString(",") { t -> t.name },
            awardedCoronasJoined = it.awardedCoronas.joinToString(",") { c -> c.name }
        )
    }

    val cohEntities = cohorts.map {
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
    }

    val bldEntities = buildings.map {
        BuildingEntity(typeName = it.type.name, level = it.level)
    }

    val legEntities = competingLegions.map {
        CompetingLegionEntity(
            id = it.id,
            name = it.name,
            ratingScore = it.ratingScore,
            victories = it.victories,
            defeats = it.defeats,
            currentActivityRu = it.currentActivityRu,
            badgeSymbol = it.badgeSymbol
        )
    }

    val chrEntities = chronicles.map {
        ChronicleEntity(
            id = it.id,
            timestamp = 0L,
            seasonFormatted = it.seasonFormatted,
            yearBc = it.yearBc,
            headlineRu = it.headlineRu,
            textRu = it.textRu,
            outcomeName = it.outcome?.name,
            commanderName = it.commanderName,
            cohortName = it.cohortName,
            casualties = it.casualties,
            lootDenarii = it.lootDenarii,
            lootProvisions = it.lootProvisions,
            gloryEarned = it.gloryEarned,
            traditionUnlocked = it.traditionUnlocked
        )
    }

    val achEntities = achievements.map {
        AchievementEntity(
            id = it.id,
            titleRu = it.titleRu,
            descRu = it.descRu,
            icon = it.icon,
            bonusPerkRu = it.bonusPerkRu,
            isUnlocked = it.isUnlocked
        )
    }

    val docEntities = doctrines.map {
        DoctrineEntity(id = it.id, isUnlocked = it.isUnlocked)
    }

    val eqEntities = equipment.map {
        EquipmentEntity(
            id = it.id,
            nameRu = it.nameRu,
            typeName = it.type.name,
            tier = it.temperLevel,
            rarityName = it.material.name,
            attackBonus = it.attackBonus,
            defenseBonus = it.defenseBonus,
            moraleBonus = it.moraleBonus,
            casualtyReductionPct = it.casualtyReductionPct,
            lootBonusPct = 0,
            costDenarii = it.costDenarii,
            costMaterials = it.forgeRequirementLevel,
            isCrafted = it.isCrafted,
            equippedCohortId = it.equippedCohortId
        )
    }

    val sqEntities = senateQuests.map {
        SenateQuestEntity(
            id = it.id,
            titleRu = it.titleRu,
            descRu = it.descriptionRu,
            categoryName = it.category.name,
            targetCount = it.targetCount,
            currentProgress = it.currentProgress,
            rewardDenarii = it.rewardDenarii,
            rewardSenateFavor = it.rewardSenateFavor,
            rewardGlory = it.rewardGlory,
            isFinished = it.isFinished,
            isClaimed = it.isClaimed
        )
    }

    val spEntities = senatePetitions.map {
        SenatePetitionEntity(
            id = it.id,
            titleRu = it.titleRu,
            petitionerRu = it.latinNameRu,
            descRu = it.descriptionRu,
            factionName = SenateFaction.OPTIMATES.name,
            costDenarii = it.denariiCost,
            favorImpact = it.favorCost,
            consequenceDescRu = it.rewardSummaryRu,
            isResolved = false
        )
    }

    val uaEntities = unitAllocations.map {
        TrainingAllocationEntity(
            unitTypeName = it.unitType.name,
            allocatedCount = it.allocatedCount
        )
    }

    val invEntities = investments.map {
        ProvincialInvestmentEntity(
            id = it.id,
            titleRu = it.titleRu,
            provinceName = it.regionRu,
            level = it.level,
            maxLevel = it.maxLevel,
            baseYieldDenarii = it.seasonalDenarii,
            baseYieldProvisions = it.seasonalProvisions,
            upgradeCostDenarii = it.baseCostDenarii,
            upgradeCostProvisions = 0
        )
    }

    val roadEntities = strategicRoads.map {
        StrategicRoadEntity(
            id = it.id,
            nameRu = it.nameRu,
            provinceName = it.connectingProvincesRu,
            level = if (it.isPaved) 1 else 0,
            maxLevel = 1,
            costDenarii = it.costDenarii,
            costProvisions = 0,
            mobilityBonus = 1,
            tradeBonus = 1
        )
    }

    val trophyEntities = trophies.map {
        LegionTrophyEntity(
            id = it.id,
            isUnlocked = it.isUnlocked
        )
    }

    return GameStateSnapshot(
        gameState = stateEntity,
        commanders = cmdEntities,
        cohorts = cohEntities,
        buildings = bldEntities,
        competingLegions = legEntities,
        chronicles = chrEntities,
        achievements = achEntities,
        doctrines = docEntities,
        equipment = eqEntities,
        senateQuests = sqEntities,
        senatePetitions = spEntities,
        unitAllocations = uaEntities,
        investments = invEntities,
        strategicRoads = roadEntities,
        trophies = trophyEntities
    )
}

/**
 * Reconstructs a full GameUiState from a GameStateSnapshot
 */
fun GameStateSnapshot.toUiState(): GameUiState {
    val defaults = GameDefaults

    val loadedCommanders = if (commanders.isNotEmpty()) {
        commanders.map { entity ->
            val unlockedTalents = entity.unlockedTalentsJoined
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { name -> runCatching { OfficerTalent.valueOf(name) }.getOrNull() }

            val awardedCoronas = entity.awardedCoronasJoined
                .split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { name -> runCatching { MilitaryCorona.valueOf(name) }.getOrNull() }

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
                moodStatus = entity.moodStatus,
                unlockedTalents = unlockedTalents,
                awardedCoronas = awardedCoronas
            )
        }
    } else defaults.createInitialCommanders()

    val loadedCohorts = if (cohorts.isNotEmpty()) {
        cohorts.map { entity ->
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
    } else defaults.createInitialCohorts()

    val loadedBuildings = if (buildings.isNotEmpty()) {
        val buildingMap = buildings.associateBy { it.typeName }
        BuildingType.entries.map { type ->
            val level = buildingMap[type.name]?.level ?: 1
            Building(type = type, level = level)
        }
    } else defaults.createInitialBuildings()

    val loadedLegions = if (competingLegions.isNotEmpty()) {
        competingLegions.map {
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
    } else defaults.createInitialCompetingLegions()

    val loadedChronicles = if (chronicles.isNotEmpty()) {
        chronicles.map {
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
    } else defaults.createInitialChronicle()

    val loadedAchievements = if (achievements.isNotEmpty()) {
        val unlockedMap = achievements.associate { it.id to it.isUnlocked }
        defaults.createInitialAchievements().map { ach ->
            ach.copy(isUnlocked = unlockedMap[ach.id] ?: ach.isUnlocked)
        }
    } else defaults.createInitialAchievements()

    val loadedDoctrines = if (doctrines.isNotEmpty()) {
        val unlockedMap = doctrines.associate { it.id to it.isUnlocked }
        defaults.createInitialDoctrines().map { doc ->
            doc.copy(isUnlocked = unlockedMap[doc.id] ?: doc.isUnlocked)
        }
    } else defaults.createInitialDoctrines()

    val loadedEquipment = if (equipment.isNotEmpty()) {
        val eqMap = equipment.associateBy { it.id }
        defaults.createInitialEquipment().map { baseEq ->
            val entity = eqMap[baseEq.id]
            if (entity != null) {
                baseEq.copy(
                    isCrafted = entity.isCrafted,
                    equippedCohortId = entity.equippedCohortId,
                    temperLevel = entity.tier
                )
            } else baseEq
        }
    } else defaults.createInitialEquipment()

    val loadedQuests = if (senateQuests.isNotEmpty()) {
        val questMap = senateQuests.associateBy { it.id }
        defaults.createInitialSenateQuests().map { baseQ ->
            val entity = questMap[baseQ.id]
            if (entity != null) {
                baseQ.copy(
                    currentProgress = entity.currentProgress,
                    isCompleted = entity.isFinished,
                    isClaimed = entity.isClaimed
                )
            } else baseQ
        }
    } else defaults.createInitialSenateQuests()

    val loadedPetitions = defaults.createInitialPetitions()

    val loadedUnitAllocations = if (unitAllocations.isNotEmpty()) {
        val allocMap = unitAllocations.associate { it.unitTypeName to it.allocatedCount }
        defaults.createInitialUnitAllocations().map { baseAlloc ->
            baseAlloc.copy(allocatedCount = allocMap[baseAlloc.unitType.name] ?: baseAlloc.allocatedCount)
        }
    } else defaults.createInitialUnitAllocations()

    val loadedInvestments = if (investments.isNotEmpty()) {
        val invMap = investments.associateBy { it.id }
        defaults.createInitialInvestments().map { baseInv ->
            val entity = invMap[baseInv.id]
            if (entity != null) {
                baseInv.copy(level = entity.level)
            } else baseInv
        }
    } else defaults.createInitialInvestments()

    val loadedRoads = if (strategicRoads.isNotEmpty()) {
        val roadMap = strategicRoads.associateBy { it.id }
        defaults.createInitialStrategicRoads().map { baseR ->
            val entity = roadMap[baseR.id]
            if (entity != null) {
                baseR.copy(isPaved = entity.level > 0)
            } else baseR
        }
    } else defaults.createInitialStrategicRoads()

    val loadedTrophies = if (trophies.isNotEmpty()) {
        val trMap = trophies.associate { it.id to it.isUnlocked }
        defaults.createInitialTrophies().map { baseTr ->
            baseTr.copy(isUnlocked = trMap[baseTr.id] ?: baseTr.isUnlocked)
        }
    } else defaults.createInitialTrophies()

    val activeBlessing = if (gameState.activeBlessingGodName != null && gameState.activeBlessingSeasonsRemaining > 0) {
        val god = runCatching { GodType.valueOf(gameState.activeBlessingGodName!!) }.getOrNull()
        if (god != null) {
            ActiveBlessing(
                god = god,
                ritualNameRu = gameState.activeBlessingRitualNameRu ?: "Священный обряд",
                effectRu = "Божественное благословение действует",
                seasonsRemaining = gameState.activeBlessingSeasonsRemaining
            )
        } else null
    } else null

    val seasonalPlan = SeasonalPlan(
        trainCohortId = gameState.seasonalPlanTrainCohortId,
        upgradeBuildingType = gameState.seasonalPlanUpgradeBuildingTypeName?.let { name ->
            runCatching { BuildingType.valueOf(name) }.getOrNull()
        },
        launchedExpeditionId = gameState.seasonalPlanLaunchedExpeditionId,
        selectedCommanderId = gameState.seasonalPlanSelectedCommanderId,
        selectedCohortId = gameState.seasonalPlanSelectedCohortId,
        selectedTactics = gameState.seasonalPlanSelectedTacticsName?.let { name ->
            runCatching { Tactics.valueOf(name) }.getOrNull()
        } ?: Tactics.BALANCED
    )

    val bankingState = RomanBankingState(
        depositDenarii = gameState.bankingDepositDenarii,
        activeLoanDenarii = gameState.bankingActiveLoanDenarii,
        loanSeasonsRemaining = gameState.bankingLoanDueSeasons
    )

    val marketState = MarketState(
        grainPriceBuy = gameState.marketGrainPriceBuy,
        grainPriceSell = gameState.marketGrainPriceSell
    )

    val magistracyRank = runCatching { MagistracyRank.valueOf(gameState.magistracyRankName) }
        .getOrDefault(MagistracyRank.TRIBUNUS_MILITUM)

    val electionCampaign = RomanElectionCampaign(
        targetRank = runCatching { MagistracyRank.valueOf(gameState.campaignTargetRankName) }
            .getOrDefault(MagistracyRank.QUAESTOR),
        plebeianSupportPct = gameState.campaignPlebeianSupportPct,
        patricianSupportPct = gameState.campaignPatricianSupportPct,
        briberyBudgetSpent = gameState.campaignFunds
    )

    val aquilaState = LegionAquilaState(
        aquilaNameRu = gameState.aquilaNameRu,
        customVexillumMotto = gameState.aquilaCustomVexillumMotto,
        eagleUpgradeLevel = gameState.aquilaSacredEagleLevel
    )

    val selectedProvince = runCatching { StrategicProvince.valueOf(gameState.selectedProvinceName) }
        .getOrDefault(StrategicProvince.LATIUM)

    return GameUiState(
        seasonYear = SeasonYear(
            seasonIndex = gameState.seasonIndex,
            seasonNumber = gameState.seasonNumber,
            yearBc = gameState.yearBc
        ),
        resources = LegionResources(
            denarii = gameState.denarii,
            provisions = gameState.provisions,
            glory = gameState.glory,
            senateFavor = gameState.senateFavor
        ),
        commanders = loadedCommanders,
        cohorts = loadedCohorts,
        buildings = loadedBuildings,
        availableExpeditions = defaults.getAllExpeditions(),
        competingLegions = loadedLegions,
        chronicles = loadedChronicles,
        achievements = loadedAchievements,
        doctrines = loadedDoctrines,
        equipment = loadedEquipment,
        senateQuests = loadedQuests,
        senatePetitions = loadedPetitions,
        unitAllocations = loadedUnitAllocations,
        seasonalPlan = seasonalPlan,
        activeBlessing = activeBlessing,
        rituals = defaults.createInitialRituals(),
        trophies = loadedTrophies,
        investments = loadedInvestments,
        bankingState = bankingState,
        marketState = marketState,
        magistracyRank = magistracyRank,
        electionCampaign = electionCampaign,
        aquilaState = aquilaState,
        strategicRoads = loadedRoads,
        selectedProvince = selectedProvince,
        activeEvent = null,
        lastExpeditionResult = null,
        showSeasonPlanDialog = false,
        showBattleResultDialog = false,
        showEventDialog = false,
        showGoldenAgeDialog = false,
        totalVictories = gameState.totalVictories,
        totalGreatVictories = gameState.totalGreatVictories,
        totalDefeats = gameState.totalDefeats,
        longestWinStreak = gameState.longestWinStreak,
        currentWinStreak = gameState.currentWinStreak,
        isSoundEnabled = gameState.isSoundEnabled
    )
}
