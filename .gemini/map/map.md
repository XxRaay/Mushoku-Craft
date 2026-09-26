# 🗺️ Архитектурная карта проекта: MushokuCraft

> Дата последнего обновления: 2026-09-25 | Версия карты: 1.3

## 1. Обзор проекта и технологический стек
- **Назначение:** Глобальный хардкорный RPG-мод для Minecraft по мотивам вселенной «Реинкарнация безработного» (Mushoku Tensei). Мод кардинально преобразует механики боя, магии, исследования и крафта. Реализует систему мастерства игрока вместо гринда: 4 стихийные школы магии (Вода, Огонь, Земля, Ветер) и Высшая магия (Святой класс Cumulonimbus), 3 школы фехтования (Бог Меча, Бог Воды, Бог Севера), Боевую Ауру (Токи), систему инкантаций с QTE и шансом осечки (Fizzle), Безмолвные мгновенные чары (Hold-to-charge), изучение магии по книгам, разделку туш маг-зверей (Бестиарий) и модульную ковку оружия на Магической наковальне.
- **Стек:**
  - Язык: **Java 21**
  - Базовая игра: **Minecraft 1.21.1** (Mojang Official Mappings)
  - Архитектура: **Architectury API 13.0.6** (Multi-Loader: единая логика в Common + загрузчики NeoForge/Fabric)
  - Сборка: **Gradle 8.x**, **Architectury Loom 1.10.455**, **Architectury Plugin 3.4.164**
  - Поддерживаемые платформы: **NeoForge 21.1.235**, **Fabric Loader 0.16.2 / Fabric API 0.102.0+1.21.1**
  - Анимации и визуал: **GeckoLib 4.6.6**, **Player Animation Lib 2.0.4+1.21.1**
  - Сеть и регистрация: **Architectury NetworkManager**, **Architectury DeferredRegister**, **Event Hooks**
  - Сохранение состояния: **SpongePowered Mixin** (`PlayerEntityMixin` + `PlayerMasteryData` NBT)
- **Точка входа (Entrypoint):**
  - Общее ядро (Common): `Common/src/main/java/com/mushokucraft/MushokuCraftCommon.java` (`init()`)
  - Платформа NeoForge: `NeoForge_1.21.1/src/main/java/com/mushokucraft/neoforge/MushokuCraftNeoForge.java` (`@Mod("mushokucraft")`)
  - Платформа Fabric: `Fabric_1.21.1/src/main/java/com/mushokucraft/fabric/MushokuCraftFabric.java` (`onInitialize()`)
  - Клиентский сетап Common: `Common/src/main/java/com/mushokucraft/client/ModClientSetup.java` (`register()`)
  - Клиент Fabric: `Fabric_1.21.1/src/main/java/com/mushokucraft/fabric/client/MushokuCraftFabricClient.java` (`onInitializeClient()`)
  - Клиент NeoForge: `NeoForge_1.21.1/src/main/java/com/mushokucraft/neoforge/NeoForgeClientEvents.java`

---

