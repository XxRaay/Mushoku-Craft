package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.client.render.model.IcicleModel;
import com.mushokucraft.magic.entity.IcicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class IcicleRenderer extends EntityRenderer<IcicleEntity> {
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/icicle.png");
    private final IcicleModel<IcicleEntity> model;

    public IcicleRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new IcicleModel<>(pContext.bakeLayer(IcicleModel.LAYER_LOCATION));
    }

    @Override
    public void render(IcicleEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(IcicleEntity pEntity) {
        return texture;
    }
}
