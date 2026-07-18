/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mushokucraft.init;

import com.mushokucraft.magic.MagicSchool;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.magic.SpellRank;
import com.mushokucraft.magic.entity.AirStrikeEntity;
import com.mushokucraft.magic.entity.FireballEntity;
import com.mushokucraft.magic.entity.RockBulletEntity;
import com.mushokucraft.magic.entity.WaterballEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class ModSpells {
    public static final Map<ResourceLocation, Spell> SPELLS = new HashMap<ResourceLocation, Spell>();
    public static final Spell WATERBALL = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"waterball"), MagicSchool.WATER, SpellRank.ELEMENTARY).castTime(60.0f).manaCost(10.0f).fizzleChance(0.3f).incantation("spell.mushokucraft.waterball.incantation").projectile(WaterballEntity::new, 1.5f, 1.0f).build());
    public static final Spell FIREBALL = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"fireball"), MagicSchool.FIRE, SpellRank.ELEMENTARY).castTime(60.0f).manaCost(15.0f).fizzleChance(0.3f).incantation("spell.mushokucraft.fireball.incantation").projectile(FireballEntity::new, 1.5f, 1.0f).build());
    public static final Spell ROCK_BULLET = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"rockbullet"), MagicSchool.EARTH, SpellRank.ELEMENTARY).castTime(40.0f).manaCost(10.0f).fizzleChance(0.3f).incantation("spell.mushokucraft.rockbullet.incantation").projectile(RockBulletEntity::new, 2.0f, 0.5f).build());
    public static final Spell AIR_STRIKE = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"airstrike"), MagicSchool.WIND, SpellRank.ELEMENTARY).castTime(40.0f).manaCost(10.0f).fizzleChance(0.3f).incantation("spell.mushokucraft.airstrike.incantation").projectile(AirStrikeEntity::new, 2.5f, 0.0f).build());
    public static final Spell LONGSWORD_LIGHT = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"longsword_light"), MagicSchool.SWORD_ARTS, SpellRank.ADVANCED).castTime(0.0f).manaCost(0.0f).fizzleChance(0.0f).incantation("spell.mushokucraft.longsword_light.incantation").build());
    public static final Spell LONGSWORD_SILENCE = ModSpells.register(new Spell.Builder(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"longsword_of_silence"), MagicSchool.SWORD_ARTS, SpellRank.ADVANCED).castTime(0.0f).manaCost(0.0f).fizzleChance(0.0f).incantation("spell.mushokucraft.longsword_of_silence.incantation").build());

    private static Spell register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
        return spell;
    }
}

