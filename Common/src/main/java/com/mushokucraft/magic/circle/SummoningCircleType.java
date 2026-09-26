package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.companion.SummonCompanionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SummoningCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "summoning");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.summoning");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.summoning.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level != null && level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            if (be.hasCapturedMob()) {
                float maxHp = be.getCapturedEntityMaxHp();
                float base = MushokuConfig.MAGIC_CIRCLE_SUMMON_BASE_MANA.get().floatValue();
                float perHp = MushokuConfig.MAGIC_CIRCLE_SUMMON_MANA_PER_HP.get().floatValue();
                return base + maxHp * perHp;
            }
        }
        return 0.0f;
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.25, cz, 4, 0.35, 0.1, 0.35, 0.05);
        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.35, cz, 3, 0.3, 0.15, 0.3, 0.02);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.8f;
            level.playSound(null, pos, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.BLOCKS, 0.6f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            if (!be.hasCapturedMob()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.summon_requires_mob"), true);
                }
                return false;
            }

            String entityId = be.getCapturedEntityId();
            CompoundTag entityTag = be.getCapturedEntityTag();
            String name = be.getCapturedEntityName();

            ResourceLocation rl = ResourceLocation.tryParse(entityId);
            if (rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
                Entity entity = type.create(level);
                if (entity != null) {
                    if (entityTag != null) {
                        try {
                            entity.load(entityTag);
                        } catch (Exception ignored) {}
                    }

                    // Reset UUID to avoid clashes
                    entity.setUUID(UUID.randomUUID());
                    entity.moveTo(origin.getX() + 0.5, origin.getY() + 0.1, origin.getZ() + 0.5,
                            level.random.nextFloat() * 360.0f, 0.0f);

                    if (entity instanceof LivingEntity le) {
                        le.setHealth(le.getMaxHealth());
                    }

                    // Bind entity to player as loyal companion/familiar
                    if (player != null) {
                        SummonCompanionManager.makeCompanion(entity, player);
                    }

                    level.addFreshEntity(entity);

                    // Spectacular summon ritual effects
                    level.playSound(null, origin, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.2f, 1.0f);
                    level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.4f);
                    level.playSound(null, origin, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.9f, 1.2f);
                    level.sendParticles(ParticleTypes.FLASH, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, 2, 0, 0, 0, 0);
                    level.sendParticles(ParticleTypes.FIREWORK, origin.getX() + 0.5, origin.getY() + 0.6, origin.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.1);
                    level.sendParticles(ParticleTypes.SOUL, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, 20, 0.35, 0.35, 0.35, 0.05);

                    if (player != null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.summon_success", name), true);
                    }

                    // Clear captured mob and reset circle mana
                    be.clearCapturedMob();
                    be.setCurrentMana(0.0f);
                    be.setChanged();
                    level.sendBlockUpdated(origin, be.getBlockState(), be.getBlockState(), 3);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        if (be.hasCapturedMob()) {
            if (level.getGameTime() % 12 == 0) {
                level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                        2, 0.2, 0.15, 0.2, 0.05);
            }
        }
    }
}
