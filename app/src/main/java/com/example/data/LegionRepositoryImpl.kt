package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Implementation of LegionRepository that delegates to Room Dao.
 */
class LegionRepositoryImpl(private val dao: LegionDao) : LegionRepository {
    override fun getGameState(): Flow<GameStateEntity?> = dao.getGameState()
    override suspend fun saveGameState(entity: GameStateEntity) = dao.saveGameState(entity)

    override fun getCommanders(): Flow<List<CommanderEntity>> = dao.getCommanders()
    override suspend fun saveCommanders(commanders: List<CommanderEntity>) = dao.saveCommanders(commanders)

    override fun getCohorts(): Flow<List<CohortEntity>> = dao.getCohorts()
    override suspend fun saveCohorts(cohorts: List<CohortEntity>) = dao.saveCohorts(cohorts)

    override fun getBuildings(): Flow<List<BuildingEntity>> = dao.getBuildings()
    override suspend fun saveBuildings(buildings: List<BuildingEntity>) = dao.saveBuildings(buildings)

    override fun getCompetingLegions(): Flow<List<CompetingLegionEntity>> = dao.getCompetingLegions()
    override suspend fun saveCompetingLegions(legions: List<CompetingLegionEntity>) = dao.saveCompetingLegions(legions)

    override fun getChronicles(): Flow<List<ChronicleEntity>> = dao.getChronicles()
    override suspend fun saveChronicle(entry: ChronicleEntity) = dao.saveChronicle(entry)

    override fun getAchievements(): Flow<List<AchievementEntity>> = dao.getAchievements()
    override suspend fun saveAchievements(achievements: List<AchievementEntity>) = dao.saveAchievements(achievements)

    override suspend fun clearGameState() = dao.clearGameState()
}
