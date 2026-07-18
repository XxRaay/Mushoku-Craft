package com.mushokucraft.event;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@EventBusSubscriber(modid="mushokucraft")
public class AdvancementHandler {
    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            String advId = event.getAdvancement().id().toString();
            PlayerMasteryData data = (PlayerMasteryData)player2.getData(ModAttachments.PLAYER_MASTERY);
            boolean sync = false;
            if (advId.equals("mushokucraft:style_sword_god")) {
                data.unlockStyle(SwordStyle.SWORD_GOD);
                sync = true;
            } else if (advId.equals("mushokucraft:style_water_god")) {
                data.unlockStyle(SwordStyle.WATER_GOD);
                sync = true;
            } else if (advId.equals("mushokucraft:style_north_god")) {
                data.unlockStyle(SwordStyle.NORTH_GOD);
                sync = true;
            }
            if (sync) {
                ModGameEvents.syncMastery(player2, data);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        ServerPlayer player;
        Entity entity = event.getSource().getEntity();
        if (entity instanceof ServerPlayer && (player = (ServerPlayer)entity).getHealth() <= ((Double)MushokuConfig.LOW_HP_THRESHOLD.get()).floatValue() && event.getEntity() instanceof Enemy) {
            PlayerMasteryData data;
            AdvancementHolder adv = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"style_north_god"));
            if (adv != null) {
                for (String criteria : adv.value().criteria().keySet()) {
                    player.getAdvancements().award(adv, criteria);
                }
            }
            if ((data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY)) != null && data.getActiveStance() == SwordStyle.NORTH_GOD) {
                ModGameEvents.addStanceMastery(player, SwordStyle.NORTH_GOD, ((Double)MushokuConfig.NORTH_GOD_LOW_HP_KILL_MASTERY_GAIN.get()).floatValue());
            }
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            if (!player.isBlocking()) {
                return;
            }
            PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
            if (data != null && !data.isStyleUnlocked(SwordStyle.WATER_GOD)) {
                AdvancementHolder adv;
                data.incrementShieldBlocks();
                if (data.getShieldBlocks() >= (Integer)MushokuConfig.SHIELD_BLOCKS_FOR_WATER_GOD.get() && (adv = player.server.getAdvancements().get(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"style_water_god"))) != null) {
                    for (String criteria : adv.value().criteria().keySet()) {
                        player.getAdvancements().award(adv, criteria);
                    }
                }
            }
        }
    }
}


