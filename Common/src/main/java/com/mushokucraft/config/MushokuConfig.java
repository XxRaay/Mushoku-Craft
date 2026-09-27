package com.mushokucraft.config;

import com.mushokucraft.client.hud.ManaDisplayMode;

public class MushokuConfig {
    public static class ConfigValue<T> {
        private T value;
        public ConfigValue(T defaultValue) { this.value = defaultValue; }
        public T get() { return value; }
        public void set(T val) { this.value = val; }
    }

    public static final ConfigValue<ManaDisplayMode> MANA_DISPLAY_MODE = new ConfigValue<>(ManaDisplayMode.HYBRID);
    public static final ConfigValue<Double> MANA_BAR_OPACITY = new ConfigValue<>(0.85);
    public static final ConfigValue<Boolean> SHOW_MANA_NUMBERS = new ConfigValue<>(true);

    // ==========================================
    // Elementary Magic Configs (Tier 1: 10-12 Mana, 35-45 Ticks, ~5-6.5 Damage)
    // ==========================================
    public static final ConfigValue<Double> WATERBALL_MANA_COST = new ConfigValue<>(10.0);
    public static final ConfigValue<Integer> WATERBALL_CAST_TIME_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Double> WATERBALL_FIZZLE_CHANCE = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> WATERBALL_BASE_DAMAGE = new ConfigValue<>(5.0);

    public static final ConfigValue<Double> FIREBALL_MANA_COST = new ConfigValue<>(12.0);
    public static final ConfigValue<Integer> FIREBALL_CAST_TIME_TICKS = new ConfigValue<>(45);
    public static final ConfigValue<Double> FIREBALL_FIZZLE_CHANCE = new ConfigValue<>(0.28);
    public static final ConfigValue<Double> FIREBALL_BASE_DAMAGE = new ConfigValue<>(5.5);

    public static final ConfigValue<Double> ROCK_BULLET_MANA_COST = new ConfigValue<>(12.0);
    public static final ConfigValue<Integer> ROCK_BULLET_CAST_TIME_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Double> ROCK_BULLET_FIZZLE_CHANCE = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> ROCK_BULLET_BASE_DAMAGE = new ConfigValue<>(6.5);

    public static final ConfigValue<Double> AIR_STRIKE_MANA_COST = new ConfigValue<>(10.0);
    public static final ConfigValue<Integer> AIR_STRIKE_CAST_TIME_TICKS = new ConfigValue<>(35);
    public static final ConfigValue<Double> AIR_STRIKE_FIZZLE_CHANCE = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> AIR_STRIKE_BASE_DAMAGE = new ConfigValue<>(5.0);

    // ==========================================
    // Intermediate Magic Configs (Tier 2: 30-32 Mana, 70-80 Ticks, ~14-16 Damage)
    // ==========================================
    // Water Slice Configs
    public static final ConfigValue<Double> WATER_SLICE_MANA_COST = new ConfigValue<>(30.0);
    public static final ConfigValue<Integer> WATER_SLICE_CAST_TIME_TICKS = new ConfigValue<>(75);
    public static final ConfigValue<Double> WATER_SLICE_FIZZLE_CHANCE = new ConfigValue<>(0.40);
    public static final ConfigValue<Double> WATER_SLICE_BASE_DAMAGE = new ConfigValue<>(14.0);
    public static final ConfigValue<Double> WATER_SLICE_CHARGE_WIDTH_MULT = new ConfigValue<>(1.5);

    // Flame Pillar Configs (Intermediate)
    public static final ConfigValue<Double> FLAME_PILLAR_MANA_COST = new ConfigValue<>(32.0);
    public static final ConfigValue<Integer> FLAME_PILLAR_CAST_TIME_TICKS = new ConfigValue<>(80);
    public static final ConfigValue<Double> FLAME_PILLAR_FIZZLE_CHANCE = new ConfigValue<>(0.42);
    public static final ConfigValue<Double> FLAME_PILLAR_BASE_DAMAGE = new ConfigValue<>(5.0);
    public static final ConfigValue<Double> FLAME_PILLAR_RADIUS = new ConfigValue<>(2.2);
    public static final ConfigValue<Double> FLAME_PILLAR_HEIGHT = new ConfigValue<>(8.0);
    public static final ConfigValue<Double> FLAME_PILLAR_KNOCKUP = new ConfigValue<>(0.75);

