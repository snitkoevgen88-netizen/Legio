package com.example.model

enum class Season(val titleRu: String, val icon: String, val effectDescRu: String) {
    SPRING("Весна", "🌱", "Сбор новобранцев. Снижена стоимость тренировок на 20%."),
    SUMMER("Лето", "☀️", "Сезон походов! +15% к шансу победы в дальних экспедициях."),
    AUTUMN("Осень", "🍂", "Сбор урожая! +30% к запасам провизии при завершении сезона."),
    WINTER("Зима", "❄️", "Зимние квартиры. Армия отдыхает в лагере, снижен риск потерь.")
}

data class SeasonYear(
    val seasonIndex: Int = 0, // 0..3 -> Spring, Summer, Autumn, Winter
    val seasonNumber: Int = 1, // Total seasons passed
    val yearBc: Int = 315 // 315 BC down to 270 BC
) {
    val season: Season get() = Season.entries[seasonIndex % Season.entries.size]
    val formatted: String get() = "${season.icon} ${season.titleRu}, $yearBc г. до н.э. (Сезон $seasonNumber)"
}

data class LegionResources(
    val denarii: Int = 240,
    val provisions: Int = 160,
    val glory: Int = 20,
    val senateFavor: Int = 50 // 0..100%
) {
    val isGoldenAge: Boolean get() = glory >= 150 && senateFavor >= 75
}

enum class CampRank(val titleRu: String, val levelReq: Int, val perkRu: String) {
    FIELD_BIVOUAC("Полевой бивак", 1, "Базовый лагерь с деревянным частоколом."),
    FORTIFIED_OUTPOST("Укрепленный форпост", 3, "+10% вместимости складов, дозорные вышки."),
    CASTRA_LEGIONIS("Легионный каструм", 6, "+20% к защите, открыты полевой лазарет и кузница."),
    GRAND_CITADEL("Великая цитадель", 10, "+35% ко всем ресурсам, легендарный статус лагеря.")
}

enum class RepublicRank(val titleRu: String, val minGlory: Int, val descriptionRu: String) {
    PROVINCIAL("Провинциальный легион", 0, "Малоизвестная часть на рубежах Лация."),
    RECOGNIZED("Признанный легион", 40, "Сенат доверяет ответственные походы в Самний."),
    RENOWNED("Прославленный легион", 90, "Ветераны Рима слагают песни о ваших победах."),
    INVICTA("Непобедимый легион (Legio Invicta)", 160, "Гордость Рима, гроза всех врагов Республики!")
}

enum class CommanderTrait(
    val titleRu: String,
    val icon: String,
    val attackBonus: Int,
    val defenseBonus: Int,
    val victoryBonusChance: Int,
    val disasterRiskChange: Int,
    val descriptionRu: String,
    val quoteRu: String
) {
    BRAVE("Храбрый", "🦁", attackBonus = 3, defenseBonus = -1, victoryBonusChance = 15, disasterRiskChange = 5,
        "Высокий урон и шанс Великой победы, но повышен риск потерь.",
        "«Победа достаётся тем, кто не оглядывается назад!»"),
    CAUTIOUS("Осторожный", "🛡️", attackBonus = -1, defenseBonus = 4, victoryBonusChance = -10, disasterRiskChange = -15,
        "Максимальная безопасность строя, минимизирует потери и риск разгрома.",
        "«Живой легионер принесёт Риму больше побед, чем мёртвый герой.»"),
    AMBITIOUS("Амбициозный", "⚡", attackBonus = 2, defenseBonus = 0, victoryBonusChance = 20, disasterRiskChange = 0,
        "+30% к наградам при победе, но болезненно переживает неудачи.",
        "«Рим должен услышать моё имя на Форуме!»"),
    DISCIPLINED("Дисциплинированный", "📐", attackBonus = 1, defenseBonus = 2, victoryBonusChance = 5, disasterRiskChange = -10,
        "Стабильность и непоколебимость строя в любых испытаниях.",
        "«Порядок в рядах побеждает ярость варваров.»"),
    GREEDY("Жадный", "💰", attackBonus = 0, defenseBonus = 0, victoryBonusChance = 0, disasterRiskChange = 5,
        "Находит на 40% больше золота и трофеев в походах, но требует жалование.",
        "«Золото врага принадлежит тем, кто держит гладиус.»"),
    LOYAL("Верный", "🦅", attackBonus = 1, defenseBonus = 1, victoryBonusChance = 5, disasterRiskChange = -5,
        "Иммунитет к интригам Сената, поднимает мораль соратников.",
        "«За Сенат и Народ Рима — до последнего вздоха!»"),
    TACTICIAN("Тактик", "📜", attackBonus = 2, defenseBonus = 2, victoryBonusChance = 12, disasterRiskChange = -8,
        "Эффективно использует разведданные и контр-тактики.",
        "«Любая битва выигрывается на карте до первого удара.»");

    val descRu: String get() = descriptionRu
}

