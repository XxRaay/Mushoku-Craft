package com.mushokucraft.init;

import com.mushokucraft.init.ModEntities;
import com.mushokucraft.item.MagicBookItem;
import com.mushokucraft.item.SwordGodScrollItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create("mushokucraft", Registries.ITEM);
    public static final Supplier<Item> WATER_MAGIC_BOOK = ITEMS.register("water_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "water", java.util.List.of("waterball", "water_slice", "icicle_break", "cumulonimbus")));
    public static final Supplier<Item> FIRE_MAGIC_BOOK = ITEMS.register("fire_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "fire", java.util.List.of("fireball")));
    public static final Supplier<Item> EARTH_MAGIC_BOOK = ITEMS.register("earth_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "earth", java.util.List.of("rockbullet")));
    public static final Supplier<Item> WIND_MAGIC_BOOK = ITEMS.register("wind_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "wind", java.util.List.of("airstrike", "air_cushion")));
    public static final Supplier<Item> SABERTOOTH_WOLF_SPAWN_EGG = ITEMS.register("sabertooth_wolf_spawn_egg", () -> new ArchitecturySpawnEggItem(ModEntities.SABERTOOTH_WOLF, 9139029, 4864810, new Item.Properties()));
    public static final Supplier<Item> SWORD_GOD_SCROLL = ITEMS.register("sword_god_scroll", () -> new SwordGodScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Supplier<Item> HUNTING_KNIFE = ITEMS.register("hunting_knife", () -> new com.mushokucraft.item.HuntingKnifeItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> MAGE_MEAT = ITEMS.register("mage_meat", () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build())));
    public static final Supplier<Item> SABERTOOTH_LEATHER = ITEMS.register("sabertooth_leather", () -> new Item(new Item.Properties()));

    public static final Supplier<Item> MODULAR_ANVIL = ITEMS.register("modular_anvil", () -> new net.minecraft.world.item.BlockItem(ModBlocks.MODULAR_ANVIL.get(), new Item.Properties()));

    public static final Supplier<Item> BLANK_CANVAS = ITEMS.register("blank_canvas", () -> new com.mushokucraft.item.BlankCanvasItem(new Item.Properties().stacksTo(64)));
    public static final Supplier<Item> INSCRIBED_MANUSCRIPT = ITEMS.register("inscribed_manuscript", () -> new com.mushokucraft.item.InscribedManuscriptItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ANCIENT_MANUSCRIPT = ITEMS.register("ancient_manuscript", () -> new com.mushokucraft.item.AncientManuscriptItem(new Item.Properties()));
    public static final Supplier<Item> MAGIC_CIRCLE = ITEMS.register("magic_circle", () -> new net.minecraft.world.item.BlockItem(ModBlocks.MAGIC_CIRCLE.get(), new Item.Properties()));

    public static final Supplier<Item> SMALL_MANA_CRYSTAL = ITEMS.register("small_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16), 50.0f));
    public static final Supplier<Item> MEDIUM_MANA_CRYSTAL = ITEMS.register("medium_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 150.0f));
    public static final Supplier<Item> LARGE_MANA_CRYSTAL = ITEMS.register("large_mana_crystal", () -> new com.mushokucraft.item.ManaCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), 400.0f));

    public static void register() {
        ITEMS.register();
    }
}


