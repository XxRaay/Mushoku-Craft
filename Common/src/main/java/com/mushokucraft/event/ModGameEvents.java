package com.mushokucraft.event;

import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.combat.ToukiManager;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
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
import dev.architectury.networking.NetworkManager;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.EventResult;

import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.magic.ManaProgressionManager;

public class ModGameEvents {
    private static final java.util.Set<java.util.UUID> sleepingPlayers = new java.util.HashSet<>();

    public static void register() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player instanceof ServerPlayer) {
                ServerPlayer player2 = (ServerPlayer)player;
                PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player2);
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
        });

        PlayerEvent.PLAYER_QUIT.register(player -> {
            sleepingPlayers.remove(player.getUUID());
        });

        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer) {
                ItemStack offhand;
                ServerPlayer player2 = (ServerPlayer)player;
                PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player2);
                if (data == null) {
                    return;
                }

                // Check sleep status for anime daily training full mana recovery
                boolean wasSleeping = sleepingPlayers.contains(player2.getUUID());
                boolean isSleeping = player2.isSleeping();
                if (isSleeping) {
                    sleepingPlayers.add(player2.getUUID());
                    if (player2.getSleepTimer() >= 100) {
                        ManaProgressionManager.handleSleepRestoration(player2, data);
                    }
                } else if (wasSleeping) {
                    sleepingPlayers.remove(player2.getUUID());
                    if (player2.level().isDay() || player2.getSleepTimer() >= 100) {
                        ManaProgressionManager.handleSleepRestoration(player2, data);
                    }
                }

                if (data.getActiveStance() == SwordStyle.SWORD_GOD && player2.getMainHandItem().getItem() instanceof SwordItem && !(offhand = player2.getOffhandItem()).isEmpty()) {
                    ItemStack toReturn = offhand.copy();
                    player2.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    if (!player2.getInventory().add(toReturn)) {
                        player2.drop(toReturn, false);
                    }
                    player2.displayClientMessage((Component)Component.literal((String)"\u00a7c\u0421\u0442\u0438\u043b\u044c \u0411\u043e\u0433\u0430 \u041c\u0435\u0447\u0430 \u043d\u0435 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0437\u0430\u043d\u0438\u043c\u0430\u0442\u044c \u0432\u0442\u043e\u0440\u0443\u044e \u0440\u0443\u043a\u0443!"), true);
                }
                boolean syncNeeded = false;
                if (com.mushokucraft.magic.AirCushionManager.tick(player2, data)) {
                    syncNeeded = true;
                }

                float effectiveMax = data.getMaxMana() + com.mushokucraft.accessory.AccessoryHelper.getMaxManaBonus(player2);
                if (data.getLastSyncedEffectiveMax() != effectiveMax) {
                    data.setLastSyncedEffectiveMax(effectiveMax);
                    if (data.getMana() > effectiveMax) {
                        data.setMana(effectiveMax);
                    }
                    syncNeeded = true;
                } else if (data.getMana() > effectiveMax) {
                    data.setMana(effectiveMax);
                    syncNeeded = true;
                }
                
                if (player2.tickCount % 20 == 0) {
                    if (ToukiManager.tick(player2, data)) {
                        syncNeeded = true;
                    }
                    float regenBonus = com.mushokucraft.accessory.AccessoryHelper.getManaRegenBonus(player2);
                    if (data.getMana() < effectiveMax && (data.getManaRegenRate() > 0.0f || regenBonus > 0.0f)) {
                        float effectiveRegenPerSec = MasteryCalculator.calculateManaRegenPerSecond(data.getManaRegenRate(), data.getMaxMana()) + regenBonus;
                        data.regenMana(effectiveRegenPerSec, effectiveMax);
                        syncNeeded = true;
                    }
                }
                
                if (syncNeeded) {
                    ModGameEvents.syncMana(player2, data);
                }
            }
        });

        dev.architectury.event.events.common.EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide) return EventResult.pass();

            if (entity instanceof ServerPlayer player) {
                // 1. Archmage's Heart Lifeline
                if (com.mushokucraft.accessory.AccessoryHelper.hasLifeline(player) && player.getHealth() <= amount) {
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    if (mastery != null && mastery.getMana() >= 500.0f) {
                        mastery.consumeMana(500.0f);
                        player.setHealth(Math.max(4.0f, player.getMaxHealth() * 0.3f));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 160, 1));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 400, 0));
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 80, 2));
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
                        ((net.minecraft.server.level.ServerLevel)player.level()).sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.4, 0.5, 0.4, 0.2);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.lifeline_triggered"), true);
                        ModGameEvents.syncMana(player, mastery);
                        return EventResult.interruptFalse();
                    }
                }

                // 2. Volcanic Sovereign Burn
                if (com.mushokucraft.accessory.AccessoryHelper.hasVolcanicBurn(player)) {
                    if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker && attacker != player) {
                        attacker.igniteForSeconds(6);
                        attacker.hurt(player.damageSources().inFire(), 3.0f);
                    }
                }

                // 3. Melee Cast Disruption (Swordsmen close-range superiority against chanting mages)
                if (com.mushokucraft.config.MushokuConfig.CAST_INTERRUPTION_ON_MELEE.get() && com.mushokucraft.magic.ServerCastManager.hasActiveCast(player.getUUID())) {
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    boolean hasPoise = mastery != null && mastery.isToukiActive();
                    boolean isMelee = source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker && attacker != player && player.distanceTo(attacker) <= 6.0;
                    if (isMelee && !hasPoise && amount >= com.mushokucraft.config.MushokuConfig.CAST_INTERRUPTION_MIN_DAMAGE.get().floatValue()) {
                        com.mushokucraft.magic.ServerCastManager.interruptCast(player, source, amount);
                    }
                }
            }

            return EventResult.pass();
        });
    }

    public static void syncMana(ServerPlayer player, PlayerMasteryData data) {
        float effectiveMax = data.getMaxMana() + com.mushokucraft.accessory.AccessoryHelper.getMaxManaBonus(player);
        NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncManaPacket(data.getMana(), effectiveMax));
    }

    public static void syncMastery(ServerPlayer player, PlayerMasteryData data) {
        NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new SyncFullMasteryPacket(data.serializeNBT((HolderLookup.Provider)player.level().registryAccess())));
    }

    public static void addStanceMastery(ServerPlayer player, SwordStyle style, float amount) {
        PlayerMasteryData data = (PlayerMasteryData)PlayerMasteryProvider.get(player);
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
        NetworkManager.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new MasteryGainedPacket(amount));
    }
}
