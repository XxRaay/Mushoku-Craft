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
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(0, 8).addBox(-3.0F, -1.0F, 3.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(0, 10).addBox(3.0F, -1.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(10, 10).addBox(-4.0F, -1.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
        .texOffs(0, 9).addBox(-3.0F, -1.0F, -4.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(0, 14).addBox(-4.0F, -1.0F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(6, 14).addBox(2.0F, -1.0F, -3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(14, 8).addBox(2.0F, -1.0F, 2.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
        .texOffs(14, 9).addBox(-4.0F, -1.0F, 2.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}





