package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun StrategicMapCanvas(
    selectedProvince: StrategicProvince,
    roads: List<StrategicRoadUpgrade>,
    onSelectProvince: (StrategicProvince) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D09)),
        border = androidx.compose.foundation.BorderStroke(2.dp, RomanGoldDark)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw Roads between coordinates
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base Sea and Land outlines
                drawCircle(
                    color = Color(0xFF0F1A24),
                    radius = w * 0.45f,
                    center = Offset(w * 0.15f, h * 0.7f)
                )

                // Draw connecting roads
                val provCoords = StrategicProvince.entries.associateWith { prov ->
                    Offset(prov.mapX * w, prov.mapY * h)
                }

                // Road links: Latium -> Etruria, Latium -> Samnium, Latium -> Campania, Samnium -> Magna Graecia, Campania -> Sicilia
                fun drawRoad(p1: StrategicProvince, p2: StrategicProvince, isPaved: Boolean) {
                    val start = provCoords[p1] ?: return
                    val end = provCoords[p2] ?: return
                    drawLine(
                        color = if (isPaved) RomanGoldLight else RomanBronze,
                        start = start,
                        end = end,
                        strokeWidth = if (isPaved) 4f else 2f,
                        pathEffect = if (!isPaved) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }

                val isAppiaPaved = roads.find { it.id == "road_via_appia" }?.isPaved == true
                val isAureliaPaved = roads.find { it.id == "road_via_aurelia" }?.isPaved == true
                val isLatinaPaved = roads.find { it.id == "road_via_latina" }?.isPaved == true
                val isTraianaPaved = roads.find { it.id == "road_via_traiana" }?.isPaved == true

                drawRoad(StrategicProvince.LATIUM, StrategicProvince.ETRURIA, isAureliaPaved)
                drawRoad(StrategicProvince.LATIUM, StrategicProvince.SAMNIUM, isLatinaPaved)
                drawRoad(StrategicProvince.LATIUM, StrategicProvince.CAMPANIA, isAppiaPaved)
                drawRoad(StrategicProvince.SAMNIUM, StrategicProvince.MAGNA_GRAECIA, isTraianaPaved)
                drawRoad(StrategicProvince.CAMPANIA, StrategicProvince.SICILIA, false)
            }

            // Province Nodes on Map
            StrategicProvince.entries.forEach { prov ->
                val isSelected = prov == selectedProvince
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (prov.mapX * 280).dp.coerceAtMost(260.dp),
                            top = (prov.mapY * 170).dp.coerceAtMost(165.dp)
                        )
                ) {
                    Surface(
                        modifier = Modifier
                            .size(if (isSelected) 36.dp else 28.dp)
                            .clip(CircleShape)
                            .clickable { onSelectProvince(prov) }
                            .testTag("province_node_${prov.id}"),
                        color = if (isSelected) RomanCrimson else RomanDarkSurfaceCard,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) RomanGoldLight else RomanGoldDark
                        ),
                        shadowElevation = if (isSelected) 6.dp else 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = prov.icon,
                                fontSize = if (isSelected) 14.sp else 11.sp
                            )
                        }
                    }
                }
            }

            // Map Legend overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0xCC1A1009), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "🗺️ Italia & Mare Nostrum (315 BC)",
                    color = RomanGoldLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StrategicRoadsSection(
    roads: List<StrategicRoadUpgrade>,
    denarii: Int,
    onPaveRoad: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, RomanBronzeDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🛣️ Военные тракты Рима (Viae Romanae)",
                color = RomanGoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Каменные мощеные дороги ускоряют переброску легионов и снабжение провиантом.",
                color = RomanTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            roads.forEach { road ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(RomanDarkSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, if (road.isPaved) RomanGoldDark else RomanBronze, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = road.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = road.nameRu,
                                color = if (road.isPaved) RomanGoldLight else RomanParchment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = road.connectingProvincesRu, color = RomanTextGold, fontSize = 10.sp)
                        Text(text = road.speedAndSupplyBonusRu, color = RomanTextMuted, fontSize = 10.sp, maxLines = 2)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (road.isPaved) {
                        Text(
                            text = "✓ Замощена",
                            color = RomanGreenLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = { onPaveRoad(road.id) },
                            enabled = denarii >= road.costDenarii,
                            colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson, contentColor = RomanGoldLight),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("pave_road_${road.id}")
                        ) {
                            Text("Мостить (${road.costDenarii}🪙)", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
