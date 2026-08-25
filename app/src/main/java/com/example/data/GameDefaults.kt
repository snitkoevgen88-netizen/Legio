package com.example.data

import com.example.model.*

object GameDefaults {

    fun createInitialCommanders(): List<Commander> = listOf(
        Commander(
            id = "cmd_marcus_fabius",
            name = "Марк Фабий",
            level = 2,
            xp = 35,
            maxXp = 100,
            rankTitle = "Центурион",
            trait = CommanderTrait.BRAVE,
            avatarSkinTone = 0,
            hairStyle = 1,
            helmetType = 1, // Red Crest
            beardStyle = 1, // Stubble
            cloakColorIndex = 0, // Crimson
            expeditionsLed = 3,
            victoriesCount = 2,
            greatVictoriesCount = 1,
            defeatsCount = 0,
            moodStatus = "Рвётся в бой за Рим!"
        ),
        Commander(
            id = "cmd_gaius_cornelius",
            name = "Гай Корнелий",
            level = 1,
            xp = 15,
            maxXp = 100,
            rankTitle = "Центурион",
            trait = CommanderTrait.CAUTIOUS,
            avatarSkinTone = 1,
            hairStyle = 0,
            helmetType = 0, // Standard Galea
            beardStyle = 2, // Full beard
            cloakColorIndex = 2, // Bronze
            expeditionsLed = 2,
            victoriesCount = 2,
            greatVictoriesCount = 0,
            defeatsCount = 0,
            moodStatus = "Изучает карты Самния"
        ),
        Commander(
            id = "cmd_sextus_julius",
            name = "Секст Юлий",
            level = 1,
            xp = 0,
            maxXp = 100,
            rankTitle = "Центурион",
            trait = CommanderTrait.TACTICIAN,
            avatarSkinTone = 0,
            hairStyle = 2,
            helmetType = 2, // Laurel
            beardStyle = 0, // Clean shaven
            cloakColorIndex = 1, // Purple
            expeditionsLed = 0,
            victoriesCount = 0,
            greatVictoriesCount = 0,
            defeatsCount = 0,
            moodStatus = "Точит гладиус перед строем"
        )
    )

    fun createInitialCohorts(): List<Cohort> = listOf(
        Cohort(
            id = "coh_legio4_ferrata",
            name = "IV Cohors «Ferrata»",
            level = 2,
            xp = 40,
            maxXp = 100,
            soldiers = 80,
            maxSoldiers = 80,
            veteransCount = 8,
            morale = 95,
            attackPower = 20,
            defensePower = 18,
            discipline = 24,
            expeditionsCount = 3,
            victoriesCount = 3,
            greatVictoriesCount = 1,
            defeatsCount = 0,
            assignedCommanderId = "cmd_marcus_fabius",
            traditions = listOf("Железная дисциплина", "Первый удар")
        ),
        Cohort(
            id = "coh_legio4_victrix",
            name = "IV Cohors «Victrix»",
            level = 1,
            xp = 20,
            maxXp = 100,
            soldiers = 80,
            maxSoldiers = 80,
            veteransCount = 4,
            morale = 85,
            attackPower = 17,
            defensePower = 16,
            discipline = 18,
            expeditionsCount = 2,
            victoriesCount = 2,
            greatVictoriesCount = 0,
            defeatsCount = 0,
            assignedCommanderId = "cmd_gaius_cornelius",
            traditions = listOf("Стена скутумов")
        ),
        Cohort(
            id = "coh_legio4_triarii",
            name = "IV Cohors «Triarii»",
            level = 1,
            xp = 0,
            maxXp = 100,
            soldiers = 75,
            maxSoldiers = 80,
            veteransCount = 12,
            morale = 90,
            attackPower = 19,
            defensePower = 22,
            discipline = 30,
            expeditionsCount = 1,
            victoriesCount = 1,
            greatVictoriesCount = 0,
            defeatsCount = 0,
            assignedCommanderId = null,
            traditions = listOf("Последний рубеж")
        )
    )

