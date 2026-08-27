package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

private enum class SenateSubTab(val titleRu: String, val icon: String) {
    QUESTS("Поручения", "📜"),
    PETITIONS("Прошения", "🏛️"),
    TREASURY("Казна & Экономика", "🪙"),
    FACTIONS("Фракции", "⚖️"),
    DECREES("Указы & Речи", "📢")
}

@Composable
fun SenateScreen(
    senateQuests: List<SenateQuest>,
    senatePetitions: List<SenatePetition>,
    resources: LegionResources,
    investments: List<ProvincialInvestment> = emptyList(),
    bankingState: RomanBankingState = RomanBankingState(),
    marketState: MarketState = MarketState(),
    campLevel: Int = 1,
    totalSoldiers: Int = 300,
    onClaimQuest: (String) -> Unit,
    onResolvePetition: (String) -> Unit,
    onHoldSpeech: () -> Unit = {},
    onDonativum: () -> Unit = {},
    onLustratio: () -> Unit = {},
    onTradeProvisions: (Int, Int) -> Unit = { _, _ -> },
    onDispatchCaravan: () -> Unit = {},
    onCollectVectigal: () -> Unit = {},
    onUpgradeInvestment: (String) -> Unit = {},
    onDepositBank: (Int) -> Unit = {},
    onWithdrawBank: (Int) -> Unit = {},
    onTakeWarLoan: (Int) -> Unit = {},
    onRepayWarLoan: () -> Unit = {},
    onMintCoins: () -> Unit = {},
    onTradeProvisionsDynamic: (Int, Int, Boolean) -> Unit = { _, _, _ -> },
    onSellSpoils: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(SenateSubTab.QUESTS) }
    var selectedQuestCategory by remember { mutableStateOf(QuestCategory.ALL) }
    var showOnlyReadyToClaim by remember { mutableStateOf(false) }

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
                                listOf(Color(0xFF311B92).copy(alpha = 0.55f), Color.Transparent)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏛️", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Сенат и Римский Народ (SPQR)",
                                    color = RomanGoldLight,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Курия Гостилия • Высшая власть Республики",
                                    color = RomanTextGold,
                                    fontSize = 11.sp
                                )
                            }
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
                            color = if (resources.senateFavor >= 70) Color(0xFF81C784) else if (resources.senateFavor >= 40) RomanGoldLight else RomanCrimsonLight,
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
                        color = if (resources.senateFavor >= 70) Color(0xFF81C784) else RomanGold,
                        trackColor = Color(0xFF3E2723)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    // Sub-tab selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SenateSubTab.entries.forEach { tab ->
                            val isSelected = tab == activeSubTab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { activeSubTab = tab },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) RomanCrimson else Color(0xFF241B15),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) RomanGoldLight else RomanGoldDark.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = tab.icon, fontSize = 14.sp)
                                    Text(
                                        text = tab.titleRu.split(" ").first(),
                                        color = if (isSelected) RomanGoldLight else RomanTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (activeSubTab) {
            SenateSubTab.QUESTS -> {
                val completedUnclaimed = senateQuests.count { it.isFinished && !it.isClaimed }
                val activeUnfinished = senateQuests.count { !it.isFinished }
                val claimedCount = senateQuests.count { it.isClaimed }

                item {
                    // Category & Filter Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (completedUnclaimed > 0) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0x334CAF50),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF81C784))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "✨", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Выполнено поручений к получению: $completedUnclaimed",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Сенат готов выплатить денарии, поднять авторитет легиона и воздать славу Рима!",
                                            color = RomanTextGold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x33000000), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Активно: $activeUnfinished • Готово: $completedUnclaimed • В архиве: $claimedCount",
                                color = RomanTextGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            FilterChip(
                                selected = showOnlyReadyToClaim,
                                onClick = { showOnlyReadyToClaim = !showOnlyReadyToClaim },
                                label = {
                                    Text(
                                        text = if (showOnlyReadyToClaim) "Показать все" else "Только готовые ($completedUnclaimed)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RomanGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0x22FFFFFF),
                                    labelColor = RomanGoldLight
                                )
                            )
                        }

                        // Category Pills Horizontal Scroll
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(QuestCategory.entries) { cat ->
                                val count = if (cat == QuestCategory.ALL) {
                                    senateQuests.count()
                                } else {
                                    senateQuests.count { it.category == cat }
                                }
                                val isSelected = selectedQuestCategory == cat

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) RomanCrimson else Color(0x221E140F),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) RomanGoldLight else RomanGoldDark.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedQuestCategory = cat }
                                        .testTag("filter_quest_cat_${cat.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${cat.titleRu} ($count)",
                                            color = if (isSelected) Color.White else RomanTextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val filteredQuests = senateQuests.filter { quest ->
                    val matchesCategory = selectedQuestCategory == QuestCategory.ALL || quest.category == selectedQuestCategory
                    val matchesReady = !showOnlyReadyToClaim || (quest.isFinished && !quest.isClaimed)
                    matchesCategory && matchesReady
                }

                if (filteredQuests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📜", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Поручений в этой категории нет",
                                    color = RomanGoldLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Новые эдикты и декреты поступят в следующем сезоне или после закрытия текущих заданий.",
                                    color = RomanTextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredQuests, key = { it.id }) { quest ->
                        SenateQuestCard(
                            quest = quest,
                            onClaim = { onClaimQuest(quest.id) }
                        )
                    }
                }
            }

            SenateSubTab.PETITIONS -> {
                item {
                    Text(
                        text = "📜 Прошения в Курию (Senatus Consultum)",
                        color = RomanGoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Подавайте официальные прошения Сенату. Для их одобрения требуется достаточный авторитет (Favor) и средства.",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                }

                items(senatePetitions) { petition ->
                    SenatePetitionCard(
                        petition = petition,
                        currentFavor = resources.senateFavor,
                        currentDenarii = resources.denarii,
                        onResolve = { onResolvePetition(petition.id) }
                    )
                }
            }

            SenateSubTab.FACTIONS -> {
                item {
                    Text(
                        text = "⚖️ Политические фракции Сената Рима",
                        color = RomanGoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Баланс сил в Курии определяет характер поручений и благосклонность патрициев к вашему легиону.",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                }

                items(SenateFaction.entries) { faction ->
                    SenateFactionCard(faction = faction)
                }
            }

            SenateSubTab.TREASURY -> {
                item {
                    SenateTreasuryCard(
                        resources = resources,
                        investments = investments,
                        bankingState = bankingState,
                        marketState = marketState,
                        campLevel = campLevel,
                        totalSoldiers = totalSoldiers,
                        onUpgradeInvestment = onUpgradeInvestment,
                        onDepositBank = onDepositBank,
                        onWithdrawBank = onWithdrawBank,
                        onTakeWarLoan = onTakeWarLoan,
                        onRepayWarLoan = onRepayWarLoan,
                        onMintCoins = onMintCoins,
                        onTradeProvisionsDynamic = onTradeProvisionsDynamic,
                        onSellSpoils = onSellSpoils,
                        onDispatchCaravan = onDispatchCaravan,
                        onCollectVectigal = onCollectVectigal
                    )
                }
            }

            SenateSubTab.DECREES -> {
                item {
                    SenateDecreesCard(
                        resources = resources,
                        onHoldSpeech = onHoldSpeech,
                        onDonativum = onDonativum,
                        onLustratio = onLustratio
                    )
                }
            }
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
    val borderColor = when {
        quest.isClaimed -> RomanGoldDark.copy(alpha = 0.25f)
        quest.isFinished -> RomanGoldLight
        quest.priority == QuestPriority.SENATUS_CONSULTUM_ULTIMUM -> RomanCrimsonLight
        quest.priority == QuestPriority.URGENT -> RomanGold
        else -> RomanGoldDark.copy(alpha = 0.45f)
    }

    val cardBg = when {
        quest.isClaimed -> RomanDarkSurfaceCard.copy(alpha = 0.65f)
        quest.isFinished -> Color(0xFF2A1C16)
        quest.priority == QuestPriority.SENATUS_CONSULTUM_ULTIMUM -> Color(0xFF241214)
        else -> RomanDarkSurfaceCard
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(if (quest.isFinished) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category, Priority, Faction & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority Badge
                    val priorityColor = when (quest.priority) {
                        QuestPriority.SENATUS_CONSULTUM_ULTIMUM -> RomanCrimsonLight
                        QuestPriority.URGENT -> RomanGoldLight
                        QuestPriority.STANDARD -> Color(0xFFB0BEC5)
                    }
                    val priorityBg = when (quest.priority) {
                        QuestPriority.SENATUS_CONSULTUM_ULTIMUM -> Color(0x33D32F2F)
                        QuestPriority.URGENT -> Color(0x33FFB300)
                        QuestPriority.STANDARD -> Color(0x2278909C)
                    }
                    Surface(
                        color = priorityBg,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, priorityColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${quest.priority.badge} ${quest.priority.titleRu}",
                            color = priorityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Faction Badge
                    Surface(
                        color = Color(0x221E140F),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, RomanGoldDark.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${quest.faction.icon} ${quest.faction.titleRu}",
                            color = RomanTextGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Status Indicator
                if (quest.isClaimed) {
                    Surface(
                        color = Color(0x224CAF50),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x6681C784))
                    ) {
                        Text(
                            text = "✓ ВЫПОЛНЕНО",
                            color = Color(0xFFA5D6A7),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (quest.isFinished) {
                    Surface(
                        color = Color(0x44FFB300),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight)
                    ) {
                        Text(
                            text = "✨ К ВРУЧЕНИЮ",
                            color = RomanGoldLight,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Icon + Title + Issuer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = quest.icon,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.titleRu,
                        color = if (quest.isClaimed) RomanTextMuted else RomanGoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Поручитель: ${quest.issuerRu}",
                        color = RomanTextGold,
                        fontSize = 11.sp
                    )
                }
            }

            // Flavor Quote if present
            if (quest.flavorHistoryRu.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0x1A000000),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "«${quest.flavorHistoryRu}»",
                        color = RomanTextMuted,
                        fontSize = 10.5.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // Description
            Text(
                text = quest.descriptionRu,
                color = if (quest.isClaimed) RomanTextMuted.copy(alpha = 0.7f) else Color(0xFFD7CCC8),
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )

            // Action Hint if not claimed
            if (!quest.isClaimed && !quest.actionHintRu.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💡", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = quest.actionHintRu,
                        color = Color(0xFFFFD54F),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Percentage
            val progressRatio = (quest.currentProgress.toFloat() / quest.targetCount.toFloat()).coerceIn(0f, 1f)
            val progressPercent = (progressRatio * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Прогресс выполнения ($progressPercent%):",
                    color = RomanTextMuted,
                    fontSize = 10.5.sp
                )
                Text(
                    text = "${quest.currentProgress} / ${quest.targetCount}",
                    color = if (quest.isFinished) Color(0xFF81C784) else RomanGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(3.5.dp)),
                color = if (quest.isFinished) Color(0xFF81C784) else RomanCrimson,
                trackColor = Color(0xFF2B1D16)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rewards Box
            Surface(
                color = Color(0x44000000),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙 +${quest.rewardDenarii} денариев",
                            color = RomanGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🏛️ +${quest.rewardSenateFavor}% милость",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🏆 +${quest.rewardGlory} слава",
                            color = Color(0xFFFFCC80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!quest.bonusPerkDescRu.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "⭐ Бонус: ${quest.bonusPerkDescRu}",
                            color = Color(0xFFA5D6A7),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Claim Action
            if (quest.isFinished && !quest.isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("claim_senate_quest_${quest.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RomanCrimson),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Принять почести и награду Сената",
                            color = RomanGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SenatePetitionCard(
    petition: SenatePetition,
    currentFavor: Int,
    currentDenarii: Int,
    onResolve: () -> Unit
) {
    val hasFavor = currentFavor >= petition.favorCost && currentFavor >= petition.minFavorRequired
    val hasDenarii = currentDenarii >= petition.denariiCost
    val canSubmit = hasFavor && hasDenarii

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (canSubmit) RomanGoldLight else RomanGoldDark.copy(alpha = 0.4f))
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF241B15))
                        .border(1.dp, RomanGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = petition.icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = petition.titleRu,
                        color = RomanGoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = petition.latinNameRu,
                        color = RomanTextGold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = petition.descriptionRu,
                color = RomanTextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Эффект: ${petition.rewardSummaryRu}",
                        color = Color(0xFFA5D6A7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Расход милости: 🏛️ ${petition.favorCost}",
                            color = if (currentFavor >= petition.favorCost) RomanGoldLight else RomanCrimsonLight,
                            fontSize = 10.sp
                        )
                        if (petition.denariiCost > 0) {
                            Text(
                                text = "Стоимость: 🪙 ${petition.denariiCost}",
                                color = if (hasDenarii) RomanGoldLight else RomanCrimsonLight,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "Мин. авторитет: 🏛️ ${petition.minFavorRequired}",
                            color = if (currentFavor >= petition.minFavorRequired) RomanTextMuted else RomanCrimsonLight,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onResolve,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("petition_btn_${petition.id}"),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RomanCrimson,
                    disabledContainerColor = Color(0xFF3E2723)
                )
            ) {
                Text(
                    text = if (canSubmit) "📜 Направить прошение в Сенат" else "Недостаточно авторитета или денариев",
                    color = if (canSubmit) RomanGoldLight else RomanTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SenateFactionCard(faction: SenateFaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = faction.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = faction.titleRu,
                        color = RomanGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Глава фракции: ${faction.leaderRu}",
                        color = RomanTextGold,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = faction.agendaRu, color = RomanTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Покровительство: ${faction.bonusDescRu}",
                    color = Color(0xFF90CAF9),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun SenateDecreesCard(
    resources: LegionResources,
    onHoldSpeech: () -> Unit,
    onDonativum: () -> Unit,
    onLustratio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "📢 Командирские указы и трибунал",
                color = RomanGoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Прямые обращения и вознаграждения для воинов и жрецов.",
                color = RomanTextMuted,
                fontSize = 11.sp
            )

            // Adlocutio
            OutlinedButton(
                onClick = onHoldSpeech,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x338E24AA)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldLight)
            ) {
                Text(
                    text = "🗣️ Adlocutio: Торжественная речь (+Мораль 100%, +Дисциплина)",
                    color = RomanGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Donativum
            val canDonativum = resources.denarii >= 40
            OutlinedButton(
                onClick = onDonativum,
                enabled = canDonativum,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33E65100)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (canDonativum) RomanGoldLight else RomanBronzeDark)
            ) {
                Text(
                    text = "🪙 Donativum: Выплата жалования (40 🪙 -> +10 Favor, +3 Ветерана)",
                    color = if (canDonativum) RomanGoldLight else RomanTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lustratio
            val canLustratio = resources.provisions >= 30
            OutlinedButton(
                onClick = onLustratio,
                enabled = canLustratio,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x331B5E20)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (canLustratio) RomanGoldLight else RomanBronzeDark)
            ) {
                Text(
                    text = "🦅 Lustratio: Освящение знамён (30 🍞 -> +3 Дисциплина)",
                    color = if (canLustratio) RomanGoldLight else RomanTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SenateTreasuryCard(
    resources: LegionResources,
    investments: List<ProvincialInvestment>,
    bankingState: RomanBankingState,
    marketState: MarketState,
    campLevel: Int,
    totalSoldiers: Int,
    onUpgradeInvestment: (String) -> Unit,
    onDepositBank: (Int) -> Unit,
    onWithdrawBank: (Int) -> Unit,
    onTakeWarLoan: (Int) -> Unit,
    onRepayWarLoan: () -> Unit,
    onMintCoins: () -> Unit,
    onTradeProvisionsDynamic: (Int, Int, Boolean) -> Unit,
    onSellSpoils: () -> Unit,
    onDispatchCaravan: () -> Unit,
    onCollectVectigal: () -> Unit
) {
    var economyTab by remember { mutableStateOf(EconomySubTab.TABULARIUM) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Economy Sub-Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EconomySubTab.entries.forEach { tab ->
                val isSelected = economyTab == tab
                Surface(
                    onClick = { economyTab = tab },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) RomanGold else Color(0x22FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) RomanGoldLight else RomanBronzeDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = tab.icon, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.titleRu,
                            color = if (isSelected) Color.Black else RomanGoldLight,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        when (economyTab) {
            EconomySubTab.TABULARIUM -> TabulariumBudgetSection(
                resources = resources,
                investments = investments,
                bankingState = bankingState,
                totalSoldiers = totalSoldiers
            )

            EconomySubTab.INVESTMENTS -> ProvincialInvestmentsSection(
                resources = resources,
                investments = investments,
                onUpgradeInvestment = onUpgradeInvestment
            )

            EconomySubTab.BANKING -> RomanBankingSection(
                resources = resources,
                bankingState = bankingState,
                campLevel = campLevel,
                onDepositBank = onDepositBank,
                onWithdrawBank = onWithdrawBank,
                onTakeWarLoan = onTakeWarLoan,
                onRepayWarLoan = onRepayWarLoan,
                onMintCoins = onMintCoins
            )

            EconomySubTab.MARKET -> ForumMarketSection(
                resources = resources,
                marketState = marketState,
                onTradeDynamic = onTradeProvisionsDynamic,
                onDispatchCaravan = onDispatchCaravan,
                onSellSpoils = onSellSpoils,
                onCollectVectigal = onCollectVectigal
            )
        }
    }
}

@Composable
private fun TabulariumBudgetSection(
    resources: LegionResources,
    investments: List<ProvincialInvestment>,
    bankingState: RomanBankingState,
    totalSoldiers: Int
) {
    val seasonalStipend = (resources.senateFavor * 1.5f + 35).toInt()
    val ownedInvestments = investments.filter { it.isOwned }
    val investDenarii = ownedInvestments.sumOf { it.currentYieldDenarii }
    val investProvisions = ownedInvestments.sumOf { it.currentYieldProvisions }
    val investGlory = ownedInvestments.sumOf { it.currentYieldGlory }
    val bankInterest = bankingState.projectedSeasonalInterest
    val totalIncomeDenarii = seasonalStipend + investDenarii + bankInterest

    val foodUpkeep = (totalSoldiers * 0.12f).toInt()
    val loanPayment = bankingState.seasonalLoanPayment
    val netDenarii = totalIncomeDenarii - loanPayment
    val netProvisions = investProvisions - foodUpkeep

    val legionWealthRank = when {
        resources.denarii + bankingState.depositDenarii >= 600 -> "🏛️ Казна консульского масштаба (Богатейший легион Республики)"
        resources.denarii + bankingState.depositDenarii >= 300 -> "💎 Зажиточный легион (Уверенная финансовая независимость)"
        resources.denarii + bankingState.depositDenarii >= 150 -> "⚖️ Устойчивый республиканский бюджет (Регулярные выплаты)"
        else -> "⚠️ Скромный лагерный фонд (Требуются новые военные трофеи и субсидии)"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Wealth Rank Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📊", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Финансовая Квестура (Tabularium Legionis)",
                                color = RomanGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Сводный квартальный баланс доходов и расходов",
                                color = RomanTextGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = RomanBronzeDark)

                Surface(
                    color = Color(0x44000000),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = legionWealthRank,
                        color = RomanGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        // Seasonal Income & Expenses Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "💰 Прогноз сезонного бюджета (В следующий сезон)",
                    color = RomanGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Income items
                Surface(
                    color = Color(0x224CAF50),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5581C784)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📈 Сезонные доходы: +$totalIncomeDenarii 🪙" + (if (investProvisions > 0) ", +$investProvisions 🌾" else ""),
                            color = Color(0xFFA5D6A7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• Жалование Сената (SPQR):", color = RomanTextMuted, fontSize = 11.sp)
                            Text(text = "+$seasonalStipend 🪙", color = Color(0xFFA5D6A7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (investDenarii > 0 || investProvisions > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "• Провинциальные предприятия (${ownedInvestments.size}/6):", color = RomanTextMuted, fontSize = 11.sp)
                                Text(text = "+$investDenarii 🪙, +$investProvisions 🌾" + (if (investGlory > 0) ", +$investGlory 🏆" else ""), color = Color(0xFFA5D6A7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (bankInterest > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "• Проценты по вкладу (+5% Mensa):", color = RomanTextMuted, fontSize = 11.sp)
                                Text(text = "+$bankInterest 🪙", color = Color(0xFFA5D6A7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Expenses items
                Surface(
                    color = Color(0x22F44336),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55E57373)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📉 Сезонные расходы: -$foodUpkeep 🌾" + (if (loanPayment > 0) ", -$loanPayment 🪙" else ""),
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• Продовольственный паек армии ($totalSoldiers чел.):", color = RomanTextMuted, fontSize = 11.sp)
                            Text(text = "-$foodUpkeep 🌾", color = Color(0xFFFFCDD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        if (loanPayment > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "• Взнос по военному займу Сената:", color = RomanTextMuted, fontSize = 11.sp)
                                Text(text = "-$loanPayment 🪙", color = Color(0xFFFFCDD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Net summary
                HorizontalDivider(color = RomanBronzeDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Чистый сезонный прирост:",
                        color = RomanGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = if (netDenarii >= 0) Color(0x334CAF50) else Color(0x33F44336),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = (if (netDenarii >= 0) "+$netDenarii" else "$netDenarii") + " 🪙",
                                color = if (netDenarii >= 0) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = if (netProvisions >= 0) Color(0x334CAF50) else Color(0x33F44336),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = (if (netProvisions >= 0) "+$netProvisions" else "$netProvisions") + " 🌾",
                                color = if (netProvisions >= 0) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProvincialInvestmentsSection(
    resources: LegionResources,
    investments: List<ProvincialInvestment>,
    onUpgradeInvestment: (String) -> Unit
) {
    val totalDen = investments.filter { it.isOwned }.sumOf { it.currentYieldDenarii }
    val totalProv = investments.filter { it.isOwned }.sumOf { it.currentYieldProvisions }
    val totalGlory = investments.filter { it.isOwned }.sumOf { it.currentYieldGlory }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "🏛️ Провинциальные предприятия",
                        color = RomanGoldLight,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Владения легиона в Италии и Средиземноморье",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = Color(0x334CAF50),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Text(
                        text = "+$totalDen 🪙, +$totalProv 🌾 / сезон",
                        color = Color(0xFFA5D6A7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        investments.forEach { inv ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (inv.isOwned) RomanDarkSurfaceCard else Color(0xFF1E1714)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (inv.isOwned) RomanGoldDark else RomanBronzeDark
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = inv.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = inv.titleRu,
                                    color = RomanGoldLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${inv.latinNameRu} • ${inv.regionRu}",
                                    color = RomanTextGold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Surface(
                            color = when (inv.level) {
                                3 -> Color(0x44FFD700)
                                2 -> Color(0x332196F3)
                                1 -> Color(0x334CAF50)
                                else -> Color(0x33757575)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (inv.level == 0) "Не открыто" else "Уровень ${inv.level}/${inv.maxLevel}",
                                color = if (inv.level > 0) RomanGoldLight else RomanTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Yield pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (inv.isOwned) {
                            Surface(
                                color = Color(0x334CAF50),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Доход: +${inv.currentYieldDenarii} 🪙",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (inv.currentYieldProvisions > 0) {
                                Surface(
                                    color = Color(0x3381C784),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+${inv.currentYieldProvisions} 🌾",
                                        color = Color(0xFFC8E6C9),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (inv.currentYieldGlory > 0) {
                                Surface(
                                    color = Color(0x33FFB300),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "+${inv.currentYieldGlory} 🏆 Слава",
                                        color = RomanGoldLight,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0x22FFFFFF),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Базовая отдача: +${inv.seasonalDenarii} 🪙, +${inv.seasonalProvisions} 🌾",
                                    color = RomanTextMuted,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = inv.specialPerkRu,
                        color = RomanTextGold,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Text(
                        text = inv.historyQuoteRu,
                        color = RomanTextMuted,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic
                    )

                    // Upgrade Button
                    if (!inv.isMaxLevel) {
                        val cost = inv.nextUpgradeCost
                        val canAfford = resources.denarii >= cost
                        val buttonText = if (inv.level == 0) {
                            "Приобрести предприятие ($cost 🪙 ➔ +${inv.nextYieldDenarii} 🪙/сезон)"
                        } else {
                            "Расширить до Уровня ${inv.level + 1} ($cost 🪙 ➔ +${inv.nextYieldDenarii} 🪙, +${inv.nextYieldProvisions} 🌾)"
                        }

                        OutlinedButton(
                            onClick = { onUpgradeInvestment(inv.id) },
                            enabled = canAfford,
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (canAfford) Color(0x22FFB300) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (canAfford) RomanGoldLight else RomanBronzeDark
                            )
                        ) {
                            Text(
                                text = buttonText,
                                color = if (canAfford) RomanGoldLight else RomanTextMuted,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0x22FFD700),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ Предприятие развито до наивысшего ранга Республики",
                                color = RomanGoldLight,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RomanBankingSection(
    resources: LegionResources,
    bankingState: RomanBankingState,
    campLevel: Int,
    onDepositBank: (Int) -> Unit,
    onWithdrawBank: (Int) -> Unit,
    onTakeWarLoan: (Int) -> Unit,
    onRepayWarLoan: () -> Unit,
    onMintCoins: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Mensa Nummaria Deposit Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏦", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Банковская касса (Mensa Nummaria)",
                                color = RomanGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Депозиты под +5% сезонного прироста",
                                color = RomanTextGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0x334CAF50),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Text(
                            text = "Вклад: ${bankingState.depositDenarii} 🪙",
                            color = Color(0xFFA5D6A7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Римские банкиры и аргентарии начисляют +5% на сумму вклада каждый сезон. Прогноз на след. сезон: +${bankingState.projectedSeasonalInterest} 🪙. Всего заработано: +${bankingState.totalInterestEarned} 🪙.",
                    color = RomanTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                // Deposit Action Buttons
                Text(text = "Внести денарии во вклад:", color = RomanGoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val canDep25 = resources.denarii >= 25
                    OutlinedButton(
                        onClick = { onDepositBank(25) },
                        enabled = canDep25,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canDep25) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+25 🪙", color = if (canDep25) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val canDep50 = resources.denarii >= 50
                    OutlinedButton(
                        onClick = { onDepositBank(50) },
                        enabled = canDep50,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canDep50) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+50 🪙", color = if (canDep50) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val canDep100 = resources.denarii >= 100
                    OutlinedButton(
                        onClick = { onDepositBank(100) },
                        enabled = canDep100,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canDep100) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+100 🪙", color = if (canDep100) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Withdraw Buttons
                if (bankingState.depositDenarii > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onWithdrawBank(50) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                        ) {
                            Text(text = "Снять 50 🪙", color = RomanGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onWithdrawBank(bankingState.depositDenarii) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
                        ) {
                            Text(text = "Снять все (${bankingState.depositDenarii} 🪙)", color = RomanGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Senate War Loans
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📜", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Военный заем Сената (Mutuum Bellicum)",
                            color = RomanGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Чрезвычайное государственное финансирование",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                if (bankingState.hasActiveLoan) {
                    Surface(
                        color = Color(0x33D84315),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "⚠️ Активный заем: ${bankingState.activeLoanDenarii} 🪙 (Осталось сезонов: ${bankingState.loanSeasonsRemaining})",
                                color = Color(0xFFFFAB91),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Сезонный вычет: по ${bankingState.seasonalLoanPayment} 🪙 в сезон.",
                                color = RomanTextMuted,
                                fontSize = 11.sp
                            )

                            val canRepayFull = resources.denarii >= bankingState.activeLoanDenarii
                            OutlinedButton(
                                onClick = onRepayWarLoan,
                                enabled = canRepayFull,
                                modifier = Modifier.fillMaxWidth().height(38.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (canRepayFull) Color(0xFF81C784) else RomanBronzeDark)
                            ) {
                                Text(
                                    text = "Досрочно погасить (${bankingState.activeLoanDenarii} 🪙 ➔ +10% Favor, +2 Слава)",
                                    color = if (canRepayFull) Color(0xFFA5D6A7) else RomanTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "В случае нехватки средств для найма и вооружения когорт легион может запросить беспроцентный военный заем Сената на 4 сезона.",
                        color = RomanTextMuted,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val canLoan = resources.senateFavor >= 25
                        OutlinedButton(
                            onClick = { onTakeWarLoan(100) },
                            enabled = canLoan,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (canLoan) RomanGoldLight else RomanBronzeDark)
                        ) {
                            Text(text = "Заем 100 🪙 (по 25 🪙/сез)", color = if (canLoan) RomanGoldLight else RomanTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onTakeWarLoan(250) },
                            enabled = canLoan,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (canLoan) RomanGoldLight else RomanBronzeDark)
                        ) {
                            Text(text = "Заем 250 🪙 (по 62 🪙/сез)", color = if (canLoan) RomanGoldLight else RomanTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Coin Minting (Officina Monetae)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Чеканка легионной монеты (Emissio Nummi)",
                            color = RomanGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Выпуск серебряных денариев с профилем легиона",
                            color = RomanTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                val canMint = resources.provisions >= 45 && campLevel >= 4
                OutlinedButton(
                    onClick = onMintCoins,
                    enabled = canMint,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33FFD700)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (canMint) RomanGold else RomanBronzeDark)
                ) {
                    Text(
                        text = if (campLevel < 4) "Требуется уровень лагеря 4+" else "Отчеканить партию монеты (Расход 45 🌾 ➔ +90 🪙 + 2 Слава)",
                        color = if (canMint) RomanGoldLight else RomanTextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ForumMarketSection(
    resources: LegionResources,
    marketState: MarketState,
    onTradeDynamic: (Int, Int, Boolean) -> Unit,
    onDispatchCaravan: () -> Unit,
    onSellSpoils: () -> Unit,
    onCollectVectigal: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Market Conditions Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = marketState.marketTrendIcon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Бычий Форум (Forum Boarium)",
                                color = RomanGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = marketState.marketConditionTitleRu,
                                color = RomanTextGold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = marketState.marketConditionDescRu,
                    color = RomanTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Grain Market Trading
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🌾 Оптовые сделки с провиантом",
                    color = RomanGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Buying Grain
                Text(text = "Закупка зерна в Хорреум:", color = Color(0xFFA5D6A7), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val cost20 = (16 * marketState.priceModifier).toInt()
                    val canBuy20 = resources.denarii >= cost20
                    OutlinedButton(
                        onClick = { onTradeDynamic(20, -cost20, true) },
                        enabled = canBuy20,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canBuy20) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+20 🌾 (-$cost20 🪙)", color = if (canBuy20) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val cost50 = (38 * marketState.priceModifier).toInt()
                    val canBuy50 = resources.denarii >= cost50
                    OutlinedButton(
                        onClick = { onTradeDynamic(50, -cost50, true) },
                        enabled = canBuy50,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canBuy50) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+50 🌾 (-$cost50 🪙)", color = if (canBuy50) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val cost100 = (72 * marketState.priceModifier).toInt()
                    val canBuy100 = resources.denarii >= cost100
                    OutlinedButton(
                        onClick = { onTradeDynamic(100, -cost100, true) },
                        enabled = canBuy100,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x224CAF50)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canBuy100) Color(0xFF81C784) else RomanBronzeDark)
                    ) {
                        Text(text = "+100 🌾 (-$cost100 🪙)", color = if (canBuy100) Color(0xFFA5D6A7) else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Selling Grain
                Text(text = "Продажа излишков хлеба на Форуме:", color = RomanGoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val gain20 = (14 * marketState.priceModifier).toInt()
                    val canSell20 = resources.provisions >= 20
                    OutlinedButton(
                        onClick = { onTradeDynamic(-20, gain20, false) },
                        enabled = canSell20,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canSell20) RomanGoldLight else RomanBronzeDark)
                    ) {
                        Text(text = "-20 🌾 (+$gain20 🪙)", color = if (canSell20) RomanGoldLight else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val gain50 = (36 * marketState.priceModifier).toInt()
                    val canSell50 = resources.provisions >= 50
                    OutlinedButton(
                        onClick = { onTradeDynamic(-50, gain50, false) },
                        enabled = canSell50,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canSell50) RomanGoldLight else RomanBronzeDark)
                    ) {
                        Text(text = "-50 🌾 (+$gain50 🪙)", color = if (canSell50) RomanGoldLight else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }

                    val gain100 = (74 * marketState.priceModifier).toInt()
                    val canSell100 = resources.provisions >= 100
                    OutlinedButton(
                        onClick = { onTradeDynamic(-100, gain100, false) },
                        enabled = canSell100,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FFB300)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (canSell100) RomanGoldLight else RomanBronzeDark)
                    ) {
                        Text(text = "-100 🌾 (+$gain100 🪙)", color = if (canSell100) RomanGoldLight else RomanTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Special Trade Caravans & Spoils & Taxes
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = RomanDarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚢 Торговые караваны и сбыт военных трофеев",
                    color = RomanGoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                val canSendCaravan = resources.denarii >= 40
                OutlinedButton(
                    onClick = onDispatchCaravan,
                    enabled = canSendCaravan,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x3300897B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (canSendCaravan) Color(0xFF4DB6AC) else RomanBronzeDark)
                ) {
                    Text(
                        text = "🚢 Снарядить караван в Остию (40 🪙 ➔ ~75 🪙 + 20 🌾 + 1 Слава)",
                        color = if (canSendCaravan) Color(0xFF80CBC4) else RomanTextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val canSellSpoils = resources.provisions >= 20
                OutlinedButton(
                    onClick = onSellSpoils,
                    enabled = canSellSpoils,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33FFA000)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (canSellSpoils) Color(0xFFFFD54F) else RomanBronzeDark)
                ) {
                    Text(
                        text = "🏺 Сбыт военных трофеев и амфор (Обоз: 20 🌾 ➔ +48 🪙)",
                        color = if (canSellSpoils) Color(0xFFFFE082) else RomanTextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val canCollectTribute = resources.senateFavor >= 15
                OutlinedButton(
                    onClick = onCollectVectigal,
                    enabled = canCollectTribute,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33D84315)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (canCollectTribute) Color(0xFFFF8A65) else RomanBronzeDark)
                ) {
                    Text(
                        text = "📜 Чрезвычайный военный налог (Vectigal: +55 🪙, -6% Favor)",
                        color = if (canCollectTribute) Color(0xFFFFAB91) else RomanTextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

