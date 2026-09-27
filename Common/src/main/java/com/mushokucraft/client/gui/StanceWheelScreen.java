package com.mushokucraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.network.ChangeStancePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.architectury.networking.NetworkManager;
import org.lwjgl.glfw.GLFW;

public class StanceWheelScreen extends Screen {
    private static final ResourceLocation SWORD_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/sword_god.png");
    private static final ResourceLocation WATER_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/water_god.png");
    private static final ResourceLocation NORTH_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/north_god.png");
    private static final int SLOTS = 3;
    private int hoveredSlot = -1;
    private float animationProgress = 0.0f;

    public StanceWheelScreen() {
        super(Component.literal("Stance Wheel"));
    }

    protected void init() {
        super.init();
        this.animationProgress = 0.0f;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModKeybindings.STANCE_MENU_KEY.matches(keyCode, scanCode)) {
            this.selectAndClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ModKeybindings.STANCE_MENU_KEY.matchesMouse(button)) {
            this.selectAndClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public void tick() {
        super.tick();
        if (this.minecraft == null) {
            return;
        }
        if (this.animationProgress < 1.0f) {
            this.animationProgress += 0.15f;
            if (this.animationProgress > 1.0f) {
                this.animationProgress = 1.0f;
            }
        }
    }

    private void selectAndClose() {
        SwordStyle newStance = null;
        if (this.hoveredSlot == 0) {
            newStance = SwordStyle.SWORD_GOD;
        } else if (this.hoveredSlot == 1) {
            newStance = SwordStyle.WATER_GOD;
        } else if (this.hoveredSlot == 2) {
            newStance = SwordStyle.NORTH_GOD;
        }
        
        PlayerMasteryData mastery = null;
        if (this.minecraft != null && this.minecraft.player != null) {
            mastery = (PlayerMasteryData)PlayerMasteryProvider.get(this.minecraft.player);
        }
        
        if (newStance != null && mastery != null && !mastery.isStyleUnlocked(newStance)) {
            newStance = ClientStanceState.selectedStance;
        }
        
        if (newStance != ClientStanceState.selectedStance && newStance != null) {
            ClientStanceState.selectedStance = newStance;
            if (mastery != null) {
                mastery.setActiveStance(newStance);
            }
            NetworkManager.sendToServer((CustomPacketPayload)new ChangeStancePacket(newStance.getId()));
        }
        
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.selectAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        double sectorAngle = Math.PI * 2.0 / 3.0; // 120 degrees
        
        if (distance > 20.0 && distance < 150.0) {
            double angle = Math.atan2(dy, dx) + Math.PI / 2.0;
            if (angle < 0.0) {
                angle += Math.PI * 2.0;
            }
            angle += sectorAngle / 2.0;
            if (angle >= Math.PI * 2.0) {
                angle -= Math.PI * 2.0;
            }
            this.hoveredSlot = (int)(angle / sectorAngle);
        } else {
            this.hoveredSlot = -1;
        }
        
        RenderSystem.enableBlend();
        int bgAlpha = (int)(120.0f * this.animationProgress);
        guiGraphics.fill(0, 0, this.width, this.height, bgAlpha << 24 | 0);
        
        float swordGodMastery = 0.0f;
        float waterGodMastery = 0.0f;
        float northGodMastery = 0.0f;
        
        PlayerMasteryData mastery = null;
        if (this.minecraft != null && this.minecraft.player != null && (mastery = (PlayerMasteryData)PlayerMasteryProvider.get(this.minecraft.player)) != null) {
            swordGodMastery = mastery.getStanceMastery(SwordStyle.SWORD_GOD);
            waterGodMastery = mastery.getStanceMastery(SwordStyle.WATER_GOD);
            northGodMastery = mastery.getStanceMastery(SwordStyle.NORTH_GOD);
        }
        
        float radius = 70.0f * this.animationProgress;
        for (int i = 0; i < 3; ++i) {
            boolean isUnlocked = true;
            if (mastery != null) {
                if (i == 0) isUnlocked = mastery.isStyleUnlocked(SwordStyle.SWORD_GOD);
                else if (i == 1) isUnlocked = mastery.isStyleUnlocked(SwordStyle.WATER_GOD);
                else if (i == 2) isUnlocked = mastery.isStyleUnlocked(SwordStyle.NORTH_GOD);
            }
            
            boolean isHovered = i == this.hoveredSlot && isUnlocked;
            double slotAngle = (double)i * sectorAngle - Math.PI / 2.0;
            int x = centerX + (int)(Math.cos(slotAngle) * (double)radius);
            int y = centerY + (int)(Math.sin(slotAngle) * (double)radius);
            
            int slotSize = isHovered ? 56 : 44; // smooth larger pop
            int halfSize = slotSize / 2;
            
            float slotMastery = 0.0f;
            if (i == 0) slotMastery = swordGodMastery;
            else if (i == 1) slotMastery = waterGodMastery;
            else if (i == 2) slotMastery = northGodMastery;
            
            int color = isHovered ? 0xAA222222 : 0x55AAAAAA;
            if (!isUnlocked) {
                color = 0x55222222;
            } else if (slotMastery >= 1.0f) {
                color = isHovered ? 0xDDFFCC00 : 0x99FFAA00; // Gold for max mastery
            } else if (slotMastery >= 0.5f) {
                color = isHovered ? 0xDDAAEEFF : 0x9988CCFF; // Silver for 50%+
            }
            
            guiGraphics.fill(x - halfSize, y - halfSize, x + halfSize, y + halfSize, color);
            
            if (isUnlocked) {
                ResourceLocation icon = null;
                if (i == 0) icon = SWORD_GOD_ICON;
                else if (i == 1) icon = WATER_GOD_ICON;
                else if (i == 2) icon = NORTH_GOD_ICON;
                
                if (icon != null) {
                    int iconSize = isHovered ? 48 : 36;
                    int halfIcon = iconSize / 2;
                    guiGraphics.blit(icon, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                }
            } else {
                guiGraphics.drawString(this.font, "?", x - this.font.width("?") / 2, y - 4, 0x555555);
            }
        }
        
        String name = "";
        String style = "Sword Style";
        int nameColor = 0xFFFFFF;
        
        boolean hoveredIsUnlocked = true;
        if (mastery != null && this.hoveredSlot != -1) {
            if (this.hoveredSlot == 0) hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.SWORD_GOD);
            else if (this.hoveredSlot == 1) hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.WATER_GOD);
            else if (this.hoveredSlot == 2) hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.NORTH_GOD);
        }
        
