/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.common.DeferredSpawnEggItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.mushokucraft.init;

import com.mushokucraft.init.ModEntities;
import com.mushokucraft.item.MagicBookItem;
import com.mushokucraft.item.SwordGodScrollItem;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((ResourceKey)Registries.ITEM, (String)"mushokucraft");
    public static final Supplier<Item> WATER_MAGIC_BOOK = ITEMS.register("water_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "water", "waterball"));
    public static final Supplier<Item> FIRE_MAGIC_BOOK = ITEMS.register("fire_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "fire", "fireball"));
    public static final Supplier<Item> EARTH_MAGIC_BOOK = ITEMS.register("earth_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "earth", "rockbullet"));
    public static final Supplier<Item> WIND_MAGIC_BOOK = ITEMS.register("wind_magic_book", () -> new MagicBookItem(new Item.Properties().stacksTo(1), "wind", "airstrike"));
    public static final Supplier<Item> SABERTOOTH_WOLF_SPAWN_EGG = ITEMS.register("sabertooth_wolf_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.SABERTOOTH_WOLF, 9139029, 4864810, new Item.Properties()));
    public static final Supplier<Item> SWORD_GOD_SCROLL = ITEMS.register("sword_god_scroll", () -> new SwordGodScrollItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

