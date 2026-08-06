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

import java.util.ArrayList;
import java.util.List;

public class SpellWheelScreen extends Screen {
    private static final ResourceLocation WATERBALL_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_waterball.png");
    private static final ResourceLocation WATER_SLICE_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_water_slice.png");
    private static final ResourceLocation ICICLE_BREAK_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_icicle_break.png");
    private static final ResourceLocation CUMULONIMBUS_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_cumulonimbus.png");
    private static final ResourceLocation FIREBALL_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_fireball.png");
    private static final ResourceLocation ROCKBULLET_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_rockbullet.png");
    private static final ResourceLocation AIRSTRIKE_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_airstrike.png");
    private static final ResourceLocation SWORD_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/sword_god.png");
    private static final ResourceLocation WATER_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/water_god.png");
    private static final ResourceLocation NORTH_GOD_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/north_god.png");
    private static final ResourceLocation LONGSWORD_LIGHT_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_longsword_light.png");
    private static final ResourceLocation LONGSWORD_SILENCE_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/spell_longsword_of_silence.png");

    private static final ResourceLocation SCHOOL_WATER_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/school_water.png");
    private static final ResourceLocation SCHOOL_FIRE_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/school_fire.png");
    private static final ResourceLocation SCHOOL_EARTH_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/school_earth.png");
    private static final ResourceLocation SCHOOL_WIND_ICON = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/school_wind.png");

    private static final int SLOTS = 8;
    private int hoveredMainSlot = -1;
    private int hoveredSubSlot = -1;
    private int activeCategorySlot = -1;
    private float animationProgress = 0.0f;
    private List<WheelSlot> slots = new ArrayList<>();

    public SpellWheelScreen() {
        super(Component.literal("Spell Wheel"));
    }

    protected void init() {
        super.init();
        this.animationProgress = 0.0f;
    }

