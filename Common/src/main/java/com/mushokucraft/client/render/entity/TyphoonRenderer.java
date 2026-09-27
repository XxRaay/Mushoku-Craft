package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.entity.TyphoonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class TyphoonRenderer extends EntityRenderer<TyphoonEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/typhoon_ground.png");
    private static final ResourceLocation VORTEX_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/typhoon_vortex.png");

    public TyphoonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TyphoonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        int fullBright = 0xF000F0;
        float radius = MushokuConfig.TYPHOON_RADIUS.get().floatValue();
        float charge = entity.getChargeScale();
        float r = radius * Math.min(1.3F, 0.95F + 0.15F * charge);

        // 1. Colossal Rotating Hurricane Eye Mandala on Ground (covers full 48-block domain)
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 1.5F));

        VertexConsumer groundConsumer = buffer.getBuffer(RenderType.entityTranslucent(GROUND_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        int alpha = 210;

        // Top face
        groundConsumer.addVertex(mat, -r, 0.0F, -r).setColor(255, 255, 255, alpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, -r, 0.0F, r).setColor(255, 255, 255, alpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, r, 0.0F, r).setColor(255, 255, 255, alpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, r, 0.0F, -r).setColor(255, 255, 255, alpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

        // Bottom face
        groundConsumer.addVertex(mat, r, -0.005F, -r).setColor(255, 255, 255, alpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, r, -0.005F, r).setColor(255, 255, 255, alpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, -r, -0.005F, r).setColor(255, 255, 255, alpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, -r, -0.005F, -r).setColor(255, 255, 255, alpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        poseStack.popPose();

        // 2. Multi-tiered Atmospheric Cyclone Walls (Domain Scale)
        VertexConsumer vortexConsumer = buffer.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));

        // Layer A: Outer Perimeter Cyclone Wall (16-sided boundary tube)
        renderGaleCylinder(poseStack, vortexConsumer, 16,
                r * 0.96F, 22.0F,
                2.2F, 0.18F, ageInTicks,
                180, 245, 235, 125);

        // Layer B: Mid Dense Hurricane Feeder Band (12-sided counter-rotating tube)
        renderGaleCylinder(poseStack, vortexConsumer, 12,
                r * 0.58F, 24.0F,
                -3.6F, 0.28F, ageInTicks,
                205, 255, 245, 160);

        // Layer C: Eyewall of the Hurricane (8-sided inner eye tube surrounding caster)
        renderGaleCylinder(poseStack, vortexConsumer, 8,
                r * 0.18F, 26.0F,
                5.0F, 0.42F, ageInTicks,
                235, 255, 255, 200);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderGaleCylinder(PoseStack poseStack, VertexConsumer consumer, int sides,
                                    float radius, float height, float rotSpeed, float scrollSpeed,
                                    float ageInTicks, int red, int green, int blue, int alpha) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * rotSpeed));

        float vOffset = -(ageInTicks * scrollSpeed) % 1.0F;
        int fullBright = 0xF000F0;
        Matrix4f mat = poseStack.last().pose();

        for (int i = 0; i < sides; i++) {
            float a1 = (float) (i * (2.0 * Math.PI / sides));
            float a2 = (float) ((i + 1) * (2.0 * Math.PI / sides));

            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;

            float u1 = (float) i / sides;
            float u2 = (float) (i + 1) / sides;
            float vTop = vOffset;
            float vBottom = vOffset + (height / 8.0F);

            // Outside Face
            consumer.addVertex(mat, x1, 0.0F, z1).setColor(red, green, blue, alpha).setUv(u1, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2, 0.0F, z2).setColor(red, green, blue, alpha).setUv(u2, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2, height, z2).setColor(red, green, blue, alpha).setUv(u2, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1, height, z1).setColor(red, green, blue, alpha).setUv(u1, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

            // Inside Face (visible when inside domain)
            consumer.addVertex(mat, x1, height, z1).setColor(red, green, blue, alpha).setUv(u1, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2, height, z2).setColor(red, green, blue, alpha).setUv(u2, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2, 0.0F, z2).setColor(red, green, blue, alpha).setUv(u2, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1, 0.0F, z1).setColor(red, green, blue, alpha).setUv(u1, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(TyphoonEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true; // Vast domain
    }

    @Override
    public ResourceLocation getTextureLocation(TyphoonEntity entity) {
        return VORTEX_TEXTURE;
    }
}
