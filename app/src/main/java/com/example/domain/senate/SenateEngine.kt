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