    private void buildSlots() {
        this.slots.clear();
        PlayerMasteryData mastery = null;
        LocalPlayer player = this.minecraft != null ? this.minecraft.player : null;
        if (player != null) {
            mastery = (PlayerMasteryData) PlayerMasteryProvider.get(player);
        }

        WheelSlot waterSlot = new WheelSlot(true, SCHOOL_WATER_ICON, Component.translatable("spell.school.mushokucraft.water"));
        float waterballMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball")) : 0f;
        float waterSliceMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "water_slice")) : 0f;
        float icicleBreakMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "icicle_break")) : 0f;
        float cumulonimbusMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "cumulonimbus")) : 0f;
        waterSlot.mastery = Math.max(Math.max(Math.max(waterballMastery, waterSliceMastery), icicleBreakMastery), cumulonimbusMastery);
        waterSlot.isUnlocked = true;
        waterSlot.descColor = 0x4BFFFF;
        waterSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "waterball"), WATERBALL_ICON, Component.translatable("spell.mushokucraft.waterball"), waterballMastery, waterballMastery > 0f));
        waterSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "water_slice"), WATER_SLICE_ICON, Component.translatable("spell.mushokucraft.water_slice"), waterSliceMastery, waterSliceMastery > 0f));
        waterSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "icicle_break"), ICICLE_BREAK_ICON, Component.translatable("spell.mushokucraft.icicle_break"), icicleBreakMastery, icicleBreakMastery > 0f));
        waterSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "cumulonimbus"), CUMULONIMBUS_ICON, Component.translatable("spell.mushokucraft.cumulonimbus"), cumulonimbusMastery, cumulonimbusMastery > 0f));
        this.slots.add(waterSlot);

        WheelSlot fireSlot = new WheelSlot(true, SCHOOL_FIRE_ICON, Component.translatable("spell.school.mushokucraft.fire"));
        float fireMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball")) : 0f;
        fireSlot.mastery = fireMastery;
        fireSlot.isUnlocked = true;
        fireSlot.descColor = 0xFF5555;
        fireSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "fireball"), FIREBALL_ICON, Component.translatable("spell.mushokucraft.fireball"), fireMastery, fireMastery > 0f));
        this.slots.add(fireSlot);

        WheelSlot earthSlot = new WheelSlot(true, SCHOOL_EARTH_ICON, Component.translatable("spell.school.mushokucraft.earth"));
        float earthMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "rockbullet")) : 0f;
        earthSlot.mastery = earthMastery;
        earthSlot.isUnlocked = true;
        earthSlot.descColor = 0xAA5500;
        earthSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "rockbullet"), ROCKBULLET_ICON, Component.translatable("spell.mushokucraft.rockbullet"), earthMastery, earthMastery > 0f));
        this.slots.add(earthSlot);

        WheelSlot windSlot = new WheelSlot(true, SCHOOL_WIND_ICON, Component.translatable("spell.school.mushokucraft.wind"));
        float windMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike")) : 0f;
        windSlot.mastery = windMastery;
        windSlot.isUnlocked = true;
        windSlot.descColor = 0x44FF44;
        windSlot.subSlots.add(new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "airstrike"), AIRSTRIKE_ICON, Component.translatable("spell.mushokucraft.airstrike"), windMastery, windMastery > 0f));
        this.slots.add(windSlot);

        WheelSlot healingSlot = new WheelSlot(true, WATERBALL_ICON, Component.translatable("spell.school.mushokucraft.healing"));
        healingSlot.isUnlocked = false;
        healingSlot.descColor = 0xFF55FF;
        this.slots.add(healingSlot);

        WheelSlot emptySlot = new WheelSlot(true, WATERBALL_ICON, Component.literal("Unknown"));
        emptySlot.isUnlocked = false;
        emptySlot.descColor = 0x555555;
        this.slots.add(emptySlot);
        
        ResourceLocation airCushionIcon = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/spell/air_cushion.png");
        float airCushionMastery = mastery != null ? mastery.getSpellMastery(ResourceLocation.fromNamespaceAndPath("mushokucraft", "air_cushion")) : 0f;
        boolean hasAirCushion = airCushionMastery > 0f;
        boolean isAirCushionActive = mastery != null && mastery.isAirCushionActive();

        SubSlot airCushionSub = new SubSlot(ResourceLocation.fromNamespaceAndPath("mushokucraft", "air_cushion"), airCushionIcon, Component.translatable("gui.mushokucraft.wind_magic_book.air_cushion.name"), airCushionMastery, hasAirCushion);
        airCushionSub.action = () -> {
            NetworkManager.sendToServer((CustomPacketPayload)new com.mushokucraft.network.ToggleAirCushionPacket());
        };
        airCushionSub.isActive = isAirCushionActive;
        windSlot.subSlots.add(airCushionSub);

        SwordStyle activeStance = ClientStanceState.selectedStance;
        float currentStanceMastery = (mastery != null && activeStance != null) ? mastery.getStanceMastery(activeStance) : 0f;
        boolean hasStanceForTouki = activeStance != null && currentStanceMastery >= 0.25f;
        boolean isToukiActive = mastery != null && mastery.isToukiActive();
        
        ResourceLocation stanceIcon = WATERBALL_ICON;
        if (activeStance == SwordStyle.SWORD_GOD) stanceIcon = SWORD_GOD_ICON;
        else if (activeStance == SwordStyle.WATER_GOD) stanceIcon = WATER_GOD_ICON;
        else if (activeStance == SwordStyle.NORTH_GOD) stanceIcon = NORTH_GOD_ICON;
        
        WheelSlot toukiSlot = new WheelSlot(false, stanceIcon, Component.translatable("gui.mushokucraft.toggle_touki"));
        toukiSlot.description = Component.translatable(isToukiActive ? "gui.mushokucraft.touki_active" : "gui.mushokucraft.touki_inactive");
        toukiSlot.mastery = currentStanceMastery;
        toukiSlot.isUnlocked = hasStanceForTouki;
        toukiSlot.descColor = isToukiActive ? 0xFFAA00 : 0xAAAAAA;
        toukiSlot.action = () -> {
            NetworkManager.sendToServer((CustomPacketPayload)new ToggleToukiPacket());
        };
        this.slots.add(toukiSlot);

        boolean hasUltimate = false;
        ResourceLocation ultimateIcon = LONGSWORD_LIGHT_ICON;
        String ultKey = "spell.mushokucraft.longsword_light";
        if (activeStance != null && currentStanceMastery >= 1.0f && activeStance == SwordStyle.SWORD_GOD) {
            hasUltimate = true;
            if (mastery != null && mastery.hasUnlockedLongswordOfSilence()) {
                ultimateIcon = LONGSWORD_SILENCE_ICON;
                ultKey = "spell.mushokucraft.longsword_of_silence";
            }
        }
        WheelSlot ultSlot = new WheelSlot(false, ultimateIcon, Component.translatable(ultKey));
        ultSlot.description = Component.translatable("spell.school.mushokucraft.sword_arts");
        ultSlot.isUnlocked = hasUltimate;
        ultSlot.mastery = hasUltimate ? 1.0f : 0f;
        ultSlot.descColor = 0xCCCCCC;
        String finalUltKey = ultKey;
        ultSlot.action = () -> {
            ClientSpellState.selectedSpell = ResourceLocation.fromNamespaceAndPath("mushokucraft", finalUltKey.replace("spell.mushokucraft.", ""));
        };
        this.slots.add(ultSlot);
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
        if (this.slots.isEmpty()) {
            this.buildSlots();
        }

        if (this.hoveredSubSlot != -1 && this.activeCategorySlot != -1) {
            WheelSlot catSlot = this.slots.get(this.activeCategorySlot);
            if (this.hoveredSubSlot < catSlot.subSlots.size()) {
                SubSlot sub = catSlot.subSlots.get(this.hoveredSubSlot);
                if (sub.isUnlocked) {
                    if (sub.action != null) {
                        sub.action.run();
                    } else {
                        ClientSpellState.selectedSpell = sub.spellId;
                    }
                }
            }
        } else if (this.hoveredMainSlot != -1) {
            WheelSlot mSlot = this.slots.get(this.hoveredMainSlot);
            if (!mSlot.isCategory && mSlot.isUnlocked && mSlot.action != null) {
                mSlot.action.run();
            }
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
        
        this.buildSlots();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double sectorAngle = Math.PI / 4.0;
        double rawAngle = Math.atan2(dy, dx) + Math.PI / 2.0;
        if (rawAngle < 0.0) rawAngle += Math.PI * 2.0;
        double adjustedAngle = rawAngle + sectorAngle / 2.0;
        if (adjustedAngle >= Math.PI * 2.0) adjustedAngle -= Math.PI * 2.0;
        int calculatedMainSlot = (int)(adjustedAngle / sectorAngle);

        if (distance < 20.0) {
            this.hoveredMainSlot = -1;
            this.hoveredSubSlot = -1;
            this.activeCategorySlot = -1;
        } else {
            int currentGeoSlot = calculatedMainSlot;
            boolean inSubMenuBand = distance >= 80.0 && distance < 160.0;
            
            if (distance < 80.0) {
                this.hoveredMainSlot = currentGeoSlot;
                if (this.slots.get(this.hoveredMainSlot).isCategory) {
                    this.activeCategorySlot = this.hoveredMainSlot;
                } else {
                    this.activeCategorySlot = -1;
                }
                this.hoveredSubSlot = -1;
            } else if (inSubMenuBand) {
                if (this.activeCategorySlot == -1) {
                    if (this.slots.get(currentGeoSlot).isCategory) {
                        this.activeCategorySlot = currentGeoSlot;
                    }
                }
                
                if (this.activeCategorySlot != -1) {
                    WheelSlot activeSlot = this.slots.get(this.activeCategorySlot);
                    int numSub = activeSlot.subSlots.size();
                    this.hoveredSubSlot = -1;
                    if (numSub > 0) {
                        double slotCenterAngle = this.activeCategorySlot * sectorAngle - Math.PI / 2.0;
                        double subSectorAngle = Math.PI / 8.0;
                        double startAngle = slotCenterAngle - (subSectorAngle * (numSub - 1)) / 2.0;
                        double absAngle = Math.atan2(dy, dx);
                        
                        for (int i = 0; i < numSub; i++) {
                            double subAngle = startAngle + i * subSectorAngle;
                            double diff = Math.abs(Math.atan2(Math.sin(absAngle - subAngle), Math.cos(absAngle - subAngle)));
                            if (diff < subSectorAngle / 2.0) {
                                this.hoveredSubSlot = i;
                                break;
                            }
                        }
                    }
                    this.hoveredMainSlot = this.activeCategorySlot;
                } else {
                    this.hoveredMainSlot = currentGeoSlot;
                    this.hoveredSubSlot = -1;
                }
            } else {
                this.hoveredMainSlot = -1;
                this.hoveredSubSlot = -1;
                this.activeCategorySlot = -1;
            }
        }

        RenderSystem.enableBlend();
        int bgAlpha = (int)(120.0f * this.animationProgress);
        guiGraphics.fill(0, 0, this.width, this.height, bgAlpha << 24 | 0);

        float mainRadius = 55.0f * this.animationProgress;
        for (int i = 0; i < 8; ++i) {
            WheelSlot slot = this.slots.get(i);
            boolean isHovered = i == this.hoveredMainSlot;
            double slotAngle = (double)i * sectorAngle - Math.PI / 2.0;
            int x = centerX + (int)(Math.cos(slotAngle) * (double)mainRadius);
            int y = centerY + (int)(Math.sin(slotAngle) * (double)mainRadius);
            
            int slotSize = isHovered ? 40 : 32;
            int halfSize = slotSize / 2;
            
            int color = isHovered ? 0xAA222222 : 0x55AAAAAA;
            if (slot.isUnlocked) {
                if (!slot.isCategory) {
                    if (slot.mastery >= 1.0f) {
                        color = isHovered ? 0xDDFFCC00 : 0x99FFAA00;
                    } else if (slot.mastery >= 0.5f) {
                        color = isHovered ? 0xDDAAEEFF : 0x9988CCFF;
                    }
                }
            } else {
                color = 0x55222222;
            }
            guiGraphics.fill(x - halfSize, y - halfSize, x + halfSize, y + halfSize, color);
            
            if (slot.isUnlocked) {
                int iconSize = isHovered ? 32 : 24;
                int halfIcon = iconSize / 2;
                guiGraphics.blit(slot.icon, x - halfIcon, y - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
            } else {
                guiGraphics.drawString(this.font, "?", x - this.font.width("?") / 2, y - 4, 0x555555);
            }
        }

        if (this.activeCategorySlot != -1 && this.animationProgress > 0.5f) {
            float subAnim = (this.animationProgress - 0.5f) * 2.0f;
            float subRadius = 110.0f * subAnim;
            WheelSlot activeSlot = this.slots.get(this.activeCategorySlot);
            int numSub = activeSlot.subSlots.size();
            double slotCenterAngle = this.activeCategorySlot * sectorAngle - Math.PI / 2.0;
            double subSectorAngle = Math.PI / 8.0;
            double startAngle = slotCenterAngle - (subSectorAngle * (numSub - 1)) / 2.0;
            
            for (int i = 0; i < numSub; i++) {
                SubSlot sub = activeSlot.subSlots.get(i);
                boolean isHovered = i == this.hoveredSubSlot && sub.isUnlocked;
                double subAngle = startAngle + i * subSectorAngle;
                int subX = centerX + (int)(Math.cos(subAngle) * subRadius);
                int subY = centerY + (int)(Math.sin(subAngle) * subRadius);
                
                int slotSize = isHovered ? 36 : 28;
                int halfSize = slotSize / 2;
                
                int color = isHovered ? 0xAA222222 : 0x55AAAAAA;
                if (sub.isUnlocked) {
                    if (sub.isActive) {
                        color = isHovered ? 0xDD44FF44 : 0x9922CC22;
                    } else if (sub.mastery >= 1.0f) {
                        color = isHovered ? 0xDDFFCC00 : 0x99FFAA00;
                    } else if (sub.mastery >= 0.5f) {
                        color = isHovered ? 0xDDAAEEFF : 0x9988CCFF;
                    }
                } else {
                    color = 0x55222222;
                }
                
                guiGraphics.fill(subX - halfSize, subY - halfSize, subX + halfSize, subY + halfSize, color);
                
                int iconSize = isHovered ? 28 : 20;
                int halfIcon = iconSize / 2;
                if (!sub.isUnlocked) {
                    RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 0.8f);
                }
                guiGraphics.blit(sub.icon, subX - halfIcon, subY - halfIcon, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
                if (!sub.isUnlocked) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }

        String name = "";
        String desc = "";
        int nameColor = 0xFFFFFF;
        int descColor = 0xAAAAAA;
        
        if (this.hoveredSubSlot != -1 && this.activeCategorySlot != -1) {
            SubSlot sub = this.slots.get(this.activeCategorySlot).subSlots.get(this.hoveredSubSlot);
            if (sub.isUnlocked) {
                name = sub.name.getString();
                desc = String.format("Mastery %.0f%%", sub.mastery * 100.0f);
                descColor = this.slots.get(this.activeCategorySlot).descColor;
            } else {
                name = "???";
                desc = "Spell Locked";
                nameColor = 0x777777;
            }
        } else if (this.hoveredMainSlot != -1) {
            WheelSlot mSlot = this.slots.get(this.hoveredMainSlot);
            name = mSlot.name.getString();
            if (mSlot.isUnlocked) {
                if (mSlot.description != null) {
                    desc = mSlot.description.getString();
                } else if (mSlot.isCategory) {
                    desc = "Magic School";
                }
                descColor = mSlot.descColor;
            } else {
                name = "???";
                desc = "Requires Advancement";
                nameColor = 0x777777;
            }
        }

        if (!name.isEmpty()) {
            int nameWidth = this.font.width(name);
            int descWidth = this.font.width(desc);
            guiGraphics.drawString(this.font, name, centerX - nameWidth / 2, centerY - 10, nameColor);
            guiGraphics.drawString(this.font, desc, centerX - descWidth / 2, centerY + 2, descColor);
        }

        RenderSystem.disableBlend();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class WheelSlot {
        boolean isCategory;
        ResourceLocation icon;
        Component name;
        Component description;
        int descColor;
        boolean isUnlocked;
        float mastery;
        List<SubSlot> subSlots = new ArrayList<>();
        Runnable action;

        public WheelSlot(boolean isCategory, ResourceLocation icon, Component name) {
            this.isCategory = isCategory;
            this.icon = icon;
            this.name = name;
        }
    }

    private static class SubSlot {
        ResourceLocation spellId;
        ResourceLocation icon;
        Component name;
        float mastery;
        boolean isUnlocked;
        Runnable action;
        boolean isActive;

        public SubSlot(ResourceLocation spellId, ResourceLocation icon, Component name, float mastery, boolean isUnlocked) {
            this.spellId = spellId;
            this.icon = icon;
            this.name = name;
            this.mastery = mastery;
            this.isUnlocked = isUnlocked;
        }
    }
}
