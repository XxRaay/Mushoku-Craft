/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.resources.ResourceLocation
 */
package com.mushokucraft.client.render.entity;

import com.mushokucraft.client.render.model.SabertoothWolfModel;
import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SabertoothWolfRenderer
extends MobRenderer<SabertoothWolfEntity, SabertoothWolfModel<SabertoothWolfEntity>> {
    public SabertoothWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new SabertoothWolfModel(context.bakeLayer(SabertoothWolfModel.LAYER_LOCATION)), 0.5f);
    }

    public ResourceLocation getTextureLocation(SabertoothWolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/entity/sabertooth_wolf.png");
    }

    protected float getFlipDegrees(SabertoothWolfEntity entity) {
        return 0.0f;
    }
}