    // Earth Lance Configs (Intermediate)
    public static final ConfigValue<Double> EARTH_LANCE_MANA_COST = new ConfigValue<>(30.0);
    public static final ConfigValue<Integer> EARTH_LANCE_CAST_TIME_TICKS = new ConfigValue<>(75);
    public static final ConfigValue<Double> EARTH_LANCE_FIZZLE_CHANCE = new ConfigValue<>(0.40);
    public static final ConfigValue<Double> EARTH_LANCE_BASE_DAMAGE = new ConfigValue<>(15.0);
    public static final ConfigValue<Double> EARTH_LANCE_RADIUS = new ConfigValue<>(1.6);

    // Air Cushion Configs (Intermediate Utility)
    public static final ConfigValue<Integer> AIR_CUSHION_CAST_TIME_TICKS = new ConfigValue<>(70);
    public static final ConfigValue<Double> AIR_CUSHION_FIZZLE_CHANCE = new ConfigValue<>(0.35);

    // ==========================================
    // Advanced Magic Configs (Tier 3: 55-60 Mana, 110-120 Ticks, ~18-22 Damage)
    // ==========================================
    // Icicle Break Configs
    public static final ConfigValue<Double> ICICLE_BREAK_MANA_COST = new ConfigValue<>(55.0);
    public static final ConfigValue<Integer> ICICLE_BREAK_CAST_TIME_TICKS = new ConfigValue<>(110);
    public static final ConfigValue<Double> ICICLE_BREAK_FIZZLE_CHANCE = new ConfigValue<>(0.50);
    public static final ConfigValue<Double> ICICLE_BREAK_ICICLE_DAMAGE = new ConfigValue<>(7.0);

    // Exodus Flame Configs (Advanced)
    public static final ConfigValue<Double> EXODUS_FLAME_MANA_COST = new ConfigValue<>(60.0);
    public static final ConfigValue<Integer> EXODUS_FLAME_CAST_TIME_TICKS = new ConfigValue<>(120);
    public static final ConfigValue<Double> EXODUS_FLAME_FIZZLE_CHANCE = new ConfigValue<>(0.52);
    public static final ConfigValue<Double> EXODUS_FLAME_BASE_DAMAGE = new ConfigValue<>(22.0);
    public static final ConfigValue<Double> EXODUS_FLAME_RADIUS = new ConfigValue<>(6.0);
    public static final ConfigValue<Double> EXODUS_FLAME_SPEED = new ConfigValue<>(1.35);
    public static final ConfigValue<Double> EXODUS_FLAME_TRAIL_DAMAGE = new ConfigValue<>(4.0);

    // Stone Pillar Configs (Advanced)
    public static final ConfigValue<Double> STONE_PILLAR_MANA_COST = new ConfigValue<>(58.0);
    public static final ConfigValue<Integer> STONE_PILLAR_CAST_TIME_TICKS = new ConfigValue<>(115);
    public static final ConfigValue<Double> STONE_PILLAR_FIZZLE_CHANCE = new ConfigValue<>(0.50);
    public static final ConfigValue<Double> STONE_PILLAR_BASE_DAMAGE = new ConfigValue<>(18.0);
    public static final ConfigValue<Double> STONE_PILLAR_RADIUS = new ConfigValue<>(3.5);
    public static final ConfigValue<Double> STONE_PILLAR_HEIGHT = new ConfigValue<>(10.0);
    public static final ConfigValue<Double> STONE_PILLAR_KNOCKUP = new ConfigValue<>(1.35);

    // Tornado Configs (Advanced)
    public static final ConfigValue<Double> TORNADO_MANA_COST = new ConfigValue<>(58.0);
    public static final ConfigValue<Integer> TORNADO_CAST_TIME_TICKS = new ConfigValue<>(115);
    public static final ConfigValue<Double> TORNADO_FIZZLE_CHANCE = new ConfigValue<>(0.48);
    public static final ConfigValue<Double> TORNADO_TICK_DAMAGE = new ConfigValue<>(4.5);
    public static final ConfigValue<Double> TORNADO_RADIUS = new ConfigValue<>(7.0);
    public static final ConfigValue<Double> TORNADO_HEIGHT = new ConfigValue<>(14.0);
    public static final ConfigValue<Integer> TORNADO_LIFETIME_TICKS = new ConfigValue<>(140);

