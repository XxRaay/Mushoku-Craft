package com.mushokucraft.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.magic.entity.EarthLanceEntity;
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

public class EarthLanceModel extends EntityModel<EarthLanceEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("mushokucraft", "earth_lance"), "main");

    private final ModelPart mainSpike;
    private final ModelPart flankNorth;
    private final ModelPart flankSouth;
    private final ModelPart flankEast;
    private final ModelPart flankWest;

    public EarthLanceModel(ModelPart root) {
        this.mainSpike = root.getChild("main_spike");
        this.flankNorth = root.getChild("flank_north");
        this.flankSouth = root.getChild("flank_south");
        this.flankEast = root.getChild("flank_east");
        this.flankWest = root.getChild("flank_west");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Central faceted lance: base to sharp apex (52 pixels = ~3.25 blocks tall)
        PartDefinition main = root.addOrReplaceChild("main_spike", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        
        // Base collar
        main.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        // Mid tapered body
        main.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(0, 22)
                .addBox(-3.5F, -32.0F, -3.5F, 7.0F, 20.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        // Razor spear tip
        main.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(28, 22)
                .addBox(-2.0F, -52.0F, -2.0F, 4.0F, 20.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
        // Apex razor point
        main.addOrReplaceChild("apex", CubeListBuilder.create().texOffs(44, 22)
                .addBox(-1.0F, -60.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.ZERO);

        // 4 Flanking jagged spike shards jutting outward at 25-degree angles
        root.addOrReplaceChild("flank_north", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 24.0F, -4.5F, -0.42F, 0.0F, 0.0F));

        root.addOrReplaceChild("flank_south", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 24.0F, 4.5F, 0.42F, 0.0F, 0.0F));

        root.addOrReplaceChild("flank_east", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.5F, 24.0F, 0.0F, 0.0F, 0.0F, 0.42F));

        root.addOrReplaceChild("flank_west", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-2.5F, -18.0F, -2.5F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.5F, 24.0F, 0.0F, 0.0F, 0.0F, -0.42F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EarthLanceEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float eruptionProgress = 1.0F;
        if (entity.isErupting()) {
            int ticks = entity.getEruptionTicks();
            if (ticks < 4) {
                eruptionProgress = (float) Math.sin((ticks / 4.0F) * (Math.PI / 2.0));
            } else if (ticks > 20) {
                // Sinking / crumbling at the end
                eruptionProgress = Math.max(0.0F, 1.0F - ((ticks - 20) / 6.0F));
            }
        } else if (entity.isCharging()) {
            eruptionProgress = 0.08F; // Just the tip peeking through the cracked ground
        }

        float yOffset = (1.0F - eruptionProgress) * 56.0F;
        this.mainSpike.y = 24.0F + yOffset;
        this.flankNorth.y = 24.0F + yOffset * 0.7F;
        this.flankSouth.y = 24.0F + yOffset * 0.7F;
        this.flankEast.y = 24.0F + yOffset * 0.7F;
        this.flankWest.y = 24.0F + yOffset * 0.7F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        mainSpike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        flankNorth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        flankSouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        flankEast.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        flankWest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
