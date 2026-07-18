/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SwordItem
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.event;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.combat.ToukiManager;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.network.MasteryGainedPacket;
import com.mushokucraft.network.SyncFullMasteryPacket;
import com.mushokucraft.network.SyncManaPacket;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mushokucraft")
public class ModGameEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            PlayerMasteryData data = (PlayerMasteryData)player2.getData(ModAttachments.PLAYER_MASTERY);
            if (data == null) {
                return;
            }
            long now = System.currentTimeMillis();
            for (Map.Entry<ResourceLocation, Long> entry : data.getItemCooldownEnds().entrySet()) {
                if (entry.getValue() <= now) continue;
                int remainingTicks = (int)((entry.getValue() - now) / 50L);
                Item item = (Item)BuiltInRegistries.ITEM.get(entry.getKey());
                if (item == null || item == Items.AIR) continue;
                player2.getCooldowns().addCooldown(item, remainingTicks);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ItemStack offhand;
            ServerPlayer player2 = (ServerPlayer)player;
            PlayerMasteryData data = (PlayerMasteryData)player2.getData(ModAttachments.PLAYER_MASTERY);
            if (data != null && data.getActiveStance() == SwordStyle.SWORD_GOD && player2.getMainHandItem().getItem() instanceof SwordItem && !(offhand = player2.getOffhandItem()).isEmpty()) {
                ItemStack toReturn = offhand.copy();
                player2.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                if (!player2.getInventory().add(toReturn)) {
                    player2.drop(toReturn, false);
                }
                player2.displayClientMessage((Component)Component.literal((String)"\u00a7c\u0421\u0442\u0438\u043b\u044c \u0411\u043e\u0433\u0430 \u041c\u0435\u0447\u0430 \u043d\u0435 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0437\u0430\u043d\u0438\u043c\u0430\u0442\u044c \u0432\u0442\u043e\u0440\u0443\u044e \u0440\u0443\u043a\u0443!"), true);
            }
            if (player2.tickCount % 20 == 0) {
                boolean syncNeeded = false;
                if (ToukiManager.tick(player2, data)) {
                    syncNeeded = true;
                } else if (data.getMana() < data.getMaxMana() && data.getManaRegenRate() > 0.0f) {
                    data.regenMana(data.getManaRegenRate());
                    syncNeeded = true;
                }
                if (syncNeeded) {
                    ModGameEvents.syncMana(player2, data);
                }
            }
        }
    }

    public static void syncMana(ServerPlayer player, PlayerMasteryData data) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncManaPacket(data.getMana(), data.getMaxMana()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void syncMastery(ServerPlayer player, PlayerMasteryData data) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void addStanceMastery(ServerPlayer player, SwordStyle style, float amount) {
        PlayerMasteryData data = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
        if (data == null || style == null) {
            return;
        }
        float current = data.getStanceMastery(style);
        if (current >= 1.0f) {
            return;
        }
        float newMastery = Math.min(1.0f, current + amount);
        switch (style) {
            case SWORD_GOD: {
                data.setSwordGodMastery(newMastery);
                break;
            }
            case WATER_GOD: {
                data.setWaterGodMastery(newMastery);
                break;
            }
            case NORTH_GOD: {
                data.setNorthGodMastery(newMastery);
            }
        }
        ModGameEvents.syncMastery(player, data);
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new MasteryGainedPacket(amount), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

