package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TrainingScreen(
    unitAllocations: List<UnitTrainingAllocation>,
    cohorts: List<Cohort>,
    resources: LegionResources,
    onUpdateCount: (UnitType, Int) -> Unit,
    onUpdateDrillIntensity: (UnitType, DrillIntensity) -> Unit,
    onUpdateTargetCohort: (UnitType, String) -> Unit,
    onStartTraining: (UnitType) -> Unit,
    onCancelTraining: (UnitType) -> Unit,
    onInstantComplete: (UnitType) -> Unit,
    onAutoAllocateBalanced: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAllocatedDenarii = unitAllocations.filter { !it.isTrainingActive }.sumOf { it.totalCostDenarii }
    val totalAllocatedProvisions = unitAllocations.filter { !it.isTrainingActive }.sumOf { it.totalCostProvisions }
    val activeTrainingCount = unitAllocations.count { it.isTrainingActive }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⚔️ Campus Martius: Набор и муштра",
                                color = RomanGoldLight,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Text(
                                text = "Формирование манипулярного строя Triplex Acies",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Auto-balance quick action
                        Button(
                            onClick = onAutoAllocateBalanced,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("auto_allocate_units_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanCrimson,
                                contentColor = RomanGoldLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚖️ Баланс",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resource allocation budget gauges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanDarkSurface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🪙 Казна на учения: $totalAllocatedDenarii / ${resources.denarii}",
                                    color = if (totalAllocatedDenarii > resources.denarii) DefeatRed else RomanGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val denariiRatio = if (resources.denarii > 0) {
                                (totalAllocatedDenarii.toFloat() / resources.denarii.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            LinearProgressIndicator(
                                progress = { denariiRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (totalAllocatedDenarii > resources.denarii) DefeatRed else RomanGold,
                                trackColor = Color(0xFF2C221B)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🌾 Зерно на обоз: $totalAllocatedProvisions / ${resources.provisions}",
                                    color = if (totalAllocatedProvisions > resources.provisions) DefeatRed else RomanGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val provRatio = if (resources.provisions > 0) {
                                (totalAllocatedProvisions.toFloat() / resources.provisions.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            LinearProgressIndicator(
                                progress = { provRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (totalAllocatedProvisions > resources.provisions) DefeatRed else RomanLaurelGreen,
                                trackColor = Color(0xFF2C221B)
                            )
                        }
                    }

                    if (activeTrainingCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚡ Идут активные учения на плацу: $activeTrainingCount отр.",
                            color = RomanGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // List of unit types to allocate resources & train
        items(unitAllocations, key = { it.unitType.name }) { allocation ->
            UnitTrainingCard(
                allocation = allocation,
                cohorts = cohorts,
                availableDenarii = resources.denarii,
                availableProvisions = resources.provisions,
                onUpdateCount = { count -> onUpdateCount(allocation.unitType, count) },
                onUpdateDrillIntensity = { intensity -> onUpdateDrillIntensity(allocation.unitType, intensity) },
                onUpdateTargetCohort = { cohortId -> onUpdateTargetCohort(allocation.unitType, cohortId) },
                onStartTraining = { onStartTraining(allocation.unitType) },
                onCancelTraining = { onCancelTraining(allocation.unitType) },
                onInstantComplete = { onInstantComplete(allocation.unitType) }
            )
        }

        // Triplex Acies Lore Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📜 Историческая справка: Манипулярная тактика Рима",
                        color = RomanGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "В республиканскую эпоху легион строился в три эшелона (Triplex Acies). Впереди действовали велиты (застрельщики), затем первую линию держали молодые гастаты с пилумами. Вторая линия состояла из опытных принципов в кольчугах, а в резерве стояли триарии — старейшие ветераны с длинными копьями, готовые переломить исход любого сражения.",
                        color = RomanTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UnitTrainingCard(
    allocation: UnitTrainingAllocation,
    cohorts: List<Cohort>,
    availableDenarii: Int,
    availableProvisions: Int,
    onUpdateCount: (Int) -> Unit,
    onUpdateDrillIntensity: (DrillIntensity) -> Unit,
    onUpdateTargetCohort: (String) -> Unit,
    onStartTraining: () -> Unit,
    onCancelTraining: () -> Unit,
    onInstantComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = allocation.unitType
    val canAfford = availableDenarii >= allocation.totalCostDenarii && availableProvisions >= allocation.totalCostProvisions
    val isTraining = allocation.isTrainingActive
    val targetCohort = cohorts.find { it.id == allocation.targetCohortId } ?: cohorts.firstOrNull()

    // Pulsating animation for active training progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "drill_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unit_training_card_${unit.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(
            if (isTraining) 2.dp else 1.dp,
            if (isTraining) RomanGold else RomanBronzeDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Names, and Line Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RomanCrimson)
                            .border(1.dp, RomanGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = unit.icon, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = unit.nameRu,
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${unit.latinName})",
                                color = RomanGold,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Text(
                            text = unit.lineRoleRu,
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Total trained badge
                if (allocation.totalTrainedSoFar > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RomanDarkSurface)
                            .border(1.dp, RomanGoldDark, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "В строю: ${allocation.totalTrainedSoFar}",
                            color = RomanGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description & Perk
            Text(
                text = unit.descriptionRu,
                color = RomanTextLight,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚡ Особое свойство: ${unit.specialPerkRu}",
                color = RomanGoldLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- DYNAMIC TRAINING PROGRESS BAR (ACTIVE DRILL) ---
            if (isTraining) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF261914))
                        .border(1.dp, RomanGold.copy(alpha = glowAlpha), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏳ Муштра на плацу: ${allocation.drillIntensity.titleRu}",
                                color = RomanGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Осталось: ${allocation.secondsRemaining} сек",
                            color = RomanGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Animated dynamic progress bar
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { allocation.currentProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = RomanGold,
                            trackColor = Color(0xFF3B281E)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Прогресс: ${(allocation.currentProgress * 100).toInt()}%",
                            color = RomanTextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Цель: ${targetCohort?.name ?: "Когорта"}",
                            color = RomanGoldLight,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons during active drill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onInstantComplete,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("instant_complete_${unit.name.lowercase()}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanLaurelGreen,
                                contentColor = RomanDarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ Завершить сейчас",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onCancelTraining,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("cancel_training_${unit.name.lowercase()}"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DefeatRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DefeatRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "❌ Отмена (75%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // --- RESOURCE ALLOCATION CONTROLS & DYNAMIC PREVIEWS ---

                // 1. Headcount Stepper & Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Численность набора: ${allocation.allocatedCount} легионеров",
                            color = RomanGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // -5
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RomanDarkSurface)
                                    .border(1.dp, RomanBronzeDark, RoundedCornerShape(4.dp))
                                    .clickable { onUpdateCount((allocation.allocatedCount - 5).coerceAtLeast(1)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "-5", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            // -1
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RomanDarkSurface)
                                    .border(1.dp, RomanBronzeDark, RoundedCornerShape(4.dp))
                                    .clickable { onUpdateCount((allocation.allocatedCount - 1).coerceAtLeast(1)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "-1", color = RomanGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            // +1
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RomanDarkSurface)
                                    .border(1.dp, RomanBronzeDark, RoundedCornerShape(4.dp))
                                    .clickable { onUpdateCount((allocation.allocatedCount + 1).coerceAtMost(50)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+1", color = RomanGoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            // +5
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RomanDarkSurface)
                                    .border(1.dp, RomanBronzeDark, RoundedCornerShape(4.dp))
                                    .clickable { onUpdateCount((allocation.allocatedCount + 5).coerceAtMost(50)) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+5", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Slider(
                        value = allocation.allocatedCount.toFloat(),
                        onValueChange = { onUpdateCount(it.toInt()) },
                        valueRange = 1f..50f,
                        steps = 48,
                        colors = SliderDefaults.colors(
                            thumbColor = RomanGold,
                            activeTrackColor = RomanGold,
                            inactiveTrackColor = RomanDarkSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slider_${unit.name.lowercase()}")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Drill Intensity Mode Selector
                Column {
                    Text(
                        text = "Интенсивность муштры:",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DrillIntensity.entries.forEach { intensity ->
                            val isSelected = allocation.drillIntensity == intensity
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) RomanCrimson else RomanDarkSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) RomanGold else RomanBronzeDark,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onUpdateDrillIntensity(intensity) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = intensity.titleRu,
                                        color = if (isSelected) RomanGoldLight else RomanTextLight,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "+${intensity.xpGain} XP | ${intensity.timeSeconds}c",
                                        color = if (isSelected) RomanGold else RomanTextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Target Cohort Selector
                Column {
                    Text(
                        text = "Пополнить когорту:",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cohorts.forEach { cohort ->
                            val isSelected = cohort.id == allocation.targetCohortId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) RomanBronzeDark else RomanDarkSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) RomanGold else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onUpdateTargetCohort(cohort.id) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cohort.name.substringBefore(" "),
                                    color = if (isSelected) RomanGoldLight else RomanTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Dynamic Projected Stats Gain & Progress Bars Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RomanDarkSurface)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Прирост атаки", color = RomanTextMuted, fontSize = 10.sp)
                        Text(
                            text = "+${allocation.projectedAttackGain} ATK",
                            color = RomanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Прирост защиты", color = RomanTextMuted, fontSize = 10.sp)
                        Text(
                            text = "+${allocation.projectedDefenseGain} DEF",
                            color = RomanLaurelGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Дисциплина", color = RomanTextMuted, fontSize = 10.sp)
                        Text(
                            text = "+${allocation.projectedDisciplineGain}",
                            color = RomanGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Шанс ветерана", color = RomanTextMuted, fontSize = 10.sp)
                        Text(
                            text = "${allocation.drillIntensity.veteranChancePct}%",
                            color = RomanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Training Launch Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Стоимость: ",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "🪙 ${allocation.totalCostDenarii} ",
                                color = if (availableDenarii < allocation.totalCostDenarii) DefeatRed else RomanGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🌾 ${allocation.totalCostProvisions}",
                                color = if (availableProvisions < allocation.totalCostProvisions) DefeatRed else RomanGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onStartTraining,
                        enabled = canAfford,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("start_training_${unit.name.lowercase()}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RomanCrimson,
                            contentColor = RomanGoldLight,
                            disabledContainerColor = RomanBronzeDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚔️ Начать муштру",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
