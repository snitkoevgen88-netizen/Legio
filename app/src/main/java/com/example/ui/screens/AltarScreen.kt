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
import com.example.model.ActiveBlessing
import com.example.model.DivineRitual
import com.example.model.GodType
import com.example.model.LegionResources
import com.example.ui.theme.*

@Composable
fun AltarScreen(
    rituals: List<DivineRitual>,
    activeBlessing: ActiveBlessing?,
    resources: LegionResources,
    onPerformRitual: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRitualForInspect by remember { mutableStateOf<DivineRitual?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Altar Header Card
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
                        text = "🕊️ Святилище Марса и Авгурии Рима",
                        color = RomanGoldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Перед каждым походом жрецы-понтифики и гаруспики вопрошают волю богов. Священные птицы (Auspicia Pullaria) укажут путь к триумфу или предостерегут от гибели.",
                        color = RomanParchment,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Active Blessing Banner
        item {
            if (activeBlessing != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2416)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TriumphGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = activeBlessing.god.icon,
                            fontSize = 32.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ ${activeBlessing.god.titleRu}",
                                    color = TriumphGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RomanGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Действует: ${activeBlessing.seasonsRemaining} сез.",
                                        color = RomanDarkSurface,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeBlessing.ritualNameRu,
                                color = RomanGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = activeBlessing.effectRu,
                                color = RomanGreenLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🕯️", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Нет активного благословения",
                                color = RomanGoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Совершите обряд на алтаре, чтобы получить милость богов на следующий сезон.",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Rituals Header
        item {
            Text(
                text = "⚡ Священные обряды и жертвоприношения",
                color = RomanGoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // List of Divine Rituals
        items(rituals) { ritual ->
            val canAfford = resources.denarii >= ritual.costDenarii && resources.provisions >= ritual.costProvisions
            val isCurrentlyActive = activeBlessing?.god == ritual.god

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRitualForInspect = ritual }
                    .testTag("ritual_card_${ritual.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentlyActive) Color(0xFF332617) else RomanDarkSurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrentlyActive) TriumphGold else RomanBronzeDark
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = ritual.god.icon, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = ritual.nameRu,
                                    color = RomanGoldLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ritual.god.titleRu} (${ritual.god.titleLatin})",
                                    color = RomanTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (isCurrentlyActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RomanLaurelGreen)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Освящено",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = ritual.descriptionRu,
                        color = RomanParchment,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1510))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✨ Эффект: ${ritual.blessingEffectRu}",
                            color = RomanGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (ritual.costDenarii > 0) {
                                Text(
                                    text = "💰 ${ritual.costDenarii} ден.",
                                    color = if (resources.denarii >= ritual.costDenarii) RomanGoldLight else DefeatRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (ritual.costProvisions > 0) {
                                Text(
                                    text = "🌾 ${ritual.costProvisions} пров.",
                                    color = if (resources.provisions >= ritual.costProvisions) Color(0xFFC5E1A5) else DefeatRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = { onPerformRitual(ritual.id) },
                            enabled = canAfford && !isCurrentlyActive,
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("perform_ritual_${ritual.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RomanGold,
                                contentColor = RomanDarkSurface,
                                disabledContainerColor = Color(0xFF3E332B),
                                disabledContentColor = RomanTextMuted
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCurrentlyActive) "Обряд совершён" else "🕯️ Совершить обряд",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
