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
    
    public static final ConfigValue<Double> DEFAULT_MANA = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> DEFAULT_MAX_MANA = new ConfigValue<>(100.0);
    public static final ConfigValue<Double> DEFAULT_MANA_REGEN_RATE = new ConfigValue<>(0.05);
    public static final ConfigValue<Double> MANA_COST_REDUCTION_PER_MASTERY = new ConfigValue<>(0.3);
    public static final ConfigValue<Double> SPELL_MASTERY_PER_CAST = new ConfigValue<>(0.05);
    public static final ConfigValue<Double> SCHOOL_MASTERY_PER_CAST = new ConfigValue<>(0.01);
    public static final ConfigValue<Double> LEARNING_SUCCESS_BASE_CHANCE = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> LEARNING_MASTERY_GAIN = new ConfigValue<>(0.1);
    public static final ConfigValue<Integer> QTE_PERFECT_TIME_BONUS_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Double> QTE_SPEED_REDUCTION_PER_MASTERY = new ConfigValue<>(0.1);
    public static final ConfigValue<Double> QTE_SIZE_INCREASE_PER_MASTERY = new ConfigValue<>(0.2);
    public static final ConfigValue<Integer> CHARGE_MAX_TICKS = new ConfigValue<>(60);
    public static final ConfigValue<Double> CHARGE_TOTAL_MANA_DRAIN = new ConfigValue<>(125.0);
    public static final ConfigValue<Double> CHARGE_MIN_SCALE = new ConfigValue<>(0.1);
    public static final ConfigValue<Double> CHARGE_MAX_SCALE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> CHARGE_MASTERY_BONUS_SPELL = new ConfigValue<>(0.05);
    public static final ConfigValue<Double> CHARGE_MASTERY_BONUS_SCHOOL = new ConfigValue<>(0.02);
    public static final ConfigValue<Double> HUNTING_KNIFE_DAMAGE = new ConfigValue<>(0.5);
    public static final ConfigValue<Integer> LONGSWORD_LIGHT_CAST_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> LONGSWORD_LIGHT_COOLDOWN_TICKS = new ConfigValue<>(900);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_MANA_COST_PERCENT = new ConfigValue<>(0.65);
    public static final ConfigValue<Integer> LONGSWORD_SILENCE_CAST_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> LONGSWORD_SILENCE_COOLDOWN_TICKS = new ConfigValue<>(3600);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_DASH_DISTANCE = new ConfigValue<>(15.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_HITBOX_INFLATE = new ConfigValue<>(3.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_HIT_RADIUS_SQ = new ConfigValue<>(9.0);
    public static final ConfigValue<Double> LONGSWORD_SILENCE_DAMAGE = new ConfigValue<>(60.0);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_DASH_DISTANCE = new ConfigValue<>(7.0);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_HITBOX_INFLATE = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_HIT_RADIUS_SQ = new ConfigValue<>(4.0);
    public static final ConfigValue<Double> LONGSWORD_LIGHT_DAMAGE = new ConfigValue<>(30.0);
    public static final ConfigValue<Double> BLEEDING_BASE_DAMAGE = new ConfigValue<>(1.0);
    public static final ConfigValue<Integer> BLEEDING_TICK_INTERVAL = new ConfigValue<>(40);
    public static final ConfigValue<Double> LOW_HP_THRESHOLD = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN = new ConfigValue<>(0.02);
    public static final ConfigValue<Integer> SHIELD_BLOCKS_FOR_WATER_GOD = new ConfigValue<>(25);
    public static final ConfigValue<Double> DUMMY_MASTERY_GAIN = new ConfigValue<>(0.001);
    public static final ConfigValue<Double> SWORD_GOD_HIGH_DAMAGE_THRESHOLD = new ConfigValue<>(12.0);
    public static final ConfigValue<Double> SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN = new ConfigValue<>(0.002);
    public static final ConfigValue<Double> TOUKI_MANA_DRAIN_BASE = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> TOUKI_MANA_DRAIN_SCALING = new ConfigValue<>(8.0);
    public static final ConfigValue<Double> TOUKI_DAMAGE_BASE = new ConfigValue<>(2.0);
    public static final ConfigValue<Double> TOUKI_DAMAGE_SCALING = new ConfigValue<>(5.0);
    public static final ConfigValue<Double> TOUKI_KB_RES_BASE = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> TOUKI_KB_RES_SCALING = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> TOUKI_SPEED_BASE = new ConfigValue<>(0.2);
    public static final ConfigValue<Double> TOUKI_SPEED_SCALING = new ConfigValue<>(0.8);
    public static final ConfigValue<Double> TOUKI_JUMP_BASE = new ConfigValue<>(0.2);
    public static final ConfigValue<Double> TOUKI_JUMP_SCALING = new ConfigValue<>(0.5);
    public static final ConfigValue<Double> NORTH_GOD_FALL_ABSORB_BASE = new ConfigValue<>(4.0);
    public static final ConfigValue<Double> NORTH_GOD_FALL_ABSORB_SCALING = new ConfigValue<>(12.0);
    public static final ConfigValue<Integer> NORTH_GOD_SLOW_DURATION = new ConfigValue<>(100);
    public static final ConfigValue<Integer> NORTH_GOD_BLEED_DURATION = new ConfigValue<>(100);
    public static final ConfigValue<Integer> PARRY_WINDOW_TICKS = new ConfigValue<>(10);
    public static final ConfigValue<Integer> PARRY_COOLDOWN_TICKS = new ConfigValue<>(40);
    public static final ConfigValue<Double> WATER_GOD_PARRY_MASTERY_GAIN = new ConfigValue<>(0.01);
    public static final ConfigValue<Double> PROJECTILE_DEFLECT_SPEED_MULT = new ConfigValue<>(-1.5);
    public static final ConfigValue<Double> COUNTER_ATTACK_DAMAGE = new ConfigValue<>(5.0);
    public static final ConfigValue<Double> TOUKI_MIN_MASTERY = new ConfigValue<>(0.25);
    public static final ConfigValue<Double> SWORD_GOD_SPEED_BONUS = new ConfigValue<>(1.5);
    public static final ConfigValue<Double> SWORD_GOD_REACH_BONUS = new ConfigValue<>(1.5);
    public static final ConfigValue<Integer> CARCASS_MAX_USES = new ConfigValue<>(3);
    public static final ConfigValue<Double> THROWN_SWORD_DAMAGE = new ConfigValue<>(6.0);
    public static final ConfigValue<Integer> THROWN_SWORD_PICKUP_DELAY = new ConfigValue<>(10);
}
