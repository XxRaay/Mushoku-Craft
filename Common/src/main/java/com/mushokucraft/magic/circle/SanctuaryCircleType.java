package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.companion.SummonCompanionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SanctuaryCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "sanctuary");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.sanctuary");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.sanctuary.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level != null && level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            return be.getSize() == 3
                    ? MushokuConfig.MAGIC_CIRCLE_SANCTUARY_3X3_MANA.get().floatValue()
                    : MushokuConfig.MAGIC_CIRCLE_SANCTUARY_1X1_MANA.get().floatValue();
        }
        return MushokuConfig.MAGIC_CIRCLE_SANCTUARY_1X1_MANA.get().floatValue();
    }

    public static int getRadius(MagicCircleBlockEntity be) {
        return be.getSize() == 3
                ? MushokuConfig.MAGIC_CIRCLE_SANCTUARY_3X3_RADIUS.get()
                : MushokuConfig.MAGIC_CIRCLE_SANCTUARY_1X1_RADIUS.get();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.25, cz, 3, 0.35, 0.1, 0.35, 0.05);
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.3, cz, 4, 0.3, 0.15, 0.3, 0.05);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        // Continuous aura powered by stored mana pool
        return false;
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        float mana = be.getCurrentMana();
        if (mana <= 0.0f) return;

        // Ambient sanctuary sparkles
        if (level.getGameTime() % 20 == 0) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    2, 0.35, 0.1, 0.35, 0.02);
            level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5,
                    1, 0.25, 0.05, 0.25, 0.01);
        }

        // Triage interval: every 20 ticks (1 second)
        if (level.getGameTime() % 20 != 0) return;

        int radius = getRadius(be);
        AABB box = new AABB(pos).inflate(radius, 4.0, radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && isFriendly(e, be.getOwnerUUID()));

        if (targets.isEmpty()) return;

        float manaPerCleanse = MushokuConfig.MAGIC_CIRCLE_SANCTUARY_MANA_PER_CLEANSE.get().floatValue();
        float manaPerHeal = MushokuConfig.MAGIC_CIRCLE_SANCTUARY_MANA_PER_HEAL.get().floatValue();
        float healRate = MushokuConfig.MAGIC_CIRCLE_SANCTUARY_HEAL_PER_SEC.get().floatValue();
        boolean playedSound = false;

        for (LivingEntity target : targets) {
            if (be.getCurrentMana() <= 0.0f) break;

            // 1. Cleansing harmful status effects (poison, wither, blindness, bleeding, etc.)
            List<MobEffectInstance> harmful = new ArrayList<>();
            for (MobEffectInstance inst : target.getActiveEffects()) {
                if (!inst.getEffect().value().isBeneficial()) {
                    harmful.add(inst);
                }
            }

            for (MobEffectInstance bad : harmful) {
                if (be.getCurrentMana() >= manaPerCleanse) {
                    target.removeEffect(bad.getEffect());
                    be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - manaPerCleanse));
                    be.setChanged();

                    level.sendParticles(ParticleTypes.WAX_OFF, target.getX(), target.getY() + 1.0, target.getZ(),
                            10, 0.35, 0.5, 0.35, 0.05);
                    if (!playedSound) {
                        level.playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.3f, 1.8f);
                        playedSound = true;
                    }
                }
            }

            // 2. Health regeneration
            if (be.getCurrentMana() >= manaPerHeal && target.getHealth() < target.getMaxHealth()) {
                float missingHp = target.getMaxHealth() - target.getHealth();
                float healAmount = Math.min(healRate, missingHp);
                float cost = healAmount * manaPerHeal;

                if (be.getCurrentMana() >= cost) {
                    target.heal(healAmount);
                    be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - cost));
                    be.setChanged();

                    level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight() + 0.2, target.getZ(),
                            1, 0.3, 0.2, 0.3, 0.02);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + 0.5, target.getZ(),
                            4, 0.3, 0.3, 0.3, 0.02);

                    if (!playedSound) {
                        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.5f, 1.6f);
                        playedSound = true;
                    }
                }
            }
        }
    }

    public static boolean isFriendly(LivingEntity entity, @Nullable UUID ownerUUID) {
        if (entity instanceof Enemy) return false;
        if (entity instanceof Player) return true;

        if (entity instanceof TamableAnimal tamable) {
            if (tamable.isTame()) {
                return ownerUUID == null || ownerUUID.equals(tamable.getOwnerUUID());
            }
            return false;
        }

        if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTags().contains(SummonCompanionManager.TAG_FAMILIAR)) {
            return ownerUUID == null || ownerUUID.equals(SummonCompanionManager.getOwnerUUID(mob));
        }

        return entity instanceof Animal || entity instanceof Villager;
    }
}
