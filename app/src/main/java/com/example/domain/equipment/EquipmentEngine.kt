package com.example.domain.equipment

import com.example.model.*
import kotlin.math.min

object EquipmentEngine {

    data class EquipmentActionResult(
        val isSuccess: Boolean,
        val updatedEquipment: List<EquipmentItem>,
        val updatedResources: LegionResources,
        val messageRu: String
    )

    /**
     * Crafts a new piece of equipment for the legion.
     */
    fun craftItem(
        equipment: List<EquipmentItem>,
        itemId: String,
        resources: LegionResources
    ): EquipmentActionResult {
        val target = equipment.find { it.id == itemId }
            ?: return EquipmentActionResult(false, equipment, resources, "Предмет не найден")

        if (target.isCrafted) {
            return EquipmentActionResult(false, equipment, resources, "Снаряжение уже выковано")
        }

        if (resources.denarii < target.costDenarii) {
            return EquipmentActionResult(false, equipment, resources, "Недостаточно денариев (${target.costDenarii} необходимо)")
        }

        val newResources = resources.copy(denarii = resources.denarii - target.costDenarii)
        val updatedEquipment = equipment.map {
            if (it.id == itemId) it.copy(isCrafted = true) else it
        }

        return EquipmentActionResult(
            isSuccess = true,
            updatedEquipment = updatedEquipment,
            updatedResources = newResources,
            messageRu = "«${target.nameRu}» успешно выковано в кузнице легиона!"
        )
    }

    /**
     * Tempers/sharpens existing crafted equipment up to level 3.
     */
    fun temperItem(
        equipment: List<EquipmentItem>,
        itemId: String,
        resources: LegionResources
    ): EquipmentActionResult {
        val target = equipment.find { it.id == itemId }
            ?: return EquipmentActionResult(false, equipment, resources, "Предмет не найден")

        if (!target.isCrafted) {
            return EquipmentActionResult(false, equipment, resources, "Сначала выкуйте базовый образец")
        }

        if (target.temperLevel >= 3) {
            return EquipmentActionResult(false, equipment, resources, "Достигнут максимальный уровень закалки (+3)")
        }

        val cost = (target.costDenarii * 0.6f * (target.temperLevel + 1)).toInt()
        if (resources.denarii < cost) {
            return EquipmentActionResult(false, equipment, resources, "Недостаточно денариев для закалки ($cost необходимо)")
        }

        val newResources = resources.copy(denarii = resources.denarii - cost)
        val updatedEquipment = equipment.map {
            if (it.id == itemId) it.copy(temperLevel = it.temperLevel + 1) else it
        }

        return EquipmentActionResult(
            isSuccess = true,
            updatedEquipment = updatedEquipment,
            updatedResources = newResources,
            messageRu = "«${target.nameRu}» закалено до +${target.temperLevel + 1}!"
        )
    }

    /**
     * Salvages equipment to recover partial denarii.
     */
    fun salvageItem(
        equipment: List<EquipmentItem>,
        itemId: String,
        resources: LegionResources
    ): EquipmentActionResult {
        val target = equipment.find { it.id == itemId }
            ?: return EquipmentActionResult(false, equipment, resources, "Предмет не найден")

        if (!target.isCrafted) {
            return EquipmentActionResult(false, equipment, resources, "Невозможно разобрать некованый предмет")
        }

        val recoveredDenarii = (target.costDenarii * 0.4f).toInt()
        val newResources = resources.copy(denarii = resources.denarii + recoveredDenarii)
        val updatedEquipment = equipment.map {
            if (it.id == itemId) it.copy(isCrafted = false, equippedCohortId = null, temperLevel = 0) else it
        }

        return EquipmentActionResult(
            isSuccess = true,
            updatedEquipment = updatedEquipment,
            updatedResources = newResources,
            messageRu = "«${target.nameRu}» переплавлено. Возвращено $recoveredDenarii денариев."
        )
    }

    /**
     * Assigns or unassigns equipment to a specific cohort.
     */
    fun toggleEquip(
        equipment: List<EquipmentItem>,
        itemId: String,
        cohortId: String?
    ): List<EquipmentItem> {
        val target = equipment.find { it.id == itemId } ?: return equipment

        return equipment.map { item ->
            when {
                item.id == itemId -> {
                    if (item.equippedCohortId == cohortId || cohortId == null) {
                        item.copy(equippedCohortId = null)
                    } else {
                        item.copy(equippedCohortId = cohortId)
                    }
                }
                // If another item of the same slot type is equipped on this cohort, unequip it
                cohortId != null && item.type == target.type && item.equippedCohortId == cohortId -> {
                    item.copy(equippedCohortId = null)
                }
                else -> item
            }
        }
    }

    /**
     * Automatically and intelligently distributes crafted equipment across all cohorts.
     */
    fun autoEquipAll(
        equipment: List<EquipmentItem>,
        cohorts: List<Cohort>
    ): List<EquipmentItem> {
        val craftedItems = equipment.filter { it.isCrafted }
        if (craftedItems.isEmpty() || cohorts.isEmpty()) return equipment

        return equipment.map { item ->
            if (!item.isCrafted) item
            else {
                val assignedCohort = when (item.type) {
                    EquipmentType.WEAPON -> cohorts.maxByOrNull { it.attackPower }
                    EquipmentType.ARMOR, EquipmentType.HELMET, EquipmentType.SHIELD -> cohorts.minByOrNull { it.defensePower }
                    EquipmentType.STANDARD, EquipmentType.ACCESSORY -> cohorts.maxByOrNull { it.veteransCount }
                }
                item.copy(equippedCohortId = assignedCohort?.id)
            }
        }
    }
}
