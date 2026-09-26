package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.config.MushokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MagicCreationCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "magic_creation");

    public static List<ItemEntity> findItems(Level level, BlockPos pos, int size) {
        if (level == null) return List.of();
        double radius = (size == 3) ? 3.0 : 1.8;
        AABB box = new AABB(
                pos.getX() + 0.5 - radius, pos.getY() - 0.5, pos.getZ() + 0.5 - radius,
                pos.getX() + 0.5 + radius, pos.getY() + 2.5, pos.getZ() + 0.5 + radius
        );
        return level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.magic_creation");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.magic_creation.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level == null) return 0.0f;
        int size = 1;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            size = be.getSize();
        }

        List<ItemEntity> items = findItems(level, origin, size);
        CreationCircleRecipe recipe = CreationCircleRecipes.findRecipe(items, size);
        if (recipe != null) {
            double mult = MushokuConfig.MAGIC_CIRCLE_CREATION_MANA_MULT.get();
            float baseMana = recipe.getRequiredMana();
            if (size == 3) {
                baseMana *= 0.75f; // 25% discount on grand 3x3 circle!
            }
            return (float) (baseMana * mult);
        }
        return 0.0f;
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.15;
        double cz = pos.getZ() + 0.5;

        float progress = Math.min(1.0f, infusedSoFar / Math.max(1.0f, required));

        // Particle vortex converging inwards
        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.3, cz, 6, 0.4, 0.2, 0.4, 0.1);
        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.2, cz, 4, 0.3, 0.1, 0.3, 0.05);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy + 0.25, cz, 3, 0.25, 0.15, 0.25, 0.02);

        if (progress > 0.5f) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 0.2, cz, 3, 0.3, 0.1, 0.3, 0.02);
        }

        // Acoustic crescendo
        if (level.getGameTime() % 10 == 0) {
            float pitch = 0.7f + progress * 1.1f;
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.6f, pitch);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, pitch);
        }
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        if (level.getGameTime() % 25 == 0) {
            List<ItemEntity> items = findItems(level, pos, be.getSize());
            CreationCircleRecipe recipe = CreationCircleRecipes.findRecipe(items, be.getSize());
            if (recipe != null) {
                // Subtle beacon glow indicating ready for infusion
                level.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                        3, 0.25, 0.1, 0.25, 0.02);
            }
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        int size = 1;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            size = be.getSize();
        }

        List<ItemEntity> items = findItems(level, origin, size);
        CreationCircleRecipe recipe = CreationCircleRecipes.findRecipe(items, size);

        if (recipe == null) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.creation_requires_ingredients"), true);
            }
            return false;
        }

        // 1. Consume required ingredients
        recipe.consume(items);

        // 2. Spawn the forged output artifact
        ItemStack output = recipe.createOutput();
        ItemEntity outputEntity = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.35, origin.getZ() + 0.5, output);
        outputEntity.setDefaultPickUpDelay();
        level.addFreshEntity(outputEntity);

        // 3. Audio & Visual Spectacle
        double cx = origin.getX() + 0.5;
        double cy = origin.getY() + 0.35;
        double cz = origin.getZ() + 0.5;

        level.playSound(null, origin, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
        level.playSound(null, origin, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, origin, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.1f);
        level.playSound(null, origin, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.5f);

        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 40, 0.4, 0.3, 0.4, 0.3);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy, cz, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 30, 0.5, 0.4, 0.5, 0.08);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy, cz, 25, 0.4, 0.2, 0.4, 0.1);

        if (player != null) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.creation_success", output.getHoverName()), true);
        }

        return true;
    }
}
