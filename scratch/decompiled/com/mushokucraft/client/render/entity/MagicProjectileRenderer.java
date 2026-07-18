/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 */
package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.MagicProjectileModel;
import com.mushokucraft.magic.entity.AbstractMagicProjectileEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class MagicProjectileRenderer
extends EntityRenderer<AbstractMagicProjectileEntity> {
    private final ResourceLocation texture;
    private final MagicProjectileModel<AbstractMagicProjectileEntity> model;

    public MagicProjectileRenderer(EntityRendererProvider.Context pContext, String textureName, ModelLayerLocation layer) {
        super(pContext);
        this.texture = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)("textures/entity/" + textureName));
        this.model = new MagicProjectileModel(pContext.bakeLayer(layer));
    }

    public void render(AbstractMagicProjectileEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        float entityYRot = Mth.lerp((float)pPartialTicks, (float)pEntity.yRotO, (float)pEntity.getYRot());
        float entityXRot = Mth.lerp((float)pPartialTicks, (float)pEntity.xRotO, (float)pEntity.getXRot());
        if (pEntity.isCharging() && pEntity.getOwner() == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double entityX = Mth.lerp((double)pPartialTicks, (double)pEntity.xo, (double)pEntity.getX());
            double entityY = Mth.lerp((double)pPartialTicks, (double)pEntity.yo, (double)pEntity.getY());
            double entityZ = Mth.lerp((double)pPartialTicks, (double)pEntity.zo, (double)pEntity.getZ());
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double camX = camera.getPosition().x;
            double camY = camera.getPosition().y;
            double camZ = camera.getPosition().z;
            float xRot = camera.getXRot();
            float yRot = camera.getYRot();
            float f = xRot * ((float)Math.PI / 180);
            float f1 = -yRot * ((float)Math.PI / 180);
            float f2 = Mth.cos((float)f1);
            float f3 = Mth.sin((float)f1);
            float f4 = Mth.cos((float)f);
            float f5 = Mth.sin((float)f);
            double exactX = camX + (double)(f3 * f4) * 1.2;
            double exactY = camY - (double)f5 * 1.2 - 0.4;
            double exactZ = camZ + (double)(f2 * f4) * 1.2;
            pPoseStack.translate(exactX - entityX, exactY - entityY, exactZ - entityZ);
            entityYRot = yRot;
            entityXRot = xRot;
        }
        pPoseStack.mulPose(Axis.YP.rotationDegrees(entityYRot + 90.0f));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(-entityXRot));
        float spin = pEntity.getSpin(pPartialTicks);
        if (spin != 0.0f) {
            pPoseStack.mulPose(Axis.XP.rotationDegrees(spin));
        }
        if (pEntity.scalesWithCharge()) {
            float scale = pEntity.getChargeScale();
            pPoseStack.scale(scale, scale, scale);
        }
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, -1);
        pPoseStack.popPose();
        super.render((Entity)pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    public ResourceLocation getTextureLocation(AbstractMagicProjectileEntity pEntity) {
        return this.texture;
    }
}

