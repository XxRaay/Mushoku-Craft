package com.mushokucraft.magic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface SpellAction {
    /**
     * Executes the custom logic for a spell.
     * @param level The server level
     * @param player The player casting the spell
     * @param spell The spell being cast
     */
    void execute(Level level, ServerPlayer player, Spell spell);
}





