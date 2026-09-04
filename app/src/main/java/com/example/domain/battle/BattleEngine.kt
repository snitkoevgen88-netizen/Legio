package com.example.domain.battle

import com.example.domain.commanders.CommanderEngine
import com.example.domain.cohorts.CohortEngine
import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BattleEngine {

    /**
     * Calculates mathematically sound, normalized battle odds [0..100] that always strictly sum to 100%.
     * Deeply accounts for Commander profile, Cohort state, Doctrines with trade-offs, Equipment, and Scout Intel.
     */
    fun calculateBattleOdds(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        campLevel: Int = 1,
        activeBlessing: ActiveBlessing? = null,
        doctrines: List<MilitaryDoctrine> = emptyList(),
        equipment: List<EquipmentItem> = emptyList()
    ): BattleOddsPreview {
        val unlockedDocs = doctrines.filter { it.isUnlocked }.map { it.id }.toSet()

        // 1. Equipment bonuses by role
        val equippedForCohort = equipment.filter { it.equippedCohortId == cohort.id && it.isCrafted }
        val weaponAttackBonus = equippedForCohort.filter { it.type == EquipmentType.WEAPON }.sumOf { it.totalAttackBonus }
        val shieldDefenseBonus = equippedForCohort.filter { it.type == EquipmentType.SHIELD }.sumOf { it.totalDefenseBonus }
        val armorDefenseBonus = equippedForCohort.filter { it.type == EquipmentType.ARMOR }.sumOf { it.totalDefenseBonus }
        val standardMoraleBonus = equippedForCohort.filter { it.type == EquipmentType.STANDARD }.sumOf { it.moraleBonus }
        val allOtherAttack = equippedForCohort.filter { it.type != EquipmentType.WEAPON }.sumOf { it.totalAttackBonus }
        val allOtherDefense = equippedForCohort.filter { it.type != EquipmentType.SHIELD && it.type != EquipmentType.ARMOR }.sumOf { it.totalDefenseBonus }

        val totalGearAttack = weaponAttackBonus + allOtherAttack
        val totalGearDefense = shieldDefenseBonus + armorDefenseBonus + allOtherDefense
        val totalGearMorale = standardMoraleBonus + equippedForCohort.sumOf { it.moraleBonus }

        // 2. Commander specialized combat profile
        val cmdProfile = CommanderEngine.evaluateCombatProfile(commander, tactics, expedition.scoutIntel)

        // 3. Doctrine Trade-offs calculation
        var doctrineAtk = 0
        var doctrineDef = 0
        var doctrineMorale = 0
        var doctrineGreatVicBonus = 0
        var doctrineDisasterChange = 0
        var doctrineDefeatChange = 0

        if (unlockedDocs.contains("doc_disciplina") || unlockedDocs.contains("doc_disciplina_ferrea")) {
            doctrineDef += 4
            doctrineDefeatChange -= 6
            doctrineDisasterChange -= 4
        }
        if (unlockedDocs.contains("doc_pila_barrage") || unlockedDocs.contains("doc_pilum")) {
            doctrineAtk += 4
            doctrineGreatVicBonus += 12
        }
        if (unlockedDocs.contains("doc_gladius_mastery")) {
            doctrineAtk += 5
            doctrineGreatVicBonus += 10
        }
        if (unlockedDocs.contains("doc_triplex_acies")) {
            doctrineAtk += 3
            doctrineDef += 3
            doctrineMorale += 5
        }
        if (unlockedDocs.contains("doc_testudo")) {
            doctrineDef += 6
            doctrineDisasterChange -= 15
            doctrineDefeatChange -= 8
        }
        if (unlockedDocs.contains("doc_auxilia") || unlockedDocs.contains("doc_equites")) {
            if (tactics == Tactics.FLANK_AMBUSH) {
                doctrineAtk += 6
                doctrineGreatVicBonus += 15
            }
        }
        if (unlockedDocs.contains("doc_siege_art") || unlockedDocs.contains("doc_art_tormentorum")) {
            if (expedition.difficulty >= 3) {
                doctrineAtk += 5
                doctrineGreatVicBonus += 10
            }
        }

        // 4. Cohort attributes
        val cohortVeteranBonus = cohort.veteransCount * 2
        val cohortTraditionBonus = if (cohort.traditions.contains("Железный строй")) 4 else 0
        val effectiveCohortAtk = cohort.attackPower + (cohort.level * 2) + cohortTraditionBonus
        val effectiveCohortDef = cohort.defensePower + (cohort.level * 2) + cohortTraditionBonus

        // 5. Total Combined Legion Power
        val totalPlayerAttack = max(1, effectiveCohortAtk + cmdProfile.effectiveAttack + totalGearAttack + doctrineAtk + tactics.attackMod)
        val totalPlayerDefense = max(1, effectiveCohortDef + cmdProfile.effectiveDefense + totalGearDefense + doctrineDef + tactics.defenseMod)
        val totalPlayerMorale = (cohort.morale + totalGearMorale + doctrineMorale + cmdProfile.moraleAuraBonus + (campLevel * 2)).coerceIn(10, 100)
        val effectiveDiscipline = (cohort.discipline + cmdProfile.disciplineAuraBonus).coerceIn(10, 100)

        // 6. Enemy Difficulty Scaling
        val enemyDifficultyRating = max(10, expedition.difficulty * 16 + 8)
        val powerRatio = (totalPlayerAttack + totalPlayerDefense).toFloat() / (enemyDifficultyRating * 2).toFloat()

        // 7. Base Weights Calculation
        var greatVictoryWeight = max(2, (powerRatio * 25 + totalPlayerMorale * 0.12f + cmdProfile.greatVictoryBonusPct + tactics.greatVictoryBonusPct + doctrineGreatVicBonus + (cohortVeteranBonus / 4)).toInt())
        var victoryWeight = max(8, (powerRatio * 45 + effectiveDiscipline * 0.35f + (cohort.level * 3)).toInt())
        var partialWeight = max(6, (24 + totalPlayerDefense * 0.25f).toInt())
        var defeatWeight = max(2, (36 - powerRatio * 18 + expedition.difficulty * 4 + doctrineDefeatChange).toInt())
        var disasterWeight = max(0, (14 - powerRatio * 12 - totalPlayerMorale * 0.08f + cmdProfile.disasterRiskChangePct + tactics.disasterRiskPct + doctrineDisasterChange).toInt())

        // 8. Scout Intel Synergy / Counter
        if (expedition.scoutIntel.recommendedTactic == tactics) {
            greatVictoryWeight = (greatVictoryWeight * 1.25f).toInt()
            victoryWeight = (victoryWeight * 1.15f).toInt()
            disasterWeight = max(0, disasterWeight - 8)
        }

        // 9. Divine Blessing Modifiers
        when (activeBlessing?.god) {
            GodType.MARS -> {
                greatVictoryWeight = (greatVictoryWeight * 1.35f).toInt()
                victoryWeight = (victoryWeight * 1.20f).toInt()
            }
            GodType.JUPITER -> {
                disasterWeight = 0
                victoryWeight = (victoryWeight * 1.15f).toInt()
            }
            GodType.FORTUNA -> {
                disasterWeight = max(0, disasterWeight - 15)
                defeatWeight = max(1, defeatWeight - 8)
                partialWeight = (partialWeight * 1.20f).toInt()
            }
            GodType.MINERVA -> {
                defeatWeight = max(1, defeatWeight - 10)
                partialWeight = (partialWeight * 1.25f).toInt()
                victoryWeight = (victoryWeight * 1.10f).toInt()
            }
            else -> {}
        }

        // Testudo & Obsidionalis absolute disaster immunities
        if (tactics == Tactics.TESTUDO && (unlockedDocs.contains("doc_testudo") || commander.awardedCoronas.contains(MilitaryCorona.CORONA_OBSIDIONALIS))) {
            disasterWeight = 0
        }

        // 10. Strict Positive Clamping
        greatVictoryWeight = max(1, greatVictoryWeight)
        victoryWeight = max(1, victoryWeight)
        partialWeight = max(1, partialWeight)
        defeatWeight = max(1, defeatWeight)
        disasterWeight = max(0, disasterWeight)

        // 11. Normalize to Exactly 100%
        val totalWeight = (greatVictoryWeight + victoryWeight + partialWeight + defeatWeight + disasterWeight).toFloat()
        var gvPct = max(0, ((greatVictoryWeight / totalWeight) * 100).toInt())
        var vicPct = max(0, ((victoryWeight / totalWeight) * 100).toInt())
        var parPct = max(0, ((partialWeight / totalWeight) * 100).toInt())
        var defPct = max(0, ((defeatWeight / totalWeight) * 100).toInt())
        var disPct = max(0, ((disasterWeight / totalWeight) * 100).toInt())

        val sum = gvPct + vicPct + parPct + defPct + disPct
        val remainder = 100 - sum
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
            gvPct + vicPct >= 75 -> "⚡ Подавляющее превосходство легиона. Исход сулит триумф Рима."
            disPct >= 18 -> "⚠️ Высокая угроза засады и разгрома! Рекомендуется перейти в оборону («Черепаха»)."
            tactics == expedition.scoutIntel.recommendedTactic -> "🎯 Тактика идеально контрит построение противника."
            else -> "⚖️ Исход кампании зависит от выдержки солдат и тактических приказов командующего."
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
     * Resolves full battle simulation with complete military cascade consequences.
     */
    fun resolveBattle(
        expedition: Expedition,
        commander: Commander,
        cohort: Cohort,
        tactics: Tactics,
        odds: BattleOddsPreview,
        campLevel: Int = 1,
        fabricaLevel: Int = 1,
        valetudinariumLevel: Int = 1,
        doctrines: List<MilitaryDoctrine> = emptyList(),
        equipment: List<EquipmentItem> = emptyList(),
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

        val unlockedDocs = doctrines.filter { it.isUnlocked }.map { it.id }.toSet()
        val cmdProfile = CommanderEngine.evaluateCombatProfile(commander, tactics, expedition.scoutIntel)

        // Equipment casualty mitigation
        val equipped = equipment.filter { it.equippedCohortId == cohort.id && it.isCrafted }
        val gearCasReduction = equipped.sumOf { it.casualtyReductionPct }

        // Combined casualty mitigation
        val docCasReduction = if (unlockedDocs.contains("doc_disciplina") || unlockedDocs.contains("doc_disciplina_ferrea")) 20 else 0
        val totalCasReductionPct = (gearCasReduction + docCasReduction + cmdProfile.casualtyMitigationPct + (fabricaLevel * 3)).coerceIn(0, 75)

        // Raw casualty base
        val baseCasualties = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> (cohort.soldiers * Random.nextDouble(0.01, 0.06)).toInt()
            ExpeditionOutcome.VICTORY -> (cohort.soldiers * Random.nextDouble(0.06, 0.14)).toInt()
            ExpeditionOutcome.PARTIAL_SUCCESS -> (cohort.soldiers * Random.nextDouble(0.12, 0.22)).toInt()
            ExpeditionOutcome.DEFEAT -> (cohort.soldiers * Random.nextDouble(0.22, 0.40)).toInt()
            ExpeditionOutcome.DISASTER -> (cohort.soldiers * Random.nextDouble(0.45, 0.70)).toInt()
        }

        // Apply Cohort cascade via CohortEngine
        val isSuccess = outcome.isSuccess
        val isGreatVic = outcome == ExpeditionOutcome.GREAT_VICTORY

        val cohortCascade = CohortEngine.applyBattleCasualties(
            cohort = cohort,
            rawCasualties = baseCasualties,
            isSuccess = isSuccess,
            isGreatVictory = isGreatVic,
            expeditionRegion = expedition.regionRu,
            veteranPreservationPct = cmdProfile.veteranPreservationPct,
            casualtyMitigationPct = totalCasReductionPct
        )

        // Valetudinarium hospital treatment
        val docMedicRatio = if (unlockedDocs.contains("doc_medici") || unlockedDocs.contains("doc_medicus")) 0.30f else 0f
        val healRatio = (valetudinariumLevel * 0.15f) + docMedicRatio
        val woundedTreated = (cohortCascade.actualCasualties * healRatio).toInt()

        // Loot calculation
        var lootMultiplier = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> 1.5f
            ExpeditionOutcome.VICTORY -> 1.0f
            ExpeditionOutcome.PARTIAL_SUCCESS -> 0.55f
            ExpeditionOutcome.DEFEAT -> 0.15f
            ExpeditionOutcome.DISASTER -> 0.0f
        }
        lootMultiplier += cmdProfile.lootMultiplierBonus
        if (unlockedDocs.contains("doc_auxilia") && isSuccess) lootMultiplier += 0.20f
        if (unlockedDocs.contains("doc_siege_art") && expedition.difficulty >= 3 && isSuccess) lootMultiplier += 0.35f

        val lootDenarii = (expedition.rewardDenarii * lootMultiplier).toInt()
        val lootProvisions = (expedition.rewardProvisions * lootMultiplier).toInt()

        val gloryDelta = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> expedition.rewardGlory + 5 + (if (commander.trait == CommanderTrait.AMBITIOUS) 2 else 0)
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

        val commanderKilled = outcome == ExpeditionOutcome.DISASTER &&
                !commander.awardedCoronas.contains(MilitaryCorona.CORONA_OBSIDIONALIS) &&
                Random.nextDouble() < 0.12

        val narrative = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> "Триумфальная победа легиона! Вражеские ряды сокрушены молниеносным натиском, знамена Рима реют над полем боя."
            ExpeditionOutcome.VICTORY -> "Уверенная победа! Римский строй выдержал натиск и сломил сопротивление врага."
            ExpeditionOutcome.PARTIAL_SUCCESS -> "Цели экспедиции достигнуты частично. Противник отступил в глубь земель, сохранив силы."
            ExpeditionOutcome.DEFEAT -> "Тяжелое отступление. Неприятель оказал яростное сопротивление, манипулы отошли в укрепленный бивуак."
            ExpeditionOutcome.DISASTER -> "Военная катастрофа! Легион угодил в засаду в ущельях, понесены тяжелые потери."
        }

        return ExpeditionResult(
            expedition = expedition,
            commander = commander,
            cohort = cohortCascade.updatedCohort,
            tactics = tactics,
            outcome = outcome,
            casualties = cohortCascade.actualCasualties,
            veteransSaved = cohortCascade.veteransSaved,
            woundedTreated = woundedTreated,
            lootDenarii = lootDenarii,
            lootProvisions = lootProvisions,
            gloryDelta = gloryDelta,
            xpEarned = xpEarned,
            commanderPromoted = false,
            newTradition = cohortCascade.newTradition,
            commanderKilled = commanderKilled,
            storyNarrativeRu = narrative
        )
    }
}
