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
            .castTime(com.mushokucraft.config.MushokuConfig.WATERBALL_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.WATERBALL_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.WATERBALL_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.waterball.incantation")
            .projectile(com.mushokucraft.magic.entity.WaterballEntity::new, 1.5f, 1.0f)
            .build());

    public static final Spell FIREBALL = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball"), MagicSchool.FIRE, SpellRank.ELEMENTARY)
            .castTime(com.mushokucraft.config.MushokuConfig.FIREBALL_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.FIREBALL_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.FIREBALL_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.fireball.incantation")
            .projectile(com.mushokucraft.magic.entity.FireballEntity::new, 1.5f, 1.0f)
            .build());

    public static final Spell FLAME_PILLAR = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "flame_pillar"), MagicSchool.FIRE, SpellRank.INTERMEDIATE)
            .castTime(com.mushokucraft.config.MushokuConfig.FLAME_PILLAR_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.FLAME_PILLAR_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.FLAME_PILLAR_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.flame_pillar.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.FlamePillarEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell EXODUS_FLAME = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "exodus_flame"), MagicSchool.FIRE, SpellRank.ADVANCED)
            .castTime(com.mushokucraft.config.MushokuConfig.EXODUS_FLAME_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.EXODUS_FLAME_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.EXODUS_FLAME_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.exodus_flame.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.ExodusFlameEntity::new, com.mushokucraft.config.MushokuConfig.EXODUS_FLAME_SPEED.get().floatValue(), 0.2f)
            .build());

    public static final Spell FLASHOVER = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "flashover"), MagicSchool.FIRE, SpellRank.SAINT)
            .castTime(com.mushokucraft.config.MushokuConfig.FLASHOVER_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.FLASHOVER_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.FLASHOVER_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.flashover.incantation")
            .channeled(true)
            .projectile(com.mushokucraft.magic.entity.FlashoverEntity::new, 0.0f, 0.0f)
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
            .castTime(com.mushokucraft.config.MushokuConfig.ROCK_BULLET_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.ROCK_BULLET_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.ROCK_BULLET_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.rockbullet.incantation")
            .projectile(com.mushokucraft.magic.entity.RockBulletEntity::new, 2.0f, 0.5f)
            .build());

    public static final Spell EARTH_LANCE = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "earth_lance"), MagicSchool.EARTH, SpellRank.INTERMEDIATE)
            .castTime(com.mushokucraft.config.MushokuConfig.EARTH_LANCE_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.EARTH_LANCE_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.EARTH_LANCE_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.earth_lance.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.EarthLanceEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell STONE_PILLAR = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "stone_pillar"), MagicSchool.EARTH, SpellRank.ADVANCED)
            .castTime(com.mushokucraft.config.MushokuConfig.STONE_PILLAR_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.STONE_PILLAR_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.STONE_PILLAR_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.stone_pillar.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.StonePillarEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell SANDSTORM = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "sandstorm"), MagicSchool.EARTH, SpellRank.SAINT)
            .castTime(com.mushokucraft.config.MushokuConfig.SANDSTORM_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.SANDSTORM_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.SANDSTORM_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.sandstorm.incantation")
            .channeled(true)
            .projectile(com.mushokucraft.magic.entity.SandstormEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell AIR_STRIKE = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike"), MagicSchool.WIND, SpellRank.ELEMENTARY)
            .castTime(com.mushokucraft.config.MushokuConfig.AIR_STRIKE_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.AIR_STRIKE_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.AIR_STRIKE_FIZZLE_CHANCE.get().floatValue())
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
            .castTime(com.mushokucraft.config.MushokuConfig.AIR_CUSHION_CAST_TIME_TICKS.get().floatValue())
            .manaCost(0f)
            .fizzleChance(com.mushokucraft.config.MushokuConfig.AIR_CUSHION_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.air_cushion.incantation")
            .action((level, player, spell) -> {
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    com.mushokucraft.magic.AirCushionManager.toggle(sp);
                }
            })
            .build());

    public static final Spell TORNADO = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "tornado"), MagicSchool.WIND, SpellRank.ADVANCED)
            .castTime(com.mushokucraft.config.MushokuConfig.TORNADO_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.TORNADO_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.TORNADO_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.tornado.incantation")
            .channeled(false)
            .projectile(com.mushokucraft.magic.entity.TornadoEntity::new, 0.0f, 0.0f)
            .build());

    public static final Spell TYPHOON = register(new Spell.Builder(
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "typhoon"), MagicSchool.WIND, SpellRank.SAINT)
            .castTime(com.mushokucraft.config.MushokuConfig.TYPHOON_CAST_TIME_TICKS.get().floatValue())
            .manaCost(com.mushokucraft.config.MushokuConfig.TYPHOON_MANA_COST.get().floatValue())
            .fizzleChance(com.mushokucraft.config.MushokuConfig.TYPHOON_FIZZLE_CHANCE.get().floatValue())
            .incantation("spell.mushokucraft.typhoon.incantation")
            .channeled(true)
            .projectile(com.mushokucraft.magic.entity.TyphoonEntity::new, 0.0f, 0.0f)
            .build());

    private static Spell register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
        return spell;
    }
}





