package com.mushokucraft.block;

import com.mushokucraft.block.entity.BarrierWallBlockEntity;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarrierWallBlock extends BaseEntityBlock {
    public static final MapCodec<BarrierWallBlock> CODEC = simpleCodec(BarrierWallBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // 1-pixel thick boundaries
    private static final VoxelShape NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.box(15, 0, 0, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = Block.box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape DEFAULT_SHAPE = Block.box(0, 0, 0, 16, 16, 1);

    public BarrierWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape combined = Shapes.empty();
        if (state.getValue(NORTH))
            combined = Shapes.or(combined, NORTH_SHAPE);
        if (state.getValue(SOUTH))
            combined = Shapes.or(combined, SOUTH_SHAPE);
        if (state.getValue(EAST))
            combined = Shapes.or(combined, EAST_SHAPE);
        if (state.getValue(WEST))
            combined = Shapes.or(combined, WEST_SHAPE);
        return combined.isEmpty() ? DEFAULT_SHAPE : combined;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ecc && ecc.getEntity() != null) {
            Entity entity = ecc.getEntity();
            if (entity instanceof Player player) {
                if (level.getBlockEntity(pos) instanceof BarrierWallBlockEntity wallBE) {
                    UUID owner = wallBE.getOwnerUUID();
                    if (owner == null && wallBE.getCirclePos() != null) {
                        if (level.getBlockEntity(wallBE.getCirclePos()) instanceof MagicCircleBlockEntity circleBE) {
                            owner = circleBE.getOwnerUUID();
                            wallBE.setOwnerUUID(owner);
                        }
                    }
                    if (player.getUUID().equals(owner)) {
                        return Shapes.empty(); // Owner/caster walks freely through their barrier!
                    }
                }
            }
        }
        return getShape(state, level, pos, context); // Solid for mobs, other players, projectiles!
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof BarrierWallBlockEntity wallBE) {
                BlockPos cPos = wallBE.getCirclePos();
                if (cPos != null && level.getBlockEntity(cPos) instanceof MagicCircleBlockEntity circleBE) {
                    float current = circleBE.getCurrentMana();
                    if (circleBE.isBarrierActive()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status",
                                String.format("%.0f", current),
                                String.format("%.0f", circleBE.getBarrierHealth()),
                                String.format("%.0f", circleBE.getBarrierMaxHealth())), true);
                    } else {
                        float req = circleBE.getRequiredMana();
                        if (req <= 0.0f && circleBE.getCircleType() != null) {
                            req = circleBE.getCircleType().calculateRequiredMana(level, cPos, circleBE.getLinkedPos());
                        }
                        int pct = (int) ((current / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.infusing_mana",
                                String.format("%.0f", current),
                                String.format("%.0f", req),
                                pct), true);
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrierWallBlockEntity(pos, state);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof BarrierWallBlockEntity wallBE) {
                BlockPos cPos = wallBE.getCirclePos();
                if (cPos != null && level.getBlockEntity(cPos) instanceof MagicCircleBlockEntity circleBE) {
                    float attackDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float dmg = Math.max(3.0f, attackDmg);

                    circleBE.damageBarrier(dmg, player);

                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0f, 1.3f);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + 0.5, pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                8, 0.25, 0.25, 0.25, 0.1);
                    }
                }
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide()) {
            BlockPos hitPos = hit.getBlockPos();
            if (level.getBlockEntity(hitPos) instanceof BarrierWallBlockEntity wallBE) {
                BlockPos cPos = wallBE.getCirclePos();
                if (cPos != null && level.getBlockEntity(cPos) instanceof MagicCircleBlockEntity circleBE) {
                    float speed = (float) projectile.getDeltaMovement().length();
                    float dmg = Math.max(2.0f, speed * 4.0f);

                    circleBE.damageBarrier(dmg, projectile.getOwner());

                    level.playSound(null, hitPos, SoundEvents.SHIELD_BLOCK, SoundSource.BLOCKS, 1.0f, 1.2f);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, hit.getLocation().x, hit.getLocation().y,
                                hit.getLocation().z,
                                6, 0.1, 0.1, 0.1, 0.05);
                    }
                    projectile.discard();
                }
            }
        }
    }
}
