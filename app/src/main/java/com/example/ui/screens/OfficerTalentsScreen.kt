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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.pixelart.PixelCommanderPortrait
import com.example.ui.theme.*

@Composable
fun OfficerTalentsScreen(
    commanders: List<Commander>,
    resources: LegionResources,
    aquilaState: LegionAquilaState,
    onRecruitCommander: () -> Unit,
    onLearnTalent: (String, OfficerTalent) -> Unit,
    onAwardCorona: (String, MilitaryCorona) -> Unit,
    onUpgradeAquila: () -> Unit,
    onSetVexillumColor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val recruitCost = 90
    val canRecruit = resources.denarii >= recruitCost
    var selectedTab by remember { mutableStateOf(0) } // 0 = Офицеры & Таланты, 1 = Наградные Венки (Coronas), 2 = Святыня Орла (Aquila)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "🎖️ Офицерский корпус и Святыни Легиона",
                    color = RomanGoldLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Развивайте таланты центурионов, жалуйте боевые венки и укрепляйте Орла легиона.",
                    color = RomanTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Sub-tabs row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF23160F), RoundedCornerShape(8.dp))
                    .border(1.dp, RomanGoldDark, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("🎖️ Таланты офицеров", "👑 Венки (Coronas)", "🦅 Орел (Aquila)").forEachIndexed { idx, label ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = idx },
                        shape = RoundedCornerShape(6.dp),
                        color = if (selectedTab == idx) RomanCrimson else Color.Transparent,
                        border = if (selectedTab == idx) androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight) else null
                    ) {
                        Text(
                            text = label,
                            color = if (selectedTab == idx) RomanGoldLight else RomanTextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Officers list + Talents tree
                item {
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Призвать знатного трибуна из Рима", color = RomanParchment, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Стоимость: 💰 $recruitCost денариев", color = RomanGold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = onRecruitCommander,
                                enabled = canRecruit,
                                colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("recruit_commander_btn")
                            ) {
                                Text("Призвать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(commanders) { commander ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("commander_card_${commander.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (commander.isAlive) RomanBronzeDark else DefeatRed
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PixelCommanderPortrait(commander = commander, size = 52.dp)

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = commander.name,
                                            color = if (commander.isAlive) RomanGoldLight else DefeatRed,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (commander.isAlive) RomanCrimson else Color(0xFF424242))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${commander.rankLabel} • Lvl ${commander.level}",
                                                color = RomanGoldLight,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Характер: ${commander.trait.icon} ${commander.trait.titleRu}",
                                        color = RomanTextGold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "🏆 Побед: ${commander.victoriesCount} (Триумфов: ${commander.greatVictoriesCount})",
                                        color = TriumphGold,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Talents section for this commander
                            Text(text = "Боевые таланты офицера:", color = RomanGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            OfficerTalent.entries.forEach { talent ->
                                val isLearned = commander.unlockedTalents.contains(talent)
                                val canLearn = commander.level >= talent.levelReq && resources.glory >= 8 && !isLearned

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .background(if (isLearned) Color(0xFF2C1E12) else RomanDarkSurface, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (isLearned) RomanGold else RomanBronzeDark, RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = talent.icon, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = talent.titleRu,
                                                color = if (isLearned) RomanGoldLight else RomanParchment,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "(${talent.branchRu})", color = RomanTextMuted, fontSize = 9.sp)
                                        }
                                        Text(text = talent.perkRu, color = RomanGreenLight, fontSize = 10.sp)
                                    }

                                    if (isLearned) {
                                        Text(text = "✓ Изучен", color = RomanGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Button(
                                            onClick = { onLearnTalent(commander.id, talent) },
                                            enabled = canLearn,
                                            colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = if (commander.level < talent.levelReq) "Lvl ${talent.levelReq}" else "Изучить (8⭐)", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Military Coronas (Наградные венки)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "👑 Воинские Венки Рима (Coronae Bellicae)",
                                color = RomanGoldLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Высшие знаки доблести Республики. Наделяют офицера могущественными пассивными аурами.",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                items(MilitaryCorona.entries) { corona ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = corona.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = corona.titleRu, color = RomanGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = corona.latinNameRu, color = RomanTextGold, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Аура: ${corona.auraPerkRu}", color = RomanGreenLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = corona.descriptionRu, color = RomanParchment, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Условие: ${corona.requirementRu}", color = RomanGold, fontSize = 10.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Award to commander picker row
                            Text(text = "Пожаловать венок офицеру:", color = RomanTextMuted, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(commanders.filter { it.isAlive }) { cmd ->
                                    val isAwarded = cmd.awardedCoronas.contains(corona)
                                    Button(
                                        onClick = { onAwardCorona(cmd.id, corona) },
                                        enabled = !isAwarded,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAwarded) RomanCrimson else RomanDarkSurface,
                                            contentColor = RomanGoldLight
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronze),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isAwarded) "✓ ${cmd.name}" else "+ ${cmd.name}",
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Aquila Shrine & Custom Vexillum
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RomanCrimsonDark),
                        border = androidx.compose.foundation.BorderStroke(2.dp, RomanGold)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🦅", fontSize = 36.sp)
                            Text(
                                text = aquilaState.aquilaNameRu,
                                color = RomanGoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = aquilaState.customVexillumMotto,
                                color = RomanGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = aquilaState.eaglePerkRu,
                                color = RomanParchment,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (aquilaState.eagleUpgradeLevel < 3) {
                                Button(
                                    onClick = onUpgradeAquila,
                                    enabled = resources.denarii >= aquilaState.upgradeCostDenarii,
                                    colors = ButtonDefaults.buttonColors(containerColor = RomanGold, contentColor = RomanDarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("upgrade_aquila_btn")
                                ) {
                                    Text(
                                        text = "Освятить Орла Tier ${aquilaState.eagleUpgradeLevel + 1} (${aquilaState.upgradeCostDenarii}🪙)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = "★ Святыня Invicta достигла высшего ранга!",
                                    color = RomanGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Vexillum Banner Color Customizer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "🚩 Цвет штандартов и вымпелов (Vexillum):", color = RomanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Пурпур Рима" to RomanCrimson,
                                    "Императорский" to Color(0xFF6A1B9A),
                                    "Золотой Марс" to Color(0xFFC67C00),
                                    "Железный Черный" to Color(0xFF37474F)
                                ).forEachIndexed { idx, (label, col) ->
                                    val isColSelected = aquilaState.selectedBannerColorIndex == idx
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSetVexillumColor(idx) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = col,
                                        border = if (isColSelected) androidx.compose.foundation.BorderStroke(2.dp, RomanGoldLight) else null
                                    ) {
                                        Text(
                                            text = label,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
