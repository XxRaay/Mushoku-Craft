/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$BooleanValue
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 *  net.neoforged.neoforge.common.ModConfigSpec$EnumValue
 *  net.neoforged.neoforge.common.ModConfigSpec$IntValue
 */
package com.mushokucraft.config;

import com.mushokucraft.client.hud.ManaDisplayMode;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MushokuConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.EnumValue<ManaDisplayMode> MANA_DISPLAY_MODE;
    public static final ModConfigSpec.DoubleValue MANA_BAR_OPACITY;
    public static final ModConfigSpec.BooleanValue SHOW_MANA_NUMBERS;
    public static final ModConfigSpec.BooleanValue SHOW_SCHOOL_ICON;
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.DoubleValue DEFAULT_MANA;
    public static final ModConfigSpec.DoubleValue DEFAULT_MAX_MANA;
    public static final ModConfigSpec.DoubleValue DEFAULT_MANA_REGEN_RATE;
    public static final ModConfigSpec.DoubleValue MANA_COST_REDUCTION_PER_MASTERY;
    public static final ModConfigSpec.DoubleValue SPELL_MASTERY_PER_CAST;
    public static final ModConfigSpec.DoubleValue SCHOOL_MASTERY_PER_CAST;
    public static final ModConfigSpec.DoubleValue LEARNING_SUCCESS_BASE_CHANCE;
    public static final ModConfigSpec.DoubleValue LEARNING_MASTERY_GAIN;
    public static final ModConfigSpec.IntValue QTE_PERFECT_TIME_BONUS_TICKS;
    public static final ModConfigSpec.DoubleValue QTE_SPEED_REDUCTION_PER_MASTERY;
    public static final ModConfigSpec.DoubleValue QTE_SIZE_INCREASE_PER_MASTERY;
    public static final ModConfigSpec.IntValue CHARGE_MAX_TICKS;
    public static final ModConfigSpec.DoubleValue CHARGE_TOTAL_MANA_DRAIN;
    public static final ModConfigSpec.DoubleValue CHARGE_MIN_SCALE;
    public static final ModConfigSpec.DoubleValue CHARGE_MAX_SCALE;
    public static final ModConfigSpec.DoubleValue CHARGE_MASTERY_BONUS_SPELL;
    public static final ModConfigSpec.DoubleValue CHARGE_MASTERY_BONUS_SCHOOL;
    public static final ModConfigSpec.DoubleValue SWORD_GOD_SPEED_BONUS;
    public static final ModConfigSpec.DoubleValue SWORD_GOD_REACH_BONUS;
    public static final ModConfigSpec.DoubleValue SWORD_GOD_HIGH_DAMAGE_THRESHOLD;
    public static final ModConfigSpec.DoubleValue SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN;
    public static final ModConfigSpec.IntValue PARRY_WINDOW_TICKS;
    public static final ModConfigSpec.IntValue PARRY_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue COUNTER_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue PROJECTILE_DEFLECT_SPEED_MULT;
    public static final ModConfigSpec.DoubleValue WATER_GOD_PARRY_MASTERY_GAIN;
    public static final ModConfigSpec.IntValue SHIELD_BLOCKS_FOR_WATER_GOD;
    public static final ModConfigSpec.DoubleValue NORTH_GOD_FALL_ABSORB_BASE;
    public static final ModConfigSpec.DoubleValue NORTH_GOD_FALL_ABSORB_SCALING;
    public static final ModConfigSpec.IntValue NORTH_GOD_SLOW_DURATION;
    public static final ModConfigSpec.IntValue NORTH_GOD_BLEED_DURATION;
    public static final ModConfigSpec.DoubleValue NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN;
    public static final ModConfigSpec.DoubleValue LOW_HP_THRESHOLD;
    public static final ModConfigSpec.DoubleValue TOUKI_MIN_MASTERY;
    public static final ModConfigSpec.DoubleValue TOUKI_DAMAGE_BASE;
    public static final ModConfigSpec.DoubleValue TOUKI_DAMAGE_SCALING;
    public static final ModConfigSpec.DoubleValue TOUKI_SPEED_BASE;
    public static final ModConfigSpec.DoubleValue TOUKI_SPEED_SCALING;
    public static final ModConfigSpec.DoubleValue TOUKI_JUMP_BASE;
    public static final ModConfigSpec.DoubleValue TOUKI_JUMP_SCALING;
    public static final ModConfigSpec.DoubleValue TOUKI_KB_RES_BASE;
    public static final ModConfigSpec.DoubleValue TOUKI_KB_RES_SCALING;
    public static final ModConfigSpec.DoubleValue TOUKI_MANA_DRAIN_BASE;
    public static final ModConfigSpec.DoubleValue TOUKI_MANA_DRAIN_SCALING;
    public static final ModConfigSpec.DoubleValue DUMMY_MASTERY_GAIN;
    public static final ModConfigSpec.DoubleValue THROWN_SWORD_DAMAGE;
    public static final ModConfigSpec.IntValue THROWN_SWORD_PICKUP_DELAY;
    public static final ModConfigSpec.DoubleValue BLEEDING_BASE_DAMAGE;
    public static final ModConfigSpec.IntValue BLEEDING_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue LONGSWORD_LIGHT_DASH_DISTANCE;
    public static final ModConfigSpec.DoubleValue LONGSWORD_LIGHT_DAMAGE;
    public static final ModConfigSpec.IntValue LONGSWORD_LIGHT_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue LONGSWORD_LIGHT_HIT_RADIUS_SQ;
    public static final ModConfigSpec.DoubleValue LONGSWORD_LIGHT_HITBOX_INFLATE;
    public static final ModConfigSpec.DoubleValue LONGSWORD_SILENCE_DASH_DISTANCE;
    public static final ModConfigSpec.DoubleValue LONGSWORD_SILENCE_DAMAGE;
    public static final ModConfigSpec.DoubleValue LONGSWORD_SILENCE_MANA_COST_PERCENT;
    public static final ModConfigSpec.IntValue LONGSWORD_SILENCE_CAST_TICKS;
    public static final ModConfigSpec.IntValue LONGSWORD_SILENCE_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue LONGSWORD_SILENCE_HIT_RADIUS_SQ;
    public static final ModConfigSpec.DoubleValue LONGSWORD_SILENCE_HITBOX_INFLATE;

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        client.comment("Mana HUD Settings").push("hud");
        MANA_DISPLAY_MODE = client.comment(new String[]{"Mana display mode:", " CLASSIC  \u2014 Bar above the hotbar (classic RPG style)", " DYNAMIC  \u2014 Arc near crosshair (clean combat HUD)", " HYBRID   \u2014 Both (arc for real-time, bar for stats)", " MINIMAL  \u2014 Hidden until mana changes"}).translation("mushokucraft.config.mana_display_mode").defineEnum("manaDisplayMode", (Enum)ManaDisplayMode.CLASSIC);
        MANA_BAR_OPACITY = client.comment("Overall opacity of the mana HUD (0.3 to 1.0)").translation("mushokucraft.config.mana_bar_opacity").defineInRange("manaBarOpacity", 0.85, 0.3, 1.0);
        SHOW_MANA_NUMBERS = client.comment("Show numeric mana values (e.g. 250 / 250) on the classic bar").translation("mushokucraft.config.show_mana_numbers").define("showManaNumbers", true);
        SHOW_SCHOOL_ICON = client.comment("Show current magic school icon next to the mana bar").translation("mushokucraft.config.show_school_icon").define("showSchoolIcon", true);
        client.pop();
        CLIENT_SPEC = client.build();
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();
        server.comment("Mana defaults for new players").push("mana");
        DEFAULT_MANA = server.comment("Starting mana").defineInRange("defaultMana", 100.0, 0.0, 10000.0);
        DEFAULT_MAX_MANA = server.comment("Starting max mana").defineInRange("defaultMaxMana", 100.0, 1.0, 10000.0);
        DEFAULT_MANA_REGEN_RATE = server.comment("Passive mana regen per tick").defineInRange("defaultManaRegenRate", 0.05, 0.0, 100.0);
        MANA_COST_REDUCTION_PER_MASTERY = server.comment("Max mana cost reduction from school mastery (0.3 = 30%)").defineInRange("manaCostReductionPerMastery", 0.3, 0.0, 1.0);
        server.pop();
        server.comment("Magic casting and learning tuning").push("magic");
        SPELL_MASTERY_PER_CAST = server.comment("Spell mastery XP gained per successful cast").defineInRange("spellMasteryPerCast", 0.05, 0.0, 1.0);
        SCHOOL_MASTERY_PER_CAST = server.comment("School mastery XP gained per successful cast").defineInRange("schoolMasteryPerCast", 0.01, 0.0, 1.0);
        LEARNING_SUCCESS_BASE_CHANCE = server.comment("Base success chance for spell learning QTE rolls").defineInRange("learningSuccessBaseChance", 0.25, 0.0, 1.0);
        LEARNING_MASTERY_GAIN = server.comment("Mastery gained upon successfully learning a spell").defineInRange("learningMasteryGain", 0.1, 0.0, 1.0);
        QTE_PERFECT_TIME_BONUS_TICKS = server.comment("Ticks of cast time reduction for a perfect QTE (40 = 2s)").defineInRange("qtePerfectTimeBonus", 40, 0, 200);
        QTE_SPEED_REDUCTION_PER_MASTERY = server.comment("QTE animation speed reduction per school mastery point").defineInRange("qteSpeedReductionPerMastery", 0.1, 0.0, 1.0);
        QTE_SIZE_INCREASE_PER_MASTERY = server.comment("QTE target size increase per school mastery point").defineInRange("qteSizeIncreasePerMastery", 0.2, 0.0, 2.0);
        server.pop();
        server.comment("Hold-to-charge spell tuning").push("charge");
        CHARGE_MAX_TICKS = server.comment("Maximum charge duration in ticks (60 = 3s)").defineInRange("chargeMaxTicks", 60, 1, 600);
        CHARGE_TOTAL_MANA_DRAIN = server.comment("Total mana drained during a full charge").defineInRange("chargeTotalManaDrain", 125.0, 0.0, 10000.0);
        CHARGE_MIN_SCALE = server.comment("Projectile scale at start of charge").defineInRange("chargeMinScale", 0.1, 0.01, 10.0);
        CHARGE_MAX_SCALE = server.comment("Projectile scale at full charge").defineInRange("chargeMaxScale", 3.0, 0.1, 50.0);
        CHARGE_MASTERY_BONUS_SPELL = server.comment("Bonus spell mastery XP for max charge").defineInRange("chargeMasteryBonusSpell", 0.05, 0.0, 1.0);
        CHARGE_MASTERY_BONUS_SCHOOL = server.comment("Bonus school mastery XP for max charge").defineInRange("chargeMasteryBonusSchool", 0.02, 0.0, 1.0);
        server.pop();
        server.comment("Sword God style tuning").push("swordGod");
        SWORD_GOD_SPEED_BONUS = server.comment("Attack speed multiplier bonus").defineInRange("speedBonus", 1.5, 0.0, 10.0);
        SWORD_GOD_REACH_BONUS = server.comment("Extra entity interaction range (blocks)").defineInRange("reachBonus", 1.5, 0.0, 10.0);
        SWORD_GOD_HIGH_DAMAGE_THRESHOLD = server.comment("Damage threshold for Sword God mastery gain").defineInRange("highDamageThreshold", 12.0, 0.0, 1000.0);
        SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN = server.comment("Mastery gain for exceeding the damage threshold").defineInRange("highDamageMasteryGain", 0.002, 0.0, 1.0);
        server.pop();
        server.comment("Water God style tuning").push("waterGod");
        PARRY_WINDOW_TICKS = server.comment("Parry active window in ticks (10 = 0.5s)").defineInRange("parryWindowTicks", 10, 1, 100);
        PARRY_COOLDOWN_TICKS = server.comment("Parry cooldown in ticks (40 = 2s)").defineInRange("parryCooldownTicks", 40, 1, 600);
        COUNTER_ATTACK_DAMAGE = server.comment("Damage dealt by successful parry counter").defineInRange("counterAttackDamage", 5.0, 0.0, 1000.0);
        PROJECTILE_DEFLECT_SPEED_MULT = server.comment("Velocity multiplier for deflected projectiles (negative = reverse)").defineInRange("projectileDeflectSpeedMult", -1.5, -10.0, 10.0);
        WATER_GOD_PARRY_MASTERY_GAIN = server.comment("Mastery gain per successful parry").defineInRange("parryMasteryGain", 0.01, 0.0, 1.0);
        SHIELD_BLOCKS_FOR_WATER_GOD = server.comment("Shield blocks required to unlock Water God style").defineInRange("shieldBlocksToUnlock", 25, 1, 1000);
        server.pop();
        server.comment("North God style tuning").push("northGod");
        NORTH_GOD_FALL_ABSORB_BASE = server.comment("Base fall damage absorption (blocks)").defineInRange("fallAbsorbBase", 4.0, 0.0, 100.0);
        NORTH_GOD_FALL_ABSORB_SCALING = server.comment("Additional fall damage absorption per mastery point").defineInRange("fallAbsorbScaling", 12.0, 0.0, 100.0);
        NORTH_GOD_SLOW_DURATION = server.comment("Slowness duration on hit (ticks)").defineInRange("slowDuration", 100, 0, 600);
        NORTH_GOD_BLEED_DURATION = server.comment("Bleeding duration on hit (ticks)").defineInRange("bleedDuration", 100, 0, 600);
        NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN = server.comment("Mastery gain for low-HP kill").defineInRange("lowHpKillMasteryGain", 0.02, 0.0, 1.0);
        LOW_HP_THRESHOLD = server.comment("Health threshold for low-HP kill bonus (in half-hearts)").defineInRange("lowHpThreshold", 2.0, 0.0, 40.0);
        server.pop();
        server.comment("Touki (Battle Aura) tuning").push("touki");
        TOUKI_MIN_MASTERY = server.comment("Minimum mastery to keep Touki active").defineInRange("minMastery", 0.25, 0.0, 1.0);
        TOUKI_DAMAGE_BASE = server.comment("Sword God Touki base damage bonus").defineInRange("damageBase", 2.0, 0.0, 100.0);
        TOUKI_DAMAGE_SCALING = server.comment("Sword God Touki damage scaling per mastery").defineInRange("damageScaling", 5.0, 0.0, 100.0);
        TOUKI_SPEED_BASE = server.comment("North God Touki base speed bonus").defineInRange("speedBase", 0.2, 0.0, 10.0);
        TOUKI_SPEED_SCALING = server.comment("North God Touki speed scaling per mastery").defineInRange("speedScaling", 0.8, 0.0, 10.0);
        TOUKI_JUMP_BASE = server.comment("North God Touki base jump bonus").defineInRange("jumpBase", 0.2, 0.0, 10.0);
        TOUKI_JUMP_SCALING = server.comment("North God Touki jump scaling per mastery").defineInRange("jumpScaling", 0.5, 0.0, 10.0);
        TOUKI_KB_RES_BASE = server.comment("Water God Touki base knockback resistance").defineInRange("kbResBase", 0.5, 0.0, 1.0);
        TOUKI_KB_RES_SCALING = server.comment("Water God Touki knockback resistance scaling per mastery").defineInRange("kbResScaling", 0.5, 0.0, 1.0);
        TOUKI_MANA_DRAIN_BASE = server.comment("Base mana drain per second while Touki is active").defineInRange("manaDrainBase", 2.0, 0.0, 100.0);
        TOUKI_MANA_DRAIN_SCALING = server.comment("Additional mana drain per mastery while Touki is active").defineInRange("manaDrainScaling", 8.0, 0.0, 100.0);
        server.pop();
        server.comment("Miscellaneous combat values").push("combatMisc");
        DUMMY_MASTERY_GAIN = server.comment("Mastery gain for hitting training dummies").defineInRange("dummyMasteryGain", 0.001, 0.0, 1.0);
        THROWN_SWORD_DAMAGE = server.comment("Damage dealt by thrown swords (North God)").defineInRange("thrownSwordDamage", 6.0, 0.0, 1000.0);
        THROWN_SWORD_PICKUP_DELAY = server.comment("Pickup delay for dropped swords (ticks)").defineInRange("thrownSwordPickupDelay", 10, 0, 200);
        BLEEDING_BASE_DAMAGE = server.comment("Base damage per bleeding tick").defineInRange("bleedingBaseDamage", 1.0, 0.0, 100.0);
        BLEEDING_TICK_INTERVAL = server.comment("Ticks between bleeding damage (at amplifier 0)").defineInRange("bleedingTickInterval", 40, 1, 200);
        server.pop();
        server.comment("Longsword of Light (Sword God ultimate)").push("longswordLight");
        LONGSWORD_LIGHT_DASH_DISTANCE = server.comment("Dash distance in blocks").defineInRange("dashDistance", 7.0, 1.0, 100.0);
        LONGSWORD_LIGHT_DAMAGE = server.comment("Damage dealt to entities in the dash path").defineInRange("damage", 30.0, 0.0, 10000.0);
        LONGSWORD_LIGHT_COOLDOWN_TICKS = server.comment("Cooldown in ticks (900 = 45s)").defineInRange("cooldownTicks", 900, 0, 72000);
        LONGSWORD_LIGHT_HIT_RADIUS_SQ = server.comment("Squared hit detection radius for dash targets").defineInRange("hitRadiusSq", 4.0, 0.0, 100.0);
        LONGSWORD_LIGHT_HITBOX_INFLATE = server.comment("Hitbox inflation for entity scan").defineInRange("hitboxInflate", 2.0, 0.0, 50.0);
        server.pop();
        server.comment("Longsword of Silence (advanced Sword God ultimate)").push("longswordSilence");
        LONGSWORD_SILENCE_DASH_DISTANCE = server.comment("Dash distance in blocks").defineInRange("dashDistance", 15.0, 1.0, 200.0);
        LONGSWORD_SILENCE_DAMAGE = server.comment("Damage dealt to entities in the dash path").defineInRange("damage", 60.0, 0.0, 10000.0);
        LONGSWORD_SILENCE_MANA_COST_PERCENT = server.comment("Mana cost as percentage of max mana (0.65 = 65%)").defineInRange("manaCostPercent", 0.65, 0.0, 1.0);
        LONGSWORD_SILENCE_CAST_TICKS = server.comment("Cast delay in ticks (10 = 0.5s)").defineInRange("castTicks", 10, 0, 200);
        LONGSWORD_SILENCE_COOLDOWN_TICKS = server.comment("Cooldown in ticks (3600 = 3min)").defineInRange("cooldownTicks", 3600, 0, 72000);
        LONGSWORD_SILENCE_HIT_RADIUS_SQ = server.comment("Squared hit detection radius").defineInRange("hitRadiusSq", 9.0, 0.0, 100.0);
        LONGSWORD_SILENCE_HITBOX_INFLATE = server.comment("Hitbox inflation for entity scan").defineInRange("hitboxInflate", 3.0, 0.0, 50.0);
        server.pop();
        SERVER_SPEC = server.build();
    }
}

