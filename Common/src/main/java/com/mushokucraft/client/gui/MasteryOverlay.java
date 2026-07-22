package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.mushokucraft.client.hud.HudConstants;

public class MasteryOverlay {
    
    private static float gainedAmount = 0f;
    private static int showTimer = 0;
    
    public static void showMasteryGain(float amount) {
        gainedAmount = amount;
        showTimer = HudConstants.MASTERY_SHOW_TICKS;
    }

    public static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (showTimer <= 0) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        showTimer--;
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        String formattedAmount = String.format("%.1f", gainedAmount * 100.0f);
        String text = "§a" + net.minecraft.client.resources.language.I18n.get("gui.mushokucraft.mastery_gain", formattedAmount);
        int textWidth = mc.font.width(text);
        
        // Render slightly above the action bar
        int yOffset = screenHeight - HudConstants.MASTERY_Y_OFFSET_FROM_BOTTOM; 
        
        RenderSystem.enableBlend();
        
        int alpha = 255;
        if (showTimer < HudConstants.MASTERY_FADE_START_TICK) {
            alpha = (int) ((showTimer / (float)HudConstants.MASTERY_FADE_START_TICK) * 255);
        }
        
        int argb = (alpha << 24) | HudConstants.MASTERY_TEXT_COLOR;
        
        guiGraphics.drawString(mc.font, text, screenWidth / 2 - textWidth / 2, yOffset, argb);
        
        RenderSystem.disableBlend();
    }
}





