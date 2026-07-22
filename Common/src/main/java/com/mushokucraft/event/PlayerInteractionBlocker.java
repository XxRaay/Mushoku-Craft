package com.mushokucraft.event;

import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.magic.ServerChargeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.EventResult;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

public class PlayerInteractionBlocker {
    private static boolean isBusy(Player player) {
        if (player.level().isClientSide) {
            return dev.architectury.platform.Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT && isClientBusy();
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            return ServerChargeManager.isCharging(sp);
        }
        return false;
    }

    private static boolean isClientBusy() {
        return ClientCastState.isCasting || CombatInputHandler.isCharging();
    }

    public static void register() {
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (isBusy(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            if (isBusy(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (isBusy(player)) {
                return dev.architectury.event.CompoundEventResult.interruptFalse(null);
            }
            return dev.architectury.event.CompoundEventResult.pass();
        });

        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (isBusy(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        
        dev.architectury.event.events.common.PlayerEvent.ATTACK_ENTITY.register((player, level, entity, hand, result) -> {
            if (isBusy(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
    }
}


