package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Cohort
import com.example.model.EquipmentItem
import com.example.model.EquipmentMaterial
import com.example.model.EquipmentType
import com.example.model.LegionResources
import com.example.ui.theme.*

@Composable
fun ArmoryScreen(
    equipment: List<EquipmentItem>,
    cohorts: List<Cohort>,
    resources: LegionResources,
    onCraftItem: (String) -> Unit,
    onTemperItem: (String) -> Unit = {},
    onSalvageItem: (String) -> Unit = {},
    onEquipItem: (String, String?) -> Unit,
    onAutoEquipAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCohortForEquip by remember { mutableStateOf<String?>(cohorts.firstOrNull()?.id) }
    var selectedTypeFilter by remember { mutableStateOf<EquipmentType?>(null) }

    val filteredEquipment = if (selectedTypeFilter != null) {
        equipment.filter { it.type == selectedTypeFilter }
    } else {
        equipment
    }

    val craftedCount = equipment.count { it.isCrafted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Forge Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4E342E).copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚒️", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Оружейная Fabrica (Кузница)",
                                    color = RomanGoldLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Выковано: $craftedCount / ${equipment.size} предметов",
                                    color = RomanTextGold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Auto-Equip Action Button
                        Button(
                            onClick = onAutoEquipAll,
                            enabled = craftedCount > 0,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("auto_equip_all_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface,
                                disabledContainerColor = RomanBronzeDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ Авто-экипировка",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Выберите когорту для экипировки:",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cohorts.forEach { cohort ->
                            val isSelected = selectedCohortForEquip == cohort.id
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCohortForEquip = cohort.id },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RomanCrimson else Color(0xFF2B221A),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) RomanGoldLight else RomanGoldDark.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = cohort.name.replace("IV Cohors ", ""),
                                        color = if (isSelected) RomanGoldLight else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⚔️${cohort.attackPower} 🛡️${cohort.defensePower}",
                                        color = RomanTextGold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    // Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTypeFilter == null,
                                onClick = { selectedTypeFilter = null },
                                label = { Text("Все типы (${equipment.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RomanGold,
                                    selectedLabelColor = RomanDarkSurface,
                                    containerColor = Color(0xFF241B15),
                                    labelColor = RomanGoldLight
                                )
                            )
                        }
                        items(EquipmentType.entries) { type ->
                            val count = equipment.count { it.type == type }
                            FilterChip(
                                selected = selectedTypeFilter == type,
                                onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                                label = { Text("${type.icon} ${type.titleRu} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RomanGold,
                                    selectedLabelColor = RomanDarkSurface,
                                    containerColor = Color(0xFF241B15),
                                    labelColor = RomanGoldLight
                                )
                            )
                        }
                    }
                }
            }
        }

        items(filteredEquipment) { item ->
            val equippedCohort = cohorts.find { it.id == item.equippedCohortId }
            EquipmentCard(
                item = item,
                canAfford = resources.denarii >= item.costDenarii,
                canAffordTemper = resources.denarii >= item.temperCostDenarii,
                equippedCohortName = equippedCohort?.name,
                selectedCohortId = selectedCohortForEquip,
                onCraft = { onCraftItem(item.id) },
                onTemper = { onTemperItem(item.id) },
                onSalvage = { onSalvageItem(item.id) },
                onEquipToggle = { cohortId ->
                    if (item.equippedCohortId == cohortId) {
                        onEquipItem(item.id, null) // Unequip
                    } else {
                        onEquipItem(item.id, cohortId) // Equip
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun EquipmentCard(
    item: EquipmentItem,
    canAfford: Boolean,
    canAffordTemper: Boolean,
    equippedCohortName: String?,
    selectedCohortId: String?,
    onCraft: () -> Unit,
    onTemper: () -> Unit,
    onSalvage: () -> Unit,
    onEquipToggle: (String?) -> Unit
) {
    val isEquippedOnSelected = item.equippedCohortId == selectedCohortId
    val borderColor = if (item.isCrafted) RomanGoldLight else RomanGoldDark.copy(alpha = 0.4f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isCrafted) RomanCrimson else Color(0xFF2B221A))
                        .border(1.dp, RomanGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.type.icon, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.nameRu,
                            color = if (item.isCrafted) RomanGoldLight else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.temperLevel > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "★".repeat(item.temperLevel),
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "${item.latinName} • ${item.type.titleRu}",
                        color = RomanTextGold,
                        fontSize = 10.sp
                    )
                }

                // Material Badge
                val materialBg = when (item.material) {
                    EquipmentMaterial.IMPERIAL_GOLD -> Color(0xFF6A1B9A)
                    EquipmentMaterial.SPANISH_STEEL -> Color(0xFF00695C)
                    EquipmentMaterial.IRON -> Color(0xFF37474F)
                    EquipmentMaterial.BRONZE -> Color(0xFF4E342E)
                }
                Surface(
                    color = materialBg,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${item.material.badge} ${item.material.titleRu}",
                        color = RomanGoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.descRu,
                color = RomanTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            // Stat bonuses row (calculated with temper bonus)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x66000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (item.totalAttackBonus > 0) {
                    Text(text = "⚔️ Атака +${item.totalAttackBonus}", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (item.totalDefenseBonus > 0) {
                    Text(text = "🛡️ Защита +${item.totalDefenseBonus}", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (item.casualtyReductionPct > 0) {
                    Text(text = "🏥 Потери -${item.casualtyReductionPct}%", color = Color(0xFF90CAF9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (item.moraleBonus > 0) {
                    Text(text = "🦅 Мораль +${item.moraleBonus}", color = Color(0xFFFFCC80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!item.isCrafted) {
                Button(
                    onClick = onCraft,
                    enabled = canAfford,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("craft_item_${item.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RomanCrimson,
                        disabledContainerColor = Color(0xFF3E2723)
                    )
                ) {
                    Text(
                        text = if (canAfford) "Выковать в кузнице (🪙 ${item.costDenarii} денариев)" else "Недостаточно монет (🪙 ${item.costDenarii})",
                        color = if (canAfford) RomanGoldLight else RomanTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Crafted Item controls: Tempering + Salvaging + Cohort Equip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Temper button if < max level (3)
                    if (item.temperLevel < 3) {
                        OutlinedButton(
                            onClick = onTemper,
                            enabled = canAffordTemper,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("temper_item_${item.id}"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33FFA000)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (canAffordTemper) RomanGoldLight else RomanBronzeDark)
                        ) {
                            Text(
                                text = "🔥 Закалить (+1) 🪙${item.temperCostDenarii}",
                                color = if (canAffordTemper) RomanGoldLight else RomanTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Salvage button
                    OutlinedButton(
                        onClick = onSalvage,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(36.dp)
                            .testTag("salvage_item_${item.id}"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33424242)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF757575))
                    ) {
                        Text(
                            text = "♻️ Сдать +🪙${item.salvageDenarii}",
                            color = Color(0xFFEEEEEE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (selectedCohortId != null) {
                    OutlinedButton(
                        onClick = { onEquipToggle(selectedCohortId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("equip_item_${item.id}"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isEquippedOnSelected) Color(0xFF4A148C) else Color(0x33000000)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
                    ) {
                        Text(
                            text = if (isEquippedOnSelected) "✓ Снять снаряжение с выбранной когорты" else "🛡️ Надеть на когорту",
                            color = RomanGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
