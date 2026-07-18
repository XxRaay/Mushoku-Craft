/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.lwjgl.glfw.GLFW
 */
package com.mushokucraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.network.ChangeStancePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class StanceWheelScreen
extends Screen {
    private static final ResourceLocation SWORD_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/sword_god.png");
    private static final ResourceLocation WATER_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/water_god.png");
    private static final ResourceLocation NORTH_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/north_god.png");
    private static final int SLOTS = 3;
    private int hoveredSlot = -1;
    private float animationProgress = 0.0f;

    public StanceWheelScreen() {
        super((Component)Component.literal((String)"Stance Wheel"));
    }

    protected void init() {
        super.init();
        this.animationProgress = 0.0f;
    }

    public void tick() {
        super.tick();
        if (this.minecraft == null) {
            return;
        }
        long window = this.minecraft.getWindow().getWindow();
        InputConstants.Key key = ModKeybindings.STANCE_MENU_KEY.getKey();
        boolean isPressed = false;
        isPressed = key.getType() == InputConstants.Type.MOUSE ? GLFW.glfwGetMouseButton((long)window, (int)key.getValue()) == 1 : InputConstants.isKeyDown((long)window, (int)key.getValue());
        if (!isPressed) {
            this.selectAndClose();
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
            mastery = (PlayerMasteryData)this.minecraft.player.getData(ModAttachments.PLAYER_MASTERY);
        }
        if (newStance != null && mastery != null && !mastery.isStyleUnlocked(newStance)) {
            newStance = ClientStanceState.selectedStance;
        }
        if (newStance != ClientStanceState.selectedStance) {
            ClientStanceState.selectedStance = newStance;
            if (mastery != null) {
                mastery.setActiveStance(newStance);
            }
            PacketDistributor.sendToServer((CustomPacketPayload)new ChangeStancePacket(newStance == null ? "none" : newStance.getId()), (CustomPacketPayload[])new CustomPacketPayload[0]);
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
        if (distance > 20.0) {
            double sectorAngle;
            double angle = Math.atan2(dy, dx) + 1.5707963267948966;
            if (angle < 0.0) {
                angle += Math.PI * 2;
            }
            if ((angle += (sectorAngle = 2.0943951023931953) / 2.0) >= Math.PI * 2) {
                angle -= Math.PI * 2;
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
        if (this.minecraft != null && this.minecraft.player != null && (mastery = (PlayerMasteryData)this.minecraft.player.getData(ModAttachments.PLAYER_MASTERY)) != null) {
            swordGodMastery = mastery.getStanceMastery(SwordStyle.SWORD_GOD);
            waterGodMastery = mastery.getStanceMastery(SwordStyle.WATER_GOD);
            northGodMastery = mastery.getStanceMastery(SwordStyle.NORTH_GOD);
        }
        float radius = 70.0f * this.animationProgress;
        for (int i = 0; i < 3; ++i) {
            int color;
            boolean isUnlocked = true;
            if (mastery != null) {
                if (i == 0) {
                    isUnlocked = mastery.isStyleUnlocked(SwordStyle.SWORD_GOD);
                } else if (i == 1) {
                    isUnlocked = mastery.isStyleUnlocked(SwordStyle.WATER_GOD);
                } else if (i == 2) {
                    isUnlocked = mastery.isStyleUnlocked(SwordStyle.NORTH_GOD);
                }
            }
            boolean isHovered = i == this.hoveredSlot && isUnlocked;
            double slotAngle = (double)i * 2.0943951023931953 - 1.5707963267948966;
            int x = centerX + (int)(Math.cos(slotAngle) * (double)radius);
            int y = centerY + (int)(Math.sin(slotAngle) * (double)radius);
            int slotSize = isHovered ? 48 : 36;
            int halfSize = slotSize / 2;
            float slotMastery = 0.0f;
            if (i == 0) {
                slotMastery = swordGodMastery;
            } else if (i == 1) {
                slotMastery = waterGodMastery;
            } else if (i == 2) {
                slotMastery = northGodMastery;
            }
            int n = color = isHovered ? -1426063361 : 0x55AAAAAA;
            if (!isUnlocked) {
                color = 0x55222222;
            } else if (slotMastery >= 1.0f) {
                color = isHovered ? -855648512 : -1715960309;
            } else if (slotMastery >= 0.5f) {
                color = isHovered ? -857677600 : -1716934231;
            }
            guiGraphics.fill(x - halfSize, y - halfSize, x + halfSize, y + halfSize, color);
            if (isUnlocked) {
                ResourceLocation icon = null;
                if (i == 0) {
                    icon = SWORD_GOD_ICON;
                } else if (i == 1) {
                    icon = WATER_GOD_ICON;
                } else if (i == 2) {
                    icon = NORTH_GOD_ICON;
                }
                if (icon == null) continue;
                int iconSize = isHovered ? 44 : 32;
                int halfIcon = iconSize / 2;
                guiGraphics.blit(icon, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            guiGraphics.drawString(this.font, "?", x - this.font.width("?") / 2, y - 4, 0x555555);
        }
        String name = "";
        String style = "Sword Style";
        int nameColor = 0xFFFFFF;
        boolean hoveredIsUnlocked = true;
        if (mastery != null) {
            if (this.hoveredSlot == 0) {
                hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.SWORD_GOD);
            } else if (this.hoveredSlot == 1) {
                hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.WATER_GOD);
            } else if (this.hoveredSlot == 2) {
                hoveredIsUnlocked = mastery.isStyleUnlocked(SwordStyle.NORTH_GOD);
            }
        }
        if (hoveredIsUnlocked) {
            if (this.hoveredSlot == 0) {
                name = "Sword God Style";
                style = String.format("Sword Style %.0f%%", Float.valueOf(swordGodMastery * 100.0f));
                nameColor = 0xFF4444;
            } else if (this.hoveredSlot == 1) {
                name = "Water God Style";
                style = String.format("Sword Style %.0f%%", Float.valueOf(waterGodMastery * 100.0f));
                nameColor = 0x4488FF;
            } else if (this.hoveredSlot == 2) {
                name = "North God Style";
                style = String.format("Sword Style %.0f%%", Float.valueOf(northGodMastery * 100.0f));
                nameColor = 0xAA44FF;
            }
        } else if (this.hoveredSlot != -1) {
            name = "Locked Style";
            style = "Requires Advancement";
            nameColor = 0x777777;
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

