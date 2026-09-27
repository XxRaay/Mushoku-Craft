package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.FlamePillarModel;
import com.mushokucraft.magic.entity.FlamePillarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class FlamePillarRenderer extends EntityRenderer<FlamePillarEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/flame_pillar_ground.png");
    private static final ResourceLocation PILLAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/flame_pillar.png");

    private final FlamePillarModel model;

    public FlamePillarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlamePillarModel(context.bakeLayer(FlamePillarModel.LAYER_LOCATION));
    }

    @Override
    public void render(FlamePillarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float charge = entity.getChargeScale();
        float widthScale = 1.0f + (charge - 1.0f) * 0.45f;
        float heightScale = 1.0f + (charge - 1.0f) * 0.6f;
        int fullBright = 0xF000F0;

        // 1. Render glowing ground summoning rune with full UV and dynamic rotation
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 2.5F));

        float r = 2.4F * widthScale;
        VertexConsumer runeConsumer = buffer.getBuffer(RenderType.entityTranslucent(GROUND_TEXTURE));
        int runeAlpha = entity.isCharging() ? Math.min(255, (int)(180 + charge * 35)) : 230;

        Matrix4f mat = poseStack.last().pose();
        // Top face
        runeConsumer.addVertex(mat, -r, 0.0F, -r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, -r, 0.0F, r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, r, 0.0F, r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        runeConsumer.addVertex(mat, r, 0.0F, -r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

        // Bottom face
        runeConsumer.addVertex(mat, r, -0.005F, -r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        runeConsumer.addVertex(mat, r, -0.005F, r).setColor(255, 255, 255, runeAlpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        runeConsumer.addVertex(mat, -r, -0.005F, r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        runeConsumer.addVertex(mat, -r, -0.005F, -r).setColor(255, 255, 255, runeAlpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        poseStack.popPose();

        // 2. If actively erupting, render the roaring 3D vertical flame vortex
        if (entity.isErupting()) {
            VertexConsumer pillarConsumer = buffer.getBuffer(RenderType.entityTranslucent(PILLAR_TEXTURE));

            // Layer A: Outer Swirling Flame Vortex (12-sided prism tube)
            renderFlameCylinder(poseStack, pillarConsumer, 12,
                    2.0F * widthScale, 8.5F * heightScale,
                    3.5F, 0.30F, ageInTicks,
                    255, 160, 30, 215);

            // Layer B: Inner Golden Plasma Vortex (8-sided counter-rotating tube)
            renderFlameCylinder(poseStack, pillarConsumer, 8,
                    1.2F * widthScale, 9.2F * heightScale,
                    -4.8F, 0.42F, ageInTicks,
                    255, 220, 70, 235);

            // Layer C: Central Incandescent Core (6-sided white-hot plasma column)
            renderFlameCylinder(poseStack, pillarConsumer, 6,
                    0.55F * widthScale, 10.0F * heightScale,
                    2.2F, 0.55F, ageInTicks,
                    255, 255, 220, 250);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderFlameCylinder(PoseStack poseStack, VertexConsumer consumer, int sides,
                                     float radius, float height, float rotSpeed, float scrollSpeed,
                                     float ageInTicks, int red, int green, int blue, int alpha) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * rotSpeed));
        Matrix4f mat = poseStack.last().pose();
        int fullBright = 0xF000F0;
        float vScroll = ageInTicks * scrollSpeed;

        for (int i = 0; i < sides; i++) {
            double angle0 = (i * 2.0 * Math.PI) / sides;
            double angle1 = ((i + 1) * 2.0 * Math.PI) / sides;

            float x0 = (float) (Math.cos(angle0) * radius);
            float z0 = (float) (Math.sin(angle0) * radius);
            float x1 = (float) (Math.cos(angle1) * radius);
            float z1 = (float) (Math.sin(angle1) * radius);

            float u0 = (float) i / sides;
            float u1 = (float) (i + 1) / sides;
            float v0 = -vScroll;
            float v1 = -vScroll + 2.5F;

            // Outer face
            consumer.addVertex(mat, x0, 0.0F, z0).setColor(red, green, blue, alpha).setUv(u0, v1)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(x0, 0.0F, z0);
            consumer.addVertex(mat, x1, 0.0F, z1).setColor(red, green, blue, alpha).setUv(u1, v1)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(x1, 0.0F, z1);
            consumer.addVertex(mat, x1, height, z1).setColor(red, green, blue, alpha).setUv(u1, v0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(x1, 0.0F, z1);
            consumer.addVertex(mat, x0, height, z0).setColor(red, green, blue, alpha).setUv(u0, v0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(x0, 0.0F, z0);

            // Inner face (for true interior volume)
            consumer.addVertex(mat, x1, 0.0F, z1).setColor(red, green, blue, alpha).setUv(u1, v1)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(-x1, 0.0F, -z1);
            consumer.addVertex(mat, x0, 0.0F, z0).setColor(red, green, blue, alpha).setUv(u0, v1)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(-x0, 0.0F, -z0);
            consumer.addVertex(mat, x0, height, z0).setColor(red, green, blue, alpha).setUv(u0, v0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(-x0, 0.0F, -z0);
            consumer.addVertex(mat, x1, height, z1).setColor(red, green, blue, alpha).setUv(u1, v0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(-x1, 0.0F, -z1);
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(FlamePillarEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(FlamePillarEntity entity) {
        return PILLAR_TEXTURE;
    }
}
