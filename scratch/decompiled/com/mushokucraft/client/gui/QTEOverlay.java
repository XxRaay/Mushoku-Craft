/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 */
package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientCastState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class QTEOverlay {
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!ClientCastState.isQteActive) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        RenderSystem.enableBlend();
        float targetSize = ClientCastState.qteTargetSizeModifier;
        float scale = ClientCastState.qteShrinkingCircleScale;
        float baseCentralRadius = 15.0f * targetSize;
        float baseGreenRadius = 5.0f * targetSize;
        float shrinkingRadius = 30.0f * scale;
        baseGreenRadius = Math.min(baseGreenRadius, baseCentralRadius - 2.0f);
        QTEOverlay.drawCircle(guiGraphics, centerX, centerY, (int)baseCentralRadius, -1);
        QTEOverlay.drawCircle(guiGraphics, centerX, centerY, (int)baseGreenRadius, -16711936);
        int shrinkColor = -22016;
        if (scale <= targetSize) {
            shrinkColor = scale <= 0.3f * targetSize ? -16711681 : -11141291;
        }
        if (shrinkingRadius > 0.0f) {
            QTEOverlay.drawCircle(guiGraphics, centerX, centerY, (int)shrinkingRadius, shrinkColor);
        }
        String keyToPress = ClientCastState.currentQteKey.toUpperCase();
        int textWidth = mc.font.width(keyToPress);
        guiGraphics.drawString(mc.font, keyToPress, centerX - textWidth / 2, centerY - 4, -1);
        RenderSystem.disableBlend();
    }

    private static void drawCircle(GuiGraphics guiGraphics, int x, int y, int radius, int color) {
        int points = 32;
        for (int i = 0; i < points; ++i) {
            double angle1 = Math.PI * 2 * (double)i / (double)points;
            double angle2 = Math.PI * 2 * (double)(i + 1) / (double)points;
            int x1 = x + (int)Math.round((double)radius * Math.cos(angle1));
            int y1 = y + (int)Math.round((double)radius * Math.sin(angle1));
            int x2 = x + (int)Math.round((double)radius * Math.cos(angle2));
            int y2 = y + (int)Math.round((double)radius * Math.sin(angle2));
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            guiGraphics.fill((x1 + x2) / 2, (y1 + y2) / 2, (x1 + x2) / 2 + 1, (y1 + y2) / 2 + 1, color);
        }
    }
}

