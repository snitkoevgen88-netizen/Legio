package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompetingLegion
import com.example.ui.theme.*

@Composable
fun RankingScreen(
    competingLegions: List<CompetingLegion>,
    modifier: Modifier = Modifier
) {
    val sortedLegions = competingLegions.sortedByDescending { it.ratingScore }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "🏛️ Легионы Римской Республики",
                color = RomanGoldLight,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Таблица славы и воинской доблести легионов Сената и Народа Рима.",
                color = RomanTextMuted,
                fontSize = 12.sp
            )
        }

        itemsIndexed(sortedLegions) { index, legion ->
            val isPlayer = legion.id == "legio_4_player"
            val rankPos = index + 1
            val rankBadgeColor = when (rankPos) {
                1 -> TriumphGold
                2 -> Color(0xFFE0E0E0)
                3 -> Color(0xFFCD7F32)
                else -> RomanBronze
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPlayer) RomanCrimsonDark else RomanDarkSurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isPlayer) 2.dp else 1.dp,
                    if (isPlayer) RomanGold else RomanBronzeDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Position Number Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RomanDarkSurface)
                            .border(1.dp, rankBadgeColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rankPos",
                            color = rankBadgeColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Legion Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${legion.badgeSymbol} ${legion.name}",
                                color = if (isPlayer) RomanGoldLight else RomanParchment,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPlayer) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RomanGold)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(text = "ТВОЙ ЛЕГИОН", color = RomanDarkSurface, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "📍 Текущее дело: ${legion.currentActivityRu}",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "⚔️ Побед: ${legion.victories} • 🚩 Поражений: ${legion.defeats}",
                            color = RomanTextGold,
                            fontSize = 11.sp
                        )
                    }

                    // Rating Score
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${legion.ratingScore}",
                            color = RomanGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Рейтинг",
                            color = RomanTextMuted,
                            fontSize = 10.sp
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
