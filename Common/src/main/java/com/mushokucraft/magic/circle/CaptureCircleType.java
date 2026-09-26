package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CaptureCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "capture");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.capture");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.capture.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        return MushokuConfig.MAGIC_CIRCLE_CAPTURE_BASE_MANA.get().floatValue();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 0.2, cz, 3, 0.35, 0.1, 0.35, 0.02);
        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.3, cz, 4, 0.3, 0.15, 0.3, 0.05);

        if (level.getGameTime() % 15 == 0) {
            float pitch = 0.7f + (infusedSoFar / Math.max(1.0f, required)) * 0.9f;
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6f, pitch);
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        // Capture circle acts as a persistent passive trap while charged with mana
        return false;
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        if (be.hasCapturedMob()) {
            // Idle ambient soul particles for trapped projection
            if (level.getGameTime() % 10 == 0) {
                level.sendParticles(ParticleTypes.SOUL, pos.getX() + 0.5, pos.getY() + 0.45, pos.getZ() + 0.5,
                        1, 0.15, 0.15, 0.15, 0.02);
            }
            return;
        }

        if (be.getCurrentMana() <= 0.0f) return;

        // Ambient idle charge particles
        if (level.getGameTime() % 20 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    2, 0.3, 0.05, 0.3, 0.01);
        }

        int size = be.getSize();
        double halfSize = (size == 3) ? 1.5 : 0.5;
        AABB box = new AABB(pos.getX() + 0.5 - halfSize, pos.getY(), pos.getZ() + 0.5 - halfSize,
                pos.getX() + 0.5 + halfSize, pos.getY() + 1.2, pos.getZ() + 0.5 + halfSize);

        List<LivingEntity> trapped = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> !(e instanceof Player) && e.isAlive());

        if (trapped.isEmpty()) return;

        // Damage interval: every 6 ticks (~3.3 times per second)
        if (level.getGameTime() % 6 != 0) return;

        float dmgRate = MushokuConfig.MAGIC_CIRCLE_CAPTURE_DAMAGE_RATE.get().floatValue();
        float manaCost = dmgRate * MushokuConfig.MAGIC_CIRCLE_CAPTURE_MANA_PER_DAMAGE.get().floatValue();

        for (LivingEntity target : trapped) {
            if (!target.isAlive() || be.hasCapturedMob()) break;
            if (be.getCurrentMana() < manaCost) break;

            be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - manaCost));
            be.setChanged();

            if (target.getHealth() <= dmgRate) {
                // Trap kills mob -> Capture soul into circle!
                CompoundTag mobTag = new CompoundTag();
                target.saveWithoutId(mobTag);
                String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
                float maxHp = target.getMaxHealth();
                String displayName = target.getName().getString();

                be.setCapturedMob(entityId, mobTag, displayName, maxHp);
                be.setChanged();
                level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);

                // Spectacular capture effects
                level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.6f);
                level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.8f, 1.8f);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        25, 0.35, 0.2, 0.35, 0.05);
                level.sendParticles(ParticleTypes.FLASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1, 0, 0, 0, 0);

                // Notify players around
                for (Player p : level.getEntitiesOfClass(Player.class, box.inflate(12.0))) {
                    p.displayClientMessage(Component.translatable("message.mushokucraft.entity_captured", displayName), true);
                }

                target.discard();
                break;
            } else {
                // Deal magic damage to target
                target.hurt(level.damageSources().magic(), dmgRate);
                level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 0.5, target.getZ(),
                        8, 0.2, 0.2, 0.2, 0.05);
                level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6f, 1.3f);
            }
        }
    }
}
