package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.client.render.model.IcicleBreakTargetModel;
import com.mushokucraft.magic.entity.IcicleBreakTargetEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class IcicleBreakTargetRenderer extends EntityRenderer<IcicleBreakTargetEntity> {
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/icicle_break_target.png");
    private final IcicleBreakTargetModel<IcicleBreakTargetEntity> model;

    public IcicleBreakTargetRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new IcicleBreakTargetModel<>(pContext.bakeLayer(IcicleBreakTargetModel.LAYER_LOCATION));
    }

    @Override
    public void render(IcicleBreakTargetEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        
        // Apply standard Java Entity transforms to fix orientation and height
        pPoseStack.translate(0.0F, 1.5F, 0.0F);
        pPoseStack.scale(-1.0F, -1.0F, 1.0F);

        VertexConsumer vertexconsumer = pBuffer.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(this.texture));
        this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public boolean shouldRender(IcicleBreakTargetEntity pLivingEntity, net.minecraft.client.renderer.culling.Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return pLivingEntity.hasSnappedToGround();
    }

    @Override
    public ResourceLocation getTextureLocation(IcicleBreakTargetEntity pEntity) {
        return texture;
    }
}
