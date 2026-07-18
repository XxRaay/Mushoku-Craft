/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class DeadSabertoothWolfRenderer
extends EntityRenderer<DeadSabertoothWolfEntity> {
    private final SabertoothWolfModel<DeadSabertoothWolfEntity> model;

    public DeadSabertoothWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SabertoothWolfModel(context.bakeLayer(SabertoothWolfModel.LAYER_LOCATION));
    }

    public void render(DeadSabertoothWolfEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        this.model.setupAnim(entity, 0.0f, 0.0f, (float)entity.tickCount + partialTicks, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render((Entity)entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    public ResourceLocation getTextureLocation(DeadSabertoothWolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/entity/sabertooth_wolf.png");
    }
}

