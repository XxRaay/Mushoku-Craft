/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.SwordItem
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 *  net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.combat;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.network.SyncToukiPacket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mushokucraft", bus=EventBusSubscriber.Bus.GAME)
public class SwordCombatHandler {
    public static final Set<UUID> DEBUG_PLAYERS = new HashSet<UUID>();
    private static final Map<UUID, Integer> stanceHits = new ConcurrentHashMap<UUID, Integer>();

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            stanceHits.remove(player.getUUID());
        }
    }

    public static void changeStance(Player player, String stanceId) {
        if (player == null) {
            return;
        }
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (stanceId.isEmpty() || stanceId.equals("none")) {
            mastery.setActiveStance(null);
        } else {
            for (SwordStyle style : SwordStyle.values()) {
                if (!style.getId().equals(stanceId)) continue;
                if (mastery.isStyleUnlocked(style)) {
                    mastery.setActiveStance(style);
                    break;
                }
                player.sendSystemMessage((Component)Component.literal((String)"You haven't unlocked this style yet!").withStyle(ChatFormatting.RED));
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery == null) {
            return;
        }
        if (mastery.parryTicks > 0) {
            --mastery.parryTicks;
        }
        boolean holdingSword = player.getMainHandItem().getItem() instanceof SwordItem;
        SwordStyle stance = mastery.getActiveStance();
        boolean isToukiActive = mastery.isToukiActive() && holdingSword && stance != null;
        float stanceMastery = mastery.getStanceMastery(stance);
        if (isToukiActive && stanceMastery < ((Double)MushokuConfig.TOUKI_MIN_MASTERY.get()).floatValue()) {
            mastery.setToukiActive(false);
            isToukiActive = false;
            if (!player.level().isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf((Entity)sp, (CustomPacketPayload)new SyncToukiPacket(sp.getId(), false), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
        for (SwordStyle s : SwordStyle.values()) {
            if (s == stance && holdingSword) {
                s.applyPassiveModifiers(player);
            } else {
                s.removePassiveModifiers(player);
            }
            if (isToukiActive && s == stance) {
                s.applyToukiModifiers(player, stanceMastery);
                continue;
            }
            s.removeToukiModifiers(player);
        }
        if (holdingSword && player.tickCount % 10 == 0 && DEBUG_PLAYERS.contains(player.getUUID())) {
            String debug = String.format("Touki: %s | Stance: %s | Mastery: %.2f | Mana: %.0f", isToukiActive, stance != null ? stance.name() : "None", Float.valueOf(stanceMastery), Float.valueOf(mastery.getMana()));
            player.displayClientMessage((Component)Component.literal((String)debug), true);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery == null) {
            return;
        }
        SwordStyle stance = mastery.getActiveStance();
        if (stance == SwordStyle.SWORD_GOD && event.getHand() == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof SwordItem) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getHand() == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof SwordItem) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getHand() == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof SwordItem) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        Player attacker;
        PlayerMasteryData mastery;
        if (event.getEntity().level().isClientSide) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (entity instanceof Player && (mastery = (PlayerMasteryData)(attacker = (Player)entity).getData(ModAttachments.PLAYER_MASTERY)) != null && attacker instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)attacker;
            if (attacker.getMainHandItem().getItem() instanceof SwordItem && mastery.getActiveStance() != null) {
                boolean isDummy = BuiltInRegistries.ENTITY_TYPE.getKey((Object)event.getEntity().getType()).getPath().contains("dummy");
                if (isDummy) {
                    ModGameEvents.addStanceMastery(sp, mastery.getActiveStance(), ((Double)MushokuConfig.DUMMY_MASTERY_GAIN.get()).floatValue());
                } else if (mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getNewDamage() >= ((Double)MushokuConfig.SWORD_GOD_HIGH_DAMAGE_THRESHOLD.get()).floatValue()) {
                    ModGameEvents.addStanceMastery(sp, SwordStyle.SWORD_GOD, ((Double)MushokuConfig.SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN.get()).floatValue());
                }
            }
        }
    }
}