    fun createInitialBuildings(): List<Building> = listOf(
        Building(type = BuildingType.PRINCIPIA, level = 1),
        Building(type = BuildingType.CAMPUS_MARTIUS, level = 1),
        Building(type = BuildingType.SPECULA, level = 1),
        Building(type = BuildingType.HORREUM, level = 1),
        Building(type = BuildingType.FABRICA, level = 1),
        Building(type = BuildingType.VALETUDINARIUM, level = 1),
        Building(type = BuildingType.AQUILA_SHRINE, level = 1)
    )

    fun createInitialCompetingLegions(): List<CompetingLegion> = listOf(
        CompetingLegion(
            id = "legio_1",
            name = "Legio I «Martia»",
            ratingScore = 88,
            victories = 14,
            defeats = 2,
            currentActivityRu = "Штурмует крепость этрусков",
            badgeSymbol = "🦅"
        ),
        CompetingLegion(
            id = "legio_3",
            name = "Legio III «Gallica»",
            ratingScore = 82,
            victories = 12,
            defeats = 3,
            currentActivityRu = "Охраняет долину реки По",
            badgeSymbol = "⚔️"
        ),
        CompetingLegion(
            id = "legio_4_player",
            name = "Legio IV «Invicta» (Ваш легион)",
            ratingScore = 65,
            victories = 3,
            defeats = 0,
            currentActivityRu = "Разбивает лагерь в Кампании",
            badgeSymbol = "⭐"
        ),
        CompetingLegion(
            id = "legio_2",
            name = "Legio II «Sabina»",
            ratingScore = 58,
            victories = 8,
            defeats = 4,
            currentActivityRu = "Пополняет новобранцев в Риме",
            badgeSymbol = "🛡️"
        ),
        CompetingLegion(
            id = "legio_5",
            name = "Legio V «Urbana»",
            ratingScore = 49,
            victories = 6,
            defeats = 5,
            currentActivityRu = "Патрулирует Аппиеву дорогу",
            badgeSymbol = "🏛️"
        )
    )

