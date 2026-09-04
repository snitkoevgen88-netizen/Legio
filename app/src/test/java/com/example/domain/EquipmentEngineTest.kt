package com.example.domain

import com.example.domain.equipment.EquipmentEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class EquipmentEngineTest {

    private val sampleItem = EquipmentItem(
        id = "eq_gladius_hispaniensis",
        nameRu = "Гладиус Испанский",
        type = EquipmentType.WEAPON,
        costDenarii = 40,
        attackBonus = 5,
        defenseBonus = 0,
        moraleBonus = 2,
        descRu = "Кованый испанский меч",
        isCrafted = false
    )

    @Test
    fun `craftItem spends denarii and marks equipment crafted`() {
        val equipment = listOf(sampleItem)
        val resources = LegionResources(denarii = 100, provisions = 50)

        val result = EquipmentEngine.craftItem(equipment, sampleItem.id, resources)
        assertTrue(result.isSuccess)
        assertTrue(result.updatedEquipment[0].isCrafted)
        assertEquals(60, result.updatedResources.denarii)
    }

    @Test
    fun `temperItem increments temper level up to max 3`() {
        val craftedItem = sampleItem.copy(isCrafted = true, temperLevel = 1)
        val equipment = listOf(craftedItem)
        val resources = LegionResources(denarii = 200, provisions = 50)

        val result = EquipmentEngine.temperItem(equipment, craftedItem.id, resources)
        assertTrue(result.isSuccess)
        assertEquals(2, result.updatedEquipment[0].temperLevel)
    }

    @Test
    fun `toggleEquip equips and unequips items for cohort`() {
        val craftedItem = sampleItem.copy(isCrafted = true, equippedCohortId = null)
        val equipment = listOf(craftedItem)

        // Equip
        val equipped = EquipmentEngine.toggleEquip(equipment, sampleItem.id, "coh_1")
        assertEquals("coh_1", equipped[0].equippedCohortId)

        // Unequip
        val unequipped = EquipmentEngine.toggleEquip(equipped, sampleItem.id, "coh_1")
        assertNull(unequipped[0].equippedCohortId)
    }
}