        if (this.hoveredSlot != -1) {
            if (hoveredIsUnlocked) {
                if (this.hoveredSlot == 0) {
                    name = "Sword God Style";
                    style = String.format("Sword Style %.0f%%", swordGodMastery * 100.0f);
                    nameColor = 0xFF4444;
                } else if (this.hoveredSlot == 1) {
                    name = "Water God Style";
                    style = String.format("Sword Style %.0f%%", waterGodMastery * 100.0f);
                    nameColor = 0x4488FF;
                } else if (this.hoveredSlot == 2) {
                    name = "North God Style";
                    style = String.format("Sword Style %.0f%%", northGodMastery * 100.0f);
                    nameColor = 0xAA44FF;
                }
            } else {
                name = "Locked Style";
                style = "Requires Advancement";
                nameColor = 0x777777;
            }
        }
        
        if (!name.isEmpty()) {
            int nameWidth = this.font.width(name);
            int styleWidth = this.font.width(style);
            guiGraphics.drawString(this.font, name, centerX - nameWidth / 2, centerY - 10, nameColor);
            guiGraphics.drawString(this.font, style, centerX - styleWidth / 2, centerY + 2, 0xAAAAAA);
        }
        
        RenderSystem.disableBlend();
    }

    public boolean isPauseScreen() {
        return false;
    }
}
