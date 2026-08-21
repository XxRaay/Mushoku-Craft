package com.mushokucraft.neoforge;

import com.mushokucraft.client.render.MagicBookItemRenderer;
import com.mushokucraft.init.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = "mushokucraft", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.mushokucraft.init.ModMenuTypes.MODULAR_ANVIL.get(), com.mushokucraft.client.gui.ModularAnvilScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private MagicBookItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new MagicBookItemRenderer("water_magic_book", "water_magic_book", "water_magic_book");
                }
                return this.renderer;
            }
        }, ModItems.WATER_MAGIC_BOOK.get());

        event.registerItem(new IClientItemExtensions() {
            private MagicBookItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new MagicBookItemRenderer("fire_magic_book", "fire_magic_book", "fire_magic_book");
                }
                return this.renderer;
            }
        }, ModItems.FIRE_MAGIC_BOOK.get());

        event.registerItem(new IClientItemExtensions() {
            private MagicBookItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new MagicBookItemRenderer("earth_magic_book", "earth_magic_book", "earth_magic_book");
                }
                return this.renderer;
            }
        }, ModItems.EARTH_MAGIC_BOOK.get());

        event.registerItem(new IClientItemExtensions() {
            private MagicBookItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new MagicBookItemRenderer("wind_magic_book", "wind_magic_book", "wind_magic_book");
                }
                return this.renderer;
            }
        }, ModItems.WIND_MAGIC_BOOK.get());
    }
}
