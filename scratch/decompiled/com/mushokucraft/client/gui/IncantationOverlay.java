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
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.magic.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class IncantationOverlay {
    /*
     * Enabled aggressive block sorting
     */
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int textLength;
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer <= 0) {
            return;
        }
        if (ClientCastState.currentSpell == null) {
            return;
        }
        Spell spell = ModSpells.SPELLS.get(ClientCastState.currentSpell);
        if (spell == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        String fullText = I18n.get((String)spell.getIncantationKey(), (Object[])new Object[0]);
        int charsToShow = textLength = fullText.length();
        if (ClientCastState.isCasting) {
            progress = (float)ClientCastState.elapsedTicks / (float)ClientCastState.totalTicks;
            charsToShow = (int)((float)textLength * progress);
            charsToShow = Math.min(charsToShow, textLength);
            if (ClientCastState.fizzleTick > 0 && ClientCastState.elapsedTicks < ClientCastState.fizzleTick) {
                // empty if block
            }
        } else if (ClientCastState.hasFizzled) {
            progress = (float)ClientCastState.fizzleTick / (float)ClientCastState.totalTicks;
            charsToShow = (int)((float)textLength * progress);
        }
        charsToShow = Math.max(0, Math.min(charsToShow, textLength));
        String textToRender = fullText.substring(0, charsToShow);
        int yOffset = screenHeight - 80;
        RenderSystem.enableBlend();
        int alpha = 255;
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer > 0) {
            alpha = (int)((float)ClientCastState.fadeOutTimer / 40.0f * 255.0f);
            if (ClientCastState.hasFizzled) {
                yOffset -= 40 - ClientCastState.fadeOutTimer;
            }
        }
        int color = ClientCastState.hasFizzled ? 0xFF0000 : 0xAAAAAA;
        int argb = alpha << 24 | color;
        int maxWidth = screenWidth - 100;
        int x = (screenWidth - Math.min(maxWidth, mc.font.width(textToRender))) / 2;
        int textWidth = mc.font.width(textToRender);
        guiGraphics.drawString(mc.font, textToRender, screenWidth / 2 - textWidth / 2, yOffset, argb);
        RenderSystem.disableBlend();
    }
}

