package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class SoulRecallHandler {
    public static void register() {
        EntityEvent.LIVING_DEATH.register((livingEntity, damageSource) -> {
            if (!(livingEntity instanceof ServerPlayer player)) {
                return EventResult.pass();
            }

            PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
            if (mastery == null || !mastery.hasSoulAnchor()) {
                return EventResult.pass();
            }

            BlockPos anchorPos = mastery.getSoulAnchorPos();
            ResourceLocation anchorDim = mastery.getSoulAnchorDim();
            if (anchorPos == null || anchorDim == null) {
                return EventResult.pass();
            }

            ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, anchorDim));
            if (targetLevel == null) {
                mastery.clearSoulAnchor();
                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_recall_failed_no_world"), false);
                return EventResult.pass();
            }

            // Ensure chunk is loaded
            if (!targetLevel.hasChunk(anchorPos.getX() >> 4, anchorPos.getZ() >> 4)) {
                targetLevel.getChunk(anchorPos.getX() >> 4, anchorPos.getZ() >> 4);
            }

            if (!(targetLevel.getBlockEntity(anchorPos) instanceof MagicCircleBlockEntity anchorBE)
                    || !anchorBE.isSoulAnchor()
                    || !anchorBE.isBoundTo(player.getUUID())) {
                mastery.clearSoulAnchor();
                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_recall_failed_destroyed"), false);
                return EventResult.pass();
            }

            float recallCost = MushokuConfig.MAGIC_CIRCLE_SOUL_ANCHOR_RECALL_COST.get().floatValue();
            if (anchorBE.getCurrentMana() < recallCost) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_recall_failed_no_mana",
                        String.format("%.0f", anchorBE.getCurrentMana()),
                        String.format("%.0f", recallCost)), false);
                return EventResult.pass();
            }

            // --- EXECUTE SOUL RECALL ---
            // 1. Deduct recall mana from the anchor altar
            anchorBE.setCurrentMana(Math.max(0.0f, anchorBE.getCurrentMana() - recallCost));
            anchorBE.setChanged();
            targetLevel.sendBlockUpdated(anchorPos, anchorBE.getBlockState(), anchorBE.getBlockState(), 3);

            // 2. Play death-site effects before warping
            ServerLevel deathLevel = player.serverLevel();
            double dx = player.getX();
            double dy = player.getY();
            double dz = player.getZ();
            deathLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, dx, dy + 1.0, dz, 50, 0.6, 0.8, 0.6, 0.15);
            deathLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, dx, dy + 0.5, dz, 30, 0.5, 0.6, 0.5, 0.05);
            deathLevel.playSound(null, dx, dy, dz, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2f, 0.9f);

            // 3. Cleanse and restore player
            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();

            // Emergency protective boons
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2, false, false, true)); // 10s Resistance III
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0, false, false, true));    // 20s Fire Resistance
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, false, true));       // 10s Regen II
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, false, false, true));         // 30s Absorption II

            // 4. Teleport directly to the center of the Soul Anchor altar
            player.teleportTo(targetLevel, anchorPos.getX() + 0.5, anchorPos.getY() + 0.1, anchorPos.getZ() + 0.5, player.getYRot(), player.getXRot());

            // 5. Altar arrival effects
            targetLevel.sendParticles(ParticleTypes.FLASH, anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5, 2, 0, 0, 0, 0);
            targetLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5, 70, 1.2, 0.5, 1.2, 0.15);
            targetLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, anchorPos.getX() + 0.5, anchorPos.getY() + 0.3, anchorPos.getZ() + 0.5, 45, 0.9, 0.3, 0.9, 0.05);
            targetLevel.playSound(null, anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.4f);
            targetLevel.playSound(null, anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 1.1f);
            targetLevel.playSound(null, anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0f, 1.2f);

            // 6. Notify player
            player.displayClientMessage(Component.translatable("message.mushokucraft.soul_recall_success",
                    String.format("%.0f", anchorBE.getCurrentMana())), false);

            // 7. Cancel death!
            return EventResult.interruptFalse();
        });
    }
}
