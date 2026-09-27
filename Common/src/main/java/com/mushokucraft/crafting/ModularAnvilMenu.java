package com.mushokucraft.crafting;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModMenuTypes;
import com.mushokucraft.network.SyncManaPacket;
import com.mushokucraft.weapon.modular.ModularWeaponItem;
import com.mushokucraft.weapon.modular.WeaponCoreRegistry;
import com.mushokucraft.weapon.modular.WeaponCoreType;
import com.mushokucraft.weapon.modular.WeaponForm;
import com.mushokucraft.weapon.modular.WeaponMaterial;
import com.mushokucraft.weapon.modular.WeaponMaterialRegistry;
import com.mushokucraft.weapon.modular.WeaponQuality;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModularAnvilMenu extends AbstractContainerMenu {
    public static final int BLADE_SLOT = 0;
    public static final int GUARD_SLOT = 1;
    public static final int HANDLE_SLOT = 2;
    public static final int CORE_SLOT = 3;
    public static final int RESULT_SLOT = 4;

    private final SimpleContainer container = new SimpleContainer(5);
    private final Player player;

    public ModularAnvilMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.MODULAR_ANVIL.get(), containerId);
        this.player = playerInventory.player;

        // 0: Blade Slot
        this.addSlot(new Slot(container, BLADE_SLOT, 17, 19));
        // 1: Guard Slot
        this.addSlot(new Slot(container, GUARD_SLOT, 17, 41));
        // 2: Handle Slot
        this.addSlot(new Slot(container, HANDLE_SLOT, 17, 63));
        // 3: Monster Core Slot
        this.addSlot(new Slot(container, CORE_SLOT, 17, 85));

        // 4: Result Slot
        this.addSlot(new Slot(container, RESULT_SLOT, 184, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player p, ItemStack stack) {
                super.onTake(p, stack);
                p.level().playSound(null, p.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
        });

        // Player Inventory (3 rows x 9)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 30 + j * 18, 122 + i * 18));
            }
        }

        // Player Hotbar (9 slots)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 180));
        }
    }

    public SimpleContainer getContainer() {
        return container;
    }

    public boolean canForge(Player p) {
        ItemStack bladeStack = container.getItem(BLADE_SLOT);
        ItemStack guardStack = container.getItem(GUARD_SLOT);
        ItemStack handleStack = container.getItem(HANDLE_SLOT);
        ItemStack resultStack = container.getItem(RESULT_SLOT);

        if (bladeStack.isEmpty() || guardStack.isEmpty() || handleStack.isEmpty()) return false;
        if (!resultStack.isEmpty()) return false;

        WeaponMaterial blade = WeaponMaterialRegistry.findMaterial(bladeStack);
        WeaponMaterial guard = WeaponMaterialRegistry.findMaterial(guardStack);
        WeaponMaterial handle = WeaponMaterialRegistry.findMaterial(handleStack);

        if (blade == null || guard == null || handle == null) return false;

        PlayerMasteryData mastery = (PlayerMasteryData) PlayerMasteryProvider.get(p);
        if (mastery != null) {
            float cost = MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST.get().floatValue();
            return mastery.getMana() >= cost;
        }

        return true;
    }

    public void serverForgeWeapon(ServerPlayer serverPlayer, String formId, String customName) {
        serverForgeWeapon(serverPlayer, formId, customName, 0.70f);
    }

    public void serverForgeWeapon(ServerPlayer serverPlayer, String formId, String customName, float qualityScore) {
        if (!canForge(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("gui.mushokucraft.modular_anvil.fail_mana"), true);
            return;
        }

        ItemStack bladeStack = container.getItem(BLADE_SLOT);
        ItemStack guardStack = container.getItem(GUARD_SLOT);
        ItemStack handleStack = container.getItem(HANDLE_SLOT);
        ItemStack coreStack = container.getItem(CORE_SLOT);

        WeaponMaterial blade = WeaponMaterialRegistry.findMaterial(bladeStack);
        WeaponMaterial guard = WeaponMaterialRegistry.findMaterial(guardStack);
        WeaponMaterial handle = WeaponMaterialRegistry.findMaterial(handleStack);
        WeaponCoreType core = WeaponCoreRegistry.fromItem(coreStack);

        WeaponForm form = WeaponForm.byId(formId);

        // Consume mana
        PlayerMasteryData mastery = (PlayerMasteryData) PlayerMasteryProvider.get(serverPlayer);
        float manaCost = MushokuConfig.MODULAR_ANVIL_BASE_MANA_COST.get().floatValue();
        if (mastery != null) {
            mastery.consumeMana(manaCost);
            NetworkManager.sendToPlayer(serverPlayer, new SyncManaPacket(mastery.getMana(), mastery.getMaxMana()));
        }

        // Consume 1 item from each input
        bladeStack.shrink(1);
        guardStack.shrink(1);
        handleStack.shrink(1);
        if (core != null && !coreStack.isEmpty()) {
            coreStack.shrink(1);
        }

        // Determine quality from mini-game score
        float clampedScore = Math.clamp(qualityScore, 0.0f, 1.0f);
        WeaponQuality qualityTier = WeaponQuality.fromScore(clampedScore);

        String crafterName = serverPlayer.getGameProfile().getName();
        ItemStack forgedWeapon = ModularWeaponItem.createWeapon(form, blade, guard, handle, core, crafterName, qualityTier, clampedScore, customName);

        container.setItem(RESULT_SLOT, forgedWeapon);
        this.slots.get(RESULT_SLOT).setChanged();
        container.setChanged();

        // Visual and auditory feedback
        if (qualityTier == WeaponQuality.MASTERPIECE) {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.4f);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, serverPlayer.getX(), serverPlayer.getY() + 1.2, serverPlayer.getZ(), 40, 0.6, 0.6, 0.6, 0.3);
        } else if (qualityTier == WeaponQuality.EXQUISITE) {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.ENCHANT, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 30, 0.5, 0.5, 0.5, 0.2);
        } else if (qualityTier == WeaponQuality.TERRIBLE) {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8f, 0.6f);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.9f, 0.8f);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.SMOKE, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
        } else {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.0f);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.ENCHANT, serverPlayer.getX(), serverPlayer.getY() + 1.0, serverPlayer.getZ(), 20, 0.4, 0.4, 0.4, 0.2);
        }

        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Result slot (index 4)
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            }
            // Player inventory -> Anvil slots
            else if (index >= 5 && index < 41) {
                // If core item, try core slot first
                if (WeaponCoreRegistry.fromItem(itemstack1) != null) {
                    if (!this.moveItemStackTo(itemstack1, CORE_SLOT, CORE_SLOT + 1, false)) {
                        // fallback
                    }
                } else if (WeaponMaterialRegistry.findMaterial(itemstack1) != null) {
                    if (!this.moveItemStackTo(itemstack1, BLADE_SLOT, HANDLE_SLOT + 1, false)) {
                        // fallback
                    }
                }

                // If not moved to anvil slots, move between hotbar and main inventory
                if (!itemstack1.isEmpty() && itemstack1.getCount() == itemstack.getCount()) {
                    if (index < 32) {
                        if (!this.moveItemStackTo(itemstack1, 32, 41, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 5, 32, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // Anvil input slots (0..3) -> player inventory
            else if (!this.moveItemStackTo(itemstack1, 5, 41, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
