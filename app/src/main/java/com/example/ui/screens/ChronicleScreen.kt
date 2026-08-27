package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChronicleEntry
import com.example.ui.theme.*

@Composable
fun ChronicleScreen(
    chronicles: List<ChronicleEntry>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "📜 Анналы и Хроники Legio IV «Invicta»",
                color = RomanGoldLight,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Летопись всех военных кампаний, триумфов, решений Сената и павших героев.",
                color = RomanTextMuted,
                fontSize = 12.sp
            )
        }

        items(chronicles, key = { it.id }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (entry.outcome?.isSuccess == true) RomanGoldDark else RomanBronzeDark
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = entry.textRu,
                        color = RomanTextLight,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    if (entry.commanderName != null || entry.cohortName != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "👤 Командир: ${entry.commanderName ?: "—"}",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "🛡️ Когорта: ${entry.cohortName ?: "—"}",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (entry.lootDenarii > 0 || entry.casualties > 0 || entry.gloryEarned != 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = RomanBronzeDark.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (entry.casualties > 0) {
                                Text(text = "🩸 Потери: -${entry.casualties}", color = DefeatRed, fontSize = 11.sp)
                            }
                            if (entry.lootDenarii > 0) {
                                Text(text = "💰 Трофеи: +${entry.lootDenarii}", color = RomanGoldLight, fontSize = 11.sp)
                            }
                            if (entry.gloryEarned != 0) {
                                Text(text = "⭐ Слава: ${if (entry.gloryEarned > 0) "+" else ""}${entry.gloryEarned}", color = TriumphGold, fontSize = 11.sp)
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
