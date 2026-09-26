package com.mushokucraft.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class MagicCircleBlockEntityRenderer implements BlockEntityRenderer<MagicCircleBlockEntity> {
    private static final ResourceLocation BLANK_TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/block/magic_circle_base.png");

    public MagicCircleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MagicCircleBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int size = be.getSize();
        boolean sheared = be.isSheared();

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

        boolean isLinked = be.getLinkedPos() != null;
        float currentMana = be.getCurrentMana();
        float reqMana = Math.max(1.0f, be.getRequiredMana());
        float chargeRatio = Math.min(1.0f, currentMana / reqMana);

        // Color computation
        int r, g, b, a;
        int light = packedLight;

        if (chargeRatio > 0.0f) {
            // Glowing pulsing cyan/magenta when charged with mana
            float pulse = (float) Math.sin((System.currentTimeMillis() % 2000) / 2000.0 * Math.PI * 2) * 0.2f + 0.8f;
            r = (int) (140 * pulse);
            g = (int) (220 * pulse);
            b = (int) (255 * pulse);
            a = 255;
            light = LightTexture.FULL_BRIGHT;
        } else if (isLinked) {
            // Calm mystical cyan when linked
            r = 80;
            g = 180;
            b = 230;
            a = 230;
        } else {
            // Dark mystical ink when unlinked
            r = 35;
            g = 25;
            b = 50;
            a = 240;
        }

        // Draw 16x16 rune pattern quads
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

        // Render 3D miniature spinning projection if the circle holds a captured mob
        if (be.hasCapturedMob()) {
            Entity renderEntity = be.getOrCreateRenderEntity(be.getLevel());
            if (renderEntity != null) {
                poseStack.pushPose();
                float centerX = 0.5f;
                float centerZ = 0.5f;

                long time = System.currentTimeMillis();
                // Smooth monotonic rotation (no back-and-forth twitching from client gameTime / partialTick resets)
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
}
