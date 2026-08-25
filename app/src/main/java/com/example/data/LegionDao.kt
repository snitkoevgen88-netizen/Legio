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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(state: GameStateEntity)

    @Query("SELECT * FROM commanders")
    fun getCommanders(): Flow<List<CommanderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCommanders(commanders: List<CommanderEntity>)

    @Query("SELECT * FROM cohorts")
    fun getCohorts(): Flow<List<CohortEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCohorts(cohorts: List<CohortEntity>)

    @Query("SELECT * FROM buildings")
    fun getBuildings(): Flow<List<BuildingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBuildings(buildings: List<BuildingEntity>)

    @Query("SELECT * FROM competing_legions")
    fun getCompetingLegions(): Flow<List<CompetingLegionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCompetingLegions(legions: List<CompetingLegionEntity>)

    @Query("SELECT * FROM chronicles ORDER BY timestamp DESC")
    fun getChronicles(): Flow<List<ChronicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChronicle(entry: ChronicleEntity)

    @Query("SELECT * FROM achievements")
    fun getAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAchievements(achievements: List<AchievementEntity>)

    @Query("DELETE FROM game_state")
    suspend fun clearGameState()
}
