package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.ExodusFlameModel;
import com.mushokucraft.magic.entity.ExodusFlameEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ExodusFlameRenderer extends EntityRenderer<ExodusFlameEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/exodus_flame.png");

    private final ExodusFlameModel model;

    public ExodusFlameRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExodusFlameModel(context.bakeLayer(ExodusFlameModel.LAYER_LOCATION));
    }

    @Override
    public void render(ExodusFlameEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float entityYRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float entityXRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // First person positioning in the caster's hand during charge
        boolean isFirstPersonCharge = entity.isCharging() && entity.getOwner() == Minecraft.getInstance().player
                && Minecraft.getInstance().options.getCameraType().isFirstPerson();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (isFirstPersonCharge) {
            double entityX = Mth.lerp(partialTicks, entity.xo, entity.getX());
            double entityY = Mth.lerp(partialTicks, entity.yo, entity.getY());
            double entityZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());

            double camX = camera.getPosition().x;
            double camY = camera.getPosition().y;
            double camZ = camera.getPosition().z;

            float xRot = camera.getXRot();
            float yRot = camera.getYRot();

            float f = xRot * ((float)Math.PI / 180F);
            float f1 = -yRot * ((float)Math.PI / 180F);
            float f2 = Mth.cos(f1);
            float f3 = Mth.sin(f1);
            float f4 = Mth.cos(f);
            float f5 = Mth.sin(f);

            // Forward vector
            double fwdX = f3 * f4;
            double fwdY = -f5;
            double fwdZ = f2 * f4;

            // Right vector (horizontal perpendicular to forward)
            double rightX = f2;
            double rightZ = -f3;

            // Positioned gracefully in caster's right hand (unobstructing crosshairs)
            double exactX = camX + fwdX * 0.85 + rightX * 0.36;
            double exactY = camY + fwdY * 0.85 - 0.28;
            double exactZ = camZ + fwdZ * 0.85 + rightZ * 0.36;

            poseStack.translate(exactX - entityX, exactY - entityY, exactZ - entityZ);
            entityYRot = yRot;
            entityXRot = xRot;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(entityYRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entityXRot));

        float charge = entity.getChargeScale();
        float visualScale = isFirstPersonCharge
                ? (0.45f + Math.min(1.0f, charge) * 0.35f)
                : (1.0f + (charge - 1.0f) * 0.65f);
        poseStack.scale(visualScale, visualScale, visualScale);

        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0, 0, ageInTicks, 0, 0);

        int fullBright = 0xF000F0;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        // 1. Render nested 3D Plasma Star (Core & Mantle)
        this.model.renderToBuffer(poseStack, consumer, fullBright, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // 2. Render Orbital Plasma Rings (circling outside mantle at non-intersecting radii)
        renderOrbitalRing(poseStack, consumer, 0.78F, Axis.XP.rotationDegrees(35.0F), Axis.YP.rotationDegrees(ageInTicks * 2.0F), fullBright);
        renderOrbitalRing(poseStack, consumer, 0.95F, Axis.ZP.rotationDegrees(-42.0F), Axis.YP.rotationDegrees(-ageInTicks * 2.4F), fullBright);

        // 3. Render Camera-Facing Coronal Halo (breathing solar flare bloom, zero z-fighting)
        poseStack.pushPose();
        // Counter-rotate to face camera
        poseStack.mulPose(Axis.ZP.rotationDegrees(-entityXRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(-(entityYRot - 90.0F)));
        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(ageInTicks * 1.2F));

        float pulse = 1.2F + (float) Math.sin(ageInTicks * 0.15F) * 0.1F;
        float hr = pulse;
        org.joml.Matrix4f hMat = poseStack.last().pose();
        consumer.addVertex(hMat, -hr, -hr, 0.0F).setColor(255, 255, 255, 230).setUv(0.0F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(hMat, -hr, hr, 0.0F).setColor(255, 255, 255, 230).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(hMat, hr, hr, 0.0F).setColor(255, 255, 255, 230).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 0.0F, 1.0F);
        consumer.addVertex(hMat, hr, -hr, 0.0F).setColor(255, 255, 255, 230).setUv(1.0F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 0.0F, 1.0F);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderOrbitalRing(PoseStack poseStack, VertexConsumer consumer, float radius,
                                   org.joml.Quaternionf tilt, org.joml.Quaternionf spin, int light) {
        poseStack.pushPose();
        poseStack.mulPose(tilt);
        poseStack.mulPose(spin);

        org.joml.Matrix4f mat = poseStack.last().pose();
        int sides = 8;
        float thickness = 0.12F;
        float rInner = radius - thickness;
        float rOuter = radius + thickness;

        for (int i = 0; i < sides; i++) {
            double a0 = (i * 2.0 * Math.PI) / sides;
            double a1 = ((i + 1) * 2.0 * Math.PI) / sides;

            float x0_in = (float) (Math.cos(a0) * rInner);
            float z0_in = (float) (Math.sin(a0) * rInner);
            float x1_in = (float) (Math.cos(a1) * rInner);
            float z1_in = (float) (Math.sin(a1) * rInner);

            float x0_out = (float) (Math.cos(a0) * rOuter);
            float z0_out = (float) (Math.sin(a0) * rOuter);
            float x1_out = (float) (Math.cos(a1) * rOuter);
            float z1_out = (float) (Math.sin(a1) * rOuter);

            float u0 = 0.5F + (float) i / (sides * 2.0F);
            float u1 = 0.5F + (float) (i + 1) / (sides * 2.0F);

            // Top face
            consumer.addVertex(mat, x0_in, 0.0F, z0_in).setColor(255, 255, 255, 220).setUv(u0, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x0_out, 0.0F, z0_out).setColor(255, 255, 255, 220).setUv(u0, 0.5F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1_out, 0.0F, z1_out).setColor(255, 255, 255, 220).setUv(u1, 0.5F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(mat, x1_in, 0.0F, z1_in).setColor(255, 255, 255, 220).setUv(u1, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);

            // Bottom face
            consumer.addVertex(mat, x1_in, -0.002F, z1_in).setColor(255, 255, 255, 220).setUv(u1, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, -1.0F, 0.0F);
            consumer.addVertex(mat, x1_out, -0.002F, z1_out).setColor(255, 255, 255, 220).setUv(u1, 0.5F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, -1.0F, 0.0F);
            consumer.addVertex(mat, x0_out, -0.002F, z0_out).setColor(255, 255, 255, 220).setUv(u0, 0.5F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, -1.0F, 0.0F);
            consumer.addVertex(mat, x0_in, -0.002F, z0_in).setColor(255, 255, 255, 220).setUv(u0, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, -1.0F, 0.0F);
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ExodusFlameEntity entity) {
        return TEXTURE;
    }
}
