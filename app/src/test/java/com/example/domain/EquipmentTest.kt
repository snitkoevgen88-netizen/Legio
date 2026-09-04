package com.example.domain

import com.example.domain.equipment.EquipmentEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class EquipmentTest {

    private val baseItem = EquipmentItem(
        id = "eq_gladius_hispaniensis",
        nameRu = "Гладиус Испанский",
        latinName = "Gladius Hispaniensis",
        type = EquipmentType.WEAPON,
        material = EquipmentMaterial.IRON,
        attackBonus = 6,
        defenseBonus = 1,
        moraleBonus = 2,
        costDenarii = 40,
        descRu = "Острое колющее оружие",
        isCrafted = false
    )

    private val sampleCohort = Cohort(
        id = "coh_1",
        name = "I Когорта",
        attackPower = 20,
        defensePower = 15,
        veteransCount = 25
    )

    @Test
    fun `craftItem deducts resources and marks item as crafted`() {
        val resources = LegionResources(denarii = 100, provisions = 100)
        val result = EquipmentEngine.craftItem(listOf(baseItem), baseItem.id, resources)

        assertTrue(result.isSuccess)
        assertTrue(result.updatedEquipment.first().isCrafted)
        assertEquals(60, result.updatedResources.denarii)
    }

    @Test
    fun `temperItem upgrades crafted gear up to level 3`() {
        val crafted = baseItem.copy(isCrafted = true)
        val resources = LegionResources(denarii = 150, provisions = 100)

        val temp1 = EquipmentEngine.temperItem(listOf(crafted), crafted.id, resources)
        assertTrue(temp1.isSuccess)
        assertEquals(1, temp1.updatedEquipment.first().temperLevel)
        assertTrue(temp1.updatedEquipment.first().totalAttackBonus > crafted.attackBonus)

        val temp2 = EquipmentEngine.temperItem(temp1.updatedEquipment, crafted.id, temp1.updatedResources)
        assertEquals(2, temp2.updatedEquipment.first().temperLevel)

        val temp3 = EquipmentEngine.temperItem(temp2.updatedEquipment, crafted.id, temp2.updatedResources)
        assertEquals(3, temp3.updatedEquipment.first().temperLevel)

        // Attempting temper 4 should fail gracefully
        val temp4 = EquipmentEngine.temperItem(temp3.updatedEquipment, crafted.id, temp3.updatedResources)
        assertFalse(temp4.isSuccess)
    }

    @Test
    fun `salvageItem dismantles crafted item and refunds partial denarii`() {
        val crafted = baseItem.copy(isCrafted = true)
        val resources = LegionResources(denarii = 20, provisions = 100)

        val result = EquipmentEngine.salvageItem(listOf(crafted), crafted.id, resources)
        assertTrue(result.isSuccess)
        assertFalse(result.updatedEquipment.first().isCrafted)
        // 40 cost * 0.4 = 16 refund
        assertEquals(36, result.updatedResources.denarii)
    }

    @Test
    fun `autoEquipAll distributes weapons to attack leaders and armor to low defense cohorts`() {
        val items = listOf(
            baseItem.copy(id = "w1", isCrafted = true, type = EquipmentType.WEAPON),
            baseItem.copy(id = "a1", isCrafted = true, type = EquipmentType.ARMOR),
            baseItem.copy(id = "s1", isCrafted = true, type = EquipmentType.STANDARD)
        )
        val cohorts = listOf(
            Cohort(id = "coh_attack", name = "Attacker", attackPower = 30, defensePower = 20, veteransCount = 10),
            Cohort(id = "coh_defense_low", name = "Defender Need", attackPower = 10, defensePower = 8, veteransCount = 5),
            Cohort(id = "coh_veterans", name = "Veterans", attackPower = 15, defensePower = 18, veteransCount = 40)
        )

        val equipped = EquipmentEngine.autoEquipAll(items, cohorts)
        assertEquals("coh_attack", equipped.find { it.id == "w1" }?.equippedCohortId)
        assertEquals("coh_defense_low", equipped.find { it.id == "a1" }?.equippedCohortId)
        assertEquals("coh_veterans", equipped.find { it.id == "s1" }?.equippedCohortId)
    }
}
