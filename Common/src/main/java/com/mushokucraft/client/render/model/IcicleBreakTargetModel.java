package com.mushokucraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.entity.Entity;

public class IcicleBreakTargetModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "icicle_break_target"), "main");
    private final ModelPart bone4;
    private final ModelPart bone5;

    public IcicleBreakTargetModel(ModelPart root) {
        this.bone4 = root.getChild("bone4");
        this.bone5 = root.getChild("bone5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(67, 82).addBox(-43.0F, -1.0F, -32.0F, 2.0F, 1.0F, 29.0F, new CubeDeformation(0.0F))
        .texOffs(66, 112).addBox(3.0F, -1.0F, -33.0F, 2.0F, 1.0F, 29.0F, new CubeDeformation(0.0F)), PartPose.offset(19.0F, 24.0F, 18.0F));

        bone4.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, -1.0F, -15.0F, 2.0F, 1.0F, 29.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, 0.0F, 5.0F, 0.0F, 1.5708F, 0.0F));
        bone4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 82).addBox(-1.0F, -1.0F, -16.0F, 2.0F, 1.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, 0.0F, -41.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone6 = bone4.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offset(-37.0F, 0.0F, 1.0F));
        bone6.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(130, 93).addBox(-5.0F, -1.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(130, 81).addBox(-3.0F, -1.0F, -17.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -3.0F, 0.0F, 1.5708F, 0.0F));
        bone6.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(128, 112).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone = bone4.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(130, 84).addBox(-3.0F, -1.0F, -17.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(130, 96).addBox(-5.0F, -1.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -3.0F, 0.0F, 1.5708F, 0.0F));
        bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(128, 117).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone2 = bone4.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -37.0F, 0.0F, 3.1416F, 0.0F));
        bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(130, 87).addBox(-3.0F, -1.0F, -17.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(130, 99).addBox(-5.0F, -1.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -3.0F, 0.0F, 1.5708F, 0.0F));
        bone2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(128, 122).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone3 = bone4.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(-38.0F, 0.0F, -36.0F, 0.0F, -1.5708F, 0.0F));
        bone3.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(130, 90).addBox(-3.0F, -1.0F, -17.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(130, 102).addBox(-5.0F, -1.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -3.0F, 0.0F, 1.5708F, 0.0F));
        bone3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(128, 127).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.0F, 0.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

        partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 0).addBox(-18.0F, 0.0F, -22.0F, 37.0F, 0.0F, 44.0F, new CubeDeformation(0.0F))
        .texOffs(1, 45).addBox(-22.0F, 0.0F, -18.0F, 44.0F, 0.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bone4.yRot = ageInTicks * 0.25F; // Fast outer spin
        this.bone5.yRot = -ageInTicks * 0.15F; // Fast inner spin in opposite direction
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        int bone5Color = (color & 0x00FFFFFF) | (((color >> 24) & 0xFF) / 2 << 24);
        bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, bone5Color);
    }
}
