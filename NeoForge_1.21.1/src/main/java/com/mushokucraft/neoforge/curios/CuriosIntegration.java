package com.mushokucraft.neoforge.curios;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.accessory.AccessoryItem;
import com.mushokucraft.init.ModItems;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.List;

public class CuriosIntegration {

    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        if (!ModList.get().isLoaded("curios")) return;

        initialized = true;
        CuriosSetup.register();
    }

    private static class CuriosSetup {
        private static void register() {
            // Register provider for AccessoryHelper in Common
            AccessoryHelper.registerProvider(entity -> {
                List<ItemStack> list = new ArrayList<>();
                CuriosApi.getCuriosInventory(entity).ifPresent(inv -> {
                    List<SlotResult> results =
                            inv.findCurios(stack -> stack.getItem() instanceof AccessoryItem);
                    for (SlotResult res : results) {
                        list.add(res.stack());
                    }
                });
                return list;
            });

            // Register Curio implementation for each AccessoryItem
            for (RegistrySupplier<Item> supplier : ModItems.ITEMS) {
                Item item = supplier.get();
                if (item instanceof AccessoryItem accessoryItem) {
                    CuriosApi.registerCurio(accessoryItem, new NeoForgeCurioItemWrapper(accessoryItem));
                }
            }
        }
    }
}
