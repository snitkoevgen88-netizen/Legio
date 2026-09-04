package com.example.domain.battle

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BattleEngine {

    /**
     * Calculates mathematically sound, normalized battle odds [0..100] that always strictly sum to 100%.
     */
    fun calculateBattleOdds(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        campLevel: Int,
        activeBlessing: ActiveBlessing?,
        doctrines: List<MilitaryDoctrine>,
        equipment: List<EquipmentItem>
    ): BattleOddsPreview {
        val equippedForCohort = equipment.filter { it.equippedCohortId == cohort.id && it.isCrafted }
        val gearAttackBonus = equippedForCohort.sumOf { it.totalAttackBonus }
        val gearDefenseBonus = equippedForCohort.sumOf { it.totalDefenseBonus }
        val gearMoraleBonus = equippedForCohort.sumOf { it.moraleBonus }

        val doctrineAttackBonus = if (doctrines.any { it.id == "doc_pilum" && it.isUnlocked }) 3 else 0
        val doctrineDefenseBonus = if (doctrines.any { it.id == "doc_scutum" && it.isUnlocked }) 3 else 0
        val doctrineMoraleBonus = if (doctrines.any { it.id == "doc_aquila" && it.isUnlocked }) 5 else 0

        val effectiveCommanderAttack = commander.level * 2 + commander.trait.attackBonus +
                if (commander.unlockedTalents.contains(OfficerTalent.INVICTA_CHAMPION)) 10 else 0

        val effectiveCommanderDefense = commander.level * 2 + commander.trait.defenseBonus +
                if (commander.unlockedTalents.contains(OfficerTalent.IRON_DISCIPLINE)) 4 else 0

        val totalPlayerAttack = cohort.attackPower + effectiveCommanderAttack + gearAttackBonus + doctrineAttackBonus + tactics.attackMod
        val totalPlayerDefense = cohort.defensePower + effectiveCommanderDefense + gearDefenseBonus + doctrineDefenseBonus + tactics.defenseMod
        val totalPlayerMorale = min(100, cohort.morale + gearMoraleBonus + doctrineMoraleBonus + (campLevel * 2))

        val enemyDifficultyRating = expedition.difficulty * 15 + 10
        val powerRatio = if (enemyDifficultyRating > 0) (totalPlayerAttack + totalPlayerDefense).toFloat() / (enemyDifficultyRating * 2).toFloat() else 1.2f

        var greatVictoryWeight = max(2, (powerRatio * 22 + totalPlayerMorale * 0.12f + commander.trait.victoryBonusChance + tactics.greatVictoryBonusPct).toInt())
        var victoryWeight = max(10, (powerRatio * 42 + cohort.discipline * 0.4f).toInt())
        var partialWeight = max(8, (25 + cohort.defensePower * 0.3f).toInt())
        var defeatWeight = max(4, (35 - powerRatio * 18 + expedition.difficulty * 4).toInt())
        var disasterWeight = max(0, (15 - powerRatio * 12 - totalPlayerMorale * 0.08f + commander.trait.disasterRiskChange + tactics.disasterRiskPct).toInt())

        // Active divine blessings modifiers
        if (activeBlessing?.god == GodType.MARS) {
            greatVictoryWeight = (greatVictoryWeight * 1.35f).toInt()
            victoryWeight = (victoryWeight * 1.2f).toInt()
        } else if (activeBlessing?.god == GodType.FORTUNA) {
            disasterWeight = max(0, disasterWeight - 15)
            victoryWeight = (victoryWeight * 1.15f).toInt()
        } else if (activeBlessing?.god == GodType.MINERVA) {
            defeatWeight = max(2, defeatWeight - 10)
            partialWeight = (partialWeight * 1.25f).toInt()
        }

        // Corona modifiers
        if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_OBSIDIONALIS)) {
            disasterWeight = 0
            defeatWeight = max(1, (defeatWeight * 0.5f).toInt())
        }

        // Safe positive clamp
        greatVictoryWeight = max(1, greatVictoryWeight)
        victoryWeight = max(1, victoryWeight)
        partialWeight = max(1, partialWeight)
        defeatWeight = max(1, defeatWeight)
        disasterWeight = max(0, disasterWeight)

        val totalWeight = (greatVictoryWeight + victoryWeight + partialWeight + defeatWeight + disasterWeight).toFloat()

        var gvPct = ((greatVictoryWeight / totalWeight) * 100).toInt()
        var vicPct = ((victoryWeight / totalWeight) * 100).toInt()
        var parPct = ((partialWeight / totalWeight) * 100).toInt()
        var defPct = ((defeatWeight / totalWeight) * 100).toInt()
        var disPct = ((disasterWeight / totalWeight) * 100).toInt()

        // Distribute remainder so the sum is always EXACTLY 100%
        val remainder = 100 - (gvPct + vicPct + parPct + defPct + disPct)
        if (remainder > 0) {
            vicPct += remainder
        } else if (remainder < 0) {
            var over = -remainder
            val reduceFromVic = min(over, max(0, vicPct - 1))
            vicPct -= reduceFromVic
            over -= reduceFromVic
            if (over > 0) {
                val reduceFromDef = min(over, max(0, defPct - 1))
                defPct -= reduceFromDef
            }
        }

        val advice = when {
            gvPct + vicPct >= 70 -> "Благоприятный прогноз: наши манипулы имеют подавляющее преимущество."
            disPct >= 20 -> "Опасность засады! Рекомендуется сменить тактику на оборонительную (Testudo)."
            else -> "Сложная кампания: исход зависит от стойкости командира и выдержки солдат."
        }

        return BattleOddsPreview(
            greatVictoryPct = gvPct,
            victoryPct = vicPct,
            partialPct = parPct,
            defeatPct = defPct,
            disasterPct = disPct,
            adviceRu = advice
        )
    }

    /**
     * Resolves the full outcome of a battle simulation.
     */
    fun resolveBattle(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        odds: BattleOddsPreview,
        doctrines: List<MilitaryDoctrine>,
        equipment: List<EquipmentItem>,
        randomSeed: Int = Random.nextInt(100)
    ): ExpeditionResult {
        val roll = randomSeed % 100

        val outcome = when {
            roll < odds.greatVictoryPct -> ExpeditionOutcome.GREAT_VICTORY
            roll < odds.greatVictoryPct + odds.victoryPct -> ExpeditionOutcome.VICTORY
            roll < odds.greatVictoryPct + odds.victoryPct + odds.partialPct -> ExpeditionOutcome.PARTIAL_SUCCESS
            roll < odds.greatVictoryPct + odds.victoryPct + odds.partialPct + odds.defeatPct -> ExpeditionOutcome.DEFEAT
            else -> ExpeditionOutcome.DISASTER
        }

        val equipped = equipment.filter { it.equippedCohortId == cohort.id && it.isCrafted }
        val casualtyReduction = equipped.sumOf { it.casualtyReductionPct } +
                if (doctrines.any { it.id == "doc_medicus" && it.isUnlocked }) 15 else 0 +
                if (commander.unlockedTalents.contains(OfficerTalent.IRON_DISCIPLINE)) 20 else 0

        val baseCasualties = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> (cohort.soldiers * Random.nextDouble(0.02, 0.08)).toInt()
            ExpeditionOutcome.VICTORY -> (cohort.soldiers * Random.nextDouble(0.08, 0.16)).toInt()
            ExpeditionOutcome.PARTIAL_SUCCESS -> (cohort.soldiers * Random.nextDouble(0.14, 0.24)).toInt()
            ExpeditionOutcome.DEFEAT -> (cohort.soldiers * Random.nextDouble(0.24, 0.45)).toInt()
            ExpeditionOutcome.DISASTER -> (cohort.soldiers * Random.nextDouble(0.48, 0.78)).toInt()
        }

        val finalCasualties = max(1, (baseCasualties * (1f - (casualtyReduction / 100f))).toInt())
        val veteransSaved = if (commander.awardedCoronas.contains(MilitaryCorona.CORONA_CIVICA)) {
            (finalCasualties * 0.35f).toInt()
        } else {
            (finalCasualties * 0.15f).toInt()
        }

        val woundedTreated = (finalCasualties * 0.25f).toInt()

        val lootMultiplier = if (commander.unlockedTalents.contains(OfficerTalent.LOGISTICS_GENIUS)) 1.2f else 1.0f

        val lootDenarii = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> (expedition.rewardDenarii * 1.5f * lootMultiplier).toInt()
            ExpeditionOutcome.VICTORY -> (expedition.rewardDenarii * lootMultiplier).toInt()
            ExpeditionOutcome.PARTIAL_SUCCESS -> (expedition.rewardDenarii * 0.6f * lootMultiplier).toInt()
            ExpeditionOutcome.DEFEAT -> (expedition.rewardDenarii * 0.2f).toInt()
            ExpeditionOutcome.DISASTER -> 0
        }

        val lootProvisions = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> (expedition.rewardProvisions * 1.4f).toInt()
            ExpeditionOutcome.VICTORY -> expedition.rewardProvisions
            ExpeditionOutcome.PARTIAL_SUCCESS -> (expedition.rewardProvisions * 0.5f).toInt()
            ExpeditionOutcome.DEFEAT -> (expedition.rewardProvisions * 0.25f).toInt()
            ExpeditionOutcome.DISASTER -> 0
        }

        val gloryDelta = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> expedition.rewardGlory + 5
            ExpeditionOutcome.VICTORY -> expedition.rewardGlory
            ExpeditionOutcome.PARTIAL_SUCCESS -> 1
            ExpeditionOutcome.DEFEAT -> -3
            ExpeditionOutcome.DISASTER -> -8
        }

        val xpEarned = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> 60
            ExpeditionOutcome.VICTORY -> 40
            ExpeditionOutcome.PARTIAL_SUCCESS -> 25
            ExpeditionOutcome.DEFEAT -> 15
            ExpeditionOutcome.DISASTER -> 5
        }

        val narrative = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> "Триумфальная победа легиона! Вражеские фаланги рассеяны, захвачены богатые трофеи и знамена неприятеля."
            ExpeditionOutcome.VICTORY -> "Уверенная победа! Римский строй выдержал натиск и сломил сопротивление врага."
            ExpeditionOutcome.PARTIAL_SUCCESS -> "Цели экспедиции достигнуты частично. Противник отступил, но сохранил боеспособность."
            ExpeditionOutcome.DEFEAT -> "Тяжелое отступление. Неприятель оказал ожесточенное сопротивление, легионеры вынуждены отойти в укрепленный лагерь."
            ExpeditionOutcome.DISASTER -> "Военная катастрофа! Легион попал в засаду в ущельях, понесены катастрофические потери."
        }

        return ExpeditionResult(
            expedition = expedition,
            commander = commander,
            cohort = cohort,
            tactics = tactics,
            outcome = outcome,
            casualties = finalCasualties,
            veteransSaved = veteransSaved,
            woundedTreated = woundedTreated,
            lootDenarii = lootDenarii,
            lootProvisions = lootProvisions,
            gloryDelta = gloryDelta,
            xpEarned = xpEarned,
            commanderPromoted = false,
            newTradition = null,
            commanderKilled = outcome == ExpeditionOutcome.DISASTER && Random.nextDouble() < 0.15,
            storyNarrativeRu = narrative
        )
    }
}
