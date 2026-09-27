package com.mushokucraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.magic.circle.DimensionalGateCircleType;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class MagicCircleBlockEntityRenderer implements BlockEntityRenderer<MagicCircleBlockEntity> {
    private static final ResourceLocation BLANK_TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/block/magic_circle_base.png");

    public MagicCircleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MagicCircleBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int size = be.getSize();
        boolean sheared = be.isSheared();
        long time = System.currentTimeMillis();

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        float minCoord = (size == 3) ? -1.0f : 0.0f;
        float totalSpan = (size == 3) ? 3.0f : 1.0f;
        float maxCoord = minCoord + totalSpan;

        // If not sheared, render the paper underlay (1x1 or 3x3)
        if (!sheared) {
            VertexConsumer paperBuilder = buffer.getBuffer(RenderType.entityCutout(BLANK_TEXTURE));
            addQuadUV(paperBuilder, pose, minCoord, maxCoord, minCoord, maxCoord, 0.0625f, 255, 255, 255, 255, packedLight, 0.0f, totalSpan, 0.0f, totalSpan);
            addSideNorth(paperBuilder, pose, minCoord, maxCoord, minCoord, 0.0f, 0.0625f, 255, 255, 255, 255, packedLight);
            addSideSouth(paperBuilder, pose, minCoord, maxCoord, maxCoord, 0.0f, 0.0625f, 255, 255, 255, 255, packedLight);
            addSideWest(paperBuilder, pose, minCoord, minCoord, maxCoord, 0.0f, 0.0625f, 255, 255, 255, 255, packedLight);
            addSideEast(paperBuilder, pose, maxCoord, minCoord, maxCoord, 0.0f, 0.0625f, 255, 255, 255, 255, packedLight);
        }

        MagicCirclePattern pattern = be.getPattern();
        if (pattern == null || pattern.countFilled() == 0) {
            poseStack.popPose();
            return;
        }

        // When sheared, runes lie flat on the ground (y = 0.005); when on paper, elevated (y = 0.0635)
        float yOffset = sheared ? 0.005f : 0.0635f;
        float pixelSpan = totalSpan / 16.0f;

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(BLANK_TEXTURE));

        boolean isGateActive = be.isDimensionalGate();
        boolean isGateCandidate = be.hasDimensionalGateType();
        boolean isAnchorActive = be.isSoulAnchor();
        boolean isAnchorCandidate = be.hasSoulAnchorType();
        boolean isLinked = be.getLinkedPos() != null;
        float currentMana = be.getCurrentMana();
        float reqMana = Math.max(1.0f, be.getRequiredMana());
        float chargeRatio = Math.min(1.0f, currentMana / reqMana);

        // Ground Rune Color computation
        int r, g, b, a;
        int light = packedLight;

        if (chargeRatio > 0.0f) {
            float pulse = (float) Math.sin((time % 2000) / 2000.0 * Math.PI * 2) * 0.2f + 0.8f;
            if (isAnchorActive || isAnchorCandidate) {
                r = (int) (40 * pulse);
                g = (int) (235 * pulse);
                b = (int) (245 * pulse);
            } else if (isGateActive || isGateCandidate) {
                r = (int) (210 * pulse);
                g = (int) (130 * pulse);
                b = (int) (255 * pulse);
            } else {
                r = (int) (140 * pulse);
                g = (int) (220 * pulse);
                b = (int) (255 * pulse);
            }
            a = 255;
            light = LightTexture.FULL_BRIGHT;
        } else if (isAnchorActive) {
            // Radiant active soul anchor runes
            float pulse = (float) Math.sin((time % 1500) / 1500.0 * Math.PI * 2) * 0.15f + 0.85f;
            r = (int) (35 * pulse);
            g = (int) (220 * pulse);
            b = (int) (240 * pulse);
            a = 245;
            light = LightTexture.FULL_BRIGHT;
        } else if (isGateActive) {
            // Radiant active gate foundation runes
            float pulse = (float) Math.sin((time % 1500) / 1500.0 * Math.PI * 2) * 0.15f + 0.85f;
            r = (int) (195 * pulse);
            g = (int) (110 * pulse);
            b = (int) (255 * pulse);
            a = 245;
            light = LightTexture.FULL_BRIGHT;
        } else if (isLinked) {
            if (isGateCandidate) {
                r = 175;
                g = 95;
                b = 255;
                a = 240;
                light = LightTexture.FULL_BRIGHT;
            } else {
                r = 80;
                g = 180;
                b = 230;
                a = 230;
            }
        } else if (isAnchorCandidate) {
            // Dormant soul anchor base - subtle pulsing soul cyan
            float pulse = (float) Math.sin((time % 3000) / 3000.0 * Math.PI * 2) * 0.15f + 0.65f;
            r = (int) (20 * pulse);
            g = (int) (150 * pulse);
            b = (int) (170 * pulse);
            a = 220;
        } else if (isGateCandidate) {
            // Dormant gate base - subtle pulsing violet
            float pulse = (float) Math.sin((time % 3000) / 3000.0 * Math.PI * 2) * 0.15f + 0.65f;
            r = (int) (115 * pulse);
            g = (int) (45 * pulse);
            b = (int) (175 * pulse);
            a = 220;
        } else {
            r = 35;
            g = 25;
            b = 50;
            a = 240;
        }

        // Draw ground 16x16 rune pattern quads
        for (int gy = 0; gy < MagicCirclePattern.SIZE; gy++) {
            for (int gx = 0; gx < MagicCirclePattern.SIZE; gx++) {
                if (!pattern.getPixel(gx, gy)) continue;

                float x0 = minCoord + gx * pixelSpan;
                float x1 = minCoord + (gx + 1) * pixelSpan;
                float z0 = minCoord + gy * pixelSpan;
                float z1 = minCoord + (gy + 1) * pixelSpan;

                addQuad(builder, pose, x0, x1, z0, z1, yOffset, r, g, b, a, light);
            }
        }

        // Draw ground anime concentric boundary ring for 3x3 circles
        if (size == 3) {
            float gRingR = 1.46f;
            int ringLight = isGateActive ? LightTexture.FULL_BRIGHT : light;
            addCircleRing(builder, pose, 0.5f, 0.5f, yOffset, gRingR, 0.035f, 32, r, g, b, a, ringLight);
            addCircleRing(builder, pose, 0.5f, 0.5f, yOffset, gRingR * 0.88f, 0.02f, 32, r, g, b, (int) (a * 0.6f), ringLight);
            addRingTicks(builder, pose, 0.5f, 0.5f, yOffset, gRingR, 0.06f, 0.025f, 8, r, g, b, a, ringLight);
        }

        // Render additional floating layers
        List<MagicCircleBlockEntity.CircleLayer> layers = be.getAdditionalLayers();
        for (int i = 0; i < layers.size(); i++) {
            MagicCircleBlockEntity.CircleLayer layer = layers.get(i);
            MagicCirclePattern layerPattern = layer.getPattern();
            if (layerPattern == null || layerPattern.countFilled() == 0) continue;

            float baseHeight = (i == 0) ? 0.38f : 0.72f;
            float bob = (float) Math.sin((time + i * 800) / 450.0) * 0.02f;
            float layerY = baseHeight + bob;

            float rot = (i == 0) ?
                    (float) ((time / 42.0) % 360.0) :
                    (float) (-(time / 32.0) % 360.0);

            int layerSize = layer.getSize();
            float lSpan = (layerSize == 3) ? 3.0f : 1.0f;
            float lHalfSpan = lSpan * 0.5f;
            float lPixelSpan = lSpan / 16.0f;

            int[] col = getLayerColor(layer.getCircleTypeId());
            int lr = col[0];
            int lg = col[1];
            int lb = col[2];
            int la = col[3];

            poseStack.pushPose();
            poseStack.translate(0.5f, layerY, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(rot));

            // Draw layer rune quads
            for (int gy = 0; gy < MagicCirclePattern.SIZE; gy++) {
                for (int gx = 0; gx < MagicCirclePattern.SIZE; gx++) {
                    if (!layerPattern.getPixel(gx, gy)) continue;

                    float x0 = -lHalfSpan + gx * lPixelSpan;
                    float x1 = x0 + lPixelSpan;
                    float z0 = -lHalfSpan + gy * lPixelSpan;
                    float z1 = z0 + lPixelSpan;

                    addQuad(builder, poseStack.last(), x0, x1, z0, z1, 0.0f, lr, lg, lb, la, LightTexture.FULL_BRIGHT);
                }
            }

            // Draw glowing concentric anime outer boundary ring around the layer
            float ringRadius = lHalfSpan * 0.98f;
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, ringRadius, 0.035f, 32, lr, lg, lb, la, LightTexture.FULL_BRIGHT);
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, ringRadius * 0.86f, 0.02f, 32, lr, lg, lb, (int) (la * 0.65f), LightTexture.FULL_BRIGHT);

            // Cardinal tick brackets on the layer
            addRingTicks(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, ringRadius, 0.05f, 0.025f, 4, lr, lg, lb, la, LightTexture.FULL_BRIGHT);

            // Rotating ornamental geometric star inside the layer!
            if (i == 0) {
                // Layer 0: Hexagram (interlocking equilateral triangles)
                addHexagram(builder, poseStack.last(), 0.0f, ringRadius * 0.72f, 0.02f, lr, lg, lb, (int) (la * 0.55f), LightTexture.FULL_BRIGHT);
            } else {
                // Layer 1: Octagram (interlocking squares)
                addOctagram(builder, poseStack.last(), 0.0f, ringRadius * 0.72f, 0.02f, lr, lg, lb, (int) (la * 0.55f), LightTexture.FULL_BRIGHT);
            }

            poseStack.popPose();
        }

        // Dimensional Gate central vertical vortex beam & dual 3D gyroscopic astrolabe
        if (isGateActive) {
            float pulse = (float) Math.sin(time / 280.0) * 0.15f + 0.85f;
            int beamR = (int) (195 * pulse);
            int beamG = (int) (125 * pulse);
            int beamB = 255;
            int beamAlpha = isLinked ? (int) (165 * pulse) : (int) (95 * pulse);

            // 1. Central vertical rotating vortex (6 intersecting planes, height 2.8 blocks)
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.05f, 0.5f);

            float vortexAngle = (float) ((time / 45.0) % 360.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(vortexAngle));

            float beamRadius = 0.62f;
            float beamHeight = 2.8f;

            for (int rAngle = 0; rAngle < 180; rAngle += 30) {
                float rad = (float) Math.toRadians(rAngle);
                float dx = (float) Math.cos(rad) * beamRadius;
                float dz = (float) Math.sin(rad) * beamRadius;
                addVerticalQuad(builder, poseStack.last(), -dx, -dz, dx, dz, 0.0f, beamHeight, beamR, beamG, beamB, beamAlpha, LightTexture.FULL_BRIGHT);
            }
            poseStack.popPose();

            // 2. Central Dimensional Core Flare at Y = 0.55
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.55f + (float) Math.sin(time / 400.0) * 0.03f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((time / 20.0) % 360.0)));
            float flareRadius = 0.22f + (float) Math.sin(time / 200.0) * 0.04f;
            addCoreFlare(builder, poseStack.last(), flareRadius, 255, 230, 255, (int) (220 * pulse));
            poseStack.popPose();

            // 3. Astrolabe Gyroscopic Outer Ring (tilted +28° on X, rotating on Y)
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.15f + (float) Math.sin(time / 450.0) * 0.04f, 0.5f);
            poseStack.mulPose(Axis.XP.rotationDegrees(28.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((time / 25.0) % 360.0)));
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 1.05f, 0.038f, 32, 225, 140, 255, 220, LightTexture.FULL_BRIGHT);
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.94f, 0.02f, 32, 225, 140, 255, 140, LightTexture.FULL_BRIGHT);
            addRingTicks(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 1.05f, 0.05f, 0.02f, 8, 225, 140, 255, 200, LightTexture.FULL_BRIGHT);
            poseStack.popPose();

            // 4. Astrolabe Gyroscopic Inner Ring (tilted -28° on Z, counter-rotating on Y)
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.15f + (float) Math.sin(time / 450.0) * 0.04f, 0.5f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-28.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (-(time / 20.0) % 360.0)));
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.88f, 0.032f, 32, 130, 220, 255, 220, LightTexture.FULL_BRIGHT);
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.78f, 0.018f, 32, 130, 220, 255, 130, LightTexture.FULL_BRIGHT);
            addRingTicks(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.88f, 0.04f, 0.02f, 6, 130, 220, 255, 190, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        // Soul Anchor central vertical spirit pillar & hovering Soul Core
        if (isAnchorActive) {
            float pulse = (float) Math.sin(time / 280.0) * 0.15f + 0.85f;
            boolean isBound = be.isBound();

            int pillarR = isBound ? (int) (60 * pulse) : (int) (40 * pulse);
            int pillarG = isBound ? (int) (240 * pulse) : (int) (200 * pulse);
            int pillarB = isBound ? (int) (255 * pulse) : (int) (230 * pulse);
            int pillarAlpha = isBound ? 150 : 90;

            // 1. Central vertical rotating soul light pillar (4 intersecting planes, height 2.4 blocks)
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.05f, 0.5f);

            float soulAngle = (float) (-(time / 55.0) % 360.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(soulAngle));

            float pillarRadius = 0.55f;
            float pillarHeight = 2.4f;

            for (int rAngle = 0; rAngle < 180; rAngle += 45) {
                float rad = (float) Math.toRadians(rAngle);
                float dx = (float) Math.cos(rad) * pillarRadius;
                float dz = (float) Math.sin(rad) * pillarRadius;
                addVerticalQuad(builder, poseStack.last(), -dx, -dz, dx, dz, 0.0f, pillarHeight, pillarR, pillarG, pillarB, pillarAlpha, LightTexture.FULL_BRIGHT);
            }
            poseStack.popPose();

            // 2. Central Soul Core Flare at Y = 0.60
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.60f + (float) Math.sin(time / 350.0) * 0.035f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((time / 22.0) % 360.0)));
            float coreRadius = 0.20f + (float) Math.sin(time / 220.0) * 0.03f;
            if (isBound) {
                addCoreFlare(builder, poseStack.last(), coreRadius, 180, 255, 250, (int) (235 * pulse));
            } else {
                addCoreFlare(builder, poseStack.last(), coreRadius, 70, 200, 225, (int) (160 * pulse));
            }
            poseStack.popPose();

            // 3. Floating Ethereal Soul Ring at Y = 1.05
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.05f + (float) Math.sin(time / 400.0) * 0.03f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees((float) ((time / 30.0) % 360.0)));
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.82f, 0.032f, 32, 60, 235, 250, isBound ? 220 : 140, LightTexture.FULL_BRIGHT);
            addCircleRing(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.72f, 0.018f, 32, 60, 235, 250, isBound ? 150 : 90, LightTexture.FULL_BRIGHT);
            addRingTicks(builder, poseStack.last(), 0.0f, 0.0f, 0.0f, 0.82f, 0.045f, 0.02f, 8, 60, 235, 250, isBound ? 210 : 130, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        // Render 3D miniature spinning projection if the circle holds a captured mob
        if (be.hasCapturedMob()) {
            Entity renderEntity = be.getOrCreateRenderEntity(be.getLevel());
            if (renderEntity != null) {
                poseStack.pushPose();
                float centerX = 0.5f;
                float centerZ = 0.5f;

                float rotationAngle = (float) ((time / 50.0) % 360.0);
                float bobbing = (float) Math.sin(time / 600.0) * 0.04f;

                poseStack.translate(centerX, 0.45f + bobbing, centerZ);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

                if (renderEntity instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.tickCount = 0;
                    living.yBodyRot = 180.0f;
                    living.yBodyRotO = 180.0f;
                    living.yHeadRot = 180.0f;
                    living.yHeadRotO = 180.0f;
                    living.setYRot(180.0f);
                    living.yRotO = 180.0f;
                    living.setXRot(0.0f);
                    living.xRotO = 0.0f;
                    living.hurtTime = 0;
                    living.deathTime = 0;
                    living.attackAnim = 0.0f;
                }

                float maxDim = Math.max(renderEntity.getBbWidth(), renderEntity.getBbHeight());
                float scale = 0.55f;
                if (maxDim > 1.0f) {
                    scale = 0.55f / maxDim;
                }
                poseStack.scale(scale, scale, scale);

                try {
                    Minecraft.getInstance().getEntityRenderDispatcher().render(
                            renderEntity, 0.0, 0.0, 0.0, 0.0f, 1.0f, poseStack, buffer, LightTexture.FULL_BRIGHT
                    );
                } catch (Throwable ignored) {}

                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private static int[] getLayerColor(ResourceLocation typeId) {
        if (typeId == null) {
            return new int[]{140, 225, 255, 235}; // Starlight Cyan
        }
        String path = typeId.getPath();
        return switch (path) {
            case "dimensional_gate" -> new int[]{195, 110, 255, 240}; // Astral Violet
            case "soul_anchor" -> new int[]{45, 235, 245, 240};       // Ethereal Soul Cyan
            case "sanctuary" -> new int[]{90, 255, 190, 240};         // Radiant Celestite
            case "barrier" -> new int[]{100, 200, 255, 240};          // Sapphire Diamond
            case "overgrowth" -> new int[]{110, 255, 110, 240};       // Verdant Emerald
            case "crystallization" -> new int[]{225, 130, 255, 240};  // Amethyst Shimmer
            case "summoning", "capture" -> new int[]{255, 170, 60, 240}; // Solar Gold
            case "teleportation", "teleport" -> new int[]{80, 220, 255, 240}; // Astral Blue
            default -> new int[]{140, 225, 255, 235};
        };
    }

    private void addCircleRing(VertexConsumer builder, PoseStack.Pose pose, float centerX, float centerZ, float y, float radius, float thickness, int segments, int r, int g, int b, int a, int light) {
        float rInner = radius - thickness * 0.5f;
        float rOuter = radius + thickness * 0.5f;
        float step = (float) (Math.PI * 2 / segments);

        for (int i = 0; i < segments; i++) {
            float a0 = i * step;
            float a1 = (i + 1) * step;

            float cos0 = (float) Math.cos(a0);
            float sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);

            float x0_in = centerX + cos0 * rInner;
            float z0_in = centerZ + sin0 * rInner;
            float x0_out = centerX + cos0 * rOuter;
            float z0_out = centerZ + sin0 * rOuter;

            float x1_in = centerX + cos1 * rInner;
            float z1_in = centerZ + sin1 * rInner;
            float x1_out = centerX + cos1 * rOuter;
            float z1_out = centerZ + sin1 * rOuter;

            builder.addVertex(pose, x0_in, y, z0_in).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            builder.addVertex(pose, x0_out, y, z0_out).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            builder.addVertex(pose, x1_out, y, z1_out).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
            builder.addVertex(pose, x1_in, y, z1_in).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        }
    }

    private void addVerticalQuad(VertexConsumer builder, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float y0, float y1, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0, y1, z0).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y0, z1).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);

        // Reverse side
        builder.addVertex(pose, x1, y0, z1).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0, y1, z0).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void addQuad(VertexConsumer builder, PoseStack.Pose pose, float x0, float x1, float z0, float z1, float y, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x0, y, z0).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0, y, z1).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y, z1).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y, z0).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void addQuadUV(VertexConsumer builder, PoseStack.Pose pose, float x0, float x1, float z0, float z1, float y, int r, int g, int b, int a, int light, float u0, float u1, float v0, float v1) {
        builder.addVertex(pose, x0, y, z0).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0, y, z1).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1, y, z0).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void addSideNorth(VertexConsumer builder, PoseStack.Pose pose, float x0, float x1, float z, float y0, float y1, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x0, y1, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        builder.addVertex(pose, x1, y1, z).setColor(r, g, b, a).setUv(3.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        builder.addVertex(pose, x1, y0, z).setColor(r, g, b, a).setUv(3.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
        builder.addVertex(pose, x0, y0, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, -1.0f);
    }

    private void addSideSouth(VertexConsumer builder, PoseStack.Pose pose, float x0, float x1, float z, float y0, float y1, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x1, y1, z).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        builder.addVertex(pose, x0, y1, z).setColor(r, g, b, a).setUv(3.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        builder.addVertex(pose, x0, y0, z).setColor(r, g, b, a).setUv(3.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
        builder.addVertex(pose, x1, y0, z).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }

    private void addSideWest(VertexConsumer builder, PoseStack.Pose pose, float x, float z0, float z1, float y0, float y1, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x, y1, z1).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y1, z0).setColor(r, g, b, a).setUv(3.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y0, z0).setColor(r, g, b, a).setUv(3.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y0, z1).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0f, 0.0f, 0.0f);
    }

    private void addSideEast(VertexConsumer builder, PoseStack.Pose pose, float x, float z0, float z1, float y0, float y1, int r, int g, int b, int a, int light) {
        builder.addVertex(pose, x, y1, z0).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y1, z1).setColor(r, g, b, a).setUv(3.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y0, z1).setColor(r, g, b, a).setUv(3.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0f, 0.0f, 0.0f);
        builder.addVertex(pose, x, y0, z0).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0f, 0.0f, 0.0f);
    }

    private void addThinRibbon(VertexConsumer builder, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float y, float thickness, int r, int g, int b, int a, int light) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001f) return;
        float nx = -dz / len * (thickness * 0.5f);
        float nz = dx / len * (thickness * 0.5f);

        builder.addVertex(pose, x0 - nx, y, z0 - nz).setColor(r, g, b, a).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x0 + nx, y, z0 + nz).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1 + nx, y, z1 + nz).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
        builder.addVertex(pose, x1 - nx, y, z1 - nz).setColor(r, g, b, a).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private void addHexagram(VertexConsumer builder, PoseStack.Pose pose, float y, float radius, float thickness, int r, int g, int b, int a, int light) {
        for (int i = 0; i < 3; i++) {
            float a0 = (float) (i * 2.0 * Math.PI / 3.0);
            float a1 = (float) ((i + 1) * 2.0 * Math.PI / 3.0);
            addThinRibbon(builder, pose,
                    (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius,
                    y, thickness, r, g, b, a, light);
        }
        float offset = (float) (Math.PI / 3.0);
        for (int i = 0; i < 3; i++) {
            float a0 = (float) (i * 2.0 * Math.PI / 3.0 + offset);
            float a1 = (float) ((i + 1) * 2.0 * Math.PI / 3.0 + offset);
            addThinRibbon(builder, pose,
                    (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius,
                    y, thickness, r, g, b, a, light);
        }
    }

    private void addOctagram(VertexConsumer builder, PoseStack.Pose pose, float y, float radius, float thickness, int r, int g, int b, int a, int light) {
        for (int i = 0; i < 4; i++) {
            float a0 = (float) (i * Math.PI * 0.5);
            float a1 = (float) ((i + 1) * Math.PI * 0.5);
            addThinRibbon(builder, pose,
                    (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius,
                    y, thickness, r, g, b, a, light);
        }
        float offset = (float) (Math.PI * 0.25);
        for (int i = 0; i < 4; i++) {
            float a0 = (float) (i * Math.PI * 0.5 + offset);
            float a1 = (float) ((i + 1) * Math.PI * 0.5 + offset);
            addThinRibbon(builder, pose,
                    (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius,
                    (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius,
                    y, thickness, r, g, b, a, light);
        }
    }

    private void addRingTicks(VertexConsumer builder, PoseStack.Pose pose, float centerX, float centerZ, float y, float radius, float tickLength, float thickness, int count, int r, int g, int b, int a, int light) {
        float step = (float) (Math.PI * 2.0 / count);
        for (int i = 0; i < count; i++) {
            float ang = i * step;
            float cos = (float) Math.cos(ang);
            float sin = (float) Math.sin(ang);
            float x0 = centerX + cos * radius;
            float z0 = centerZ + sin * radius;
            float x1 = centerX + cos * (radius + tickLength);
            float z1 = centerZ + sin * (radius + tickLength);
            addThinRibbon(builder, pose, x0, z0, x1, z1, y, thickness, r, g, b, a, light);
        }
    }

    private void addCoreFlare(VertexConsumer builder, PoseStack.Pose pose, float radius, int r, int g, int b, int a) {
        addVerticalQuad(builder, pose, -radius, 0, radius, 0, -radius, radius, r, g, b, a, LightTexture.FULL_BRIGHT);
        addVerticalQuad(builder, pose, 0, -radius, 0, radius, -radius, radius, r, g, b, a, LightTexture.FULL_BRIGHT);
        addQuad(builder, pose, -radius, radius, -radius, radius, 0, r, g, b, a, LightTexture.FULL_BRIGHT);
    }
}
