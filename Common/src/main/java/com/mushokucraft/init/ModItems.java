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
    public static final Supplier<Item> WIND_MAGIC_BOOK = ITEMS.register("wind_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "wind", java.util.List.of("airstrike")));
    public static final Supplier<Item> SABERTOOTH_WOLF_SPAWN_EGG = ITEMS.register("sabertooth_wolf_spawn_egg", () -> new ArchitecturySpawnEggItem(ModEntities.SABERTOOTH_WOLF, 9139029, 4864810, new Item.Properties()));
    public static final Supplier<Item> SWORD_GOD_SCROLL = ITEMS.register("sword_god_scroll", () -> new SwordGodScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final Supplier<Item> HUNTING_KNIFE = ITEMS.register("hunting_knife", () -> new com.mushokucraft.item.HuntingKnifeItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> MAGE_MEAT = ITEMS.register("mage_meat", () -> new Item(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build())));
    public static final Supplier<Item> SABERTOOTH_LEATHER = ITEMS.register("sabertooth_leather", () -> new Item(new Item.Properties()));

    public static void register() {
        ITEMS.register();
    }
}


