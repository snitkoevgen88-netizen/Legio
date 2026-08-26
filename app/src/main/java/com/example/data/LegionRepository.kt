package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface that abstracts data persistence for the game.
 * Implemented on top of Room (LegionDao) in LegionRepositoryImpl.
 */
interface LegionRepository {
    fun getGameState(): Flow<GameStateEntity?>
    suspend fun saveGameState(entity: GameStateEntity)

    fun getCommanders(): Flow<List<CommanderEntity>>
    suspend fun saveCommanders(commanders: List<CommanderEntity>)

    fun getCohorts(): Flow<List<CohortEntity>>
    suspend fun saveCohorts(cohorts: List<CohortEntity>)

    fun getBuildings(): Flow<List<BuildingEntity>>
    suspend fun saveBuildings(buildings: List<BuildingEntity>)

    fun getCompetingLegions(): Flow<List<CompetingLegionEntity>>
    suspend fun saveCompetingLegions(legions: List<CompetingLegionEntity>)

    fun getChronicles(): Flow<List<ChronicleEntity>>
    suspend fun saveChronicle(entry: ChronicleEntity)

    fun getAchievements(): Flow<List<AchievementEntity>>
    suspend fun saveAchievements(achievements: List<AchievementEntity>)

    suspend fun clearGameState()
}
