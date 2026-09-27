package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.StonePillarModel;
import com.mushokucraft.magic.entity.StonePillarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class StonePillarRenderer extends EntityRenderer<StonePillarEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/stone_pillar_ground.png");
    private static final ResourceLocation PILLAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/stone_pillar.png");

    private final StonePillarModel model;

    public StonePillarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new StonePillarModel(context.bakeLayer(StonePillarModel.LAYER_LOCATION));
    }

    @Override
    public void render(StonePillarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float charge = entity.getChargeScale();
        float scale = 1.0f + (charge - 1.0f) * 0.45f;

        // 1. Render ground fault rupture rune
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 0.8F));

        float r = 3.6F * scale;
        VertexConsumer runeConsumer = buffer.getBuffer(RenderType.entityTranslucent(GROUND_TEXTURE));
        int runeAlpha = entity.isCharging() ? Math.min(255, (int)(170 + charge * 35)) : 240;
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

        // 2. Render 3D monolithic pillar model
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -1.5F, 0.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        VertexConsumer pillarConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(PILLAR_TEXTURE));
        this.model.renderToBuffer(poseStack, pillarConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public boolean shouldRender(StonePillarEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true; // Large height
    }

    @Override
    public ResourceLocation getTextureLocation(StonePillarEntity entity) {
        return PILLAR_TEXTURE;
    }
}