## 2. Иерархическая структура каталогов и файлов
```text
MushokuCraft/
├── .gemini/
│   └── map/
│       └── map.md                                      # Архитектурная карта проекта (Single Source of Truth)
├── Common/                                             # Ядро мода (99% кроссплатформенного кода)
│   ├── build.gradle                                    # Спецификация зависимостей модуля Common
│   └── src/main/
│       ├── java/com/mushokucraft/
│       │   ├── MushokuCraftCommon.java                 # Главная общая точка инициализации мода
│       │   ├── block/                                  # Пользовательские блоки и тайлы
│       │   │   ├── AirCushionBlock.java                # Неразрушимый блок воздушной подушки
│       │   │   ├── BarrierWallBlock.java               # 1-пиксельная полупрозрачная защитная стена барьера с перенаправлением урона
│       │   │   ├── MagicCircleBlock.java               # 1-пиксельный блок установленного магического круга
│       │   │   ├── ModularAnvilBlock.java              # Блок модульной наковальни для крафта оружия
│       │   │   └── entity/
│       │   │       ├── AirCushionBlockEntity.java      # Блок-сущность подушки с таймером исчезновения
│       │   │       ├── BarrierWallBlockEntity.java     # Блок-сущность стены барьера (привязка к кругу)
│       │   │       └── MagicCircleBlockEntity.java     # Блок-сущность магического круга (мана, узор, связывание, барьер)
│       │   ├── client/                                 # Клиентская логика, HUD, ввод и рендеринг
│       │   │   ├── ModClientSetup.java                 # Инициализация рендереров, слоев и моделей
│       │   │   ├── gui/                                # Экраны интерфейса и экранные оверлеи
│       │   │   │   ├── AncientManuscriptScreen.java    # GUI просмотра древней схемы магического круга
│       │   │   │   ├── IncantationOverlay.java         # Оверлей каста заклинания и инкантаций
│       │   │   │   ├── MagicBookScreen.java            # GUI чтения и изучения книги заклинаний
│       │   │   │   ├── MagicCanvasScreen.java          # GUI попиксельного начертания 16x16 узора с расходом чернил в реальном времени
│       │   │   │   ├── MasteryOverlay.java             # Оверлей уведомления о прокачке мастерства
│       │   │   │   ├── ModularAnvilScreen.java         # GUI модульной ковки оружия
│       │   │   │   ├── QTEOverlay.java                 # HUD мини-игры QTE (клавиша, зона успеха, прогресс)
│       │   │   │   ├── SpellWheelScreen.java           # Радиальное меню заклинаний (клавиша R)
│       │   │   │   ├── StanceWheelScreen.java          # Радиальное меню стоек меча (клавиша X)
│       │   │   │   └── widget/
│       │   │   │       └── FantasyButton.java          # Кастомная стилизованная кнопка (резное дерево, латунь, золотое свечение)
│       │   │   ├── hud/                                # Кастомный рендеринг шкалы маны
│       │   │   │   ├── ArcManaRenderer.java            # Дугообразный HUD маны вокруг прицела
│       │   │   │   ├── ClassicManaBarRenderer.java     # Классический линейный HUD маны над хотбаром
│       │   │   │   ├── HudConstants.java               # Цвета и геометрические константы HUD
│       │   │   │   ├── ManaAnimationState.java         # Анимация сотрясения и интерполяция маны
│       │   │   │   ├── ManaDisplayMode.java            # Перечисление режимов (CLASSIC, ARC, HYBRID, MINIMAL)
│       │   │   │   └── ManaHudManager.java             # Диспетчер выбора и отрисовки мана-баров
│       │   │   ├── input/                              # Обработчики пользовательского ввода
│       │   │   │   ├── ClientCastState.java            # Клиентское состояние активного каста
│       │   │   │   ├── ClientSpellState.java           # Клиентский кэш выбранного заклинания
│       │   │   │   ├── ClientStanceState.java          # Клиентский кэш активной стойки
│       │   │   │   ├── CombatInputHandler.java         # Обработка ЛКМ/ПКМ, атака, парирование, чардж
│       │   │   │   ├── MenuInputHandler.java           # Вызов радиальных меню заклинаний и стоек
│       │   │   │   ├── ModKeybindings.java             # Регистрация горячих клавиш (R, X)
│       │   │   │   └── QteInputHandler.java            # Перехват нажатия нужной клавиши в окне QTE
│       │   │   ├── network/
│       │   │   │   └── ClientPayloadHandler.java       # Клиентские приемники S2C сетевых пакетов
│       │   │   └── render/                             # Рендереры и модели сущностей и предметов
│       │   │       ├── MagicBookItemModel.java         # 3D-модель книги заклинаний
│       │   │       ├── MagicBookItemRenderer.java      # Специальный рендерер предмета книги
│       │   │       ├── block/
│       │   │       │   └── MagicCircleBlockEntityRenderer.java # Рендерер 16x16 узора и свечения круга
│       │   │       ├── entity/                         # EntityRenderers (AirStrike, Wolf, Icicle, Water, etc.)
│       │   │       └── model/                          # EntityModels (Fireball, Icicle, SabertoothWolf, etc.)
│       │   ├── combat/                                 # Боевые школы меча и Токи (Боевая Аура)
│       │   │   ├── NorthGodHandler.java                # Логика Стиля Бога Севера (броски, замедление, кровотечение)
│       │   │   ├── ParryHandler.java                   # Логика Стиля Бога Воды (парирование ПКМ, отражение, контратака)
│       │   │   ├── SwordCombatHandler.java             # Общий боевой контроллер, атаки мечом, прогрессия стоек
│       │   │   ├── SwordStyle.java                     # Перечисление стоек (SWORD_GOD, WATER_GOD, NORTH_GOD)
│       │   │   ├── ToukiManager.java                   # Управление Боевой Аурой (Токи), расход маны, баффы
│       │   │   └── entity/
│       │   │       └── ThrownSwordEntity.java          # Снаряд метнутого клинка (Стиль Бога Севера)
│       │   ├── command/                                # Консольные команды
│       │   │   ├── AdminCommandService.java            # Сервис управления маной, рангами и сбросом данных
│       │   │   └── ModCommands.java                    # Регистрация команды `/mushoku`
│       │   ├── config/
│       │   │   └── MushokuConfig.java                  # Централизованный конфиг параметров мода
│       │   ├── crafting/                               # Логика модульной наковальни и рецептов
│       │   │   ├── ForgingResult.java                  # Структура данных результата ковки
│       │   │   ├── ManuscriptCloningRecipe.java        # Динамический рецепт копирования манускрипта (холст + чернила)
│       │   │   ├── ManuscriptExpandRecipe.java         # Динамический рецепт увеличения круга до 3x3 (манускрипт + 8 бумаги)
│       │   │   └── ModularAnvilMenu.java               # Контейнер меню модульной ковки
│       │   ├── data/                                   # Система данных, прокачки и формул
│       │   │   ├── MasteryCalculator.java              # Централизованные математические формулы баланса
│       │   │   ├── PlayerMasteryAccessor.java          # Duck-интерфейс доступа к данным игрока
│       │   │   ├── PlayerMasteryData.java              # Модель состояния игрока (Мана, Стили, Школы, Спеллы, Изученные круги)
│       │   │   └── PlayerMasteryProvider.java          # Статический провайдер и синхронизатор данных игрока
│       │   ├── effect/
│       │   │   └── BleedingEffect.java                 # Эффект кровотечения (MobEffect)
│       │   ├── entity/                                 # Игровые сущности и мобы
│       │   │   ├── AbstractCarcassEntity.java          # Базовый класс для разделываемых туш монстров
│       │   │   └── monster/
│       │   │       ├── DeadSabertoothWolfEntity.java   # Туша убитого саблезубого волка
│       │   │       └── SabertoothWolfEntity.java       # Саблезубый маг-волк с анимациями GeckoLib
│       │   ├── event/                                  # Слушатели жизненного цикла игры (Architectury Events)
│       │   │   ├── AdvancementHandler.java             # Выдача ачивок за прогресс и мастерство
│       │   │   ├── ModClientEvents.java                # Клиентский тик, рендер HUD, регистрация слоев
│       │   │   ├── ModEventBusEvents.java              # Регистрация атрибутов мобов
│       │   │   ├── ModGameEvents.java                  # Тик игрока, реген маны, синхронизация, урон
│       │   │   ├── PlayerInteractionBlocker.java       # Блокировка действий при касте/чтении
│       │   │   └── PlayerSessionHandler.java           # Обработка входа, возрождения и смены измерений
│       │   ├── init/                                   # Реестры Architectury DeferredRegister
│       │   │   ├── ModBlockEntities.java               # Реестр BlockEntityType
│       │   │   ├── ModBlocks.java                      # Реестр Block
│       │   │   ├── ModCreativeTabs.java                # Творческая вкладка мода
│       │   │   ├── ModDamageTypes.java                 # Реестр кастомных типов урона
│       │   │   ├── ModEffects.java                     # Реестр MobEffect
│       │   │   ├── ModEntities.java                    # Реестр EntityType
│       │   │   ├── ModItems.java                       # Реестр Item
│       │   │   ├── ModLootModifiers.java               # Реестр лут-модификаторов
│       │   │   ├── ModMenuTypes.java                   # Реестр MenuType
│       │   │   ├── ModRecipeSerializers.java           # Реестр RecipeSerializer
│       │   │   └── ModSpells.java                      # Реестр определений заклинаний
│       │   ├── item/                                   # Предметы
│       │   │   ├── AbstractScrollItem.java             # Базовый свиток обучения
│       │   │   ├── AncientManuscriptItem.java          # Древний манускрипт с подсказками узоров
│       │   │   ├── BlankCanvasItem.java                # Чистое полотно для рисования кругов
│       │   │   ├── HuntingKnifeItem.java               # Охотничий нож для разделки туш
│       │   │   ├── IMagicBook.java                     # Интерфейс книги заклинаний
│       │   │   ├── InscribedManuscriptItem.java        # Начертанный манускрипт для установки/связывания кругов
│       │   │   ├── MagicBookItem.java                  # Книги школ магии (Вода, Огонь, Земля, Ветер)
│       │   │   ├── ManaCrystalItem.java                # Кристаллы маны (малый/средний/великий), мгновенное восполнение маны
│       │   │   └── SwordGodScrollItem.java             # Свиток изучения Стиля Бога Меча
│       │   ├── magic/                                  # Магическая механика
│       │   │   ├── AirCushionManager.java              # Механика смягчения падения воздушной подушкой
│       │   │   ├── LearningManager.java                # Менеджер изучения спеллов по книгам и QTE
│       │   │   ├── MagicSchool.java                    # Перечисление магических школ (WATER, FIRE, EARTH, WIND)
│       │   │   ├── ProjectileSpellAction.java          # Реализация действия заклинания через снаряд
│       │   │   ├── ServerCastManager.java              # Серверный контроль произношения заклинаний, таймингов и Fizzle
│       │   │   ├── ServerChargeManager.java            # Механика Hold-to-Charge для безмолвных чар
│       │   │   ├── Spell.java                          # Доменная модель заклинания с паттерном Builder
│       │   │   ├── SpellAction.java                    # Функциональный интерфейс эффекта заклинания
│       │   │   ├── SpellRank.java                      # Ранги спеллов (BEGINNER -> DIVINE)
│       │   │   ├── circle/                             # Архитектура магических кругов и рун
│       │   │   │   ├── BarrierCircleType.java          # Логика круга территориального защитного барьера (Кеккай)
│       │   │   │   ├── CaptureCircleType.java          # Логика круга-ловушки запечатывания мобов в 3D-проекцию
│       │   │   │   ├── ClientMagicCircleState.java     # Клиентский кэш активного узора сида мира
│       │   │   │   ├── CrystallizationCircleType.java  # Логика трансмутационного круга кристаллизации маны в кристаллы
│       │   │   │   ├── DimensionalGateCircleType.java  # Логика межпространственных врат (3x3 многослойные, связь через средний кристалл маны)
│       │   │   │   ├── MagicCirclePattern.java         # 16x16 попиксельная модель и NBT сериализация
│       │   │   │   ├── MagicCirclePatterns.java        # Банк из 25 узоров 16x16, детерминированный shuffle по сиду мира
│       │   │   │   ├── MagicCircleRegistry.java        # Реестр типов кругов и процедурная идентификация по сиду мира (identify)
│       │   │   │   ├── MagicCircleType.java            # Базовый интерфейс типа магического круга
│       │   │   │   ├── OvergrowthCircleType.java       # Логика природного круга изобилия, ускорения роста культур и увлажнения почвы
│       │   │   │   ├── SanctuaryCircleType.java        # Логика сакрального круга святилища, регенерации HP и снятия дебаффов
│       │   │   │   ├── SoulAnchorCircleType.java       # Логика Круга Привязки Души (3x3 многослойный алтарь воскрешения)
│       │   │   │   ├── SoulRecallHandler.java          # Перехватчик смертельного урона LIVING_DEATH, спасение от гибели и возврат на алтарь
│       │   │   │   ├── SummoningCircleType.java        # Логика ритуального круга призыва запечатанного существа
│       │   │   │   └── TeleportCircleType.java         # Логика круга пространственной телепортации
│       │   │   ├── companion/                          # Система фамильяров и призванных спутников
│       │   │   │   └── SummonCompanionManager.java     # Управление лояльностью, целями ИИ, следованием и защитой владельца
│       │   │   └── entity/                             # Снаряды заклинаний (Waterball, Fireball, RockBullet, etc.)
│       │   ├── mixin/
│       │   │   └── PlayerEntityMixin.java              # Внедрение PlayerMasteryData в сущность игрока и NBT
│       │   └── network/                                # Сетевые пакеты Architectury (C2S и S2C)
│       │       ├── ModNetworking.java                  # Регистрация пейлоадов и методы отправки
│       │       ├── CastSpellPacket.java                # C2S: запрос игрока на каст заклинания
│       │       ├── CastStartedPacket.java              # S2C: старт анимации и инкантации каста
│       │       ├── ChangeStancePacket.java             # C2S: переключение боевой стойки
│       │       ├── ConsumeCanvasInkPacket.java         # C2S: списание мешочка чернил при рисовании на полотне
│       │       ├── InscribeCanvasPacket.java           # C2S: подтверждение начертания и создание готового манускрипта
│       │       ├── LearnSpellResultPacket.java         # S2C: итог попытки изучения заклинания
│       │       ├── LearnSpellSyncPacket.java           # S2C: синхронизация прогресса изучения
│       │       ├── MasteryGainedPacket.java            # S2C: уведомление об увеличении мастерства
│       │       ├── QteResultPacket.java                # C2S: ответ игрока на нажатие клавиши в QTE
│       │       ├── QteTriggerPacket.java               # S2C: вызов окна QTE на клиенте
│       │       ├── ReleaseChargePacket.java            # C2S: сброс/выпуск заряженного спелла
│       │       ├── SaveCanvasDraftPacket.java          # C2S: автосохранение текущего наброска в полотно при закрытии экрана
│       │       ├── StartChargePacket.java              # C2S: начало удержания для зарядки
│       │       ├── StartLearnSpellPacket.java          # C2S: начало чтения заклинания из книги
│       │       ├── SyncAirCushionPacket.java           # S2C: синхронизация состояния воздушной подушки
│       │       ├── SyncFullMasteryPacket.java          # S2C: полная синхронизация прогресса игрока
│       │       ├── SyncMagicCirclesPacket.java         # S2C: синхронизация активного узора сида мира
│       │       ├── SyncManaPacket.java                 # S2C: быстрая синхронизация маны
│       │       ├── SyncToukiPacket.java                # S2C: синхронизация ауры Токи
│       │       ├── ToggleAirCushionPacket.java         # C2S: включение/выключение воздушной подушки
│       │       ├── ToggleToukiPacket.java              # C2S: включение/выключение боевой ауры
│       │       └── TriggerParryPacket.java             # C2S: срабатывание парирования
│       └── resources/
│           ├── assets/mushokucraft/                    # Текстуры, модели, звуки, локализации (ru_ru, en_us)
│           ├── data/mushokucraft/                      # Рецепты, таблицы лута, достижения, типы урона
│           ├── mushokucraft-common.mixins.json         # Конфигурация SpongePowered Mixin
│           ├── mushokucraft.accesswidener              # Access Widener для доступа к скрытым полям Minecraft
│           └── pack.mcmeta                             # Манифест ресурспака
├── Fabric_1.21.1/                                      # Загрузчик Fabric
│   ├── build.gradle                                    # Спецификация Fabric-модуля и зависимостей
│   └── src/main/
│       ├── java/com/mushokucraft/fabric/
│       │   ├── FabricLootModifiers.java                # Регистрация лута в данжах для Fabric
│       │   ├── MushokuCraftFabric.java                 # Главный вход Fabric (ModInitializer)
│       │   └── client/
│       │       └── MushokuCraftFabricClient.java       # Клиентский вход Fabric (ClientModInitializer)
│       └── resources/
│           ├── fabric.mod.json                         # Манифест мода для Fabric
│           └── mushokucraft.mixins.json                # Mixin-конфигурация Fabric
├── NeoForge_1.21.1/                                    # Загрузчик NeoForge
│   ├── build.gradle                                    # Спецификация NeoForge-модуля и зависимостей
│   └── src/main/
│       ├── java/com/mushokucraft/neoforge/
│       │   ├── MushokuCraftNeoForge.java               # Главный вход NeoForge (@Mod)
│       │   ├── NeoForgeClientEvents.java               # Клиентские слушатели NeoForge Event Bus
│       │   └── loot/
│       │       ├── AddItemModifier.java                # GLM кодек для добавления книг в лут
│       │       └── NeoForgeLootModifiers.java          # Регистрация Global Loot Modifiers
│       └── resources/
│           ├── META-INF/neoforge.mods.toml             # Манифест мода для NeoForge
│           └── data/                                   # JSON-конфигурации модификаторов лута
├── build.gradle                                        # Корневой скрипт сборки Gradle
├── gradle.properties                                   # Конфигурация версий движка и зависимостей
├── settings.gradle                                     # Подключение подпроектов Common, Fabric, NeoForge
├── Mushoku Craft.md                                    # Оригинальный геймдизайн-документ мода
└── SCALING.md                                          # Правила масштабирования архитектуры и чистоты кода
```