data class Commander(
    val id: String,
    val name: String,
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val rankTitle: String = "Центурион", // Центурион -> Военный трибун -> Легат
    val trait: CommanderTrait = CommanderTrait.BRAVE,
    val avatarSkinTone: Int = 0, // 0..3
    val hairStyle: Int = 0, // 0..3
    val helmetType: Int = 0, // 0 = standard, 1 = crest, 2 = golden wreath, 3 = winged
    val beardStyle: Int = 0, // 0 = clean, 1 = stubble, 2 = full beard
    val cloakColorIndex: Int = 0, // 0 = crimson, 1 = purple, 2 = bronze gold, 3 = iron
    val expeditionsLed: Int = 0,
    val victoriesCount: Int = 0,
    val greatVictoriesCount: Int = 0,
    val defeatsCount: Int = 0,
    val isAlive: Boolean = true,
    val moodStatus: String = "Готов к бою"
) {
    val rankIndex: Int get() = when {
        level >= 7 -> 3 // Легат
        level >= 4 -> 2 // Военный трибун
        else -> 1 // Центурион
    }
    val rankName: String get() = when (rankIndex) {
        3 -> "Легат легиона"
        2 -> "Военный трибун"
        else -> "Центурион"
    }
    val rankLabel: String get() = rankName
}

data class Cohort(
    val id: String,
    val name: String, // e.g. "I Cohors «Ferrata»"
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 100,
    val soldiers: Int = 80,
    val maxSoldiers: Int = 80,
    val veteransCount: Int = 6,
    val morale: Int = 90, // 0..100
    val attackPower: Int = 18,
    val defensePower: Int = 16,
    val discipline: Int = 20,
    val expeditionsCount: Int = 0,
    val victoriesCount: Int = 0,
    val greatVictoriesCount: Int = 0,
    val defeatsCount: Int = 0,
    val casualtiesSuffered: Int = 0,
    val assignedCommanderId: String? = null,
    val traditions: List<String> = listOf("Железная дисциплина")
) {
    val isElite: Boolean get() = level >= 5 || veteransCount >= 25
    val rankLabel: String get() = when {
        level >= 8 -> "Непобедимая центурия"
        level >= 5 -> "Закаленные ветераны"
        level >= 3 -> "Опытные манипулы"
        else -> "Новобранцы Республики"
    }
}

enum class BuildingType(
    val titleRu: String,
    val icon: String,
    val roleDescRu: String
) {
    PRINCIPIA("Штаб легиона (Principia)", "🏛️", "Командный центр. Открывает новые тактики и дипломатию Сената."),
    CAMPUS_MARTIUS("Тренировочный плац (Campus Martius)", "⚔️", "Увеличивает прирост опыта отрядов и открывает продвинутые учения."),
    SPECULA("Башня разведки (Specula)", "👁️", "Дает точные разведданные перед экспедициями, снижает риск засад."),
    HORREUM("Склады и Зернохранилище (Horreum)", "🌾", "Увеличивает запасы провизии и выживаемость армии в походах."),
    FABRICA("Оружейная мастерская (Fabrica)", "⚒️", "Ковка доспехов, гладиусов и скутумов. Повышает атаку и снижает потери."),
    VALETUDINARIUM("Полевой лазарет (Valetudinarium)", "🏥", "Спасает раненых легионеров и сохраняет ценных ветеранов."),
    AQUILA_SHRINE("Святилище Орла (Aedes Signorum)", "🦅", "Штандарт легиона. Повышает боевой дух и славу от побед."),
    CASTRA_EQUITUM("Конюшни эквитов (Castra Equitum)", "🐎", "Разведение скакунов и тренировка турм конницы. Усиливает фланговые удары."),
    TABULARIUM("Канцелярия и Архив (Tabularium)", "📜", "Учет трофеев и логистика. Снижает расходы армии и увеличивает субсидии Сената."),
    THERMAE_LEGIONIS("Лагерные термы (Thermae)", "🛁", "Римские бани для легионеров. Восстанавливают силы и поднимают мораль каждого сезона."),
    BALLISTARIUM("Парк осадных машин (Ballistarium)", "🎯", "Сборка баллист, скорпионов и онагров для штурма вражеских укреплений.")
}

