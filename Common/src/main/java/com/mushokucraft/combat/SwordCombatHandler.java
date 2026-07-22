package com.mushokucraft.combat;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.combat.entity.ThrownSwordEntity;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModEffects;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.EventResult;
// removed ModAttachments

public class SwordCombatHandler {
    
    public static final Set<UUID> DEBUG_PLAYERS = new HashSet<>();
    private static final Map<UUID, Integer> stanceHits = new ConcurrentHashMap<>();

    public static void changeStance(Player player, String stanceId) {
        if (player == null) return;
        PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
        
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

    public static void register() {
        PlayerEvent.PLAYER_QUIT.register(player -> {
            if (player instanceof ServerPlayer) {
                stanceHits.remove(player.getUUID());
            }
        });

        TickEvent.PLAYER_POST.register(player -> {
            PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
            if (mastery == null) return;

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
                    // Architectury networking can be adapted if PacketDistributor is changed to NetworkManager.
                    // keeping neoforge packet send for now as requested to mostly keep logic intact
                    dev.architectury.networking.NetworkManager.sendToPlayers(sp.getServer().getPlayerList().getPlayers(), new com.mushokucraft.network.SyncToukiPacket(sp.getId(), false));
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
        });

        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
            if (mastery == null) return dev.architectury.event.CompoundEventResult.pass();

            SwordStyle stance = mastery.getActiveStance();

            if (stance == SwordStyle.SWORD_GOD && hand == net.minecraft.world.InteractionHand.OFF_HAND) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    return dev.architectury.event.CompoundEventResult.interruptFalse(null);
                }
            }
            return dev.architectury.event.CompoundEventResult.pass();
        });

        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
            if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && hand == net.minecraft.world.InteractionHand.OFF_HAND) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
            if (mastery != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && hand == net.minecraft.world.InteractionHand.OFF_HAND) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide) return EventResult.pass();

            if (source.getEntity() instanceof Player attacker) {
                PlayerMasteryData mastery = PlayerMasteryProvider.get(attacker);
                if (mastery != null) {
                    if (attacker instanceof net.minecraft.server.level.ServerPlayer sp && attacker.getMainHandItem().getItem() instanceof SwordItem && mastery.getActiveStance() != null) {
                        boolean isDummy = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().contains("dummy");
                        if (isDummy) {
                            com.mushokucraft.event.ModGameEvents.addStanceMastery(sp, mastery.getActiveStance(), MushokuConfig.DUMMY_MASTERY_GAIN.get().floatValue());
                        } else if (mastery.getActiveStance() == SwordStyle.SWORD_GOD && amount >= MushokuConfig.SWORD_GOD_HIGH_DAMAGE_THRESHOLD.get().floatValue()) {
                            com.mushokucraft.event.ModGameEvents.addStanceMastery(sp, SwordStyle.SWORD_GOD, MushokuConfig.SWORD_GOD_HIGH_DAMAGE_MASTERY_GAIN.get().floatValue());
                        }
                    }
                }
            }
            return EventResult.pass();
        });
    }
}
