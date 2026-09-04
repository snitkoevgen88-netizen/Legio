package com.example.domain.commanders

import com.example.model.*
import java.util.UUID
import kotlin.random.Random

object CommanderEngine {

    private val ROMAN_PRAENOMINA = listOf("Марк", "Гай", "Луций", "Публий", "Тит", "Квинт", "Авл", "Сервий", "Тиберий", "Децим")
    private val ROMAN_COGNOMINA = listOf("Валерий", "Корнелий", "Клавдий", "Фабий", "Юлий", "Сципион", "Метелл", "Катон", "Фламиний", "Брут")

    /**
     * Awards XP to a commander, handles leveling up and promotions.
     */
    fun awardXp(commander: Commander, xpGained: Int): Commander {
        var currentXp = commander.xp + xpGained
        var currentLevel = commander.level
        var currentMaxXp = commander.maxXp

        while (currentXp >= currentMaxXp && currentLevel < 10) {
            currentXp -= currentMaxXp
            currentLevel += 1
            currentMaxXp = (currentMaxXp * 1.35f).toInt()
        }

        val updatedRankTitle = when {
            currentLevel >= 7 -> "Легат легиона"
            currentLevel >= 4 -> "Военный трибун"
            else -> "Центурион"
        }

        return commander.copy(
            level = currentLevel,
            xp = currentXp,
            maxXp = currentMaxXp,
            rankTitle = updatedRankTitle
        )
    }

    /**
     * Unlocks an officer talent if level requirement is met and not already unlocked.
     */
    fun unlockTalent(commander: Commander, talent: OfficerTalent): Commander {
        if (commander.level < talent.levelReq || commander.unlockedTalents.contains(talent)) {
            return commander
        }
        return commander.copy(
            unlockedTalents = commander.unlockedTalents + talent
        )
    }

    /**
     * Bestows a military corona award upon a commander.
     */
    fun awardCorona(commander: Commander, corona: MilitaryCorona): Commander {
        if (commander.awardedCoronas.contains(corona)) return commander
        return commander.copy(
            awardedCoronas = commander.awardedCoronas + corona
        )
    }

    /**
     * Generates a procedurally created Roman officer candidate for recruitment.
     */
    fun generateRecruitCandidate(): Commander {
        val praenomen = ROMAN_PRAENOMINA.random()
        val cognomen = ROMAN_COGNOMINA.random()
        val fullName = "$praenomen $cognomen"
        val trait = CommanderTrait.entries.random()

        return Commander(
            id = "cmd_${UUID.randomUUID().toString().take(8)}",
            name = fullName,
            level = 1,
            xp = 0,
            maxXp = 100,
            rankTitle = "Центурион новобранцев",
            trait = trait,
            avatarSkinTone = Random.nextInt(4),
            hairStyle = Random.nextInt(4),
            helmetType = Random.nextInt(4),
            beardStyle = Random.nextInt(3),
            cloakColorIndex = Random.nextInt(4),
            expeditionsLed = 0,
            victoriesCount = 0,
            greatVictoriesCount = 0,
            defeatsCount = 0,
            isAlive = true,
            moodStatus = "Готов служить Республике"
        )
    }

    /**
     * Assigns or unassigns a commander to a cohort.
     */
    fun assignCommanderToCohort(
        cohorts: List<Cohort>,
        commanderId: String?,
        targetCohortId: String
    ): List<Cohort> {
        return cohorts.map { cohort ->
            when {
                cohort.id == targetCohortId -> cohort.copy(assignedCommanderId = commanderId)
                // If this commander was assigned elsewhere, clear it
                commanderId != null && cohort.assignedCommanderId == commanderId -> cohort.copy(assignedCommanderId = null)
                else -> cohort
            }
        }
    }
}
