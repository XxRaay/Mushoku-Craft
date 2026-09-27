package com.mushokucraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.magic.entity.ExodusFlameEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class ExodusFlameModel extends EntityModel<ExodusFlameEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "exodus_flame"), "main");

    private final ModelPart core;
    private final ModelPart mantle;

    public ExodusFlameModel(ModelPart root) {
        this.core = root.getChild("core");
        this.mantle = root.getChild("mantle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 1. Incandescent white-hot inner core (8x8x8 cube)
        root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 2. Churning golden plasma outer mantle (12x12x12 cube, nested outside core with zero intersection)
        root.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ExodusFlameEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Gentle, majestic rotation (no high-frequency stroboscopic spin)
        this.core.yRot = ageInTicks * 0.04F;
        this.core.xRot = ageInTicks * 0.02F;

        this.mantle.yRot = -ageInTicks * 0.05F;
        this.mantle.zRot = ageInTicks * 0.03F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        // Inner white-hot core
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        // Outer golden plasma mantle
        mantle.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
