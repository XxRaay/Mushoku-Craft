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
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;

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
        int attempts = be.getSize() == 3 ? 6 : 2;
        float manaCost = MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_MANA_PER_GROWTH.get().floatValue();
        boolean playedSound = false;

        for (int i = 0; i < attempts; i++) {
            if (be.getCurrentMana() < manaCost) break;

            int rx = pos.getX() + level.random.nextInt(radius * 2 + 1) - radius;
            int rz = pos.getZ() + level.random.nextInt(radius * 2 + 1) - radius;
            int ry = pos.getY() + level.random.nextInt(7) - 3;
            BlockPos targetPos = new BlockPos(rx, ry, rz);
            BlockState targetState = level.getBlockState(targetPos);

            // 1. Farmland hydration
            if (targetState.is(Blocks.FARMLAND) && targetState.hasProperty(FarmBlock.MOISTURE)) {
                int moisture = targetState.getValue(FarmBlock.MOISTURE);
                if (moisture < 7 && be.getCurrentMana() >= 1.0f) {
                    level.setBlock(targetPos, targetState.setValue(FarmBlock.MOISTURE, 7), 3);
                    be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - 1.0f));
                    be.setChanged();

                    level.sendParticles(ParticleTypes.SPLASH, rx + 0.5, ry + 0.8, rz + 0.5, 3, 0.2, 0.1, 0.2, 0.02);
                    continue;
                }
            }

            // 2. Bonemealable crops, saplings, stems, bushes
            if (targetState.getBlock() instanceof BonemealableBlock bonemealable) {
                if (bonemealable.isValidBonemealTarget(level, targetPos, targetState)) {
                    if (bonemealable.isBonemealSuccess(level, level.random, targetPos, targetState)) {
                        bonemealable.performBonemeal(level, level.random, targetPos, targetState);
                        be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - manaCost));
                        be.setChanged();

                        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, rx + 0.5, ry + 0.5, rz + 0.5,
                                5, 0.25, 0.25, 0.25, 0.05);

                        if (!playedSound) {
                            level.playSound(null, targetPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.4f, 1.2f);
                            playedSound = true;
                        }
                    }
                    continue;
                }
            }

            // 3. Sugar cane, cactus, bamboo, nether wart
            if (targetState.getBlock() instanceof SugarCaneBlock
                    || targetState.getBlock() instanceof CactusBlock
                    || targetState.getBlock() instanceof BambooStalkBlock
                    || targetState.getBlock() instanceof NetherWartBlock) {
                targetState.randomTick(level, targetPos, level.random);
                be.setCurrentMana(Math.max(0.0f, be.getCurrentMana() - manaCost));
                be.setChanged();

                level.sendParticles(ParticleTypes.COMPOSTER, rx + 0.5, ry + 0.5, rz + 0.5,
                        3, 0.2, 0.2, 0.2, 0.02);
                if (!playedSound) {
                    level.playSound(null, targetPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.4f, 1.2f);
                    playedSound = true;
                }
            }
        }
    }
}
