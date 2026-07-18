/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.mushokucraft.init;

import com.mushokucraft.init.ModItems;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"mushokucraft");
    public static final Supplier<CreativeModeTab> MUSHOKU_TAB = TABS.register("mushoku_tab", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.mushokucraft")).icon(() -> Items.DIAMOND_SWORD.getDefaultInstance()).displayItems((params, output) -> {
        output.accept((ItemLike)ModItems.WATER_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.FIRE_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.EARTH_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.WIND_MAGIC_BOOK.get());
        output.accept((ItemLike)ModItems.SWORD_GOD_SCROLL.get());
        output.accept((ItemLike)ModItems.SABERTOOTH_WOLF_SPAWN_EGG.get());
    }).build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}

