package com.mushokucraft.client.gui;

import com.mushokucraft.crafting.ModularAnvilMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ModularAnvilScreen extends AbstractContainerScreen<ModularAnvilMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/modular_anvil_gui.png");
    private static final ResourceLocation BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/custom_button.png");
    
    private EditBox nameField;

    public ModularAnvilScreen(ModularAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 222;
        this.imageHeight = 208;
        this.inventoryLabelY = 110;
        this.inventoryLabelX = 30;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        
        // Name EditBox
        this.nameField = new EditBox(this.font, this.leftPos + 112, this.topPos + 23, 88, 12, Component.translatable("gui.mushokucraft.modular_anvil.name_placeholder"));
        this.nameField.setCanLoseFocus(false);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(50);
        this.addWidget(this.nameField);
        this.setInitialFocus(this.nameField);

        // Forge Button (Use standard vanilla button, looks better and doesn't get cut off)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mushokucraft.modular_anvil.forge"), button -> {
            // No functionality yet, just visual
        }).bounds(this.leftPos + 116, this.topPos + 98, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Render EditBox
        this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // Mana Bar Fill
        int manaFillWidth = (int) (88 * (150.0 / 250.0)); // Placeholder ratio
        guiGraphics.fill(x + 112, y + 75, x + 112 + manaFillWidth, y + 85, 0xFF2255AA);
        
        // Mana Text
        Component manaText = Component.translatable("gui.mushokucraft.modular_anvil.mana", "150/250");
        guiGraphics.drawCenteredString(this.font, manaText, x + 111 + 45, y + 76, 0xFFFFFFFF);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
        }
        if (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
