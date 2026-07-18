/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.resources.language.I18n
 */
package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class MasteryOverlay {
    private static float gainedAmount = 0.0f;
    private static int showTimer = 0;

    public static void showMasteryGain(float amount) {
        gainedAmount = amount;
        showTimer = 40;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (showTimer <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        --showTimer;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        String formattedAmount = String.format("%.1f", Float.valueOf(gainedAmount * 100.0f));
        String text = "\u00a7a" + I18n.get((String)"gui.mushokucraft.mastery_gain", (Object[])new Object[]{formattedAmount});
        int textWidth = mc.font.width(text);
        int yOffset = screenHeight - 85;
        RenderSystem.enableBlend();
        int alpha = 255;
        if (showTimer < 10) {
            alpha = (int)((float)showTimer / 10.0f * 255.0f);
        }
        int argb = alpha << 24 | 0xFFFFFF;
        guiGraphics.drawString(mc.font, text, screenWidth / 2 - textWidth / 2, yOffset, argb);
        RenderSystem.disableBlend();
    }
}

