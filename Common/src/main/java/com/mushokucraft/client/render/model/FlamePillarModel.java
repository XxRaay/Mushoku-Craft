package com.mushokucraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.magic.entity.FlamePillarEntity;
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

public class FlamePillarModel extends EntityModel<FlamePillarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "flame_pillar"), "main");

    private final ModelPart outerPillar;
    private final ModelPart innerCore;

    public FlamePillarModel(ModelPart root) {
        this.outerPillar = root.getChild("outer_pillar");
        this.innerCore = root.getChild("inner_core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Outer swirling flame column (vertical 4-plane intersecting cylinder, 64 pixels tall = 4 blocks base)
        PartDefinition outer = root.addOrReplaceChild("outer_pillar", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        outer.addOrReplaceChild("outer_plane1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-14.0F, -64.0F, 0.0F, 28.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        outer.addOrReplaceChild("outer_plane2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-14.0F, -64.0F, 0.0F, 28.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.0F, 1.5708F, 0.0F));
        outer.addOrReplaceChild("outer_plane3", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-14.0F, -64.0F, 0.0F, 28.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.0F, 0.7854F, 0.0F));
        outer.addOrReplaceChild("outer_plane4", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-14.0F, -64.0F, 0.0F, 28.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.0F, -0.7854F, 0.0F));

        // Inner superheated core column (tighter, rotates counter to outer column)
        PartDefinition inner = root.addOrReplaceChild("inner_core", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        inner.addOrReplaceChild("inner_plane1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -64.0F, 0.0F, 16.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        inner.addOrReplaceChild("inner_plane2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -64.0F, 0.0F, 16.0F, 64.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.rotation(0.0F, 1.5708F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(FlamePillarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.outerPillar.yRot = ageInTicks * 0.16F;
        this.innerCore.yRot = -ageInTicks * 0.24F;
    }

    public void renderPillarColumns(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        outerPillar.render(poseStack, consumer, light, overlay, color);
        innerCore.render(poseStack, consumer, light, overlay, color);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        outerPillar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        innerCore.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
