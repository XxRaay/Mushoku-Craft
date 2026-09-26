package com.mushokucraft.block;

import com.mojang.serialization.MapCodec;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.init.ModBlockEntities;
import com.mushokucraft.item.InscribedManuscriptItem;
import com.mushokucraft.magic.circle.MagicCircleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MagicCircleBlock extends BaseEntityBlock {
    public enum CirclePart implements StringRepresentable {
        CENTER("center", 0, 0),
        NORTH("north", 0, -1),
        SOUTH("south", 0, 1),
        WEST("west", -1, 0),
        EAST("east", 1, 0),
        NORTH_WEST("north_west", -1, -1),
        NORTH_EAST("north_east", 1, -1),
        SOUTH_WEST("south_west", -1, 1),
        SOUTH_EAST("south_east", 1, 1);

        private final String name;
        private final int dx;
        private final int dz;

        CirclePart(String name, int dx, int dz) {
            this.name = name;
            this.dx = dx;
            this.dz = dz;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getDx() {
            return dx;
        }

        public int getDz() {
            return dz;
        }

        public static CirclePart fromOffset(int dx, int dz) {
            for (CirclePart part : values()) {
                if (part.dx == dx && part.dz == dz) {
                    return part;
                }
            }
            return CENTER;
        }
    }

    public static final MapCodec<MagicCircleBlock> CODEC = simpleCodec(MagicCircleBlock::new);
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 1, 3);
    public static final BooleanProperty SHEARED = BooleanProperty.create("sheared");
    public static final EnumProperty<CirclePart> PART = EnumProperty.create("part", CirclePart.class);

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public MagicCircleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SIZE, 1)
                .setValue(SHEARED, false)
                .setValue(PART, CirclePart.CENTER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, SHEARED, PART);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        if (part != CirclePart.CENTER) {
            BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
            BlockState centerState = level.getBlockState(centerPos);
            return centerState.is(this) && centerState.hasProperty(PART) && centerState.getValue(PART) == CirclePart.CENTER;
        }
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Cutting off the paper underlay with shears
        if (stack.is(Items.SHEARS)) {
            CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
            BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
            BlockState centerState = level.getBlockState(centerPos);

            if (centerState.is(this) && !centerState.getValue(SHEARED)) {
                if (!level.isClientSide()) {
                    int size = centerState.getValue(SIZE);
                    level.setBlock(centerPos, centerState.setValue(SHEARED, true), 3);
                    if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
                        circleBE.setSheared(true);
                        circleBE.setChanged();
                        level.sendBlockUpdated(centerPos, centerState.setValue(SHEARED, true), centerState.setValue(SHEARED, true), 3);
                    }

                    if (size == 3) {
                        for (CirclePart p : CirclePart.values()) {
                            if (p == CirclePart.CENTER) continue;
                            BlockPos pPos = centerPos.offset(p.getDx(), 0, p.getDz());
                            BlockState pState = level.getBlockState(pPos);
                            if (pState.is(this)) {
                                level.setBlock(pPos, pState.setValue(SHEARED, true), 3);
                            }
                        }
                    }

                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);

                    // Drops 3 paper when paper underlay is cut away
                    ItemStack paper = new ItemStack(Items.PAPER, 3);
                    ItemEntity entity = new ItemEntity(level, centerPos.getX() + 0.5, centerPos.getY() + 0.1, centerPos.getZ() + 0.5, paper);
                    entity.setDefaultPickUpDelay();
                    level.addFreshEntity(entity);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
            BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());

            if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
                BlockPos linked = circleBE.getLinkedPos();
                float currentMana = circleBE.getCurrentMana();
                float required = circleBE.getRequiredMana();
                MagicCircleType type = circleBE.getCircleType();

                if (type == null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_inactive_unmatched"), true);
                } else if (circleBE.isBarrierActive()) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status",
                            String.format("%.0f", currentMana),
                            String.format("%.0f", circleBE.getBarrierHealth()),
                            String.format("%.0f", circleBE.getBarrierMaxHealth())), true);
                } else if (com.mushokucraft.magic.circle.TeleportCircleType.ID.equals(circleBE.getCircleTypeId()) && linked == null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_not_linked"), true);
                } else if (com.mushokucraft.magic.circle.TeleportCircleType.ID.equals(circleBE.getCircleTypeId()) && !circleBE.isDestinationValid(level)) {
                    // Target circle is missing/broken -> reset link!
                    circleBE.setLinked(null, null);
                    circleBE.setCurrentMana(0.0f);
                    circleBE.setChanged();
                    level.sendBlockUpdated(centerPos, level.getBlockState(centerPos), level.getBlockState(centerPos), 3);
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
                } else if (com.mushokucraft.magic.circle.SummoningCircleType.ID.equals(circleBE.getCircleTypeId())) {
                    if (circleBE.hasCapturedMob()) {
                        float req = required;
                        if (req <= 0.0f) {
                            req = type.calculateRequiredMana(level, centerPos, null);
                        }
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.summon_status",
                                circleBE.getCapturedEntityName(),
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.summon_requires_mob"), true);
                    }
                } else if (com.mushokucraft.magic.circle.CaptureCircleType.ID.equals(circleBE.getCircleTypeId())) {
                    if (circleBE.hasCapturedMob()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.capture_already_full",
                                circleBE.getCapturedEntityName()), true);
                    } else {
                        float req = required > 0.0f ? required : type.calculateRequiredMana(level, centerPos, null);
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.infusing_mana",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct), true);
                    }
                } else if (com.mushokucraft.magic.circle.CrystallizationCircleType.ID.equals(circleBE.getCircleTypeId())) {
                    net.minecraft.world.entity.item.ItemEntity target = com.mushokucraft.magic.circle.CrystallizationCircleType.findTargetItemEntity(level, centerPos, circleBE.getSize());
                    if (target != null) {
                        float req = required > 0.0f ? required : type.calculateRequiredMana(level, centerPos, null);
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_status",
                                target.getItem().getHoverName(),
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_requires_mineral"), true);
                    }
                } else if (com.mushokucraft.magic.circle.SanctuaryCircleType.ID.equals(circleBE.getCircleTypeId())) {
                    float req = required > 0.0f ? required : type.calculateRequiredMana(level, centerPos, null);
                    int radius = com.mushokucraft.magic.circle.SanctuaryCircleType.getRadius(circleBE);
                    if (currentMana > 0.0f) {
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.sanctuary_status",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct,
                                radius), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.sanctuary_empty"), true);
                    }
                } else if (com.mushokucraft.magic.circle.OvergrowthCircleType.ID.equals(circleBE.getCircleTypeId())) {
                    float req = required > 0.0f ? required : type.calculateRequiredMana(level, centerPos, null);
                    int radius = com.mushokucraft.magic.circle.OvergrowthCircleType.getRadius(circleBE);
                    if (currentMana > 0.0f) {
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.overgrowth_status",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct,
                                radius), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.overgrowth_empty"), true);
                    }
                } else {
                    float req = required;
                    if (req <= 0.0f) {
                        req = type.calculateRequiredMana(level, centerPos, linked);
                    }
                    int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                    player.displayClientMessage(Component.translatable("message.mushokucraft.infusing_mana",
                            String.format("%.0f", currentMana),
                            String.format("%.0f", req),
                            pct), true);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void unlinkRemoteCircle(Level level, BlockPos myCenterPos, @Nullable MagicCircleBlockEntity circleBE) {
        if (level.isClientSide() || circleBE == null) return;
        BlockPos linked = circleBE.getLinkedPos();
        if (linked == null) return;

        ServerLevel targetLevel = (level instanceof ServerLevel sl) ? sl : null;
        if (targetLevel != null && circleBE.getLinkedDim() != null && !circleBE.getLinkedDim().equals(level.dimension().location())) {
            targetLevel = targetLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, circleBE.getLinkedDim()));
        }

        if (targetLevel != null) {
            if (!targetLevel.hasChunk(linked.getX() >> 4, linked.getZ() >> 4)) {
                targetLevel.getChunk(linked.getX() >> 4, linked.getZ() >> 4);
            }
            if (targetLevel.getBlockEntity(linked) instanceof MagicCircleBlockEntity otherBE) {
                if (myCenterPos.equals(otherBE.getLinkedPos()) || otherBE.getLinkedPos() == null) {
                    otherBE.setLinked(null, null);
                    otherBE.setCurrentMana(0.0f);
                    otherBE.setChanged();
                    BlockState otherState = targetLevel.getBlockState(linked);
                    targetLevel.sendBlockUpdated(linked, otherState, otherState, 3);
                    targetLevel.playSound(null, linked, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 1.2f);
                    targetLevel.sendParticles(ParticleTypes.SMOKE, linked.getX() + 0.5, linked.getY() + 0.1, linked.getZ() + 0.5, 12, 0.3, 0.1, 0.3, 0.05);
                }
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
        BlockState centerState = level.getBlockState(centerPos);

        if (!level.isClientSide()) {
            if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
                // Deactivate barrier if active
                if (circleBE.isBarrierActive()) {
                    circleBE.deactivateBarrier(level, false);
                }

                // 1. Immediately unlink the remote partner circle in the process!
                unlinkRemoteCircle(level, centerPos, circleBE);

                // 2. Drop item in survival
                if (!player.isCreative()) {
                    boolean isSheared = state.hasProperty(SHEARED) && state.getValue(SHEARED);
                    if (centerState.is(this) && centerState.hasProperty(SHEARED) && centerState.getValue(SHEARED)) {
                        isSheared = true;
                    }
                    if (circleBE.isSheared()) {
                        isSheared = true;
                    }

                    // If not sheared, drop manuscript WITH LINK RESET!
                    if (!isSheared) {
                        ItemStack drop = InscribedManuscriptItem.create(circleBE.getPattern(), circleBE.getCircleTypeId());
                        int size = centerState.is(this) && centerState.hasProperty(SIZE) ? centerState.getValue(SIZE) : circleBE.getSize();
                        InscribedManuscriptItem.setSize(drop, size);
                        // Reset link on dropped item
                        InscribedManuscriptItem.setLinkedPos(drop, null, null);

                        if (circleBE.hasCapturedMob()) {
                            InscribedManuscriptItem.setCapturedMob(drop,
                                    circleBE.getCapturedEntityId(),
                                    circleBE.getCapturedEntityTag(),
                                    circleBE.getCapturedEntityName(),
                                    circleBE.getCapturedEntityMaxHp());
                        }

                        ItemEntity itemEntity = new ItemEntity(level, centerPos.getX() + 0.5, centerPos.getY() + 0.1, centerPos.getZ() + 0.5, drop);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    } else if (circleBE.hasCapturedMob()) {
                        ItemStack drop = InscribedManuscriptItem.create(circleBE.getPattern(), circleBE.getCircleTypeId());
                        InscribedManuscriptItem.setCapturedMob(drop,
                                circleBE.getCapturedEntityId(),
                                circleBE.getCapturedEntityTag(),
                                circleBE.getCapturedEntityName(),
                                circleBE.getCapturedEntityMaxHp());
                        ItemEntity itemEntity = new ItemEntity(level, centerPos.getX() + 0.5, centerPos.getY() + 0.1, centerPos.getZ() + 0.5, drop);
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }

                // 3. Clear circle link
                circleBE.setLinked(null, null);
                circleBE.setChanged();
            }

            // 4. Clean up all blocks of the 3x3 circle
            int size = centerState.is(this) && centerState.hasProperty(SIZE) ? centerState.getValue(SIZE) : 1;
            if (size == 3) {
                for (CirclePart p : CirclePart.values()) {
                    BlockPos pPos = centerPos.offset(p.getDx(), 0, p.getDz());
                    if (!pPos.equals(pos) && level.getBlockState(pPos).is(this)) {
                        level.removeBlock(pPos, false);
                    }
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
            if (part == CirclePart.CENTER) {
                if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MagicCircleBlockEntity circleBE) {
                    if (circleBE.isBarrierActive()) {
                        circleBE.deactivateBarrier(level, false);
                    }
                    unlinkRemoteCircle(level, pos, circleBE);
                }

                // If center removed, clean up surrounding blocks if 3x3
                int size = state.hasProperty(SIZE) ? state.getValue(SIZE) : 1;
                if (size == 3 && !level.isClientSide()) {
                    for (CirclePart p : CirclePart.values()) {
                        if (p == CirclePart.CENTER) continue;
                        BlockPos pPos = pos.offset(p.getDx(), 0, p.getDz());
                        if (level.getBlockState(pPos).is(this)) {
                            level.removeBlock(pPos, false);
                        }
                    }
                }
            } else {
                // If an outer part was removed, destroy center block too!
                BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
                if (!level.isClientSide() && level.getBlockState(centerPos).is(this)) {
                    level.destroyBlock(centerPos, false);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        if (part != CirclePart.CENTER) return;

        if (level.getBlockEntity(pos) instanceof MagicCircleBlockEntity circleBE) {
            if (circleBE.getLinkedPos() != null || circleBE.getCurrentMana() > 0) {
                if (random.nextFloat() < 0.35f) {
                    int size = state.hasProperty(SIZE) ? state.getValue(SIZE) : 1;
                    double spread = (size == 3) ? 1.4 : 0.4;
                    double dx = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * spread * 2;
                    double dy = pos.getY() + 0.08;
                    double dz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * spread * 2;
                    level.addParticle(ParticleTypes.ENCHANT, dx, dy, dz, 0, 0.02, 0);
                    if (random.nextFloat() < 0.2f) {
                        level.addParticle(ParticleTypes.PORTAL, dx, dy, dz, 0, 0.01, 0);
                    }
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        if (part == CirclePart.CENTER) {
            return RenderShape.ENTITYBLOCK_ANIMATED;
        }
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        if (part != CirclePart.CENTER) {
            return null;
        }
        MagicCircleBlockEntity be = new MagicCircleBlockEntity(pos, state);
        if (state.hasProperty(SIZE)) {
            be.setSize(state.getValue(SIZE));
        }
        if (state.hasProperty(SHEARED)) {
            be.setSheared(state.getValue(SHEARED));
        }
        return be;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) return null;
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        if (part != CirclePart.CENTER) return null;
        return createTickerHelper(blockEntityType, ModBlockEntities.MAGIC_CIRCLE_BE.get(), MagicCircleBlockEntity::tick);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
        if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
            ItemStack stack = InscribedManuscriptItem.create(circleBE.getPattern(), circleBE.getCircleTypeId());
            InscribedManuscriptItem.setSize(stack, circleBE.getSize());
            if (circleBE.hasCapturedMob()) {
                InscribedManuscriptItem.setCapturedMob(stack,
                        circleBE.getCapturedEntityId(),
                        circleBE.getCapturedEntityTag(),
                        circleBE.getCapturedEntityName(),
                        circleBE.getCapturedEntityMaxHp());
            }
            return stack;
        }
        return super.getCloneItemStack(level, pos, state);
    }
}
