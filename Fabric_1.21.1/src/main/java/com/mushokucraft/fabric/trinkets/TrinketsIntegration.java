package com.mushokucraft.fabric.trinkets;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.accessory.AccessoryItem;
import com.mushokucraft.init.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TrinketsIntegration {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) return;

        initialized = true;
        TrinketsSetup.register();
    }

    private static class TrinketsSetup {
        private static void register() {
            // Register provider for AccessoryHelper in Common
            AccessoryHelper.registerProvider(entity -> {
                List<ItemStack> list = new ArrayList<>();
                if (entity == null) return list;
                try {
                    TrinketsApi.getTrinketComponent(entity).ifPresent(comp -> {
                        comp.forEach((slotReference, stack) -> {
                            if (!stack.isEmpty() && stack.getItem() instanceof AccessoryItem) {
                                list.add(stack);
                            }
                        });
                    });
                } catch (Throwable ignored) {
                }
                return list;
            });

            // Register Trinket implementation for each AccessoryItem
            for (RegistrySupplier<Item> supplier : ModItems.ITEMS) {
                Item item = supplier.get();
                if (item instanceof AccessoryItem accessoryItem) {
                    TrinketsApi.registerTrinket(accessoryItem, new FabricTrinketItemWrapper(accessoryItem));
                }
            }
        }
    }
}
