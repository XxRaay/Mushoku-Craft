package com.mushokucraft.block;

import com.mojang.serialization.MapCodec;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.init.ModBlockEntities;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.item.InscribedManuscriptItem;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import net.minecraft.resources.ResourceLocation;
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
        CirclePart part = state.hasProperty(PART) ? state.getValue(PART) : CirclePart.CENTER;
        BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());
        BlockState centerState = level.getBlockState(centerPos);
        if (!centerState.is(this)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 1. Cutting off the paper underlay with shears
        if (stack.is(Items.SHEARS)) {
            if (!centerState.getValue(SHEARED)) {
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

        // 2. Expanding 1x1 base circle to 3x3 OR expanding floating layer from 1x1 to 3x3 using Blank Canvas or 8 Paper
        if (stack.is(ModItems.BLANK_CANVAS.get()) || stack.is(Items.PAPER)) {
            if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
                // Check insufficient paper count
                if (stack.is(Items.PAPER) && stack.getCount() < 8) {
                    if (circleBE.getSize() == 1 || (circleBE.hasAdditionalLayers() && circleBE.getTopLayer() != null && circleBE.getTopLayer().getSize() < 3)) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.layer_expand_requires_material"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // Case A: Expand 1x1 base circle on the ground to 3x3
                if (circleBE.getSize() == 1) {
                    boolean flatAndClear = true;
                    for (int dx = -1; dx <= 1 && flatAndClear; dx++) {
                        for (int dz = -1; dz <= 1 && flatAndClear; dz++) {
                            if (dx == 0 && dz == 0) continue;
                            BlockPos pPos = centerPos.offset(dx, 0, dz);
                            BlockPos bPos = pPos.below();
                            if (!level.getBlockState(pPos).canBeReplaced() || !level.getBlockState(bPos).isFaceSturdy(level, bPos, Direction.UP)) {
                                flatAndClear = false;
                            }
                        }
                    }

                    if (!flatAndClear) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.requires_3x3_flat"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }

                    if (!level.isClientSide()) {
                        if (player == null || !player.isCreative()) {
                            if (stack.is(ModItems.BLANK_CANVAS.get())) {
                                stack.shrink(1);
                            } else {
                                stack.shrink(8);
                            }
                        }

                        boolean sheared = centerState.getValue(SHEARED);
                        BlockState newCenterState = centerState.setValue(SIZE, 3);
                        level.setBlock(centerPos, newCenterState, 3);
                        circleBE.setSize(3);
                        circleBE.setChanged();

                        for (CirclePart p : CirclePart.values()) {
                            if (p == CirclePart.CENTER) continue;
                            BlockPos pPos = centerPos.offset(p.getDx(), 0, p.getDz());
                            BlockState pState = defaultBlockState()
                                    .setValue(SIZE, 3)
                                    .setValue(PART, p)
                                    .setValue(SHEARED, sheared);
                            level.setBlock(pPos, pState, 3);
                        }

                        level.sendBlockUpdated(centerPos, newCenterState, newCenterState, 3);
                        level.playSound(null, centerPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.9f);
                        level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.4f);
                        if (level instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.ENCHANT, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 35, 1.2, 0.3, 1.2, 0.1);
                            sl.sendParticles(ParticleTypes.GLOW, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 15, 0.8, 0.2, 0.8, 0.05);
                        }
                        if (player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_expanded_to_3x3"), true);
                        }
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                // Case B: Expand top floating layer from 1x1 to 3x3 on an existing 3x3 circle
                if (circleBE.getSize() == 3 && circleBE.hasAdditionalLayers()) {
                    MagicCircleBlockEntity.CircleLayer top = circleBE.getTopLayer();
                    if (top != null && top.getSize() < 3) {
                        if (!level.isClientSide()) {
                            if (player == null || !player.isCreative()) {
                                if (stack.is(ModItems.BLANK_CANVAS.get())) {
                                    stack.shrink(1);
                                } else {
                                    stack.shrink(8);
                                }
                            }
                            top.setSize(3);
                            circleBE.setChanged();
                            level.sendBlockUpdated(centerPos, centerState, centerState, 3);

                            level.playSound(null, centerPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.9f);
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.4f);
                            if (level instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.ENCHANT, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 35, 1.2, 0.3, 1.2, 0.1);
                                sl.sendParticles(ParticleTypes.GLOW, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 15, 0.8, 0.2, 0.8, 0.05);
                            }
                            if (circleBE.isDimensionalGate()) {
                                level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.6f);
                                level.playSound(null, centerPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.9f, 1.3f);
                                if (level instanceof ServerLevel sl) {
                                    sl.sendParticles(ParticleTypes.FLASH, centerPos.getX() + 0.5, centerPos.getY() + 0.8, centerPos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
                                    sl.sendParticles(ParticleTypes.REVERSE_PORTAL, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 80, 1.4, 0.4, 1.4, 0.2);
                                }
                                if (player != null) {
                                    player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_stabilized"), true);
                                }
                            } else if (circleBE.isSoulAnchor()) {
                                level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 1.6f);
                                level.playSound(null, centerPos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.2f);
                                if (level instanceof ServerLevel sl) {
                                    sl.sendParticles(ParticleTypes.FLASH, centerPos.getX() + 0.5, centerPos.getY() + 0.8, centerPos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
                                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 60, 1.2, 0.4, 1.2, 0.15);
                                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, centerPos.getX() + 0.5, centerPos.getY() + 0.3, centerPos.getZ() + 0.5, 30, 0.8, 0.2, 0.8, 0.05);
                                }
                                if (player != null) {
                                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_stabilized"), true);
                                }
                            } else if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.layer_expanded"), true);
                            }
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // If circle is already 3x3 and has no layers to expand:
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_already_max_size"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        // 3. Inscribed Manuscript: Teleport Linking, Mob Souls, and Multi-layer Attachment
        if (stack.is(ModItems.INSCRIBED_MANUSCRIPT.get())) {
            if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE) {
                ResourceLocation targetType = circleBE.getCircleTypeId();
                if (targetType == null && circleBE.getPattern() != null && level != null) {
                    MagicCircleType identified = MagicCircleRegistry.identify(level, circleBE.getPattern());
                    if (identified != null) {
                        targetType = identified.getId();
                        circleBE.setCircleTypeId(targetType);
                    }
                }
                ResourceLocation myType = InscribedManuscriptItem.getOrIdentifyCircleTypeId(stack, level);

                // A: Place captured mob into Summoning Circle
                if (circleBE.hasCircleType(com.mushokucraft.magic.circle.SummoningCircleType.ID)) {
                    if (InscribedManuscriptItem.hasCapturedMob(stack)) {
                        if (circleBE.hasCapturedMob()) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.summon_already_has_mob"), true);
                            }
                            return ItemInteractionResult.sidedSuccess(level.isClientSide());
                        }
                        if (!level.isClientSide()) {
                            circleBE.setCapturedMob(
                                    InscribedManuscriptItem.getCapturedEntityId(stack),
                                    InscribedManuscriptItem.getCapturedEntityTag(stack),
                                    InscribedManuscriptItem.getCapturedEntityName(stack),
                                    InscribedManuscriptItem.getCapturedEntityMaxHp(stack)
                            );
                            InscribedManuscriptItem.clearCapturedMob(stack);
                            circleBE.setCurrentMana(0.0f);
                            circleBE.setRequiredMana(circleBE.calculateTotalRequiredMana(level));
                            circleBE.setChanged();
                            level.sendBlockUpdated(centerPos, centerState, centerState, 3);
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.4f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.entity_placed_in_summon", circleBE.getCapturedEntityName()), true);
                            }
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // B: Extract captured mob from Capture Circle into empty manuscript
                if (circleBE.hasCircleType(com.mushokucraft.magic.circle.CaptureCircleType.ID)) {
                    if (circleBE.hasCapturedMob() && !InscribedManuscriptItem.hasCapturedMob(stack)) {
                        if (!level.isClientSide()) {
                            InscribedManuscriptItem.setCapturedMob(stack, circleBE.getCapturedEntityId(), circleBE.getCapturedEntityTag(), circleBE.getCapturedEntityName(), circleBE.getCapturedEntityMaxHp());
                            circleBE.clearCapturedMob();
                            circleBE.setRequiredMana(circleBE.calculateTotalRequiredMana(level));
                            circleBE.setChanged();
                            level.sendBlockUpdated(centerPos, centerState, centerState, 3);
                            level.playSound(null, centerPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.entity_extracted", InscribedManuscriptItem.getCapturedEntityName(stack)), true);
                            }
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // C: Teleport Linking
                // Applies when both the circle on the ground and the manuscript are teleport circles,
                // and either the circle is 1x1, or player is not sneaking (shift on 3x3 is for adding as layer).
                boolean isTargetTeleport = InscribedManuscriptItem.isTeleportType(targetType);
                boolean isMyTeleport = InscribedManuscriptItem.isTeleportType(myType);

                if (isTargetTeleport && isMyTeleport && (circleBE.getSize() < 3 || (player != null && !player.isShiftKeyDown()))) {
                    if (!level.isClientSide()) {
                        BlockPos linkedPos = InscribedManuscriptItem.getLinkedPos(stack);
                        ResourceLocation linkedDim = InscribedManuscriptItem.getLinkedDim(stack);

                        if (linkedPos != null && !linkedPos.equals(centerPos)) {
                            // Linking circle on ground with previously recorded circle
                            circleBE.setLinked(linkedPos, linkedDim);
                            circleBE.setChanged();
                            level.sendBlockUpdated(centerPos, centerState, centerState, 3);

                            if (linkedDim == null || linkedDim.equals(level.dimension().location())) {
                                if (level.getBlockEntity(linkedPos) instanceof MagicCircleBlockEntity otherBE) {
                                    otherBE.setLinked(centerPos, level.dimension().location());
                                    otherBE.setChanged();
                                    level.sendBlockUpdated(linkedPos, otherBE.getBlockState(), otherBE.getBlockState(), 3);
                                }
                            }

                            InscribedManuscriptItem.setLinkedPos(stack, null, null);
                            level.playSound(null, centerPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.4f);
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
                            if (level instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.PORTAL, centerPos.getX() + 0.5, centerPos.getY() + 0.2, centerPos.getZ() + 0.5, 40, 0.8, 0.1, 0.8, 0.5);
                                sl.sendParticles(ParticleTypes.FLASH, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 1, 0, 0, 0, 0);
                            }
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.circles_linked_success"), true);
                            }
                        } else if (linkedPos != null && linkedPos.equals(centerPos)) {
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_already_linked_to_this"), true);
                            }
                        } else {
                            // Record centerPos to manuscript
                            InscribedManuscriptItem.setLinkedPos(stack, centerPos, level.dimension().location());
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.3f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_linked_to_item", centerPos.getX(), centerPos.getY(), centerPos.getZ()), true);
                            }
                        }
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }

                // D: Multi-layer Attachment
                if (circleBE.getSize() == 3) {
                    boolean isBaseGate = com.mushokucraft.magic.circle.DimensionalGateCircleType.ID.equals(targetType);
                    boolean isLayerGate = com.mushokucraft.magic.circle.DimensionalGateCircleType.ID.equals(myType);

                    if (isBaseGate && !isLayerGate) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_only_self"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                    if (!isBaseGate && isLayerGate) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_cannot_be_layer"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }

                    boolean isBaseAnchor = com.mushokucraft.magic.circle.SoulAnchorCircleType.ID.equals(targetType);
                    boolean isLayerAnchor = com.mushokucraft.magic.circle.SoulAnchorCircleType.ID.equals(myType);
                    boolean isLayerSealing = com.mushokucraft.magic.circle.CaptureCircleType.ID.equals(myType);

                    if (isBaseAnchor) {
                        if (!isLayerAnchor && !isLayerSealing) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_only_anchor_and_sealing"), true);
                            }
                            return ItemInteractionResult.sidedSuccess(level.isClientSide());
                        }
                        if (circleBE.hasLayerType(myType)) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_layer_already_present"), true);
                            }
                            return ItemInteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                    if (!isBaseAnchor && isLayerAnchor) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_cannot_be_layer"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }

                    if (circleBE.isBarrierActive()) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_cannot_modify_active"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }

                    if (circleBE.canAddLayer(myType)) {
                        if (!level.isClientSide()) {
                            if (InscribedManuscriptItem.hasCapturedMob(stack) && !circleBE.hasCapturedMob()) {
                                circleBE.setCapturedMob(
                                        InscribedManuscriptItem.getCapturedEntityId(stack),
                                        InscribedManuscriptItem.getCapturedEntityTag(stack),
                                        InscribedManuscriptItem.getCapturedEntityName(stack),
                                        InscribedManuscriptItem.getCapturedEntityMaxHp(stack)
                                );
                            }
                            circleBE.addLayer(myType, InscribedManuscriptItem.getPattern(stack), InscribedManuscriptItem.getSize(stack));
                            if (player == null || !player.isCreative()) {
                                stack.shrink(1);
                            }
                            level.playSound(null, centerPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.4f);
                            level.playSound(null, centerPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
                            if (level instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.ENCHANT, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 30, 1.0, 0.3, 1.0, 0.1);
                                sl.sendParticles(ParticleTypes.GLOW, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 15, 0.6, 0.2, 0.6, 0.05);
                            }
                            if (player != null) {
                                MagicCircleType layerTypeObj = MagicCircleRegistry.get(myType);
                                Component typeName = layerTypeObj != null ? layerTypeObj.getDisplayName() : Component.literal(myType != null ? myType.toString() : "Неизвестный");
                                int layerNum = 1 + circleBE.getAdditionalLayers().size();
                                String sizeStr = InscribedManuscriptItem.getSize(stack) == 3 ? "3x3" : "1x1";
                                player.displayClientMessage(Component.translatable("message.mushokucraft.layer_added", layerNum, typeName, sizeStr), true);
                            }
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    } else {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.layer_max_reached"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                    }
                } else {
                    // Circle is 1x1, multi-layers are not permitted
                    if (!level.isClientSide() && player != null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.multilayer_requires_3x3"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide());
                }
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
                // Sneak-right click with empty hand: pop off top layer!
                if (player.isShiftKeyDown()) {
                    if (circleBE.isBarrierActive()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_cannot_modify_active"), true);
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                    if (circleBE.hasAdditionalLayers()) {
                        MagicCircleBlockEntity.CircleLayer popped = circleBE.popTopLayer();
                        if (popped != null) {
                            ItemStack drop = InscribedManuscriptItem.create(popped.getPattern(), popped.getCircleTypeId());
                            InscribedManuscriptItem.setSize(drop, popped.getSize());
                            if (com.mushokucraft.magic.circle.SummoningCircleType.ID.equals(popped.getCircleTypeId()) && circleBE.hasCapturedMob()) {
                                InscribedManuscriptItem.setCapturedMob(drop, circleBE.getCapturedEntityId(), circleBE.getCapturedEntityTag(), circleBE.getCapturedEntityName(), circleBE.getCapturedEntityMaxHp());
                                circleBE.clearCapturedMob();
                            }

                            if (!player.getInventory().add(drop)) {
                                ItemEntity itemEntity = new ItemEntity(level, centerPos.getX() + 0.5, centerPos.getY() + 0.2, centerPos.getZ() + 0.5, drop);
                                itemEntity.setDefaultPickUpDelay();
                                level.addFreshEntity(itemEntity);
                            }

                            level.playSound(null, centerPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 0.9f);
                            level.playSound(null, centerPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.1f);

                            MagicCircleType typeObj = MagicCircleRegistry.get(popped.getCircleTypeId());
                            Component typeName = typeObj != null ? typeObj.getDisplayName() : Component.literal(popped.getCircleTypeId() != null ? popped.getCircleTypeId().toString() : "Неизвестный");
                            String sizeStr = popped.getSize() == 3 ? "3x3" : "1x1";
                            player.displayClientMessage(Component.translatable("message.mushokucraft.layer_removed", typeName, sizeStr), true);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                BlockPos linked = circleBE.getLinkedPos();
                float currentMana = circleBE.getCurrentMana();
                float required = circleBE.getRequiredMana();
                MagicCircleType type = circleBE.getCircleType();

                if (type == null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_inactive_unmatched"), true);
                } else if (circleBE.isBarrierActive()) {
                    if (circleBE.hasAdditionalLayers()) {
                        int count = circleBE.getAdditionalLayers().size();
                        player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status_multilayer",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", circleBE.getBarrierHealth()),
                                String.format("%.0f", circleBE.getBarrierMaxHealth()),
                                count), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", circleBE.getBarrierHealth()),
                                String.format("%.0f", circleBE.getBarrierMaxHealth())), true);
                    }
                } else if (circleBE.isDimensionalGate()) {
                    if (linked == null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_unlinked"), true);
                    } else if (!circleBE.isDestinationValid(level)) {
                        circleBE.setLinked(null, null);
                        circleBE.setCurrentMana(0.0f);
                        circleBE.setChanged();
                        level.sendBlockUpdated(centerPos, level.getBlockState(centerPos), level.getBlockState(centerPos), 3);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
                    } else {
                        float req = circleBE.calculateTotalRequiredMana(level);
                        int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                        String dimName = circleBE.getLinkedDim() != null ? circleBE.getLinkedDim().toString() : "unknown";
                        String destStr = String.format("[%d, %d, %d] (%s)", linked.getX(), linked.getY(), linked.getZ(), dimName);
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_status",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct,
                                destStr), true);
                    }
                } else if (circleBE.hasDimensionalGateType() && !circleBE.isDimensionalGate()) {
                    if (circleBE.getSize() < 3) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_3x3"), true);
                    } else if (circleBE.getAdditionalLayers().isEmpty()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers"), true);
                    } else if (!circleBE.allLayersExpanded()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers_3x3"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_only_self"), true);
                    }
                } else if (circleBE.isSoulAnchor()) {
                    float req = circleBE.calculateTotalRequiredMana(level);
                    int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                    if (circleBE.getBoundPlayerUUID() != null) {
                        String name = circleBE.getBoundPlayerName() != null ? circleBE.getBoundPlayerName() : "Неизвестный";
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_status_bound",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct,
                                name), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_status_unbound",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct), true);
                    }
                } else if (circleBE.hasSoulAnchorType() && !circleBE.isSoulAnchor()) {
                    if (circleBE.getSize() < 3) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_3x3"), true);
                    } else if (circleBE.getAdditionalLayers().size() < 2) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_three_layers"), true);
                    } else if (!circleBE.allLayersExpanded()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_layers_3x3"), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_sealing_layer"), true);
                    }
                } else if (circleBE.hasAdditionalLayers()) {
                    float req = circleBE.calculateTotalRequiredMana(level);
                    int pct = (int) ((currentMana / Math.max(1.0f, req)) * 100);
                    int totalLayers = 1 + circleBE.getAdditionalLayers().size();
                    if (circleBE.isBaseBarrier()) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_waiting_layers",
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct,
                                circleBE.getAdditionalLayers().size()), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.multilayer_circle_status",
                                totalLayers,
                                String.format("%.0f", currentMana),
                                String.format("%.0f", req),
                                pct), true);
                    }
                } else if (InscribedManuscriptItem.isTeleportType(circleBE.getCircleTypeId()) && linked == null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_not_linked"), true);
                } else if (InscribedManuscriptItem.isTeleportType(circleBE.getCircleTypeId()) && !circleBE.isDestinationValid(level)) {
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

                    // Drop all additional layers as manuscripts
                    for (MagicCircleBlockEntity.CircleLayer layer : circleBE.getAdditionalLayers()) {
                        ItemStack layerDrop = InscribedManuscriptItem.create(layer.getPattern(), layer.getCircleTypeId());
                        InscribedManuscriptItem.setSize(layerDrop, layer.getSize());
                        ItemEntity lEntity = new ItemEntity(level, centerPos.getX() + 0.5, centerPos.getY() + 0.1, centerPos.getZ() + 0.5, layerDrop);
                        lEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(lEntity);
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
            boolean isGateActive = circleBE.isDimensionalGate();
            boolean isGateCandidate = circleBE.hasDimensionalGateType();
            boolean isAnchorActive = circleBE.isSoulAnchor();
            boolean isAnchorCandidate = circleBE.hasSoulAnchorType();
            boolean hasLayers = circleBE.hasAdditionalLayers();

            if (circleBE.getLinkedPos() != null || circleBE.getCurrentMana() > 0 || isGateCandidate || isAnchorCandidate || hasLayers) {
                // If Soul Anchor is active (multi-layered and stable):
                if (isAnchorActive) {
                    if (random.nextFloat() < 0.65f) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double dist = 0.3 + random.nextDouble() * 1.2;
                        double px = pos.getX() + 0.5 + Math.cos(angle) * dist;
                        double pz = pos.getZ() + 0.5 + Math.sin(angle) * dist;
                        double py = pos.getY() + 0.05;
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0, 0.02, 0);
                        if (random.nextFloat() < 0.35f) {
                            level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, px, py + 0.15, pz, 0, 0.02, 0);
                        }
                    }
                    if (circleBE.isBound() && random.nextFloat() < 0.25f) {
                        level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.8 + random.nextDouble() * 0.8, pos.getZ() + 0.5, (random.nextDouble() - 0.5) * 0.3, 0.05, (random.nextDouble() - 0.5) * 0.3);
                    }
                    if (random.nextFloat() < 0.03f) {
                        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 0.35f, 1.2f, false);
                    }
                } else if (isAnchorCandidate) {
                    // Dormant soul anchor base - faint soul wisps
                    if (random.nextFloat() < 0.25f) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double dist = 0.3 + random.nextDouble() * 0.8;
                        double px = pos.getX() + 0.5 + Math.cos(angle) * dist;
                        double pz = pos.getZ() + 0.5 + Math.sin(angle) * dist;
                        level.addParticle(ParticleTypes.SOUL, px, pos.getY() + 0.1, pz, 0, 0.015, 0);
                    }
                } else if (isGateActive) {
                    if (random.nextFloat() < 0.8f) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double dist = 0.4 + random.nextDouble() * 1.1;
                        double px = pos.getX() + 0.5 + Math.cos(angle) * dist;
                        double pz = pos.getZ() + 0.5 + Math.sin(angle) * dist;
                        double py = pos.getY() + 0.05;
                        level.addParticle(ParticleTypes.REVERSE_PORTAL, px, py, pz, (pos.getX() + 0.5 - px) * 0.08, 0.04, (pos.getZ() + 0.5 - pz) * 0.08);
                        if (random.nextFloat() < 0.4f) {
                            level.addParticle(ParticleTypes.END_ROD, px, py + random.nextDouble() * 1.2, pz, 0, 0.03, 0);
                        }
                        if (random.nextFloat() < 0.3f) {
                            level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.3 + random.nextDouble() * 1.5, pos.getZ() + 0.5, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.2);
                        }
                    }
                    if (random.nextFloat() < 0.04f) {
                        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35f, 1.3f, false);
                    }
                } else if (isGateCandidate) {
                    // Dormant gate base - faint purple smoke and glyph motes
                    if (random.nextFloat() < 0.25f) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double dist = 0.3 + random.nextDouble() * 0.8;
                        double px = pos.getX() + 0.5 + Math.cos(angle) * dist;
                        double pz = pos.getZ() + 0.5 + Math.sin(angle) * dist;
                        level.addParticle(ParticleTypes.SMOKE, px, pos.getY() + 0.1, pz, 0, 0.01, 0);
                        level.addParticle(ParticleTypes.PORTAL, px, pos.getY() + 0.1, pz, 0, 0.02, 0);
                    }
                }

                // If hovering layers: mana sparkle drifting around the floating layers
                if (hasLayers && random.nextFloat() < 0.45f) {
                    double spread = 0.8;
                    double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * spread * 2;
                    double py = pos.getY() + 0.25 + random.nextDouble() * 0.45;
                    double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * spread * 2;
                    level.addParticle(ParticleTypes.ENCHANT, px, py, pz, 0, 0.015, 0);
                }

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
