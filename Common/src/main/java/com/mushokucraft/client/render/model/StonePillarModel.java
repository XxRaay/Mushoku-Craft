package com.mushokucraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.magic.entity.StonePillarEntity;
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

public class StonePillarModel extends EntityModel<StonePillarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "stone_pillar"), "main");

    private final ModelPart pillar;

    public StonePillarModel(ModelPart root) {
        this.pillar = root.getChild("pillar");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Monolithic pillar: base, main column, capital, and rugged top crown
        PartDefinition p = root.addOrReplaceChild("pillar", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        // Pediment Base: 24x16x24 (1.5 blocks wide)
        p.addOrReplaceChild("pediment", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-12.0F, -16.0F, -12.0F, 24.0F, 16.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Lower Column: 18x48x18
        p.addOrReplaceChild("lower_shaft", CubeListBuilder.create().texOffs(0, 40)
                .addBox(-9.0F, -64.0F, -9.0F, 18.0F, 48.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Mid-Capital Belt: 21x12x21
        p.addOrReplaceChild("mid_belt", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-10.5F, -76.0F, -10.5F, 21.0F, 12.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Upper Column: 16x56x16
        p.addOrReplaceChild("upper_shaft", CubeListBuilder.create().texOffs(64, 40)
                .addBox(-8.0F, -132.0F, -8.0F, 16.0F, 56.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // Cragged Monolith Top: 18x20x18
        p.addOrReplaceChild("top_crown", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-9.0F, -152.0F, -9.0F, 18.0F, 20.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(StonePillarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float eruptionProgress = 1.0F;
        if (entity.isErupting()) {
            int ticks = entity.getEruptionTicks();
            if (ticks < 6) {
                // Explosive surge upward
                eruptionProgress = (float) Math.sin((ticks / 6.0F) * (Math.PI / 2.0));
            } else if (ticks > 72) {
                // Shaking and sinking crumbling
                float crumble = (ticks - 72) / 8.0F;
                eruptionProgress = Math.max(0.0F, 1.0F - crumble);
                this.pillar.xRot = (float) Math.sin(ticks * 2.0) * 0.03F;
                this.pillar.zRot = (float) Math.cos(ticks * 2.0) * 0.03F;
            } else {
                this.pillar.xRot = 0.0F;
                this.pillar.zRot = 0.0F;
            }
        } else if (entity.isCharging()) {
            eruptionProgress = 0.05F; // Ground trembling crest
        }

        float yOffset = (1.0F - eruptionProgress) * 152.0F;
        this.pillar.y = 24.0F + yOffset;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        pillar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
