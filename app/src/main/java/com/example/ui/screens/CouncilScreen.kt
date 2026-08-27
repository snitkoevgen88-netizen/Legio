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
import com.example.model.LegionResources
import com.example.model.LegionTrophy
import com.example.ui.theme.*

@Composable
fun CouncilScreen(
    trophies: List<LegionTrophy>,
    resources: LegionResources,
    onHoldSpeech: () -> Unit,
    onPayDonativum: () -> Unit,
    onPerformLustratio: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCouncilTab by remember { mutableStateOf(0) } // 0 = Трибуна, 1 = Зал Трофеев

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏛️ Военный Совет и Зал Трофеев",
                        color = RomanGoldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Здесь легат Сципион держит совет со своими центурионами, вдохновляет легионеров на подвиги и хранит святыни, отбитые у врагов Рима.",
                        color = RomanParchment,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Sub-Tab Switcher
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
                        .clickable { selectedCouncilTab = 0 },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedCouncilTab == 0) RomanCrimson else Color.Transparent,
                    border = if (selectedCouncilTab == 0) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null
                ) {
                    Text(
                        text = "🗣️ Трибуна Полководца",
                        color = if (selectedCouncilTab == 0) RomanGoldLight else RomanTextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (selectedCouncilTab == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedCouncilTab = 1 },
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedCouncilTab == 1) RomanCrimson else Color.Transparent,
                    border = if (selectedCouncilTab == 1) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null
                ) {
                    Text(
                        text = "🏆 Зал Трофеев (${trophies.count { it.isUnlocked }}/${trophies.size})",
                        color = if (selectedCouncilTab == 1) RomanGoldLight else RomanTextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (selectedCouncilTab == 1) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        if (selectedCouncilTab == 0) {
            // SPEECHES & COMMANDER ACTIONS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🗣️", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Речь перед строем (Adlocutio)",
                                    color = RomanGoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Обращение к легиону с трибуны лагеря",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "«Воины Рима! В ваших жилах течет кровь Марса! Ни один варвар Самния или Галлии не устоит перед железной поступью наших манипул!»",
                            color = RomanParchment,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1510))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "✨ Эффект: восстанавливает мораль всех когорт до максимума (100%) и укрепляет верность центурионов.",
                                color = RomanGreenLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onHoldSpeech,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("hold_speech_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📢 Произнести пламенную речь к легиону", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "💰", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Выплата золотого донатива (Donativum)",
                                    color = RomanGoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Щедрая награда воинам из личной казны",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Донатив укрепляет братство оружия. Воины охотнее записываются в ветераны, зная, что Рим помнит об их пролитой крови.",
                            color = RomanParchment,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1510))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "✨ Эффект: превращает по 2 новобранца в каждой когорте в закаленных ветеранов и дает +10 расположения Сената.",
                                color = RomanGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val canAffordDonativum = resources.denarii >= 50
                        Button(
                            onClick = onPayDonativum,
                            enabled = canAffordDonativum,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("pay_donativum_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanCrimson,
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF3A2B23),
                                disabledContentColor = RomanTextMuted
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (canAffordDonativum) "🪙 Раздать Донатив (50 денариев)" else "Недостаточно золота (нужно 50 ден.)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🦅", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "Освящение знамен (Lustratio Exercitus)",
                                    color = RomanGoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Торжественный ритуал очищения лагерных орлов",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Штандарты когорт и священная Аквила омываются родниковой водой и окуриваются ладаном в присутствии всего легиона.",
                            color = RomanParchment,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1F1510))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "✨ Эффект: +20 Славы Рима и +3 постоянной дисциплины строя.",
                                color = TriumphGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val canAffordLustratio = resources.provisions >= 30
                        Button(
                            onClick = onPerformLustratio,
                            enabled = canAffordLustratio,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("lustratio_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface,
                                disabledContainerColor = Color(0xFF3A2B23),
                                disabledContentColor = RomanTextMuted
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (canAffordLustratio) "🦅 Освятить знамёна (30 провизии)" else "Недостаточно провизии (нужно 30 пров.)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // TROPHIES HALL
            items(trophies) { trophy ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trophy_card_${trophy.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (trophy.isUnlocked) Color(0xFF2B2117) else Color(0xFF1A130F)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (trophy.isUnlocked) TriumphGold else RomanBronzeDark.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = trophy.icon,
                            fontSize = 32.sp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trophy.titleRu,
                                    color = if (trophy.isUnlocked) TriumphGold else RomanTextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (trophy.isUnlocked) RomanLaurelGreen else Color(0xFF3E3129))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (trophy.isUnlocked) "В коллекции" else "Заблокировано",
                                        color = if (trophy.isUnlocked) Color.White else RomanTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Происхождение: ${trophy.originRu}",
                                color = RomanGoldDark,
                                fontSize = 10.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = trophy.descriptionRu,
                                color = if (trophy.isUnlocked) RomanParchment else RomanTextMuted.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            if (trophy.isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E1510))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "⚡ Пассивный бонус: ${trophy.passivePerkRu}",
                                        color = RomanGoldLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "🔒 Как получить: ${trophy.unlockConditionRu}",
                                    color = RomanCrimson,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
