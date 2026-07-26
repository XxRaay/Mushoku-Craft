package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.MagicProjectileModel;
import com.mushokucraft.client.render.model.WaterSliceModel;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.entity.WaterSliceEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class WaterSliceRenderer extends EntityRenderer<WaterSliceEntity> {
    private final ResourceLocation texture;
    private final MagicProjectileModel<WaterSliceEntity> model;

    public WaterSliceRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.texture = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/waterslice.png");
        this.model = new MagicProjectileModel<>(pContext.bakeLayer(WaterSliceModel.LAYER_LOCATION));
    }

    @Override
    public void render(WaterSliceEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        
        float entityYRot = net.minecraft.util.Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot());
        float entityXRot = net.minecraft.util.Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());

        if (pEntity.isCharging() && pEntity.getOwner() == net.minecraft.client.Minecraft.getInstance().player && net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double entityX = net.minecraft.util.Mth.lerp((double)pPartialTicks, pEntity.xo, pEntity.getX());
            double entityY = net.minecraft.util.Mth.lerp((double)pPartialTicks, pEntity.yo, pEntity.getY());
            double entityZ = net.minecraft.util.Mth.lerp((double)pPartialTicks, pEntity.zo, pEntity.getZ());

            net.minecraft.client.Camera camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
            double camX = camera.getPosition().x;
            double camY = camera.getPosition().y;
            double camZ = camera.getPosition().z;

            float xRot = camera.getXRot();
            float yRot = camera.getYRot();

            float f = xRot * ((float)Math.PI / 180F);
            float f1 = -yRot * ((float)Math.PI / 180F);
            float f2 = net.minecraft.util.Mth.cos(f1);
            float f3 = net.minecraft.util.Mth.sin(f1);
            float f4 = net.minecraft.util.Mth.cos(f);
            float f5 = net.minecraft.util.Mth.sin(f);
            
            double exactX = camX + f3 * f4 * 1.2;
            double exactY = camY - f5 * 1.2 - 0.4;
            double exactZ = camZ + f2 * f4 * 1.2;
            
            pPoseStack.translate(exactX - entityX, exactY - entityY, exactZ - entityZ);
            entityYRot = yRot;
            entityXRot = xRot;
        }

        pPoseStack.mulPose(Axis.YP.rotationDegrees(entityYRot - 90.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(entityXRot));
        
        float spin = pEntity.getSpin(pPartialTicks);
        if (spin != 0) {
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        }
        
        float charge = pEntity.getChargeScale();
        float widthScale = 1.0f + (charge - 1.0f) * MushokuConfig.WATER_SLICE_CHARGE_WIDTH_MULT.get().floatValue();
        // Scale uniformly to avoid shearing the rotated model
        pPoseStack.scale(widthScale, widthScale, widthScale);

        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterSliceEntity pEntity) {
        return texture;
    }
}