---

## 3. Зависимости (Dependencies)

### 3.1. Внешние зависимости
- `minecraft` (`1.21.1`): Базовая платформа игры, среда исполнения логики и физики.
- `dev.architectury:architectury` (`13.0.6`): Кроссплатформенная абстракция поверх Forge/Fabric/NeoForge для эвентов, регистраций и сети.
- `software.bernie.geckolib:geckolib` (`4.6.6`): Мотор скелетных анимаций для мобов (`SabertoothWolfEntity`) и предметов.
- `dev.kosmx.player-anim:player-animation-lib` (`2.0.4+1.21.1`): Библиотека расширенных анимаций моделей игрока при ударах и кастах.
- `net.fabricmc:fabric-loader` (`0.16.2`) & `fabric-api` (`0.102.0+1.21.1`): Загрузчик и среда API для платформы Fabric.
- `net.neoforged:neoforge` (`21.1.235`): Загрузчик и окружение для платформы NeoForge.

### 3.2. Внутренние связи модулей
- `NeoForge_1.21.1` -> импортирует -> `Common`:
  - Вызывает `MushokuCraftCommon.init()` при конструировании `@Mod`.
  - Преобразует и запаковывает `Common` через `transformProductionNeoForge`.
  - Реализует платформенный код Global Loot Modifiers (`NeoForgeLootModifiers`, `AddItemModifier`).
- `Fabric_1.21.1` -> импортирует -> `Common`:
  - Вызывает `MushokuCraftCommon.init()` в `ModInitializer.onInitialize()`.
  - Преобразует и запаковывает `Common` через `transformProductionFabric`.
  - Регистрирует рендер-слои блоков и лут через Fabric API (`FabricLootModifiers`).
- `Common` -> изоляция:
  - **Никогда** не импортирует классы из `net.neoforged.*` или `net.fabricmc.*`. Все вызовы идут через прослойку `dev.architectury.*`.

---

## 4. Путь выполнения программы (Runtime Flow)

### 1. Инициализация (Bootstrapping)
1. **Запуск платформы:**
   - Fabric загружает `MushokuCraftFabric.onInitialize()`.
   - NeoForge инстанциирует `MushokuCraftNeoForge(IEventBus, ModContainer)`.
2. **Инициализация Common:**
   - Вызывается `MushokuCraftCommon.init()`.
   - Регистрируются блоки (`ModBlocks`), энтити (`ModEntities`), предметы (`ModItems`), эффекты (`ModEffects`), меню (`ModMenuTypes`), звуки и вкладки креатива.
   - Инициализируется сеть: `ModNetworking.register()` регистрирует 17 C2S и S2C пакетов через Architectury NetworkManager.
   - Подписываются игровые слушатели: `ModGameEvents`, `AdvancementHandler`, `PlayerInteractionBlocker`, `PlayerSessionHandler`, `ModCommands`.
   - Инициализируются менеджеры боевых и магических систем:
     - `SwordCombatHandler.register()`, `ParryHandler.register()`, `NorthGodHandler.register()`.
     - `LearningManager.register()`, `ServerCastManager.register()`, `ServerChargeManager.register()`.
3. **Клиентская инициализация (Client Bootstrap):**
   - Fabric: `MushokuCraftFabricClient.onInitializeClient()`.
   - NeoForge: проверка `FMLEnvironment.dist == Dist.CLIENT` в конструкторе мода.
   - Вызов `ModClientSetup.register()`: привязка моделей, рендереров сущностей и фабрик оверлеев.
   - Вызов `ModClientEvents.register()`: подписка на рендер HUD (`HudRenderEvent`) и тики клиента.
   - Регистрация биндов клавиш (`ModKeybindings.register()`) и диспетчеров инпута (`CombatInputHandler`, `MenuInputHandler`, `QteInputHandler`).

### 2. Основной игровой цикл (Game Loop & Event Flow)

#### А. Цикл каста заклинания (Spellcasting Flow)
```
[Игрок] ──(клавиша 'R')──> [SpellWheelScreen] ──(выбор)──> [ClientSpellState]
   │
[ЛКМ/ПКМ] ──> [CombatInputHandler] ──(CastSpellPacket C2S)──> [ModNetworking]
                                                                     │
                                                           [ServerCastManager]
                                                                     │
                      ┌──────────────────────────────────────────────┴─────────────────────────────┐
                      ▼                                                                            ▼
             [Обычный каст]                                                               [Безмолвные чары (100%)]
                      │                                                                            │
      - Проверка маны и кулдауна                                                      - Мгновенный каст (0 сек)
      - Наложение замедления на игрока                                                - Запуск ServerChargeManager (LMB hold)
      - Расчет тиков каста и точки осечки (Fizzle)                                     - Накопление маны каждую секунду
      - Вызов CastStartedPacket S2C                                                   - При отпускании: ReleaseChargePacket C2S
      - Клиент: показ IncantationOverlay                                               - Масштабирование снаряда (до 300%)
      - В контрольных точках: QteTriggerPacket S2C ──> [QTEOverlay]
      - Игрок жмет клавишу ──(QteResultPacket C2S)──> Проверка успеха
                      │
   [Успешный каст (remainingTicks == 0)]
      │
      ├── Списание маны (с учетом скидки от школы: MasteryCalculator)
      ├── Выполнение SpellAction (спавн WaterballEntity / Fireball / Icicle / Storm)
      ├── Начисление опыта заклинанию и школе магии
      └── Отправка SyncManaPacket и SyncFullMasteryPacket клиенту
```

#### Б. Цикл фехтования, стоек и парирования (Sword Combat Flow)
```
[Игрок] ──(клавиша 'X')──> [StanceWheelScreen] ──(ChangeStancePacket C2S)──> [SwordCombatHandler.changeStance]
                                                                                            │
                         ┌───────────────────────────────────┬──────────────────────────────┴──────────────────────────────┐
                         ▼                                   ▼                                                             ▼
                [Стиль Бога Меча]                   [Стиль Бога Воды]                                             [Стиль Бога Севера]
                         │                                   │                                                             │
        - Пассивный бонус к скорости и дальности   - ПКМ: ParryHandler.triggerParry (окно 10 тиков)       - Пассивное поглощение урона от падений
        - Ультимативный рывок (Longsword Light)    - При получении урона (LivingHurtEvent):                - Атаки накладывают замедление и кровотечение
        - Нанесение критов качает мастерство         * Если окно активно: урон аннулируется                 - ПКМ: метание клинка (ThrownSwordEntity)
                                                     * Снаряды отражаются во врага                          - Добивание лоу-хп врагов качает стиль
                                                     * Наносится контратака (Counterattack Damage)
                                                     * Начисляется мастерство Водного стиля
```

#### В. Боевая Аура — Токи (Battle Aura Flow)
1. Активируется через радиальное меню `SpellWheelScreen` ('R') -> `ToggleToukiPacket` C2S.
2. `ToukiManager.toggleTouki()`: валидация минимального мастерства (от 25%) и наличия маны.
3. Каждую секунду `ToukiManager.tick()` списывает ману: `drain = TOUKI_MANA_DRAIN_BASE + TOUKI_MANA_DRAIN_SCALING * mastery`.
4. Пока Токи активно, `SwordCombatHandler` применяет атрибутивные модификаторы `SwordStyle.applyToukiModifiers()`:
   - Прирост урона, прыжка, скорости передвижения и 100% сопротивления отбрасыванию.
