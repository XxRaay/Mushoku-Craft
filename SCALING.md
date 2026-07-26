# Руководство по масштабированию MushokuCraft (SCALING.md)

Это руководство описывает архитектуру мода после переезда на **Architectury API** и объясняет, как добавлять новый контент (заклинания, стили меча, конфиги и элементы UI) так, чтобы он работал и на NeoForge, и на Fabric одновременно, без нарушения принципов чистого кода.

---

## 1. Архитектура Multi-Loader (Architectury)

Мод разделен на три модуля:
- **`Common`**: Здесь находится 99% всего кода. Блоки, предметы, магия, боевка, рендер, эвенты — всё пишется здесь.
- **`NeoForge_1.21.1`** и **`Fabric_1.21.1`**: Это просто легковесные "загрузчики", которые вызывают `MushokuCraftCommon.init()` и содержат мелкий платформозависимый код (например, регистрацию `LootModifiers` в Fabric или чтение JSON-атрибутов в NeoForge).

**Главное правило:** Никогда не используйте классы, специфичные для `net.neoforged.*` или `net.fabricmc.*` внутри модуля `Common`. Используйте абстракции `dev.architectury.*`!

---

## 2. Конфигурация и Баланс (`MushokuConfig.java`)

Вся балансировка (урон, мана, кулдауны, шансы) вынесена в `MushokuConfig.java`. **Никакого хардкода чисел в логике!**

**Как добавить новый параметр:**
1. Откройте `MushokuConfig.java`.
2. Найдите или создайте подходящую категорию.
3. Добавьте `ForgeConfigSpec.ConfigValue`:
   ```java
   public static ForgeConfigSpec.DoubleValue NEW_SPELL_DAMAGE;
   // ...
   NEW_SPELL_DAMAGE = builder.comment("Damage of the new spell")
       .defineInRange("newSpellDamage", 10.0, 0.0, 100.0);
   ```

---

## 3. Добавление новых Заклинаний

Мы используем паттерн **Builder** для заклинаний. Теперь заклинания могут иметь ранг (SpellRank), инкантацию, прожектайлы и свойство поддерживаемости (channeled).

**Как добавить заклинание:**
1. Зарегистрируйте заклинание в `ModSpells.java`:
   ```java
   public static final Spell NEW_SPELL = register(new Spell.Builder(
           ResourceLocation.fromNamespaceAndPath("mushokucraft", "new_spell"), MagicSchool.WATER, SpellRank.ADVANCED)
           .castTime(MushokuConfig.NEW_SPELL_CAST_TIME.get().floatValue())
           .manaCost(MushokuConfig.NEW_SPELL_MANA.get().floatValue())
           .fizzleChance(MushokuConfig.NEW_SPELL_FIZZLE.get().floatValue())
           .incantation("spell.mushokucraft.new_spell.incantation")
           .channeled(false) // Опционально: true для заклинаний, которые можно держать бесконечно
           // Выберите один из способов эффекта:
           .projectile(com.mushokucraft.magic.entity.NewSpellEntity::new, 2.0f, 0.5f) // Если это прожектайл
           // .action(new NewSpellAction()) // Если это кастомное действие
           .build());
   ```
2. Если заклинание использует `SpellAction`, создайте его реализацию в пакете `com.mushokucraft.magic.action`.
3. Добавьте локализацию инкантации и названия в языковые файлы (`en_us.json` и `ru_ru.json`).
4. Добавьте заклинание в список заклинаний в круговом меню (например, `SpellWheelScreen`).

## 4. Добавление новых Стилей Меча и Эвентов

В Architectury мы больше не используем аннотации `@SubscribeEvent`. Вместо этого мы используем лямбды и регистрацию через `dev.architectury.event.events.*`.

**Как добавить стиль или любой другой обработчик эвентов:**
1. Создайте класс хэндлера (например, `NewStyleHandler.java`) в пакете `com.mushokucraft.combat`.
2. Напишите метод `register()` и подпишитесь на нужный Architectury Event:
   ```java
   public class NewStyleHandler {
       public static void register() {
           // Например, хук на урон перед атакой
           EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
               if (source.getEntity() instanceof Player player) {
                   // Ваша логика применения стиля меча
               }
               return EventResult.pass();
           });
       }
   }
   ```
3. Вызовите `NewStyleHandler.register()` внутри `MushokuCraftCommon.init()`.

---

## 5. Сетевые пакеты (Network)

Сетевой код переведен на `NetworkManager` из Architectury.

**Правила для пакетов:**
1. Регистрируйте пакеты в `ModNetworking.java` в `Common`:
   ```java
   NetworkManager.registerReceiver(NetworkManager.Side.C2S, PACKET_ID, (buf, context) -> {
       // Чтение данных
       context.queue(() -> {
           // Исполнение на главном потоке сервера
       });
   });
   ```
2. **Никакой тяжелой логики в пакетах**. Пакет должен вызывать метод из менеджера, например `ServerCastManager.startCast(...)`.

---

## 6. Регистрация Предметов, Блоков, Энтити (Registries)

Вместо `DeferredRegister` от NeoForge, мы используем `DeferredRegister` из Architectury API (`dev.architectury.registry.registries.DeferredRegister`).
Они выглядят и работают точно так же, но создаются через `DeferredRegister.create(MushokuCraftCommon.MOD_ID, Registries.ITEM)`.
Все регистрации (items, blocks, entities, effects) вызываются в `MushokuCraftCommon.init()`.

---

## 7. UI, Инпуты и Клиентская часть

Все, что связано с рендером и инпутами, изолировано в пакете `com.mushokucraft.client`.
Регистрация клиентских эвентов и биндов происходит через `ModClientEvents.register()` и `ModClientSetup.register()`, которые вызываются только на стороне клиента в `NeoForge_1.21.1` и `Fabric_1.21.1` модулях (или через `EnvExecutor.runInEnv(Env.CLIENT, ...)`).

- **Инпуты:** Ранее огромный класс инпутов разбит на `MenuInputHandler` (круговое меню), `QteInputHandler` (мини-игра) и `CombatInputHandler` (удары/касты). Инпуты биндятся через Architectury `ClientTickEvent.CLIENT_POST`.
- **HUD:** Рендереры разбиты на отдельные классы (`ManaHudManager`, `QTEOverlay`, `MasteryOverlay`).

---

## 8. Прогрессия и Формулы

Все формулы вычисления статов (размер маны, реген, шансы успеха, урон Тоуки) находятся в `MasteryCalculator.java`. 

Если вам нужно изменить то, как мастерство влияет на размер окна QTE или урон, меняйте это **только** в `MasteryCalculator`, а не раскидывайте умножения (`* 0.2f`) по всему коду.

---

## Чеклист добавления новой фичи:
- [ ] Логика кросс-платформенна? (Нет ли импортов `net.neoforged` или `net.fabricmc` в `Common` модуле?)
- [ ] Логика не захардкожена? (Числа вынесены в Config/Constants?)
- [ ] Эвенты зарегистрированы через `dev.architectury.event.*` в методе `register()`?
- [ ] Серверный код не вызывает классы рендера/клиента?
