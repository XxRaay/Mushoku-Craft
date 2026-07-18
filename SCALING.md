# Руководство по масштабированию MushokuCraft (SCALING.md)

Это руководство описывает архитектуру мода после рефакторинга и объясняет, как добавлять новый контент (заклинания, стили меча, конфиги и элементы UI) без нарушения принципов чистого кода.

---

## 1. Конфигурация и Баланс (`MushokuConfig.java`)

Вся балансировка (урон, мана, кулдауны, шансы) вынесена в `MushokuConfig.java`. **Никакого хардкода чисел в логике!**

**Как добавить новый параметр:**
1. Откройте `MushokuConfig.java`.
2. Найдите или создайте подходящую категорию (например, `builder.push("magic")`).
3. Добавьте `ForgeConfigSpec.ConfigValue`:
   ```java
   public static ForgeConfigSpec.DoubleValue NEW_SPELL_DAMAGE;
   // ...
   NEW_SPELL_DAMAGE = builder.comment("Damage of the new spell")
       .defineInRange("newSpellDamage", 10.0, 0.0, 100.0);
   ```
4. В коде используйте `MushokuConfig.NEW_SPELL_DAMAGE.get().floatValue()`.

---

## 2. Добавление новых Заклинаний

Мы используем **Паттерн Стратегия (Strategy Pattern)** для заклинаний. Больше не нужно писать `if (spellName.equals(...))` в `CastSpellPacket` или `ServerCastManager`.

**Как добавить заклинание:**
1. Создайте класс реализации `SpellAction` в пакете `com.mushokucraft.magic.action`:
   ```java
   public class NewSpellAction implements SpellAction {
       @Override
       public void execute(Level level, ServerPlayer player, Spell spell) {
           // Логика инстант-каста (спавн энтити, урон, хилка и т.д.)
       }
   }
   ```
2. Зарегистрируйте заклинание в `ModSpells.java`:
   ```java
   public static final Spell NEW_SPELL = new Spell.Builder(ResourceLocation.fromNamespaceAndPath(MushokuCraft.MOD_ID, "new_spell"))
       .school(MagicSchool.WATER)
       .baseManaCost(MushokuConfig.NEW_SPELL_MANA.get().floatValue())
       .baseCastTimeTicks(60) // 3 секунды
       .fizzleChance(0.2f)
       .action(new NewSpellAction()) // Ваша логика
       .build();
   ```
3. Для **заряжаемых (Charge)** заклинаний реализуйте логику в `ServerChargeManager.java` (аналогично `handleLongswordLight`), но в будущем рекомендуется вынести `ChargeAction` в отдельный интерфейс по аналогии со `SpellAction`.

---

## 3. Добавление новых Стилей Меча

Боевая система разбита на независимые хэндлеры.

**Как добавить стиль:**
1. Добавьте его в `SwordStyle.java` (enum):
   ```java
   NEW_STYLE("new_style", ChatFormatting.LIGHT_PURPLE)
   ```
2. Создайте специализированный хэндлер `NewStyleHandler.java` в пакете `com.mushokucraft.combat`.
3. Подпишите его на события (EventBusSubscriber).
   ```java
   @EventBusSubscriber(modid = MushokuCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
   public class NewStyleHandler {
       @SubscribeEvent
       public static void onAttack(LivingDamageEvent.Pre event) {
           // Проверьте активный стиль и примените логику
       }
   }
   ```
4. Если стиль дает пассивные бонусы к статам, добавьте их в `MasteryCalculator.java` или обновляйте атрибуты в `SwordCombatHandler.onPlayerTick`.

---

## 4. Сетевые пакеты (Network)

Пакеты должны быть **тонкими**. Они только переносят данные и делегируют выполнение менеджерам.

**Правила для пакетов:**
1. **Никакой логики в `handle()`**. Пакет должен вызывать метод вроде `Manager.doSomething(player, data)`.
2. **Нет клиентских импортов на сервере**. Если пакет летит от Сервера к Клиенту (PlayToClient), его обработчик должен вызывать метод из `com.mushokucraft.client.network.ClientPayloadHandler`. 
   - *Почему?* Иначе выделенный сервер (Dedicated Server) упадет с `NoClassDefFoundError` при попытке загрузить класс пакета.

---

## 5. UI и Клиентская часть

Все, что связано с рендером и инпутами, изолировано в пакете `com.mushokucraft.client`.

- **Инпуты:** Ранее огромный `ClientInputHandler` разбит на `MenuInputHandler` (круговое меню), `QteInputHandler` (мини-игра) и `CombatInputHandler` (удары/касты). Добавляйте новые бинды в соответствующие классы.
- **HUD:** Рендереры разбиты на отдельные классы (`ManaHudManager`, `QTEOverlay`, `MasteryOverlay`). Константы для отрисовки (цвета, размеры) вынесены в `HudConstants.java`.

---

## 6. Прогрессия и Формулы

Все формулы вычисления статов (размер маны, реген, шансы успеха, урон Тоуки) находятся в `MasteryCalculator.java`. 

Если вам нужно изменить то, как мастерство влияет на размер окна QTE или урон, меняйте это **только** в `MasteryCalculator`, а не раскидывайте умножения (`* 0.2f`) по всему коду.

## 7. Система разделки туш (Carcass System)

Для добавления разделываемых туш монстров, используйте базовый класс `AbstractCarcassEntity`. Это позволяет избежать дублирования логики взаимодействия с Охотничьим ножом.

**Как добавить новую тушу:**
1. Создайте класс (например, `DeadGiantFrogEntity`), наследующий `AbstractCarcassEntity`.
2. Зарегистрируйте `EntityType` в `ModEntities`.
3. Переопределите метод `getCarcassLootTable()` и верните уникальный `ResourceKey<LootTable>` (например, `entities/dead_giant_frog_carcass`).
4. Создайте соответствующий `.json` файл лут-таблицы в ресурсах.
Все эффекты (партиклы, кулдауны, трата прочности ножа) обрабатываются автоматически в `AbstractCarcassEntity`. Количество использований для разделки настраивается глобально в `MushokuConfig.CARCASS_MAX_USES`.

## Чеклист добавления новой фичи:
- [ ] Логика не захардкожена? (Числа вынесены в Config/Constants?)
- [ ] Код не дублируется? (Используется базовый класс `AbstractMagicProjectileEntity`?)
- [ ] В пакетах нет логики? (Они тонкие?)
- [ ] Серверный код не импортирует классы рендера/Minecraft.getInstance()?