5. При истощении маны Токи мгновенно выключается со звуковым сигналом `BEACON_DEACTIVATE`.

#### Г. Изучение магии по книгам (Book Learning Flow)
1. Игрок берет `MagicBookItem` в руки и нажимает ПКМ -> открывается `MagicBookScreen`.
2. Нажатие кнопки изучения шлет `StartLearnSpellPacket` C2S.
3. `LearningManager` регистрирует задачу в `learningTasks`, запускает таймер и генерирует серию QTE.
4. Клиент ловит `QteTriggerPacket`, отображает анимированный круг с сектором успеха в `QTEOverlay`.
5. Игрок нажимает соответствующую клавишу клавиатуры -> отправляет `QteResultPacket`.
6. При успехе списывается мана, игрок стреляет тестовым снарядом заклинания, начисляется базовое мастерство, клиенту шлется `LearnSpellResultPacket`.
7. При неудаче игрок запинается (fizzle), мана тратится, книга сохраняется в инвентаре.

#### Д. Бестиарий и разделка туш (Bestiary & Carcass Harvesting Flow)
1. При смерти `SabertoothWolfEntity` не исчезает мгновенно, а порождает `DeadSabertoothWolfEntity` (`AbstractCarcassEntity`).
2. Туша лежит в мире ограниченное время.
3. Игрок с `HuntingKnifeItem` взаимодействует с тушей -> списываются заряды использования (максимум 3).
4. Выпадают редкие алхимические и крафтовые ресурсы (`mage_meat`, `sabertooth_leather`), туша распадается.

#### Е. Система Магических Кругов и Пространственной Телепортации (Magic Circles & Teleportation Flow)
1. **Изучение схемы:**
   - Игрок открывает `AncientManuscriptItem` (ПКМ) -> `AncientManuscriptScreen`.
   - Экран визуализирует активный для данного сида мира узор 16x16 (один из 15 пресетов `MagicCirclePatterns`, синхронизированный через `SyncMagicCirclesPacket`).
   - Изучение открывает понимание типа круга (снимает обфускацию `§k` в описании начертанного манускрипта).
2. **Попиксельное начертание и копирование:**
   - Игрок берет `BlankCanvasItem` в одну руку и `Items.INK_SAC` во вторую -> открывается `MagicCanvasScreen`.
   - Игрок рисует пиксели узора (ЛКМ — чернила, ПКМ — ластик).
   - Расход чернил сбалансирован: 1 мешок на каждые 12 пикселей (`MAGIC_CIRCLE_INK_PIXELS_PER_SAC`). Прогресс начертания сохраняется в предмете при закрытии GUI.
   - Завершение начертания выдает `InscribedManuscriptItem` с NBT-данными узора.
   - Манускрипт можно копировать в сетке верстака (`ManuscriptCloningRecipe`: манускрипт + пустой холст + нужное число чернильных мешков).
   - Манускрипт можно расширить до ритуального размера 3x3 (`ManuscriptExpandRecipe`: манускрипт в центре + 8 бумаги вокруг).
3. **Установка, хитбокс 3х3, срезка подложки и сброс связей:**
   - Круг 1x1 ставится на любую твердую грань; круг 3x3 требует ровной твердой площадки 3x3 (`message.mushokucraft.requires_3x3_flat`).
   - Для круга 3х3 размещается полноценный мультиблочный конструкт 3х3 (`MagicCircleBlock.CirclePart`): центральный блок с BE и 8 окружающих невидимых частей с реальными хитбоксами (`Block.box(0, 0, 0, 16, 1, 16)`). Вся площадь 3х3 интерактивна для кликов, ножниц и ломания.
   - Ножницами (ПКМ с `Items.SHEARS`) с установленного круга можно срезать бумажную подложку: выпадает 3 бумаги (`Items.PAPER`), узор ложится прямо на грунт без подложки (`SHEARED = true`). При разрушении срезанного круга ничего не выпадает. Если подложка не срезана — выпадает исходный манускрипт.
   - **Сброс привязки:** При разрушении круга связь всегда сбрасывается (выпадающий манускрипт становится чистым без привязки). Также парный связанный круг немедленно сбрасывает свою связь при разрушении напарника (`onRemove`).
4. **Вливание маны, валидация цели и групповая телепортация:**
   - Стоя в круге (в радиусе 1.5 блока для 3x3 или 0.5 блока для 1x1), игрок зажимает Shift (приседание).
   - `MagicCircleBlockEntity.tick()` рассчитывает расстояние `d` и требуемую ману.
   - Ежетиково перекачивает ману игрока в круг с эффектами частиц и звукового резонанса.
   - При попытке вливания маны или срабатывания проверяется валидность целевой точки (`isDestinationValid`): если целевой круг разрушен или перемещен, телепортация отменяется, привязка круга автоматически сбрасывается с сообщением `circle_target_missing`.
   - При успешном срабатывании телепортируются **все** сущности (игроки, мобы, животные, наездники на маунтах), находящиеся в зоне круга, с сохранением их относительного взаимного расположения.

#### Ж. Система Запечатывания и Призыва Фамильяров (Capture & Summoning Circles Flow)
1. **Запечатывание сущности (Capture Trap):**
   - Установка круга типа `mushokucraft:capture` на поверхности.
   - Игрок наполняет круг маной через Shift (требуется от `CAPTURE_CIRCLE_BASE_MANA` = 50 маны).
   - Заряженный круг действует как магическая ловушка: наступающие живые существа получают периодический магический урон (`CAPTURE_CIRCLE_DAMAGE_RATE` = 4 HP), расходующий ману круга (`CAPTURE_CIRCLE_MANA_PER_DAMAGE` = 2.5 маны за 1 HP урона).
   - Если урон круга смертелен для моба, моб не погибает, а его сущность и душа **запечатываются** в круг (`setCapturedMob`): сохраняются entityId, compound NBT-теги, имя и Max HP.
   - Над кругом появляется и непрерывно вращается уменьшенная голографическая 3D-проекция запечатанного существа (`MagicCircleBlockEntityRenderer`).
2. **Перенос проекции в манускрипт:**
   - Игрок разрушает круг (или кликает ПКМ с пустым `inscribed_manuscript` в руке) — манускрипт сохраняет проекцию сущности внутри предмета (`hasCapturedMob = true`).
   - Во всплывающей подсказке манускрипта отображается имя запечатанного моба, его здоровье и статус готовности к призыву.
3. **Ритуал Призыва (Summoning Circle):**
   - Игрок устанавливает круг призыва `mushokucraft:summoning` и кликает ПКМ манускриптом с душой существа -> 3D-проекция переносится в круг.
   - Необходимое количество маны рассчитывается динамически по формуле: `SUMMON_BASE_MANA + SUMMON_MANA_PER_HP * maxHp`.
   - Игрок вливает ману через Shift: при 100% срабатывает ритуал призыва (звуки `EVOKER_CAST_SPELL`, `BEACON_ACTIVATE`, вспышка `FLASH`, салюты `FIREWORK` и частицы душ `SOUL`).
   - Проекция материализуется в реального моба с полным здоровьем, а манускрипт освобождается.
4. **ИИ Верного Фамильяра (`SummonCompanionManager`):**
   - Призванное существо становится лояльным компаньоном призвавшего игрока (хозяина).
   - Защита от дружественного огня: хозяин и фамильяр не наносят друг другу урон; моб не агрится на хозяина (`HurtByTargetGoal` игнорирует хозяина).
   - `FamiliarFollowGoal`: моб неотступно следует за хозяином на расстоянии от 3 до 10 блоков; при отдалении свыше 24 блоков телепортируется к хозяину с портальными искрами.
   - `FamiliarDefendOwnerGoal`: моб немедленно атакует любого врага, атаковавшего хозяина.
   - `FamiliarAttackOwnerTargetGoal`: моб фокусирует атаки на цели, которую ударил хозяин.

#### З. Система Кристаллизации и Кристаллов Маны (Crystallization & Mana Crystals Flow)
1. **Назначение:** Портативное хранение и мгновенное восполнение запаса маны в бою или при дальних странствиях без необходимости длительного ожидания естественного регена.
2. **Типы кристаллов (`ManaCrystalItem`):**
   - **Малый кристалл маны (`small_mana_crystal`):** Восстанавливает `+50` маны при использовании (ПКМ). Рецепт: осколок аметиста или кварц + 50 влитой маны.
   - **Средний кристалл маны (`medium_mana_crystal`):** Восстанавливает `+150` маны при использовании (ПКМ). Рецепт: блок аметиста, лазурит или блок лазурита + 150 влитой маны.
   - **Великий кристалл маны (`large_mana_crystal`):** Восстанавливает `+400` маны при использовании (ПКМ). Рецепт: алмаз или изумруд + 400 влитой маны.
3. **Процесс кристаллизации (`CrystallizationCircleType`):**
   - Игрок чертит и размещает круг типа `mushokucraft:crystallization` (определяемый детерминированно по сиду мира среди 25 узоров).
   - Игрок бросает минерал на круг в виде упавшего предмета (`ItemEntity`). Круг распознает предмет и начинает излучать резонирующие электрические искры (`ELECTRIC_SPARK`).
   - Игрок встает на круг и удерживает Shift для насыщения минерала чистой маной.
   - При достижении требуемого объема маны минерал расходуется, а на его месте кристаллизуется соответствующий кристалл маны с перезвоном аметиста и частицами `END_ROD`.
   - Защита от перерасхода: если запас маны полон, использование кристалла блокируется с уведомлением в экшн-баре.

