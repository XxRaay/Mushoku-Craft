package com.mushokucraft.init;

import com.mushokucraft.crafting.ModularAnvilMenu;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create("mushokucraft", Registries.MENU);

    public static final RegistrySupplier<MenuType<ModularAnvilMenu>> MODULAR_ANVIL = MENUS.register("modular_anvil", () ->
            MenuRegistry.of(ModularAnvilMenu::new)
    );

    public static void register() {
        MENUS.register();
    }
}
