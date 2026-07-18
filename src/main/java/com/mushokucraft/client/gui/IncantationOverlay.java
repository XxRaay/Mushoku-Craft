package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.magic.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import com.mushokucraft.client.hud.HudConstants;

public class IncantationOverlay {

    public static void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer <= 0) return;
        if (ClientCastState.currentSpell == null) return;

        Spell spell = ModSpells.SPELLS.get(ClientCastState.currentSpell);
        if (spell == null) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        String fullText = I18n.get(spell.getIncantationKey());
        
        int textLength = fullText.length();
        int charsToShow = textLength;
        
        if (ClientCastState.isCasting) {
            float progress = (float) ClientCastState.elapsedTicks / ClientCastState.totalTicks;
            charsToShow = (int) (textLength * progress);
            charsToShow = Math.min(charsToShow, textLength);
            
            // If fizzling, stop text at the fizzle point
            if (ClientCastState.fizzleTick > 0 && ClientCastState.elapsedTicks >= ClientCastState.fizzleTick) {
                // Should not happen here since state changes to !isCasting, but just in case
            }
        } else {
            // It's in fadeout state. If fizzled, it only showed a portion.
            if (ClientCastState.hasFizzled) {
                float progress = (float) ClientCastState.fizzleTick / ClientCastState.totalTicks;
                charsToShow = (int) (textLength * progress);
            }
        }
        
        charsToShow = Math.max(0, Math.min(charsToShow, textLength));
        String textToRender = fullText.substring(0, charsToShow);

        int yOffset = screenHeight - HudConstants.INCANTATION_Y_OFFSET_FROM_BOTTOM;

        RenderSystem.enableBlend();

        int alpha = 255;
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer > 0) {
            alpha = (int) ((ClientCastState.fadeOutTimer / (float)HudConstants.INCANTATION_FADE_TICKS) * 255);
            
            // If fizzled, animate it going up
            if (ClientCastState.hasFizzled) {
                yOffset -= (HudConstants.INCANTATION_FADE_TICKS - ClientCastState.fadeOutTimer);
            }
        }

        int color = ClientCastState.hasFizzled ? HudConstants.INCANTATION_COLOR_FIZZLED : HudConstants.INCANTATION_COLOR_NORMAL;
        // Combine alpha with color
        int argb = (alpha << 24) | color;

        // Render centered text
        // If the text is very long, we might need to split it into multiple lines.
        // For simplicity, let's just draw it. A single line might overflow.
        // Let's use font.drawWordWrap
        int maxWidth = screenWidth - 100;
        int x = (screenWidth - Math.min(maxWidth, mc.font.width(textToRender))) / 2;
        
        // This splits by words, but we only have a substring, which might cut in the middle of a word.
        // It's better to render just a simple string for now, or just limit line width.
        // Let's draw centered:
        int textWidth = mc.font.width(textToRender);
        guiGraphics.drawString(mc.font, textToRender, screenWidth / 2 - textWidth / 2, yOffset, argb);

        RenderSystem.disableBlend();
    }
}





