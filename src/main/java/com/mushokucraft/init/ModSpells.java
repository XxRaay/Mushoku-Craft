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

    private static Spell register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
        return spell;
    }
}





