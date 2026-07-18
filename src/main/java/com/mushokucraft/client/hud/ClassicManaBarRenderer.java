package com.mushokucraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Renders the classic mana bar above the hotbar.
 * <p>
 * Features:
 * <ul>
 *   <li>Gradient liquid fill (sapphire → neon teal) with animated shimmer</li>
 *   <li>Fizzle flash (red/gray with crack lines)</li>
 *   <li>Silent Casting charge sparks (white pulsing dots)</li>
 *   <li>Touki aura glow (style-colored edge effects)</li>
 *   <li>Numeric mana display</li>
 * </ul>
 */
public class ClassicManaBarRenderer {

    // ── Dimensions ───────────────────────────────────────────────
    private static final int BAR_WIDTH = 81; // Matches the width of 10 vanilla hearts exactly
    private static final int BAR_HEIGHT = 9;
    private static final int FILL_WIDTH = 79;
    private static final int FILL_HEIGHT = 7;
    private static final int BORDER = 1;

    // ── Colors ───────────────────────────────────────────────────
    // Gradient endpoints
    private static final int SAPPHIRE_R = 10, SAPPHIRE_G = 36, SAPPHIRE_B = 99;
    private static final int TEAL_R = 0, TEAL_G = 255, TEAL_B = 204;

    // Frame (stone metal)
    private static final int FRAME_LIGHT = argb(255, 80, 80, 80);
    private static final int FRAME_SHADOW = argb(255, 30, 30, 30);
    private static final int FRAME_BG = argb(255, 15, 15, 15);

    // ── Main Render ──────────────────────────────────────────────

    /**
     * Render the classic mana bar above the hotbar.
     *
     * @param guiGraphics the GUI graphics context
     * @param state       current animation state
     * @param partialTick fractional tick for smooth animation
     * @param globalAlpha overall opacity multiplier (for MINIMAL fade)
     */
    public static void render(GuiGraphics guiGraphics, ManaAnimationState state,
                              float partialTick, float globalAlpha) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: centered horizontally, above health/armor bars
        // Vanilla health/hunger is at ~39px, armor at ~49px
        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = screenHeight - HudConstants.MANA_BAR_Y_OFFSET_FROM_BOTTOM - BAR_HEIGHT;

        float configAlpha = MushokuConfig.MANA_BAR_OPACITY.get().floatValue();
        float alpha = configAlpha * globalAlpha;
        if (alpha <= 0.01f) return;

        long gameTime = mc.level != null ? mc.level.getGameTime() : 0;
        float time = gameTime + partialTick;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Frame / background
        renderFrame(guiGraphics, barX, barY, alpha);

        // 2. Mana fill with gradient
        float manaPercent = state.getManaPercent();
        int fillPixels = (int) (FILL_WIDTH * manaPercent);
        if (fillPixels > 0) {
            renderGradientFill(guiGraphics, barX + BORDER, barY + BORDER,
                    fillPixels, FILL_HEIGHT, alpha);

            // 3. Shimmer (liquid animation)
            renderShimmer(guiGraphics, barX + BORDER, barY + BORDER,
                    fillPixels, FILL_HEIGHT, time, alpha);
        }

        // 4. Fizzle flash
        float fizzle = state.getFizzleProgress();
        if (fizzle > 0) {
            renderFizzle(guiGraphics, barX + BORDER, barY + BORDER,
                    FILL_WIDTH, FILL_HEIGHT, fizzle, alpha, gameTime);
        }

        // 5. Charge sparks (Silent Casting)
        float charge = state.getChargeIntensity();
        if (charge > 0 && fillPixels > 0) {
            renderChargeSparks(guiGraphics, barX + BORDER, barY + BORDER,
                    fillPixels, FILL_HEIGHT, charge, time, alpha);
        }

        // 6. Touki aura edges
        if (state.isToukiActive() && state.getActiveSwordStyle() != null) {
            renderToukiAura(guiGraphics, barX, barY, BAR_WIDTH, BAR_HEIGHT,
                    state.getActiveSwordStyle(), time, alpha);
        }

        // 7. Mana numbers
        if (MushokuConfig.SHOW_MANA_NUMBERS.get()) {
            renderManaText(guiGraphics, mc, barX, barY, state, alpha);
        }

