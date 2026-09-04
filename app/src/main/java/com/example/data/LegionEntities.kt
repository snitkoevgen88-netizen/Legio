package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val seasonIndex: Int,
    val seasonNumber: Int,
    val yearBc: Int,
    val denarii: Int,
    val provisions: Int,
    val glory: Int,
    val senateFavor: Int,
    val campLevel: Int,
    val totalVictories: Int,
    val totalGreatVictories: Int,
    val totalDefeats: Int,
    val longestWinStreak: Int,
    val currentWinStreak: Int,
    val isSoundEnabled: Boolean = true,
    // Banking state
    val bankingDepositDenarii: Int = 0,
    val bankingActiveLoanDenarii: Int = 0,
    val bankingLoanDueSeasons: Int = 0,
    val bankingCreditRating: Int = 100,
    // Market state
    val marketGrainPriceBuy: Int = 30,
    val marketGrainPriceSell: Int = 22,
    val marketTariffsUnlocked: Boolean = false,
    val marketGrainStock: Int = 100,
    // Magistracy & Elections
    val magistracyRankName: String = "TRIBUNUS_MILITUM",
    val campaignTargetRankName: String = "QUAESTOR",
    val campaignPlebeianSupportPct: Int = 50,
    val campaignPatricianSupportPct: Int = 45,
    val campaignBribedVotes: Int = 0,
    val campaignFunds: Int = 0,
    // Sacred Aquila state
    val aquilaNameRu: String = "Золотой Орел Марса (Aquila Martia)",
    val aquilaCustomVexillumMotto: String = "SENATVS POPVLVSQVE ROMANVS • LEGIO IV",
    val aquilaSacredEagleLevel: Int = 1,
    val aquilaStandardBearersCount: Int = 2,
    val aquilaRelicBonusesJoined: String = "",
    val aquilaDecorationsJoined: String = "",
    // Strategic province
    val selectedProvinceName: String = "LATIUM",
    // Divine blessing
    val activeBlessingGodName: String? = null,
    val activeBlessingRitualNameRu: String? = null,
    val activeBlessingSeasonsRemaining: Int = 0,
    // Seasonal plan
    val seasonalPlanTrainCohortId: String? = null,
    val seasonalPlanUpgradeBuildingTypeName: String? = null,
    val seasonalPlanLaunchedExpeditionId: String? = null,
    val seasonalPlanSelectedCommanderId: String? = null,
    val seasonalPlanSelectedCohortId: String? = null,
    val seasonalPlanSelectedTacticsName: String? = null,
    val activeEventId: String? = null
)

@Entity(tableName = "commanders")
data class CommanderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val level: Int,
    val xp: Int,
    val maxXp: Int,
    val traitName: String,
    val avatarSkinTone: Int,
    val hairStyle: Int,
    val helmetType: Int,
    val beardStyle: Int,
    val cloakColorIndex: Int,
    val expeditionsLed: Int,
    val victoriesCount: Int,
    val greatVictoriesCount: Int,
    val defeatsCount: Int,
    val isAlive: Boolean,
    val moodStatus: String,
    val unlockedTalentsJoined: String = "",
    val awardedCoronasJoined: String = ""
)

@Entity(tableName = "cohorts")
data class CohortEntity(
    @PrimaryKey val id: String,
    val name: String,
    val level: Int,
    val xp: Int,
    val maxXp: Int,
    val soldiers: Int,
    val maxSoldiers: Int,
    val veteransCount: Int,
    val morale: Int,
    val attackPower: Int,
    val defensePower: Int,
    val discipline: Int,
    val expeditionsCount: Int,
    val victoriesCount: Int,
    val greatVictoriesCount: Int,
    val defeatsCount: Int,
    val casualtiesSuffered: Int,
    val assignedCommanderId: String?,
    val traditionsJoined: String
)

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey val typeName: String,
    val level: Int
)

@Entity(tableName = "competing_legions")
data class CompetingLegionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ratingScore: Int,
    val victories: Int,
    val defeats: Int,
    val currentActivityRu: String,
    val badgeSymbol: String
)

@Entity(tableName = "chronicles")
data class ChronicleEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val seasonFormatted: String,
    val yearBc: Int,
    val headlineRu: String,
    val textRu: String,
    val outcomeName: String?,
    val commanderName: String,
    val cohortName: String,
    val casualties: Int,
    val lootDenarii: Int,
    val lootProvisions: Int,
    val gloryEarned: Int,
    val traditionUnlocked: String?
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val titleRu: String,
    val descRu: String,
    val icon: String,
    val bonusPerkRu: String,
    val isUnlocked: Boolean
)

@Entity(tableName = "doctrines")
data class DoctrineEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean
)

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey val id: String,
    val nameRu: String,
    val typeName: String,
    val tier: Int,
    val rarityName: String,
    val attackBonus: Int,
    val defenseBonus: Int,
    val moraleBonus: Int,
    val casualtyReductionPct: Int,
    val lootBonusPct: Int,
    val costDenarii: Int,
    val costMaterials: Int,
    val isCrafted: Boolean,
    val equippedCohortId: String?
)

@Entity(tableName = "senate_quests")
data class SenateQuestEntity(
    @PrimaryKey val id: String,
    val titleRu: String,
    val descRu: String,
    val categoryName: String,
    val targetCount: Int,
    val currentProgress: Int,
    val rewardDenarii: Int,
    val rewardSenateFavor: Int,
    val rewardGlory: Int,
    val isFinished: Boolean,
    val isClaimed: Boolean
)

@Entity(tableName = "senate_petitions")
data class SenatePetitionEntity(
    @PrimaryKey val id: String,
    val titleRu: String,
    val petitionerRu: String,
    val descRu: String,
    val factionName: String,
    val costDenarii: Int,
    val favorImpact: Int,
    val consequenceDescRu: String,
    val isResolved: Boolean
)

@Entity(tableName = "unit_allocations")
data class TrainingAllocationEntity(
    @PrimaryKey val unitTypeName: String,
    val allocatedCount: Int
)

@Entity(tableName = "provincial_investments")
data class ProvincialInvestmentEntity(
    @PrimaryKey val id: String,
    val titleRu: String,
    val provinceName: String,
    val level: Int,
    val maxLevel: Int,
    val baseYieldDenarii: Int,
    val baseYieldProvisions: Int,
    val upgradeCostDenarii: Int,
    val upgradeCostProvisions: Int
)

@Entity(tableName = "strategic_roads")
data class StrategicRoadEntity(
    @PrimaryKey val id: String,
    val nameRu: String,
    val provinceName: String,
    val level: Int,
    val maxLevel: Int,
    val costDenarii: Int,
    val costProvisions: Int,
    val mobilityBonus: Int,
    val tradeBonus: Int
)

@Entity(tableName = "legion_trophies")
data class LegionTrophyEntity(
    @PrimaryKey val id: String,
    val isUnlocked: Boolean
)

