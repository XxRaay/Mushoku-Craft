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
import com.mushokucraft.client.render.model.IcicleBreakTargetModel;
import com.mushokucraft.client.render.model.IcicleModel;
import com.mushokucraft.client.render.model.MagicProjectileModel;
import com.mushokucraft.client.render.model.RockBulletModel;
import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.client.render.model.WaterballModel;
import com.mushokucraft.init.ModEntities;
import com.mushokucraft.init.ModItems;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class ModClientSetup {

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath("mushokucraft", "animation"),
                42,
                player -> new ModifierLayer<>()
        );
        com.mushokucraft.init.ModBlockEntities.MAGIC_CIRCLE_BE.listen(type -> {
            dev.architectury.registry.client.rendering.BlockEntityRendererRegistry.register(
                    type,
                    com.mushokucraft.client.render.block.MagicCircleBlockEntityRenderer::new
            );
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
        EntityRendererRegistry.register(ModEntities.WATER_SLICE, com.mushokucraft.client.render.entity.WaterSliceRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICICLE_BREAK_TARGET, com.mushokucraft.client.render.entity.IcicleBreakTargetRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICICLE_ENTITY, com.mushokucraft.client.render.entity.IcicleRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWN_SWORD, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SABERTOOTH_WOLF, SabertoothWolfRenderer::new);
        EntityRendererRegistry.register(ModEntities.DEAD_SABERTOOTH_WOLF, DeadSabertoothWolfRenderer::new);
        EntityRendererRegistry.register(ModEntities.CUMULONIMBUS_STORM, net.minecraft.client.renderer.entity.NoopRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLAME_PILLAR, com.mushokucraft.client.render.entity.FlamePillarRenderer::new);
        EntityRendererRegistry.register(ModEntities.EXODUS_FLAME, com.mushokucraft.client.render.entity.ExodusFlameRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLASHOVER, com.mushokucraft.client.render.entity.FlashoverRenderer::new);
        EntityRendererRegistry.register(ModEntities.EARTH_LANCE, com.mushokucraft.client.render.entity.EarthLanceRenderer::new);
        EntityRendererRegistry.register(ModEntities.STONE_PILLAR, com.mushokucraft.client.render.entity.StonePillarRenderer::new);
        EntityRendererRegistry.register(ModEntities.SANDSTORM, com.mushokucraft.client.render.entity.SandstormRenderer::new);
        EntityRendererRegistry.register(ModEntities.TORNADO, com.mushokucraft.client.render.entity.TornadoRenderer::new);
        EntityRendererRegistry.register(ModEntities.TYPHOON, com.mushokucraft.client.render.entity.TyphoonRenderer::new);

        EntityModelLayerRegistry.register(WaterballModel.LAYER_LOCATION, WaterballModel::createBodyLayer);
        EntityModelLayerRegistry.register(com.mushokucraft.client.render.model.WaterSliceModel.LAYER_LOCATION, com.mushokucraft.client.render.model.WaterSliceModel::createBodyLayer);
        EntityModelLayerRegistry.register(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        EntityModelLayerRegistry.register(RockBulletModel.LAYER_LOCATION, RockBulletModel::createBodyLayer);
        EntityModelLayerRegistry.register(MagicProjectileModel.LAYER_LOCATION, MagicProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.register(SabertoothWolfModel.LAYER_LOCATION, SabertoothWolfModel::createBodyLayer);
        EntityModelLayerRegistry.register(IcicleModel.LAYER_LOCATION, IcicleModel::createBodyLayer);
        EntityModelLayerRegistry.register(IcicleBreakTargetModel.LAYER_LOCATION, IcicleBreakTargetModel::createBodyLayer);
        EntityModelLayerRegistry.register(com.mushokucraft.client.render.model.FlamePillarModel.LAYER_LOCATION, com.mushokucraft.client.render.model.FlamePillarModel::createBodyLayer);
        EntityModelLayerRegistry.register(com.mushokucraft.client.render.model.ExodusFlameModel.LAYER_LOCATION, com.mushokucraft.client.render.model.ExodusFlameModel::createBodyLayer);
        EntityModelLayerRegistry.register(com.mushokucraft.client.render.model.EarthLanceModel.LAYER_LOCATION, com.mushokucraft.client.render.model.EarthLanceModel::createBodyLayer);
        EntityModelLayerRegistry.register(com.mushokucraft.client.render.model.StonePillarModel.LAYER_LOCATION, com.mushokucraft.client.render.model.StonePillarModel::createBodyLayer);

        dev.architectury.event.events.client.ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> {
            registerModularWeaponProperties();
        });
    }

    private static void registerModularWeaponProperties() {
        ResourceLocation hasCoreId = ResourceLocation.fromNamespaceAndPath("mushokucraft", "has_core");
        net.minecraft.client.renderer.item.ClampedItemPropertyFunction hasCoreFunc =
                (stack, level, entity, seed) -> com.mushokucraft.weapon.modular.ModularWeaponItem.getCore(stack) != null ? 1.0F : 0.0F;

        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_SWORD.get(), hasCoreId, hasCoreFunc);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_GREATSWORD.get(), hasCoreId, hasCoreFunc);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_SCYTHE.get(), hasCoreId, hasCoreFunc);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_DAGGER.get(), hasCoreId, hasCoreFunc);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_KATANA.get(), hasCoreId, hasCoreFunc);
        dev.architectury.registry.item.ItemPropertiesRegistry.register(ModItems.MODULAR_RAPIER.get(), hasCoreId, hasCoreFunc);
    }
}
