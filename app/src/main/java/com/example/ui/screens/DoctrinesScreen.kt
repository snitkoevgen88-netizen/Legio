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
import com.example.model.MilitaryDoctrine
import com.example.ui.theme.*

@Composable
fun DoctrinesScreen(
    doctrines: List<MilitaryDoctrine>,
    resources: LegionResources,
    onUnlockDoctrine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Header Banner
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
                                listOf(RomanCrimsonDark.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🦅", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Военные Доктрины Рима",
                                color = RomanGoldLight,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Изучайте древние военные трактаты за Славу (Glory)",
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
                        Text(text = "Доступная Слава Легиона:", color = RomanTextMuted, fontSize = 12.sp)
                        Text(
                            text = "🏆 ${resources.glory}",
                            color = RomanGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(doctrines, key = { it.id }) { doctrine ->
            DoctrineCard(
                doctrine = doctrine,
                canAfford = resources.glory >= doctrine.costGlory,
                onUnlock = { onUnlockDoctrine(doctrine.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DoctrineCard(
    doctrine: MilitaryDoctrine,
    canAfford: Boolean,
    onUnlock: () -> Unit
) {
    val borderColor = if (doctrine.isUnlocked) RomanGoldLight else RomanGoldDark.copy(alpha = 0.5f)
    val bgColor = if (doctrine.isUnlocked) Color(0xFF2A1C12) else RomanDarkSurfaceCard

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (doctrine.isUnlocked) 1.5.dp else 1.dp, borderColor)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (doctrine.isUnlocked) RomanCrimson else Color(0xFF2B221A))
                        .border(1.dp, RomanGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = doctrine.icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doctrine.titleRu,
                            color = if (doctrine.isUnlocked) RomanGoldLight else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "«${doctrine.latinNameRu}»",
                        color = RomanTextGold,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                if (doctrine.isUnlocked) {
                    Surface(
                        color = Color(0x334CAF50),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Text(
                            text = "✓ ИЗУЧЕНО",
                            color = Color(0xFFA5D6A7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doctrine.descRu,
                color = RomanTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            // Effect Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x66000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚡ Эффект: ${doctrine.effectRu}",
                    color = if (doctrine.isUnlocked) RomanGoldLight else RomanTextGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!doctrine.isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onUnlock,
                    enabled = canAfford,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("unlock_doctrine_${doctrine.id}"),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RomanCrimson,
                        disabledContainerColor = Color(0xFF3E2723)
                    )
                ) {
                    Text(
                        text = if (canAfford) "Изучить доктрину (🏆 ${doctrine.costGlory} Славы)" else "Недостаточно славы (🏆 ${doctrine.costGlory})",
                        color = if (canAfford) RomanGoldLight else RomanTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
