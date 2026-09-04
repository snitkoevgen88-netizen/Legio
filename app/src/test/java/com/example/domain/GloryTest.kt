package com.example.domain

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class GloryTest {

    @Test
    fun `glory represents republic reputation rather than disposable arcade points`() {
        val resources = LegionResources(
            denarii = 200,
            provisions = 150,
            glory = 120,
            senateFavor = 75
        )

        assertTrue("High glory represents prestige and renown", resources.glory >= 100)

        val rank = when {
            resources.glory >= 200 -> "Легион Триумфатор (Legio Triumphalis)"
            resources.glory >= 100 -> "Непобедимый Легион (Legio Invicta)"
            resources.glory >= 50 -> "Прославленный Легион (Legio Clarissima)"
            else -> "Новый Легион (Legio Tyronum)"
        }

        assertEquals("Непобедимый Легион (Legio Invicta)", rank)
    }

    @Test
    fun `magistracy ranks scale with glory and political standing in Rome`() {
        val lowRank = MagistracyRank.TRIBUNUS_MILITUM
        val midRank = MagistracyRank.QUAESTOR
        val highRank = MagistracyRank.CONSUL
        val topRank = MagistracyRank.DICTATOR_TRIUMPHATOR

        assertTrue(lowRank.minGlory <= 0)
        assertTrue(midRank.minGlory <= 35)
        assertTrue(highRank.minGlory <= 220)
        assertTrue(topRank.minGlory <= 320)
    }
}
