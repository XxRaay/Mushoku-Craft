package com.mushokucraft.magic;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.event.ModGameEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles mana progression and anime-accurate mana capacity scaling.
 * In Mushoku Tensei, using magic repeatedly expands your total mana capacity,
 * with dramatic growth occurring when pushing past limits and exhausting mana reserves.
 */
public class ManaProgressionManager {

    private static final Map<UUID, Long> LAST_EXHAUSTION_ALERT = new HashMap<>();
    private static final long EXHAUSTION_COOLDOWN_MS = 6000L; // Don't spam exhaustion effects more than once every 6s

    /**
     * Applies mana capacity progression whenever a spell is cast or channeled.
     *
     * @param player    The server player casting the spell.
     * @param data      The player's mastery data.
     * @param manaSpent The actual mana expended on the cast or charge.
     * @param spell     The spell that was used.
     */
    public static void applySpellManaGrowth(ServerPlayer player, PlayerMasteryData data, float manaSpent, Spell spell) {
        if (player == null || data == null || manaSpent <= 0f || spell == null) {
            return;
        }

        // In anime lore, swordsmen develop Touki (physical aura), not mage mana pools.
        if (spell.getSchool() == MagicSchool.SWORD_ARTS && !MushokuConfig.ALLOW_SWORD_ARTS_MANA_GROWTH.get()) {
            return;
        }

        float currentMana = data.getMana();
        float currentMax = data.getMaxMana();

        float growth = MasteryCalculator.calculateMaxManaGrowth(manaSpent, spell.getRank(), currentMana, currentMax);
        if (growth > 0f) {
            int oldMaxInt = (int) currentMax;
            data.addMaxMana(growth);
            int newMaxInt = (int) data.getMaxMana();

            // Milestone: reached a new integer threshold of max mana
            if (newMaxInt > oldMaxInt) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.6f);

                if (MushokuConfig.SHOW_MANA_GROWTH_NOTIFICATIONS.get()) {
                    player.displayClientMessage(
                            Component.translatable("message.mushokucraft.max_mana_increased", oldMaxInt, newMaxInt),
                            true
                    );
                }
            }
        }

        // Exhaustion Limit-Break: Player drained their mana to the very limit
        if (currentMana <= 0.5f && MushokuConfig.MANA_EXHAUSTION_EFFECTS.get()) {
            triggerExhaustionFeedback(player);
        }

        ModGameEvents.syncMana(player, data);
    }

    /**
     * Atmospheric feedback when the player reaches 0 mana through magical exertion.
     */
    private static void triggerExhaustionFeedback(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long lastTime = LAST_EXHAUSTION_ALERT.get(player.getUUID());
        if (lastTime != null && (now - lastTime) < EXHAUSTION_COOLDOWN_MS) {
            return;
        }
        LAST_EXHAUSTION_ALERT.put(player.getUUID(), now);

        // Tactile fatigue: brief Slowness I (2 seconds)
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, true));

        // Atmospheric heartbeat sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.9f, 0.8f);

        // Message
        player.displayClientMessage(Component.translatable("message.mushokucraft.mana_exhaustion"), true);
    }

    /**
     * Completely restores mana when the player sleeps through the night,
     * fulfilling the anime daily training loop (train until empty -> sleep -> awake full).
     */
    public static void handleSleepRestoration(ServerPlayer player, PlayerMasteryData data) {
        if (!MushokuConfig.SLEEP_FULL_MANA_RESTORE.get() || player == null || data == null) {
            return;
        }

        if (data.getMana() < data.getMaxMana()) {
            data.fullRestore();
            ModGameEvents.syncMana(player, data);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);

            player.displayClientMessage(Component.translatable("message.mushokucraft.mana_restored_sleep"), true);
        }
    }
}
