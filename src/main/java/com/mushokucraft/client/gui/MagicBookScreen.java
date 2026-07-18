package com.mushokucraft.client.gui;

import com.mushokucraft.network.StartLearnSpellPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class MagicBookScreen
extends Screen {
    private final String elementPrefix;
    private final String spellId;

    public MagicBookScreen(String elementPrefix, String spellId) {
        super((Component)Component.translatable((String)("gui.mushokucraft." + elementPrefix + "_magic_book.title")));
        this.elementPrefix = elementPrefix;
        this.spellId = spellId;
    }

    protected void init() {
        super.init();
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (this.width - bookWidth) / 2;
        int y = (this.height - bookHeight) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mushokucraft." + this.elementPrefix + "_magic_book.learn"), button -> {
            PacketDistributor.sendToServer(new StartLearnSpellPacket(ResourceLocation.fromNamespaceAndPath("mushokucraft", this.spellId)));
            this.minecraft.setScreen(null);
        }).bounds(x + (bookWidth - 120) / 2, y + 185, 120, 50).build());
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
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)("textures/gui/spell_" + this.spellId + ".png")), x + 18, y + 20, 0.0f, 0.0f, 48, 48, 48, 48);
        MutableComponent school = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book.school"));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.9f, 0.9f, 0.9f);
        guiGraphics.drawString(this.font, (Component)school, (int)((float)(x + 72) / 0.9f), (int)((float)(y + 25) / 0.9f), 3021323, false);
        guiGraphics.pose().popPose();
        MutableComponent spellName = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book.spell_name"));
        guiGraphics.drawString(this.font, (Component)spellName, x + 72, y + 42, 4861461, false);
        MutableComponent desc1 = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book.desc1"));
        MutableComponent desc2 = Component.translatable((String)("gui.mushokucraft." + this.elementPrefix + "_magic_book.desc2"));
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.9f, 0.9f, 0.9f);
        guiGraphics.drawString(this.font, (Component)desc1, (int)((float)(x + 18) / 0.9f), (int)((float)(y + 85) / 0.9f), 2037520, false);
        guiGraphics.drawString(this.font, (Component)desc2, (int)((float)(x + 18) / 0.9f), (int)((float)(y + 98) / 0.9f), 2037520, false);
        guiGraphics.pose().popPose();
    }

    public boolean isPauseScreen() {
        return false;
    }
}


