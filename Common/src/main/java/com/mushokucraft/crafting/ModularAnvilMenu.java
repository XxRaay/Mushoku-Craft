package com.mushokucraft.crafting;

import com.mushokucraft.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModularAnvilMenu extends AbstractContainerMenu {
    private final SimpleContainer container = new SimpleContainer(4);

    public ModularAnvilMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.MODULAR_ANVIL.get(), containerId);

        // Blade Slot
        this.addSlot(new Slot(container, 0, 44, 25));
        // Handle Slot
        this.addSlot(new Slot(container, 1, 44, 43));
        // Monster Core Slot
        this.addSlot(new Slot(container, 2, 44, 61));

        // Result Slot
        this.addSlot(new Slot(container, 3, 116, 43) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 30 + j * 18, 122 + i * 18));
            }
        }
        
        // Player Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 180));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // Result slot (index 3)
            if (index == 3) {
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } 
            // Player inventory -> input slots
            else if (index >= 4 && index < 40) {
                if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                    // if it can't go to input slots, move between hotbar and main inventory
                    if (index < 31) {
                        if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 4, 31, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } 
            // Input slots -> player inventory
            else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
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
    public boolean stillValid(Player player) {
        return true;
    }
}
