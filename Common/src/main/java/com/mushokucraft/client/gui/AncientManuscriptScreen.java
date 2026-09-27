package com.mushokucraft.client.gui;

import com.mushokucraft.client.gui.widget.FantasyButton;
import com.mushokucraft.magic.circle.ClientMagicCircleState;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AncientManuscriptScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/ancient_manuscript_gui.png");
    private static final int GUI_SIZE = 256;
    private static final int GRID_SIZE = MagicCirclePattern.SIZE; // 16
    private static final int CELL_SIZE = 5; // 16 * 5 = 80px

    private final ResourceLocation circleTypeId;

    public AncientManuscriptScreen() {
        this(com.mushokucraft.magic.circle.TeleportCircleType.ID);
    }

    public AncientManuscriptScreen(ResourceLocation circleTypeId) {
        super(Component.translatable("gui.mushokucraft.ancient_manuscript.title"));
        this.circleTypeId = (circleTypeId != null) ? circleTypeId : com.mushokucraft.magic.circle.TeleportCircleType.ID;
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - GUI_SIZE) / 2;
        int top = (this.height - GUI_SIZE) / 2;

        // Custom compact fantasy close button cleanly placed inside bottom parchment area
        this.addRenderableWidget(new FantasyButton(left + (GUI_SIZE - 74) / 2, top + 195, 74, 18,
                Component.translatable("gui.mushokucraft.ancient_manuscript.close"), true, btn -> {
            this.minecraft.setScreen(null);
        }));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int left = (this.width - GUI_SIZE) / 2;
        int top = (this.height - GUI_SIZE) / 2;
        guiGraphics.blit(GUI_TEXTURE, left, top, 0.0f, 0.0f, GUI_SIZE, GUI_SIZE, GUI_SIZE, GUI_SIZE);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int left = (this.width - GUI_SIZE) / 2;
        int top = (this.height - GUI_SIZE) / 2;
        int centerX = left + GUI_SIZE / 2;

        // Title and Subtitle centered in upper parchment
        com.mushokucraft.magic.circle.MagicCircleType circleType = com.mushokucraft.magic.circle.MagicCircleRegistry.get(this.circleTypeId);
        Component circleName = circleType != null ? circleType.getDisplayName() : Component.translatable("gui.mushokucraft.ancient_manuscript.circle_name");
        guiGraphics.drawString(this.font, circleName, centerX - this.font.width(circleName) / 2, top + 56, 0x2A150A, false);

        Component title = Component.translatable("gui.mushokucraft.ancient_manuscript.title");
        guiGraphics.drawString(this.font, title, centerX - this.font.width(title) / 2, top + 66, 0x6E4C36, false);

        // 80x80 Diagram (Y = 77 to 157, X = 88 to 168)
        int gridX = left + (GUI_SIZE - GRID_SIZE * CELL_SIZE) / 2;
        int gridY = top + 77;

        // Antique border around diagram
        guiGraphics.fill(gridX - 2, gridY - 2, gridX + GRID_SIZE * CELL_SIZE + 2, gridY + GRID_SIZE * CELL_SIZE + 2, 0x993D2616);
        guiGraphics.fill(gridX - 1, gridY - 1, gridX + GRID_SIZE * CELL_SIZE + 1, gridY + GRID_SIZE * CELL_SIZE + 1, 0xFFEFE4CC);

        // Render 1:1 active world pattern
        MagicCirclePattern pattern = ClientMagicCircleState.getPatternForType(this.circleTypeId);
        if (pattern == null) {
            pattern = ClientMagicCircleState.getActiveTeleportPattern();
        }

        for (int gy = 0; gy < GRID_SIZE; gy++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int px = gridX + gx * CELL_SIZE;
                int py = gridY + gy * CELL_SIZE;

                boolean filled = pattern.getPixel(gx, gy);
                int color = filled ? 0xFF140D22 : 0xFFFAF3E5;
                guiGraphics.fill(px, py, px + CELL_SIZE, py + CELL_SIZE, color);

                if (!filled) {
                    guiGraphics.renderOutline(px, py, CELL_SIZE, CELL_SIZE, 0x10000000);
                }

                // 4x4 quadrant division guides
                if (gx % 4 == 0 && gy % 4 == 0) {
                    guiGraphics.renderOutline(px, py, CELL_SIZE * 4, CELL_SIZE * 4, 0x18705030);
                }
            }
        }

        // Instructions formatted to strictly fit within the 152px parchment width
        Component desc1 = Component.translatable("gui.mushokucraft.ancient_manuscript.desc1");
        Component desc2 = circleType != null ? circleType.getInstruction() : Component.translatable("gui.mushokucraft.ancient_manuscript.desc2");
        Component desc3 = Component.translatable("gui.mushokucraft.ancient_manuscript.desc3");

        guiGraphics.drawString(this.font, desc1, centerX - this.font.width(desc1) / 2, top + 160, 0x362113, false);
        guiGraphics.drawString(this.font, desc2, centerX - this.font.width(desc2) / 2, top + 171, 0x4A2E1A, false);
        guiGraphics.drawString(this.font, desc3, centerX - this.font.width(desc3) / 2, top + 182, 0x5E3D26, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