data class Building(
    val type: BuildingType,
    val level: Int = 1, // 1..3
    val maxLevel: Int = 3
) {
    val upgradeCostDenarii: Int get() = when (level) {
        1 -> 90
        2 -> 180
        else -> 320
    }
    val upgradeCostProvisions: Int get() = when (level) {
        1 -> 50
        2 -> 100
        else -> 180
    }
    val currentPerkRu: String get() = when (type) {
        BuildingType.PRINCIPIA -> when (level) {
            1 -> "I: Базовое планирование походов."
            2 -> "II: Тактические маневры «Черепаха» и «Фланг»."
            else -> "III: Чрезвычайные поручения Сената и триумфы."
        }
        BuildingType.CAMPUS_MARTIUS -> when (level) {
            1 -> "I: Базовые строевые тренировки (+25 XP)."
            2 -> "II: Манипулярные учения ветеранов (+50 XP)."
            else -> "III: Элитная школа гладиаторов и тактиков (+90 XP)."
        }
        BuildingType.SPECULA -> when (level) {
            1 -> "I: Базовая оценка сил противника."
            2 -> "II: Раскрытие тактики врага и уровня опасности."
            else -> "III: Полная тактическая разведка и предупреждение о засадах."
        }
        BuildingType.HORREUM -> when (level) {
            1 -> "I: Вместимость +100 провизии."
            2 -> "II: Вместимость +250 провизии, защита от порчи зерна."
            else -> "III: Вместимость +500 провизии, +25% к урожаю осенью."
        }
        BuildingType.FABRICA -> when (level) {
            1 -> "I: Ковка стандартных пилумов и шлемов (+2 к атаке)."
            2 -> "II: Бронзовые кольчуги лорика хамата и заточка мечей (-15% потерь, +4 к атаке)."
            else -> "III: Закаленная испанская сталь и доспехи центурионов (+8 к атаке, -30% потерь)."
        }
        BuildingType.VALETUDINARIUM -> when (level) {
            1 -> "I: Возврат 15% раненых солдат."
            2 -> "II: Возврат 35% раненых солдат, сохранение ветеранов."
            else -> "III: Скорая полевая хирургия: спасение до 60% раненых!"
        }
        BuildingType.AQUILA_SHRINE -> when (level) {
            1 -> "I: +1 к славе за победы."
            2 -> "II: +3 к славе, устойчивость морали к потерям."
            else -> "III: Знамя Invicta: +5 к славе, шанс Золотого Века!"
        }
        BuildingType.CASTRA_EQUITUM -> when (level) {
            1 -> "I: +2 к атаке союзных конных турм."
            2 -> "II: +5 к атаке кавалерии, -10% к риску вражеских засад."
            else -> "III: Элитная конница патрициев: двойные трофеи при охвате с флангов!"
        }
        BuildingType.TABULARIUM -> when (level) {
            1 -> "I: Точный учет казны (+10 денариев в сезон)."
            2 -> "II: -15% к расходу провизии армии, +15% к субсидиям Сената."
            else -> "III: Полный архив Республики: +30 денариев за каждое выполненное поручение."
        }
        BuildingType.THERMAE_LEGIONIS -> when (level) {
            1 -> "I: +5 к морали всех когорт каждый сезон."
            2 -> "II: +10 к морали, снижение усталости ветеранов."
            else -> "III: Великие лагерные термы: постоянный иммунитет к падению боевого духа ниже 70%!"
        }
        BuildingType.BALLISTARIUM -> when (level) {
            1 -> "I: Расчет баллист: +3 к штурмовой мощи."
            2 -> "II: Скорпионы и катапульты: +7 к атаке против крепостей."
            else -> "III: Парк тяжелых онагров: гарантированное разрушение любых укреплений противника!"
        }
    }
}

enum class Tactics(
    val titleRu: String,
    val icon: String,
    val attackMod: Int,
    val defenseMod: Int,
    val greatVictoryBonusPct: Int,
    val disasterRiskPct: Int,
    val descRu: String
) {
    AGGRESSIVE(
        "Агрессивный штурм", "⚔️", attackMod = 4, defenseMod = -2,
        greatVictoryBonusPct = 25, disasterRiskPct = 8,
        "Бросок пилумов и яростная атака гладиусами. Выше шанс триумфа, но выше риск потерь."
    ),
    CAUTIOUS(
        "Осторожный маневр", "🛡️", attackMod = -2, defenseMod = 5,
        greatVictoryBonusPct = -15, disasterRiskPct = -12,
        "Сомкнутый строй со скутумами. Надежная защита, минимизация риска разгрома."
    ),
    BALANCED(
        "Сбалансированная манипула", "⚖️", attackMod = 1, defenseMod = 1,
        greatVictoryBonusPct = 5, disasterRiskPct = 0,
        "Классическое республиканское построение. Надежный баланс во всех ситуациях."
    ),
    TESTUDO(
        "Стена щитов (Черепаха)", "🐢", attackMod = 0, defenseMod = 6,
        greatVictoryBonusPct = -5, disasterRiskPct = -15,
        "Непробиваемая защита от стрелков и кавалерии. Идеальна против превосходящих сил."
    ),
    FLANK_AMBUSH(
        "Охват с флангов", "🐎", attackMod = 5, defenseMod = -1,
        greatVictoryBonusPct = 30, disasterRiskPct = 6,
        "Рискованный удар во фланг вражеского строя. Сокрушителен при правильной разведке."
    )
}

