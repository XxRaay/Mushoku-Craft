package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.EarthLanceModel;
import com.mushokucraft.magic.entity.EarthLanceEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class EarthLanceRenderer extends EntityRenderer<EarthLanceEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/earth_lance_ground.png");
    private static final ResourceLocation LANCE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/earth_lance.png");

    private final EarthLanceModel model;

    public EarthLanceRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EarthLanceModel(context.bakeLayer(EarthLanceModel.LAYER_LOCATION));
    }

    @Override
    public void render(EarthLanceEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float charge = entity.getChargeScale();
        float scale = 1.0f + (charge - 1.0f) * 0.4f;

        // 1. Render ground fissure rune
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 1.5F));

        float r = 1.6F * scale;
        VertexConsumer runeConsumer = buffer.getBuffer(RenderType.entityTranslucent(GROUND_TEXTURE));
        int runeAlpha = entity.isCharging() ? Math.min(255, (int)(180 + charge * 30)) : 240;
        int fullBright = 0xF000F0;

        Matrix4f mat = poseStack.last().pose();
        runeConsumer.addVertex(mat, -r, 0.0F, -r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, -r, 0.0F, r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, r, 0.0F, r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, r, 0.0F, -r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        poseStack.popPose();

        // 2. Render 3D stone spear model
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        // Flip model to standard Minecraft entity orientation (Y down)
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -1.5F, 0.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        VertexConsumer lanceConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LANCE_TEXTURE));
        this.model.renderToBuffer(poseStack, lanceConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EarthLanceEntity entity) {
        return LANCE_TEXTURE;
    }
}
