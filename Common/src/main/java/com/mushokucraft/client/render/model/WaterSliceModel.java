package com.mushokucraft.client.render.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class WaterSliceModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "water_slice"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(4, 6).addBox(2.0F, -9.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(4, 4).addBox(0.0F, -7.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(4, 2).addBox(0.0F, 5.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
        .texOffs(4, 0).addBox(2.0F, 7.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}
