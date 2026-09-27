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

## 9. Добавление новых Магических Кругов (Magic Circles)

Система магических кругов построена вокруг рисования пиксельных рунических паттернов 16×16 на холсте (`MagicCanvasScreen`), исследования древних манускриптов и размещения в мире мультиблочных ритуальных структур с вливанием маны.

### Архитектурные компоненты:
1. **`MagicCircleType`** — интерфейс поведения круга (стоимость маны, эффекты тика инфузии, логика активации).
2. **`MagicCircleRegistry`** — реестр всех типов кругов и метод `identify(level, pattern)`, динамически сопоставляющий нарисованный холст с активными кругами текущего сида.
3. **`MagicCirclePatterns`** — общий банк (пул) древних рунических матриц 16×16 (`ALL_PATTERNS`).
4. **`MagicCircleBlock` & `MagicCircleBlockEntity`** — блок в мире. Поддерживает размеры 1×1 и 3×3 (мультиблок с частями `CirclePart`), хранит текущую и требуемую ману, привязанные координаты (`linkedPos`, `linkedDim`), и состояние срезанной подложки (`sheared`).
5. **Предметы**:
   - `AncientManuscriptItem` — древний свиток с запечатанным кругом. Открытие свитка навсегда разблокирует его расшифровку в `PlayerMasteryData`.
   - `InscribedManuscriptItem` — нарисованный игроком свиток. Позволяет привязать круг кликом ПКМ по существующему кругу того же типа, или установить новый круг на ровную поверхность (1×1 или 3×3).
   - `BlankCanvasItem` — чистый холст для рисования в GUI.

---

### Главное правило: Сид-зависимость узоров (Seed-Based Shuffling) и Пустышки

> [!IMPORTANT]
> **В MushokuCraft категорически запрещены статичные узоры для кругов!**
> 
> Ни один магический круг не имеет постоянного узора между мирами. Все узоры берутся из единого пула матриц (`MagicCirclePatterns`), который детерминированно перемешивается для каждого сида мира (`worldSeed`):
> - **Один и тот же узор в Сиде А** может быть кругом **Телепортации**.
> - **В Сиде B** этот же узор станет кругом **Призыва**.
> - **В Сиде C** этот же узор окажется **пустышкой** (неактивной древней руной, которая ничего не делает).
> 
> Пул матриц всегда больше количества зарегистрированных кругов — поэтому лишние узоры в каждом сиде являются «ложными рунами». Игрок **не может** списать узор из интернета: он обязан исследовать структуры конкретно своего мира, находить древние манускрипты и расшифровывать их!

---

### Пошаговое добавление нового типа Магического Круга:

#### Шаг 1: Реализация интерфейса `MagicCircleType`
Создайте класс в пакете `com.mushokucraft.magic.circle` (по аналогии с `TeleportCircleType.java`):

```java
package com.mushokucraft.magic.circle;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SummoningCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "summoning");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.summoning");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.summoning.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        // Конфигурируемая стоимость маны (не хардкодить!)
        return MushokuConfig.MAGIC_CIRCLE_SUMMON_BASE_MANA.get().floatValue();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        // Визуальные эффекты пока игрок зажимает Shift на круге и вливает ману
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 0.2, cz, 3, 0.3, 0.1, 0.3, 0.02);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.5f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        // Логика срабатывания при полном заполнении маны (infused >= required).
        // Верните true, если действие выполнено успешно (мана круга сбросится в 0).
        // Верните false, если условия не соблюдены (сработает 2-секундный кулдаун для предотвращения спама).
        
        level.sendParticles(ParticleTypes.EXPLOSION, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, 5, 0, 0, 0, 0);
        level.playSound(null, origin, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0f, 1.0f);
        
        return true;
    }
}
```

#### Шаг 2: Регистрация в `MagicCircleRegistry`
Зарегистрируйте синглтон типа в `MagicCircleRegistry.java`:

```java
public class MagicCircleRegistry {
    public static final MagicCircleType TELEPORTATION = register(new TeleportCircleType());
    public static final MagicCircleType SUMMONING = register(new SummoningCircleType());
    // ...
}
```

