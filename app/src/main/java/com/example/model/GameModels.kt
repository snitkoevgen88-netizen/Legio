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
    PRINCIPIA("Штаб легиона", "🏛️", "Командный центр. Открывает новые тактики и дипломатию Сената."),
    CAMPUS_MARTIUS("Тренировочный плац", "⚔️", "Увеличивает прирост опыта отрядов и открывает продвинутые учения."),
    SPECULA("Башня разведки", "👁️", "Дает точные разведданные перед экспедициями, снижает риск засад."),
    HORREUM("Склады и Зернохранилище", "🌾", "Увеличивает запасы провизии и выживаемость армии в походах."),
    FABRICA("Походная кузница", "🔨", "Ковка доспехов и гладиусов. Повышает атаку и снижает потери."),
    VALETUDINARIUM("Полевой лазарет", "🏥", "Спасает раненых легионеров и сохраняет ценных ветеранов."),
    AQUILA_SHRINE("Святилище Орла", "🦅", "Штандарт легиона. Повышает боевой дух и славу от побед.")
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
            1 -> "I: Ковка стандартных пилумов (+2 к атаке)."
            2 -> "II: Бронзовые кольчуги лорика хамата (-15% потерь)."
            else -> "III: Закаленный гладиус и шлемы монтефортино (+5 к атаке, -30% потерь)."
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
    STANDARD("Штандарт", "🦅")
}

data class EquipmentItem(
    val id: String,
    val nameRu: String,
    val type: EquipmentType,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val moraleBonus: Int = 0,
    val casualtyReductionPct: Int = 0,
    val costDenarii: Int = 80,
    val forgeRequirementLevel: Int = 1,
    val descRu: String,
    val isCrafted: Boolean = false,
    val equippedCohortId: String? = null
)

data class SenateQuest(
    val id: String,
    val titleRu: String,
    val issuerRu: String, // "Консул Фабий Максим", "Цензор Аппий Клавдий"
    val descriptionRu: String,
    val rewardDenarii: Int,
    val rewardSenateFavor: Int,
    val rewardGlory: Int,
    val targetType: String, // "VICTORIES", "BUILDING_LEVEL", "LEGION_SIZE", "EXPEDITION"
    val targetCount: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val deadlineSeasonsRemaining: Int? = null
) {
    val isFinished: Boolean get() = currentProgress >= targetCount
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

