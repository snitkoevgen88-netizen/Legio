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
        Building(type = BuildingType.AQUILA_SHRINE, level = 1),
        Building(type = BuildingType.CASTRA_EQUITUM, level = 1),
        Building(type = BuildingType.TABULARIUM, level = 1),
        Building(type = BuildingType.THERMAE_LEGIONIS, level = 1),
        Building(type = BuildingType.BALLISTARIUM, level = 1)
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
            descRu = "Безупречная выучка строя и подчинение командам центурионов.",
            effectRu = "+3 к защите всех когорт, боевые потери снижены на 20%.",
            isUnlocked = false,
            requiredBuildingLevel = 1
        ),
        MilitaryDoctrine(
            id = "doc_pila_barrage",
            titleRu = "Залп Пилумами",
            latinNameRu = "Volatus Pilorum",
            icon = "🏹",
            costGlory = 35,
            descRu = "Синхронный сокрушительный бросок тяжелых дротиков перед ударом гладиусов.",
            effectRu = "+4 к атаке когорт, +15% к шансу Великой победы.",
            isUnlocked = false,
            requiredBuildingLevel = 1
        ),
        MilitaryDoctrine(
            id = "doc_gladius_mastery",
            titleRu = "Школа Гладиуса",
            latinNameRu = "Gladii Usus",
            icon = "🗡️",
            costGlory = 30,
            descRu = "Колющие удары в сочленения доспехов и щитовой бой на ближней дистанции.",
            effectRu = "+5 к атаке пехоты и +15% шанс критического прорыва строя врага.",
            isUnlocked = false,
            requiredBuildingLevel = 1
        ),
        MilitaryDoctrine(
            id = "doc_triplex_acies",
            titleRu = "Строй Триплекс Ациес",
            latinNameRu = "Triplex Acies",
            icon = "📐",
            costGlory = 45,
            descRu = "Шахматное построение в три линии (Гастаты, Принципы, Триарии).",
            effectRu = "+4 к атаке, +4 к защите, снижает усталость когорт в длительных битвах.",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_testudo",
            titleRu = "Мастерство Черепахи",
            latinNameRu = "Testudo Formatio",
            icon = "🐢",
            costGlory = 40,
            descRu = "Идеальное смыкание скутумов со всех сторон и сверху от стрел и копий.",
            effectRu = "Тактика «Черепаха» полностью исключает риск Разгрома (0% катастрофы).",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_medici",
            titleRu = "Полевые Хирурги",
            latinNameRu = "Medici Castrorum",
            icon = "🏥",
            costGlory = 35,
            descRu = "Организация санитарной службы и полевых перевязочных пунктов.",
            effectRu = "Лазарет спасает на 50% больше раненых, ветераны защищены от гибели.",
            isUnlocked = false,
            requiredBuildingLevel = 2
        ),
        MilitaryDoctrine(
            id = "doc_castra_munita",
            titleRu = "Фортификация Каструма",
            latinNameRu = "Castra Munita",
            icon = "🧱",
            costGlory = 40,
            descRu = "Возведение вала, частокола и рва вокруг любого временного бивуака.",
            effectRu = "+8 к защите лагеря при событиях и полное отражение ночных набегов.",
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
            costGlory = 55,
            descRu = "Интеграция критских стрелков и кампанской конницы во фланги легиона.",
            effectRu = "+30% к шансу разгрома врагов при тактике «Охват с флангов».",
            isUnlocked = false,
            requiredBuildingLevel = 3
        ),
        MilitaryDoctrine(
            id = "doc_cursus_belli",
            titleRu = "Форсированный Марш",
            latinNameRu = "Cursus Bellicus",
            icon = "⚡",
            costGlory = 50,
            descRu = "Марш-броски по римским дорогам с полной выкладкой до 30 миль в день.",
            effectRu = "Снижает расход провизии на экспедиции на 25% и повышает инициативу в бою.",
            isUnlocked = false,
            requiredBuildingLevel = 3
        ),
        MilitaryDoctrine(
            id = "doc_corona_civica",
            titleRu = "Гражданская Корона",
            latinNameRu = "Corona Civica",
            icon = "🌿",
            costGlory = 65,
            descRu = "Высшая награда из дубовых листьев за спасение жизни римского гражданина.",
            effectRu = "Сохраняет 60% опыта когорт даже при поражениях и ускоряет закалку ветеранов.",
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
            effectRu = "+30 Денариев от Сената каждый сезон, +30% к приросту Славы за все деяния.",
            isUnlocked = false,
            requiredBuildingLevel = 3
        )
    )

    fun createInitialEquipment(): List<EquipmentItem> = listOf(
        // HELMETS
        EquipmentItem(
            id = "eq_galea_montefortino",
            nameRu = "Шлем Монтефортино",
            latinName = "Galea Montefortino",
            type = EquipmentType.HELMET,
            material = EquipmentMaterial.BRONZE,
            defenseBonus = 2,
            casualtyReductionPct = 10,
            costDenarii = 55,
            forgeRequirementLevel = 1,
            descRu = "Бронзовый куполообразный шлем с нащечниками и шишкой для плюмажа.",
            isCrafted = true,
            equippedCohortId = "coh_legio4_ferrata"
        ),
        EquipmentItem(
            id = "eq_galea_coolus",
            nameRu = "Шлем Кулус с гребнем",
            latinName = "Galea Coolus",
            type = EquipmentType.HELMET,
            material = EquipmentMaterial.IRON,
            defenseBonus = 4,
            moraleBonus = 5,
            casualtyReductionPct = 15,
            costDenarii = 95,
            forgeRequirementLevel = 2,
            descRu = "Кованый железный шлем с широким назатыльником и красным плюмажем центуриона.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_galea_attic_gold",
            nameRu = "Аттический золоченый шлем",
            latinName = "Galea Attica Praetoriana",
            type = EquipmentType.HELMET,
            material = EquipmentMaterial.IMPERIAL_GOLD,
            defenseBonus = 6,
            moraleBonus = 15,
            casualtyReductionPct = 25,
            costDenarii = 180,
            forgeRequirementLevel = 3,
            descRu = "Роскошный парадный шлем с чеканкой орлов и крыльями победы.",
            isCrafted = false
        ),

        // WEAPONS
        EquipmentItem(
            id = "eq_gladius_hispaniensis",
            nameRu = "Гладиус Испанский",
            latinName = "Gladius Hispaniensis",
            type = EquipmentType.WEAPON,
            material = EquipmentMaterial.IRON,
            attackBonus = 3,
            costDenarii = 70,
            forgeRequirementLevel = 1,
            descRu = "Обоюдоострый колющий меч из отборного закаленного железа с костяной рукоятью.",
            isCrafted = true,
            equippedCohortId = "coh_legio4_ferrata"
        ),
        EquipmentItem(
            id = "eq_heavy_pilum",
            nameRu = "Утяжеленный Пилум",
            latinName = "Pilum Gravis",
            type = EquipmentType.WEAPON,
            material = EquipmentMaterial.IRON,
            attackBonus = 5,
            costDenarii = 85,
            forgeRequirementLevel = 2,
            descRu = "Тяжелый дротик с мягким железным стержнем, гнущимся в щитах вражеской фаланги.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_gladius_toledo",
            nameRu = "Клинок из Толедо",
            latinName = "Gladius Chalybs",
            type = EquipmentType.WEAPON,
            material = EquipmentMaterial.SPANISH_STEEL,
            attackBonus = 8,
            moraleBonus = 5,
            costDenarii = 150,
            forgeRequirementLevel = 3,
            descRu = "Выкован из многослойной испанской стали. Рассекает вражеские доспехи как тростник.",
            isCrafted = false
        ),

        // ARMOR
        EquipmentItem(
            id = "eq_pectorale_bronze",
            nameRu = "Бронзовая Пектораль",
            latinName = "Pectorale Aeneum",
            type = EquipmentType.ARMOR,
            material = EquipmentMaterial.BRONZE,
            defenseBonus = 2,
            casualtyReductionPct = 10,
            costDenarii = 60,
            forgeRequirementLevel = 1,
            descRu = "Квадратная нагрудная пластина на кожаных ремнях для защиты сердца.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_lorica_hamata",
            nameRu = "Кольчуга Лорика Хамата",
            latinName = "Lorica Hamata",
            type = EquipmentType.ARMOR,
            material = EquipmentMaterial.IRON,
            defenseBonus = 5,
            casualtyReductionPct = 20,
            costDenarii = 120,
            forgeRequirementLevel = 2,
            descRu = "Кольчужная броня из 20 000 клепаных колец с усиленными наплечниками.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_lorica_musculata",
            nameRu = "Лорика Мускулата",
            latinName = "Lorica Musculata Aurea",
            type = EquipmentType.ARMOR,
            material = EquipmentMaterial.IMPERIAL_GOLD,
            defenseBonus = 8,
            moraleBonus = 20,
            casualtyReductionPct = 35,
            costDenarii = 220,
            forgeRequirementLevel = 3,
            descRu = "Анатомический золоченый панцирь трибунов с изображением горгоны Медузы.",
            isCrafted = false
        ),

        // SHIELDS
        EquipmentItem(
            id = "eq_scutum_republican",
            nameRu = "Овальный Скутум",
            latinName = "Scutum Republicanum",
            type = EquipmentType.SHIELD,
            material = EquipmentMaterial.IRON,
            defenseBonus = 4,
            casualtyReductionPct = 15,
            costDenarii = 75,
            forgeRequirementLevel = 1,
            descRu = "Изогнутый щит из трех слоев дерева, обтянутый воловьей кожей с железным умбоном.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_scutum_fulmen",
            nameRu = "Скутум «Молнии Юпитера»",
            latinName = "Scutum Fulminata",
            type = EquipmentType.SHIELD,
            material = EquipmentMaterial.SPANISH_STEEL,
            defenseBonus = 7,
            attackBonus = 2,
            casualtyReductionPct = 25,
            costDenarii = 135,
            forgeRequirementLevel = 2,
            descRu = "Усиленный стальным кантом щит с чеканными молниями бога грома.",
            isCrafted = false
        ),

        // ACCESSORIES / GREAVES
        EquipmentItem(
            id = "eq_ocreae_greaves",
            nameRu = "Бронзовые Кнемиды",
            latinName = "Ocreae Aeneae",
            type = EquipmentType.ACCESSORY,
            material = EquipmentMaterial.BRONZE,
            defenseBonus = 2,
            casualtyReductionPct = 8,
            costDenarii = 50,
            forgeRequirementLevel = 1,
            descRu = "Поножи для защиты левой выдвинутой вперед ноги в рукопашной схватке.",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_caligae_iron",
            nameRu = "Калиги с железными шипами",
            latinName = "Caligae Clavatae",
            type = EquipmentType.ACCESSORY,
            material = EquipmentMaterial.IRON,
            attackBonus = 1,
            defenseBonus = 2,
            costDenarii = 65,
            forgeRequirementLevel = 1,
            descRu = "Прочные армейские сандалии на тройной подошве с сотней кованых гвоздей.",
            isCrafted = false
        ),

        // STANDARDS
        EquipmentItem(
            id = "eq_signum_cohortis",
            nameRu = "Штандарт Сигнум Когорты",
            latinName = "Signum Cohortis",
            type = EquipmentType.STANDARD,
            material = EquipmentMaterial.BRONZE,
            attackBonus = 2,
            moraleBonus = 15,
            costDenarii = 110,
            forgeRequirementLevel = 2,
            descRu = "Шест с серебряными фалерами и бронзовой ладонью верности (Manus).",
            isCrafted = false
        ),
        EquipmentItem(
            id = "eq_aquila_standard",
            nameRu = "Золотой Орел Юпитера",
            latinName = "Aquila Legionis",
            type = EquipmentType.STANDARD,
            material = EquipmentMaterial.IMPERIAL_GOLD,
            attackBonus = 3,
            defenseBonus = 3,
            moraleBonus = 30,
            costDenarii = 240,
            forgeRequirementLevel = 3,
            descRu = "Священный золотой орел легиона, вдохновляющий воинов стоять насмерть.",
            isCrafted = false
        )
    )

    fun createInitialSenateQuests(): List<SenateQuest> = listOf(
        // 1. SENATE CAMPAIGN QUESTS
        SenateQuest(
            id = "sq_appian_way",
            titleRu = "Безопасность Аппиевой дороги",
            issuerRu = "Цензор Аппий Клавдий",
            descriptionRu = "Очистите южные тракты от разбойников и самнитских банд (одержите 2 победы в кампаниях).",
            rewardDenarii = 150,
            rewardSenateFavor = 15,
            rewardGlory = 12,
            targetType = "VICTORIES",
            targetCount = 2,
            currentProgress = 1,
            category = QuestCategory.SENATE_CAMPAIGN,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.OPTIMATES,
            icon = "🛣️",
            flavorHistoryRu = "«Via Appia Regina Viarum» — царица дорог свяжет Рим с Капуей и обеспечит переброску легионов.",
            bonusPerkDescRu = "+15% к скорости снабжения легиона",
            actionHintRu = "Совершите победоносные походы в регионе Лаций или Кампания",
            targetScreenHint = "EXPEDITIONS"
        ),
        SenateQuest(
            id = "sq_samnium_subjugation",
            titleRu = "Умиротворение Самния",
            issuerRu = "Консул Квинт Фабий Максим",
            descriptionRu = "Разгромите горные племена самнитов и отомстите за позор в Кавдинском ущелье.",
            rewardDenarii = 240,
            rewardSenateFavor = 25,
            rewardGlory = 30,
            targetType = "EXPEDITION_WIN",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.SENATE_CAMPAIGN,
            priority = QuestPriority.SENATUS_CONSULTUM_ULTIMUM,
            faction = SenateFaction.BELLICOSI,
            icon = "⛰️",
            flavorHistoryRu = "Самниты — самый упорный враг Рима в Италии. Их разгром откроет путь к господству над Апеннинами.",
            bonusPerkDescRu = "Открывает трофей «Шлем самнитского медраса» и уважение консулов",
            actionHintRu = "Победите в экспедиции «Умиротворение Самния»",
            targetScreenHint = "EXPEDITIONS"
        ),
        SenateQuest(
            id = "sq_pyrrhic_threat",
            titleRu = "Чрезвычайный приказ: Пирр Эпирский",
            issuerRu = "Сенат и Римский Народ (SPQR)",
            descriptionRu = "Остановите вторжение царя Пирра и его боевых слонов в Лукании.",
            rewardDenarii = 450,
            rewardSenateFavor = 40,
            rewardGlory = 50,
            targetType = "EXPEDITION_WIN",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.SENATE_CAMPAIGN,
            priority = QuestPriority.SENATUS_CONSULTUM_ULTIMUM,
            faction = SenateFaction.BELLICOSI,
            icon = "🐘",
            flavorHistoryRu = "«Еще одна такая победа, и я останусь без войска!» — слова царя Пирра перед стеной римских скутумов.",
            bonusPerkDescRu = "Присвоение легиону высшего звания Legio Invicta",
            actionHintRu = "Одержите победу в чрезвычайном испытании Сената против Эпира",
            targetScreenHint = "EXPEDITIONS"
        ),

        // 2. SEASONAL MANDATES
        SenateQuest(
            id = "sq_velites_skirmish",
            titleRu = "Выучка застрельщиков (Velites)",
            issuerRu = "Трибун Тиберий Деций",
            descriptionRu = "Проведите муштру велитов на Марсовом поле для отражения легкой конницы врага.",
            rewardDenarii = 100,
            rewardSenateFavor = 10,
            rewardGlory = 8,
            targetType = "UNIT_TRAINING",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.SEASONAL_MANDATE,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.POPULARES,
            icon = "🏹",
            flavorHistoryRu = "Быстрые юноши с дротиками и волчьими шкурами первыми встречают наступающего противника.",
            bonusPerkDescRu = "+5 к скорости реакции и инициативе стрелков",
            actionHintRu = "Завершите тренировку велитов во вкладке «Муштра»",
            targetScreenHint = "TRAINING"
        ),
        SenateQuest(
            id = "sq_triarii_reserve",
            titleRu = "Формирование несокрушимых триариев",
            issuerRu = "Главный Инструктор (Campidoctor)",
            descriptionRu = "Обучите и включите в боевой строй ветеранов-триариев с тяжелыми копьями.",
            rewardDenarii = 160,
            rewardSenateFavor = 15,
            rewardGlory = 18,
            targetType = "UNIT_TRAINING",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.SEASONAL_MANDATE,
            priority = QuestPriority.URGENT,
            faction = SenateFaction.OPTIMATES,
            icon = "🔱",
            flavorHistoryRu = "«Дело дошло до триариев» — высшая мера стойкости в римском военном искусстве.",
            bonusPerkDescRu = "+10 к защите от прорыва строя фалангой",
            actionHintRu = "Проведите интенсивную муштру отряда триариев",
            targetScreenHint = "TRAINING"
        ),
        SenateQuest(
            id = "sq_great_triumph",
            titleRu = "Великий Триумф Рима",
            issuerRu = "Сенат и Римский Народ (SPQR)",
            descriptionRu = "Одержите Великую победу (Триумф) без значительных потерь в личном составе.",
            rewardDenarii = 220,
            rewardSenateFavor = 30,
            rewardGlory = 35,
            targetType = "GREAT_VICTORIES",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.SEASONAL_MANDATE,
            priority = QuestPriority.URGENT,
            faction = SenateFaction.BELLICOSI,
            icon = "🏆",
            flavorHistoryRu = "Триумфатор в лавровом венке въезжает на Капитолий под крики «Io Triumphe!».",
            bonusPerkDescRu = "+20 к постоянной морали легионеров",
            actionHintRu = "Разгромите врага с минимальными потерями при помощи эффективной тактики",
            targetScreenHint = "EXPEDITIONS"
        ),

        // 3. LOGISTICS & FABRICA QUESTS
        SenateQuest(
            id = "sq_granary_reserves",
            titleRu = "Зерновой резерв Республики",
            issuerRu = "Эдил Квинт Сервилий",
            descriptionRu = "Улучшите лагерный Хорреум (Horreum) до II уровня для надежного снабжения легиона.",
            rewardDenarii = 130,
            rewardSenateFavor = 20,
            rewardGlory = 10,
            targetType = "BUILDING_LEVEL",
            targetCount = 2,
            currentProgress = 1,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.POPULARES,
            icon = "🌾",
            flavorHistoryRu = "«Сытый легион шагает победной поступью.» Хорреум защищает запасы зерна от сырости и мышей.",
            bonusPerkDescRu = "Увеличение вместимости складов и снижение затрат на продовольствие",
            actionHintRu = "Улучшите здание Хорреум во вкладке «Стройка»",
            targetScreenHint = "BUILDINGS"
        ),
        SenateQuest(
            id = "sq_forge_equipment",
            titleRu = "Оружейное перевооружение",
            issuerRu = "Легат Партии Войны",
            descriptionRu = "Выкуйте не менее 3 единиц снаряжения в мастерской Фабрика (шлемы, гладиусы или скутумы).",
            rewardDenarii = 170,
            rewardSenateFavor = 15,
            rewardGlory = 15,
            targetType = "FORGED_EQUIPMENT",
            targetCount = 3,
            currentProgress = 2,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.BELLICOSI,
            icon = "🗡️",
            flavorHistoryRu = "Испанские гладиусы и прочные скутумы превращают манипулы в непробиваемую стену.",
            bonusPerkDescRu = "+5 к силе атаки экипированных когорт",
            actionHintRu = "Выкуйте броню или клинки в мастерской «Кузница»",
            targetScreenHint = "ARMORY"
        ),
        SenateQuest(
            id = "sq_provisions_stockpile",
            titleRu = "Стратегический запас провизии",
            issuerRu = "Квестор Легиона",
            descriptionRu = "Накопите в зернохранилищах каструма не менее 250 мер отборного зерна.",
            rewardDenarii = 140,
            rewardSenateFavor = 15,
            rewardGlory = 10,
            targetType = "PROVISIONS_RESERVE",
            targetCount = 250,
            currentProgress = 180,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.POPULARES,
            icon = "🍞",
            flavorHistoryRu = "Зимние походы требуют надежной базы снабжения без риска голода в гарнизоне.",
            bonusPerkDescRu = "Защита от зимней убыли легионеров",
            actionHintRu = "Закупайте зерно на рынке, собирайте осенний урожай или снаряжайте обозы",
            targetScreenHint = "TREASURY"
        ),

        // 4. DIVINE VOW QUESTS
        SenateQuest(
            id = "sq_mars_vow",
            titleRu = "Кровавый обет Марсу Мстителю",
            issuerRu = "Великий Понтифик Луций",
            descriptionRu = "Совершите ритуальное жертвоприношение Марсу перед началом боевой кампании.",
            rewardDenarii = 110,
            rewardSenateFavor = 15,
            rewardGlory = 14,
            targetType = "ACTIVE_BLESSING",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.DIVINE_VOW,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.PONTIFICES,
            icon = "🕊️",
            flavorHistoryRu = "«Mars Gradivus, благослови мечи сынов Ромула на ратные подвиги!»",
            bonusPerkDescRu = "+15% к шансу Великого Триумфа в следующем сражении",
            actionHintRu = "Совершите ритуал Марса на священном Алтаре",
            targetScreenHint = "ALTAR"
        ),
        SenateQuest(
            id = "sq_jupiter_auspices",
            titleRu = "Священные Авгурии Капитолия",
            issuerRu = "Коллегия Авгуров Рима",
            descriptionRu = "Получите благословение Юпитера Величайшего через священные знамения.",
            rewardDenarii = 150,
            rewardSenateFavor = 25,
            rewardGlory = 15,
            targetType = "ACTIVE_BLESSING",
            targetCount = 1,
            currentProgress = 0,
            category = QuestCategory.DIVINE_VOW,
            priority = QuestPriority.URGENT,
            faction = SenateFaction.PONTIFICES,
            icon = "⚡",
            flavorHistoryRu = "Юпитер Оптимус Максимус защищает Рим от военных катастроф и смуты.",
            bonusPerkDescRu = "Полная защита легиона от разгрома",
            actionHintRu = "Проведите обряд Юпитера на вкладке «Алтарь»",
            targetScreenHint = "ALTAR"
        )
    )

    fun createDynamicQuestPool(): List<SenateQuest> = listOf(
        SenateQuest(
            id = "sq_dyn_caravan_trade",
            titleRu = "Снабжение через порт Остии",
            issuerRu = "Эдил Морской Торговли",
            descriptionRu = "Снарядите торговый караван в Остию и доставьте товары для граждан Рима.",
            rewardDenarii = 120,
            rewardSenateFavor = 12,
            rewardGlory = 10,
            targetType = "CARAVAN_DISPATCHED",
            targetCount = 1,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.POPULARES,
            icon = "🚢",
            flavorHistoryRu = "Тибр полон купеческих судов, везущих оливковое масло, вина и зерно Сицилии.",
            actionHintRu = "Отправьте караван в Остию во вкладке «Казна & Рынок»",
            targetScreenHint = "TREASURY"
        ),
        SenateQuest(
            id = "sq_dyn_veteran_drill",
            titleRu = "Формирование ветеранского ядра",
            issuerRu = "Консул Военного Совета",
            descriptionRu = "Воспитайте в строю легиона не менее 35 закаленных ветеранов.",
            rewardDenarii = 200,
            rewardSenateFavor = 20,
            rewardGlory = 25,
            targetType = "VETERANS_COUNT",
            targetCount = 35,
            category = QuestCategory.SEASONAL_MANDATE,
            priority = QuestPriority.URGENT,
            faction = SenateFaction.OPTIMATES,
            icon = "🛡️",
            flavorHistoryRu = "Ветераны с серебряными браслетами — становой хребет римского манипулярного строя.",
            actionHintRu = "Проводите интенсивную муштру и побеждайте в походах",
            targetScreenHint = "TRAINING"
        ),
        SenateQuest(
            id = "sq_dyn_doctrines_reform",
            titleRu = "Военно-теоретическая реформа",
            issuerRu = "Военный Трибун-Теоретик",
            descriptionRu = "Изучите 3 военные доктрины за Славу легиона.",
            rewardDenarii = 210,
            rewardSenateFavor = 25,
            rewardGlory = 30,
            targetType = "DOCTRINES_LEARNED",
            targetCount = 3,
            category = QuestCategory.SENATE_CAMPAIGN,
            priority = QuestPriority.URGENT,
            faction = SenateFaction.OPTIMATES,
            icon = "📜",
            flavorHistoryRu = "Рим перенимает лучшее у своих врагов: самнитские скутумы, греческие баллисты и кельтские клинки.",
            actionHintRu = "Изучите доктрины во вкладке «Доктрины»",
            targetScreenHint = "DOCTRINES"
        ),
        SenateQuest(
            id = "sq_dyn_win_streak",
            titleRu = "Непобедимая поступь (Серия побед)",
            issuerRu = "Консул Партии Войны",
            descriptionRu = "Одержите серию из 5 побед подряд без единого поражения.",
            rewardDenarii = 260,
            rewardSenateFavor = 30,
            rewardGlory = 35,
            targetType = "WIN_STREAK",
            targetCount = 5,
            category = QuestCategory.SEASONAL_MANDATE,
            priority = QuestPriority.SENATUS_CONSULTUM_ULTIMUM,
            faction = SenateFaction.BELLICOSI,
            icon = "🔥",
            flavorHistoryRu = "Слава о непобедимом легионе повергает врагов Рима в ужас еще до первого броска пилумов.",
            actionHintRu = "Тщательно подбирайте командиров и тактику для бескомпромиссных побед",
            targetScreenHint = "EXPEDITIONS"
        ),
        SenateQuest(
            id = "sq_dyn_fortress_citadel",
            titleRu = "Неприступная Цитадель Каструма",
            issuerRu = "Префект Лагеря (Praefectus Castrorum)",
            descriptionRu = "Улучшите все сооружения лагеря до II ранга или выше.",
            rewardDenarii = 300,
            rewardSenateFavor = 35,
            rewardGlory = 40,
            targetType = "ALL_BUILDINGS_UPGRADED",
            targetCount = 2,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.SENATUS_CONSULTUM_ULTIMUM,
            faction = SenateFaction.OPTIMATES,
            icon = "🏰",
            flavorHistoryRu = "Каменный вал, ворота с железными оковками и башни делают лагерь неприступным для любых набегов.",
            actionHintRu = "Модернизируйте все 6 зданий лагеря",
            targetScreenHint = "BUILDINGS"
        ),
        SenateQuest(
            id = "sq_dyn_treasury_wealth",
            titleRu = "Казна Великой Республики",
            issuerRu = "Цензор Аппий Клавдий",
            descriptionRu = "Увеличьте золотой запас казначейства легиона до 500 денариев.",
            rewardDenarii = 150,
            rewardSenateFavor = 25,
            rewardGlory = 20,
            targetType = "DENARII_TREASURY",
            targetCount = 500,
            category = QuestCategory.LOGISTICS_FABRICA,
            priority = QuestPriority.STANDARD,
            faction = SenateFaction.OPTIMATES,
            icon = "💰",
            flavorHistoryRu = "Богатство легиона гарантирует своевременную выплату жалованья и закупку лучшей испанской стали.",
            actionHintRu = "Торгуйте зерном, отправляйте караваны и побеждайте в богатых походах",
            targetScreenHint = "TREASURY"
        )
    )

    fun createInitialPetitions(): List<SenatePetition> = listOf(
        SenatePetition(
            id = "pet_subventio",
            titleRu = "Чрезвычайная субсидия",
            latinNameRu = "Subventio Militaris",
            icon = "💰",
            descriptionRu = "Запросить у Курии экстренные средства из казны Республики на жалованье легиону.",
            favorCost = 15,
            denariiCost = 0,
            minFavorRequired = 30,
            rewardSummaryRu = "+160 💰 Денариев в казну легиона"
        ),
        SenatePetition(
            id = "pet_veteran_levy",
            titleRu = "Сенаторский призыв ветеранов",
            latinNameRu = "Evocati Senatus",
            icon = "⚔️",
            descriptionRu = "Призвать на службу закаленных добровольцев Эвокатов во все когорты.",
            favorCost = 20,
            denariiCost = 0,
            minFavorRequired = 45,
            rewardSummaryRu = "+20 воинов и +10 ветеранов во все когорты"
        ),
        SenatePetition(
            id = "pet_lex_agraria",
            titleRu = "Закон о земле (Lex Agraria)",
            latinNameRu = "Lex Agraria Veteranorum",
            icon = "📜",
            descriptionRu = "Провести через народных трибунов закон о наделе ветеранов участками в Кампании.",
            favorCost = 25,
            denariiCost = 0,
            minFavorRequired = 60,
            rewardSummaryRu = "+25 к боевому духу (морали) и +15 к дисциплине всего легиона"
        ),
        SenatePetition(
            id = "pet_oratio_curia",
            titleRu = "Речь в Курии (Дебаты)",
            latinNameRu = "Oratio in Curia",
            icon = "🗣️",
            descriptionRu = "Направить красноречивого оратора для восхваления подвигов легиона перед сенаторами.",
            favorCost = 0,
            denariiCost = 45,
            minFavorRequired = 0,
            rewardSummaryRu = "+15 🏛️ Расположения Сената"
        ),
        SenatePetition(
            id = "pet_banquet_munera",
            titleRu = "Сенаторские Игры и Дары",
            latinNameRu = "Munera et Epulum",
            icon = "🍷",
            descriptionRu = "Устроить пышный пир с фалернским вином и гладиаторские бои в честь патрициев.",
            favorCost = 0,
            denariiCost = 90,
            minFavorRequired = 0,
            rewardSummaryRu = "+30 🏛️ Расположения Сената и +10 🏆 Славы"
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

    fun createInitialRituals(): List<DivineRitual> = listOf(
        DivineRitual(
            id = "rit_mars_sacrifice",
            god = GodType.MARS,
            nameRu = "Кровавое жертвоприношение Марсу",
            descriptionRu = "Жрец-гаруспик приносит в жертву белого быка на алтаре лагеря перед походом.",
            blessingEffectRu = "+15% шанс Великой Победы (Триумфа) и +5 к атаке всех когорт на текущий сезон.",
            costDenarii = 30,
            costProvisions = 10
        ),
        DivineRitual(
            id = "rit_jupiter_auspices",
            god = GodType.JUPITER,
            nameRu = "Священные Авгурии Юпитера",
            descriptionRu = "Гадание по полету орлов и клеву священных кур (Auspicia Pullaria). Защищает легион от разгрома.",
            blessingEffectRu = "+15% к одобрению Сената и полный иммунитет к Катастрофическим разгромам.",
            costDenarii = 25,
            costProvisions = 0
        ),
        DivineRitual(
            id = "rit_ceres_offering",
            god = GodType.CERES,
            nameRu = "Жатвенное приношение Церере",
            descriptionRu = "Возлияние вина, меда и первых колосьев пшеницы богине плодородия.",
            blessingEffectRu = "+40% к сбору зерна и провизии, снижение зимних потерь армии.",
            costDenarii = 15,
            costProvisions = 20
        ),
        DivineRitual(
            id = "rit_minerva_vow",
            god = GodType.MINERVA,
            nameRu = "Обет Мудрой Минерве",
            descriptionRu = "Молитва об остроте ума командиров и военном искусстве.",
            blessingEffectRu = "+35% к получаемому боевому опыту (XP) и +3 к защите манипул.",
            costDenarii = 35,
            costProvisions = 0
        ),
        DivineRitual(
            id = "rit_fortuna_prayer",
            god = GodType.FORTUNA,
            nameRu = "Молитва Фортуне Победительнице",
            descriptionRu = "Просьба к богине удачи о спасении воинов и благополучном возвращении в лагерь.",
            blessingEffectRu = "-30% потерь в любых битвах и удвоенный шанс спасения ветеранов.",
            costDenarii = 20,
            costProvisions = 10
        )
    )

    fun createInitialTrophies(): List<LegionTrophy> = listOf(
        LegionTrophy(
            id = "trophy_senones_torc",
            titleRu = "Золотой торквес галльского вождя",
            originRu = "Битва у реки Адидже (Галльские набеги)",
            icon = "💍",
            descriptionRu = "Массивное кованое шейное кольцо из чистого альпийского золота, сорванное с павшего вождя сенонов.",
            passivePerkRu = "+10% золота и добычи из всех военных походов.",
            isUnlocked = true,
            unlockConditionRu = "Добыто в первой победной кампании легиона"
        ),
        LegionTrophy(
            id = "trophy_samnite_crest",
            titleRu = "Шлем самнитского медраса с перьями",
            originRu = "Самнитские войны (Кавдинские горы)",
            icon = "🪖",
            descriptionRu = "Бронзовый боевой шлем с пышным плюмажем горцев Самния. Символ превосходства римских манипул.",
            passivePerkRu = "+2 к постоянной броне и защите всех когорт легиона.",
            isUnlocked = false,
            unlockConditionRu = "Одержите победу в экспедиции «Умиротворение Самния»"
        ),
        LegionTrophy(
            id = "trophy_etruscan_stele",
            titleRu = "Этрусская бронзовая химера",
            originRu = "Вейи и Этрурия",
            icon = "🦁",
            descriptionRu = "Изысканный жертвенный сосуд древних этрусков, переданный в дар легиону старейшинами Капуи.",
            passivePerkRu = "+5 к морали и дисциплине всех манипул.",
            isUnlocked = false,
            unlockConditionRu = "Достигните уровня 3 для любого здания лагеря"
        ),
        LegionTrophy(
            id = "trophy_pyrrhic_phalanx_banner",
            titleRu = "Штандарт Эпирской фаланги",
            originRu = "Тарент и Пирровы войны",
            icon = "🚩",
            descriptionRu = "Захваченный шелковый стяг царских телохранителей Пирра после ожесточенного штыкового боя.",
            passivePerkRu = "+3 к базовой атаке легионеров-ветеранов.",
            isUnlocked = false,
            unlockConditionRu = "Одержите 5 Великих побед (Триумфов) в истории легиона"
        ),
        LegionTrophy(
            id = "trophy_golden_aquila",
            titleRu = "Золотой Орел Республики (Aquila)",
            originRu = "Дар Сената и Римского Народа",
            icon = "🦅",
            descriptionRu = "Священный золотой орел на инкрустированном древке. Главная реликвия Legio IV Scipio.",
            passivePerkRu = "+25% к расположению Сената и +15 Славы за каждый триумф.",
            isUnlocked = false,
            unlockConditionRu = "Достигните ранга Республики «Legio Invicta»"
        )
    )

    fun createInitialUnitAllocations(): List<UnitTrainingAllocation> = listOf(
        UnitTrainingAllocation(
            unitType = UnitType.HASTATI,
            allocatedCount = 12,
            drillIntensity = DrillIntensity.STANDARD,
            targetCohortId = "cohort_1"
        ),
        UnitTrainingAllocation(
            unitType = UnitType.PRINCIPES,
            allocatedCount = 8,
            drillIntensity = DrillIntensity.INTENSIVE,
            targetCohortId = "cohort_2"
        ),
        UnitTrainingAllocation(
            unitType = UnitType.TRIARII,
            allocatedCount = 4,
            drillIntensity = DrillIntensity.GLADIATOR,
            targetCohortId = "cohort_3"
        ),
        UnitTrainingAllocation(
            unitType = UnitType.VELITES,
            allocatedCount = 15,
            drillIntensity = DrillIntensity.STANDARD,
            targetCohortId = "cohort_4"
        ),
        UnitTrainingAllocation(
            unitType = UnitType.EQUITES,
            allocatedCount = 6,
            drillIntensity = DrillIntensity.INTENSIVE,
            targetCohortId = "cohort_1"
        ),
        UnitTrainingAllocation(
            unitType = UnitType.FUNDITORES,
            allocatedCount = 10,
            drillIntensity = DrillIntensity.STANDARD,
            targetCohortId = "cohort_2"
        )
    )

    fun createInitialInvestments(): List<ProvincialInvestment> = listOf(
        ProvincialInvestment(
            id = "inv_latian_latifundia",
            titleRu = "Латифундии Лация",
            latinNameRu = "Latifundia Latii",
            regionRu = "Лаций и Альбанские холмы",
            icon = "🌿",
            level = 1,
            maxLevel = 3,
            baseCostDenarii = 60,
            seasonalDenarii = 18,
            seasonalProvisions = 22,
            seasonalGlory = 0,
            specialPerkRu = "Стабильное зерновое и денежное довольствие с плодородных равнин вокруг Рима.",
            historyQuoteRu = "«Земля отцов наших питает сыновей Рима в каждом походе.»",
            descriptionRu = "Обширные сельские поместья и зерновые угодья, принадлежащие ветеранам и патрициям легиона. Обеспечивают базовый сезонный приток провианта и денариев."
        ),
        ProvincialInvestment(
            id = "inv_campania_vineyard",
            titleRu = "Винодельни и оливковые сады Капуи",
            latinNameRu = "Vinea Campaniae",
            regionRu = "Кампания (Campania Felix)",
            icon = "🍇",
            level = 0,
            maxLevel = 3,
            baseCostDenarii = 95,
            seasonalDenarii = 38,
            seasonalProvisions = 16,
            seasonalGlory = 0,
            specialPerkRu = "Знаменитое фалернское вино и оливковое масло. Высокая торговая маржа на рынках Рима.",
            historyQuoteRu = "«Плодороднейшая земля Италии, дарующая золото из сока виноградных лоз.»",
            descriptionRu = "Вложения в богатые винодельческие усадьбы у подножия Везувия. Элитное вино и амфоры с маслом пользуются огромным спросом в Курии и гарнизонах."
        ),
        ProvincialInvestment(
            id = "inv_etruria_mines",
            titleRu = "Рудники и серебряные копи Этрурии",
            latinNameRu = "Metalla Etruriae",
            regionRu = "Этрурия (Вейи и Тарквинии)",
            icon = "⚒️",
            level = 0,
            maxLevel = 3,
            baseCostDenarii = 150,
            seasonalDenarii = 65,
            seasonalProvisions = 10,
            seasonalGlory = 0,
            specialPerkRu = "Прямые поставки оружейного железа и серебряной руды для кузниц легиона.",
            historyQuoteRu = "«В недрах Этрурии куется непобедимая сталь римских гладиусов и пилумов.»",
            descriptionRu = "Горнодобывающие концессии на древних этрусских разработках. Гарантируют постоянный приток слитков серебра и высокосортного железа."
        ),
        ProvincialInvestment(
            id = "inv_ostia_fleet",
            titleRu = "Купеческий флот Остии",
            latinNameRu = "Classis Mercatoria Ostiensis",
            regionRu = "Остия и устье Тибра",
            icon = "🚢",
            level = 0,
            maxLevel = 3,
            baseCostDenarii = 220,
            seasonalDenarii = 100,
            seasonalProvisions = 25,
            seasonalGlory = 1,
            specialPerkRu = "Морской торговый синдикат. Крупные доходы от импорта сицилийского и пунического зерна.",
            historyQuoteRu = "«Все богатства морей стекаются в гавань Остии, питая могущество Республики.»",
            descriptionRu = "Доля в торговых кораблях-корбитах, курсирующих между Римом, Сардинией и Сицилией. Приносит солидную прибыль и престиж среди нобилитета."
        ),
        ProvincialInvestment(
            id = "inv_magna_graecia",
            titleRu = "Фактории Великой Греции",
            latinNameRu = "Emporia Magnae Graeciae",
            regionRu = "Тарент, Неаполь и Сиракузы",
            icon = "🏛️",
            level = 0,
            maxLevel = 3,
            baseCostDenarii = 330,
            seasonalDenarii = 160,
            seasonalProvisions = 30,
            seasonalGlory = 2,
            specialPerkRu = "Торговля шелком, финикийским пурпуром и античной керамикой. Повышает расположение Сената.",
            historyQuoteRu = "«Греческая роскошь и тонкое ремесло обращаются на службу римскому оружию.»",
            descriptionRu = "Элитные торговые фактории в греческих полисах Южной Италии. Обеспечивают масштабные финансовые вливания и укрепляют дипломатический вес легиона."
        ),
        ProvincialInvestment(
            id = "inv_moneta_legionis",
            titleRu = "Монетный двор легиона",
            latinNameRu = "Officina Monetae Legionis",
            regionRu = "Лагерный монетный двор",
            icon = "🪙",
            level = 0,
            maxLevel = 3,
            baseCostDenarii = 450,
            seasonalDenarii = 220,
            seasonalProvisions = 15,
            seasonalGlory = 3,
            specialPerkRu = "Право чеканки именной серебряной монеты легиона с профилем победоносного полководца.",
            historyQuoteRu = "«Серебро с печатью SPQR и орлом IV Легиона принимают во всех уголках ойкумены.»",
            descriptionRu = "Высший статус экономической автономии. Собственная чеканка монет легиона утверждает легендарный престиж и обеспечивает колоссальный доход."
        )
    )
}

