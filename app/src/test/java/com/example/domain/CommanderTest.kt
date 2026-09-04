package com.example.domain

import com.example.domain.commanders.CommanderEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class CommanderTest {

    private val baseCommander = Commander(
        id = "cmd_test",
        name = "Гай Корнелий Сципион",
        level = 1,
        xp = 0,
        maxXp = 100,
        rankTitle = "Центурион",
        trait = CommanderTrait.TACTICIAN
    )

    @Test
    fun `awardXp levels up commander and promotes rank titles accurately`() {
        // Gaining 120 XP at level 1 (maxXp = 100) -> level 2, xp 20
        val (lvl2Cmd, promoted1) = CommanderEngine.awardXp(baseCommander, 120)
        assertEquals(2, lvl2Cmd.level)
        assertEquals(20, lvl2Cmd.xp)
        assertFalse(promoted1)

        // Leveling to Tribunus (level 4)
        val (tribunusCmd, wasPromotedToTribun) = CommanderEngine.awardXp(baseCommander, 450)
        assertTrue(tribunusCmd.level >= 4)
        assertEquals("Военный трибун", tribunusCmd.rankTitle)
        assertTrue(wasPromotedToTribun)

        // Leveling to Legatus (level 7)
        val (legatusCmd, wasPromotedToLegatus) = CommanderEngine.awardXp(baseCommander, 1500)
        assertTrue(legatusCmd.level >= 7)
        assertEquals("Легат легиона", legatusCmd.rankTitle)
        assertTrue(wasPromotedToLegatus)
    }

    @Test
    fun `unlockTalent respects level requirements and prevents duplicates`() {
        // INVICTA_CHAMPION requires level 4
        val lvl1Attempt = CommanderEngine.unlockTalent(baseCommander, OfficerTalent.INVICTA_CHAMPION)
        assertFalse(lvl1Attempt.unlockedTalents.contains(OfficerTalent.INVICTA_CHAMPION))

        // Give level 3 to commander and unlock IRON_DISCIPLINE (requires level 2)
        val (lvl3Cmd, _) = CommanderEngine.awardXp(baseCommander, 250)
        val unlocked = CommanderEngine.unlockTalent(lvl3Cmd, OfficerTalent.IRON_DISCIPLINE)
        assertTrue(unlocked.unlockedTalents.contains(OfficerTalent.IRON_DISCIPLINE))

        // Prevent duplicate addition
        val duplicateCheck = CommanderEngine.unlockTalent(unlocked, OfficerTalent.IRON_DISCIPLINE)
        assertEquals(1, duplicateCheck.unlockedTalents.size)
    }

    @Test
    fun `awardCorona bestows honors and confers specialized combat auras`() {
        val withCorona = CommanderEngine.awardCorona(baseCommander, MilitaryCorona.CORONA_OBSIDIONALIS)
        assertTrue(withCorona.awardedCoronas.contains(MilitaryCorona.CORONA_OBSIDIONALIS))

        val profile = CommanderEngine.evaluateCombatProfile(withCorona, Tactics.TESTUDO)
        assertTrue(profile.disasterRiskChangePct <= -25)
        assertTrue(profile.casualtyMitigationPct >= 20)
    }

    @Test
    fun `commander traits grant distinct tactical advantages`() {
        val braveCmd = baseCommander.copy(trait = CommanderTrait.BRAVE)
        val cautiousCmd = baseCommander.copy(trait = CommanderTrait.CAUTIOUS)
        val greedyCmd = baseCommander.copy(trait = CommanderTrait.GREEDY)

        val braveProfile = CommanderEngine.evaluateCombatProfile(braveCmd, Tactics.AGGRESSIVE)
        val cautiousProfile = CommanderEngine.evaluateCombatProfile(cautiousCmd, Tactics.CAUTIOUS)
        val greedyProfile = CommanderEngine.evaluateCombatProfile(greedyCmd, Tactics.BALANCED)

        assertTrue("Brave commander with aggressive tactics gets attack boost", braveProfile.effectiveAttack > cautiousProfile.effectiveAttack)
        assertTrue("Cautious commander gets casualty mitigation", cautiousProfile.casualtyMitigationPct > 0)
        assertTrue("Greedy commander yields extra loot multiplier", greedyProfile.lootMultiplierBonus >= 0.40f)
    }

    @Test
    fun `generateRecruitCandidate produces valid Roman officer`() {
        val recruit = CommanderEngine.generateRecruitCandidate()
        assertNotNull(recruit.id)
        assertTrue(recruit.name.isNotBlank())
        assertEquals(1, recruit.level)
        assertTrue(recruit.isAlive)
    }

    @Test
    fun `assignCommanderToCohort manages cohort leader bindings`() {
        val cohorts = listOf(
            Cohort(id = "coh_1", name = "I Cohort"),
            Cohort(id = "coh_2", name = "II Cohort")
        )

        val assigned = CommanderEngine.assignCommanderToCohort(cohorts, "cmd_1", "coh_1")
        assertEquals("cmd_1", assigned.find { it.id == "coh_1" }?.assignedCommanderId)
        assertNull(assigned.find { it.id == "coh_2" }?.assignedCommanderId)

        // Reassigning to another cohort clears previous assignment
        val reassigned = CommanderEngine.assignCommanderToCohort(assigned, "cmd_1", "coh_2")
        assertNull(reassigned.find { it.id == "coh_1" }?.assignedCommanderId)
        assertEquals("cmd_1", reassigned.find { it.id == "coh_2" }?.assignedCommanderId)
    }
}
