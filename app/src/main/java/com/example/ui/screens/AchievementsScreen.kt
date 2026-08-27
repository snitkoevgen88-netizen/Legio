package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.Achievement
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    totalVictories: Int,
    totalGreatVictories: Int,
    totalDefeats: Int,
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    val unlockedCount = achievements.count { it.isUnlocked }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "🏆 Зал Славы и Достижения",
                color = RomanGoldLight,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Открыто $unlockedCount из ${achievements.size} достижений Республики.",
                color = RomanTextMuted,
                fontSize = 12.sp
            )
        }

        // Summary stats card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏆 Триумфы", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "$totalGreatVictories", color = TriumphGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "⚔️ Победы", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "$totalVictories", color = RomanGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🚩 Поражения", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "$totalDefeats", color = DefeatRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔥 Серия", color = RomanTextMuted, fontSize = 11.sp)
                        Text(text = "$currentStreak", color = RomanGoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(achievements, key = { it.id }) { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (ach.isUnlocked) RomanDarkSurfaceCard else RomanDarkSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (ach.isUnlocked) RomanGold else RomanBronzeDark.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (ach.isUnlocked) RomanCrimson else Color(0xFF2C2523))
                            .border(1.dp, if (ach.isUnlocked) RomanGold else RomanBronzeDark, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = ach.icon, fontSize = 20.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ach.titleRu,
                                color = if (ach.isUnlocked) RomanGoldLight else RomanTextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (ach.isUnlocked) {
                                Text(
                                    text = "✓ Получено",
                                    color = RomanGreenLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = ach.descRu,
                            color = if (ach.isUnlocked) RomanTextLight else RomanTextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "🎁 Бонус: ${ach.bonusPerkRu}",
                            color = if (ach.isUnlocked) RomanGold else RomanTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
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
