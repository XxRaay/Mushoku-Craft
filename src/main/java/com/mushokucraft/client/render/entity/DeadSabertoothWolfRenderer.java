package com.mushokucraft.client.render.entity;

import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class DeadSabertoothWolfRenderer extends EntityRenderer<DeadSabertoothWolfEntity> {
    private final SabertoothWolfModel<DeadSabertoothWolfEntity> model;

    public DeadSabertoothWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SabertoothWolfModel<>(context.bakeLayer(SabertoothWolfModel.LAYER_LOCATION));
    }

    @Override
    public void render(DeadSabertoothWolfEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Setup base rotations similar to living entity
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - entityYaw));
        
        // Flip Y axis and translate down to align feet to the ground (like LivingEntityRenderer)
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        
        com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DeadSabertoothWolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/sabertooth_wolf.png");
    }
}





