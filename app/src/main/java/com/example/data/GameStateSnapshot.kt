package com.example.data

import com.example.model.*

/**
 * An immutable, complete snapshot of all persistent game systems in Legio Invicta.
 * Used for atomic persistence transactions and robust save/load verification.
 */
data class GameStateSnapshot(
    val gameState: GameStateEntity,
    val commanders: List<CommanderEntity>,
    val cohorts: List<CohortEntity>,
    val buildings: List<BuildingEntity>,
    val competingLegions: List<CompetingLegionEntity>,
    val chronicles: List<ChronicleEntity>,
    val achievements: List<AchievementEntity>,
    val doctrines: List<DoctrineEntity>,
    val equipment: List<EquipmentEntity>,
    val senateQuests: List<SenateQuestEntity>,
    val senatePetitions: List<SenatePetitionEntity>,
    val unitAllocations: List<TrainingAllocationEntity>,
    val investments: List<ProvincialInvestmentEntity>,
    val strategicRoads: List<StrategicRoadEntity>,
    val trophies: List<LegionTrophyEntity>
)
