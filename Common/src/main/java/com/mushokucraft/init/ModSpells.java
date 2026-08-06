package com.mushokucraft.init;

import com.mushokucraft.magic.MagicSchool;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.magic.SpellRank;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class ModSpells {
    public static final Map<ResourceLocation, Spell> SPELLS = new HashMap<>();

    public static final Spell WATERBALL = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball"), MagicSchool.WATER, SpellRank.ELEMENTARY)
            .castTime(60f).manaCost(10f).fizzleChance(0.3f)
            .incantation("spell.mushokucraft.waterball.incantation")
            .projectile(com.mushokucraft.magic.entity.WaterballEntity::new, 1.5f, 1.0f)
            .build());

    public static final Spell FIREBALL = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball"), MagicSchool.FIRE, SpellRank.ELEMENTARY)
            .castTime(60f).manaCost(15f).fizzleChance(0.3f)
            .incantation("spell.mushokucraft.fireball.incantation")
            .projectile(com.mushokucraft.magic.entity.FireballEntity::new, 1.5f, 1.0f)
            .build());

    public static final Spell WATER_SLICE = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "water_slice"), MagicSchool.WATER, SpellRank.INTERMEDIATE)
            .castTime(com.mushokucraft.config.MushokuConfig.WATER_SLICE_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.WATER_SLICE_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.WATER_SLICE_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.water_slice.incantation")
            .projectile(com.mushokucraft.magic.entity.WaterSliceEntity::new, 3.0f, 0.0f)
            .build());

    public static final Spell ROCK_BULLET = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "rockbullet"), MagicSchool.EARTH, SpellRank.ELEMENTARY)
            .castTime(40f).manaCost(10f).fizzleChance(0.3f)
            .incantation("spell.mushokucraft.rockbullet.incantation")
            .projectile(com.mushokucraft.magic.entity.RockBulletEntity::new, 2.0f, 0.5f)
            .build());

    public static final Spell AIR_STRIKE = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike"), MagicSchool.WIND, SpellRank.ELEMENTARY)
            .castTime(40f).manaCost(10f).fizzleChance(0.3f)
            .incantation("spell.mushokucraft.airstrike.incantation")
            .projectile(com.mushokucraft.magic.entity.AirStrikeEntity::new, 2.5f, 0.0f)
            .build());

    public static final Spell ICICLE_BREAK = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "icicle_break"), MagicSchool.WATER, SpellRank.ADVANCED)
            .castTime(com.mushokucraft.config.MushokuConfig.ICICLE_BREAK_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.ICICLE_BREAK_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.ICICLE_BREAK_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.icicle_break.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.IcicleBreakTargetEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell CUMULONIMBUS = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "cumulonimbus"), MagicSchool.WATER, SpellRank.SAINT)
            .castTime(com.mushokucraft.config.MushokuConfig.CUMULONIMBUS_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.CUMULONIMBUS_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.CUMULONIMBUS_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.cumulonimbus.incantation")
            .channeled(true)
            .projectile(com.mushokucraft.magic.entity.CumulonimbusStormEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell LONGSWORD_LIGHT = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "longsword_light"), MagicSchool.SWORD_ARTS, SpellRank.ADVANCED)
            .castTime(0f).manaCost(0f).fizzleChance(0f)
            .incantation("spell.mushokucraft.longsword_light.incantation")
            .build());

    public static final Spell LONGSWORD_SILENCE = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "longsword_of_silence"), MagicSchool.SWORD_ARTS, SpellRank.ADVANCED)
            .castTime(0f).manaCost(0f).fizzleChance(0f)
            .incantation("spell.mushokucraft.longsword_of_silence.incantation")
            .build());

    public static final Spell AIR_CUSHION = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "air_cushion"), MagicSchool.WIND, SpellRank.INTERMEDIATE)
            .castTime(80f).manaCost(0f).fizzleChance(0f)
            .incantation("spell.mushokucraft.air_cushion.incantation")
            .build());

    private static Spell register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
        return spell;
    }
}





