package com.mushokucraft.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface IMagicBook {
    @Environment(EnvType.CLIENT)
    public Screen getLearningScreen();

    public void triggerCastAnimation(Player var1, ItemStack var2);
}


