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
    activeBlessing: ActiveBlessing?,
    onOpenSeasonPlan: () -> Unit,
    onAutoPlanSeason: () -> Unit,
    onBuildingUpgrade: (BuildingType) -> Unit,
    onNavigateToCohorts: () -> Unit,
    onNavigateToExpeditions: () -> Unit,
    onNavigateToAltar: () -> Unit,
    onNavigateToCouncil: () -> Unit,
    onNavigateToTraining: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBuildingForModal by remember { mutableStateOf<Building?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Pixel Art Top-Down Tactical Castra
        item {
            PixelCampTopDownView(
                seasonYear = seasonYear,
                buildings = buildings,
                cohorts = cohorts,
                commanders = commanders,
                onBuildingClick = { bType ->
                    selectedBuildingForModal = buildings.find { it.type == bType }
                }
            )
        }

        // 2. Active Blessing / Divine Favor Ribbon (if active)
        if (activeBlessing != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAltar() },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2216)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TriumphGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = activeBlessing.god.icon, fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ Покровительство: ${activeBlessing.god.titleRu}",
                                    color = TriumphGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeBlessing.seasonsRemaining} сез.",
                                    color = RomanGoldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = activeBlessing.effectRu,
                                color = RomanGreenLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Camp & Republic Rank Status Banner
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

        // 4. Central "План Сезона / Завершить сезон" Action Card
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
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

        // 5. Quick Shortcuts Row: 🕊️ Алтарь Богов | 🦅 Совет & Трофеи
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAltar() },
                    shape = RoundedCornerShape(8.dp),
                    color = RomanDarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🕊️", fontSize = 18.sp)
                        Column {
                            Text(text = "Алтарь Богов", color = RomanGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Авгурии и жертвы", color = RomanTextMuted, fontSize = 10.sp)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToCouncil() },
                    shape = RoundedCornerShape(8.dp),
                    color = RomanDarkSurfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🦅", fontSize = 18.sp)
                        Column {
                            Text(text = "Совет & Трофеи", color = RomanGoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Речь и реликвии", color = RomanTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 5.5 Quick Link to Campus Martius / Training Screen
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTraining() },
                shape = RoundedCornerShape(10.dp),
                color = RomanDarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RomanCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏋️", fontSize = 18.sp)
                        }
                        Column {
                            Text(
                                text = "Марсово поле: Муштра и набор юнитов",
                                color = RomanGoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Набор гастатов, принципов, триариев и тренировка с прогрессом",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "Войти →",
                        color = RomanGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. Quick Legion Readiness Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚔️ Боеготовность когорт",
                    color = RomanGoldLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Все когорты →",
                    color = RomanGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToCohorts() }
                )
            }
        }

        items(cohorts) { cohort ->
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

        item {
            Spacer(modifier = Modifier.height(16.dp))
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
