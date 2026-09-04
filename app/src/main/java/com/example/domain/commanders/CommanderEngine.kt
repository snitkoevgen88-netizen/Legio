package com.example.domain.commanders

import com.example.model.*
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CommanderEngine {

    private val ROMAN_PRAENOMINA = listOf("Марк", "Гай", "Луций", "Публий", "Тит", "Квинт", "Авл", "Сервий", "Тиберий", "Децим")
    private val ROMAN_COGNOMINA = listOf("Валерий", "Корнелий", "Клавдий", "Фабий", "Юлий", "Сципион", "Метелл", "Катон", "Фламиний", "Брут")

    /**
     * Calculates specialized combat modifiers provided by the commander.
     */
    data class CommanderCombatProfile(
        val effectiveAttack: Int,
        val effectiveDefense: Int,
        val greatVictoryBonusPct: Int,
        val disasterRiskChangePct: Int,
        val casualtyMitigationPct: Int,
        val veteranPreservationPct: Int,
        val lootMultiplierBonus: Float,
        val moraleAuraBonus: Int,
        val disciplineAuraBonus: Int,
        val tacticalAdviceRu: String
    )

    fun evaluateCombatProfile(
        commander: Commander,
        tactics: Tactics,
        scoutIntel: ScoutIntel? = null
    ): CommanderCombatProfile {
        var atk = commander.level * 2 + commander.trait.attackBonus
        var def = commander.level * 2 + commander.trait.defenseBonus
        var gvBonus = commander.trait.victoryBonusChance
        var disasterChange = commander.trait.disasterRiskChange
        var casMitigation = 0
        var vetPreservation = 15
        var lootBonus = 0f
        var moraleAura = 0
        var discAura = 0

        // Trait specialization
        when (commander.trait) {
            CommanderTrait.BRAVE -> {
                if (tactics == Tactics.AGGRESSIVE) {
                    atk += 4
                    gvBonus += 10
                }
            }
            CommanderTrait.CAUTIOUS -> {
                if (tactics == Tactics.CAUTIOUS || tactics == Tactics.TESTUDO) {
                    def += 5
                    disasterChange -= 10
                    casMitigation += 15
                }
            }
            CommanderTrait.TACTICIAN -> {
                if (scoutIntel != null && tactics == scoutIntel.recommendedTactic) {
                    atk += 3
                    def += 3
                    gvBonus += 15
                }
            }
            CommanderTrait.DISCIPLINED -> {
                discAura += 8
                vetPreservation += 20
                casMitigation += 10
            }
            CommanderTrait.AMBITIOUS -> {
                if (commander.victoriesCount >= 3) {
                    moraleAura += 10
                }
            }
            CommanderTrait.GREEDY -> {
                lootBonus += 0.40f
            }
            CommanderTrait.LOYAL -> {
                moraleAura += 8
                discAura += 5
            }
        }

        // Talent bonuses
        if (commander.unlockedTalents.contains(OfficerTalent.INVICTA_CHAMPION)) {
            atk += 10
            gvBonus += 10
        }
        if (commander.unlockedTalents.contains(OfficerTalent.IRON_DISCIPLINE)) {
            def += 4
            casMitigation += 20
        }
        if (commander.unlockedTalents.contains(OfficerTalent.LOGISTICS_GENIUS)) {
            lootBonus += 0.20f
        }
        if (commander.unlockedTalents.contains(OfficerTalent.SIEGE_ENGINEER)) {
            atk += 6
            gvBonus += 8
        }
        if (commander.unlockedTalents.contains(OfficerTalent.CAVALRY_TACTICIAN)) {
            atk += 5
            gvBonus += 10
            def += 2
        }

        // Military Corona awards
        if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_OBSIDIONALIS)) {
            disasterChange -= 25
            casMitigation += 20
        }
        if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_CIVICA)) {
            vetPreservation += 35
        }
        if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_MURALIS)) {
            atk += 5
        }
        if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_AUREA)) {
            gvBonus += 10
            moraleAura += 10
        }

        val advice = when {
            commander.trait == CommanderTrait.TACTICIAN && scoutIntel != null ->
                "Командир ${commander.name} изучил разведданные: рекомендует тактику ${scoutIntel.recommendedTactic.titleRu}."
            commander.trait == CommanderTrait.BRAVE ->
                "Командир ${commander.name} готов возглавить решительный штурм позиций врага!"
            commander.trait == CommanderTrait.CAUTIOUS ->
                "Командир ${commander.name} призывает сохранять строй и защищать жизни ветеранов."
            else ->
                "Офицер ${commander.name} готов вести легион в бой с максимальной отдачей."
        }

        return CommanderCombatProfile(
            effectiveAttack = atk,
            effectiveDefense = def,
            greatVictoryBonusPct = gvBonus,
            disasterRiskChangePct = disasterChange,
            casualtyMitigationPct = casMitigation,
            veteranPreservationPct = vetPreservation,
            lootMultiplierBonus = lootBonus,
            moraleAuraBonus = moraleAura,
            disciplineAuraBonus = discAura,
            tacticalAdviceRu = advice
        )
    }

    /**
     * Awards XP to a commander, handles leveling up and promotions.
     */
    fun awardXp(commander: Commander, xpGained: Int): Pair<Commander, Boolean> {
        var currentXp = commander.xp + xpGained
        var currentLevel = commander.level
        var currentMaxXp = commander.maxXp
        var wasPromoted = false

        while (currentXp >= currentMaxXp && currentLevel < 10) {
            currentXp -= currentMaxXp
            currentLevel += 1
            currentMaxXp = (currentMaxXp * 1.35f).toInt()
            if (currentLevel == 4 || currentLevel == 7) {
                wasPromoted = true
            }
        }

        val updatedRankTitle = when {
            currentLevel >= 7 -> "Легат легиона"
            currentLevel >= 4 -> "Военный трибун"
            else -> "Центурион"
        }

        val updated = commander.copy(
            level = currentLevel,
            xp = currentXp,
            maxXp = currentMaxXp,
            rankTitle = updatedRankTitle
        )

        return Pair(updated, wasPromoted)
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
            rankTitle = "Центурион",
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
                commanderId != null && cohort.assignedCommanderId == commanderId -> cohort.copy(assignedCommanderId = null)
                else -> cohort
            }
        }
    }
}
