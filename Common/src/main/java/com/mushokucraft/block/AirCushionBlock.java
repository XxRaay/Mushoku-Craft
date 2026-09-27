package com.mushokucraft.block;

import com.mushokucraft.block.entity.AirCushionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

public class AirCushionBlock extends BaseEntityBlock {
    public static final MapCodec<AirCushionBlock> CODEC = simpleCodec(AirCushionBlock::new);
    
    // The blockbench model is 1 pixel high at the bottom.
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public AirCushionBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F; // No shading for translucent block
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        for (int i = 0; i < 2; ++i) {
            double dx = (random.nextDouble() - 0.5) * 1.2;
            double dy = random.nextDouble() * 0.5;
            double dz = (random.nextDouble() - 0.5) * 1.2;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD, pos.getX() + 0.5 + dx, pos.getY() + dy, pos.getZ() + 0.5 + dz, 0, 0.05, 0);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Negate fall damage
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AirCushionBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        return createTickerHelper(blockEntityType, com.mushokucraft.init.ModBlockEntities.AIR_CUSHION_BE.get(), AirCushionBlockEntity::tick);
    }
}
