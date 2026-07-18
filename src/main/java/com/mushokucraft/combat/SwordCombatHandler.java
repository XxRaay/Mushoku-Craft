package com.mushokucraft.combat;

import com.mushokucraft.MushokuCraft;
import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.init.ModEffects;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = MushokuCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SwordCombatHandler {
    
    public static final Set<UUID> DEBUG_PLAYERS = new HashSet<>();
    private static final Map<UUID, Integer> stanceHits = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            stanceHits.remove(player.getUUID());
        }
    }

    public static void changeStance(Player player, String stanceId) {
        if (player == null) return;
        PlayerMasteryData mastery = player.getData(ModAttachments.PLAYER_MASTERY);
        
        if (stanceId.isEmpty() || stanceId.equals("none")) {
            mastery.setActiveStance(null);
        } else {
            for (SwordStyle style : SwordStyle.values()) {
                if (style.getId().equals(stanceId)) {
                    if (mastery.isStyleUnlocked(style)) {
                        mastery.setActiveStance(style);
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("You haven't unlocked this style yet!").withStyle(net.minecraft.ChatFormatting.RED));
                    }
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        PlayerMasteryData mastery = player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery == null) return;

        // Decrease parry ticks
        if (mastery.parryTicks > 0) {
            mastery.parryTicks--;
        }

        boolean holdingSword = player.getMainHandItem().getItem() instanceof SwordItem;
        SwordStyle stance = mastery.getActiveStance();
        boolean isToukiActive = mastery.isToukiActive() && holdingSword && stance != null;
        float stanceMastery = mastery.getStanceMastery(stance);

        if (isToukiActive && stanceMastery < MushokuConfig.TOUKI_MIN_MASTERY.get().floatValue()) {
            mastery.setToukiActive(false);
            isToukiActive = false;
            if (!player.level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(sp, new com.mushokucraft.network.SyncToukiPacket(sp.getId(), false));
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
            } else {
                s.removeToukiModifiers(player);
            }
        }
        
        if (holdingSword && player.tickCount % 10 == 0 && DEBUG_PLAYERS.contains(player.getUUID())) {
            String debug = String.format("Touki: %s | Stance: %s | Mastery: %.2f | Mana: %.0f",
                    isToukiActive, stance != null ? stance.name() : "None", stanceMastery, mastery.getMana());
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(debug), true);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            // PlayerTickEvent will handle the modifiers
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery == null) return;

        SwordStyle stance = mastery.getActiveStance();

        // Block offhand usage for Sword God
        if (stance == SwordStyle.SWORD_GOD && event.getHand() == net.minecraft.world.InteractionHand.OFF_HAND) {
            if (player.getMainHandItem().getItem() instanceof SwordItem) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getHand() == net.minecraft.world.InteractionHand.OFF_HAND) {
            if (player.getMainHandItem().getItem() instanceof SwordItem) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        PlayerMasteryData mastery = player.getData(ModAttachments.PLAYER_MASTERY);
        if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getHand() == net.minecraft.world.InteractionHand.OFF_HAND) {
            if (player.getMainHandItem().getItem() instanceof SwordItem) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide) return;

        // Handled in separate handlers

        // Mastery Progression
        if (event.getSource().getEntity() instanceof Player attacker) {
            PlayerMasteryData mastery = attacker.getData(ModAttachments.PLAYER_MASTERY);
            if (mastery != null) {
                if (attacker instanceof net.minecraft.server.level.ServerPlayer sp && attacker.getMainHandItem().getItem() instanceof SwordItem && mastery.getActiveStance() != null) {
                    boolean isDummy = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).getPath().contains("dummy");
                    if (isDummy) {
                        com.mushokucraft.event.ModGameEvents.addStanceMastery(sp, mastery.getActiveStance(), MushokuConfig.DUMMY_MASTERY_GAIN.get().floatValue());
                    } else if (mastery.getActiveStance() == SwordStyle.SWORD_GOD && event.getNewDamage() >= MushokuConfig.SWORD_GOD_HIGH_DAMAGE_THRESHOLD.get().floatValue()) {
                        com.mushokucraft.event.ModGameEvents.addStanceMastery(sp, SwordStyle.SWORD_GOD, MushokuConfig.SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN.get().floatValue());
                    }
                }
            }
        }
    }
}

