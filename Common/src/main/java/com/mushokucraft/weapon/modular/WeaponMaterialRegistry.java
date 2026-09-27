package com.mushokucraft.weapon.modular;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponMaterialRegistry {
    private static final Map<String, WeaponMaterial> MATERIALS = new LinkedHashMap<>();

    // Metals & Minerals
    public static final WeaponMaterial IRON = register(new WeaponMaterial(
            "iron", "material.mushokucraft.iron", 0xDCDCDC, 2.0f, 0.0f, 250, 0.0f, "material.mushokucraft.iron.perk"
    ));
    public static final WeaponMaterial GOLD = register(new WeaponMaterial(
            "gold", "material.mushokucraft.gold", 0xFFD700, 1.5f, 0.15f, 45, 0.0f, "material.mushokucraft.gold.perk"
    ));
    public static final WeaponMaterial COPPER = register(new WeaponMaterial(
            "copper", "material.mushokucraft.copper", 0xE07850, 1.8f, 0.0f, 180, 0.0f, "material.mushokucraft.copper.perk"
    ));
    public static final WeaponMaterial DIAMOND = register(new WeaponMaterial(
            "diamond", "material.mushokucraft.diamond", 0x4AEDD9, 3.5f, 0.05f, 1561, 0.05f, "material.mushokucraft.diamond.perk"
    ));
    public static final WeaponMaterial NETHERITE = register(new WeaponMaterial(
            "netherite", "material.mushokucraft.netherite", 0x8A7E8A, 5.0f, -0.15f, 2031, 0.1f, "material.mushokucraft.netherite.perk"
    ));
    public static final WeaponMaterial LAPIS = register(new WeaponMaterial(
            "lapis", "material.mushokucraft.lapis", 0x345EC8, 1.6f, 0.05f, 300, 0.0f, "material.mushokucraft.lapis.perk"
    ));
    public static final WeaponMaterial REDSTONE = register(new WeaponMaterial(
            "redstone", "material.mushokucraft.redstone", 0xFF3322, 2.0f, 0.25f, 220, 0.0f, "material.mushokucraft.redstone.perk"
    ));
    public static final WeaponMaterial EMERALD = register(new WeaponMaterial(
            "emerald", "material.mushokucraft.emerald", 0x22EE66, 2.5f, 0.0f, 650, 0.0f, "material.mushokucraft.emerald.perk"
    ));
    public static final WeaponMaterial AMETHYST = register(new WeaponMaterial(
            "amethyst", "material.mushokucraft.amethyst", 0xBE76F2, 2.2f, 0.1f, 450, 0.0f, "material.mushokucraft.amethyst.perk"
    ));
    public static final WeaponMaterial OBSIDIAN = register(new WeaponMaterial(
            "obsidian", "material.mushokucraft.obsidian", 0x583A7E, 3.2f, -0.2f, 2500, 0.0f, "material.mushokucraft.obsidian.perk"
    ));
    public static final WeaponMaterial FLINT = register(new WeaponMaterial(
            "flint", "material.mushokucraft.flint", 0x606060, 1.2f, 0.05f, 120, 0.0f, "material.mushokucraft.flint.perk"
    ));
    public static final WeaponMaterial BONE = register(new WeaponMaterial(
            "bone", "material.mushokucraft.bone", 0xF0ECE0, 1.0f, 0.05f, 100, 0.0f, "material.mushokucraft.bone.perk"
    ));

    // Woods
    public static final WeaponMaterial OAK = register(new WeaponMaterial(
            "oak", "material.mushokucraft.oak", 0xB8945F, 0.6f, 0.0f, 60, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial SPRUCE = register(new WeaponMaterial(
            "spruce", "material.mushokucraft.spruce", 0x8A6644, 0.7f, -0.05f, 75, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial BIRCH = register(new WeaponMaterial(
            "birch", "material.mushokucraft.birch", 0xDCD0BA, 0.5f, 0.1f, 55, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial JUNGLE = register(new WeaponMaterial(
            "jungle", "material.mushokucraft.jungle", 0xB87E5A, 0.6f, 0.05f, 65, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial ACACIA = register(new WeaponMaterial(
            "acacia", "material.mushokucraft.acacia", 0xBA6336, 0.8f, -0.05f, 80, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial DARK_OAK = register(new WeaponMaterial(
            "dark_oak", "material.mushokucraft.dark_oak", 0x5C3E24, 0.9f, -0.1f, 90, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial MANGROVE = register(new WeaponMaterial(
            "mangrove", "material.mushokucraft.mangrove", 0x883E3E, 0.7f, 0.0f, 75, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial CHERRY = register(new WeaponMaterial(
            "cherry", "material.mushokucraft.cherry", 0xF0A8A8, 0.5f, 0.1f, 60, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial BAMBOO = register(new WeaponMaterial(
            "bamboo", "material.mushokucraft.bamboo", 0xC8C060, 0.4f, 0.2f, 50, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial CRIMSON = register(new WeaponMaterial(
            "crimson", "material.mushokucraft.crimson", 0x943250, 1.0f, 0.0f, 95, 0.0f, "material.mushokucraft.wood.perk"
    ));
    public static final WeaponMaterial WARPED = register(new WeaponMaterial(
            "warped", "material.mushokucraft.warped", 0x2E8882, 1.0f, 0.0f, 95, 0.0f, "material.mushokucraft.wood.perk"
    ));

    public static WeaponMaterial register(WeaponMaterial material) {
        MATERIALS.put(material.id(), material);
        return material;
    }

    public static WeaponMaterial get(String id) {
        if (id == null) return IRON;
        return MATERIALS.getOrDefault(id.toLowerCase(), IRON);
    }

    public static Collection<WeaponMaterial> getAll() {
        return Collections.unmodifiableCollection(MATERIALS.values());
    }

    public static WeaponMaterial findMaterial(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        var item = stack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();

        // Exact match checks
        if (item == Items.IRON_INGOT || item == Items.IRON_BLOCK || item == Items.RAW_IRON) return IRON;
        if (item == Items.GOLD_INGOT || item == Items.GOLD_BLOCK || item == Items.RAW_GOLD) return GOLD;
        if (item == Items.COPPER_INGOT || item == Items.COPPER_BLOCK || item == Items.RAW_COPPER) return COPPER;
        if (item == Items.DIAMOND || item == Items.DIAMOND_BLOCK) return DIAMOND;
        if (item == Items.NETHERITE_INGOT || item == Items.NETHERITE_BLOCK) return NETHERITE;
        if (item == Items.LAPIS_LAZULI || item == Items.LAPIS_BLOCK) return LAPIS;
        if (item == Items.REDSTONE || item == Items.REDSTONE_BLOCK) return REDSTONE;
        if (item == Items.EMERALD || item == Items.EMERALD_BLOCK) return EMERALD;
        if (item == Items.AMETHYST_SHARD || item == Items.AMETHYST_BLOCK) return AMETHYST;
        if (item == Items.OBSIDIAN || item == Items.CRYING_OBSIDIAN) return OBSIDIAN;
        if (item == Items.FLINT) return FLINT;
        if (item == Items.BONE || item == Items.BONE_BLOCK) return BONE;

        // Wood types by path
        if (path.contains("dark_oak")) return DARK_OAK;
        if (path.contains("oak")) return OAK;
        if (path.contains("spruce")) return SPRUCE;
        if (path.contains("birch")) return BIRCH;
        if (path.contains("jungle")) return JUNGLE;
        if (path.contains("acacia")) return ACACIA;
        if (path.contains("mangrove")) return MANGROVE;
        if (path.contains("cherry")) return CHERRY;
        if (path.contains("bamboo")) return BAMBOO;
        if (path.contains("crimson")) return CRIMSON;
        if (path.contains("warped")) return WARPED;

        // Tags fallback
        if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS)) {
            return OAK;
        }

        return null;
    }
}