    fun getAllExpeditions(): List<Expedition> = listOf(
        Expedition(
            id = "exp_samnite_scout",
            titleRu = "Разведка ущелий Самния",
            historicalContextRu = "Самнитские горцы тревожат союзные Риму кампанские города. Необходимо выбить засады из теснин.",
            regionRu = "Самний (Апеннины)",
            difficulty = 1,
            denariiCost = 40,
            provisionsCost = 30,
            rewardDenarii = 95,
            rewardProvisions = 60,
            rewardGlory = 4,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Умеренная (отряды застрельщиков)",
                dangerLevelRu = "Низкая",
                enemyTacticRu = "Засады на горных склонах",
                recommendedTactic = Tactics.CAUTIOUS,
                intelClarity = 2
            ),
            minCampLevel = 1
        ),
        Expedition(
            id = "exp_etruscan_raid",
            titleRu = "Осада этрусского форпоста",
            historicalContextRu = "Этрусские наёмники укрепились на высотах близ Вейев. Требуется решительный штурм вала.",
            regionRu = "Этрурия",
            difficulty = 2,
            denariiCost = 65,
            provisionsCost = 45,
            rewardDenarii = 150,
            rewardProvisions = 90,
            rewardGlory = 7,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Средняя (гоплиты и лучники)",
                dangerLevelRu = "Умеренная",
                enemyTacticRu = "Оборонительная фаланга за палисадом",
                recommendedTactic = Tactics.BALANCED,
                intelClarity = 2
            ),
            minCampLevel = 1
        ),
        Expedition(
            id = "exp_volsci_pass",
            titleRu = "Битва у Вольских холмов",
            historicalContextRu = "Вольски собрали ополчение для набега на Лаций. Римские консулы требуют остановить их в полевом бою.",
            regionRu = "Земли Вольсков",
            difficulty = 2,
            denariiCost = 75,
            provisionsCost = 50,
            rewardDenarii = 175,
            rewardProvisions = 110,
            rewardGlory = 8,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Высокая (многочисленная пехота)",
                dangerLevelRu = "Повышенная",
                enemyTacticRu = "Лобовой натиск толпой",
                recommendedTactic = Tactics.TESTUDO,
                intelClarity = 2
            ),
            minCampLevel = 2
        ),
        Expedition(
            id = "exp_caudine_revenge",
            titleRu = "Возмездие за Кавдинское ущелье",
            historicalContextRu = "Самниты выставили отборную тяжелую пехоту с копьями. Легиону предстоит восстановить честь Рима.",
            regionRu = "Кавдийские горы",
            difficulty = 3,
            denariiCost = 110,
            provisionsCost = 75,
            rewardDenarii = 240,
            rewardProvisions = 150,
            rewardGlory = 14,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Очень высокая (Самнитская фаланга)",
                dangerLevelRu = "Высокая (риск тяжелых потерь)",
                enemyTacticRu = "Смыкание щитов в узком дефиле",
                recommendedTactic = Tactics.FLANK_AMBUSH,
                intelClarity = 2
            ),
            minCampLevel = 2
        ),
        Expedition(
            id = "exp_gaul_incursion",
            titleRu = "Отражение галльских сенонов",
            historicalContextRu = "Галльские боевые колесницы и яростные воины с двуручными мечами прорвали границу по реке Аллии.",
            regionRu = "Долина реки Аллия",
            difficulty = 4,
            denariiCost = 140,
            provisionsCost = 90,
            rewardDenarii = 320,
            rewardProvisions = 180,
            rewardGlory = 20,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Грозная (свирепые берсерки и конница)",
                dangerLevelRu = "Критическая",
                enemyTacticRu = "Сокрушительный чардж и вой",
                recommendedTactic = Tactics.TESTUDO,
                intelClarity = 3
            ),
            minCampLevel = 3
        ),
        Expedition(
            id = "exp_senate_pyrrhic_trial",
            titleRu = "⚡ Испытание Сената: Битва с Пирром Эпирским",
            historicalContextRu = "Чрезвычайный приказ Сената: царь Пирр выставил боевых слонов и македонскую фалангу при Гераклее! Чрезвычайный риск и колоссальная слава!",
            regionRu = "Лукания / Тарент",
            difficulty = 5,
            denariiCost = 180,
            provisionsCost = 130,
            rewardDenarii = 500,
            rewardProvisions = 300,
            rewardGlory = 35,
            scoutIntel = ScoutIntel(
                estimatedEnemyStrengthRu = "Легендарная (Эпирская фаланга + Боевые слоны)",
                dangerLevelRu = "СМЕРТЕЛЬНАЯ (Возможен разгром)",
                enemyTacticRu = "Атака слонов при поддержке сариссофоров",
                recommendedTactic = Tactics.AGGRESSIVE,
                intelClarity = 3
            ),
            isSenateTrial = true,
            minCampLevel = 3
        )
    )

    fun createInitialAchievements(): List<Achievement> = listOf(
        Achievement(
            id = "ach_first_victory",
            titleRu = "Первый триумф",
            descRu = "Одержите свою первую победу в кампании.",
            icon = "⚔️",
            bonusPerkRu = "+10% к боевому духу новобранцев",
            isUnlocked = true
        ),
        Achievement(
            id = "ach_great_victory",
            titleRu = "Великая победа",
            descRu = "Завоюйте Великую победу (Триумф) без значительных потерь.",
            icon = "🏆",
            bonusPerkRu = "+1 к базовой славе за все последующие победы",
            isUnlocked = false
        ),
        Achievement(
            id = "ach_ten_victories",
            titleRu = "Десять побед Рима",
            descRu = "Одержите 10 побед в составе легиона.",
            icon = "🦅",
            bonusPerkRu = "Снижение стоимости жалования на 15%",
            isUnlocked = false
        ),
        Achievement(
            id = "ach_veteran_wall",
            titleRu = "Железные ветераны",
            descRu = "Воспитайте когорту с более чем 20 ветеранами.",
            icon = "🛡️",
            bonusPerkRu = "+3 к защите всех отрядов в строю",
            isUnlocked = false
        ),
        Achievement(
            id = "ach_best_legion",
            titleRu = "Лучший легион Республики",
            descRu = "Займите 1-е место в рейтинге легионов Сената!",
            icon = "👑",
            bonusPerkRu = "Постоянный статус Legio Invicta и уважение Сената",
            isUnlocked = false
        ),
        Achievement(
            id = "ach_pyrrhic_conqueror",
            titleRu = "Победитель Эпира",
            descRu = "Выполните Чрезвычайный приказ Сената против царя Пирра.",
            icon = "🐘",
            bonusPerkRu = "+50 к славе и постоянная традиция «Сокрушители слонов»",
            isUnlocked = false
        ),
        Achievement(
            id = "ach_grand_camp",
            titleRu = "Великая цитадель",
            descRu = "Улучшите все здания лагеря до максимального ранга.",
            icon = "🏛️",
            bonusPerkRu = "+25% к добыче золота и притоку провизии",
            isUnlocked = false
        )
    )

    fun getRandomEvents(): List<CampEvent> = listOf(
        CampEvent(
            id = "evt_senate_envoy",
            titleRu = "Посол Римского Сената",
            descRu = "В лагерь прибыл патриций из Сената с инспекцией боеготовности. Он предлагает дополнительное финансирование в обмен на клятву верности консулам.",
            icon = "📜",
            choices = listOf(
                CampEventChoice(
                    textRu = "Принять субсидию и присягнуть консулам",
                    effectDescRu = "+80 Денариев, +15 Уважения Сената, -5 к Морали солдат (не любят политиков).",
                    denariiDelta = 80,
                    senateFavorDelta = 15,
                    moraleDelta = -5,
                    resultLogRu = "Сенат выделил мешки с золотом. Командиры устроили пир в претории."
                ),
                CampEventChoice(
                    textRu = "Заявить о независимости чести легиона",
                    effectDescRu = "+10 к Славе, +15 к Морали солдат, -10 Уважения Сената.",
                    gloryDelta = 10,
                    moraleDelta = 15,
                    senateFavorDelta = -10,
                    resultLogRu = "Легионеры встретили ваши слова ликованием и ударами мечей о скутумы!"
                )
            )
        ),
        CampEvent(
            id = "evt_veteran_smith",
            titleRu = "Италийский оружейник",
            descRu = "Странствующий кампанский кузнец предлагает секрет закалки иберийских гладиусов.",
            icon = "🔨",
            choices = listOf(
                CampEventChoice(
                    textRu = "Нанять мастера в кузницу (за 50 монет)",
                    effectDescRu = "-50 Денариев, +40 Опыта всем когортам, повышение атаки.",
                    denariiDelta = -50,
                    cohortXpDelta = 40,
                    resultLogRu = "Мечи легионеров сверкают смертоносной остротой!"
                ),
                CampEventChoice(
                    textRu = "Попросить солдат учиться самим",
                    effectDescRu = "+10 Опыта, экономия казны.",
                    cohortXpDelta = 10,
                    resultLogRu = "Легионеры тренируются на деревянных столбах."
                )
            )
        ),
        CampEvent(
            id = "evt_grain_merchant",
            titleRu = "Караван сицилийского зерна",
            descRu = "Купеческий обоз предлагает партию отборной пшеницы по выгодной цене.",
            icon = "🌾",
            choices = listOf(
                CampEventChoice(
                    textRu = "Закупить зерно для складов (за 40 монет)",
                    effectDescRu = "-40 Денариев, +80 Провизии.",
                    denariiDelta = -40,
                    provisionsDelta = 80,
                    resultLogRu = "Зернохранилища лагеря забиты отборным зерном!"
                ),
                CampEventChoice(
                    textRu = "Ограничиться местной охотой и сбором",
                    effectDescRu = "+20 Провизии бесплатно.",
                    provisionsDelta = 20,
                    resultLogRu = "Разведчики пригнали стадо коз из предгорий."
                )
            )
        ),
        CampEvent(
            id = "evt_omen_eagle",
            titleRu = "Священное знамение Марса",
            descRu = "Орёл опустился прямо на навершие штандарта IV Легиона во время утренней переклички. Авгуры ликуют!",
            icon = "🦅",
            choices = listOf(
                CampEventChoice(
                    textRu = "Принести жертву Марсу Мстителю (30 провизии)",
                    effectDescRu = "-30 Провизии, +8 Славы, +20 Морали всех отрядов.",
                    provisionsDelta = -30,
                    gloryDelta = 8,
                    moraleDelta = 20,
                    resultLogRu = "Дым от алтаря вознесся прямо в небеса. Легион непобедим!"
                ),
                CampEventChoice(
                    textRu = "Продолжить подготовку без лишних трат",
                    effectDescRu = "+3 Славы, солдаты воодушевлены.",
                    gloryDelta = 3,
                    moraleDelta = 5,
                    resultLogRu = "Орёл взмыл ввысь, огласив лагерь победным криком."
                )
            )
        )
    )

    fun createInitialDoctrines(): List<MilitaryDoctrine> = listOf(
        MilitaryDoctrine(
            id = "doc_disciplina",
            titleRu = "Железная Дисциплина",
            latinNameRu = "Disciplina Ferrea",
            icon = "🛡️",
            costGlory = 25,
            descRu = "Безупречная выучка строя и подчинение командам трибунов.",
            effectRu = "+3 к защите всех когорт, боевые потери снижены на 20%.",
            isUnlocked = false,
            requiredBuildingLevel = 1
        ),
        MilitaryDoctrine(
            id = "doc_pila_barrage",
            titleRu = "Залп Пилумами",
            latinNameRu = "Volley of Pila",
            icon = "🏹",
            costGlory = 35,
            descRu = "Синхронный сокрушительный бросок тяжелых дротиков перед ударом гладиусов.",
            effectRu = "+4 к атаке когорт, +15% к шансу Великой победы.",
            isUnlocked = false,
            requiredBuildingLevel = 1
        ),
        MilitaryDoctrine(
            id = "doc_medici",
            titleRu = "Полевые Хирурги",
            latinNameRu = "Medici Castrorum",
            icon = "🏥",
            costGlory = 30,
            descRu = "Организация санитарной службы и полевых перевязочных пунктов.",
            effectRu = "Лазарет спасает на 50% больше раненых, ветераны защищены от гибели.",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_testudo",
            titleRu = "Мастерство Черепахи",
            latinNameRu = "Testudo Formatio",
            icon = "🐢",
            costGlory = 40,
            descRu = "Идеальное смыкание скутумов со всех сторон и сверху.",
            effectRu = "Тактика «Черепаха» полностью исключает риск Разгрома (0% катастрофы).",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_siege_art",
            titleRu = "Осадная Инженерия",
            latinNameRu = "Ars Tormentorum",
            icon = "🎯",
            costGlory = 50,
            descRu = "Строительство баллист, скорпионов и штурмовых лестниц.",
            effectRu = "+35% к военной добыче в крепостях и горных цитаделях.",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_auxilia",
            titleRu = "Союзная Кавалерия",
            latinNameRu = "Auxilia Equestris",
            icon = "🐎",
            costGlory = 60,
            descRu = "Интеграция критских стрелков и нумидийской конницы во фланги легиона.",
            effectRu = "+30% к шансу разгрома врагов при тактике «Охват с флангов».",
            isUnlocked = false,
            requiredBuildingLevel = 3
        ),
        MilitaryDoctrine(
            id = "doc_imperium",
            titleRu = "Империй Триумфатора",
            latinNameRu = "Imperium Triumphale",
            icon = "👑",
            costGlory = 80,
            descRu = "Право на триумфальное шествие по Священной дороге Рима.",
            effectRu = "+30 Денариев от Сената каждый сезон, +30% к приросту Славы.",
            isUnlocked = false,
            requiredBuildingLevel = 3
        )
    )

    fun createInitialEquipment(): List<EquipmentItem> = listOf(
        EquipmentItem(
            id = "eq_galea_montefortino",
            nameRu = "Шлем Монтефортино",
            type = EquipmentType.HELMET,
            defenseBonus = 2,
            casualtyReductionPct = 10,
            costDenarii = 60,
            forgeRequirementLevel = 1,
            descRu = "Бронзовый литой шлем с нащечниками для защиты центурий.",
            isCrafted = true,
            equippedCohortId = "coh_legio4_ferrata"
        ),
        EquipmentItem(
            id = "eq_gladius_hispaniensis",
            nameRu = "Гладиус Испанский",
            type = EquipmentType.WEAPON,
            attackBonus = 3,
            costDenarii = 75,
            forgeRequirementLevel = 1,
            descRu = "Обоюдоострый колющий меч из отборного закаленного железа.",
            isCrafted = true,
            equippedCohortId = "coh_legio4_ferrata"
        ),
        EquipmentItem(
            id = "eq_lorica_hamata",
            nameRu = "Кольчуга Лорика Хамата",
            type = EquipmentType.ARMOR,
            defenseBonus = 4,
            casualtyReductionPct = 20,
            costDenarii = 120,
            forgeRequirementLevel = 2,
            descRu = "Плетеный железный доспех с наплечниками, снижающий потери в рубке.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_heavy_pilum",
            nameRu = "Утяжеленный Пилум",
            type = EquipmentType.WEAPON,
            attackBonus = 5,
            costDenarii = 90,
            forgeRequirementLevel = 2,
            descRu = "Дротик с мягким стержнем, сминающим щиты вражеской фаланги.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_aquila_standard",
            nameRu = "Золотой Орел Юпитера",
            type = EquipmentType.STANDARD,
            attackBonus = 2,
            defenseBonus = 2,
            moraleBonus = 25,
            costDenarii = 200,
            forgeRequirementLevel = 3,
            descRu = "Освященный штандарт легиона, вдохновляющий воинов стоять насмерть.",
            isCrafted = false
        )
    )

    fun createInitialSenateQuests(): List<SenateQuest> = listOf(
        SenateQuest(
            id = "sq_appian_way",
            titleRu = "Безопасность Аппиевой дороги",
            issuerRu = "Цензор Аппий Клавдий",
            descriptionRu = "Очистите южные тракты от разбойников и самнитских банд (совершите 2 победы).",
            rewardDenarii = 140,
            rewardSenateFavor = 15,
            rewardGlory = 12,
            targetType = "VICTORIES",
            targetCount = 2,
            currentProgress = 1
        ),
        SenateQuest(
            id = "sq_granary_reserves",
            titleRu = "Зерновой резерв Республики",
            issuerRu = "Эдил Квинт Сервилий",
            descriptionRu = "Улучшите лагерный Хорреум (Horreum) до II уровня для надежного снабжения легиона.",
            rewardDenarii = 100,
            rewardSenateFavor = 20,
            rewardGlory = 10,
            targetType = "BUILDING_LEVEL",
            targetCount = 2,
            currentProgress = 1
        ),
        SenateQuest(
            id = "sq_legion_veterans",
            titleRu = "Закалка ветеранов Рима",
            issuerRu = "Консул Фабий Максим Руллан",
            descriptionRu = "Добейтесь, чтобы численность легиона достигла 200 воинов при высокой морали.",
            rewardDenarii = 180,
            rewardSenateFavor = 25,
            rewardGlory = 20,
            targetType = "LEGION_SIZE",
            targetCount = 200,
            currentProgress = 160
        )
    )

    fun createInitialChronicle(): List<ChronicleEntry> = listOf(
        ChronicleEntry(
            id = "chr_0",
            seasonFormatted = "🌱 Весна, 315 г. до н.э.",
            yearBc = 315,
            headlineRu = "Основание каструма Legio IV в Кампании",
            textRu = "По указу Сената и римского народа Legio IV основал укрепленный лагерь. Центурионы Марк Фабий и Гай Корнелий выставили первые дозоры.",
            outcome = ExpeditionOutcome.VICTORY,
            commanderName = "Марк Фабий",
            cohortName = "IV Cohors «Ferrata»",
            casualties = 0,
            lootDenarii = 100,
            lootProvisions = 80,
            gloryEarned = 10,
            traditionUnlocked = "Основатели Лагеря"
        )
    )
}
