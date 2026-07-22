package com.mushokucraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mushokucraft.magic.entity.AirStrikeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AirStrikeRenderer extends EntityRenderer<AirStrikeEntity> {
    
    // Invisible entity, we don't render a model, only particles in its tick() method.
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/empty.png");

    public AirStrikeRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(AirStrikeEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        // Render nothing, it's invisible wind
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AirStrikeEntity pEntity) {
        return TEXTURE;
    }
}





