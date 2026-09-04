package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LegionDao {
    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    fun getGameState(): Flow<GameStateEntity?>

    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    suspend fun getGameStateDirect(): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)

    @Query("SELECT * FROM commanders")
    fun getCommanders(): Flow<List<CommanderEntity>>

    @Query("SELECT * FROM commanders")
    suspend fun getCommandersDirect(): List<CommanderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCommanders(commanders: List<CommanderEntity>)

    @Query("SELECT * FROM cohorts")
    fun getCohorts(): Flow<List<CohortEntity>>

    @Query("SELECT * FROM cohorts")
    suspend fun getCohortsDirect(): List<CohortEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCohorts(cohorts: List<CohortEntity>)

    @Query("SELECT * FROM buildings")
    fun getBuildings(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings")
    suspend fun getBuildingsDirect(): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBuildings(buildings: List<BuildingEntity>)

    @Query("SELECT * FROM competing_legions")
    fun getCompetingLegions(): Flow<List<CompetingLegionEntity>>

    @Query("SELECT * FROM competing_legions")
    suspend fun getCompetingLegionsDirect(): List<CompetingLegionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompetingLegions(legions: List<CompetingLegionEntity>)

    @Query("SELECT * FROM chronicles ORDER BY timestamp DESC")
    fun getChronicles(): Flow<List<ChronicleEntity>>

    @Query("SELECT * FROM chronicles ORDER BY timestamp DESC")
    suspend fun getChroniclesDirect(): List<ChronicleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChronicle(entry: ChronicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChronicles(entries: List<ChronicleEntity>)

    @Query("SELECT * FROM achievements")
    fun getAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAchievementsDirect(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM doctrines")
    fun getDoctrines(): Flow<List<DoctrineEntity>>

    @Query("SELECT * FROM doctrines")
    suspend fun getDoctrinesDirect(): List<DoctrineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDoctrines(doctrines: List<DoctrineEntity>)

    @Query("SELECT * FROM equipment")
    fun getEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment")
    suspend fun getEquipmentDirect(): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEquipment(equipment: List<EquipmentEntity>)

    @Query("SELECT * FROM senate_quests")
    fun getSenateQuests(): Flow<List<SenateQuestEntity>>

    @Query("SELECT * FROM senate_quests")
    suspend fun getSenateQuestsDirect(): List<SenateQuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSenateQuests(quests: List<SenateQuestEntity>)

    @Query("SELECT * FROM senate_petitions")
    fun getSenatePetitions(): Flow<List<SenatePetitionEntity>>

    @Query("SELECT * FROM senate_petitions")
    suspend fun getSenatePetitionsDirect(): List<SenatePetitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSenatePetitions(petitions: List<SenatePetitionEntity>)

    @Query("SELECT * FROM unit_allocations")
    fun getTrainingAllocations(): Flow<List<TrainingAllocationEntity>>

    @Query("SELECT * FROM unit_allocations")
    suspend fun getTrainingAllocationsDirect(): List<TrainingAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTrainingAllocations(allocations: List<TrainingAllocationEntity>)

    @Query("SELECT * FROM provincial_investments")
    fun getInvestments(): Flow<List<ProvincialInvestmentEntity>>

    @Query("SELECT * FROM provincial_investments")
    suspend fun getInvestmentsDirect(): List<ProvincialInvestmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInvestments(investments: List<ProvincialInvestmentEntity>)

    @Query("SELECT * FROM strategic_roads")
    fun getStrategicRoads(): Flow<List<StrategicRoadEntity>>

    @Query("SELECT * FROM strategic_roads")
    suspend fun getStrategicRoadsDirect(): List<StrategicRoadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStrategicRoads(roads: List<StrategicRoadEntity>)

    @Query("SELECT * FROM legion_trophies")
    fun getLegionTrophies(): Flow<List<LegionTrophyEntity>>

    @Query("SELECT * FROM legion_trophies")
    suspend fun getLegionTrophiesDirect(): List<LegionTrophyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLegionTrophies(trophies: List<LegionTrophyEntity>)

    @Query("DELETE FROM game_state")
    suspend fun clearGameState()

    @Query("DELETE FROM chronicles")
    suspend fun clearChronicles()

    @Query("DELETE FROM commanders")
    suspend fun clearCommanders()

    @Query("DELETE FROM cohorts")
    suspend fun clearCohorts()

    @Query("DELETE FROM buildings")
    suspend fun clearBuildings()

    @Query("DELETE FROM doctrines")
    suspend fun clearDoctrines()

    @Query("DELETE FROM equipment")
    suspend fun clearEquipment()

    @Query("DELETE FROM senate_quests")
    suspend fun clearSenateQuests()

    @Query("DELETE FROM senate_petitions")
    suspend fun clearSenatePetitions()

    @Query("DELETE FROM unit_allocations")
    suspend fun clearTrainingAllocations()

    @Query("DELETE FROM provincial_investments")
    suspend fun clearInvestments()

    @Query("DELETE FROM strategic_roads")
    suspend fun clearStrategicRoads()

    @Query("DELETE FROM legion_trophies")
    suspend fun clearLegionTrophies()
}