data class ScoutIntel(
    val estimatedEnemyStrengthRu: String, // "Примерно средняя", "Чрезвычайно высокая"
    val dangerLevelRu: String, // "Умеренная", "Высокая", "Смертельная"
    val enemyTacticRu: String, // "Оборонительная фаланга", "Конная засада", "Яростный навал"
    val recommendedTactic: Tactics = Tactics.BALANCED,
    val intelClarity: Int = 1 // 1 = туманная, 2 = частичная, 3 = точная
)

data class Expedition(
    val id: String,
    val titleRu: String,
    val historicalContextRu: String,
    val regionRu: String,
    val difficulty: Int, // 1..5
    val denariiCost: Int,
    val provisionsCost: Int,
    val rewardDenarii: Int,
    val rewardProvisions: Int,
    val rewardGlory: Int,
    val scoutIntel: ScoutIntel,
    val isSenateTrial: Boolean = false, // Чрезвычайное Испытание Славы от Сената
    val minCampLevel: Int = 1
)

enum class ExpeditionOutcome(
    val titleRu: String,
    val icon: String,
    val gloryDelta: Int,
    val moralImpact: Int,
    val isSuccess: Boolean
) {
    GREAT_VICTORY("Великая победа (Триумф!)", "🏆", gloryDelta = 12, moralImpact = 25, isSuccess = true),
    VICTORY("Победа", "⚔️", gloryDelta = 5, moralImpact = 10, isSuccess = true),
    PARTIAL_SUCCESS("Частичный успех", "⚖️", gloryDelta = 1, moralImpact = 0, isSuccess = true),
    DEFEAT("Поражение", "🚩", gloryDelta = -4, moralImpact = -15, isSuccess = false),
    DISASTER("Катастрофа (Разгром)", "💀", gloryDelta = -10, moralImpact = -35, isSuccess = false)
}

data class BattleOddsPreview(
    val greatVictoryPct: Int,
    val victoryPct: Int,
    val partialPct: Int,
    val defeatPct: Int,
    val disasterPct: Int,
    val adviceRu: String
)

data class CompetingLegion(
    val id: String,
    val name: String,
    val ratingScore: Int,
    val victories: Int,
    val defeats: Int,
    val currentActivityRu: String,
    val badgeSymbol: String
)

data class ChronicleEntry(
    val id: String,
    val seasonFormatted: String,
    val yearBc: Int,
    val headlineRu: String,
    val textRu: String,
    val outcome: ExpeditionOutcome?,
    val commanderName: String,
    val cohortName: String,
    val casualties: Int,
    val lootDenarii: Int,
    val lootProvisions: Int,
    val gloryEarned: Int,
    val traditionUnlocked: String? = null
)

data class Achievement(
    val id: String,
    val titleRu: String,
    val descRu: String,
    val icon: String,
    val bonusPerkRu: String,
    val isUnlocked: Boolean = false
)

data class CampEventChoice(
    val textRu: String,
    val effectDescRu: String,
    val denariiDelta: Int = 0,
    val provisionsDelta: Int = 0,
    val gloryDelta: Int = 0,
    val senateFavorDelta: Int = 0,
    val moraleDelta: Int = 0,
    val cohortXpDelta: Int = 0,
    val resultLogRu: String
)

data class CampEvent(
    val id: String,
    val titleRu: String,
    val descRu: String,
    val icon: String,
    val choices: List<CampEventChoice>
)

data class SeasonalPlan(
    val trainCohortId: String? = null,
    val upgradeBuildingType: BuildingType? = null,
    val launchedExpeditionId: String? = null,
    val selectedCommanderId: String? = null,
    val selectedCohortId: String? = null,
    val selectedTactics: Tactics = Tactics.BALANCED
) {
    fun hasAnyAction(): Boolean =
        trainCohortId != null || upgradeBuildingType != null || launchedExpeditionId != null
}

data class MilitaryDoctrine(
    val id: String,
    val titleRu: String,
    val latinNameRu: String,
    val icon: String,
    val costGlory: Int,
    val descRu: String,
    val effectRu: String,
    val isUnlocked: Boolean = false,
    val requiredBuildingLevel: Int = 1
)

