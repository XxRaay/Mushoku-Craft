package com.mushokucraft.fabric;

import com.mushokucraft.init.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class FabricLootModifiers {
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (BuiltInLootTables.SIMPLE_DUNGEON.equals(key) ||
                BuiltInLootTables.ABANDONED_MINESHAFT.equals(key) ||
                BuiltInLootTables.STRONGHOLD_LIBRARY.equals(key) ||
                BuiltInLootTables.DESERT_PYRAMID.equals(key) ||
                BuiltInLootTables.JUNGLE_TEMPLE.equals(key) ||
                BuiltInLootTables.WOODLAND_MANSION.equals(key) ||
                BuiltInLootTables.PILLAGER_OUTPOST.equals(key)) {
                
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.WATER_MAGIC_BOOK.get()).setWeight(15)) // roughly 0.15 chance
                        .add(LootItem.lootTableItem(ModItems.FIRE_MAGIC_BOOK.get()).setWeight(15))
                        .add(LootItem.lootTableItem(ModItems.EARTH_MAGIC_BOOK.get()).setWeight(15));
                tableBuilder.withPool(poolBuilder);
            }

            if (BuiltInLootTables.END_CITY_TREASURE.equals(key)) {
                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.SWORD_GOD_SCROLL.get()).setWeight(20)); // roughly 0.20 chance
                
                tableBuilder.withPool(poolBuilder);
            }
        });
    }
}
