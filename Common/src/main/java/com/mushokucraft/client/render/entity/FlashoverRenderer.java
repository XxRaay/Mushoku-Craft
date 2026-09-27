package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.magic.entity.FlashoverEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class FlashoverRenderer extends EntityRenderer<FlashoverEntity> {
    private static final ResourceLocation AURA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/flashover_aura.png");

    public FlashoverRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlashoverEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Position 2cm above the player's feet / ground to eliminate Z-fighting
        poseStack.translate(0.0F, 0.02F, 0.0F);

        float age = entity.tickCount + partialTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.5F));

        // Pulsing thermal radius
        float pulse = 3.5F + (float) Math.sin(age * 0.15F) * 0.3F;
        float r = pulse;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        int fullBright = 0xF000F0;
        int alpha = 190;
        int red = 255;
        int green = 255;
        int blue = 255;

        // Draw flat horizontal quad - Top Face (visible from above)
        consumer.addVertex(mat, -r, 0.0F, -r).setColor(red, green, blue, alpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(mat, -r, 0.0F, r).setColor(red, green, blue, alpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(mat, r, 0.0F, r).setColor(red, green, blue, alpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(mat, r, 0.0F, -r).setColor(red, green, blue, alpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, 1.0F, 0.0F);

        // Bottom Face (visible if player jumps or looks from below)
        consumer.addVertex(mat, r, -0.005F, -r).setColor(red, green, blue, alpha).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        consumer.addVertex(mat, r, -0.005F, r).setColor(red, green, blue, alpha).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        consumer.addVertex(mat, -r, -0.005F, r).setColor(red, green, blue, alpha).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);
        consumer.addVertex(mat, -r, -0.005F, -r).setColor(red, green, blue, alpha).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(fullBright).setNormal(0.0F, -1.0F, 0.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public boolean shouldRender(FlashoverEntity entity, net.minecraft.client.renderer.culling.Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(FlashoverEntity entity) {
        return AURA_TEXTURE;
    }
}
