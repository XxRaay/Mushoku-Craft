package com.mushokucraft.client.render.model;

import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class SabertoothWolfModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "sabertooth_wolf"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;

    public SabertoothWolfModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, (float)Math.PI, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 6.0F));

        PartDefinition leg3 = body.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(54, 67).addBox(-2.0F, 2.0F, -6.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(72, 35).addBox(-2.0F, 2.0F, -7.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -9.0F, -20.0F));

        PartDefinition cube_r1 = leg3.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 69).addBox(-2.0F, -4.0F, -2.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, 0.9163F, 0.0F, 0.0F));

        PartDefinition foot = leg3.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(74, 75).addBox(0.0F, -0.2F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(76, 58).addBox(-2.0F, -0.2F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 56).addBox(-2.0F, -1.0F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(52, 38).addBox(-2.0F, -5.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, -7.0F));

        PartDefinition leg4 = body.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(62, 75).addBox(-2.0F, 2.0F, -7.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 69).addBox(-2.0F, 2.0F, -6.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -9.0F, -20.0F));

        PartDefinition cube_r2 = leg4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(70, 67).addBox(-2.0F, -4.0F, -2.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, 0.9163F, 0.0F, 0.0F));

        PartDefinition foot2 = leg4.addOrReplaceChild("foot2", CubeListBuilder.create().texOffs(76, 60).addBox(1.0F, 3.8F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(76, 62).addBox(-1.0F, 3.8F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(72, 56).addBox(-1.0F, 3.0F, 1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 62).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 5.0F, -7.0F));

        PartDefinition bone2 = body.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, -14.0F));

        PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.221F, -8.1261F, 10.0F, 7.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 23).addBox(-7.0F, -7.8316F, -6.1128F, 14.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -14.0F, -22.0F));

        PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(72, 19).addBox(-3.0F, -5.0F, -5.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-3.0F, -8.0F, -5.0F, 4.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 7.5F, -6.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, -11.0F, -1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r6 = leg1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(52, 12).addBox(-9.0F, -4.0F, 16.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 3.3752F, -18.5321F, 0.0436F, 0.0F, 0.0F));

        PartDefinition foot3 = leg1.addOrReplaceChild("foot3", CubeListBuilder.create(), PartPose.offset(-4.0F, 6.6752F, 2.4679F));

        PartDefinition cube_r7 = foot3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(72, 31).addBox(2.0F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(72, 27).addBox(0.5F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(72, 23).addBox(-1.0F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(54, 58).addBox(-1.0F, 2.6922F, -5.2019F, 4.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r8 = foot3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(34, 58).addBox(2.0F, -1.3078F, -5.2019F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, 0.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition leg2 = body.addOrReplaceChild("leg2", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.0F, -11.0F, -1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r9 = leg2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 25).addBox(-10.0F, -4.0F, 16.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 3.3752F, -18.5321F, 0.0436F, 0.0F, 0.0F));

        PartDefinition foot4 = leg2.addOrReplaceChild("foot4", CubeListBuilder.create(), PartPose.offset(-5.0F, 6.6752F, 2.4679F));

        PartDefinition cube_r10 = foot4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(52, 75).addBox(2.0F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(10, 73).addBox(0.5F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 73).addBox(-1.0F, 3.6922F, -1.2019F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(64, 38).addBox(-1.0F, 2.6922F, -5.2019F, 4.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r11 = foot4.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 62).addBox(2.0F, -0.5774F, -3.0885F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.3F, -2.0F, -0.1309F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(30, 62).addBox(-2.0F, 1.0F, 12.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(70, 75).addBox(1.0F, 1.0F, 12.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 5.0F));

        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(76, 64).addBox(-2.0F, -6.0F, 37.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(64, 47).addBox(-4.0F, -9.0F, 35.0F, 6.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 0).addBox(-5.0F, -9.0F, 30.0F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(34, 46).addBox(-6.0F, -9.0F, 25.0F, 10.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 6.0F, -24.0F, 0.0436F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // Invert head rotations because the root is rotated 180 degrees
        if (!(entity instanceof DeadSabertoothWolfEntity)) {
            this.head.yRot = -netHeadYaw * ((float)Math.PI / 180F);
            this.head.xRot = -headPitch * ((float)Math.PI / 180F);
        }

        if (entity instanceof SabertoothWolfEntity wolf) {
            this.animate(wolf.idleAnimationState, SabertoothWolfAnimation.idle, ageInTicks, 1f);
            this.animate(wolf.walkAnimationState, SabertoothWolfAnimation.walk, ageInTicks, 1f);
            this.animate(wolf.attackAnimationState, SabertoothWolfAnimation.attack, ageInTicks, 1f);
            this.animate(wolf.damageAnimationState, SabertoothWolfAnimation.damage, ageInTicks, 1f);
            this.animate(wolf.deathAnimationState, SabertoothWolfAnimation.death, ageInTicks, 1f);
        } else if (entity instanceof DeadSabertoothWolfEntity deadWolf) {
            this.animate(deadWolf.deadAnimationState, SabertoothWolfAnimation.dead, ageInTicks, 1f);
        }
    }
}