    // ==========================================
    // Saint Magic Configs (Tier 4 / Mid-Tier Domain: 100-110 Mana, 190-200 Ticks, ~14-16 Damage/Pulse, 45-48 Radius)
    // NOTE: In the grand Mushoku Tensei cosmology (7 tiers), Saint is the middle tier (T4).
    // King (T5), Emperor (T6), and God (T7) will expand above this tier later.
    // ==========================================
    // Cumulonimbus Configs
    public static final ConfigValue<Double> CUMULONIMBUS_MANA_COST = new ConfigValue<>(100.0);
    public static final ConfigValue<Integer> CUMULONIMBUS_CAST_TIME_TICKS = new ConfigValue<>(190);
    public static final ConfigValue<Double> CUMULONIMBUS_FIZZLE_CHANCE = new ConfigValue<>(0.60);
    public static final ConfigValue<Double> CUMULONIMBUS_RADIUS = new ConfigValue<>(48.0);
    public static final ConfigValue<Double> CUMULONIMBUS_LIGHTNING_DAMAGE = new ConfigValue<>(16.0);
    public static final ConfigValue<Integer> CUMULONIMBUS_STRIKE_INTERVAL_TICKS = new ConfigValue<>(40);

    // Flashover Configs (Saint)
    public static final ConfigValue<Double> FLASHOVER_MANA_COST = new ConfigValue<>(110.0);
    public static final ConfigValue<Integer> FLASHOVER_CAST_TIME_TICKS = new ConfigValue<>(200);
    public static final ConfigValue<Double> FLASHOVER_FIZZLE_CHANCE = new ConfigValue<>(0.62);
    public static final ConfigValue<Double> FLASHOVER_RADIUS = new ConfigValue<>(45.0);
    public static final ConfigValue<Double> FLASHOVER_DETONATION_DAMAGE = new ConfigValue<>(16.0);
    public static final ConfigValue<Double> FLASHOVER_THERMAL_DAMAGE = new ConfigValue<>(3.5);
    public static final ConfigValue<Integer> FLASHOVER_DETONATION_INTERVAL_TICKS = new ConfigValue<>(25);

    // Sandstorm Configs (Saint)
    public static final ConfigValue<Double> SANDSTORM_MANA_COST = new ConfigValue<>(105.0);
    public static final ConfigValue<Integer> SANDSTORM_CAST_TIME_TICKS = new ConfigValue<>(195);
    public static final ConfigValue<Double> SANDSTORM_FIZZLE_CHANCE = new ConfigValue<>(0.60);
    public static final ConfigValue<Double> SANDSTORM_RADIUS = new ConfigValue<>(45.0);
    public static final ConfigValue<Double> SANDSTORM_ABRASION_DAMAGE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> SANDSTORM_VORTEX_DAMAGE = new ConfigValue<>(14.0);
    public static final ConfigValue<Integer> SANDSTORM_VORTEX_INTERVAL_TICKS = new ConfigValue<>(30);

    // Typhoon Configs (Saint)
    public static final ConfigValue<Double> TYPHOON_MANA_COST = new ConfigValue<>(110.0);
    public static final ConfigValue<Integer> TYPHOON_CAST_TIME_TICKS = new ConfigValue<>(200);
    public static final ConfigValue<Double> TYPHOON_FIZZLE_CHANCE = new ConfigValue<>(0.62);
    public static final ConfigValue<Double> TYPHOON_RADIUS = new ConfigValue<>(48.0);
    public static final ConfigValue<Double> TYPHOON_STRIKE_DAMAGE = new ConfigValue<>(16.0);
    public static final ConfigValue<Double> TYPHOON_GALE_DAMAGE = new ConfigValue<>(3.0);
    public static final ConfigValue<Integer> TYPHOON_STRIKE_INTERVAL_TICKS = new ConfigValue<>(25);

    public static final ConfigValue<Double> DEFAULT_MANA = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> DEFAULT_MAX_MANA = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> DEFAULT_MANA_REGEN_RATE = new ConfigValue<>(0.05);
    public static final ConfigValue<Double> MANA_COST_REDUCTION_PER_MASTERY = new ConfigValue<>(0.3);

