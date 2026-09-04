package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GameStateEntity::class,
        CommanderEntity::class,
        CohortEntity::class,
        BuildingEntity::class,
        CompetingLegionEntity::class,
        ChronicleEntity::class,
        AchievementEntity::class,
        DoctrineEntity::class,
        EquipmentEntity::class,
        SenateQuestEntity::class,
        SenatePetitionEntity::class,
        TrainingAllocationEntity::class,
        ProvincialInvestmentEntity::class,
        StrategicRoadEntity::class,
        LegionTrophyEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun legionDao(): LegionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Upgrade game_state columns
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `isSoundEnabled` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `bankingDepositDenarii` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `bankingActiveLoanDenarii` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `bankingLoanDueSeasons` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `bankingCreditRating` INTEGER NOT NULL DEFAULT 100")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `marketGrainPriceBuy` INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `marketGrainPriceSell` INTEGER NOT NULL DEFAULT 22")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `marketTariffsUnlocked` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `marketGrainStock` INTEGER NOT NULL DEFAULT 100")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `magistracyRankName` TEXT NOT NULL DEFAULT 'TRIBUNUS_MILITUM'")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `campaignTargetRankName` TEXT NOT NULL DEFAULT 'QUAESTOR'")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `campaignPlebeianSupportPct` INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `campaignPatricianSupportPct` INTEGER NOT NULL DEFAULT 45")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `campaignBribedVotes` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `campaignFunds` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaNameRu` TEXT NOT NULL DEFAULT 'Золотой Орел Марса (Aquila Martia)'")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaCustomVexillumMotto` TEXT NOT NULL DEFAULT 'SENATVS POPVLVSQVE ROMANVS • LEGIO IV'")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaSacredEagleLevel` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaStandardBearersCount` INTEGER NOT NULL DEFAULT 2")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaRelicBonusesJoined` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `aquilaDecorationsJoined` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `selectedProvinceName` TEXT NOT NULL DEFAULT 'LATIUM'")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `activeBlessingGodName` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `activeBlessingRitualNameRu` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `activeBlessingSeasonsRemaining` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanTrainCohortId` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanUpgradeBuildingTypeName` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanLaunchedExpeditionId` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanSelectedCommanderId` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanSelectedCohortId` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `seasonalPlanSelectedTacticsName` TEXT")
                db.execSQL("ALTER TABLE `game_state` ADD COLUMN `activeEventId` TEXT")

                // 2. Upgrade commanders columns
                db.execSQL("ALTER TABLE `commanders` ADD COLUMN `unlockedTalentsJoined` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `commanders` ADD COLUMN `awardedCoronasJoined` TEXT NOT NULL DEFAULT ''")

                // 3. Create newly persisted systems tables
                db.execSQL("CREATE TABLE IF NOT EXISTS `doctrines` (`id` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `equipment` (`id` TEXT NOT NULL, `nameRu` TEXT NOT NULL, `typeName` TEXT NOT NULL, `tier` INTEGER NOT NULL, `rarityName` TEXT NOT NULL, `attackBonus` INTEGER NOT NULL, `defenseBonus` INTEGER NOT NULL, `moraleBonus` INTEGER NOT NULL, `casualtyReductionPct` INTEGER NOT NULL, `lootBonusPct` INTEGER NOT NULL, `costDenarii` INTEGER NOT NULL, `costMaterials` INTEGER NOT NULL, `isCrafted` INTEGER NOT NULL, `equippedCohortId` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `senate_quests` (`id` TEXT NOT NULL, `titleRu` TEXT NOT NULL, `descRu` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `targetCount` INTEGER NOT NULL, `currentProgress` INTEGER NOT NULL, `rewardDenarii` INTEGER NOT NULL, `rewardSenateFavor` INTEGER NOT NULL, `rewardGlory` INTEGER NOT NULL, `isFinished` INTEGER NOT NULL, `isClaimed` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `senate_petitions` (`id` TEXT NOT NULL, `titleRu` TEXT NOT NULL, `petitionerRu` TEXT NOT NULL, `descRu` TEXT NOT NULL, `factionName` TEXT NOT NULL, `costDenarii` INTEGER NOT NULL, `favorImpact` INTEGER NOT NULL, `consequenceDescRu` TEXT NOT NULL, `isResolved` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `unit_allocations` (`unitTypeName` TEXT NOT NULL, `allocatedCount` INTEGER NOT NULL, PRIMARY KEY(`unitTypeName`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `provincial_investments` (`id` TEXT NOT NULL, `titleRu` TEXT NOT NULL, `provinceName` TEXT NOT NULL, `level` INTEGER NOT NULL, `maxLevel` INTEGER NOT NULL, `baseYieldDenarii` INTEGER NOT NULL, `baseYieldProvisions` INTEGER NOT NULL, `upgradeCostDenarii` INTEGER NOT NULL, `upgradeCostProvisions` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `strategic_roads` (`id` TEXT NOT NULL, `nameRu` TEXT NOT NULL, `provinceName` TEXT NOT NULL, `level` INTEGER NOT NULL, `maxLevel` INTEGER NOT NULL, `costDenarii` INTEGER NOT NULL, `costProvisions` INTEGER NOT NULL, `mobilityBonus` INTEGER NOT NULL, `tradeBonus` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `legion_trophies` (`id` TEXT NOT NULL, `isUnlocked` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "legio_invicta_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