enum class EquipmentType(val titleRu: String, val icon: String) {
    HELMET("Шлем", "🪖"),
    ARMOR("Доспех", "🛡️"),
    WEAPON("Оружие", "🗡️"),
    SHIELD("Скутум (Щит)", "🛡️"),
    STANDARD("Штандарт (Орёл)", "🦅"),
    ACCESSORY("Амуниция / Поножи", "🦿")
}

enum class EquipmentMaterial(val titleRu: String, val colorHex: Long, val badge: String) {
    BRONZE("Бронза Лация", 0xFFCD7F32, "🥉"),
    IRON("Кованое железо", 0xFF9E9E9E, "⚔️"),
    SPANISH_STEEL("Испанская сталь", 0xFF42A5F5, "💎"),
    IMPERIAL_GOLD("Золоченая бронза", 0xFFFFD700, "👑")
}

data class EquipmentItem(
    val id: String,
    val nameRu: String,
    val latinName: String = "",
    val type: EquipmentType,
    val material: EquipmentMaterial = EquipmentMaterial.IRON,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val moraleBonus: Int = 0,
    val casualtyReductionPct: Int = 0,
    val costDenarii: Int = 80,
    val forgeRequirementLevel: Int = 1,
    val descRu: String,
    val isCrafted: Boolean = false,
    val temperLevel: Int = 0, // 0..3 (Заточка / Усиление в кузнице)
    val equippedCohortId: String? = null
) {
    val totalAttackBonus: Int get() = attackBonus + (temperLevel * 2)
    val totalDefenseBonus: Int get() = defenseBonus + (temperLevel * 2)
    val totalCostDenarii: Int get() = costDenarii
    val temperCostDenarii: Int get() = (costDenarii * 0.45f).toInt() + (temperLevel * 25)
    val salvageDenarii: Int get() = (costDenarii * 0.5f).toInt()
}

enum class SenateFaction(
    val titleRu: String,
    val leaderRu: String,
    val icon: String,
    val agendaRu: String,
    val bonusDescRu: String
) {
    OPTIMATES(
        titleRu = "Оптиматы (Патриции)",
        leaderRu = "Консул Квинт Фабий",
        icon = "🏛️",
        agendaRu = "Сохранение традиций Рима, слава аристократических родов и строгая дисциплина.",
        bonusDescRu = "+15% к приросту Славы за Великие победы и +10 к дисциплине ветеранов."
    ),
    POPULARES(
        titleRu = "Популяры (Народные трибуны)",
        leaderRu = "Трибун Тиберий Деций",
        icon = "📜",
        agendaRu = "Защита прав рядовых легионеров, наделение землей и щедрые донативы.",
        bonusDescRu = "Снижает стоимость пополнения когорт на 25% и поднимает базовую мораль новобранцев."
    ),
    BELLICOSI(
        titleRu = "Партия Войны (Милитаристы)",
        leaderRu = "Легат Аппий Клавдий",
        icon = "⚔️",
        agendaRu = "Беспощадное сокрушение врагов Республики, покорение Самния и Этрурии.",
        bonusDescRu = "+20% к военным трофеям и золоту за победные экспедиции."
    ),
    PONTIFICES(
        titleRu = "Коллегия Понтификов",
        leaderRu = "Великий Понтифик Луций",
        icon = "🕊️",
        agendaRu = "Почитание бессмертных богов, священные авгурии и ритуалы очищения.",
        bonusDescRu = "Увеличивает длительность божественных благословений и снижает риск катастроф."
    )
}

data class SenatePetition(
    val id: String,
    val titleRu: String,
    val latinNameRu: String,
    val icon: String,
    val descriptionRu: String,
    val favorCost: Int,
    val denariiCost: Int = 0,
    val minFavorRequired: Int,
    val rewardSummaryRu: String
)

enum class QuestCategory(val titleRu: String, val icon: String) {
    ALL("Все", "📜"),
    SENATE_CAMPAIGN("Кампании", "🏛️"),
    SEASONAL_MANDATE("Боевые приказы", "⚔️"),
    LOGISTICS_FABRICA("Снабжение & Стройка", "🏗️"),
    DIVINE_VOW("Священные обеты", "🕊️")
}

enum class QuestPriority(val titleRu: String, val badge: String, val colorHex: Long) {
    STANDARD("Эдикт", "📜", 0xFFCD7F32),
    URGENT("Важный приказ", "⚡", 0xFFFF9800),
    SENATUS_CONSULTUM_ULTIMUM("Чрезвычайный декрет SPQR", "👑", 0xFFD32F2F)
}

