package com.mushokucraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.hud.HudConstants;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.magic.Spell;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

public class IncantationOverlay {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer <= 0) return;
        if (ClientCastState.currentSpell == null) return;

        Spell spell = ModSpells.SPELLS.get(ClientCastState.currentSpell);
        if (spell == null) return;

        String fullText = I18n.get(spell.getIncantationKey());
        if (fullText == null || fullText.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int maxWidth = Math.max(120, screenWidth - 40);

        List<String> allLines = wrapText(mc.font, fullText, maxWidth);
        if (allLines.isEmpty()) return;

        int totalChars = 0;
        for (String line : allLines) {
            totalChars += line.length();
        }
        if (totalChars <= 0) return;

        float partialTick = (deltaTracker != null) ? deltaTracker.getGameTimeDeltaPartialTick(true) : 0f;

        float progress = 1.0f;
        if (ClientCastState.isCasting) {
            float currentTicks = ClientCastState.elapsedTicks + partialTick;
            progress = ClientCastState.totalTicks > 0 ? currentTicks / ClientCastState.totalTicks : 1.0f;
        } else if (ClientCastState.hasFizzled) {
            progress = ClientCastState.totalTicks > 0 ? (float) ClientCastState.fizzleTick / ClientCastState.totalTicks : 0.0f;
        }
        progress = Math.max(0.0f, Math.min(1.0f, progress));

        int charsToShow = (int) (totalChars * progress);
        if (charsToShow <= 0 && ClientCastState.isCasting) return;

        List<String> visibleLines = new ArrayList<>();
        int remaining = charsToShow;
        for (String line : allLines) {
            if (remaining <= 0) break;
            if (remaining >= line.length()) {
                visibleLines.add(line);
                remaining -= line.length();
            } else {
                visibleLines.add(line.substring(0, remaining));
                remaining = 0;
                break;
            }
        }
        if (visibleLines.isEmpty()) return;

        int yOffset = screenHeight - HudConstants.INCANTATION_Y_OFFSET_FROM_BOTTOM;

        int baseAlpha = 255;
        if (!ClientCastState.isCasting && ClientCastState.fadeOutTimer > 0) {
            float fadeProgress = (ClientCastState.fadeOutTimer - partialTick) / (float) HudConstants.INCANTATION_FADE_TICKS;
            baseAlpha = (int) (Math.max(0.0f, Math.min(1.0f, fadeProgress)) * 255);

            // If fizzled, animate it floating up
            if (ClientCastState.hasFizzled) {
                float fizzleOffset = (HudConstants.INCANTATION_FADE_TICKS - (ClientCastState.fadeOutTimer - partialTick));
                yOffset -= (int) fizzleOffset;
            }
        }
        baseAlpha = Math.max(0, Math.min(255, baseAlpha));
        if (baseAlpha <= 0) return;

        int lineHeight = mc.font.lineHeight;
        int lineSpacing = lineHeight + 3;

        int crosshairMargin = screenHeight / 2 + 16;
        int availableHeight = Math.max(lineSpacing, yOffset - crosshairMargin);
        int maxLinesAllowed = Math.min(5, Math.max(2, availableHeight / lineSpacing));

        int totalVisible = visibleLines.size();
        int startIndex = Math.max(0, totalVisible - maxLinesAllowed);
        int numDisplayLines = totalVisible - startIndex;

        RenderSystem.enableBlend();

        for (int i = 0; i < numDisplayLines; i++) {
            int lineIdx = startIndex + i;
            String line = visibleLines.get(lineIdx);
            if (line.isEmpty()) continue;

            boolean isCurrentLine = (i == numDisplayLines - 1);
            int y = yOffset - (numDisplayLines - 1 - i) * lineSpacing;

            int lineAlpha;
            int color;

            if (ClientCastState.hasFizzled) {
                lineAlpha = baseAlpha;
                color = HudConstants.INCANTATION_COLOR_FIZZLED;
            } else {
                float fadeFactor = Math.max(0.4f, 1.0f - (numDisplayLines - 1 - i) * 0.15f);
                lineAlpha = (int) (baseAlpha * fadeFactor);
                color = HudConstants.INCANTATION_COLOR_NORMAL;
            }

            int argb = (lineAlpha << 24) | (color & 0x00FFFFFF);
            int lineWidth = mc.font.width(line);

            if (lineWidth > maxWidth && lineWidth > 0) {
                float scale = (float) maxWidth / lineWidth;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(screenWidth / 2.0f, y, 0);
                guiGraphics.pose().scale(scale, scale, 1.0f);
                guiGraphics.drawString(mc.font, line, -lineWidth / 2, 0, argb, true);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.drawString(mc.font, line, (screenWidth - lineWidth) / 2, y, argb, true);
            }
        }

        RenderSystem.disableBlend();
    }

    public static List<String> wrapText(Font font, String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        String[] paragraphs = normalized.split("\n", -1);

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (font.width(trimmed) <= maxWidth) {
                result.add(trimmed);
            } else {
                String[] words = trimmed.split("\\s+");
                StringBuilder currentLine = new StringBuilder();

                for (String word : words) {
                    if (currentLine.length() == 0) {
                        currentLine.append(word);
                    } else {
                        String candidate = currentLine + " " + word;
                        if (font.width(candidate) <= maxWidth) {
                            currentLine.append(" ").append(word);
                        } else {
                            result.add(currentLine.toString());
                            currentLine = new StringBuilder(word);
                        }
                    }
                }

                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                }
            }
        }

        return result;
    }
}





