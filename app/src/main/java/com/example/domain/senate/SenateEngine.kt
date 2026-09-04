package com.example.domain.senate

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object SenateEngine {

    enum class CampaignActionType(val costDenarii: Int, val titleRu: String) {
        SPEECH(0, "Речь на Римском Форуме"),
        CIRCUS_GAMES(60, "Гладиаторские игры для плебеев"),
        BRIBE_VOTERS(45, "Подкуп центуриатных комиций")
    }

    /**
     * Evaluates and updates the progress of all active Senate Quests.
     */
    fun evaluateQuests(
        quests: List<SenateQuest>,
        totalVictories: Int,
        campLevel: Int,
        doctrinesUnlocked: Int,
        equipmentCrafted: Int,
        currentWinStreak: Int,
        denariiInTreasury: Int
    ): List<SenateQuest> {
        return quests.map { quest ->
            if (quest.isClaimed) return@map quest

            val updatedProgress = when (quest.id) {
                "q_first_blood", "q_samnite_war", "q_pyrrhus_triumph" -> totalVictories
                "q_fortify_castrum", "q_grand_citadel" -> campLevel
                "q_forge_weapons", "q_legendary_arsenal" -> equipmentCrafted
                "q_reform_doctrines" -> doctrinesUnlocked
                "q_unbroken_streak" -> currentWinStreak
                "q_treasury_wealth" -> denariiInTreasury
                else -> quest.currentProgress
            }

            val isNowCompleted = updatedProgress >= quest.targetCount
            quest.copy(
                currentProgress = min(quest.targetCount, updatedProgress),
                isCompleted = isNowCompleted
            )
        }
    }

    /**
     * Claims the reward of a single completed Senate Quest.
     */
    fun claimQuest(
        quest: SenateQuest,
        resources: LegionResources
    ): Pair<SenateQuest, LegionResources>? {
        if (!quest.isCompleted || quest.isClaimed) return null

        val updatedQuest = quest.copy(isClaimed = true)
        val updatedResources = resources.copy(
            denarii = resources.denarii + quest.rewardDenarii,
            senateFavor = min(100, resources.senateFavor + quest.rewardSenateFavor),
            glory = resources.glory + quest.rewardGlory
        )

        return Pair(updatedQuest, updatedResources)
    }

    /**
     * Handles player decision on a Senate Quest (ACCEPT, DECLINE, NEGOTIATE, DELAY).
     * «Существующие задания Сената должны иметь выбор с реальными последствиями.»
     */
    fun handleQuestDecision(
        quest: SenateQuest,
        decision: SenateQuestDecision,
        resources: LegionResources
    ): Pair<SenateQuest, LegionResources> {
        return when (decision) {
            SenateQuestDecision.ACCEPT -> {
                val updated = quest.copy(status = QuestStatus.ACTIVE)
                Pair(updated, resources)
            }
            SenateQuestDecision.DECLINE -> {
                val updated = quest.copy(status = QuestStatus.DECLINED)
                val newResources = resources.copy(
                    senateFavor = max(0, resources.senateFavor - 4)
                )
                Pair(updated, newResources)
            }
            SenateQuestDecision.NEGOTIATE -> {
                val newTarget = max(quest.targetCount + 1, (quest.targetCount * 1.2f).toInt())
                val newRewardDenarii = (quest.rewardDenarii * 1.4f).toInt()
                val newRewardGlory = quest.rewardGlory + 2
                val updated = quest.copy(
                    status = QuestStatus.NEGOTIATED,
                    targetCount = newTarget,
                    rewardDenarii = newRewardDenarii,
                    rewardGlory = newRewardGlory,
                    isNegotiated = true
                )
                // Advance stipend from Senate treasury
                val newResources = resources.copy(
                    denarii = resources.denarii + 35,
                    provisions = resources.provisions + 25
                )
                Pair(updated, newResources)
            }
            SenateQuestDecision.DELAY -> {
                val currentDeadline = quest.deadlineSeasonsRemaining ?: 2
                val updated = quest.copy(
                    status = QuestStatus.DELAYED,
                    deadlineSeasonsRemaining = currentDeadline + 2
                )
                val newResources = resources.copy(
                    senateFavor = max(0, resources.senateFavor - 2)
                )
                Pair(updated, newResources)
            }
        }
    }

    /**
     * Evaluates Senate reaction and favor modification after military actions.
     * «High Glory -> positive Senate reaction; Heavy casualties -> criticism; Too many victories -> rival attention.»
     */
    data class SenateReactionResult(
        val favorDelta: Int,
        val updatedFavor: Int,
        val senateCommentRu: String,
        val factionReactionRu: String
    )

    fun evaluateSenateReaction(
        outcome: ExpeditionOutcome,
        casualtiesRatio: Float,
        isAquilaLost: Boolean,
        currentFavor: Int,
        totalGlory: Int
    ): SenateReactionResult {
        var delta = when (outcome) {
            ExpeditionOutcome.GREAT_VICTORY -> 8
            ExpeditionOutcome.VICTORY -> 4
            ExpeditionOutcome.PARTIAL_SUCCESS -> 1
            ExpeditionOutcome.DEFEAT -> -6
            ExpeditionOutcome.DISASTER -> -15
        }

        // Heavy casualties penalty
        if (casualtiesRatio > 0.35f) {
            delta -= 5
        }

        // Catastrophic Aquila loss
        if (isAquilaLost) {
            delta -= 25
        }

        val newFavor = (currentFavor + delta).coerceIn(0, 100)

        val comment = when {
            isAquilaLost -> "🏛️ СЕНАТ В ЯРОСТИ: «Потеря священного Орла — несмываемый позор перед Богами и Римом! Сенат требует ответа!»"
            outcome == ExpeditionOutcome.GREAT_VICTORY -> "🏛️ СЕНАТ ВОСХИЩЕН: Патриции аплодируют триумфу легиона в Курии Гостилия!"
            outcome == ExpeditionOutcome.VICTORY -> "🏛️ СЕНАТ ОДОБРЯЕТ: «Легион надежно защищает границы Республики от варваров.»"
            outcome == ExpeditionOutcome.DEFEAT && casualtiesRatio > 0.35f -> "🏛️ КРИТИКА В СЕНАТЕ: «Слишком много сыновей Рима пало на поле брани. Командованию объявлен выговор!»"
            outcome == ExpeditionOutcome.DISASTER -> "🏛️ СЕНАТСКОЕ РАССЛЕДОВАНИЕ: Сенаторы потрясены разгромом и требуют сократить финансирование."
            else -> "🏛️ Сенат принял донесение квестуры к сведению."
        }

        val factionReaction = when {
            totalGlory >= 150 -> "Оптиматы насторожены вашим величием; Популяры требуют консульского триумфа."
            newFavor >= 75 -> "Патрицианские роды выражают доверие вашему командованию."
            newFavor <= 30 -> "Трибуны и цензоры грозят судебным процессом на Форуме."
            else -> "Фракции Сената соблюдают шаткое согласие."
        }

        return SenateReactionResult(
            favorDelta = delta,
            updatedFavor = newFavor,
            senateCommentRu = comment,
            factionReactionRu = factionReaction
        )
    }

    /**
     * Resolves a diplomatic petition submitted to the Senate.
     */
    fun resolveSenatePetition(
        petition: SenatePetition,
        resources: LegionResources,
        cohorts: List<Cohort>,
        campLevel: Int = 1
    ): Pair<LegionResources, List<Cohort>>? {
        if (resources.senateFavor < petition.favorCost || resources.denarii < petition.denariiCost) {
            return null
        }

        var newDenarii = resources.denarii - petition.denariiCost
        var newProvisions = resources.provisions
        var newGlory = resources.glory
        val newSenateFavor = max(0, resources.senateFavor - petition.favorCost)

        var updatedCohorts = cohorts

        when (petition.id) {
            "pet_emergency_grain" -> {
                newProvisions += 90 + (campLevel * 10)
            }
            "pet_auxiliary_levy" -> {
                updatedCohorts = cohorts.map { cohort ->
                    cohort.copy(
                        soldiers = min(cohort.maxSoldiers, cohort.soldiers + 15),
                        morale = min(100, cohort.morale + 5)
                    )
                }
            }
            "pet_treasury_grant" -> {
                newDenarii += 110 + (campLevel * 15)
            }
            "pet_triumph_acclamation" -> {
                newGlory += 15
                updatedCohorts = cohorts.map { it.copy(morale = 100) }
            }
        }

        val updatedResources = resources.copy(
            denarii = newDenarii,
            provisions = newProvisions,
            glory = newGlory,
            senateFavor = newSenateFavor
        )

        return Pair(updatedResources, updatedCohorts)
    }

    /**
     * Claims all completed, unclaimed Senate Quests at once.
     */
    fun claimAllQuests(
        quests: List<SenateQuest>,
        resources: LegionResources
    ): Pair<List<SenateQuest>, LegionResources> {
        var curResources = resources
        val updatedQuests = quests.map { quest ->
            if (quest.isCompleted && !quest.isClaimed) {
                curResources = curResources.copy(
                    denarii = curResources.denarii + quest.rewardDenarii,
                    senateFavor = min(100, curResources.senateFavor + quest.rewardSenateFavor),
                    glory = curResources.glory + quest.rewardGlory
                )
                quest.copy(isClaimed = true)
            } else {
                quest
            }
        }
        return Pair(updatedQuests, curResources)
    }

    /**
     * Simulates and ranks competing rival legions across Glory, Victories, Senate standing, and Commander renown.
     */
    fun updateCompetingLegions(
        currentLegions: List<CompetingLegion>,
        playerGlory: Int,
        playerVictories: Int,
        playerSenateFavor: Int,
        seasonNumber: Int
    ): List<CompetingLegion> {
        return currentLegions.map { legion ->
            val aiWins = Random.nextInt(100) < 55
            val vicDelta = if (aiWins) 1 else 0
            val defDelta = if (!aiWins) 1 else 0
            val gloryDelta = if (aiWins) Random.nextInt(4, 10) else Random.nextInt(-3, 2)
            val favorDelta = if (aiWins) Random.nextInt(2, 6) else Random.nextInt(-4, 1)

            val newGlory = max(20, legion.glory + gloryDelta)
            val newVictories = legion.victories + vicDelta
            val newDefeats = legion.defeats + defDelta
            val newSenateRep = (legion.senateReputation + favorDelta).coerceIn(20, 100)
            val newRating = (newGlory * 1.5f + newVictories * 12 + newSenateRep * 0.8f).toInt()

            val activities = listOf(
                "Ведет осаду этрусских крепостей на севере",
                "Патрулирует Аппиеву дорогу и границы Самния",
                "Чествует триумф своего легата на Форуме",
                "Отражает набеги галльских племен в Цизальпине",
                "Разбила лагерь на границах Кампании"
            )

            legion.copy(
                ratingScore = newRating,
                glory = newGlory,
                victories = newVictories,
                defeats = newDefeats,
                senateReputation = newSenateRep,
                commanderReputation = min(100, legion.commanderReputation + (if (aiWins) 4 else 0)),
                currentActivityRu = activities[seasonNumber % activities.size]
            )
        }.sortedByDescending { it.ratingScore }
    }

    /**
     * Executes a political election campaign action.
     */
    fun performCampaignAction(
        campaign: RomanElectionCampaign,
        actionType: CampaignActionType,
        resources: LegionResources
    ): Pair<RomanElectionCampaign, LegionResources>? {
        if (resources.denarii < actionType.costDenarii) return null

        val newResources = resources.copy(denarii = resources.denarii - actionType.costDenarii)

        val updatedCampaign = when (actionType) {
            CampaignActionType.SPEECH -> {
                campaign.copy(
                    speechesDelivered = campaign.speechesDelivered + 1,
                    plebeianSupportPct = min(100, campaign.plebeianSupportPct + 8),
                    patricianSupportPct = min(100, campaign.patricianSupportPct + 5)
                )
            }
            CampaignActionType.CIRCUS_GAMES -> {
                campaign.copy(
                    gamesOrganizedCount = campaign.gamesOrganizedCount + 1,
                    plebeianSupportPct = min(100, campaign.plebeianSupportPct + 18),
                    patricianSupportPct = min(100, campaign.patricianSupportPct + 4),
                    briberyBudgetSpent = campaign.briberyBudgetSpent + actionType.costDenarii
                )
            }
            CampaignActionType.BRIBE_VOTERS -> {
                campaign.copy(
                    patricianSupportPct = min(100, campaign.patricianSupportPct + 22),
                    plebeianSupportPct = min(100, campaign.plebeianSupportPct + 5),
                    briberyBudgetSpent = campaign.briberyBudgetSpent + actionType.costDenarii
                )
            }
        }

        return Pair(updatedCampaign, newResources)
    }

    /**
     * Attempts to elect the candidate to the target magistracy rank in the Centuriate Assembly.
     */
    fun runCenturiateElection(
        campaign: RomanElectionCampaign,
        currentRank: MagistracyRank,
        senateFavor: Int
    ): Pair<Boolean, MagistracyRank> {
        val totalScore = campaign.totalElectionScore + (senateFavor * 0.25f).toInt()
        val successThreshold = 75

        return if (totalScore >= successThreshold) {
            Pair(true, campaign.targetRank)
        } else {
            Pair(false, currentRank)
        }
    }
}
