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

        // Register magic circle block entity renderer on Fabric directly
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                com.mushokucraft.init.ModBlockEntities.MAGIC_CIRCLE_BE.get(),
                com.mushokucraft.client.render.block.MagicCircleBlockEntityRenderer::new
        );

        // Register render types for special blocks
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MAGIC_CIRCLE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.AIR_CUSHION.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BARRIER_WALL.get(), RenderType.translucent());
    }
}
