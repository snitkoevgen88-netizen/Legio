package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Building
import com.example.model.BuildingType
import com.example.model.LegionResources
import com.example.model.SeasonalPlan
import com.example.ui.theme.*

@Composable
fun BuildingsScreen(
    buildings: List<Building>,
    resources: LegionResources,
    seasonalPlan: SeasonalPlan,
    onSetUpgradeBuilding: (BuildingType?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🏗️ Строительство и инфраструктура лагеря",
                color = RomanGoldLight,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Повышайте уровень зданий для открытия новых тактик, спасения раненых и точной разведки.",
                color = RomanTextMuted,
                fontSize = 12.sp
            )
        }

        items(buildings, key = { it.type.name }) { building ->
            val isSelectedForUpgrade = seasonalPlan.upgradeBuildingType == building.type
            val isMaxLevel = building.level >= building.maxLevel
            val canAfford = resources.denarii >= building.upgradeCostDenarii && resources.provisions >= building.upgradeCostProvisions

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("building_card_${building.type.name}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelectedForUpgrade) 2.dp else 1.dp,
                    if (isSelectedForUpgrade) RomanGold else RomanBronzeDark
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = building.type.icon, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = building.type.titleRu,
                                    color = RomanGoldLight,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = building.type.roleDescRu,
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isMaxLevel) RomanGold else RomanCrimson)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Ранг ${building.level} / ${building.maxLevel}",
                                color = if (isMaxLevel) RomanDarkSurface else RomanGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current effect box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanDarkSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Текущие возможности:",
                                color = RomanGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = building.currentPerkRu,
                                color = RomanParchment,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (!isMaxLevel) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Стоимость: 💰 ${building.upgradeCostDenarii} | 🌾 ${building.upgradeCostProvisions}",
                                color = if (canAfford) RomanGoldLight else DefeatRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Button(
                                onClick = {
                                    if (isSelectedForUpgrade) onSetUpgradeBuilding(null) else onSetUpgradeBuilding(building.type)
                                },
                                enabled = canAfford || isSelectedForUpgrade,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelectedForUpgrade) RomanGold else RomanCrimson,
                                    contentColor = if (isSelectedForUpgrade) RomanDarkSurface else RomanGoldLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("upgrade_btn_${building.type.name}")
                            ) {
                                Text(
                                    text = if (isSelectedForUpgrade) "✓ В плане сезона" else "Улучшить",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✨ Легендарное строение (Макс. уровень)",
                            color = RomanGreenLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
