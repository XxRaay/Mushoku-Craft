package com.mushokucraft.client.gui;

import com.mushokucraft.accessory.AccessoryHelper;
import com.mushokucraft.client.gui.widget.FantasyButton;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.crafting.ModularAnvilMenu;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.network.ForgeWeaponPacket;
import com.mushokucraft.weapon.modular.ModularWeaponItem;
import com.mushokucraft.weapon.modular.WeaponCoreRegistry;
import com.mushokucraft.weapon.modular.WeaponCoreType;
import com.mushokucraft.weapon.modular.WeaponForm;
import com.mushokucraft.weapon.modular.WeaponMaterial;
import com.mushokucraft.weapon.modular.WeaponMaterialRegistry;
import com.mushokucraft.weapon.modular.WeaponQuality;
import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModularAnvilScreen extends AbstractContainerScreen<ModularAnvilMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("mushokucraft", "textures/gui/modular_anvil_gui.png");

    public enum ForgingState {
        IDLE,
        STRIKE_1,
        STRIKE_2,
        STRIKE_3,
        STRIKE_4,
        EVALUATING,
        COMPLETED
    }

    private static class SparkParticle {
        float x, y;
        float vx, vy;
        int color;
        int life;
        int maxLife;

        SparkParticle(float x, float y, float vx, float vy, int color, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = maxLife;
            this.maxLife = maxLife;
        }
    }

    private WeaponForm selectedForm = WeaponForm.SWORD;
    private EditBox nameField;
    private FantasyButton forgeButton;
    private FantasyButton prevButton;
    private FantasyButton nextButton;

    // Mini-game state
    private ForgingState forgingState = ForgingState.IDLE;
    private float totalScore = 0.0f;
    private int currentStrike = 1;
    private float strikeOscillation = 0.0f;
    private float sweetSpotCenter = 49.0f;
    private float sweetSpotWidth = 10.0f;
    private float goodZoneWidth = 30.0f;
    private float strikeSpeed = 2.6f;
    private int heatTicks = 80;
    private int evaluationTicks = 0;
    private Component lastFeedbackText = null;
    private int feedbackColor = 0xFFFFFF;
    private WeaponQuality finalQuality = null;

    private final List<SparkParticle> sparks = new ArrayList<>();
    private float tickCount = 0;

    public ModularAnvilScreen(ModularAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 222;
        this.imageHeight = 208;
        this.inventoryLabelX = 30;
        this.inventoryLabelY = 112;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        // 1. Previous Form Button (<)
        this.prevButton = new FantasyButton(this.leftPos + 48, this.topPos + 18, 12, 13, Component.literal("<"), false, btn -> {
            if (forgingState != ForgingState.IDLE) return;
            this.selectedForm = this.selectedForm.previous();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.2f);
            }
        });
        this.addRenderableWidget(this.prevButton);

        // 2. Next Form Button (>)
        this.nextButton = new FantasyButton(this.leftPos + 143, this.topPos + 18, 12, 13, Component.literal(">"), false, btn -> {
            if (forgingState != ForgingState.IDLE) return;
            this.selectedForm = this.selectedForm.next();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.2f);
            }
        });
        this.addRenderableWidget(this.nextButton);

        // 3. Artifact Name Text Field
        this.nameField = new EditBox(this.font, this.leftPos + 48, this.topPos + 34, 106, 11, Component.translatable("gui.mushokucraft.modular_anvil.name_placeholder"));
        this.nameField.setCanLoseFocus(true);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setTextColorUneditable(0x888888);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(36);
        this.nameField.setHint(Component.translatable("gui.mushokucraft.modular_anvil.name_placeholder").withStyle(ChatFormatting.DARK_GRAY));
        this.addWidget(this.nameField);

        // 4. Forge / Strike Button
        this.forgeButton = new FantasyButton(this.leftPos + 162, this.topPos + 19, 54, 18, Component.translatable("gui.mushokucraft.modular_anvil.forge"), true, btn -> {
            handleForgeButtonClick();
        });
        this.addRenderableWidget(this.forgeButton);
    }

    private void handleForgeButtonClick() {
        if (forgingState == ForgingState.IDLE) {
            startForging();
        } else if (isStrikeState()) {
            performStrike();
        }
    }

    private boolean isStrikeState() {
        return forgingState == ForgingState.STRIKE_1 ||
                forgingState == ForgingState.STRIKE_2 ||
                forgingState == ForgingState.STRIKE_3 ||
                forgingState == ForgingState.STRIKE_4;
    }

    private void startForging() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        if (!this.menu.canForge(this.minecraft.player)) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 0.6f);
            return;
        }

        // Start QTE mini-game
        this.forgingState = ForgingState.STRIKE_1;
        this.currentStrike = 1;
        this.totalScore = 0.0f;
        this.strikeOscillation = 0.0f;
        this.sweetSpotCenter = 49.0f;
        this.sweetSpotWidth = 10.0f;
        this.goodZoneWidth = 30.0f;
        this.strikeSpeed = 2.6f;
        this.heatTicks = 80;
        this.evaluationTicks = 0;
        this.lastFeedbackText = null;
        this.finalQuality = null;

        this.forgeButton.setMessage(Component.translatable("gui.mushokucraft.modular_anvil.strike"));

        this.minecraft.player.playSound(SoundEvents.ANVIL_USE, 0.7f, 1.2f);
        this.minecraft.player.playSound(SoundEvents.FIRE_AMBIENT, 0.8f, 1.0f);

        // Spawn welcoming sparks
        spawnSparks(this.leftPos + 101, this.topPos + 72, 12, 0xFFFFCC33);
    }

    private void performStrike() {
        if (this.minecraft == null || this.minecraft.player == null) return;

        // Cursor position (0 to 94 px within track)
        float cursorX = (float) ((Math.sin(strikeOscillation) * 0.5f + 0.5f) * 94.0f) + 2.0f;
        float dist = Math.abs(cursorX - sweetSpotCenter);

        float points;
        if (dist <= sweetSpotWidth / 2.0f) {
            // Perfect Strike!
            points = 25.0f;
            this.lastFeedbackText = Component.translatable("gui.mushokucraft.modular_anvil.hit_perfect");
            this.feedbackColor = 0xFFFFD700;
            this.minecraft.player.playSound(SoundEvents.ANVIL_USE, 1.0f, 1.45f);
            this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.6f, 1.7f);
            spawnSparks(this.leftPos + 52 + (int) cursorX, this.topPos + 68, 22, 0xFFFFD700);
        } else if (dist <= goodZoneWidth / 2.0f) {
            // Good Strike!
            points = 17.5f;
            this.lastFeedbackText = Component.translatable("gui.mushokucraft.modular_anvil.hit_good");
            this.feedbackColor = 0xFF55FFFF;
            this.minecraft.player.playSound(SoundEvents.ANVIL_USE, 0.85f, 1.0f);
            spawnSparks(this.leftPos + 52 + (int) cursorX, this.topPos + 68, 14, 0xFF55FFFF);
        } else {
            // Miss Strike!
            points = -5.0f;
            this.lastFeedbackText = Component.translatable("gui.mushokucraft.modular_anvil.hit_miss");
            this.feedbackColor = 0xFFFF5555;
            this.minecraft.player.playSound(SoundEvents.ANVIL_LAND, 0.7f, 0.6f);
            this.minecraft.player.playSound(SoundEvents.ITEM_BREAK, 0.8f, 0.8f);
            spawnSparks(this.leftPos + 52 + (int) cursorX, this.topPos + 68, 10, 0xFFFF3333);
        }

        this.totalScore = Math.clamp(this.totalScore + points, 0.0f, 100.0f);
        this.evaluationTicks = 14;
        this.forgingState = ForgingState.EVALUATING;
    }

    private void spawnSparks(int originX, int originY, int count, int color) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.random() * Math.PI * 2.0);
            float speed = 1.0f + (float) Math.random() * 2.5f;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed - 0.5f;
            int maxLife = 10 + (int) (Math.random() * 10);
            sparks.add(new SparkParticle(originX, originY, vx, vy, color, maxLife));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.tickCount++;

        // Update spark particles
        Iterator<SparkParticle> it = sparks.iterator();
        while (it.hasNext()) {
            SparkParticle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.12f; // subtle gravity
            p.life--;
            if (p.life <= 0) {
                it.remove();
            }
        }

        // Check if player emptied materials mid-forging
        if (forgingState != ForgingState.IDLE && this.minecraft != null && this.minecraft.player != null) {
            if (!this.menu.canForge(this.minecraft.player) && forgingState != ForgingState.COMPLETED) {
                // Interrupted
                this.forgingState = ForgingState.IDLE;
                this.forgeButton.setMessage(Component.translatable("gui.mushokucraft.modular_anvil.forge"));
                return;
            }
        }

        // Active strike mechanics
        if (isStrikeState()) {
            this.strikeOscillation += 0.06f * strikeSpeed;
            this.heatTicks--;
            if (this.heatTicks <= 0) {
                // Time out -> Auto Miss!
                performStrike();
            }
        } else if (forgingState == ForgingState.EVALUATING) {
            this.evaluationTicks--;
            if (this.evaluationTicks <= 0) {
                advanceStrikeStage();
            }
        } else if (forgingState == ForgingState.COMPLETED) {
            this.evaluationTicks--;
            if (this.evaluationTicks <= 0) {
                this.forgingState = ForgingState.IDLE;
                this.forgeButton.setMessage(Component.translatable("gui.mushokucraft.modular_anvil.forge"));
            }
        }

        // Update button states
        if (forgingState == ForgingState.IDLE) {
            boolean canForge = this.minecraft != null && this.minecraft.player != null && this.menu.canForge(this.minecraft.player);
            this.forgeButton.active = canForge;
            this.prevButton.active = true;
            this.nextButton.active = true;
        } else if (isStrikeState()) {
            this.forgeButton.active = true;
            this.prevButton.active = false;
            this.nextButton.active = false;
        } else {
            this.forgeButton.active = false;
            this.prevButton.active = false;
            this.nextButton.active = false;
        }
    }

    private void advanceStrikeStage() {
        this.currentStrike++;
        if (this.currentStrike == 2) {
            this.forgingState = ForgingState.STRIKE_2;
            this.strikeSpeed = 3.4f;
            this.sweetSpotCenter = 28.0f; // shifted to the left
            this.sweetSpotWidth = 9.0f;
            this.goodZoneWidth = 26.0f;
            this.heatTicks = 75;
        } else if (this.currentStrike == 3) {
            this.forgingState = ForgingState.STRIKE_3;
            this.strikeSpeed = 4.2f;
            this.sweetSpotCenter = 70.0f; // shifted to the right
            this.sweetSpotWidth = 8.0f;
            this.goodZoneWidth = 24.0f;
            this.heatTicks = 70;
        } else if (this.currentStrike == 4) {
            this.forgingState = ForgingState.STRIKE_4;
            this.strikeSpeed = 5.0f;
            this.sweetSpotCenter = 46.0f;
            this.sweetSpotWidth = 7.0f;
            this.goodZoneWidth = 20.0f;
            this.heatTicks = 65;
        } else {
            // Completed!
            this.forgingState = ForgingState.COMPLETED;
            this.evaluationTicks = 45;
            float normalizedScore = this.totalScore / 100.0f;
            this.finalQuality = WeaponQuality.fromScore(normalizedScore);

            // Send packet to server to craft weapon with quality
            NetworkManager.sendToServer(new ForgeWeaponPacket(this.selectedForm.getId(), this.nameField.getValue(), normalizedScore));

            if (this.minecraft != null && this.minecraft.player != null) {
                if (this.finalQuality == WeaponQuality.MASTERPIECE) {
                    this.minecraft.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    spawnSparks(this.leftPos + 101, this.topPos + 72, 35, 0xFFFFD700);
                } else if (this.finalQuality == WeaponQuality.EXQUISITE) {
                    this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.2f);
                    spawnSparks(this.leftPos + 101, this.topPos + 72, 25, 0xFF55FFFF);
                } else if (this.finalQuality == WeaponQuality.TERRIBLE) {
                    this.minecraft.player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 0.8f);
                    spawnSparks(this.leftPos + 101, this.topPos + 72, 15, 0xFF882222);
                } else {
                    this.minecraft.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.8f, 1.0f);
                    spawnSparks(this.leftPos + 101, this.topPos + 72, 20, 0xFFEEEEEE);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render Name Field if in idle
        if (forgingState == ForgingState.IDLE) {
            this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // Render Spark Particles
        for (SparkParticle p : sparks) {
            int alpha = (int) (255 * ((float) p.life / p.maxLife));
            int renderColor = (alpha << 24) | (p.color & 0x00FFFFFF);
            guiGraphics.fill((int) p.x, (int) p.y, (int) p.x + 2, (int) p.y + 2, renderColor);
        }

        // Render Tooltips for input slots if empty
        int mx = mouseX - this.leftPos;
        int my = mouseY - this.topPos;

        if (mx >= 16 && mx <= 34) {
            ItemStack blade = this.menu.getContainer().getItem(ModularAnvilMenu.BLADE_SLOT);
            ItemStack guard = this.menu.getContainer().getItem(ModularAnvilMenu.GUARD_SLOT);
            ItemStack handle = this.menu.getContainer().getItem(ModularAnvilMenu.HANDLE_SLOT);
            ItemStack core = this.menu.getContainer().getItem(ModularAnvilMenu.CORE_SLOT);

            if (my >= 18 && my <= 36 && blade.isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.translatable("gui.mushokucraft.modular_anvil.slot_blade"), mouseX, mouseY);
            } else if (my >= 40 && my <= 58 && guard.isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.translatable("gui.mushokucraft.modular_anvil.slot_guard"), mouseX, mouseY);
            } else if (my >= 62 && my <= 80 && handle.isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.translatable("gui.mushokucraft.modular_anvil.slot_handle"), mouseX, mouseY);
            } else if (my >= 84 && my <= 102 && core.isEmpty()) {
                guiGraphics.renderTooltip(this.font, Component.translatable("gui.mushokucraft.modular_anvil.slot_core"), mouseX, mouseY);
            }
        }

        // Tooltip for Workstation (idle preview perks)
        if (forgingState == ForgingState.IDLE && mx >= 46 && mx <= 156 && my >= 48 && my <= 96) {
            List<Component> previewTips = new ArrayList<>();
            previewTips.add(this.selectedForm.getDisplayName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            previewTips.add(Component.literal("✦ ").withStyle(ChatFormatting.AQUA).append(this.selectedForm.getPerkDescription().copy().withStyle(ChatFormatting.GRAY)));

            ItemStack coreStack = this.menu.getContainer().getItem(ModularAnvilMenu.CORE_SLOT);
            WeaponCoreType core = WeaponCoreRegistry.fromItem(coreStack);
            if (core != null) {
                previewTips.add(Component.literal("-------------------").withStyle(ChatFormatting.DARK_GRAY));
                previewTips.add(Component.literal("◈ ").withColor(core.getColor()).append(core.getDisplayName().copy().withColor(core.getColor())));
                previewTips.add(Component.literal("  ").append(core.getPerkDescription().copy().withStyle(ChatFormatting.YELLOW)));
            }
            guiGraphics.renderComponentTooltip(this.font, previewTips, mouseX, mouseY);
        }

        // Tooltip for Mana Bar
        if (mx >= 46 && mx <= 156 && my >= 99 && my <= 106) {
            float reqMana = MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST.get().floatValue();
            float currentMana = 0.0f;
            float maxMana = 100.0f;
            if (this.minecraft != null && this.minecraft.player != null) {
                PlayerMasteryData mastery = (PlayerMasteryData) PlayerMasteryProvider.get(this.minecraft.player);
                if (mastery != null) {
                    currentMana = mastery.getMana();
                    maxMana = mastery.getMaxMana() + AccessoryHelper.getMaxManaBonus(this.minecraft.player);
                }
            }
            List<Component> manaTips = new ArrayList<>();
            manaTips.add(Component.literal(String.format("Мана: %d / %d MP", (int) currentMana, (int) maxMana)).withStyle(ChatFormatting.AQUA));
            manaTips.add(Component.literal(String.format("Расход на ковку: %d MP", (int) reqMana)).withStyle(ChatFormatting.GRAY));
            if (currentMana >= reqMana) {
                manaTips.add(Component.literal("✔ Достаточно маны").withStyle(ChatFormatting.GREEN));
            } else {
                manaTips.add(Component.literal("✗ Недостаточно маны").withStyle(ChatFormatting.RED));
            }
            guiGraphics.renderComponentTooltip(this.font, manaTips, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Render base background texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Read active materials from slots
        ItemStack bladeStack = this.menu.getContainer().getItem(ModularAnvilMenu.BLADE_SLOT);
        ItemStack guardStack = this.menu.getContainer().getItem(ModularAnvilMenu.GUARD_SLOT);
        ItemStack handleStack = this.menu.getContainer().getItem(ModularAnvilMenu.HANDLE_SLOT);
        ItemStack coreStack = this.menu.getContainer().getItem(ModularAnvilMenu.CORE_SLOT);

        WeaponMaterial blade = WeaponMaterialRegistry.findMaterial(bladeStack);
        WeaponMaterial guard = WeaponMaterialRegistry.findMaterial(guardStack);
        WeaponMaterial handle = WeaponMaterialRegistry.findMaterial(handleStack);
        WeaponCoreType core = WeaponCoreRegistry.fromItem(coreStack);

        // 1. Form Header Title (Centered between x+61 and x+141)
        Component formTitle = this.selectedForm.getShortName();
        guiGraphics.drawCenteredString(this.font, formTitle, x + 101, y + 21, 0xFFFFD700);

        // 2. Workstation Box (x+46, y+48, w=110, h=48)
        renderWorkstation(guiGraphics, x + 46, y + 48, blade, guard, handle, core, partialTick);

        // 3. Mana Bar (x+47, y+100, w=108, h=5)
        renderManaBar(guiGraphics, x + 47, y + 100, 108, 5);

        // 4. Status / Quality Badge on the right (x+160, y+82, w=58, h=24)
        renderStatusBadge(guiGraphics, x + 160, y + 84);
    }

    private void renderWorkstation(GuiGraphics guiGraphics, int wx, int wy, WeaponMaterial blade, WeaponMaterial guard, WeaponMaterial handle, WeaponCoreType core, float partialTick) {
        if (forgingState == ForgingState.IDLE) {
            // Idle State: 2D Pixel-Art Animated Preview
            ModularWeaponCodeRenderer.renderWeaponPreview(guiGraphics, wx + 55, wy + 20, this.selectedForm, blade, guard, handle, core, this.tickCount + partialTick);

            // Live Stat Summary
            float damage = ModularWeaponItem.calculateDamage(this.selectedForm, blade, guard, 1.0f);
            float speed = ModularWeaponItem.calculateSpeed(this.selectedForm, handle, blade);
            float reach = ModularWeaponItem.calculateReach(this.selectedForm, blade);
            int durability = ModularWeaponItem.calculateDurability(this.selectedForm, blade, guard, handle, 1.0f);

            String statLine = String.format("⚔%.1f ⚡%.1f ✦+%.1f 🛡%d", damage, speed, reach, durability);
            guiGraphics.drawCenteredString(this.font, Component.literal(statLine), wx + 55, wy + 38, 0xFFE0E0E0);
        } else if (isStrikeState() || forgingState == ForgingState.EVALUATING) {
            // Active Mini-Game State!
            // Subtle forge heat glow fill
            float pulse = (float) (Math.sin((this.tickCount + partialTick) * 0.2) * 0.5 + 0.5);
            int glowAlpha = 15 + (int) (pulse * 25);
            guiGraphics.fill(wx + 1, wy + 1, wx + 109, wy + 47, (glowAlpha << 24) | 0xDD5511);

            // Stage Title
            Component stageTitle = switch (currentStrike) {
                case 1 -> Component.translatable("gui.mushokucraft.modular_anvil.stage_1");
                case 2 -> Component.translatable("gui.mushokucraft.modular_anvil.stage_2");
                case 3 -> Component.translatable("gui.mushokucraft.modular_anvil.stage_3");
                default -> Component.translatable("gui.mushokucraft.modular_anvil.stage_4");
            };
            guiGraphics.drawCenteredString(this.font, stageTitle, wx + 55, wy + 5, 0xFFFFCC44);

            // Strike Track Bar (Track x: wx+6 to wx+104, w=98, h=10)
            int trackX = wx + 6;
            int trackY = wy + 17;
            int trackW = 98;
            int trackH = 9;

            // Miss zone background (dark crimson iron)
            guiGraphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF351717);

            // Good zone band
            int goodLeft = (int) (sweetSpotCenter - goodZoneWidth / 2.0f);
            int goodRight = (int) (sweetSpotCenter + goodZoneWidth / 2.0f);
            guiGraphics.fill(trackX + goodLeft, trackY, trackX + goodRight, trackY + trackH, 0xFF1B6A45);

            // Sweet Spot band
            int sweetLeft = (int) (sweetSpotCenter - sweetSpotWidth / 2.0f);
            int sweetRight = (int) (sweetSpotCenter + sweetSpotWidth / 2.0f);
            guiGraphics.fill(trackX + sweetLeft, trackY, trackX + sweetRight, trackY + trackH, 0xFFFFD700);

            // Sweet spot center tick
            guiGraphics.fill(trackX + (int) sweetSpotCenter, trackY, trackX + (int) sweetSpotCenter + 1, trackY + trackH, 0xFFFFFFFF);

            // Oscillating Cursor Marker
            float cursorX = (float) ((Math.sin(strikeOscillation) * 0.5f + 0.5f) * 94.0f) + 2.0f;
            int markerX = trackX + (int) cursorX;
            // Draw hammer / needle cursor
            guiGraphics.fill(markerX - 1, trackY - 1, markerX + 2, trackY + trackH + 1, 0xFFFFFFFF);
            guiGraphics.fill(markerX, trackY, markerX + 1, trackY + trackH, 0xFFFFCC00);

            // Heat / Urgency Bar (wy+28, w=98, h=2)
            int maxHeat = switch (currentStrike) {
                case 1 -> 80;
                case 2 -> 75;
                case 3 -> 70;
                default -> 65;
            };
            float heatRatio = Math.clamp((float) this.heatTicks / maxHeat, 0.0f, 1.0f);
            int heatW = (int) (trackW * heatRatio);
            guiGraphics.fill(trackX, wy + 28, trackX + trackW, wy + 30, 0xFF1C181A);
            guiGraphics.fill(trackX, wy + 28, trackX + heatW, wy + 30, 0xFFFF5511);

            // Feedback / Instruction Text
            if (forgingState == ForgingState.EVALUATING && lastFeedbackText != null) {
                guiGraphics.drawCenteredString(this.font, lastFeedbackText, wx + 55, wy + 34, feedbackColor);
            } else {
                guiGraphics.drawCenteredString(this.font, Component.translatable("gui.mushokucraft.modular_anvil.press_space"), wx + 55, wy + 34, 0xFFAAAAAA);
            }
        } else if (forgingState == ForgingState.COMPLETED) {
            // Completion Fanfare Display!
            guiGraphics.fill(wx + 1, wy + 1, wx + 109, wy + 47, 0x33FFD700);

            if (finalQuality != null) {
                guiGraphics.drawCenteredString(this.font, finalQuality.getTitleComponent(), wx + 55, wy + 10, finalQuality.getColor().getColor() != null ? finalQuality.getColor().getColor() : 0xFFFFD700);
            }
            String scoreStr = String.format("Качество ковки: %d%%", (int) this.totalScore);
            guiGraphics.drawCenteredString(this.font, Component.literal(scoreStr), wx + 55, wy + 24, 0xFFFFFFFF);

            guiGraphics.drawCenteredString(this.font, Component.literal("Оружие в слоте выдачи!").withStyle(ChatFormatting.GREEN), wx + 55, wy + 36, 0xFF88FF88);
        }
    }

    private void renderManaBar(GuiGraphics guiGraphics, int mx, int my, int mw, int mh) {
        float reqMana = MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST.get().floatValue();
        float currentMana = 0.0f;
        float maxMana = 100.0f;
        if (this.minecraft != null && this.minecraft.player != null) {
            PlayerMasteryData mastery = (PlayerMasteryData) PlayerMasteryProvider.get(this.minecraft.player);
            if (mastery != null) {
                currentMana = mastery.getMana();
                maxMana = mastery.getMaxMana() + AccessoryHelper.getMaxManaBonus(this.minecraft.player);
            }
        }
        if (maxMana <= 0) maxMana = 1.0f;

        boolean hasMana = currentMana >= reqMana;
        float manaRatio = Math.clamp(currentMana / maxMana, 0.0f, 1.0f);
        int fillWidth = (int) (mw * manaRatio);

        int barColor = hasMana ? 0xFF3399FF : 0xFFDD3333;
        guiGraphics.fill(mx, my, mx + fillWidth, my + mh, barColor);

        // Golden notch at the forging cost threshold
        float costRatio = Math.clamp(reqMana / maxMana, 0.0f, 1.0f);
        int notchX = mx + (int) (mw * costRatio);
        guiGraphics.fill(notchX, my - 1, notchX + 1, my + mh + 1, 0xFFFFD700);
    }

    private void renderStatusBadge(GuiGraphics guiGraphics, int bx, int by) {
        if (forgingState == ForgingState.IDLE) {
            boolean canForge = this.minecraft != null && this.minecraft.player != null && this.menu.canForge(this.minecraft.player);
            if (canForge) {
                guiGraphics.drawCenteredString(this.font, Component.literal("✔ Готов"), bx + 27, by, 0xFF55FF55);
            } else {
                guiGraphics.drawCenteredString(this.font, Component.literal("✕ Нет ресурсов"), bx + 27, by, 0xFFAAAAAA);
            }
        } else if (isStrikeState() || forgingState == ForgingState.EVALUATING) {
            String prog = String.format("%d%%", (int) this.totalScore);
            int color = this.totalScore >= 75 ? 0xFF55FF55 : (this.totalScore >= 50 ? 0xFFFFFF55 : 0xFFFF5555);
            guiGraphics.drawCenteredString(this.font, Component.literal(prog), bx + 27, by, color);
        } else if (forgingState == ForgingState.COMPLETED && finalQuality != null) {
            guiGraphics.drawCenteredString(this.font, Component.literal(finalQuality.getId().toUpperCase()), bx + 27, by, 0xFFFFD700);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            // ESC
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
            return true;
        }

        // SPACE (32) or ENTER (257) triggers strike when in mini-game
        if (keyCode == 32 || keyCode == 257) {
            if (isStrikeState()) {
                performStrike();
                return true;
            } else if (forgingState == ForgingState.IDLE && !this.nameField.isFocused() && this.menu.canForge(this.minecraft.player)) {
                startForging();
                return true;
            }
        }

        if (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
