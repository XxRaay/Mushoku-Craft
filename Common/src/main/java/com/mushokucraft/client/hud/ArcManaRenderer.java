package com.mushokucraft.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Renders a thin semicircular mana arc to the right of the crosshair.
 * <p>
 * The arc is a 120° sweep on the right side of the screen center.
 * It fades out when mana is full and no effects are active.
 * <p>
 * Effects:
 * <ul>
 *   <li>Fill gradient: sapphire (empty end) → teal (full end)</li>
 *   <li>Touki outer glow in sword style color</li>
 *   <li>Charge brightness pulse</li>
 *   <li>Fizzle red flash</li>
 * </ul>
 */
public class ArcManaRenderer {

    // ── Arc geometry ─────────────────────────────────────────────
    private static final float INNER_RADIUS = 16.0f;
    private static final float OUTER_RADIUS = 19.0f;
    private static final int SEGMENTS = 32;

    // Arc sweep: from -60° to +60° relative to 3 o'clock (right)
    // In radians: from -π/3 to +π/3
    private static final float ARC_START = (float) (-Math.PI / 3.0); // -60°
    private static final float ARC_END = (float) (Math.PI / 3.0);    // +60°

    // ── Colors ───────────────────────────────────────────────────
    private static final int SAPPHIRE_R = 10, SAPPHIRE_G = 36, SAPPHIRE_B = 99;
    private static final int TEAL_R = 0, TEAL_G = 255, TEAL_B = 204;
    private static final int BG_R = 15, BG_G = 15, BG_B = 15;
    private static final int OUTLINE_R = 5, OUTLINE_G = 5, OUTLINE_B = 5;

    // ── Main Render ──────────────────────────────────────────────

    /**
     * Render the mana arc near the crosshair.
     *
     * @param guiGraphics the GUI graphics context
     * @param state       current animation state
     * @param partialTick fractional tick
     * @param globalAlpha overall opacity multiplier
     */
    public static void render(GuiGraphics guiGraphics, ManaAnimationState state,
                              float partialTick, float globalAlpha) {
        Minecraft mc = Minecraft.getInstance();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        if (globalAlpha <= 0.01f) return;

        long gameTime = mc.level != null ? mc.level.getGameTime() : 0;
        float time = gameTime + partialTick;
        float manaPercent = state.getManaPercent();
        float fizzle = state.getFizzleProgress();
        float charge = state.getChargeIntensity();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull(); // Disable backface culling in case winding order is backwards
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        // 1. Touki outer glow (behind main arc)
        if (state.isToukiActive() && state.getActiveSwordStyle() != null) {
            renderToukiGlow(matrix, centerX, centerY, state.getActiveSwordStyle(),
                    time, globalAlpha);
        }

        // 1.5 Black outline (behind main arc)
        renderOutlineArc(matrix, centerX, centerY, globalAlpha);

        // 2. Main arc (background + fill)
        renderMainArc(matrix, centerX, centerY, manaPercent, fizzle, charge,
                time, globalAlpha);

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        // 3. Numbers
        if (MushokuConfig.SHOW_MANA_NUMBERS.get()) {
            renderManaText(guiGraphics, mc, centerX, centerY, state, globalAlpha);
        }
    }

    // ── Main Arc ─────────────────────────────────────────────────

    private static void renderOutlineArc(Matrix4f matrix, int cx, int cy, float alpha) {
        float outR = OUTER_RADIUS + 1.0f;
        float inR = INNER_RADIUS - 1.0f;
        int a = (int) (180 * alpha);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            float segPercent = (float) i / SEGMENTS;
            float angle = Mth.lerp(segPercent, ARC_START, ARC_END);

            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);

            float outerX = cx + cosA * outR;
            float outerY = cy - sinA * outR;
            float innerX = cx + cosA * inR;
            float innerY = cy - sinA * inR;