#### И. Система Святилища и Исцеления (Sanctuary & Restoration Circle Flow)
1. **Назначение:** Защитная сакральная зона лагеря или базы, исцеляющая раненых союзников и очищающая их от ядов, проклятий и негативных эффектов.
2. **Параметры и масштабирование (`SanctuaryCircleType`):**
   - Круг 1x1: радиус действия 5 блоков, емкость маны 250.
   - Круг 3x3: радиус действия 12 блоков, емкость маны 750.
3. **Механика работы:**
   - Игрок наполняет круг маной через Shift (до максимальной емкости).
   - Пока в круге есть мана, каждую секунду проводится триаж всех дружественных сущностей (игроки, прирученные питомцы, призванные фамильяры `SummonCompanionManager`, мирные животные и жители):
     - **Очищение дебаффов:** Снимает любые вредоносные эффекты (отравление, иссушение, слабость, слепота, кровотечение `BleedingEffect`). Стоимость: 5 маны за снятый дебафф.
     - **Регенерация HP:** Восстанавливает до 2 HP/сек всем раненым союзникам. Стоимость: 1.5 маны за 1 единицу HP.
     - Эффекты: частицы `HEART`, `HAPPY_VILLAGER`, `WAX_OFF`, звук `AMETHYST_BLOCK_RESONATE`.

#### К. Система Природного Изобилия и Земледелия (Overgrowth & Fertility Circle Flow)
1. **Назначение:** Магический агрономический круг Земли и Воды для автоматизации и ускорения фермерства.
2. **Параметры и масштабирование (`OvergrowthCircleType`):**
   - Круг 1x1: радиус действия 6 блоков, емкость маны 250.
   - Круг 3x3: радиус действия 16 блоков, емкость маны 750.
3. **Механика работы:**
   - Игрок наполняет круг маной через Shift.
   - Каждые 15 тиков круг совершает циклы роста случайных блоков в радиусе (расход: 2.5 маны за рост):
     - **Культуры и саженцы:** Ускоряет пшеницу, морковь, картофель, свеклу, стебли тыкв/арбузов, кусты ягод и саженцы деревьев через `BonemealableBlock`.
     - **Тростник, бамбук, кактусы и адский нарост:** Симулирует ускоренные случайные тики созревания.
     - **Увлажнение почвы:** Автоматически превращает сухую пашню `Blocks.FARMLAND` в увлажненную (влага = 7) без необходимости источника воды рядом (расход: 1 мана).
     - Эффекты: частицы `SPORE_BLOSSOM_AIR`, `HAPPY_VILLAGER`, `COMPOSTER`, звук `BONE_MEAL_USE`.

#### Л. Архитектура Многослойных Магических Кругов и Межпространственных Врат (Multi-Layer Magic Circles & Dimensional Gate Flow)
1. **Назначение:** Реализация продвинутой аниме-системы концентрических и парящих многослойных магических кругов (Mushoku Tensei, Frieren), а также высших Врат Перемещения между измерениями.
2. **Ограничение масштаба структуры:**
   - Многослойные построения доступны **исключительно для кругов масштаба 3x3** (`size == 3`).
   - Попытка наложить слой на круг 1x1 пресекается с уведомлением: `"Многослойные структуры можно создавать только на кругах 3x3!"`.
