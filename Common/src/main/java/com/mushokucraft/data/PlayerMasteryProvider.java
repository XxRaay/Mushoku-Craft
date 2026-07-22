package com.mushokucraft.data;

import net.minecraft.world.entity.player.Player;
import com.mushokucraft.event.ModGameEvents;
import net.minecraft.server.level.ServerPlayer;

public class PlayerMasteryProvider {
    public static PlayerMasteryData get(Player player) {
        if (player instanceof PlayerMasteryAccessor) {
            return ((PlayerMasteryAccessor) player).getPlayerMasteryData();
        }
        return null;
    }
    
    public static void sync(Player player) {
        if (player instanceof ServerPlayer sp) {
            PlayerMasteryData data = get(player);
            if (data != null) {
                ModGameEvents.syncMastery(sp, data);
            }
        }
    }
}
