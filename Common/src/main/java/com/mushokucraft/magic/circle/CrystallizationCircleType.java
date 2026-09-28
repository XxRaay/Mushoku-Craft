package com.mushokucraft.magic.circle;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class CrystallizationCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "crystallization");

    public record CrystallizationRecipe(Item input, Item output, float manaCost) {}

    private static final CrystallizationRecipe[] RECIPES = new CrystallizationRecipe[] {
            // Small crystal (50 mana): Amethyst Shard, Quartz
            new CrystallizationRecipe(Items.AMETHYST_SHARD, ModItems.SMALL_MANA_CRYSTAL.get(), 50.0f),
            new CrystallizationRecipe(Items.QUARTZ, ModItems.SMALL_MANA_CRYSTAL.get(), 50.0f),

            // Medium crystal (150 mana): Amethyst Block, Lapis Lazuli, Lapis Block
            new CrystallizationRecipe(Items.AMETHYST_BLOCK, ModItems.MEDIUM_MANA_CRYSTAL.get(), 150.0f),
            new CrystallizationRecipe(Items.LAPIS_LAZULI, ModItems.MEDIUM_MANA_CRYSTAL.get(), 150.0f),
            new CrystallizationRecipe(Items.LAPIS_BLOCK, ModItems.MEDIUM_MANA_CRYSTAL.get(), 150.0f),

            // Large crystal (400 mana): Diamond, Emerald
            new CrystallizationRecipe(Items.DIAMOND, ModItems.LARGE_MANA_CRYSTAL.get(), 400.0f),
            new CrystallizationRecipe(Items.EMERALD, ModItems.LARGE_MANA_CRYSTAL.get(), 400.0f)
    };

    public static CrystallizationRecipe getRecipe(Item item) {
        for (CrystallizationRecipe r : RECIPES) {
            if (r.input() == item) {
                return r;
            }
        }
        return null;
    }

    public static List<CrystallizationRecipe> getAllRecipes() {
        return List.of(RECIPES);
    }

    public static ItemEntity findTargetItemEntity(Level level, BlockPos pos, int size) {
        if (level == null) return null;
        double halfSize = (size == 3) ? 1.5 : 0.6;
        AABB box = new AABB(
                pos.getX() + 0.5 - halfSize, pos.getY(), pos.getZ() + 0.5 - halfSize,
                pos.getX() + 0.5 + halfSize, pos.getY() + 0.8, pos.getZ() + 0.5 + halfSize
        );
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box,
                e -> e.isAlive() && getRecipe(e.getItem().getItem()) != null);
        if (items.isEmpty()) return null;
        return items.get(0);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.crystallization");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.crystallization.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level == null) return 0.0f;
        int size = 1;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            size = be.getSize();
        }
        ItemEntity target = findTargetItemEntity(level, origin, size);
        if (target != null) {
            CrystallizationRecipe recipe = getRecipe(target.getItem().getItem());
            if (recipe != null) {
                return recipe.manaCost();
            }
        }
        return 0.0f;
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.ENCHANT, cx, cy + 0.25, cz, 4, 0.3, 0.1, 0.3, 0.05);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy + 0.2, cz, 2, 0.25, 0.1, 0.25, 0.02);

        if (level.getGameTime() % 12 == 0) {
            float pitch = 0.8f + (infusedSoFar / Math.max(1.0f, required)) * 0.9f;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7f, pitch);
        }
    }

    @Override
    public void onServerTick(ServerLevel level, BlockPos pos, MagicCircleBlockEntity be) {
        if (level.getGameTime() % 20 == 0) {
            ItemEntity target = findTargetItemEntity(level, pos, be.getSize());
            if (target != null) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5,
                        1, 0.2, 0.05, 0.2, 0.01);
            }
        }
    }

    @Override
    public boolean onTrigger(ServerLevel level, BlockPos origin, BlockPos destination, Player player) {
        int size = 1;
        if (level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            size = be.getSize();
        }
        ItemEntity target = findTargetItemEntity(level, origin, size);
        if (target == null) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_requires_mineral"), true);
            }
            return false;
        }

        CrystallizationRecipe recipe = getRecipe(target.getItem().getItem());
        if (recipe == null) return false;

        // Consume 1 unit of input
        ItemStack stack = target.getItem();
        stack.shrink(1);
        if (stack.isEmpty()) {
            target.discard();
        } else {
            target.setItem(stack);
        }

        // Spawn output crystal
        ItemStack output = new ItemStack(recipe.output());
        ItemEntity outputEntity = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.25, origin.getZ() + 0.5, output);
        outputEntity.setDefaultPickUpDelay();
        level.addFreshEntity(outputEntity);

        // Visual and sound effects
        level.playSound(null, origin, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.2f, 1.2f);
        level.playSound(null, origin, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.5f);
        level.sendParticles(ParticleTypes.END_ROD, origin.getX() + 0.5, origin.getY() + 0.3, origin.getZ() + 0.5, 15, 0.25, 0.25, 0.25, 0.05);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, origin.getX() + 0.5, origin.getY() + 0.25, origin.getZ() + 0.5, 20, 0.3, 0.2, 0.3, 0.08);

        if (player != null) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_success", output.getHoverName()), true);
        }
        return true;
    }
}
