package com.example.domain

import com.example.domain.commanders.CommanderEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class CommanderEngineTest {

    @Test
    fun `awardXp levels up commander and increases max XP`() {
        val recruit = Commander(
            id = "cmd_test",
            name = "Луций Сципион",
            level = 1,
            xp = 0,
            maxXp = 100,
            rankTitle = "Центурион новобранцев",
            trait = CommanderTrait.TACTICIAN
        )

        val leveledUp = CommanderEngine.awardXp(recruit, 150)
        assertEquals(2, leveledUp.level)
        assertEquals(50, leveledUp.xp)
        assertTrue(leveledUp.maxXp > 100)
    }

    @Test
    fun `unlockTalent succeeds when level requirement met`() {
        val experiencedCmd = Commander(
            id = "cmd_exp",
            name = "Гай Корнелий",
            level = 4,
            xp = 0,
            maxXp = 200,
            rankTitle = "Военный трибун",
            trait = CommanderTrait.BRAVE
        )

        val withTalent = CommanderEngine.unlockTalent(experiencedCmd, OfficerTalent.IRON_DISCIPLINE)
        assertTrue(withTalent.unlockedTalents.contains(OfficerTalent.IRON_DISCIPLINE))
    }

    @Test
    fun `awardCorona bestows honor once`() {
        val commander = Commander(
            id = "cmd_hero",
            name = "Марк Валерий",
            level = 5,
            xp = 0,
            maxXp = 300,
            rankTitle = "Легат легиона",
            trait = CommanderTrait.AMBITIOUS
        )

        val awarded = CommanderEngine.awardCorona(commander, MilitaryCorona.CORONA_CIVICA)
        assertEquals(1, awarded.awardedCoronas.size)

        val awardedAgain = CommanderEngine.awardCorona(awarded, MilitaryCorona.CORONA_CIVICA)
        assertEquals(1, awardedAgain.awardedCoronas.size)
    }
}
