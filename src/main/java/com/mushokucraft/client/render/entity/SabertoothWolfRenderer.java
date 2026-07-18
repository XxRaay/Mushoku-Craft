package com.mushokucraft.client.render.entity;

import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SabertoothWolfRenderer extends MobRenderer<SabertoothWolfEntity, SabertoothWolfModel<SabertoothWolfEntity>> {
    public SabertoothWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new SabertoothWolfModel<>(context.bakeLayer(SabertoothWolfModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SabertoothWolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/entity/sabertooth_wolf.png");
    }

    @Override
    protected float getFlipDegrees(SabertoothWolfEntity entity) {
        return 0.0F; // Prevent vanilla 90-degree death flip
    }
}