data class SenateQuest(
    val id: String,
    val titleRu: String,
    val issuerRu: String, // "Консул Фабий Максим", "Цензор Аппий Клавдий"
    val descriptionRu: String,
    val rewardDenarii: Int,
    val rewardSenateFavor: Int,
    val rewardGlory: Int,
    val targetType: String, // "VICTORIES", "GREAT_VICTORIES", "BUILDING_LEVEL", "LEGION_SIZE", "VETERANS_COUNT", "FORGED_EQUIPMENT", "DOCTRINES_LEARNED", "PROVISIONS_RESERVE", "DENARII_TREASURY", "ACTIVE_BLESSING", "UNIT_TRAINING", "WIN_STREAK", "EXPEDITION_WIN", "ALL_BUILDINGS_UPGRADED", "OFFICERS_RECRUITED", "CARAVAN_DISPATCHED"
    val targetCount: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val category: QuestCategory = QuestCategory.SENATE_CAMPAIGN,
    val priority: QuestPriority = QuestPriority.STANDARD,
    val faction: SenateFaction = SenateFaction.OPTIMATES,
    val icon: String = "📜",
    val flavorHistoryRu: String = "",
    val bonusPerkDescRu: String? = null,
    val actionHintRu: String? = null,
    val targetScreenHint: String? = null, // "EXPEDITIONS", "BUILDINGS", "TRAINING", "ARMORY", "ALTAR", "TREASURY"
    val deadlineSeasonsRemaining: Int? = null
) {
    val isFinished: Boolean get() = currentProgress >= targetCount
    val progressRatio: Float get() = if (targetCount > 0) (currentProgress.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) else 0f
    val progressPercent: Int get() = (progressRatio * 100).toInt()
}

data class ExpeditionResult(
    val expedition: Expedition,
    val commander: Commander,
    val cohort: Cohort,
    val tactics: Tactics,
    val outcome: ExpeditionOutcome,
    val casualties: Int,
    val veteransSaved: Int,
    val woundedTreated: Int,
    val lootDenarii: Int,
    val lootProvisions: Int,
    val gloryDelta: Int,
    val xpEarned: Int,
    val commanderPromoted: Boolean,
    val newTradition: String?,
    val commanderKilled: Boolean = false,
    val storyNarrativeRu: String
)

enum class GodType(
    val titleRu: String,
    val titleLatin: String,
    val icon: String,
    val domainRu: String,
    val quoteRu: String
) {
    MARS("Марс Грозный", "Mars Gradivus", "⚔️", "Бог войны и доблести", "«Mars ultor legionem ducit!» — Марс Мститель ведет легион!"),
    JUPITER("Юпитер Величайший", "Iuppiter Optimus Maximus", "⚡", "Верховный бог неба и защитник Сената", "«По воле Юпитера Капитолийского Рим правит народами.»"),
    CERES("Церера Благодатная", "Ceres Alma", "🌾", "Богиня плодородия и урожая", "«Земля Италии насытит защитников Республики.»"),
    MINERVA("Минерва Стратегическая", "Minerva Bellica", "🦉", "Богиня военной мудрости и ремёсел", "«Холодный разум полководца острее любого меча.»"),
    FORTUNA("Фортуна Воинская", "Fortuna Victrix", "🎲", "Богиня удачи и спасения в бою", "«Audaces Fortuna iuvat — Храбрым судьба помогает!»")
}

data class DivineRitual(
    val id: String,
    val god: GodType,
    val nameRu: String,
    val descriptionRu: String,
    val blessingEffectRu: String,
    val costDenarii: Int = 0,
    val costProvisions: Int = 0
)

data class ActiveBlessing(
    val god: GodType,
    val ritualNameRu: String,
    val effectRu: String,
    val seasonsRemaining: Int = 1
)

data class LegionTrophy(
    val id: String,
    val titleRu: String,
    val originRu: String,
    val icon: String,
    val descriptionRu: String,
    val passivePerkRu: String,
    val isUnlocked: Boolean = false,
    val unlockConditionRu: String
)

enum class DrillIntensity(
    val titleRu: String,
    val latinNameRu: String,
    val costMultiplier: Float,
    val timeSeconds: Int,
    val xpGain: Int,
    val veteranChancePct: Int,
    val descRu: String
) {
    STANDARD("Строевая муштра", "Militia Ordinaria", 1.0f, 6, 20, 10, "Базовые строевые упражнения и обращение с пилумом."),
    INTENSIVE("Усиленные маневры", "Exercitus Impetus", 1.4f, 10, 45, 30, "Манипулярные перестроения и марш-броски с полной выкладкой."),
    GLADIATOR("Гладиаторская закалка", "Schola Bellica", 2.0f, 15, 80, 65, "Элитные фехтовальные поединки и спартанская стойкость под градом стрел.")
}

