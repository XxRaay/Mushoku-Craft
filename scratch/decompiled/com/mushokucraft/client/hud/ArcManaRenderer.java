/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.BufferUploader
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.util.Mth
 *  org.joml.Matrix4f
 */
package com.mushokucraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mushokucraft.client.hud.ManaAnimationState;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class ArcManaRenderer {
    private static final float INNER_RADIUS = 16.0f;
    private static final float OUTER_RADIUS = 19.0f;
    private static final int SEGMENTS = 32;
    private static final float ARC_START = -1.0471976f;
    private static final float ARC_END = 1.0471976f;
    private static final int SAPPHIRE_R = 10;
    private static final int SAPPHIRE_G = 36;
    private static final int SAPPHIRE_B = 99;
    private static final int TEAL_R = 0;
    private static final int TEAL_G = 255;
    private static final int TEAL_B = 204;
    private static final int BG_R = 15;
    private static final int BG_G = 15;
    private static final int BG_B = 15;
    private static final int OUTLINE_R = 5;
    private static final int OUTLINE_G = 5;
    private static final int OUTLINE_B = 5;

    public static void render(GuiGraphics guiGraphics, ManaAnimationState state, float partialTick, float globalAlpha) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;
        if (globalAlpha <= 0.01f) {
            return;
        }
        long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
        float time = (float)gameTime + partialTick;
        float manaPercent = state.getManaPercent();
        float fizzle = state.getFizzleProgress();
        float charge = state.getChargeIntensity();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Matrix4f matrix = guiGraphics.pose().last().pose();
        if (state.isToukiActive() && state.getActiveSwordStyle() != null) {
            ArcManaRenderer.renderToukiGlow(matrix, centerX, centerY, state.getActiveSwordStyle(), time, globalAlpha);
        }
        ArcManaRenderer.renderOutlineArc(matrix, centerX, centerY, globalAlpha);
        ArcManaRenderer.renderMainArc(matrix, centerX, centerY, manaPercent, fizzle, charge, time, globalAlpha);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        if (((Boolean)MushokuConfig.SHOW_MANA_NUMBERS.get()).booleanValue()) {
            ArcManaRenderer.renderManaText(guiGraphics, mc, centerX, centerY, state, globalAlpha);
        }
    }

    private static void renderOutlineArc(Matrix4f matrix, int cx, int cy, float alpha) {
        float outR = 20.0f;
        float inR = 15.0f;
        int a = (int)(180.0f * alpha);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 32; ++i) {
            float segPercent = (float)i / 32.0f;
            float angle = Mth.lerp((float)segPercent, (float)-1.0471976f, (float)1.0471976f);
            float cosA = (float)Math.cos(angle);
            float sinA = (float)Math.sin(angle);
            float outerX = (float)cx + cosA * outR;
            float outerY = (float)cy - sinA * outR;
            float innerX = (float)cx + cosA * inR;
            float innerY = (float)cy - sinA * inR;
            buffer.addVertex(matrix, outerX, outerY, 0.0f).setColor(5, 5, 5, a);
            buffer.addVertex(matrix, innerX, innerY, 0.0f).setColor(5, 5, 5, a);
        }
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void renderMainArc(Matrix4f matrix, int cx, int cy, float manaPercent, float fizzle, float charge, float time, float alpha) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 32; ++i) {
            int a;
            int b;
            int g;
            int r;
            boolean filled;
            float segPercent = (float)i / 32.0f;
            float angle = Mth.lerp((float)segPercent, (float)-1.0471976f, (float)1.0471976f);
            float cosA = (float)Math.cos(angle);
            float sinA = (float)Math.sin(angle);
            float outerX = (float)cx + cosA * 19.0f;
            float outerY = (float)cy - sinA * 19.0f;
            float innerX = (float)cx + cosA * 16.0f;
            float innerY = (float)cy - sinA * 16.0f;
            boolean bl = filled = segPercent <= manaPercent;
            if (fizzle > 0.0f) {
                if (filled) {
                    r = (int)Mth.lerp((float)fizzle, (float)0.0f, (float)255.0f);
                    g = (int)Mth.lerp((float)fizzle, (float)100.0f, (float)50.0f);
                    b = (int)Mth.lerp((float)fizzle, (float)150.0f, (float)50.0f);
                } else {
                    r = (int)(60.0f * fizzle);
                    g = 20;
                    b = 20;
                }
                a = (int)(200.0f * alpha);
            } else if (filled) {
                float gradT = manaPercent > 0.0f ? segPercent / manaPercent : 0.0f;
                r = (int)Mth.lerp((float)gradT, (float)10.0f, (float)0.0f);
                g = (int)Mth.lerp((float)gradT, (float)36.0f, (float)255.0f);
                b = (int)Mth.lerp((float)gradT, (float)99.0f, (float)204.0f);
                a = (int)(200.0f * alpha);
                if (charge > 0.0f) {
                    float pulse = (float)Math.sin((double)time * (0.3 + (double)charge * 0.5)) * 0.5f + 0.5f;
                    float brighten = charge * pulse * 0.4f;
                    r = Math.min(255, r + (int)(brighten * (float)(255 - r)));
                    g = Math.min(255, g + (int)(brighten * (float)(255 - g)));
                    b = Math.min(255, b + (int)(brighten * (float)(255 - b)));
                }
            } else {
                r = 15;
                g = 15;
                b = 15;
                a = (int)(120.0f * alpha);
            }
            buffer.addVertex(matrix, outerX, outerY, 0.0f).setColor(r, g, b, a);
            buffer.addVertex(matrix, innerX, innerY, 0.0f).setColor(r, g, b, a);
        }
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void renderToukiGlow(Matrix4f matrix, int cx, int cy, SwordStyle style, float time, float alpha) {
        int[] color = ArcManaRenderer.getStyleColor(style);
        float pulse = 0.7f + 0.3f * (float)Math.sin((double)time * 0.2);
        int glowAlpha = (int)(35.0f * pulse * alpha);
        float glowInner = 19.0f;
        float glowOuter = 22.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 32; ++i) {
            float segPercent = (float)i / 32.0f;
            float angle = Mth.lerp((float)segPercent, (float)-1.0471976f, (float)1.0471976f);
            float cosA = (float)Math.cos(angle);
            float sinA = (float)Math.sin(angle);
            float outerX = (float)cx + cosA * glowOuter;
            float outerY = (float)cy - sinA * glowOuter;
            float innerX = (float)cx + cosA * glowInner;
            float innerY = (float)cy - sinA * glowInner;
            buffer.addVertex(matrix, outerX, outerY, 0.0f).setColor(color[0], color[1], color[2], 0);
            buffer.addVertex(matrix, innerX, innerY, 0.0f).setColor(color[0], color[1], color[2], glowAlpha);
        }
        BufferUploader.drawWithShader((MeshData)buffer.buildOrThrow());
    }

    private static void renderManaText(GuiGraphics g, Minecraft mc, int cx, int cy, ManaAnimationState state, float alpha) {
        String text = (int)state.getCurrentMana() + " / " + (int)state.getMaxMana();
        float scale = 0.65f;
        float invScale = 1.0f / scale;
        float textX = (float)cx + 19.0f + 4.0f;
        float textY = (float)cy - 4.0f * scale;
        int a = (int)(220.0f * alpha);
        a = Mth.clamp((int)a, (int)0, (int)255);
        int textColor = a << 24 | 0xF00000 | 0xF800 | 0xFF;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0f);
        g.drawString(mc.font, text, (int)(textX * invScale), (int)(textY * invScale), textColor, true);
        g.pose().popPose();
    }

    private static int[] getStyleColor(SwordStyle style) {
        int[] nArray;
        switch (style) {
            default: {
                throw new MatchException(null, null);
            }
            case SWORD_GOD: {
                int[] nArray2 = new int[3];
                nArray2[0] = 255;
                nArray2[1] = 215;
                nArray = nArray2;
                nArray2[2] = 0;
                break;
            }
            case WATER_GOD: {
                int[] nArray3 = new int[3];
                nArray3[0] = 0;
                nArray3[1] = 191;
                nArray = nArray3;
                nArray3[2] = 255;
                break;
            }
            case NORTH_GOD: {
                int[] nArray4 = new int[3];
                nArray4[0] = 170;
                nArray4[1] = 68;
                nArray = nArray4;
                nArray4[2] = 255;
            }
        }
        return nArray;
    }
}

