/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.util.Mth
 */
package com.mushokucraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.hud.ManaAnimationState;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class ClassicManaBarRenderer {
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 9;
    private static final int FILL_WIDTH = 79;
    private static final int FILL_HEIGHT = 7;
    private static final int BORDER = 1;
    private static final int SAPPHIRE_R = 10;
    private static final int SAPPHIRE_G = 36;
    private static final int SAPPHIRE_B = 99;
    private static final int TEAL_R = 0;
    private static final int TEAL_G = 255;
    private static final int TEAL_B = 204;
    private static final int FRAME_LIGHT = ClassicManaBarRenderer.argb(255, 80, 80, 80);
    private static final int FRAME_SHADOW = ClassicManaBarRenderer.argb(255, 30, 30, 30);
    private static final int FRAME_BG = ClassicManaBarRenderer.argb(255, 15, 15, 15);

    public static void render(GuiGraphics guiGraphics, ManaAnimationState state, float partialTick, float globalAlpha) {
        float charge;
        float fizzle;
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int barX = (screenWidth - 81) / 2;
        int barY = screenHeight - 54 - 9;
        float configAlpha = ((Double)MushokuConfig.MANA_BAR_OPACITY.get()).floatValue();
        float alpha = configAlpha * globalAlpha;
        if (alpha <= 0.01f) {
            return;
        }
        long gameTime = mc.level != null ? mc.level.getGameTime() : 0L;
        float time = (float)gameTime + partialTick;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ClassicManaBarRenderer.renderFrame(guiGraphics, barX, barY, alpha);
        float manaPercent = state.getManaPercent();
        int fillPixels = (int)(79.0f * manaPercent);
        if (fillPixels > 0) {
            ClassicManaBarRenderer.renderGradientFill(guiGraphics, barX + 1, barY + 1, fillPixels, 7, alpha);
            ClassicManaBarRenderer.renderShimmer(guiGraphics, barX + 1, barY + 1, fillPixels, 7, time, alpha);
        }
        if ((fizzle = state.getFizzleProgress()) > 0.0f) {
            ClassicManaBarRenderer.renderFizzle(guiGraphics, barX + 1, barY + 1, 79, 7, fizzle, alpha, gameTime);
        }
        if ((charge = state.getChargeIntensity()) > 0.0f && fillPixels > 0) {
            ClassicManaBarRenderer.renderChargeSparks(guiGraphics, barX + 1, barY + 1, fillPixels, 7, charge, time, alpha);
        }
        if (state.isToukiActive() && state.getActiveSwordStyle() != null) {
            ClassicManaBarRenderer.renderToukiAura(guiGraphics, barX, barY, 81, 9, state.getActiveSwordStyle(), time, alpha);
        }
        if (((Boolean)MushokuConfig.SHOW_MANA_NUMBERS.get()).booleanValue()) {
            ClassicManaBarRenderer.renderManaText(guiGraphics, mc, barX, barY, state, alpha);
        }
        RenderSystem.disableBlend();
    }

    private static void renderFrame(GuiGraphics g, int x, int y, float alpha) {
        int light = ClassicManaBarRenderer.applyAlpha(FRAME_LIGHT, alpha);
        int shadow = ClassicManaBarRenderer.applyAlpha(FRAME_SHADOW, alpha);
        int bg = ClassicManaBarRenderer.applyAlpha(FRAME_BG, alpha);
        g.fill(x - 1, y - 1, x + 81, y, light);
        g.fill(x - 1, y, x, y + 9, light);
        g.fill(x - 1, y + 9, x + 81 + 1, y + 9 + 1, shadow);
        g.fill(x + 81, y - 1, x + 81 + 1, y + 9, shadow);
        g.fill(x, y, x + 81, y + 9, bg);
    }

    private static void renderGradientFill(GuiGraphics g, int x, int y, int width, int height, float alpha) {
        for (int row = 0; row < height; ++row) {
            float t = (float)row / (float)(height - 1);
            float inv = 1.0f - t;
            int r = (int)Mth.lerp((float)inv, (float)10.0f, (float)0.0f);
            int green = (int)Mth.lerp((float)inv, (float)36.0f, (float)255.0f);
            int b = (int)Mth.lerp((float)inv, (float)99.0f, (float)204.0f);
            int color = ClassicManaBarRenderer.argb((int)(220.0f * alpha), r, green, b);
            g.fill(x, y + row, x + width, y + row + 1, color);
        }
    }

    private static void renderShimmer(GuiGraphics g, int x, int y, int fillWidth, int height, float time, float alpha) {
        float shimmerPos = ((float)Math.sin((double)time * 0.15) * 0.5f + 0.5f) * (float)fillWidth;
        int bandX = x + (int)shimmerPos;
        int bandWidth = 3;
        int bx1 = Math.max(bandX - bandWidth / 2, x);
        int bx2 = Math.min(bandX + bandWidth / 2 + 1, x + fillWidth);
        if (bx2 > bx1) {
            int shimmerAlpha = (int)(40.0f * alpha);
            g.fill(bx1, y, bx2, y + height, ClassicManaBarRenderer.argb(shimmerAlpha, 255, 255, 255));
        }
        float shimmerPos2 = ((float)Math.sin((double)time * 0.08 + 2.0) * 0.5f + 0.5f) * (float)fillWidth;
        int band2X = x + (int)shimmerPos2;
        bx1 = Math.max(band2X - 1, x);
        bx2 = Math.min(band2X + 2, x + fillWidth);
        if (bx2 > bx1) {
            int shimmerAlpha2 = (int)(25.0f * alpha);
            g.fill(bx1, y, bx2, y + height, ClassicManaBarRenderer.argb(shimmerAlpha2, 200, 240, 255));
        }
    }

    private static void renderFizzle(GuiGraphics g, int x, int y, int width, int height, float progress, float alpha, long gameTime) {
        int redAlpha = (int)(progress * 180.0f * alpha);
        g.fill(x, y, x + width, y + height, ClassicManaBarRenderer.argb(redAlpha, 255, 50, 50));
        if (progress > 0.5f) {
            int crackAlpha = (int)((progress - 0.5f) * 2.0f * 200.0f * alpha);
            int crackColor = ClassicManaBarRenderer.argb(crackAlpha, 30, 10, 10);
            long seed = gameTime / 20L;
            int crack1 = x + (int)((seed * 7L + 3L) % (long)width);
            int crack2 = x + (int)((seed * 13L + 11L) % (long)width);
            int crack3 = x + (int)((seed * 23L + 7L) % (long)width);
            g.fill(crack1, y, crack1 + 1, y + height, crackColor);
            g.fill(crack2, y + 1, crack2 + 1, y + height - 1, crackColor);
            g.fill(crack3, y, crack3 + 1, y + height, crackColor);
            g.fill(crack1, y + height / 2, crack1 + 2, y + height / 2 + 1, crackColor);
        }
        if (progress < 0.4f) {
            int grayAlpha = (int)((0.4f - progress) * 2.5f * 60.0f * alpha);
            g.fill(x, y, x + width, y + height, ClassicManaBarRenderer.argb(grayAlpha, 80, 80, 80));
        }
    }

    private static void renderChargeSparks(GuiGraphics g, int x, int y, int fillWidth, int height, float intensity, float time, float alpha) {
        int glowAlpha = (int)(20.0f * intensity * alpha);
        g.fill(x, y, x + fillWidth, y + height, ClassicManaBarRenderer.argb(glowAlpha, 255, 255, 255));
        int sparkCount = 3 + (int)(intensity * 3.0f);
        int sparkAlpha = (int)(200.0f * intensity * alpha);
        for (int i = 0; i < sparkCount; ++i) {
            int sx = x + (int)((time * 3.0f + (float)(i * 17)) % (float)fillWidth);
            float sy = (float)(y + 1) + (float)Math.sin((double)time * 0.5 + (double)i * 1.7) * (float)(height - 3);
            int sparkY = (int)Mth.clamp((float)sy, (float)y, (float)(y + height - 2));
            g.fill(sx, sparkY, sx + 2, sparkY + 2, ClassicManaBarRenderer.argb(sparkAlpha, 255, 255, 255));
            g.fill(sx - 1, sparkY - 1, sx + 3, sparkY + 3, ClassicManaBarRenderer.argb(sparkAlpha / 4, 200, 230, 255));
        }
        float pulse = (float)Math.sin((double)time * 0.4) * 0.5f + 0.5f;
        int pulseAlpha = (int)(15.0f * intensity * pulse * alpha);
        g.fill(x, y, x + fillWidth, y + height, ClassicManaBarRenderer.argb(pulseAlpha, 255, 255, 255));
    }

    private static void renderToukiAura(GuiGraphics g, int x, int y, int width, int height, SwordStyle style, float time, float alpha) {
        int a;
        float edgeAlpha;
        int i;
        int[] color = ClassicManaBarRenderer.getStyleColor(style);
        float pulse = 0.7f + 0.3f * (float)Math.sin((double)time * 0.2);
        int edgeWidth = 4;
        for (i = 0; i < edgeWidth; ++i) {
            edgeAlpha = (1.0f - (float)i / (float)edgeWidth) * pulse * alpha;
            a = (int)(120.0f * edgeAlpha);
            g.fill(x + i, y, x + i + 1, y + height, ClassicManaBarRenderer.argb(a, color[0], color[1], color[2]));
        }
        for (i = 0; i < edgeWidth; ++i) {
            edgeAlpha = (1.0f - (float)i / (float)edgeWidth) * pulse * alpha;
            a = (int)(120.0f * edgeAlpha);
            g.fill(x + width - 1 - i, y, x + width - i, y + height, ClassicManaBarRenderer.argb(a, color[0], color[1], color[2]));
        }
        int topAlpha = (int)(60.0f * pulse * alpha);
        g.fill(x, y - 1, x + width, y, ClassicManaBarRenderer.argb(topAlpha, color[0], color[1], color[2]));
        g.fill(x, y + height, x + width, y + height + 1, ClassicManaBarRenderer.argb(topAlpha, color[0], color[1], color[2]));
    }

    private static void renderManaText(GuiGraphics g, Minecraft mc, int barX, int barY, ManaAnimationState state, float alpha) {
        String text = (int)state.getCurrentMana() + " / " + (int)state.getMaxMana();
        int textWidth = mc.font.width(text);
        float scale = 0.75f;
        float invScale = 1.0f / scale;
        float centerX = (float)barX + 40.5f;
        float centerY = (float)barY + 4.5f;
        float textX = centerX - (float)textWidth * scale / 2.0f;
        float textY = centerY - 4.0f * scale;
        int textColor = ClassicManaBarRenderer.argb((int)(220.0f * alpha), 240, 248, 255);
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

    private static int argb(int a, int r, int g, int b) {
        a = Mth.clamp((int)a, (int)0, (int)255);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int applyAlpha(int argbColor, float alphaMultiplier) {
        int a = argbColor >> 24 & 0xFF;
        a = (int)((float)a * alphaMultiplier);
        return a << 24 | argbColor & 0xFFFFFF;
    }
}

