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
import com.example.ui.theme.*

@Composable
fun CursusHonorumScreen(
    currentRank: MagistracyRank,
    electionCampaign: RomanElectionCampaign,
    resources: LegionResources,
    onFundGames: () -> Unit,
    onBribePatricians: () -> Unit,
    onDeliverSpeech: () -> Unit,
    onHoldElection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val target = electionCampaign.targetRank
    val isReadyToVote = electionCampaign.isReadyForVote &&
            resources.glory >= target.minGlory &&
            resources.senateFavor >= target.minSenateFavor

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current Magistracy Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentRank.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Текущий титул: ${currentRank.titleRu}",
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentRank.latinNameRu,
                                color = RomanGold,
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Одеяние: ${currentRank.togaTitleRu}", color = RomanParchment, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Привилегии: ${currentRank.bonusSummaryRu}", color = RomanGreenLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Election in Progress for Next Rank
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
                                text = "🏛️ Избирательная кампания в Комициях",
                                color = RomanGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Цель: избрание на пост «${target.titleRu}» (${target.latinNameRu})",
                                color = RomanTextGold,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${electionCampaign.totalElectionScore}% / 75%",
                            color = if (electionCampaign.isReadyForVote) RomanGreenLight else RomanGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (electionCampaign.totalElectionScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (electionCampaign.isReadyForVote) RomanGreenLight else RomanGold,
                        trackColor = Color(0xFF2E1C11)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Plebs vs Patricians Support
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = RomanDarkSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "🍞 Поддержка плебеев", color = RomanParchment, fontSize = 11.sp)
                                Text(text = "${electionCampaign.plebeianSupportPct}%", color = RomanGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Игр устроено: ${electionCampaign.gamesOrganizedCount}", color = RomanTextMuted, fontSize = 9.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = RomanDarkSurface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "🏛️ Сенаторы-патриции", color = RomanParchment, fontSize = 11.sp)
                                Text(text = "${electionCampaign.patricianSupportPct}%", color = RomanGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Казна вложена: ${electionCampaign.briberyBudgetSpent}🪙", color = RomanTextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campaign Action Buttons
                    Text(text = "Предвыборные действия полководца:", color = RomanGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onFundGames,
                            enabled = resources.denarii >= 40,
                            modifier = Modifier.weight(1f).testTag("fund_games_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RomanDarkSurface, contentColor = RomanGoldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronze),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎪 Игры (40🪙)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("+15% Плебеи", fontSize = 8.sp, color = RomanGreenLight)
                            }
                        }

                        Button(
                            onClick = onBribePatricians,
                            enabled = resources.denarii >= 60,
                            modifier = Modifier.weight(1f).testTag("bribe_senators_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RomanDarkSurface, contentColor = RomanGoldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronze),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💰 Сенат (60🪙)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("+20% Патриции", fontSize = 8.sp, color = RomanGreenLight)
                            }
                        }

                        Button(
                            onClick = onDeliverSpeech,
                            modifier = Modifier.weight(1f).testTag("forum_speech_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RomanDarkSurface, contentColor = RomanGoldLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronze),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🗣️ Речь на Форуме", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("+8% народ", fontSize = 8.sp, color = RomanGold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Final Election Button
                    Button(
                        onClick = onHoldElection,
                        enabled = isReadyToVote,
                        modifier = Modifier.fillMaxWidth().testTag("hold_election_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isReadyToVote) "👑 Начать Голосование в Комициях (Требует Слава ≥ ${target.minGlory})"
                            else "⏳ Требуется: 75% поддержки, Слава ≥ ${target.minGlory}, Сенат ≥ ${target.minSenateFavor}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Ladder of Cursus Honorum ranks
        item {
            Text(text = "Лестница должностей (Cursus Honorum):", color = RomanGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(MagistracyRank.entries) { rank ->
            val isCurrent = rank == currentRank
            val isPast = rank.ordinal < currentRank.ordinal
            val isTarget = rank == target

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) RomanCrimsonDark else if (isPast) Color(0xFF1E1610) else RomanDarkSurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isCurrent || isTarget) 2.dp else 1.dp,
                    if (isCurrent) RomanGold else if (isTarget) RomanGoldLight else RomanBronzeDark
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = rank.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rank.titleRu,
                                color = if (isCurrent) RomanGoldLight else RomanParchment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "★ ВЫ", color = RomanGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Text(text = rank.latinNameRu, color = RomanTextGold, fontSize = 10.sp)
                        Text(text = rank.bonusSummaryRu, color = RomanTextMuted, fontSize = 10.sp)
                    }
                    Text(
                        text = "⭐ ${rank.minGlory}",
                        color = RomanGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
