package com.mushokucraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class FantasyButton extends AbstractButton {
    public interface OnPress {
        void onPress(FantasyButton button);
    }

    private final OnPress onPress;
    private final boolean isPrimary;

    public FantasyButton(int x, int y, int width, int height, Component message, boolean isPrimary, OnPress onPress) {
        super(x, y, width, height, message);
        this.isPrimary = isPrimary;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        boolean hovered = this.isHoveredOrFocused();

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        // Background colors
        int bgTop, bgBottom, borderColor, borderHighlight;
        int textColor;

        if (!this.active) {
            bgTop = 0xDD1E1A18;
            bgBottom = 0xDD141210;
            borderColor = 0xFF4A423D;
            borderHighlight = 0xFF35302C;
            textColor = 0xFF7A726B;
        } else if (hovered) {
            bgTop = isPrimary ? 0xEE44281A : 0xEE33261C;
            bgBottom = isPrimary ? 0xEE2E1A10 : 0xEE221812;
            borderColor = isPrimary ? 0xFFFFD700 : 0xFFE0B066;
            borderHighlight = isPrimary ? 0xFFFFF2A0 : 0xFFFFE4B5;
            textColor = 0xFFFFFBE8;
        } else {
            bgTop = isPrimary ? 0xEE321D12 : 0xEE261B14;
            bgBottom = isPrimary ? 0xEE1E110A : 0xEE18100C;
            borderColor = isPrimary ? 0xFFC99738 : 0xFFA67C38;
            borderHighlight = isPrimary ? 0xFFE6B85C : 0xFFC29B4A;
            textColor = isPrimary ? 0xFFFFE599 : 0xFFE8D4A2;
        }

        // Fill background gradient
        guiGraphics.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, bgTop, bgBottom);

        // Outer border
        guiGraphics.fill(x + 1, y, x + w - 1, y + 1, borderColor);             // Top
        guiGraphics.fill(x + 1, y + h - 1, x + w - 1, y + h, borderColor);     // Bottom
        guiGraphics.fill(x, y + 1, x + 1, y + h - 1, borderColor);             // Left
        guiGraphics.fill(x + w - 1, y + 1, x + w, y + h - 1, borderColor);     // Right

        // Inner highlight for 3D beveled effect
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + 2, borderHighlight);     // Top inner
        guiGraphics.fill(x + 1, y + 1, x + 2, y + h - 1, borderHighlight);     // Left inner

        // Corner accents (antique brass studs)
        int cornerColor = hovered ? 0xFFFFDF78 : (isPrimary ? 0xFFDBA342 : 0xFFB88636);
        guiGraphics.fill(x, y, x + 1, y + 1, cornerColor);
        guiGraphics.fill(x + w - 1, y, x + w, y + 1, cornerColor);
        guiGraphics.fill(x, y + h - 1, x + 1, y + h, cornerColor);
        guiGraphics.fill(x + w - 1, y + h - 1, x + w, y + h, cornerColor);

        // Text
        int textX = x + (w - font.width(this.getMessage())) / 2;
        int textY = y + (h - 8) / 2;
        guiGraphics.drawString(font, this.getMessage(), textX, textY, textColor, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