    // Mana Progression & Capacity Growth (Mushoku Tensei Anime System)
    public static final ConfigValue<Double> MANA_GROWTH_RATE = new ConfigValue<>(0.02);
    public static final ConfigValue<Double> MANA_EXHAUSTION_THRESHOLD = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> MANA_EXHAUSTION_BONUS_MULT = new ConfigValue<>(2.0);
    public static final ConfigValue<Boolean> ENABLE_MANA_GROWTH_DECAY = new ConfigValue<>(true);
    public static final ConfigValue<Double> MANA_GROWTH_DECAY_POWER = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> MIN_MANA_GROWTH_FACTOR = new ConfigValue<>(0.1);
    public static final ConfigValue<Double> MAX_MANA_CAP = new ConfigValue<>(50000.0);
    public static final ConfigValue<Double> FIZZLE_MANA_GROWTH_MULT = new ConfigValue<>(0.5);
    public static final ConfigValue<Boolean> ALLOW_SWORD_ARTS_MANA_GROWTH = new ConfigValue<>(false);
    public static final ConfigValue<Double> MANA_REGEN_FROM_MAX_MANA_RATE = new ConfigValue<>(0.0002);
    public static final ConfigValue<Double> MANA_REGEN_FROM_MAX_MANA_CAP = new ConfigValue<>(1.0);
    public static final ConfigValue<Boolean> SHOW_MANA_GROWTH_NOTIFICATIONS = new ConfigValue<>(true);
    public static final ConfigValue<Boolean> MANA_EXHAUSTION_EFFECTS = new ConfigValue<>(true);
    public static final ConfigValue<Boolean> SLEEP_FULL_MANA_RESTORE = new ConfigValue<>(true);
    // Progression & Mastery Balancing (Harder grind, significantly rewarding)
    public static final ConfigValue<Double> SPELL_MASTERY_PER_CAST = new ConfigValue<>(0.006);
    public static final ConfigValue<Double> SCHOOL_MASTERY_PER_CAST = new ConfigValue<>(0.002);
    public static final ConfigValue<Double> LEARNING_SUCCESS_BASE_CHANCE = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> LEARNING_MASTERY_GAIN = new ConfigValue<>(0.08);
    public static final ConfigValue<Integer> QTE_PERFECT_TIME_BONUS_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Double> QTE_SPEED_REDUCTION_PER_MASTERY = new ConfigValue<>(0.1);
    public static final ConfigValue<Double> QTE_SIZE_INCREASE_PER_MASTERY = new ConfigValue<>(0.2);
    public static final ConfigValue<Integer> CHARGE_MAX_TICKS = new ConfigValue<>(60);
    public static final ConfigValue<Double> CHARGE_TOTAL_MANA_DRAIN = new ConfigValue<>(125.0);
    public static final ConfigValue<Double> CHARGE_MIN_SCALE = new ConfigValue<>(0.1);
    public static final ConfigValue<Double> CHARGE_MAX_SCALE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> CHARGE_MASTERY_BONUS_SPELL = new ConfigValue<>(0.012);
    public static final ConfigValue<Double> CHARGE_MASTERY_BONUS_SCHOOL = new ConfigValue<>(0.005);
    public static final ConfigValue<Double> HUNTING_KNIFE_DAMAGE = new ConfigValue<>(1.0);

    // ==========================================
    // Sword Arts, Touki & Close-Quarters Combat (Swords dominate Close/Mid-range)
    // ==========================================
    public static final ConfigValue<Integer> LONGSWORD_LIGHT_CAST_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> LONGSWORD_LIGHT_COOLDOWN_TICKS = new ConfigValue<>(600); // 30s
    public static final ConfigValue<Double> LONGSWORD_LIGHT_DASH_DISTANCE = new ConfigValue<>(7.5);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_HITBOX_INFLATE = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_HIT_RADIUS_SQ = new ConfigValue<>(4.5);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_DAMAGE = new ConfigValue<>(28.0);

