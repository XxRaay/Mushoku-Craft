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
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class ModClientSetup {

    public static void register() {
        ClientLifecycleEvent.CLIENT_SETUP.register(mc -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath("mushokucraft", "animation"), 42, player -> new ModifierLayer<>());
        });

        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) -> {
            ManaHudManager.render(graphics, tickDelta);
            IncantationOverlay.render(graphics, tickDelta);
            MasteryOverlay.render(graphics, tickDelta);
            QTEOverlay.render(graphics, tickDelta);
        });

        EntityRendererRegistry.register(ModEntities.WATERBALL, ctx -> new MagicProjectileRenderer(ctx, "waterball.png", WaterballModel.LAYER_LOCATION));
        EntityRendererRegistry.register(ModEntities.FIREBALL, ctx -> new MagicProjectileRenderer(ctx, "fireball.png", FireballModel.LAYER_LOCATION));
        EntityRendererRegistry.register(ModEntities.ROCK_BULLET, ctx -> new MagicProjectileRenderer(ctx, "rockbullet.png", RockBulletModel.LAYER_LOCATION));
        EntityRendererRegistry.register(ModEntities.AIR_STRIKE, AirStrikeRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_SWORD, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SABERTOOTH_WOLF, SabertoothWolfRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEAD_SABERTOOTH_WOLF, DeadSabertoothWolfRenderer::new);

        EntityModelLayerRegistry.register(WaterballModel.LAYER_LOCATION, WaterballModel::createBodyLayer);
        EntityModelLayerRegistry.register(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        EntityModelLayerRegistry.register(RockBulletModel.LAYER_LOCATION, RockBulletModel::createBodyLayer);
        EntityModelLayerRegistry.register(MagicProjectileModel.LAYER_LOCATION, MagicProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.register(SabertoothWolfModel.LAYER_LOCATION, SabertoothWolfModel::createBodyLayer);
    }
}