3. **Механика управления слоями (`MagicCircleBlockEntity.CircleLayer`):**
   - **Наложение слоя:** Клик ПКМ (или Shift+ПКМ) манускриптом [InscribedManuscriptItem](file:///d:/Projects/gamedev/minecraft/MushokuCraft/Common/src/main/java/com/mushokucraft/item/InscribedManuscriptItem.java) по центру 3x3 круга добавляет его как парящий слой (до 2 дополнительных слоев, итого до 3 уровней структуры).
   - **Динамическое расширение слоев в мире:**
     - Если парящий слой был наложен в компактном масштабе 1x1, клик по кругу с 1 чистым полотном `ModItems.BLANK_CANVAS` или 8 листами `Items.PAPER` мгновенно расширяет верхний слой до полного масштаба 3x3.
     - Радиус рендера слоя плавно трансформируется от компактного кольца (радиус 0.5) до монументального парящего ореола (радиус 1.5).
   - **Демонтаж слоя:** Клик пустой рукой с зажатым Shift по центру 3x3 круга аккуратно снимает верхний слой и возвращает игроку готовый манускрипт с сохранением исходного/расширенного размера.
   - **Синергия эффектов:** Все наложенные слои исполняют свой серверный тик `onServerTick` параллельно базовому кругу (например, Врата + Святилище = лечение команды при перемещении; Барьер + Святилище = укрепленная крепость с лечением).
4. **Межпространственные Врата (`DimensionalGateCircleType`):**
   - **Идентификатор:** `mushokucraft:dimensional_gate`.
   - **Обязательная многослойность и саморезонанс (`isDimensionalGate`):** Врата не могут функционировать как плоский одиночный круг и **резонируют исключительно сами с собой**:
     1. Масштаб 3x3 на земле (`size == 3`).
     2. Основание обязано быть типом `mushokucraft:dimensional_gate`.
     3. Минимум один наложенный парящий слой (манускрипт).
     4. Все наложенные слои **обязаны быть также типом `mushokucraft:dimensional_gate`** (наложение слоев других типов на Врата или Врат на обычные круги строго заблокировано).
     5. Все наложенные слои должны быть расширены до 3x3 чистым полотном или бумагой.
     - При несоблюдении условий врата остаются спящими, а игроку выводятся подсказки о необходимости наложения и расширения слоев.
   - **Связывание через Средний Кристалл Маны (`ModItems.MEDIUM_MANA_CRYSTAL`):**
     - ПКМ средним кристаллом по первым стабилизированным вратам: кристалл запечатлевает резонанс (`GateX`, `GateY`, `GateZ`, `GateDim`) и обретает зачарованное свечение.
     - ПКМ настроенным кристаллом по вторым стабилизированным многослойным вратам (Overworld, Nether, End, мод-дименшены): связывает оба разлома, поглощая 1 кристалл.
   - **Стоимость телепорта:** Фиксированная (500 маны по умолчанию в `MushokuConfig.MAGIC_CIRCLE_DIMENSIONAL_GATE_MANA`), пробивающая ткань пространства независимо от расстояния.
   - **Перемещение сущностей:** Все стоящие на платформе 3x3 игроки, мобы, питомцы и транспорт бесшовно перемещаются через `DimensionTransition` с обнулением урона от падения.
5. **Суммирование маны и одновременная активация слоев (Mana Summation & Simultaneous Triggering):**
   - **Суммирование требуемой маны (`calculateTotalRequiredMana`):**
     - Полная стоимость активации многослойного круга рассчитывается как сумма маны основания и всех наложенных парящих слоев: $M_{total} = M_{base} + \sum M_{layer}$ (с учетом масштаба каждого слоя 1x1 или 3x3, расстояния для телепортации, рецепта минерала для кристаллизации и HP моба для призыва).
     - Пример 1: Основание Врат Перемещения (500 маны) + наложенный второй слой Врат 3x3 (500 маны) = 1000 суммарной маны для активации (или 1500 при 3 слоях Врат).
     - Пример 2: Основание Святилища (750 маны) + наложенный Барьер 3x3 (1000 маны) = 1750 суммарной маны для одновременного развертывания барьера и ауры исцеления.
   - **Канализация и синхронные эффекты (`onChannelTick`):**
     - При удержании Shift игрок вливает ману в единый резервуар структуры. Все типы кругов основания и слоев одновременно генерируют соответствующие частицы канализации (портальные разломы, золотые искры барьера, руны святилища).
   - **Синхронный запуск всех слоев (`triggerAllLayers`):**
     - При достижении `currentMana >= requiredMana` мгновенно активируются все эффекты структуры в строго детерминированном порядке:
       1. *Мгновенные эффекты (Instant):* Кристаллизация минералов, призыв запечатанных фамильяров, захват душ.
       2. *Перманентные структуры (Persistent):* Возведение барьера, инициация ауры святилища или природного изобилия.
       3. *Пространственный транзит (Transit):* Межпространственные врата или телепортация сквозь пространство.
     - Расход маны: стоимость мгновенных и транзитных эффектов вычитается, а оставшаяся мана сохраняется в резервуаре для подпитки прочности барьера и исцеления святилища.
6. **Визуальное исполнение (High-End Anime Visuals):**
   - **Концентрические границы основания:** Земляной круг 3x3 обрамлен двойным светящимся круговым кольцом (радиусы 1.46 и 1.34) с 8 радиальными кардинальными и диагональными засечками.
   - **Парящие аниме-слои с геометрическими звездами:**
     - Первый слой парит на Y = 0.38, вращается по часовой стрелке, содержит внутреннюю светящуюся гексаграмму (два пересекающихся треугольника) и двойной обод с засечками.
     - Второй слой парит на Y = 0.72, вращается против часовой стрелки с октаграммой (два пересекающихся квадрата).
   - **Межпространственный разлом и 3D-астролябия:**
     - 6 пересекающихся вертикальных полупрозрачных вихревых плоскостей высотой 2.8 блоков.
     - Пульсирующее ядро разлома на высоте Y = 0.55 между слоями.
     - Двойная 3D-гироскопическая астролябия: внешнее кольцо наклонено на +28° по оси X, внутреннее на -28° по оси Z, вращаясь в противоположные стороны.

### 3. Завершение работы и очистка (Graceful Shutdown)
- `PlayerEvent.PLAYER_QUIT`: удаляет активные сессии из `ServerCastManager.activeCasts`, `LearningManager.learningTasks`, `CombatInputHandler` и очередей комбо.
- NBT Сохранение: данные `PlayerMasteryData` сериализуются через `PlayerEntityMixin` в тег `MushokuCraftMastery` при сохранении сущности игрока на сервере.

---

## 5. Каталог файлов и ключевых функций

### `Common/src/main/java/com/mushokucraft/MushokuCraftCommon.java`
- **Роль:** Главная кроссплатформенная точка инициализации мода для всех загрузчиков.
- **Ключевые сущности:**
  - `MOD_ID: String` — строковый идентификатор мода `"mushokucraft"`.
  - `LOGGER: Logger` — глобальный логгер мода.
  - `init(): void` — регистрирует все компоненты реестров, сеть, слушатели эвентов, боевку и магию.

### `Common/src/main/java/com/mushokucraft/config/MushokuConfig.java`
- **Роль:** Централизованное хранилище всех настраиваемых числовых параметров баланса, урона, таймингов и маны.
- **Ключевые сущности:**
  - `WATER_SLICE_*`, `ICICLE_BREAK_*`, `CUMULONIMBUS_*` — числовые характеристики заклинаний.
  - `DEFAULT_MANA`, `DEFAULT_MAX_MANA`, `DEFAULT_MANA_REGEN_RATE` — базовые параметры маны игрока.
  - `TOUKI_*` — коэффициенты расхода маны и баффов боевой ауры Токи.
  - `PARRY_*`, `COUNTER_ATTACK_DAMAGE` — параметры окна парирования стиля Бога Воды.
  - `ConfigValue<T>` — обертка для типобезопасного чтения конфигурационных значений.

### `Common/src/main/java/com/mushokucraft/data/PlayerMasteryData.java`
- **Роль:** Модель данных игрока, хранящая состояние маны, школ магии, заклинаний, стоек меча и кулдаунов.
- **Ключевые сущности:**
  - `consumeMana(amount: float): boolean` — атомарная проверка и списание маны.
  - `regenMana(amount: float): void` — восполнение маны до предела `maxMana`.
  - `getSchoolMastery(school: MagicSchool): float` — получение уровня школы [0.0 - 1.0].
  - `addSchoolMastery(school: MagicSchool, amount: float): void` — прокачка школы магии.
  - `getSpellMastery(spellId: ResourceLocation): float` — получение уровня заклинания.
  - `addSpellMastery(spellId: ResourceLocation, amount: float): void` — прокачка конкретного заклинания.
  - `getActiveStance(): SwordStyle` — возвращает текущую активированную стойку фехтования.
  - `setActiveStance(stance: SwordStyle): void` — переключение текущей боевой стойки.
  - `serializeNBT(provider: HolderLookup.Provider): CompoundTag` — полная сериализация состояния в NBT.
  - `deserializeNBT(provider: HolderLookup.Provider, tag: CompoundTag): void` — восстановление состояния из NBT.

### `Common/src/main/java/com/mushokucraft/data/MasteryCalculator.java`
- **Роль:** Централизованный калькулятор математических формул прогрессии и масштабирования.
- **Ключевые сущности:**
  - `calculateEffectiveManaCost(baseCost: float, schoolMastery: float): float` — вычисляет скидку на ману от мастерства.
  - `calculateSpellSuccessChance(spellMastery: float, schoolMastery: float): double` — шанс успешного каста.
  - `calculateQteSpeedModifier(schoolMastery: float, tier: int): float` — замедление полосы QTE при высоком мастерстве.
  - `calculateQteTargetSize(tier: int): float` — базовая ширина зоны успеха QTE в зависимости от ранга.
  - `calculateToukiDamage(mastery: float): double` — прибавка к урону от Токи.
  - `calculateToukiSpeed(mastery: float): double` — множитель скорости под Токи.

### `Common/src/main/java/com/mushokucraft/data/PlayerMasteryProvider.java` & `PlayerMasteryAccessor.java`
- **Роль:** Duck-typing провайдер доступа к данным игрока без платформозависимых Attachment API.
- **Ключевые сущности:**
  - `PlayerMasteryProvider.get(player: Player): PlayerMasteryData` — извлечение состояния из экземпляра игрока.
  - `PlayerMasteryProvider.sync(player: Player): void` — синхронизация данных с сервером на клиент.

### `Common/src/main/java/com/mushokucraft/magic/ServerCastManager.java`
- **Роль:** Серверный контроллер жизненного цикла каста заклинаний (таймеры, QTE-точки, осечки, каст спеллов).
- **Ключевые сущности:**
  - `register(): void` — подписка на `TickEvent.SERVER_POST` и `PlayerEvent.PLAYER_QUIT`.
  - `startCast(player: ServerPlayer, spell: Spell, castTime: int, fizzleTick: int): void` — старт каста заклинания.
  - `handleQteResult(player: ServerPlayer, result: int): void` — обработка результата мини-игры QTE (0 - промах, 1 - успех, 2 - идеально).

### `Common/src/main/java/com/mushokucraft/magic/ServerChargeManager.java`
- **Роль:** Обработка механики Hold-to-Charge для заклинаний со 100% мастерством (безмолвные мгновенные чары).
- **Ключевые сущности:**
  - `startCharge(player: ServerPlayer, spell: Spell): void` — начало удержания для накопления дополнительной маны.
  - `releaseCharge(player: ServerPlayer): void` — выпуск усиленного снаряда с увеличенным масштабом и уроном.

### `Common/src/main/java/com/mushokucraft/magic/LearningManager.java`
- **Роль:** Менеджер изучения заклинаний по книгам через чтение и серию QTE проверок.
- **Ключевые сущности:**
  - `startLearning(player: ServerPlayer, spellId: ResourceLocation): void` — запуск процесса обучения.
  - `handleQteResult(player: ServerPlayer, result: int): void` — обработка нажатий в процессе чтения книги.

### `Common/src/main/java/com/mushokucraft/magic/AirCushionManager.java`
- **Роль:** Логика защитной воздушной подушки, плавно гасящей скорость падения и создающей временные блоки.
- **Ключевые сущности:**
  - `toggle(player: ServerPlayer): void` — включение/выключение режима подушки.
  - `tick(player: ServerPlayer, data: PlayerMasteryData): boolean` — периодическое сканирование высоты и расход маны.

### `Common/src/main/java/com/mushokucraft/combat/SwordCombatHandler.java`
- **Роль:** Общий контроллер владения мечом, применение модификаторов стоек и начисление боевого мастерства.
- **Ключевые сущности:**
  - `changeStance(player: Player, stanceId: String): void` — смена боевой стойки игрока.
  - `register(): void` — регистрация слушателей тиков, атак и выхода игроков.

### `Common/src/main/java/com/mushokucraft/combat/ParryHandler.java`
- **Роль:** Реализация механики парирования на ПКМ для Стиля Бога Воды.
- **Ключевые сущности:**
  - `triggerParry(player: ServerPlayer): void` — активация временного окна парирования с наложением кулдауна.
  - `register(): void` — перехват входящего урона в `EntityEvent.LIVING_HURT`, отражение снарядов и контратака.

### `Common/src/main/java/com/mushokucraft/combat/NorthGodHandler.java`
- **Роль:** Обработчик трюков Стиля Бога Севера (бросок меча, наложение дебаффов, добивания).
- **Ключевые сущности:**
  - `throwSword(player: ServerPlayer): void` — метание клинка в виде сущности `ThrownSwordEntity`.
  - `register(): void` — перехват ударов для наложения кровотечения и замедления.

### `Common/src/main/java/com/mushokucraft/combat/ToukiManager.java`
- **Роль:** Управление состоянием боевой ауры (Токи), ежесекундный расход маны и наложение баффов.
- **Ключевые сущности:**
  - `toggleTouki(player: ServerPlayer): void` — переключение ауры Токи.
  - `tick(player: ServerPlayer, data: PlayerMasteryData): boolean` — расчет расхода маны за секунду.

### `Common/src/main/java/com/mushokucraft/network/ModNetworking.java`
- **Роль:** Центральный хаб сетевых пакетов мода на базе Architectury NetworkManager.
- **Ключевые сущности:**
  - `register(): void` — регистрация всех C2S и S2C пейлоадов и их обработчиков.
  - `sendToServer(payload: CustomPacketPayload): void` — отправка клиентом на сервер.
  - `sendToPlayer(player: ServerPlayer, payload: CustomPacketPayload): void` — отправка конкретному игроку.
  - `sendToTracking(entity: Entity, payload: CustomPacketPayload): void` — широковещательная рассылка игрокам в радиусе.

### `Common/src/main/java/com/mushokucraft/client/gui/SpellWheelScreen.java` & `StanceWheelScreen.java`
- **Роль:** Интерактивные круговые (радиальные) меню на клавиши 'R' и 'X' для выбора магии и стоек.
- **Ключевые сущности:**
  - `render(guiGraphics: GuiGraphics, mouseX: int, mouseY: int, partialTick: float): void` — рендер секторов, иконок и подсветки.
  - `mouseClicked(mouseX: double, mouseY: double, button: int): boolean` — выбор активного сектора.

### `Common/src/main/java/com/mushokucraft/client/gui/QTEOverlay.java`
- **Роль:** Рендерер всплывающего QTE-события во время каста и чтения книг.
- **Ключевые сущности:**
  - `render(guiGraphics: GuiGraphics, partialTick: float): void` — отрисовка круговой шкалы тайминга и требуемой клавиши.

### `Common/src/main/java/com/mushokucraft/client/hud/ManaHudManager.java`
- **Роль:** Менеджер отображения индикатора маны на экране игрока.
- **Ключевые сущности:**
  - `render(guiGraphics: GuiGraphics, partialTick: float): void` — отрисовка классического, дугового или гибридного индикатора маны.

### `Common/src/main/java/com/mushokucraft/magic/circle/MagicCircleRegistry.java` & `MagicCirclePatterns.java`
- **Роль:** Реестр магических кругов, библиотека из 15 пресетов узоров 16x16 и сидо-зависимый селектор активных узоров.
- **Ключевые сущности:**
  - `identify(level: Level, pattern: MagicCirclePattern): MagicCircleType` — попиксельная 1:1 сверка начертанного узора.
  - `getTeleportPatternForSeed(seed: long): MagicCirclePattern` — детерминированный выбор активной схемы телепортации.

### `Common/src/main/java/com/mushokucraft/block/MagicCircleBlock.java` & `MagicCircleBlockEntity.java`
- **Роль:** Физический 1-пиксельный блок установленного магического круга на полу и его блок-сущность.
- **Ключевые сущности:**
  - `tick(level, pos, state, be): void` — перекачка маны из крадущегося игрока (Shift), частицы и вызов активации при полном заряде.
  - `setLinked(pos: BlockPos, dim: ResourceLocation): void` — сохранение пространственной связки двух кругов.

### `Common/src/main/java/com/mushokucraft/client/gui/MagicCanvasScreen.java` & `AncientManuscriptScreen.java`
- **Роль:** Клиентские интерфейсы попиксельного начертания на полотне и чтения древнего манускрипта со схемой.
- **Ключевые сущности:**
  - `MagicCanvasScreen`: интерактивная сетка 16x16, поддержка drag-рисования, динамический учет и валидация чернил.
  - `AncientManuscriptScreen`: визуализация активной схемы 1:1, описание механики и формул расстояния.

### `Common/src/main/java/com/mushokucraft/magic/circle/CaptureCircleType.java` & `SummoningCircleType.java`
- **Роль:** Реализация типов кругов запечатывания мобов в голограмму и ритуала материализации фамильяра.
- **Ключевые сущности:**
  - `CaptureCircleType.onServerTick()` — нанесение магического урона мобам, захват сущности при гибели в NBT и создание 3D-проекции.
  - `SummoningCircleType.onTrigger()` — воскрешение сущности со сбросом UUID, исцелением, регистрацией лояльности в `SummonCompanionManager`.

### `Common/src/main/java/com/mushokucraft/magic/companion/SummonCompanionManager.java`
- **Роль:** Комплексная подсистема управления призванными фамильярами и боевыми спутниками.
- **Ключевые сущности:**
  - `makeCompanion(entity: Entity, owner: Player): void` — привязка UUID владельца и внедрение динамических целей ИИ (`GoalSelector`).
  - `FamiliarFollowGoal` — постоянное следование за игроком и телепортация при дистанции > 24 блоков.
  - `FamiliarDefendOwnerGoal` & `FamiliarAttackOwnerTargetGoal` — защита хозяина и фокус атаки на целях хозяина.
  - `init(): void` — подписка на `EntityEvent.LIVING_HURT` для блокировки дружественного огня и `EntityEvent.ADD` для восстановления целей после загрузки чанков.

### `Common/src/main/java/com/mushokucraft/magic/circle/OvergrowthCircleType.java`
- **Роль:** Реализация природного круга изобилия (`mushokucraft:overgrowth`) для ускорения роста культур и увлажнения почвы.
- **Ключевые сущности:**
  - `onServerTick()` — сканирование столбцов радиуса сверху вниз, сбор исключительно культур, саженцев и грядок.
  - `isEligiblePlant()` — строгий черный список, исключающий траву (`GRASS_BLOCK`, `SHORT_GRASS`, `TALL_GRASS`, сорняки, мох), предотвращая зарастание газона сорняками вместо ускорения урожая.
  - `growSinglePlant()` — точечный рост `CropBlock`, `StemBlock` (с появлением тыкв/арбузов при максимальной зрелости стебля), тростника, кактуса, адского нароста, какао, ягод и саженцев, а также автоматическое увлажнение грядок (`MOISTURE = 7`).

### `Common/src/main/java/com/mushokucraft/magic/circle/DimensionalGateCircleType.java`
- **Роль:** Реализация высших древних межпространственных врат (`mushokucraft:dimensional_gate`).
- **Ключевые сущности:**
  - `isDimensionalGate()`: строгая валидация структуры (база 3x3, минимум 1 дополнительный слой, все слои — исключительно узоры врат, все слои расширены до 3x3 полотном/бумагой).
  - Сонастройка средним кристаллом маны (`ManaCrystalItem`): запись частоты врат в NBT кристалла и взаимное мгновенное связывание сквозь любые измерения.
  - Межпространственный транзит (`teleportEntities`): мгновенный перенос сущностей в целевое измерение с подгрузкой чанков и вихревыми спецэффектами `REVERSE_PORTAL` и звуком `PORTAL_TRAVEL`.

### `Common/src/main/java/com/mushokucraft/magic/circle/SoulAnchorCircleType.java` & `SoulRecallHandler.java`
- **Роль:** Реализация ритуала сохранения души и возрождения на алтаре (`mushokucraft:soul_anchor`).
- **Ключевые сущности:**
  - `isSoulAnchor()`: строгая валидация сакральной 3-слойной структуры (основа 3x3 «Привязка Души», второй слой 3x3 «Привязка Души», третий слой 3x3 «Запечатывание» (`mushokucraft:capture`), все слои расширены до 3x3).
  - Привязка души Великим кристаллом маны (`large_mana_crystal`): запечатление координат алтаря в `PlayerMasteryData` и UUID игрока в `MagicCircleBlockEntity`.
  - `SoulRecallHandler.register()`: перехват события `EntityEvent.LIVING_DEATH`. При гибели привязанного игрока смерть отменяется (`EventResult.interruptFalse()`), предотвращая выпадение инвентаря и экран смерти. С алтаря списывается мана (800 ед.), снимаются дебаффы, восстанавливается здоровье, накладываются защитные эффекты (Resistance III, Fire Resistance, Regeneration II, Absorption II), а игрок мгновенно телепортируется в центр алтаря.

### `Fabric_1.21.1/src/main/java/com/mushokucraft/fabric/MushokuCraftFabric.java`
- **Роль:** Точка входа для Fabric Loader.
- **Ключевые сущности:**
  - `onInitialize(): void` — вызов `MushokuCraftCommon.init()` и `FabricLootModifiers.register()`.

### `NeoForge_1.21.1/src/main/java/com/mushokucraft/neoforge/MushokuCraftNeoForge.java`
- **Роль:** Точка входа для NeoForge с поддержкой EventBus.
- **Ключевые сущности:**
  - `MushokuCraftNeoForge(modEventBus: IEventBus, modContainer: ModContainer)` — запуск ядра Common, регистрация лут-модификаторов и клиентских обработчиков.

---

- **2026-09-26 (v1.9):** Выделена отдельная вкладка творческого режима «Магические Круги» (`ModCreativeTabs.MAGIC_CIRCLES_TAB`):
  - Создана специализированная вкладка `magic_circles_tab` (`itemGroup.mushokucraft.magic_circles`) с иконкой начертанного манускрипта.
  - В новую вкладку перенесены:
    - Все схемы и чертежи Древних манускриптов для всех 9 типов кругов (`AncientManuscriptItem.createForType`).
    - Готовые предварительно начертанные манускрипты 1x1 для всех 9 типов кругов (`InscribedManuscriptItem.create(pattern, id, 1)`).
    - Готовые предварительно начертанные манускрипты 3x3 для всех 9 типов кругов (`InscribedManuscriptItem.create(pattern, id, 3)`).
    - Базовые инструменты: чистый пергамент, блок магического круга, кристаллы маны.
  - Из основной вкладки мода (`MUSHOKU_TAB`) полностью удалены все древние манускрипты и специализированные свитки для чистоты интерфейса.
  - Подсказки свитков в креативном режиме: игроки в режиме Творчества теперь видят точный тип круга в подсказке золотым текстом без необходимости предварительного ручного изучения в выживании.
  - Авто-синхронизация узора при размещении: начертанные манускрипты из креативного меню при установке в мир автоматически адаптируют рисунок рун под конкретный сид мира (`sl.getSeed()`).
- **2026-09-26 (v1.8):** Реализован Круг Привязки Души (`SoulAnchorCircleType`, `SoulRecallHandler`, `large_mana_crystal`):
  - Трёхслойная сакральная архитектура 3x3: основа — 3x3 Привязка Души, второй слой — 3x3 Привязка Души, третий слой — 3x3 Запечатывание (`mushokucraft:capture`), запечатывающий душу в алтаре.
  - Привязка Великим кристаллом маны: ПКМ кристаллом по готовому 3-слойному алтарю привязывает душу игрока, сохраняя координаты алтаря в NBT игрока и UUID игрока в блоке.
  - Спасение от гибели без потери лута: при смертельном уроне в любом измерении `LIVING_DEATH` перехватывается, смерть отменяется, восстанавливается полное здоровье, накладывается бафф-щит и игрок телепортируется к алтарю с полным сохранением инвентаря.
  - Эпический 3D-визуал: вращающийся столб синего пламени душ, парящее ядро души, золотые руны запечатывания и вращающееся кольцо нимба над алтарем.
- **2026-09-26 (v1.8):** Исправлено связывание телепортационных кругов и устранена ошибка несовпадения ResourceLocation:
  - Устранено ложное срабатывание сообщения «Многослойные структуры можно создавать только на кругах 3x3»: причина заключалась в расхождении путей идентификатора (`"teleport"` против зарегистрированного в `TeleportCircleType.ID` пути `"teleportation"`).
  - Введен универсальный хелпер `InscribedManuscriptItem.isTeleportType(ResourceLocation id)` с кросс-совместимостью для обоих вариантов (`"teleport"` и `"teleportation"`).
  - Добавлена динамическая авто-идентификация типа круга `getOrIdentifyCircleTypeId(stack, level)` для манускриптов и кругов, у которых тег типа отсутствовал до первого тика.
  - Исправлены все проверки связывания и размещения в `InscribedManuscriptItem.java` (линии `useOn`, `placement` 1x1 и 3x3, а также всплывающие подсказки `appendHoverText`).
  - Исправлен обработчик `MagicCircleBlock.useItemOn` и статус в `useWithoutItem`.
- **2026-09-26 (v1.7):** Реализованы Межпространственные Врата (`DimensionalGateCircleType`):
  - Межпространственный транзит между любыми измерениями (Overworld, Nether, End и кастомные миры).
  - Сонастройка через средний кристалл маны: перенос частоты координат и двустороннее связывание врат.
  - Требование моно-многослойности 3x3: врата активируются только при наличии дополнительного слоя своего типа, расширенного до 3x3.
  - Анимированный 3D-астролябий и вертикальный вихревой портальный луч.
- **2026-09-26 (v1.6):** Реализована синхронизация активации слоёв для кругов с основой Барьера (`BarrierCircleType`):
  - Механика отложенной активации: если базой многослойной структуры является защитный круг барьера (`isBaseBarrier()`), все наложенные слои (святилище, изобилие, призыв, кристаллизация и т.д.) активируются **исключительно после** успешного возведения стен барьера (`isBarrierActive()`).
  - Блокировка фонового расхода маны: в `tick()` вызов `onServerTick()` для наложенных слоев заморожен до момента поднятия барьера, исключая утечку маны во время зарядки.
  - Порядок срабатывания в `triggerAllLayers()`: барьер возводится первым. При неудаче слои не активируются. При успехе все слои одновременно активируются, а игрок получает подтверждение.
  - Защита целостности активного барьера: во время действия барьера запрещено накладывать, расширять или снимать слои на Shift+ПКМ (`barrier_cannot_modify_active`).
  - Деактивация слоев при падении барьера: при истощении маны или разрушении стен барьера все эффекты слоев мгновенно отключаются.
- **2026-09-26 (v1.5):** Исправлена логика круга Изобилия (`OvergrowthCircleType`):
  - Устранена ошибка спавна травы и цветов на земле: в ванильном Minecraft блок травы (`GrassBlock`) реализует `BonemealableBlock`, из-за чего случайный выбор координат в радиусе покрывал сорняками весь газон и тратил ману впустую.
  - Введен интеллектуальный алгоритм сканирования столбцов сверху вниз с прямым обнаружением культур и грядок, а также строгий фильтр `isEligiblePlant()`, исключающий блоки травы, сорняки и мох.
  - Добавлена прямая акселерация грядок (`Blocks.FARMLAND` увлажняется до максимума `moisture=7` с эффектом брызг воды и сразу стимулирует культуру на ней), ускорение роста пшеницы, моркови, картофеля, свеклы, арбузных и тыквенных стеблей (со стимуляцией генерации плодов), тростника, кактусов, адского нароста, ягод и саженцев деревьев.
- **2026-09-25 (v1.3):** Реализована система Запечатывания и Призыва фамильяров (`CaptureCircleType`, `SummoningCircleType`, `SummonCompanionManager`):
  - Круг Запечатывания (`mushokucraft:capture`): конвертирует ману в урон по наступающим мобам, захватывает их душу при гибели и проецирует уменьшенную вращающуюся 3D-голограмму над кругом (`MagicCircleBlockEntityRenderer`).
  - Плавный непрерывный рендеринг: вращение голограммы переведено на монотонный миллисекундный таймер реального времени (`System.currentTimeMillis()`) со сбросом состояний ранения (`hurtTime = 0`) и фиксацией ориентации, что полностью устранило рывки и подергивания назад-вперед от скачков тиков клиента.
  - Перенос проекции и размещение: сбор круга или клик пустым манускриптом сохраняет NBT моба в `InscribedManuscriptItem`. Размещение манускрипта на блоке (1x1 и 3x3) надежно передает душу в `MagicCircleBlockEntity` без потери данных.
  - Ограничение связывания: привязка ПКМ свитком разрешена исключительно для кругов телепортации (`mushokucraft:teleport`); попытка связать боевые круги, круги барьера или призыва отклоняется с поясняющим сообщением.
  - Круг Призыва (`mushokucraft:summoning`): динамическое масштабирование маны от здоровья сущности, ритуал высвобождения со звуковыми и визуальными спецэффектами.
  - Система ИИ фамильяра (`SummonCompanionManager`): защита от взаимного урона с хозяином, следование, телепортация при отдалении > 24 блоков, защита хозяина и атака его целей.
- **2026-09-26 (v1.6):** Исправлены события ПКМ взаимодействия с магическими кругами:
  - Устранена блокировка связывания кругов телепортации 1x1: перехват взаимодействия перенесен из `useWithoutItem` в `MagicCircleBlock.useItemOn`, исключая ложное срабатывание сообщения «Круг не привязан» при клике манускриптом.
  - Реализовано взаимное связывание уже установленных кругов: клик манускриптом по первому кругу запоминает координаты, а последующий клик по второму уже размещенному кругу связывает их между собой с портальными эффектами и очисткой метки в свитке.
  - Реализовано расширение основы круга с 1x1 до 3x3: клик Чистым холстом или 8 бумагами по кругу 1x1 проверяет плоскую площадку 3x3, тратит материалы и возводит полную структуру 3x3 с сохранением всех свойств, начертания и связей.
  - Добавлена ленивая инициализация `circleTypeId` в `MagicCircleBlockEntity.getCircleTypeId()`, предотвращающая возвращение `null` до первого серверного тика.
- **2026-09-26 (v1.3):** Исправлена невидимость магических кругов на платформе Fabric:
  - Регистрация `MagicCircleBlockEntityRenderer` перенесена из отложенного `ClientLifecycleEvent.CLIENT_SETUP` на прямую регистрацию через `MAGIC_CIRCLE_BE.listen(...)` в `ModClientSetup` и нативный `BlockEntityRendererRegistry.register(...)` в `MushokuCraftFabricClient`.
  - Добавлена регистрация слоя вырезания `RenderType.cutout()` для блока `magic_circle` в `BlockRenderLayerMap` на Fabric.
  - Исправлен порядок инициализации: теперь рендерер гарантированно регистрируется до создания `BlockEntityRenderDispatcher` в `Minecraft.<init>`.
- **2026-09-25 (v1.2):** Реализован защитный магический круг Кеккай (`BarrierCircleType`) и стены барьера (`BarrierWallBlock`, `BarrierWallBlockEntity`):
  - Генерация тонких 1-пиксельных прозрачных стен барьера по периметру круга (радиус 4x4 / 8x8 в зависимости от размера 1x1 или 3x3).
  - Механика коллизии с распознаванием создателя (**Owner Bypass**): создатель барьера проходит сквозь свои стены свободно (`Shapes.empty()`), а для мобов, врагов, стрел и других игроков барьер является непроницаемой стеной.
  - Поглощение внешнего урона: атаки оружием и снарядами передают урон здоровью барьера и кругу с частицами и звуками удара.
  - Расход маны на содержание (upkeep) и автоматическая деактивация при истощении или разрушении.
  - Клик ПКМ по кругу и стене барьера выводит актуальный статус наполнения маной и прочности (`[Барьер] Мана: X | Прочность: Y/Z HP`).
- **2026-09-25 (v1.1):** Реализована полноценная система Магических Кругов и Пространственной Телепортации:
  - Создана расширяемая архитектура `com.mushokucraft.magic.circle` (`MagicCircleType`, `TeleportCircleType`, `MagicCircleRegistry`, `MagicCirclePattern`, `MagicCirclePatterns`, `ClientMagicCircleState`).
  - Добавлена библиотека из 15 уникальных симметричных рунических узоров 16x16, привязка активного узора к сиду мира и его синхронизация через `SyncMagicCirclesPacket`.
  - Добавлены предметы `blank_canvas` (крафт из 9 бумаги), `inscribed_manuscript` (хранение NBT-узора и привязки координат) и `ancient_manuscript` (изучение схемы).
  - Разработаны кастомные экраны `MagicCanvasScreen` (попиксельное рисование, расход 1 чернильного мешка за 3 пикселя при наличии во второй руке) и `AncientManuscriptScreen` (1:1 превью схемы).
  - Реализован 1-пиксельный блок `magic_circle` с блок-сущностью `MagicCircleBlockEntity` и динамическим рендерером `MagicCircleBlockEntityRenderer`.
  - Реализована механика взаимного связывания двух кругов ПКМ и вливания маны через Shift с частицами `PORTAL`/`ENCHANT`, звуковым резонансом и формулой маны от дистанции в `MushokuConfig`.
- **2026-09-21 (v1.0):** Первоначальное создание комплексной архитектурной карты проекта по вызову команды `/map`. Зафиксирована мульти-лоадер архитектура на Architectury API (Common, NeoForge, Fabric), описаны 130 классов кодовой базы, сетевой протокол, формулы прокачки и дерево исходников.
