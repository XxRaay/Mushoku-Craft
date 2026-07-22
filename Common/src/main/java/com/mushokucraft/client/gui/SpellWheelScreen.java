package com.mushokucraft.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mushokucraft.client.input.ClientSpellState;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.client.input.ModKeybindings;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.network.ToggleToukiPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.architectury.networking.NetworkManager;
import org.lwjgl.glfw.GLFW;

public class SpellWheelScreen
extends Screen {
    private static final ResourceLocation WATERBALL_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_waterball.png");
    private static final ResourceLocation FIREBALL_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_fireball.png");
    private static final ResourceLocation ROCKBULLET_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_rockbullet.png");
    private static final ResourceLocation AIRSTRIKE_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_airstrike.png");
    private static final ResourceLocation SWORD_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/sword_god.png");
    private static final ResourceLocation WATER_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/water_god.png");
    private static final ResourceLocation NORTH_GOD_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/north_god.png");
    private static final ResourceLocation LONGSWORD_LIGHT_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_longsword_light.png");
    private static final ResourceLocation LONGSWORD_SILENCE_ICON = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"textures/gui/spell_longsword_of_silence.png");
    private static final int SLOTS = 8;
    private int hoveredSlot = -1;
    private float animationProgress = 0.0f;

    public SpellWheelScreen() {
        super((Component)Component.literal((String)"Spell Wheel"));
    }

    protected void init() {
        super.init();
        this.animationProgress = 0.0f;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModKeybindings.SPELL_MENU_KEY.matches(keyCode, scanCode)) {
            this.selectAndClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ModKeybindings.SPELL_MENU_KEY.matchesMouse(button)) {
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
        PlayerMasteryData mastery;
        LocalPlayer player = this.minecraft.player;
        boolean hasWaterball = false;
        boolean hasFireball = false;
        boolean hasRockbullet = false;
        boolean hasAirstrike = false;
        boolean hasStance = false;
        boolean hasUltimate = false;
        String ultimateSpellId = null;
        if (player != null && (mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player)) != null) {
            if (mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball")) > 0.0f) {
                hasWaterball = true;
            }
            if (mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball")) > 0.0f) {
                hasFireball = true;
            }
            if (mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "rockbullet")) > 0.0f) {
                hasRockbullet = true;
            }
            if (mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike")) > 0.0f) {
                hasAirstrike = true;
            }
            if (ClientStanceState.selectedStance != null) {
                if (mastery.getStanceMastery(ClientStanceState.selectedStance) >= 0.25f) {
                    hasStance = true;
                }
                if (mastery.getStanceMastery(ClientStanceState.selectedStance) >= 1.0f && ClientStanceState.selectedStance == SwordStyle.SWORD_GOD) {
                    hasUltimate = true;
                    ultimateSpellId = mastery.hasUnlockedLongswordOfSilence() ? "longsword_of_silence" : "longsword_light";
                }
            }
        }
        if (this.hoveredSlot == 0 && hasWaterball) {
            ClientSpellState.selectedSpell = ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball");
        } else if (this.hoveredSlot == 1 && hasFireball) {
            ClientSpellState.selectedSpell = ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball");
        } else if (this.hoveredSlot == 2 && hasRockbullet) {
            ClientSpellState.selectedSpell = ResourceLocation.fromNamespaceAndPath("mushokucraft", "rockbullet");
        } else if (this.hoveredSlot == 3 && hasAirstrike) {
            ClientSpellState.selectedSpell = ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike");
        } else if (this.hoveredSlot == 4 && hasStance) {
            NetworkManager.sendToServer((CustomPacketPayload)new ToggleToukiPacket());
        } else {
            ClientSpellState.selectedSpell = this.hoveredSlot == 5 && hasUltimate ? ResourceLocation.fromNamespaceAndPath("mushokucraft", ultimateSpellId) : null;
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
        PlayerMasteryData mastery;
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
            if ((angle += (sectorAngle = 0.7853981633974483) / 2.0) >= Math.PI * 2) {
                angle -= Math.PI * 2;
            }
            this.hoveredSlot = (int)(angle / sectorAngle);
        } else {
            this.hoveredSlot = -1;
        }
        RenderSystem.enableBlend();
        int bgAlpha = (int)(120.0f * this.animationProgress);
        guiGraphics.fill(0, 0, this.width, this.height, bgAlpha << 24 | 0);
        LocalPlayer player = this.minecraft.player;
        boolean hasWaterball = false;
        boolean hasFireball = false;
        boolean hasRockbullet = false;
        boolean hasAirstrike = false;
        boolean isToukiActive = false;
        boolean hasUltimate = false;
        ResourceLocation ultimateIcon = null;
        String ultimateNameKey = null;
        SwordStyle activeStance = ClientStanceState.selectedStance;
        float waterballMastery = 0.0f;
        float fireballMastery = 0.0f;
        float rockbulletMastery = 0.0f;
        float airstrikeMastery = 0.0f;
        float currentStanceMastery = 0.0f;
        if (player != null && (mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player)) != null) {
            waterballMastery = mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"waterball"));
            if (waterballMastery > 0.0f) {
                hasWaterball = true;
            }
            if ((fireballMastery = mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"fireball"))) > 0.0f) {
                hasFireball = true;
            }
            if ((rockbulletMastery = mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"rockbullet"))) > 0.0f) {
                hasRockbullet = true;
            }
            if ((airstrikeMastery = mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"airstrike"))) > 0.0f) {
                hasAirstrike = true;
            }
            isToukiActive = mastery.isToukiActive();
            if (activeStance != null && (currentStanceMastery = mastery.getStanceMastery(activeStance)) >= 1.0f && activeStance == SwordStyle.SWORD_GOD) {
                hasUltimate = true;
                if (mastery.hasUnlockedLongswordOfSilence()) {
                    ultimateIcon = LONGSWORD_SILENCE_ICON;
                    ultimateNameKey = "spell.mushokucraft.longsword_of_silence";
                } else {
                    ultimateIcon = LONGSWORD_LIGHT_ICON;
                    ultimateNameKey = "spell.mushokucraft.longsword_light";
                }
            }
        }
        boolean hasStanceForTouki = activeStance != null && currentStanceMastery >= 0.25f;
        float radius = 70.0f * this.animationProgress;
        for (int i = 0; i < 8; ++i) {
            int halfIcon;
            int color;
            boolean isHovered = i == this.hoveredSlot;
            double slotAngle = (double)i * 0.7853981633974483 - 1.5707963267948966;
            int x = centerX + (int)(Math.cos(slotAngle) * (double)radius);
            int y = centerY + (int)(Math.sin(slotAngle) * (double)radius);
            int slotSize = isHovered ? 36 : 28;
            int halfSize = slotSize / 2;
            float slotMastery = 0.0f;
            if (i == 0 && hasWaterball) {
                slotMastery = waterballMastery;
            } else if (i == 1 && hasFireball) {
                slotMastery = fireballMastery;
            } else if (i == 2 && hasRockbullet) {
                slotMastery = rockbulletMastery;
            } else if (i == 3 && hasAirstrike) {
                slotMastery = airstrikeMastery;
            } else if (i == 4 && hasStanceForTouki) {
                slotMastery = currentStanceMastery;
            } else if (i == 5 && hasUltimate) {
                slotMastery = 1.0f;
            }
            int n = color = isHovered ? -1426063361 : 0x55AAAAAA;
            if (slotMastery >= 1.0f) {
                color = isHovered ? -855648512 : -1715960309;
            } else if (slotMastery >= 0.5f) {
                int n2 = color = isHovered ? -857677600 : -1716934231;
            }
            if (i == 4 && isToukiActive && hasStanceForTouki && !isHovered) {
                color = slotMastery >= 1.0f ? -570435840 : (slotMastery >= 0.5f ? -572464928 : -1996510720);
            }
            guiGraphics.fill(x - halfSize, y - halfSize, x + halfSize, y + halfSize, color);
            if (i == 0 && hasWaterball) {
                int iconSize = isHovered ? 32 : 24;
                halfIcon = iconSize / 2;
                guiGraphics.blit(WATERBALL_ICON, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            if (i == 1 && hasFireball) {
                int iconSize = isHovered ? 32 : 24;
                halfIcon = iconSize / 2;
                guiGraphics.blit(FIREBALL_ICON, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            if (i == 2 && hasRockbullet) {
                int iconSize = isHovered ? 32 : 24;
                halfIcon = iconSize / 2;
                guiGraphics.blit(ROCKBULLET_ICON, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            if (i == 3 && hasAirstrike) {
                int iconSize = isHovered ? 32 : 24;
                halfIcon = iconSize / 2;
                guiGraphics.blit(AIRSTRIKE_ICON, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            if (i == 4 && hasStanceForTouki) {
                ResourceLocation icon = null;
                if (activeStance == SwordStyle.SWORD_GOD) {
                    icon = SWORD_GOD_ICON;
                } else if (activeStance == SwordStyle.WATER_GOD) {
                    icon = WATER_GOD_ICON;
                } else if (activeStance == SwordStyle.NORTH_GOD) {
                    icon = NORTH_GOD_ICON;
                }
                if (icon == null) continue;
                int iconSize = isHovered ? 32 : 24;
                int halfIcon2 = iconSize / 2;
                guiGraphics.blit(icon, x - halfIcon2, y - halfIcon2, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                continue;
            }
            if (i != 5 || !hasUltimate || ultimateIcon == null) continue;
            int iconSize = isHovered ? 32 : 24;
            halfIcon = iconSize / 2;
            guiGraphics.blit(ultimateIcon, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        }
        if (this.hoveredSlot == 0 && hasWaterball) {
            MutableComponent nameComp = Component.translatable((String)"spell.mushokucraft.waterball");
            String schoolStr = Component.translatable((String)"spell.school.mushokucraft.water").getString() + String.format(" %.0f%%", Float.valueOf(waterballMastery * 100.0f));
            int nameWidth = this.font.width((FormattedText)nameComp);
            int schoolWidth = this.font.width(schoolStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, schoolStr, centerX - schoolWidth / 2, centerY + 2, 49151);
        } else if (this.hoveredSlot == 1 && hasFireball) {
            MutableComponent nameComp = Component.translatable((String)"spell.mushokucraft.fireball");
            String schoolStr = Component.translatable((String)"spell.school.mushokucraft.fire").getString() + String.format(" %.0f%%", Float.valueOf(fireballMastery * 100.0f));
            int nameWidth = this.font.width((FormattedText)nameComp);
            int schoolWidth = this.font.width(schoolStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, schoolStr, centerX - schoolWidth / 2, centerY + 2, 0xFF5555);
        } else if (this.hoveredSlot == 2 && hasRockbullet) {
            MutableComponent nameComp = Component.translatable((String)"spell.mushokucraft.rockbullet");
            String schoolStr = Component.translatable((String)"spell.school.mushokucraft.earth").getString() + String.format(" %.0f%%", Float.valueOf(rockbulletMastery * 100.0f));
            int nameWidth = this.font.width((FormattedText)nameComp);
            int schoolWidth = this.font.width(schoolStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, schoolStr, centerX - schoolWidth / 2, centerY + 2, 0xAA5500);
        } else if (this.hoveredSlot == 3 && hasAirstrike) {
            MutableComponent nameComp = Component.translatable((String)"spell.mushokucraft.airstrike");
            String schoolStr = Component.translatable((String)"spell.school.mushokucraft.wind").getString() + String.format(" %.0f%%", Float.valueOf(airstrikeMastery * 100.0f));
            int nameWidth = this.font.width((FormattedText)nameComp);
            int schoolWidth = this.font.width(schoolStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, schoolStr, centerX - schoolWidth / 2, centerY + 2, 65416);
        } else if (this.hoveredSlot == 4 && hasStanceForTouki) {
            MutableComponent nameComp = Component.translatable((String)"gui.mushokucraft.toggle_touki");
            String stateStr = Component.translatable((String)(isToukiActive ? "gui.mushokucraft.touki_active" : "gui.mushokucraft.touki_inactive")).getString() + String.format(" %.1f%%", Float.valueOf(currentStanceMastery * 100.0f));
            int color = isToukiActive ? 0xFFAA00 : 0xAAAAAA;
            int nameWidth = this.font.width((FormattedText)nameComp);
            int stateWidth = this.font.width(stateStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, stateStr, centerX - stateWidth / 2, centerY + 2, color);
        } else if (this.hoveredSlot == 5 && hasUltimate) {
            MutableComponent nameComp = Component.translatable(ultimateNameKey);
            String schoolStr = Component.translatable((String)"spell.school.mushokucraft.sword_arts").getString();
            int nameWidth = this.font.width((FormattedText)nameComp);
            int schoolWidth = this.font.width(schoolStr);
            guiGraphics.drawString(this.font, nameComp.getString(), centerX - nameWidth / 2, centerY - 10, 0xFFFFFF);
            guiGraphics.drawString(this.font, schoolStr, centerX - schoolWidth / 2, centerY + 2, 0xCCCCCC);
        }
        RenderSystem.disableBlend();
    }

    public boolean isPauseScreen() {
        return false;
    }
}
