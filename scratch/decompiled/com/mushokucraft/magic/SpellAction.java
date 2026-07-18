/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.Level
 */
package com.mushokucraft.magic;

import com.mushokucraft.magic.Spell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface SpellAction {
    public void execute(Level var1, ServerPlayer var2, Spell var3);
}