enum class UnitType(
    val id: String,
    val nameRu: String,
    val latinName: String,
    val icon: String,
    val lineRoleRu: String,
    val baseCostDenarii: Int,
    val baseCostProvisions: Int,
    val attackBonus: Int,
    val defenseBonus: Int,
    val disciplineBonus: Int,
    val specialPerkRu: String,
    val historyQuoteRu: String,
    val descriptionRu: String
) {
    VELITES(
        id = "unit_velites",
        nameRu = "Велиты",
        latinName = "Velites",
        icon = "🏹",
        lineRoleRu = "Легкая пехота и застрельщики",
        baseCostDenarii = 8,
        baseCostProvisions = 6,
        attackBonus = 3,
        defenseBonus = 1,
        disciplineBonus = 2,
        specialPerkRu = "«Охотники за слонами»: Рассеивают застрельщиков врага и снижают риск засады.",
        historyQuoteRu = "«Быстрые как волки, метают дротики и отступают за строй щитов.»",
        descriptionRu = "Молодые легковооруженные метатели дротиков в волчьих шкурах. Завязывают бой и прикрывают развертывание основных манипул."
    ),
    HASTATI(
        id = "unit_hastati",
        nameRu = "Гастаты",
        latinName = "Hastati",
        icon = "🗡️",
        lineRoleRu = "Первая линия тяжелой пехоты",
        baseCostDenarii = 14,
        baseCostProvisions = 10,
        attackBonus = 5,
        defenseBonus = 3,
        disciplineBonus = 4,
        specialPerkRu = "«Залп тяжелых пилумов»: Пробивает щиты и сокрушает первый натиск врага.",
        historyQuoteRu = "«Первыми встречают ярость самнитов и галлов, держа стену скутумов.»",
        descriptionRu = "Юноши первой линии манипулярного легиона. Вооружены двумя пилумами, гладиусом и большим щитом скутум."
    ),
    PRINCIPES(
        id = "unit_principes",
        nameRu = "Принципы",
        latinName = "Principes",
        icon = "🛡️",
        lineRoleRu = "Вторая линия (Главная ударная сила)",
        baseCostDenarii = 22,
        baseCostProvisions = 16,
        attackBonus = 7,
        defenseBonus = 6,
        disciplineBonus = 6,
        specialPerkRu = "«Железный клин»: Мощная контратака манипул в кольчугах лорика хамата.",
        historyQuoteRu = "«Сердце легиона в расцвете сил. Их поступь невозможно сломить.»",
        descriptionRu = "Опытные воины второй линии в полных кольчугах. Вступают в сражение, когда необходимо решительно сломить сопротивление противника."
    ),
    TRIARII(
        id = "unit_triarii",
        nameRu = "Триарии",
        latinName = "Triarii",
        icon = "🔱",
        lineRoleRu = "Третья линия (Несокрушимый резерв ветеранов)",
        baseCostDenarii = 36,
        baseCostProvisions = 24,
        attackBonus = 9,
        defenseBonus = 10,
        disciplineBonus = 10,
        specialPerkRu = "«Res ad Triarios venit»: Полная защита от разгрома и несокрушимая стойкость.",
        historyQuoteRu = "«Дело дошло до триариев! Последний оплот и гордость Сената Рима.»",
        descriptionRu = "Старейшие и самые прославленные ветераны с длинными копьями-гастами. Преклоняют колено в резерве и вступают в бой в решающий миг."
    ),
    EQUITES(
        id = "unit_equites",
        nameRu = "Эквиты",
        latinName = "Equites",
        icon = "🐎",
        lineRoleRu = "Римская конница (Фланги и преследование)",
        baseCostDenarii = 32,
        baseCostProvisions = 20,
        attackBonus = 8,
        defenseBonus = 4,
        disciplineBonus = 5,
        specialPerkRu = "«Фланговый охват»: Удваивает трофеи и преследует отступающих врагов.",
        historyQuoteRu = "«Всадники из знатнейших патрицианских родов Рима на горячих скакунах.»",
        descriptionRu = "Кавалерийские турмы Республики. Обеспечивают мобильность, разведку и удары во фланг и тыл неприятеля."
    ),
    FUNDITORES(
        id = "unit_funditores",
        nameRu = "Балеарские пращники",
        latinName = "Funditores",
        icon = "🪨",
        lineRoleRu = "Специализированные стрелки (Ауксилии)",
        baseCostDenarii = 18,
        baseCostProvisions = 12,
        attackBonus = 6,
        defenseBonus = 2,
        disciplineBonus = 3,
        specialPerkRu = "«Свинцовые пули»: Наносят сокрушительный урон плотным фалангам врага.",
        historyQuoteRu = "«С расстояния в сотни шагов их пращи бьют точнее стрел.»",
        descriptionRu = "Искуснейшие союзные стрелки. Забрасывают ряды врагов тяжелыми свинцовыми ядрами, сея панику и пробивая броню."
    )
}

