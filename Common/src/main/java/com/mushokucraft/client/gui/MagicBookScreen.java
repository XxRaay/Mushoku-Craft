package com.mushokucraft.client.gui;

import com.mushokucraft.network.StartLearnSpellPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.architectury.networking.NetworkManager;

public class MagicBookScreen
extends Screen {
    private final String elementPrefix;
    private final java.util.List<String> spellIds;
    private int currentPage;
    private static final java.util.Map<String, Integer> LAST_OPENED_PAGES = new java.util.HashMap<>();
    private PageButton forwardButton;
    private PageButton backButton;

    public MagicBookScreen(String elementPrefix, java.util.List<String> spellIds) {
        super((Component)Component.translatable((String)("gui.mushokucraft." + elementPrefix + "_magic_book.title")));
        this.elementPrefix = elementPrefix;
        this.spellIds = spellIds;
        this.currentPage = Math.min(LAST_OPENED_PAGES.getOrDefault(elementPrefix, 0), spellIds.size() - 1);
        if (this.currentPage < 0) this.currentPage = 0;
    }

    protected void init() {
        super.init();
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;
        
        this.addRenderableWidget(new CustomButton(x + (bookWidth - 120) / 2, y + 185, 120, 50, Component.translatable("gui.mushokucraft." + this.elementPrefix + "_magic_book.learn"), button -> {
            NetworkManager.sendToServer(new StartLearnSpellPacket(ResourceLocation.fromNamespaceAndPath("mushokucraft", this.spellIds.get(this.currentPage))));
            this.minecraft.setScreen(null);
        }));

        this.forwardButton = this.addRenderableWidget(new PageButton(x + 160, y + 200, true, (button) -> {
            this.pageForward();
        }, true));
        this.backButton = this.addRenderableWidget(new PageButton(x + 17, y + 200, false, (button) -> {
            this.pageBack();
        }, true));
        this.updateButtonVisibility();
    }

    private void pageForward() {
        if (this.currentPage < this.spellIds.size() - 1) {
            this.currentPage++;
            LAST_OPENED_PAGES.put(this.elementPrefix, this.currentPage);
        }
        this.updateButtonVisibility();
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            this.currentPage--;
            LAST_OPENED_PAGES.put(this.elementPrefix, this.currentPage);
        }
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.spellIds.size() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/custom_book.png"), x, y, 0.0f, 0.0f, bookWidth, bookHeight, bookWidth, bookHeight);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;
        String currentSpell = this.spellIds.get(this.currentPage);
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0f, 0.0f, 50.0f);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)("textures/gui/spell_" + currentSpell + ".png")), x + 18, y + 20, 0.0f, 0.0f, 48, 48, 48, 48);
        MutableComponent school = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book.school"));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.9f, 0.9f, 1.0f);
        guiGraphics.drawString(this.font, (Component)school, (int)((float)(x + 72) / 0.9f), (int)((float)(y + 25) / 0.9f), 0xFF2E1A0B, false);
        guiGraphics.pose().popPose();
        MutableComponent spellName = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book." + currentSpell + ".name"));
        guiGraphics.drawString(this.font, (Component)spellName, x + 72, y + 42, 0xFF4A2D15, false);
        MutableComponent desc1 = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book." + currentSpell + ".desc1"));
        MutableComponent desc2 = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book." + currentSpell + ".desc2"));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.9f, 0.9f, 1.0f);
        guiGraphics.drawWordWrap(this.font, (net.minecraft.network.chat.FormattedText)desc1, (int)((float)(x + 18) / 0.9f), (int)((float)(y + 85) / 0.9f), (int)(160 / 0.9f), 0xFF1F1710);
        guiGraphics.drawWordWrap(this.font, (net.minecraft.network.chat.FormattedText)desc2, (int)((float)(x + 18) / 0.9f), (int)((float)(y + 100) / 0.9f), (int)(160 / 0.9f), 0xFF1F1710);
        
        com.mushokucraft.data.PlayerMasteryData masteryData = com.mushokucraft.data.PlayerMasteryProvider.get(this.minecraft.player);
        if (masteryData != null) {
            try {
                com.mushokucraft.magic.MagicSchool magicSchool = com.mushokucraft.magic.MagicSchool.valueOf(this.elementPrefix.toUpperCase());
                float masteryPercent = masteryData.getSchoolMastery(magicSchool) * 100.0f;
                MutableComponent masteryText = Component.translatable("gui.mushokucraft.magic_book.school_mastery", String.format(java.util.Locale.US, "%.1f%%", masteryPercent));
                
                guiGraphics.drawString(this.font, masteryText, (int)((float)(x + 18) / 0.9f), (int)((float)(y + 115) / 0.9f), 0xFF2E1A0B, false);
            } catch (Exception e) {}
        }
        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private class CustomButton extends Button {
        public CustomButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/custom_button.png"), this.getX(), this.getY(), 0.0f, 0.0f, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight());
            int color = this.isHovered() ? 0xFF6E4524 : 0xFF4A2D15;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float)this.getX() + (float)this.getWidth() / 2.0f, (float)this.getY() + (float)(this.getHeight() - 8) / 2.0f, 0.0f);
            guiGraphics.pose().scale(1.1f, 1.1f, 1.1f);
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), 0, 0, color);
            guiGraphics.pose().popPose();
        }
    }
}



