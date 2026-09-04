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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChronicleEntry
import com.example.model.ExpeditionOutcome
import com.example.ui.theme.*

@Composable
fun ChronicleScreen(
    chronicles: List<ChronicleEntry>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Summary calculations
    val totalEntries = chronicles.size
    val totalVictories = chronicles.count { it.outcome?.isSuccess == true }
    val totalDefeats = chronicles.count { it.outcome?.isSuccess == false }
    val totalCasualties = chronicles.sumOf { it.casualties }
    val totalSpoils = chronicles.sumOf { it.lootDenarii.coerceAtLeast(0) }
    val totalGlory = chronicles.sumOf { it.gloryEarned.coerceAtLeast(0) }

    val filteredChronicles = when (selectedFilter) {
        "BATTLES" -> chronicles.filter { it.outcome != null }
        "SENATE" -> chronicles.filter { it.headlineRu.contains("Сенат") || it.headlineRu.contains("Курия") || it.headlineRu.contains("эдикт") || it.headlineRu.contains("жалование") }
        "ECONOMY" -> chronicles.filter { it.headlineRu.contains("имени") || it.headlineRu.contains("банк") || it.headlineRu.contains("урожай") || it.headlineRu.contains("рынок") }
        else -> chronicles
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Header and 20-Year Grand Annals Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📜 Великие Анналы Legio IV",
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Летопись всех сезонов, походов и славы легиона",
                                color = RomanParchment,
                                fontSize = 11.sp
                            )
                        }
                        Text(text = "🏛️ SPQR", color = TriumphGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = RomanGoldDark.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            icon = "🏆",
                            label = "Триумфы",
                            value = "$totalVictories / $totalDefeats"
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            icon = "🩸",
                            label = "Павшие",
                            value = "$totalCasualties"
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            icon = "💰",
                            label = "Трофеи",
                            value = "+$totalSpoils 🪙"
                        )
                        SummaryPill(
                            modifier = Modifier.weight(1f),
                            icon = "⭐",
                            label = "Слава",
                            value = "+$totalGlory"
                        )
                    }
                }
            }
        }

        // 2. Filter Category Chips
        item {
            val filters = listOf(
                "ALL" to "📜 Все записи ($totalEntries)",
                "BATTLES" to "⚔️ Походы и битвы",
                "SENATE" to "🏛️ Сенат и декреты",
                "ECONOMY" to "🌾 Казна и имения"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { (key, title) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) RomanCrimson else RomanDarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RomanGold else RomanBronzeDark
                        ),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) RomanGoldLight else RomanTextLight,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. Chronicle Entries List
        items(filteredChronicles, key = { it.id }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (entry.outcome?.isSuccess == true) RomanGoldDark else RomanBronzeDark
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.seasonFormatted,
                            color = RomanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.outcome != null) {
                            Text(
                                text = "${entry.outcome.icon} ${entry.outcome.titleRu}",
                                color = if (entry.outcome.isSuccess) RomanGreenLight else DefeatRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = entry.headlineRu,
                        color = RomanGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = entry.textRu,
                        color = RomanTextLight,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    if (entry.commanderName.isNotEmpty() || entry.cohortName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (entry.commanderName.isNotEmpty()) {
                                Text(
                                    text = "👤 ${entry.commanderName}",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (entry.cohortName.isNotEmpty()) {
                                Text(
                                    text = "🛡️ ${entry.cohortName}",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (entry.lootDenarii != 0 || entry.casualties > 0 || entry.gloryEarned != 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = RomanBronzeDark.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (entry.casualties > 0) {
                                Text(text = "🩸 Потери: -${entry.casualties}", color = DefeatRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            if (entry.lootDenarii != 0) {
                                Text(
                                    text = "💰 ${if (entry.lootDenarii > 0) "+${entry.lootDenarii}" else "${entry.lootDenarii}"} 🪙",
                                    color = if (entry.lootDenarii > 0) RomanGoldLight else DefeatRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (entry.gloryEarned != 0) {
                                Text(
                                    text = "⭐ Слава: ${if (entry.gloryEarned > 0) "+${entry.gloryEarned}" else "${entry.gloryEarned}"}",
                                    color = TriumphGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun SummaryPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = RomanDarkSurface,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, RomanBronzeDark)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 12.sp)
            Text(text = value, color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = RomanTextMuted, fontSize = 8.sp)
        }
    }
}
