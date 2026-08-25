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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LegionResources
import com.example.model.SenateQuest
import com.example.ui.theme.*

@Composable
fun SenateScreen(
    senateQuests: List<SenateQuest>,
    resources: LegionResources,
    onClaimQuest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Senate Curia Banner
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
                                listOf(Color(0xFF311B92).copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏛️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Сенат и Римский Народ (SPQR)",
                                color = RomanGoldLight,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Поручения Консулов, Цензоров и Трибунов Республики",
                                color = RomanTextGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x80000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Расположение Сената (Favor):", color = RomanTextMuted, fontSize = 12.sp)
                        Text(
                            text = "🏛️ ${resources.senateFavor} / 100",
                            color = RomanGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { resources.senateFavor / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RomanGold,
                        trackColor = Color(0xFF3E2723)
                    )
                }
            }
        }

        items(senateQuests) { quest ->
            SenateQuestCard(
                quest = quest,
                onClaim = { onClaimQuest(quest.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SenateQuestCard(
    quest: SenateQuest,
    onClaim: () -> Unit
) {
    val borderColor = if (quest.isClaimed) RomanGoldDark.copy(alpha = 0.3f) else if (quest.isFinished) RomanGoldLight else RomanGoldDark.copy(alpha = 0.5f)

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
                Text(text = "📜", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.titleRu,
                        color = if (quest.isClaimed) RomanTextMuted else RomanGoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "От: ${quest.issuerRu}",
                        color = RomanTextGold,
                        fontSize = 11.sp
                    )
                }

                if (quest.isClaimed) {
                    Surface(
                        color = Color(0x334CAF50),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "✓ НАГРАДА ПОЛУЧЕНА",
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = quest.descriptionRu,
                color = RomanTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Прогресс:", color = RomanTextMuted, fontSize = 10.sp)
                Text(
                    text = "${quest.currentProgress} / ${quest.targetCount}",
                    color = if (quest.isFinished) RomanGoldLight else RomanTextGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { (quest.currentProgress.toFloat() / quest.targetCount.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (quest.isFinished) RomanGoldLight else RomanCrimson,
                trackColor = Color(0xFF2B1D16)
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Rewards Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x66000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Награда: 🪙 +${quest.rewardDenarii}", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "🏛️ +${quest.rewardSenateFavor} Милость", color = Color(0xFF90CAF9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = "🏆 +${quest.rewardGlory} Слава", color = Color(0xFFFFCC80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (quest.isFinished && !quest.isClaimed) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("claim_senate_quest_${quest.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson)
                ) {
                    Text(
                        text = "Получить награду Сената",
                        color = RomanGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