#### Шаг 3: Как работает привязка узоров (Не нужно хардкодить `identify`!)
Благодаря архитектуре реестра, вам **НЕ нужно** вручную прописывать сопоставление в `identify`:
1. `MagicCircleRegistry.identify(level, pattern)` автоматически перебирает все зарегистрированные типы кругов и сопоставляет нарисованный холст с узором, который алгоритм назначил данному кругу в текущем сиде мира (`MagicCirclePatterns.getPatternForSeed(seed, type.getId())`).
2. Если совпадений не найдено, метод возвращает `null` (узор является пустышкой в этом мире).
3. **Если вы хотите пополнить банк древних узоров**:
   Просто добавьте новую 16×16 матрицу в список `list.add(...)` внутри `MagicCirclePatterns.java`. Она автоматически пополнит общий пул и будет участвовать в процедурном распределении между кругами и пустышками для всех будущих сидов:
   ```java
   list.add(new MagicCirclePattern(new String[]{
       "....########....",
       "..##...##...##..",
       ".#....####....#.",
       // ... ровно 16 строк по 16 символов
       "....########...."
   }));
   ```
4. **Получение узора круга в коде**:
   - На сервере: `MagicCirclePatterns.getPatternForSeed(serverLevel.getSeed(), circleTypeId)`
   - На клиенте (GUI/рендер): `ClientMagicCircleState.getPatternForType(circleTypeId)` (клиент получает сид мира при входе через `SyncMagicCirclesPacket`).

#### Шаг 4: Вынос параметров в `MushokuConfig.java`
Все затраты маны, рейты и дистанции должны настраиваться игроками/модпаками:
```java
public static ForgeConfigSpec.DoubleValue MAGIC_CIRCLE_SUMMON_BASE_MANA;
// ...
MAGIC_CIRCLE_SUMMON_BASE_MANA = builder
    .comment("Base mana required to activate a summoning circle")
    .defineInRange("magicCircleSummonBaseMana", 250.0, 1.0, 100000.0);
```

#### Шаг 5: Добавление локализации (`ru_ru.json` и `en_us.json`)
Добавьте ключи названия и описания:
```json
"magic_circle.mushokucraft.summoning": "Магический круг призыва",
"magic_circle.mushokucraft.summoning.desc": "Древний ритуальный круг, концентрирующий ману для вызова сущностей."
```

#### Шаг 6: Изучение и Древние манускрипты
- Круг автоматически поддерживает систему исследования:
  - Если игрок ещё **не** исследовал данный тип круга через древний свиток, название круга в тултипах свитков пишется обфусцированным шрифтом (`§k`), а описание — *"Не расшифровано"*.
  - Чтобы игрок мог изучить круг, добавьте `AncientManuscriptItem` с NBT-тегом `CircleType: "mushokucraft:summoning"` в сундуки структур или дроп с боссов. При открытии GUI свитком игрок навсегда разблокирует его знание.

#### Механики работы с кругами в мире (готовые фичи движка):
- **Срезание ножницами (`Shearing`)**: ПКМ ножницами по поставленному кругу срезает бумажную основу (выпадает 3 бумаги), оставляя на земле лишь рунический след (`SHEARED=true`). При разрушении срезанного круга ничего не выпадает. Несрезанный круг при ломании выпадает в виде отвязанного манускрипта.
- **Расширение 1×1 → 3×3**: Окружив нарисованный манускрипт 8 листами бумаги на верстаке (`ManuscriptExpandRecipe`), игрок получает версию 3×3. 3×3 круг формирует полноценный мультиблок из 9 блоков с честными хитбоксами (`CirclePart`) и увеличенной зоной действия.
- **Копирование манускрипта**: Чистый холст + оригинал + чернильные мешки (по количеству пикселей оригинала) в верстаке (`ManuscriptCopyRecipe`) позволяют тиражировать манускрипты.
- **Связка и автоотвязка**: Если тип круга предусматривает парную связь (`destination`), поломка или срезание одного из кругов моментально находит парный круг через чанки сервера (`unlinkRemoteCircle`), безопасно отвязывает его, сбрасывает ману и обновляет визуал на клиенте.

---

## Чеклист добавления новой фичи:
- [ ] Логика кросс-платформенна? (Нет ли импортов `net.neoforged` или `net.fabricmc` в `Common` модуле?)
- [ ] Логика не захардкожена? (Числа вынесены в Config/Constants?)
- [ ] Эвенты зарегистрированы через `dev.architectury.event.*` в методе `register()`?
- [ ] Серверный код не вызывает классы рендера/клиента?
- [ ] Для магических кругов: новый тип `MagicCircleType` зарегистрирован в `MagicCircleRegistry` (узор назначается процедурно по сиду из пула), а строки (`magic_circle.<id>`, `gui.mushokucraft.ancient_manuscript.<id>.desc2`) добавлены в языковые файлы?

