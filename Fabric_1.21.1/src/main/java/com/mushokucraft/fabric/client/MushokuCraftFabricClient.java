package com.mushokucraft.fabric.client;

import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.client.input.MenuInputHandler;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.client.input.QteInputHandler;
import com.mushokucraft.client.ModClientSetup;
import com.mushokucraft.event.ModClientEvents;
import com.mushokucraft.init.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class MushokuCraftFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModClientEvents.register();
        ModClientSetup.register();
        net.minecraft.client.gui.screens.MenuScreens.register(com.mushokucraft.init.ModMenuTypes.MODULAR_ANVIL.get(), com.mushokucraft.client.gui.ModularAnvilScreen::new);
        
        CombatInputHandler.register();
        MenuInputHandler.register();
        QteInputHandler.register();
        ModKeybindings.register();

        // Register translucent render type for air cushion blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AIR_CUSHION.get(), RenderType.translucent());
    }
}
