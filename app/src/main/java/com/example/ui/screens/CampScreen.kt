package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.BuildingUpgradeDialog
import com.example.ui.pixelart.PixelCampTopDownView
import com.example.ui.pixelart.PixelCampView
import com.example.ui.theme.*

@Composable
fun CampScreen(
    seasonYear: SeasonYear,
    resources: LegionResources,
    campRank: CampRank,
    republicRank: RepublicRank,
    buildings: List<Building>,
    cohorts: List<Cohort>,
    commanders: List<Commander>,
    seasonalPlan: SeasonalPlan,
    onOpenSeasonPlan: () -> Unit,
    onAutoPlanSeason: () -> Unit,
    onBuildingUpgrade: (BuildingType) -> Unit,
    onNavigateToCohorts: () -> Unit,
    onNavigateToExpeditions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBuildingForModal by remember { mutableStateOf<Building?>(null) }
    var isTopDownView by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // View Mode Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF23160F), RoundedCornerShape(8.dp))
                    .border(1.dp, RomanGoldDark, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isTopDownView = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isTopDownView) RomanCrimson else Color.Transparent,
                    border = if (isTopDownView) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null
                ) {
                    Text(
                        text = "🗺️ План лагеря (Вид сверху)",
                        color = if (isTopDownView) RomanGoldLight else RomanTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isTopDownView) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isTopDownView = false },
                    shape = RoundedCornerShape(6.dp),
                    color = if (!isTopDownView) RomanCrimson else Color.Transparent,
                    border = if (!isTopDownView) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null
                ) {
                    Text(
                        text = "🌄 Панорама (Вид сбоку)",
                        color = if (!isTopDownView) RomanGoldLight else RomanTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (!isTopDownView) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // 1. Pixel Art Animated Camp (Top-Down or Side View)
        item {
            if (isTopDownView) {
                PixelCampTopDownView(
                    seasonYear = seasonYear,
                    buildings = buildings,
                    cohorts = cohorts,
                    commanders = commanders,
                    onBuildingClick = { bType ->
                        selectedBuildingForModal = buildings.find { it.type == bType }
                    }
                )
            } else {
                PixelCampView(
                    seasonYear = seasonYear,
                    buildings = buildings,
                    cohorts = cohorts,
                    commanders = commanders,
                    onBuildingClick = { bType ->
                        selectedBuildingForModal = buildings.find { it.type == bType }
                    }
                )
            }
        }

        // 2. Camp & Republic Rank Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🏛️ ${campRank.titleRu}",
                            color = RomanGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = campRank.perkRu,
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "👑 ${republicRank.titleRu}",
                            color = TriumphGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ранг Республики",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. Central "План Сезона / Завершить сезон" Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Военный совет: ${seasonYear.season.titleRu}",
                        color = RomanGoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = seasonYear.season.effectDescRu,
                        color = RomanParchment,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onAutoPlanSeason,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(46.dp)
                                .testTag("quick_auto_plan_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RomanGoldLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚡ Авто-план",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onOpenSeasonPlan,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("end_season_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (seasonalPlan.hasAnyAction()) "📜 Завершить сезон" else "📜 План сезона",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Legion Readiness Overview
        item {
            Text(
                text = "⚔️ Боеготовность когорт",
                color = RomanGoldLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(cohorts) { cohort ->
            val assignedCmd = commanders.find { it.id == cohort.assignedCommanderId }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCohorts() },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = cohort.name,
                            color = RomanParchment,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cohort.rankLabel} • Уровень ${cohort.level}",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛡️ ${cohort.soldiers}/${cohort.maxSoldiers}",
                            color = if (cohort.soldiers < 50) DefeatRed else RomanGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "🎖️ ${cohort.veteransCount} вет.",
                            color = RomanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom space for navigation bar
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal if building clicked
    selectedBuildingForModal?.let { building ->
        BuildingUpgradeDialog(
            building = building,
            resources = resources,
            onUpgrade = {
                onBuildingUpgrade(building.type)
                selectedBuildingForModal = null
            },
            onDismiss = { selectedBuildingForModal = null }
        )
    }
}
