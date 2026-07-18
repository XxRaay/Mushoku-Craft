/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.resources.ResourceLocation
 */
package com.mushokucraft.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class WaterballModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"waterball"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 8).addBox(-3.0f, -1.0f, 3.0f, 6.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 10).addBox(3.0f, -1.0f, -2.0f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(10, 10).addBox(-4.0f, -1.0f, -2.0f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 9).addBox(-3.0f, -1.0f, -4.0f, 6.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(0, 14).addBox(-4.0f, -1.0f, -3.0f, 2.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(6, 14).addBox(2.0f, -1.0f, -3.0f, 2.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(14, 8).addBox(2.0f, -1.0f, 2.0f, 2.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(14, 9).addBox(-4.0f, -1.0f, 2.0f, 2.0f, 0.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)32, (int)32);
    }
}