data class UnitTrainingAllocation(
    val unitType: UnitType,
    val allocatedCount: Int = 10,
    val drillIntensity: DrillIntensity = DrillIntensity.STANDARD,
    val targetCohortId: String = "cohort_1",
    val isTrainingActive: Boolean = false,
    val currentProgress: Float = 0f, // 0.0f .. 1.0f
    val secondsRemaining: Int = 0,
    val totalSeconds: Int = 0,
    val totalTrainedSoFar: Int = 0
) {
    val totalCostDenarii: Int
        get() = (unitType.baseCostDenarii * allocatedCount * drillIntensity.costMultiplier).toInt()

    val totalCostProvisions: Int
        get() = (unitType.baseCostProvisions * allocatedCount * drillIntensity.costMultiplier).toInt()

    val projectedAttackGain: Int
        get() = ((unitType.attackBonus * allocatedCount) / 10).coerceAtLeast(1)

    val projectedDefenseGain: Int
        get() = ((unitType.defenseBonus * allocatedCount) / 10).coerceAtLeast(1)

    val projectedDisciplineGain: Int
        get() = ((unitType.disciplineBonus * allocatedCount) / 15).coerceAtLeast(1)
}

enum class EconomySubTab(val titleRu: String, val icon: String) {
    TABULARIUM("Квестура & Баланс", "📊"),
    INVESTMENTS("Инвестиции в провинциях", "🏛️"),
    BANKING("Банк & Займы", "🏦"),
    MARKET("Рынок & Торговля", "🌾")
}

data class ProvincialInvestment(
    val id: String,
    val titleRu: String,
    val latinNameRu: String,
    val regionRu: String,
    val icon: String,
    val level: Int = 0, // 0 = not owned, 1..3 = tiers
    val maxLevel: Int = 3,
    val baseCostDenarii: Int,
    val seasonalDenarii: Int,
    val seasonalProvisions: Int = 0,
    val seasonalGlory: Int = 0,
    val specialPerkRu: String,
    val historyQuoteRu: String,
    val descriptionRu: String
) {
    val isOwned: Boolean get() = level > 0
    val isMaxLevel: Boolean get() = level >= maxLevel

    val nextUpgradeCost: Int get() = when (level) {
        0 -> baseCostDenarii
        1 -> (baseCostDenarii * 1.55f).toInt()
        2 -> (baseCostDenarii * 2.35f).toInt()
        else -> 0
    }

    val currentYieldDenarii: Int get() = when (level) {
        1 -> seasonalDenarii
        2 -> (seasonalDenarii * 1.85f).toInt()
        3 -> (seasonalDenarii * 2.9f).toInt()
        else -> 0
    }

    val currentYieldProvisions: Int get() = when (level) {
        1 -> seasonalProvisions
        2 -> (seasonalProvisions * 1.85f).toInt()
        3 -> (seasonalProvisions * 2.9f).toInt()
        else -> 0
    }

    val currentYieldGlory: Int get() = when (level) {
        1 -> seasonalGlory
        2 -> seasonalGlory + (if (seasonalGlory > 0) 1 else 0)
        3 -> seasonalGlory + 2
        else -> 0
    }

    val nextYieldDenarii: Int get() = when (level + 1) {
        1 -> seasonalDenarii
        2 -> (seasonalDenarii * 1.85f).toInt()
        3 -> (seasonalDenarii * 2.9f).toInt()
        else -> currentYieldDenarii
    }

    val nextYieldProvisions: Int get() = when (level + 1) {
        1 -> seasonalProvisions
        2 -> (seasonalProvisions * 1.85f).toInt()
        3 -> (seasonalProvisions * 2.9f).toInt()
        else -> currentYieldProvisions
    }
}

data class RomanBankingState(
    val depositDenarii: Int = 0,
    val activeLoanDenarii: Int = 0,
    val loanSeasonsRemaining: Int = 0,
    val totalInterestEarned: Int = 0,
    val totalCoinsMinted: Int = 0
) {
    val hasActiveLoan: Boolean get() = activeLoanDenarii > 0
    val seasonalLoanPayment: Int get() = if (loanSeasonsRemaining > 0) activeLoanDenarii / kotlin.math.max(1, loanSeasonsRemaining) else 0
    val projectedSeasonalInterest: Int get() = (depositDenarii * 0.05f).toInt()
}

data class MarketState(
    val grainPriceBuy: Int = 30, // for 40 grain
    val grainPriceSell: Int = 22, // for 30 grain
    val marketConditionTitleRu: String = "Стабильный рынок провианта",
    val marketConditionDescRu: String = "Торговые пути Лация и Кампании открыты, поставки пшеницы регулярны.",
    val marketTrendIcon: String = "⚖️",
    val priceModifier: Float = 1.0f // 0.7f .. 1.4f
)


