package com.example.domain

import com.example.domain.economy.EconomyEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class EconomyEngineTest {

    private val initialResources = LegionResources(
        denarii = 200,
        provisions = 150,
        glory = 25,
        senateFavor = 60
    )

    private val initialMarket = MarketState(
        grainPriceBuy = 30,
        grainPriceSell = 22
    )

    @Test
    fun `buyGrain deducts denarii and adds provisions`() {
        val result = EconomyEngine.buyGrain(
            marketState = initialMarket,
            resources = initialResources,
            batchCount = 2
        )

        assertTrue(result.isSuccess)
        assertEquals(200 - (30 * 2), result.updatedResources.denarii)
        assertEquals(150 + (40 * 2), result.updatedResources.provisions)
    }

    @Test
    fun `buyGrain fails when denarii insufficient`() {
        val poorResources = initialResources.copy(denarii = 10)
        val result = EconomyEngine.buyGrain(
            marketState = initialMarket,
            resources = poorResources,
            batchCount = 1
        )

        assertFalse(result.isSuccess)
        assertEquals(poorResources.denarii, result.updatedResources.denarii)
    }

    @Test
    fun `takeLoan credits treasury and sets up repayment schedule`() {
        val banking = RomanBankingState()
        val result = EconomyEngine.takeLoan(
            bankingState = banking,
            resources = initialResources,
            loanAmount = 100,
            termSeasons = 4
        )

        assertTrue(result.isSuccess)
        assertTrue(result.updatedState.hasActiveLoan)
        assertEquals(115, result.updatedState.activeLoanDenarii)
        assertEquals(4, result.updatedState.loanSeasonsRemaining)
        assertEquals(initialResources.denarii + 100, result.updatedResources.denarii)
    }

    @Test
    fun `depositDenarii transfers denarii to bank`() {
        val banking = RomanBankingState()
        val result = EconomyEngine.depositDenarii(
            bankingState = banking,
            resources = initialResources,
            amount = 50
        )

        assertTrue(result.isSuccess)
        assertEquals(50, result.updatedState.depositDenarii)
        assertEquals(initialResources.denarii - 50, result.updatedResources.denarii)
    }
}