    public static final ConfigValue<Double> LONGSWORD_SILENCE_MANA_COST_PERCENT = new ConfigValue<>(0.50);
    public static final ConfigValue<Integer> LONGSWORD_SILENCE_CAST_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> LONGSWORD_SILENCE_COOLDOWN_TICKS = new ConfigValue<>(2400); // 2 minutes
    public static final ConfigValue<Double> LONGSWORD_SILENCE_DASH_DISTANCE = new ConfigValue<>(16.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_HITBOX_INFLATE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_HIT_RADIUS_SQ = new ConfigValue<>(9.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_DAMAGE = new ConfigValue<>(50.0);

    public static final ConfigValue<Double> BLEEDING_BASE_DAMAGE = new ConfigValue<>(1.5);
    public static final ConfigValue<Integer> BLEEDING_TICK_INTERVAL = new ConfigValue<>(35);
    public static final ConfigValue<Double> LOW_HP_THRESHOLD = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN = new ConfigValue<>(0.015);
    public static final ConfigValue<Integer> SHIELD_BLOCKS_FOR_WATER_GOD = new ConfigValue<>(25);
    public static final ConfigValue<Double> DUMMY_MASTERY_GAIN = new ConfigValue<>(0.0006);
    public static final ConfigValue<Double> SWORD_GOD_HIGH_DAMAGE_THRESHOLD = new ConfigValue<>(10.0);
    public static final ConfigValue<Double> SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN = new ConfigValue<>(0.003);

    // Touki (Battle Aura) scaling: devastating close-range physical enhancement
    public static final ConfigValue<Double> TOUKI_MANA_DRAIN_BASE = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> TOUKI_MANA_DRAIN_SCALING = new ConfigValue<>(6.0);
    public static final ConfigValue<Double> TOUKI_DAMAGE_BASE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> TOUKI_DAMAGE_SCALING = new ConfigValue<>(7.0); // Up to +10.0 damage at 100%
    public static final ConfigValue<Double> TOUKI_KB_RES_BASE = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> TOUKI_KB_RES_SCALING = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> TOUKI_SPEED_BASE = new ConfigValue<>(0.2);
    public static final ConfigValue<Double> TOUKI_SPEED_SCALING = new ConfigValue<>(0.8);
    public static final ConfigValue<Double> TOUKI_JUMP_BASE = new ConfigValue<>(0.2);
    public static final ConfigValue<Double> TOUKI_JUMP_SCALING = new ConfigValue<>(0.5);

    // North God Perks
    public static final ConfigValue<Double> NORTH_GOD_FALL_ABSORB_BASE = new ConfigValue<>(4.0);
    public static final ConfigValue<Double> NORTH_GOD_FALL_ABSORB_SCALING = new ConfigValue<>(12.0);
    public static final ConfigValue<Integer> NORTH_GOD_SLOW_DURATION = new ConfigValue<>(100);
    public static final ConfigValue<Integer> NORTH_GOD_BLEED_DURATION = new ConfigValue<>(100);
    public static final ConfigValue<Double> THROWN_SWORD_DAMAGE = new ConfigValue<>(10.0);
    public static final ConfigValue<Integer> THROWN_SWORD_PICKUP_DELAY = new ConfigValue<>(10);

    // Water God Parry & Counter
    public static final ConfigValue<Integer> PARRY_WINDOW_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> PARRY_COOLDOWN_TICKS = new ConfigValue<>(35);
    public static final ConfigValue<Double> WATER_GOD_PARRY_MASTERY_GAIN = new ConfigValue<>(0.008);
    public static final ConfigValue<Double> PROJECTILE_DEFLECT_SPEED_MULT = new ConfigValue<>(-1.5);
    public static final ConfigValue<Double> COUNTER_ATTACK_DAMAGE = new ConfigValue<>(10.0);

    // Sword God Perks
    public static final ConfigValue<Double> TOUKI_MIN_MASTERY = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> SWORD_GOD_SPEED_BONUS = new ConfigValue<>(1.5);
    public static final ConfigValue<Double> SWORD_GOD_REACH_BONUS = new ConfigValue<>(1.5);
    public static final ConfigValue<Integer> CARCASS_MAX_USES = new ConfigValue<>(3);

    // Melee vs Magic Matchup Rule: Melee hits disrupt active chanting
    public static final ConfigValue<Boolean> CAST_INTERRUPTION_ON_MELEE = new ConfigValue<>(true);
    public static final ConfigValue<Double> CAST_INTERRUPTION_MIN_DAMAGE = new ConfigValue<>(2.0);

    // Air Cushion Configs
    public static final ConfigValue<Double> AIR_CUSHION_MANA_DRAIN_BASE = new ConfigValue<>(5.0);
    public static final ConfigValue<Double> AIR_CUSHION_MANA_DRAIN_HEIGHT_MULT = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> AIR_CUSHION_MASTERY_GAIN = new ConfigValue<>(0.5); // per second
    public static final ConfigValue<Double> AIR_CUSHION_SPEED_MULT_MAX = new ConfigValue<>(1.3);
    public static final ConfigValue<Double> AIR_CUSHION_SPEED_MULT_BASE = new ConfigValue<>(1.0);
    public static final ConfigValue<Integer> AIR_CUSHION_BLOCK_LIFETIME = new ConfigValue<>(14); // 0.7s = 14 ticks

    // Magic Circle Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_TELEPORT_BASE_MANA = new ConfigValue<>(40.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_TELEPORT_MANA_PER_BLOCK = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> MAGIC_CIRCLE_INFUSION_RATE_PER_TICK = new ConfigValue<>(2.5);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_INK_PIXELS_PER_SAC = new ConfigValue<>(12);

    // Magic Circle Barrier Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_1X1_MANA = new ConfigValue<>(250.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_3X3_MANA = new ConfigValue<>(750.0);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_BARRIER_1X1_RADIUS = new ConfigValue<>(5);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_BARRIER_3X3_RADIUS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_BARRIER_1X1_HEIGHT = new ConfigValue<>(4);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_BARRIER_3X3_HEIGHT = new ConfigValue<>(6);
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_1X1_MAX_HP = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_3X3_MAX_HP = new ConfigValue<>(350.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_1X1_UPKEEP = new ConfigValue<>(1.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_BARRIER_3X3_UPKEEP = new ConfigValue<>(4.0);

    // Magic Circle Capture (Sealing) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_CAPTURE_BASE_MANA = new ConfigValue<>(300.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_CAPTURE_DAMAGE_RATE = new ConfigValue<>(4.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_CAPTURE_MANA_PER_DAMAGE = new ConfigValue<>(1.0);

    // Magic Circle Summoning Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_SUMMON_BASE_MANA = new ConfigValue<>(50.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SUMMON_MANA_PER_HP = new ConfigValue<>(5.0);

    // Magic Circle Sanctuary (Healing & Cleansing) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_SANCTUARY_1X1_MANA = new ConfigValue<>(250.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SANCTUARY_3X3_MANA = new ConfigValue<>(750.0);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_SANCTUARY_1X1_RADIUS = new ConfigValue<>(4);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_SANCTUARY_3X3_RADIUS = new ConfigValue<>(8);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SANCTUARY_HEAL_PER_SEC = new ConfigValue<>(1.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SANCTUARY_MANA_PER_HEAL = new ConfigValue<>(5.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SANCTUARY_MANA_PER_CLEANSE = new ConfigValue<>(20.0);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_SANCTUARY_INTERVAL_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_SANCTUARY_COMBAT_COOLDOWN_TICKS = new ConfigValue<>(60);

    // Magic Circle Overgrowth (Fertility & Farming) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_OVERGROWTH_1X1_MANA = new ConfigValue<>(250.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_OVERGROWTH_3X3_MANA = new ConfigValue<>(750.0);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_OVERGROWTH_1X1_RADIUS = new ConfigValue<>(6);
    public static final ConfigValue<Integer> MAGIC_CIRCLE_OVERGROWTH_3X3_RADIUS = new ConfigValue<>(16);
    public static final ConfigValue<Double> MAGIC_CIRCLE_OVERGROWTH_MANA_PER_GROWTH = new ConfigValue<>(2.5);

    // Magic Circle Dimensional Gate (Interdimensional Transit) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_DIMENSIONAL_GATE_MANA = new ConfigValue<>(500.0);

    // Magic Circle Soul Anchor (Reincarnation & Soul Recall) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_SOUL_ANCHOR_MANA = new ConfigValue<>(1000.0);
    public static final ConfigValue<Double> MAGIC_CIRCLE_SOUL_ANCHOR_RECALL_COST = new ConfigValue<>(800.0);

    // Magic Circle Magic Creation (Ritual Transmutation) Configs
    public static final ConfigValue<Double> MAGIC_CIRCLE_CREATION_MANA_MULT = new ConfigValue<>(1.0);

    // Modular Anvil & Modular Weapon Configs
    public static final ConfigValue<Double> MODULAR_ANVIL_BASE_MANA_COST = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> SABERTOOTH_CORE_DROP_CHANCE = new ConfigValue<>(0.15); // 15% drop chance on butchering
    public static final ConfigValue<Double> MODULAR_WEAPON_QUALITY_MAX_BONUS = new ConfigValue<>(0.25);

    // Village Mage & Ancient Manuscript Configs
    public static final ConfigValue<Integer> MAGE_HOUSE_VILLAGE_WEIGHT = new ConfigValue<>(1); // Village jigsaw pool weight
    public static final ConfigValue<Double> MAGE_MAGIC_BOOK_TRADE_CHANCE = new ConfigValue<>(0.20);
    public static final ConfigValue<Double> MAGE_ANCIENT_MANUSCRIPT_TRADE_CHANCE = new ConfigValue<>(0.10);
    public static final ConfigValue<Double> DUNGEON_ANCIENT_MANUSCRIPT_CHANCE = new ConfigValue<>(0.04);
}

