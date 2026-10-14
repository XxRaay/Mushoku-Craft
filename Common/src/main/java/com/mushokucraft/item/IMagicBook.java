package com.mushokucraft.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IMagicBook {
    void openLearningScreen();

    void triggerCastAnimation(Player var1, ItemStack var2);
}


