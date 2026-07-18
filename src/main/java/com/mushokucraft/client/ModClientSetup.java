package com.mushokucraft.client;

import com.mushokucraft.client.gui.IncantationOverlay;
import com.mushokucraft.client.gui.MasteryOverlay;
import com.mushokucraft.client.gui.QTEOverlay;
import com.mushokucraft.client.hud.ManaHudManager;
import com.mushokucraft.client.render.MagicBookItemRenderer;
import com.mushokucraft.client.render.entity.AirStrikeRenderer;
import com.mushokucraft.client.render.entity.DeadSabertoothWolfRenderer;
import com.mushokucraft.client.render.entity.MagicProjectileRenderer;
import com.mushokucraft.client.render.entity.SabertoothWolfRenderer;
import com.mushokucraft.client.render.model.FireballModel;
import com.mushokucraft.client.render.model.MagicProjectileModel;
import com.mushokucraft.client.render.model.RockBulletModel;
import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.client.render.model.WaterballModel;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.init.ModItems;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.MOD)
public class ModClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"animation"), 42, player -> new ModifierLayer());
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"mana_hud"), ManaHudManager::render);
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"incantation_hud"), IncantationOverlay::render);
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"mastery_hud"), MasteryOverlay::render);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"qte_hud"), QTEOverlay::render);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WATERBALL.get(), ctx -> new MagicProjectileRenderer(ctx, "waterball.png", WaterballModel.LAYER_LOCATION));
        event.registerEntityRenderer(ModEntities.FIREBALL.get(), ctx -> new MagicProjectileRenderer(ctx, "fireball.png", FireballModel.LAYER_LOCATION));
        event.registerEntityRenderer(ModEntities.ROCK_BULLET.get(), ctx -> new MagicProjectileRenderer(ctx, "rockbullet.png", RockBulletModel.LAYER_LOCATION));
        event.registerEntityRenderer(ModEntities.AIR_STRIKE.get(), AirStrikeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWN_SWORD.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SABERTOOTH_WOLF.get(), SabertoothWolfRenderer::new);
        event.registerEntityRenderer(ModEntities.DEAD_SABERTOOTH_WOLF.get(), DeadSabertoothWolfRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WaterballModel.LAYER_LOCATION, WaterballModel::createBodyLayer);
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        event.registerLayerDefinition(RockBulletModel.LAYER_LOCATION, RockBulletModel::createBodyLayer);
        event.registerLayerDefinition(MagicProjectileModel.LAYER_LOCATION, MagicProjectileModel::createBodyLayer);
        event.registerLayerDefinition(SabertoothWolfModel.LAYER_LOCATION, SabertoothWolfModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions(){

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new MagicBookItemRenderer("waterbook.png");
            }
        }, new Item[]{ModItems.WATER_MAGIC_BOOK.get()});
        event.registerItem(new IClientItemExtensions(){

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new MagicBookItemRenderer("pirobook.png");
            }
        }, new Item[]{ModItems.FIRE_MAGIC_BOOK.get()});
        event.registerItem(new IClientItemExtensions(){

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new MagicBookItemRenderer("geobook.png");
            }
        }, new Item[]{ModItems.EARTH_MAGIC_BOOK.get()});
        event.registerItem(new IClientItemExtensions(){

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new MagicBookItemRenderer("airbook.png");
            }
        }, new Item[]{ModItems.WIND_MAGIC_BOOK.get()});
    }
}


