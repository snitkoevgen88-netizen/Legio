package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CommanderTrait
import com.example.model.ExpeditionOutcome
import com.example.model.BuildingType

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
    val currentWinStreak: Int
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
    val moodStatus: String
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
