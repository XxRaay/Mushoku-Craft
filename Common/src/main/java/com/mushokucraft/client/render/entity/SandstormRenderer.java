package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.magic.entity.SandstormEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SandstormRenderer extends EntityRenderer<SandstormEntity> {
    private static final ResourceLocation GROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/sandstorm_ground.png");
    private static final ResourceLocation VORTEX_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/sandstorm_vortex.png");

    public SandstormRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SandstormEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float ageInTicks = entity.tickCount + partialTicks;
        int fullBright = 0xF000F0;
        float baseRadius = com.mushokucraft.config.MushokuConfig.SANDSTORM_RADIUS.get().floatValue();
        float charge = entity.getChargeScale();
        float r = baseRadius * Math.min(1.4f, 0.9f + 0.2f * charge);

        // 1. Grand Rotating Cyclone Mandala on Ground (exactly covers damage radius)
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 1.8F));

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

        // 2. Swirling Cylindrical Sand Vortex Tubes (scaled to full storm diameter)
        VertexConsumer vortexConsumer = buffer.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));

        // Layer A: Outer Sweeping Cyclone Wall (12-sided prism tube covering outer perimeter)
        renderSandCylinder(poseStack, vortexConsumer, 12,
                r * 0.95F, 16.0F,
                2.8F, 0.20F, ageInTicks,
                240, 200, 110, 135);

        // Layer B: Mid Dense Cyclone Wall (8-sided counter-rotating tube)
        renderSandCylinder(poseStack, vortexConsumer, 8,
                r * 0.58F, 18.0F,
                -4.2F, 0.32F, ageInTicks,
                255, 220, 130, 170);

        // Layer C: Central Raging Sand Vortex (6-sided vortex core)
        renderSandCylinder(poseStack, vortexConsumer, 6,
                r * 0.22F, 20.0F,
                5.5F, 0.45F, ageInTicks,
                255, 235, 160, 210);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderSandCylinder(PoseStack poseStack, VertexConsumer consumer, int sides,
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

            // Inside Face (visible when player is within vortex)
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
    public boolean shouldRender(SandstormEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(SandstormEntity entity) {
        return VORTEX_TEXTURE;
    }
}
