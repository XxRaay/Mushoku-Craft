package com.mushokucraft.event;

import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.magic.ServerChargeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="mushokucraft")
public class PlayerInteractionBlocker {
    private static boolean isBusy(Player player) {
        if (player.level().isClientSide) {
            return PlayerInteractionBlocker.isClientBusy();
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            return ServerChargeManager.isCharging(sp);
        }
        return false;
    }

    @OnlyIn(value=Dist.CLIENT)
    private static boolean isClientBusy() {
        return ClientCastState.isCasting || CombatInputHandler.isCharging();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (PlayerInteractionBlocker.isBusy(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (PlayerInteractionBlocker.isBusy(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (PlayerInteractionBlocker.isBusy(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (PlayerInteractionBlocker.isBusy(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}


