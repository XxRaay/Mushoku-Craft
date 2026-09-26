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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OvergrowthCircleType implements MagicCircleType {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "overgrowth");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("magic_circle.mushokucraft.overgrowth");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("magic_circle.mushokucraft.overgrowth.desc");
    }

    @Override
    public float calculateRequiredMana(Level level, BlockPos origin, BlockPos destination) {
        if (level != null && level.getBlockEntity(origin) instanceof MagicCircleBlockEntity be) {
            return be.getSize() == 3
                    ? MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_3X3_MANA.get().floatValue()
                    : MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_1X1_MANA.get().floatValue();
        }
        return MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_1X1_MANA.get().floatValue();
    }

    public static int getRadius(MagicCircleBlockEntity be) {
        return be.getSize() == 3
                ? MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_3X3_RADIUS.get()
                : MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_1X1_RADIUS.get();
    }

    @Override
    public void onChannelTick(ServerLevel level, BlockPos pos, Player player, float infusedSoFar, float required) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.25, cz, 4, 0.35, 0.1, 0.35, 0.05);
        level.sendParticles(ParticleTypes.COMPOSTER, cx, cy + 0.3, cz, 3, 0.3, 0.15, 0.3, 0.02);

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

        // Ambient overgrowth sparkles
        if (level.getGameTime() % 20 == 0) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    2, 0.35, 0.1, 0.35, 0.02);
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
                    3, 0.4, 0.2, 0.4, 0.01);
        }

        // Growth pulse interval: every 15 ticks (~1.3 times per second)
        if (level.getGameTime() % 15 != 0) return;

        int radius = getRadius(be);
        int attempts = be.getSize() == 3 ? 8 : 3;
        float manaCost = MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_MANA_PER_GROWTH.get().floatValue();
        boolean playedSound = false;

        // Collect all eligible crop / plant / farmland positions within horizontal radius
        List<BlockPos> targets = new ArrayList<>();
        int minY = Math.max(level.getMinBuildHeight(), pos.getY() - 3);
        int maxY = Math.min(level.getMaxBuildHeight(), pos.getY() + 4);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;

                int x = pos.getX() + dx;
                int z = pos.getZ() + dz;

                // Look from top of column downwards
                for (int y = maxY; y >= minY; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);

                    if (s.isAir()) continue;

                    // If farmland: target found!
                    if (s.is(Blocks.FARMLAND)) {
                        targets.add(p);
                        break;
                    }

                    // If growable crop or plant: target found!
                    if (isEligiblePlant(s)) {
                        targets.add(p);
                        break;
                    }

                    // If solid ground/stone (not a crop/farmland), stop scanning deeper in this column
                    if (s.isSolidRender(level, p)) {
                        break;
                    }
                }
            }
        }

        if (targets.isEmpty()) return;

        // Randomize candidate order
        Collections.shuffle(targets, new Random(level.random.nextLong()));

        int grownCount = 0;
        for (BlockPos targetPos : targets) {
            if (grownCount >= attempts) break;
            if (be.getCurrentMana() < manaCost) break;

            BlockState targetState = level.getBlockState(targetPos);
            boolean acted = false;

            // 1. Farmland handling: hydrate soil and grow the crop directly above it!
            if (targetState.is(Blocks.FARMLAND)) {
                if (targetState.hasProperty(FarmBlock.MOISTURE)) {
                    int moisture = targetState.getValue(FarmBlock.MOISTURE);
                    if (moisture < 7) {
                        level.setBlock(targetPos, targetState.setValue(FarmBlock.MOISTURE, 7), 3);
                        level.sendParticles(ParticleTypes.SPLASH, targetPos.getX() + 0.5, targetPos.getY() + 0.8, targetPos.getZ() + 0.5, 3, 0.2, 0.1, 0.2, 0.02);
                        acted = true;
                    }
                }
                BlockPos abovePos = targetPos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (growSinglePlant(level, abovePos, aboveState)) {
                    acted = true;
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, abovePos.getX() + 0.5, abovePos.getY() + 0.4, abovePos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.05);
                }
            } else {
                // 2. Direct plant growth
                if (growSinglePlant(level, targetPos, targetState)) {
                    acted = true;
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, targetPos.getX() + 0.5, targetPos.getY() + 0.4, targetPos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.05);

                    // Also hydrate farmland underneath if present
                    BlockPos belowPos = targetPos.below();
                    BlockState belowState = level.getBlockState(belowPos);
                    if (belowState.is(Blocks.FARMLAND) && belowState.hasProperty(FarmBlock.MOISTURE)) {
                        if (belowState.getValue(FarmBlock.MOISTURE) < 7) {
                            level.setBlock(belowPos, belowState.setValue(FarmBlock.MOISTURE, 7), 3);
                        }
                    }
                }
            }

            if (acted) {
                grownCount++;
                be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - manaCost));
                be.setChanged();

                level.sendParticles(ParticleTypes.COMPOSTER, targetPos.getX() + 0.5, targetPos.getY() + 0.2, targetPos.getZ() + 0.5, 3, 0.2, 0.1, 0.2, 0.02);

                if (!playedSound) {
                    level.playSound(null, targetPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.45f, 1.15f + level.random.nextFloat() * 0.2f);
                    playedSound = true;
                }
            }
        }
    }

    /**
     * Checks if a block is a cultivateable crop or plant, strictly excluding grass blocks, weeds, moss, etc.
     */
    public static boolean isEligiblePlant(BlockState state) {
        // Strictly exclude grass blocks and lawn weeds
        if (state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.CRIMSON_NYLIUM)
                || state.is(Blocks.WARPED_NYLIUM)
                || state.is(Blocks.SPORE_BLOSSOM)) {
            return false;
        }

        Block block = state.getBlock();

        // Standard crops (Wheat, Carrots, Potatoes, Beetroot)
        if (block instanceof CropBlock crop) {
            return !crop.isMaxAge(state);
        }

        // Stems (Melons, Pumpkins)
        if (block instanceof StemBlock) {
            return true;
        }

        // Vertical stalk plants
        if (block instanceof SugarCaneBlock || block instanceof CactusBlock) {
            return true;
        }

        // Nether wart
        if (state.is(Blocks.NETHER_WART)) {
            return state.getValue(NetherWartBlock.AGE) < 3;
        }

        // Cocoa
        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) < 2;
        }

        // Berry bushes
        if (block instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) < 3;
        }

        // Saplings, Bamboo
        if (block instanceof SaplingBlock || block instanceof BambooStalkBlock || block instanceof BambooSaplingBlock) {
            return true;
        }

        // Sniffer crops
        if (state.is(Blocks.TORCHFLOWER_CROP) || state.is(Blocks.PITCHER_CROP)) {
            return true;
        }

        // Any other BonemealableBlock (modded crops, etc.)
        if (block instanceof BonemealableBlock) {
            return true;
        }

        return false;
    }

    /**
     * Attempts to advance the growth of a single plant/crop block.
     */
    private boolean growSinglePlant(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        // 1. Standard Crops
        if (block instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                crop.performBonemeal(level, level.random, pos, state);
                return true;
            }
            return false;
        }

        // 2. Pumpkin / Melon Stems
        if (block instanceof StemBlock stemBlock) {
            if (state.getValue(StemBlock.AGE) < 7) {
                stemBlock.performBonemeal(level, level.random, pos, state);
                return true;
            } else {
                // Fully grown stem: trigger random tick to spawn pumpkin/melon block!
                for (int i = 0; i < 3; i++) {
                    state.randomTick(level, pos, level.random);
                }
                return true;
            }
        }

        // 3. Sugar Cane
        if (block instanceof SugarCaneBlock) {
            BlockPos topPos = pos;
            while (level.getBlockState(topPos.above()).is(block)) {
                topPos = topPos.above();
            }
            int height = 1;
            while (level.getBlockState(topPos.below(height)).is(block)) {
                height++;
            }
            if (height < 3 && level.isEmptyBlock(topPos.above())) {
                level.setBlockAndUpdate(topPos.above(), block.defaultBlockState());
                return true;
            }
            return false;
        }

        // 4. Cactus
        if (block instanceof CactusBlock) {
            BlockPos topPos = pos;
            while (level.getBlockState(topPos.above()).is(block)) {
                topPos = topPos.above();
            }
            int height = 1;
            while (level.getBlockState(topPos.below(height)).is(block)) {
                height++;
            }
            if (height < 3 && level.isEmptyBlock(topPos.above())) {
                level.setBlockAndUpdate(topPos.above(), block.defaultBlockState());
                return true;
            }
            return false;
        }

        // 5. Nether Wart
        if (state.is(Blocks.NETHER_WART)) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age < 3) {
                level.setBlock(pos, state.setValue(NetherWartBlock.AGE, age + 1), 3);
                return true;
            }
            return false;
        }

        // 6. Generic Bonemealable (Saplings, Cocoa, Berries, Bamboo, etc.) EXCLUDING grass/moss
        if (block instanceof BonemealableBlock bonemealable) {
            if (state.is(Blocks.GRASS_BLOCK)
                    || state.is(Blocks.SHORT_GRASS)
                    || state.is(Blocks.TALL_GRASS)
                    || state.is(Blocks.SEAGRASS)
                    || state.is(Blocks.FERN)
                    || state.is(Blocks.LARGE_FERN)
                    || state.is(Blocks.MOSS_BLOCK)
                    || state.is(Blocks.CRIMSON_NYLIUM)
                    || state.is(Blocks.WARPED_NYLIUM)
                    || state.is(Blocks.SPORE_BLOSSOM)) {
                return false;
            }

            if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                if (bonemealable.isBonemealSuccess(level, level.random, pos, state)) {
                    bonemealable.performBonemeal(level, level.random, pos, state);
                    return true;
                }
            }
        }

        return false;
    }
}