        RenderSystem.disableBlend();
    }

    // ── Frame ────────────────────────────────────────────────────

    private static void renderFrame(GuiGraphics g, int x, int y, float alpha) {
        int light = applyAlpha(FRAME_LIGHT, alpha);
        int shadow = applyAlpha(FRAME_SHADOW, alpha);
        int bg = applyAlpha(FRAME_BG, alpha);

        // Top and Left borders (light)
        g.fill(x - 1, y - 1, x + BAR_WIDTH, y, light); // Top
        g.fill(x - 1, y, x, y + BAR_HEIGHT, light);    // Left
        
        // Bottom and Right borders (shadow)
        g.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, shadow); // Bottom
        g.fill(x + BAR_WIDTH, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT, shadow);      // Right

        // Inner background
        g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, bg);
    }

    // ── Gradient Fill ────────────────────────────────────────────

    private static void renderGradientFill(GuiGraphics g, int x, int y,
                                           int width, int height, float alpha) {
        for (int row = 0; row < height; row++) {
            float t = (float) row / (height - 1); // 0 = top (teal), 1 = bottom (sapphire)
            // Invert: bottom = sapphire, top = teal
            float inv = 1.0f - t;
            int r = (int) Mth.lerp(inv, SAPPHIRE_R, TEAL_R);
            int green = (int) Mth.lerp(inv, SAPPHIRE_G, TEAL_G);
            int b = (int) Mth.lerp(inv, SAPPHIRE_B, TEAL_B);
            int color = argb((int) (220 * alpha), r, green, b);
            g.fill(x, y + row, x + width, y + row + 1, color);
        }
    }

    // ── Shimmer (Liquid Animation) ───────────────────────────────

    private static void renderShimmer(GuiGraphics g, int x, int y,
                                      int fillWidth, int height,
                                      float time, float alpha) {
        // Moving highlight band
        float shimmerPos = ((float) Math.sin(time * 0.15) * 0.5f + 0.5f) * fillWidth;
        int bandX = x + (int) shimmerPos;
        int bandWidth = 3;

        // Clamp to fill area
        int bx1 = Math.max(bandX - bandWidth / 2, x);
        int bx2 = Math.min(bandX + bandWidth / 2 + 1, x + fillWidth);

        if (bx2 > bx1) {
            int shimmerAlpha = (int) (40 * alpha);
            g.fill(bx1, y, bx2, y + height, argb(shimmerAlpha, 255, 255, 255));
        }

        // Secondary slower shimmer for depth
        float shimmerPos2 = ((float) Math.sin(time * 0.08 + 2.0) * 0.5f + 0.5f) * fillWidth;
        int band2X = x + (int) shimmerPos2;
        bx1 = Math.max(band2X - 1, x);
        bx2 = Math.min(band2X + 2, x + fillWidth);
        if (bx2 > bx1) {
            int shimmerAlpha2 = (int) (25 * alpha);
            g.fill(bx1, y, bx2, y + height, argb(shimmerAlpha2, 200, 240, 255));
        }
    }

    // ── Fizzle Effect ────────────────────────────────────────────

    private static void renderFizzle(GuiGraphics g, int x, int y,
                                     int width, int height,
                                     float progress, float alpha, long gameTime) {
        // Red flash overlay
        int redAlpha = (int) (progress * 180 * alpha);
        g.fill(x, y, x + width, y + height, argb(redAlpha, 255, 50, 50));

        // Crack lines in the first half of the effect
        if (progress > 0.5f) {
            int crackAlpha = (int) ((progress - 0.5f) * 2.0f * 200 * alpha);
            int crackColor = argb(crackAlpha, 30, 10, 10);

            // Fixed pseudo-random crack positions based on gameTime seed
            long seed = gameTime / 20; // Changes every fizzle event
            int crack1 = x + (int) ((seed * 7 + 3) % width);
            int crack2 = x + (int) ((seed * 13 + 11) % width);
            int crack3 = x + (int) ((seed * 23 + 7) % width);

            // Vertical crack lines
            g.fill(crack1, y, crack1 + 1, y + height, crackColor);
            g.fill(crack2, y + 1, crack2 + 1, y + height - 1, crackColor);
            g.fill(crack3, y, crack3 + 1, y + height, crackColor);

            // Diagonal crack fragment
            g.fill(crack1, y + height / 2, crack1 + 2, y + height / 2 + 1, crackColor);
        }

        // Gray desaturation as fizzle fades
        if (progress < 0.4f) {
            int grayAlpha = (int) ((0.4f - progress) * 2.5f * 60 * alpha);
            g.fill(x, y, x + width, y + height, argb(grayAlpha, 80, 80, 80));
        }
    }

    // ── Charge Sparks (Silent Casting) ───────────────────────────

    private static void renderChargeSparks(GuiGraphics g, int x, int y,
                                           int fillWidth, int height,
                                           float intensity, float time, float alpha) {
        // White glow overlay
        int glowAlpha = (int) (20 * intensity * alpha);
        g.fill(x, y, x + fillWidth, y + height, argb(glowAlpha, 255, 255, 255));

        // Sparks (small bright dots)
        int sparkCount = 3 + (int) (intensity * 3); // 3–6 sparks
        int sparkAlpha = (int) (200 * intensity * alpha);

        for (int i = 0; i < sparkCount; i++) {
            int sx = x + (int) ((time * 3 + i * 17) % fillWidth);
            float sy = y + 1 + (float) Math.sin(time * 0.5 + i * 1.7) * (height - 3);
            int sparkY = (int) Mth.clamp(sy, y, y + height - 2);

            // Bright white core
            g.fill(sx, sparkY, sx + 2, sparkY + 2, argb(sparkAlpha, 255, 255, 255));
            // Soft glow around spark
            g.fill(sx - 1, sparkY - 1, sx + 3, sparkY + 3,
                    argb(sparkAlpha / 4, 200, 230, 255));
        }

        // Pulsing edge brightness
        float pulse = (float) Math.sin(time * 0.4) * 0.5f + 0.5f;
        int pulseAlpha = (int) (15 * intensity * pulse * alpha);
        g.fill(x, y, x + fillWidth, y + height, argb(pulseAlpha, 255, 255, 255));
    }

    // ── Touki Aura Effect ────────────────────────────────────────

    private static void renderToukiAura(GuiGraphics g, int x, int y,
                                        int width, int height,
                                        SwordStyle style, float time, float alpha) {
        int[] color = getStyleColor(style);
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * 0.2);
        int edgeWidth = 4;

        // Left edge gradient (style color fading inward)
        for (int i = 0; i < edgeWidth; i++) {
            float edgeAlpha = (1.0f - (float) i / edgeWidth) * pulse * alpha;
            int a = (int) (120 * edgeAlpha);
            g.fill(x + i, y, x + i + 1, y + height, argb(a, color[0], color[1], color[2]));
        }

        // Right edge gradient (mirrored)
        for (int i = 0; i < edgeWidth; i++) {
            float edgeAlpha = (1.0f - (float) i / edgeWidth) * pulse * alpha;
            int a = (int) (120 * edgeAlpha);
            g.fill(x + width - 1 - i, y, x + width - i, y + height,
                    argb(a, color[0], color[1], color[2]));
        }

        // Top and bottom thin glow
        int topAlpha = (int) (60 * pulse * alpha);
        g.fill(x, y - 1, x + width, y, argb(topAlpha, color[0], color[1], color[2]));
        g.fill(x, y + height, x + width, y + height + 1,
                argb(topAlpha, color[0], color[1], color[2]));
    }

    // ── Mana Text ────────────────────────────────────────────────

    private static void renderManaText(GuiGraphics g, Minecraft mc,
                                       int barX, int barY,
                                       ManaAnimationState state, float alpha) {
        String text = (int) state.getCurrentMana() + " / " + (int) state.getMaxMana();
        int textWidth = mc.font.width(text);
        
        float scale = 0.75f;
        float invScale = 1.0f / scale;
        
        // Target center position
        float centerX = barX + BAR_WIDTH / 2.0f;
        float centerY = barY + BAR_HEIGHT / 2.0f;
        
        // Calculate top-left for text rendering (font height is ~8, so 4 is half)
        float textX = centerX - (textWidth * scale) / 2.0f;
        float textY = centerY - (4.0f * scale);
        
        int textColor = argb((int) (220 * alpha), 240, 248, 255); // Alice Blue

        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0f);
        
        // Draw string with drop shadow. Multiply coordinates by invScale because we scaled the matrix
        g.drawString(mc.font, text, (int)(textX * invScale), (int)(textY * invScale), textColor, true);
        
        g.pose().popPose();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static int[] getStyleColor(SwordStyle style) {
        return switch (style) {
            case SWORD_GOD -> new int[]{255, 215, 0};    // Gold
            case WATER_GOD -> new int[]{0, 191, 255};    // Deep sky blue
            case NORTH_GOD -> new int[]{170, 68, 255};   // Purple
        };
    }

    private static int argb(int a, int r, int g, int b) {
        a = Mth.clamp(a, 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int applyAlpha(int argbColor, float alphaMultiplier) {
        int a = (argbColor >> 24) & 0xFF;
        a = (int) (a * alphaMultiplier);
        return (a << 24) | (argbColor & 0x00FFFFFF);
    }
}





