package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientCastState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import com.mushokucraft.client.hud.HudConstants;

public class QTEOverlay {
    
    public static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (!ClientCastState.isQteActive) return;
        
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        RenderSystem.enableBlend();
        
        float targetSize = ClientCastState.qteTargetSizeModifier;
        float scale = ClientCastState.qteShrinkingCircleScale;
        
        // Base radii
        float baseCentralRadius = HudConstants.QTE_CENTRAL_RADIUS_MULT * targetSize;
        float baseGreenRadius = HudConstants.QTE_GREEN_RADIUS_MULT * targetSize;
        float shrinkingRadius = HudConstants.QTE_SHRINKING_BASE_RADIUS * scale;
        
        // Ensure green radius is less than central radius
        baseGreenRadius = Math.min(baseGreenRadius, baseCentralRadius - 2.0f);
        
        // 1. Draw central circle
        drawCircle(guiGraphics, centerX, centerY, (int)baseCentralRadius, HudConstants.QTE_COLOR_CENTRAL);
        
        // 2. Draw green circle (perfect zone)
        drawCircle(guiGraphics, centerX, centerY, (int)baseGreenRadius, HudConstants.QTE_COLOR_GREEN);
        
        // 3. Draw shrinking circle
        int shrinkColor = HudConstants.QTE_COLOR_SHRINK_DEFAULT;
        if (scale <= targetSize) {
            if (scale <= HudConstants.QTE_PERFECT_MULTIPLIER * targetSize) {
                shrinkColor = HudConstants.QTE_COLOR_SHRINKING_PERFECT;
            } else {
                shrinkColor = HudConstants.QTE_COLOR_SHRINKING_GOOD;
            }
        }
        if (shrinkingRadius > 0) {
            drawCircle(guiGraphics, centerX, centerY, (int)shrinkingRadius, shrinkColor);
        }
        
        // 4. Draw key letter
        String keyToPress = ClientCastState.currentQteKey.toUpperCase();
        int textWidth = mc.font.width(keyToPress);
        guiGraphics.drawString(mc.font, keyToPress, centerX - textWidth / 2, centerY - 4, HudConstants.QTE_COLOR_TEXT);
        
        RenderSystem.disableBlend();
    }
    
    private static void drawCircle(GuiGraphics guiGraphics, int x, int y, int radius, int color) {
        // Approximate a hollow circle
        int points = 32;
        for (int i = 0; i < points; i++) {
            double angle1 = 2 * Math.PI * i / points;
            double angle2 = 2 * Math.PI * (i + 1) / points;
            
            int x1 = x + (int) Math.round(radius * Math.cos(angle1));
            int y1 = y + (int) Math.round(radius * Math.sin(angle1));
            int x2 = x + (int) Math.round(radius * Math.cos(angle2));
            int y2 = y + (int) Math.round(radius * Math.sin(angle2));
            
            // Draw a line (approximated by fill)
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            // Connect points to make it look continuous
            guiGraphics.fill((x1+x2)/2, (y1+y2)/2, (x1+x2)/2 + 1, (y1+y2)/2 + 1, color);
        }
    }
}





