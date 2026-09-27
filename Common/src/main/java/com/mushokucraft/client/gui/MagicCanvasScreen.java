package com.mushokucraft.client.gui;

import com.mushokucraft.client.gui.widget.FantasyButton;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.network.ConsumeCanvasInkPacket;
import com.mushokucraft.network.InscribeCanvasPacket;
import com.mushokucraft.network.SaveCanvasDraftPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MagicCanvasScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/magic_canvas_gui.png");
    private static final int GUI_SIZE = 256;
    private static final int GRID_SIZE = MagicCirclePattern.SIZE; // 16
    private static final int CELL_SIZE = 8; // 16 * 8 = 128px

    private final boolean isMainHand;
    private final MagicCirclePattern pattern;
    private int spentInkSacs;
    private boolean isDrawing = false;
    private boolean isErasing = false;
    private int noInkWarningTicks = 0;
    private FantasyButton inscribeButton;
    private final ItemStack inkSacStack = new ItemStack(Items.INK_SAC);

    public MagicCanvasScreen(boolean isMainHand) {
        this(isMainHand, new MagicCirclePattern(), 0);
    }

    public MagicCanvasScreen(boolean isMainHand, MagicCirclePattern initialPattern, int initialSpent) {
        super(Component.translatable("gui.mushokucraft.magic_canvas.title"));
        this.isMainHand = isMainHand;
        this.pattern = initialPattern != null ? new MagicCirclePattern(initialPattern.getRawBytes()) : new MagicCirclePattern();
        this.spentInkSacs = Math.max(0, initialSpent);
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - GUI_SIZE) / 2;
        int top = (this.height - GUI_SIZE) / 2;

        // Custom fantasy styled "Inscribe" Button
        this.inscribeButton = new FantasyButton(left + 36, top + 215, 88, 22,
                Component.translatable("gui.mushokucraft.magic_canvas.confirm"), true, btn -> {
            if (pattern.countFilled() > 0) {
                NetworkManager.sendToServer(new InscribeCanvasPacket(pattern.getRawBytes(), isMainHand));
                this.minecraft.setScreen(null);
            }
        });
        this.addRenderableWidget(inscribeButton);

        // Custom fantasy styled "Clear" Button
        this.addRenderableWidget(new FantasyButton(left + 132, top + 215, 88, 22,
                Component.translatable("gui.mushokucraft.magic_canvas.clear"), false, btn -> {
            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    pattern.setPixel(x, y, false);
                }
            }
            NetworkManager.sendToServer(new SaveCanvasDraftPacket(pattern.getRawBytes(), spentInkSacs, isMainHand));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SAND_HIT, 1.2f));
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (noInkWarningTicks > 0) {
            noInkWarningTicks--;
        }
        if (inscribeButton != null) {
            inscribeButton.active = pattern.countFilled() > 0;
        }
    }

    @Override
    public void onClose() {
        // Automatically save whatever was drawn when closing the GUI
        NetworkManager.sendToServer(new SaveCanvasDraftPacket(pattern.getRawBytes(), spentInkSacs, isMainHand));
        super.onClose();
    }

    private int countAvailableInkSacs() {
        if (minecraft == null || minecraft.player == null) return 0;
        int count = 0;
        for (int i = 0; i < minecraft.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (stack.is(Items.INK_SAC)) {
                count += stack.getCount();
            }
        }
        return count;
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

        // Title banner on top wooden frame
        Component title = Component.translatable("gui.mushokucraft.magic_canvas.title");
        int titleWidth = this.font.width(title);
        guiGraphics.fill(centerX - titleWidth / 2 - 8, top + 15, centerX + titleWidth / 2 + 8, top + 31, 0xB01A120A);
        guiGraphics.renderOutline(centerX - titleWidth / 2 - 8, top + 15, titleWidth + 16, 16, 0x88D4AF37);
        guiGraphics.drawString(this.font, title, centerX - titleWidth / 2, top + 19, 0xFFFFE599, true);

        // Drawing Grid (128x128)
        int gridX = left + 64;
        int gridY = top + 44;

        // Subtle recessed canvas bevel
        guiGraphics.fill(gridX - 2, gridY - 2, gridX + GRID_SIZE * CELL_SIZE + 2, gridY + GRID_SIZE * CELL_SIZE + 2, 0x772A1F18);
        guiGraphics.fill(gridX - 1, gridY - 1, gridX + GRID_SIZE * CELL_SIZE + 1, gridY + GRID_SIZE * CELL_SIZE + 1, 0xFFE5D5BA);

        // 16x16 Grid cells
        for (int gy = 0; gy < GRID_SIZE; gy++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int px = gridX + gx * CELL_SIZE;
                int py = gridY + gy * CELL_SIZE;

                boolean filled = pattern.getPixel(gx, gy);
                int color;
                if (filled) {
                    color = 0xFF140F22; // Deep midnight magical ink
                } else {
                    color = ((gx + gy) % 2 == 0) ? 0xFFFAF4E6 : 0xFFF2EAD6;
                }
                guiGraphics.fill(px, py, px + CELL_SIZE, py + CELL_SIZE, color);

                // Grid cell borders
                guiGraphics.renderOutline(px, py, CELL_SIZE, CELL_SIZE, 0x12000000);

                // 4x4 Major quadrant guides
                if (gx % 4 == 0 && gy % 4 == 0) {
                    guiGraphics.renderOutline(px, py, CELL_SIZE * 4, CELL_SIZE * 4, 0x18705030);
                }
            }
        }

        // Cell hover effect
        int hx = (mouseX - gridX) / CELL_SIZE;
        int hy = (mouseY - gridY) / CELL_SIZE;
        if (hx >= 0 && hx < GRID_SIZE && hy >= 0 && hy < GRID_SIZE) {
            int px = gridX + hx * CELL_SIZE;
            int py = gridY + hy * CELL_SIZE;
            if (isErasing) {
                guiGraphics.fill(px, py, px + CELL_SIZE, py + CELL_SIZE, 0x55E53935);
            } else {
                guiGraphics.fill(px, py, px + CELL_SIZE, py + CELL_SIZE, 0x443F2A56);
            }
        }

        // Ink Sac Icon & Stats below grid
        int inkY = top + 175;
        int inkSacs = countAvailableInkSacs();
        int pixelsPerSac = Math.max(1, MushokuConfig.MAGIC_CIRCLE_INK_PIXELS_PER_SAC.get());
        int filled = pattern.countFilled();
        int requiredSacs = (filled + pixelsPerSac - 1) / pixelsPerSac;

        if (noInkWarningTicks > 0) {
            Component warn = Component.translatable("gui.mushokucraft.magic_canvas.no_ink");
            int warnWidth = this.font.width(warn);
            guiGraphics.drawString(this.font, warn, centerX - warnWidth / 2, inkY + 4, 0xFFD32F2F, false);
        } else {
            Component inkInfo = Component.translatable("gui.mushokucraft.magic_canvas.ink_info",
                    filled, requiredSacs, inkSacs);
            int infoWidth = this.font.width(inkInfo);
            int contentStart = centerX - (infoWidth + 18) / 2;
            guiGraphics.renderItem(inkSacStack, contentStart, inkY);
            int inkColor = (requiredSacs > spentInkSacs && inkSacs == 0) ? 0xFFC62828 : 0x2A1C12;
            guiGraphics.drawString(this.font, inkInfo, contentStart + 18, inkY + 4, inkColor, false);
        }

        // Controls hint
        Component hint = Component.translatable("gui.mushokucraft.magic_canvas.hint");
        int hintWidth = this.font.width(hint);
        guiGraphics.drawString(this.font, hint, centerX - hintWidth / 2, top + 194, 0x6E5947, false);
    }

    private void handlePixelInteraction(double mouseX, double mouseY, boolean paint) {
        int left = (this.width - GUI_SIZE) / 2;
        int top = (this.height - GUI_SIZE) / 2;
        int gridX = left + 64;
        int gridY = top + 44;

        int gx = (int) (mouseX - gridX) / CELL_SIZE;
        int gy = (int) (mouseY - gridY) / CELL_SIZE;

        if (gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE) {
            if (paint) {
                if (!pattern.getPixel(gx, gy)) {
                    int pixelsPerSac = Math.max(1, MushokuConfig.MAGIC_CIRCLE_INK_PIXELS_PER_SAC.get());
                    int newFilled = pattern.countFilled() + 1;
                    int requiredSacs = (newFilled + pixelsPerSac - 1) / pixelsPerSac;

                    if (requiredSacs > spentInkSacs) {
                        int available = countAvailableInkSacs();
                        if (available <= 0) {
                            noInkWarningTicks = 30;
                            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.2f));
                            return;
                        }

                        // Consume ink sac in real-time
                        spentInkSacs++;
                        NetworkManager.sendToServer(new ConsumeCanvasInkPacket(isMainHand));
                    }

                    pattern.setPixel(gx, gy, true);
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.3f));
                }
            } else {
                if (pattern.getPixel(gx, gy)) {
                    pattern.setPixel(gx, gy, false);
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SAND_HIT, 1.6f));
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDrawing = true;
            handlePixelInteraction(mouseX, mouseY, true);
        } else if (button == 1) {
            isErasing = true;
            handlePixelInteraction(mouseX, mouseY, false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) isDrawing = false;
        if (button == 1) isErasing = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDrawing) {
            handlePixelInteraction(mouseX, mouseY, true);
        } else if (isErasing) {
            handlePixelInteraction(mouseX, mouseY, false);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
