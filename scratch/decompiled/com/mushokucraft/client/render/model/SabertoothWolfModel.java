/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HierarchicalModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.PartPose
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.CubeListBuilder
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package com.mushokucraft.client.render.model;

import com.mushokucraft.client.render.model.SabertoothWolfAnimation;
import com.mushokucraft.entity.monster.DeadSabertoothWolfEntity;
import com.mushokucraft.entity.monster.SabertoothWolfEntity;
import net.minecraft.client.model.HierarchicalModel;
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

public class SabertoothWolfModel<T extends Entity>
extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sabertooth_wolf"), "main");
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
        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation((float)0.0f, (float)24.0f, (float)0.0f, (float)0.0f, (float)((float)Math.PI), (float)0.0f));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)0.0f, (float)6.0f));
        PartDefinition leg3 = body.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(54, 67).addBox(-2.0f, 2.0f, -6.0f, 3.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)).texOffs(72, 35).addBox(-2.0f, 2.0f, -7.0f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)5.0f, (float)-9.0f, (float)-20.0f));
        PartDefinition cube_r1 = leg3.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(36, 69).addBox(-2.0f, -4.0f, -2.0f, 3.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)3.0f, (float)1.0f, (float)0.9163f, (float)0.0f, (float)0.0f));
        PartDefinition foot = leg3.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(74, 75).addBox(0.0f, -0.2f, 1.0f, 1.0f, 0.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(76, 58).addBox(-2.0f, -0.2f, 1.0f, 1.0f, 0.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(64, 56).addBox(-2.0f, -1.0f, 1.0f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(52, 38).addBox(-2.0f, -5.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)9.0f, (float)-7.0f));
        PartDefinition leg4 = body.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(62, 75).addBox(-2.0f, 2.0f, -7.0f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(20, 69).addBox(-2.0f, 2.0f, -6.0f, 3.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-4.0f, (float)-9.0f, (float)-20.0f));
        PartDefinition cube_r2 = leg4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(70, 67).addBox(-2.0f, -4.0f, -2.0f, 3.0f, 3.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)3.0f, (float)1.0f, (float)0.9163f, (float)0.0f, (float)0.0f));
        PartDefinition foot2 = leg4.addOrReplaceChild("foot2", CubeListBuilder.create().texOffs(76, 60).addBox(1.0f, 3.8f, 1.0f, 1.0f, 0.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(76, 62).addBox(-1.0f, 3.8f, 1.0f, 1.0f, 0.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(72, 56).addBox(-1.0f, 3.0f, 1.0f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(20, 62).addBox(-1.0f, -1.0f, -1.0f, 3.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-1.0f, (float)5.0f, (float)-7.0f));
        PartDefinition bone2 = body.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-11.0f, (float)-14.0f));
        PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, -5.221f, -8.1261f, 10.0f, 7.0f, 16.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-11.0f, (float)0.0f));
        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 23).addBox(-7.0f, -7.8316f, -6.1128f, 14.0f, 11.0f, 12.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-14.0f, (float)-22.0f));
        PartDefinition cube_r5 = tail.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(72, 19).addBox(-3.0f, -5.0f, -5.0f, 4.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(0, 46).addBox(-3.0f, -8.0f, -5.0f, 4.0f, 3.0f, 13.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)1.0f, (float)7.5f, (float)-6.5f, (float)0.1745f, (float)0.0f, (float)0.0f));
        PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create(), PartPose.offsetAndRotation((float)7.0f, (float)-11.0f, (float)-1.0f, (float)0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition cube_r6 = leg1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(52, 12).addBox(-9.0f, -4.0f, 16.0f, 4.0f, 7.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)7.0f, (float)3.3752f, (float)-18.5321f, (float)0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition foot3 = leg1.addOrReplaceChild("foot3", CubeListBuilder.create(), PartPose.offset((float)-4.0f, (float)6.6752f, (float)2.4679f));
        PartDefinition cube_r7 = foot3.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(72, 31).addBox(2.0f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(72, 27).addBox(0.5f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(72, 23).addBox(-1.0f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(54, 58).addBox(-1.0f, 2.6922f, -5.2019f, 4.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)3.0f, (float)0.0f, (float)0.0f, (float)-0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition cube_r8 = foot3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(34, 58).addBox(2.0f, -1.3078f, -5.2019f, 4.0f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-0.3f, (float)0.0f, (float)-0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition leg2 = body.addOrReplaceChild("leg2", CubeListBuilder.create(), PartPose.offsetAndRotation((float)-6.0f, (float)-11.0f, (float)-1.0f, (float)0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition cube_r9 = leg2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(52, 25).addBox(-10.0f, -4.0f, 16.0f, 4.0f, 7.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)7.0f, (float)3.3752f, (float)-18.5321f, (float)0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition foot4 = leg2.addOrReplaceChild("foot4", CubeListBuilder.create(), PartPose.offset((float)-5.0f, (float)6.6752f, (float)2.4679f));
        PartDefinition cube_r10 = foot4.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(52, 75).addBox(2.0f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(10, 73).addBox(0.5f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 73).addBox(-1.0f, 3.6922f, -1.2019f, 1.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(64, 38).addBox(-1.0f, 2.6922f, -5.2019f, 4.0f, 2.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)3.0f, (float)0.0f, (float)0.0f, (float)-0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition cube_r11 = foot4.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 62).addBox(2.0f, -0.5774f, -3.0885f, 4.0f, 5.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)-1.3f, (float)-2.0f, (float)-0.1309f, (float)0.0f, (float)0.0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(30, 62).addBox(-2.0f, 1.0f, 12.0f, 1.0f, 4.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(70, 75).addBox(1.0f, 1.0f, 12.0f, 1.0f, 4.0f, 1.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)-14.0f, (float)5.0f));
        PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(76, 64).addBox(-2.0f, -6.0f, 37.0f, 2.0f, 1.0f, 1.0f, new CubeDeformation(0.0f)).texOffs(64, 47).addBox(-4.0f, -9.0f, 35.0f, 6.0f, 7.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(52, 0).addBox(-5.0f, -9.0f, 30.0f, 8.0f, 7.0f, 5.0f, new CubeDeformation(0.0f)).texOffs(34, 46).addBox(-6.0f, -9.0f, 25.0f, 10.0f, 7.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)1.0f, (float)6.0f, (float)-24.0f, (float)0.0436f, (float)0.0f, (float)0.0f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)128, (int)128);
    }

    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (!(entity instanceof DeadSabertoothWolfEntity)) {
            this.head.yRot = -netHeadYaw * ((float)Math.PI / 180);
            this.head.xRot = -headPitch * ((float)Math.PI / 180);
        }
        if (entity instanceof SabertoothWolfEntity) {
            SabertoothWolfEntity wolf = (SabertoothWolfEntity)((Object)entity);
            this.animate(wolf.idleAnimationState, SabertoothWolfAnimation.idle, ageInTicks, 1.0f);
            this.animate(wolf.walkAnimationState, SabertoothWolfAnimation.walk, ageInTicks, 1.0f);
            this.animate(wolf.attackAnimationState, SabertoothWolfAnimation.attack, ageInTicks, 1.0f);
            this.animate(wolf.damageAnimationState, SabertoothWolfAnimation.damage, ageInTicks, 1.0f);
            this.animate(wolf.deathAnimationState, SabertoothWolfAnimation.death, ageInTicks, 1.0f);
        } else if (entity instanceof DeadSabertoothWolfEntity) {
            DeadSabertoothWolfEntity deadWolf = (DeadSabertoothWolfEntity)((Object)entity);
            this.animate(deadWolf.deadAnimationState, SabertoothWolfAnimation.dead, ageInTicks, 1.0f);
        }
    }
}

