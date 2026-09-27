package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.entity.TornadoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class TornadoRenderer extends EntityRenderer<TornadoEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/tornado_ground.png");
    private static final ResourceLocation VORTEX_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/tornado_vortex.png");

    public TornadoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TornadoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        float charge = entity.getChargeScale();
        float baseRadius = MushokuConfig.TORNADO_RADIUS.get().floatValue();
        float baseHeight = MushokuConfig.TORNADO_HEIGHT.get().floatValue();

        float rTop = baseRadius * Math.min(1.6F, 0.85F + 0.3F * charge);
        float rBottom = 1.3F * Math.min(1.4F, 0.9F + 0.2F * charge);
        float height = baseHeight * Math.min(1.5F, 0.9F + 0.25F * charge);
        int fullBright = 0xF000F0;

        // 1. Rotating Tactical Ground Cyclone Mandala
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 2.2F));

        VertexConsumer groundConsumer = buffer.getBuffer(RenderType.entityTranslucent(GROUND_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        int groundAlpha = 220;

        float groundR = rTop * 1.1F;
        // Top Face
        groundConsumer.addVertex(mat, -groundR, 0.0F, -groundR).setColor(255, 255, 255, groundAlpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, -groundR, 0.0F, groundR).setColor(255, 255, 255, groundAlpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, groundR, 0.0F, groundR).setColor(255, 255, 255, groundAlpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        groundConsumer.addVertex(mat, groundR, 0.0F, -groundR).setColor(255, 255, 255, groundAlpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

        // Bottom Face
        groundConsumer.addVertex(mat, groundR, -0.005F, -groundR).setColor(255, 255, 255, groundAlpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, groundR, -0.005F, groundR).setColor(255, 255, 255, groundAlpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, -groundR, -0.005F, groundR).setColor(255, 255, 255, groundAlpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        groundConsumer.addVertex(mat, -groundR, -0.005F, -groundR).setColor(255, 255, 255, groundAlpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        poseStack.popPose();

        // 2. Swirling Conical Tornado Funnel Layers
        VertexConsumer vortexConsumer = buffer.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));

        // Layer A: Outer Sweeping Funnel (12-sided tapered cone)
        renderTornadoFunnel(poseStack, vortexConsumer, 12,
                rBottom * 1.0F, rTop * 1.0F, height,
                3.8F, 0.28F, ageInTicks,
                200, 255, 235, 140);

        // Layer B: Mid Dense Counter-Rotating Funnel (8-sided tapered cone)
        renderTornadoFunnel(poseStack, vortexConsumer, 8,
                rBottom * 0.65F, rTop * 0.68F, height * 1.05F,
                -5.2F, 0.38F, ageInTicks,
                220, 255, 245, 175);

        // Layer C: Core Rapid Vortex Funnel (6-sided central eye)
        renderTornadoFunnel(poseStack, vortexConsumer, 6,
                rBottom * 0.32F, rTop * 0.35F, height * 1.1F,
                7.0F, 0.52F, ageInTicks,
                245, 255, 255, 215);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderTornadoFunnel(PoseStack poseStack, VertexConsumer consumer, int sides,
                                     float rBottom, float rTop, float height,
                                     float rotSpeed, float scrollSpeed, float ageInTicks,
                                     int red, int green, int blue, int alpha) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * rotSpeed));

        float vOffset = -(ageInTicks * scrollSpeed) % 1.0F;
        int fullBright = 0xF000F0;
        Matrix4f mat = poseStack.last().pose();

        for (int i = 0; i < sides; i++) {
            float a1 = (float) (i * (2.0 * Math.PI / sides));
            float a2 = (float) ((i + 1) * (2.0 * Math.PI / sides));

            float x1Bottom = (float) Math.cos(a1) * rBottom;
            float z1Bottom = (float) Math.sin(a1) * rBottom;
            float x2Bottom = (float) Math.cos(a2) * rBottom;
            float z2Bottom = (float) Math.sin(a2) * rBottom;

            float x1Top = (float) Math.cos(a1) * rTop;
            float z1Top = (float) Math.sin(a1) * rTop;
            float x2Top = (float) Math.cos(a2) * rTop;
            float z2Top = (float) Math.sin(a2) * rTop;

            float u1 = (float) i / sides;
            float u2 = (float) (i + 1) / sides;
            float vTop = vOffset;
            float vBottom = vOffset + (height / 6.0F);

            // Outside Face
            consumer.addVertex(mat, x1Bottom, 0.0F, z1Bottom).setColor(red, green, blue, alpha).setUv(u1, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2Bottom, 0.0F, z2Bottom).setColor(red, green, blue, alpha).setUv(u2, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2Top, height, z2Top).setColor(red, green, blue, alpha).setUv(u2, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1Top, height, z1Top).setColor(red, green, blue, alpha).setUv(u1, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

            // Inside Face (visible when inside the funnel)
            consumer.addVertex(mat, x1Top, height, z1Top).setColor(red, green, blue, alpha).setUv(u1, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2Top, height, z2Top).setColor(red, green, blue, alpha).setUv(u2, vTop)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x2Bottom, 0.0F, z2Bottom).setColor(red, green, blue, alpha).setUv(u2, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1Bottom, 0.0F, z1Bottom).setColor(red, green, blue, alpha).setUv(u1, vBottom)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(TornadoEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true; // Large height
    }

    @Override
    public ResourceLocation getTextureLocation(TornadoEntity entity) {
        return VORTEX_TEXTURE;
    }
}