            buffer.addVertex(matrix, outerX, outerY, 0)
                    .setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a);
            buffer.addVertex(matrix, innerX, innerY, 0)
                    .setColor(OUTLINE_R, OUTLINE_G, OUTLINE_B, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderMainArc(Matrix4f matrix, int cx, int cy,
                                      float manaPercent, float fizzle, float charge,
                                      float time, float alpha) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            float segPercent = (float) i / SEGMENTS;
            // Angle: sweep from bottom (-60°) to top (+60°) on the right side
            // We go from ARC_START to ARC_END
            float angle = Mth.lerp(segPercent, ARC_START, ARC_END);

            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);

            float outerX = cx + cosA * OUTER_RADIUS;
            float outerY = cy - sinA * OUTER_RADIUS; // Y is inverted in screen space
            float innerX = cx + cosA * INNER_RADIUS;
            float innerY = cy - sinA * INNER_RADIUS;

            // Determine color for this segment
            boolean filled = segPercent <= manaPercent;
            int r, g, b, a;

            if (fizzle > 0) {
                // Fizzle: flash red
                if (filled) {
                    r = (int) Mth.lerp(fizzle, 0, 255);
                    g = (int) Mth.lerp(fizzle, 100, 50);
                    b = (int) Mth.lerp(fizzle, 150, 50);
                } else {
                    r = (int) (60 * fizzle);
                    g = 20;
                    b = 20;
                }
                a = (int) (200 * alpha);
            } else if (filled) {
                // Gradient from sapphire (start) to teal (end of fill)
                float gradT = manaPercent > 0 ? segPercent / manaPercent : 0;
                r = (int) Mth.lerp(gradT, SAPPHIRE_R, TEAL_R);
                g = (int) Mth.lerp(gradT, SAPPHIRE_G, TEAL_G);
                b = (int) Mth.lerp(gradT, SAPPHIRE_B, TEAL_B);
                a = (int) (200 * alpha);

                // Charge brightness pulse
                if (charge > 0) {
                    float pulse = (float) Math.sin(time * (0.3 + charge * 0.5)) * 0.5f + 0.5f;
                    float brighten = charge * pulse * 0.4f;
                    r = Math.min(255, r + (int) (brighten * (255 - r)));
                    g = Math.min(255, g + (int) (brighten * (255 - g)));
                    b = Math.min(255, b + (int) (brighten * (255 - b)));
                }
            } else {
                // Unfilled background
                r = BG_R;
                g = BG_G;
                b = BG_B;
                a = (int) (120 * alpha);
            }

            buffer.addVertex(matrix, outerX, outerY, 0).setColor(r, g, b, a);
            buffer.addVertex(matrix, innerX, innerY, 0).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    // ── Touki Outer Glow ─────────────────────────────────────────

    private static void renderToukiGlow(Matrix4f matrix, int cx, int cy,
                                        SwordStyle style, float time, float alpha) {
        int[] color = getStyleColor(style);
        float pulse = 0.7f + 0.3f * (float) Math.sin(time * 0.2);
        int glowAlpha = (int) (35 * pulse * alpha);

        float glowInner = OUTER_RADIUS;
        float glowOuter = OUTER_RADIUS + 3.0f;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS; i++) {
            float segPercent = (float) i / SEGMENTS;
            float angle = Mth.lerp(segPercent, ARC_START, ARC_END);

            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);

            float outerX = cx + cosA * glowOuter;
            float outerY = cy - sinA * glowOuter;
            float innerX = cx + cosA * glowInner;
            float innerY = cy - sinA * glowInner;

            // Outer edge is fully transparent, inner edge has color
            buffer.addVertex(matrix, outerX, outerY, 0)
                    .setColor(color[0], color[1], color[2], 0);
            buffer.addVertex(matrix, innerX, innerY, 0)
                    .setColor(color[0], color[1], color[2], glowAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    // ── Text ─────────────────────────────────────────────────────

    private static void renderManaText(GuiGraphics g, Minecraft mc, int cx, int cy,
                                       ManaAnimationState state, float alpha) {
        String text = (int) state.getCurrentMana() + " / " + (int) state.getMaxMana();
        
        float scale = 0.65f;
        float invScale = 1.0f / scale;
        
        // Place it just to the right of the arc.
        // Arc outer radius is 19.
        float textX = cx + OUTER_RADIUS + 4.0f;
        float textY = cy - (4.0f * scale);
        
        int a = (int) (220 * alpha);
        a = Mth.clamp(a, 0, 255);
        int textColor = (a << 24) | (240 << 16) | (248 << 8) | 255; // Alice Blue
        
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0f);
        
        g.drawString(mc.font, text, (int)(textX * invScale), (int)(textY * invScale), textColor, true);
        
        g.pose().popPose();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static int[] getStyleColor(SwordStyle style) {
        return switch (style) {
            case SWORD_GOD -> new int[]{255, 215, 0};
            case WATER_GOD -> new int[]{0, 191, 255};
            case NORTH_GOD -> new int[]{170, 68, 255};
        };
    }
}





