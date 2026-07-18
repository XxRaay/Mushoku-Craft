package com.mushokucraft.item;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IMagicBook {
    @OnlyIn(value=Dist.CLIENT)
    public Screen getLearningScreen();

    public void triggerCastAnimation(Player var1, ItemStack var2);
}


