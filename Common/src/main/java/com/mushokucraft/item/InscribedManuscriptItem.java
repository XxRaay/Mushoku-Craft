package com.mushokucraft.item;

import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.magic.circle.MagicCirclePatterns;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import com.mushokucraft.magic.circle.TeleportCircleType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InscribedManuscriptItem extends Item {

    public InscribedManuscriptItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(MagicCirclePattern pattern, ResourceLocation circleTypeId) {
        return create(pattern, circleTypeId, 1);
    }

    public static ItemStack create(MagicCirclePattern pattern, ResourceLocation circleTypeId, int size) {
        ItemStack stack = new ItemStack(ModItems.INSCRIBED_MANUSCRIPT.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (pattern != null) {
                tag.put("Pattern", pattern.save());
            }
            if (circleTypeId != null) {
                tag.putString("CircleType", circleTypeId.toString());
            }
            if (size > 1) {
                tag.putInt("Size", size);
            }
        });
        return stack;
    }

    public static MagicCirclePattern getPattern(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("Pattern")) {
            return MagicCirclePattern.load(tag.getCompound("Pattern"));
        }
        return new MagicCirclePattern();
    }

    public static ResourceLocation getCircleTypeId(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("CircleType")) {
            return ResourceLocation.tryParse(tag.getString("CircleType"));
        }
        return null;
    }

    public static ResourceLocation getOrIdentifyCircleTypeId(ItemStack stack, Level level) {
        ResourceLocation id = getCircleTypeId(stack);
        if (id == null && level != null) {
            MagicCirclePattern pattern = getPattern(stack);
            if (pattern != null && pattern.countFilled() > 0) {
                MagicCircleType type = MagicCircleRegistry.identify(level, pattern);
                if (type != null) {
                    id = type.getId();
                    ResourceLocation finalId = id;
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, cTag -> {
                        cTag.putString("CircleType", finalId.toString());
                    });
                }
            }
        }
        return id;
    }

    public static boolean isTeleportType(ResourceLocation id) {
        if (id == null) return false;
        return TeleportCircleType.ID.equals(id) ||
                (id.getNamespace().equals("mushokucraft") && ("teleportation".equals(id.getPath()) || "teleport".equals(id.getPath())));
    }

    public static BlockPos getLinkedPos(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("LinkedX") && tag.contains("LinkedY") && tag.contains("LinkedZ")) {
            return new BlockPos(tag.getInt("LinkedX"), tag.getInt("LinkedY"), tag.getInt("LinkedZ"));
        }
        return null;
    }

    public static ResourceLocation getLinkedDim(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("LinkedDim")) {
            return ResourceLocation.tryParse(tag.getString("LinkedDim"));
        }
        return null;
    }

    public static void setLinkedPos(ItemStack stack, BlockPos pos, ResourceLocation dim) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (pos != null) {
                tag.putInt("LinkedX", pos.getX());
                tag.putInt("LinkedY", pos.getY());
                tag.putInt("LinkedZ", pos.getZ());
            } else {
                tag.remove("LinkedX");
                tag.remove("LinkedY");
                tag.remove("LinkedZ");
            }
            if (dim != null) {
                tag.putString("LinkedDim", dim.toString());
            } else {
                tag.remove("LinkedDim");
            }
        });
    }

    public static int getSize(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("Size")) {
            return Math.max(1, tag.getInt("Size"));
        }
        return 1;
    }

    public static void setSize(ItemStack stack, int size) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("Size", size);
        });
    }

    public static boolean hasCapturedMob(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("CapturedEntityId");
    }

    public static String getCapturedEntityId(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("CapturedEntityId") ? tag.getString("CapturedEntityId") : null;
    }

    public static CompoundTag getCapturedEntityTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("CapturedEntityTag") ? tag.getCompound("CapturedEntityTag") : null;
    }

    public static String getCapturedEntityName(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("CapturedEntityName") ? tag.getString("CapturedEntityName") : null;
    }

    public static float getCapturedEntityMaxHp(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.contains("CapturedEntityMaxHp") ? tag.getFloat("CapturedEntityMaxHp") : 0.0f;
    }

    public static void setCapturedMob(ItemStack stack, String id, CompoundTag tag, String name, float maxHp) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, cTag -> {
            if (id != null) cTag.putString("CapturedEntityId", id);
            if (tag != null) cTag.put("CapturedEntityTag", tag);
            if (name != null) cTag.putString("CapturedEntityName", name);
            cTag.putFloat("CapturedEntityMaxHp", maxHp);
        });
    }

    public static void clearCapturedMob(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, cTag -> {
            cTag.remove("CapturedEntityId");
            cTag.remove("CapturedEntityTag");
            cTag.remove("CapturedEntityName");
            cTag.remove("CapturedEntityMaxHp");
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 1. Linking / Soul Transfer: Check if clicked block is an existing MagicCircleBlock (center or surrounding part)
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.is(ModBlocks.MAGIC_CIRCLE.get())) {
            com.mushokucraft.block.MagicCircleBlock.CirclePart part = clickedState.hasProperty(com.mushokucraft.block.MagicCircleBlock.PART) ?
                    clickedState.getValue(com.mushokucraft.block.MagicCircleBlock.PART) : com.mushokucraft.block.MagicCircleBlock.CirclePart.CENTER;
            BlockPos centerPos = clickedPos.offset(-part.getDx(), 0, -part.getDz());

            if (level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity targetBE) {
                ResourceLocation targetType = targetBE.getCircleTypeId();

                // A: Place captured mob into Summoning Circle
                if (targetBE.hasCircleType(com.mushokucraft.magic.circle.SummoningCircleType.ID)) {
                    if (hasCapturedMob(stack)) {
                        if (targetBE.hasCapturedMob()) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.summon_already_has_mob"), true);
                            }
                            return InteractionResult.FAIL;
                        }
                        if (!level.isClientSide()) {
                            targetBE.setCapturedMob(getCapturedEntityId(stack), getCapturedEntityTag(stack), getCapturedEntityName(stack), getCapturedEntityMaxHp(stack));
                            clearCapturedMob(stack);
                            targetBE.setCurrentMana(0.0f);
                            targetBE.setRequiredMana(targetBE.calculateTotalRequiredMana(level));
                            targetBE.setChanged();
                            level.sendBlockUpdated(centerPos, clickedState, clickedState, 3);
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.4f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.entity_placed_in_summon", targetBE.getCapturedEntityName()), true);
                            }
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // B: Extract captured mob from Capture Circle into empty manuscript
                if (targetBE.hasCircleType(com.mushokucraft.magic.circle.CaptureCircleType.ID)) {
                    if (targetBE.hasCapturedMob() && !hasCapturedMob(stack)) {
                        if (!level.isClientSide()) {
                            setCapturedMob(stack, targetBE.getCapturedEntityId(), targetBE.getCapturedEntityTag(), targetBE.getCapturedEntityName(), targetBE.getCapturedEntityMaxHp());
                            targetBE.clearCapturedMob();
                            targetBE.setRequiredMana(targetBE.calculateTotalRequiredMana(level));
                            targetBE.setChanged();
                            level.sendBlockUpdated(centerPos, clickedState, clickedState, 3);
                            level.playSound(null, centerPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.2f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.entity_extracted", getCapturedEntityName(stack)), true);
                            }
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }

                // C: Multi-layer Attachment & Teleport Linking
                ResourceLocation myType = getOrIdentifyCircleTypeId(stack, level);

                boolean isTeleportLinkAttempt = isTeleportType(targetType) && isTeleportType(myType) && (targetBE.getSize() < 3 || (player != null && !player.isShiftKeyDown()));

                if (isTeleportLinkAttempt) {
                    if (!level.isClientSide()) {
                        BlockPos linkedPos = getLinkedPos(stack);
                        ResourceLocation linkedDim = getLinkedDim(stack);

                        if (linkedPos != null && !linkedPos.equals(centerPos)) {
                            targetBE.setLinked(linkedPos, linkedDim);
                            targetBE.setChanged();
                            level.sendBlockUpdated(centerPos, clickedState, clickedState, 3);

                            if (linkedDim == null || linkedDim.equals(level.dimension().location())) {
                                if (level.getBlockEntity(linkedPos) instanceof MagicCircleBlockEntity otherBE) {
                                    otherBE.setLinked(centerPos, level.dimension().location());
                                    otherBE.setChanged();
                                    level.sendBlockUpdated(linkedPos, otherBE.getBlockState(), otherBE.getBlockState(), 3);
                                }
                            }

                            setLinkedPos(stack, null, null);
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
                            setLinkedPos(stack, centerPos, level.dimension().location());
                            level.playSound(null, centerPos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.3f);
                            if (player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.circle_linked_to_item", centerPos.getX(), centerPos.getY(), centerPos.getZ()), true);
                            }
                        }
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                // Multi-layer attachment: only allowed on 3x3 circles!
                if (targetBE.getSize() == 3) {
                    boolean isBaseGate = com.mushokucraft.magic.circle.DimensionalGateCircleType.ID.equals(targetBE.getCircleTypeId());
                    boolean isLayerGate = com.mushokucraft.magic.circle.DimensionalGateCircleType.ID.equals(myType);

                    if (isBaseGate && !isLayerGate) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_only_self"), true);
                        }
                        return InteractionResult.FAIL;
                    }
                    if (!isBaseGate && isLayerGate) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_cannot_be_layer"), true);
                        }
                        return InteractionResult.FAIL;
                    }

                    boolean isBaseAnchor = com.mushokucraft.magic.circle.SoulAnchorCircleType.ID.equals(targetBE.getCircleTypeId());
                    boolean isLayerAnchor = com.mushokucraft.magic.circle.SoulAnchorCircleType.ID.equals(myType);
                    boolean isLayerSealing = com.mushokucraft.magic.circle.CaptureCircleType.ID.equals(myType);

                    if (isBaseAnchor) {
                        if (!isLayerAnchor && !isLayerSealing) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_only_anchor_and_sealing"), true);
                            }
                            return InteractionResult.FAIL;
                        }
                        if (targetBE.hasLayerType(myType)) {
                            if (!level.isClientSide() && player != null) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_layer_already_present"), true);
                            }
                            return InteractionResult.FAIL;
                        }
                    }
                    if (!isBaseAnchor && isLayerAnchor) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_cannot_be_layer"), true);
                        }
                        return InteractionResult.FAIL;
                    }

                    if (targetBE.isBarrierActive()) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_cannot_modify_active"), true);
                        }
                        return InteractionResult.FAIL;
                    }

                    if (targetBE.canAddLayer(myType)) {
                        if (!level.isClientSide()) {
                            if (hasCapturedMob(stack) && !targetBE.hasCapturedMob()) {
                                targetBE.setCapturedMob(getCapturedEntityId(stack), getCapturedEntityTag(stack), getCapturedEntityName(stack), getCapturedEntityMaxHp(stack));
                            }
                            targetBE.addLayer(myType, getPattern(stack), getSize(stack));
                            stack.shrink(1);
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
                                int layerNum = 1 + targetBE.getAdditionalLayers().size();
                                String sizeStr = getSize(stack) == 3 ? "3x3" : "1x1";
                                player.displayClientMessage(Component.translatable("message.mushokucraft.layer_added", layerNum, typeName, sizeStr), true);
                            }
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    } else {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.layer_max_reached"), true);
                        }
                        return InteractionResult.FAIL;
                    }
                } else {
                    if (!level.isClientSide() && player != null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.multilayer_requires_3x3"), true);
                    }
                    return InteractionResult.FAIL;
                }
            }
        }

        // 2. Placement: Place as MagicCircleBlock on top face
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = clickedPos.above();
        int size = getSize(stack);

        if (size == 3) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pPos = placePos.offset(dx, 0, dz);
                    BlockPos bPos = clickedPos.offset(dx, 0, dz);
                    if (!level.getBlockState(pPos).canBeReplaced()) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.requires_3x3_flat"), true);
                        }
                        return InteractionResult.FAIL;
                    }
                    if (!level.getBlockState(bPos).isFaceSturdy(level, bPos, Direction.UP)) {
                        if (!level.isClientSide() && player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.requires_3x3_flat"), true);
                        }
                        return InteractionResult.FAIL;
                    }
                }
            }
        } else {
            BlockState currentState = level.getBlockState(placePos);
            if (!currentState.canBeReplaced()) {
                return InteractionResult.PASS;
            }
            BlockState belowState = level.getBlockState(clickedPos);
            if (!belowState.isFaceSturdy(level, clickedPos, Direction.UP)) {
                return InteractionResult.PASS;
            }
        }

        if (!level.isClientSide()) {
            if (size == 3) {
                // Place center block
                BlockState centerState = ModBlocks.MAGIC_CIRCLE.get().defaultBlockState()
                        .setValue(com.mushokucraft.block.MagicCircleBlock.SIZE, 3)
                        .setValue(com.mushokucraft.block.MagicCircleBlock.PART, com.mushokucraft.block.MagicCircleBlock.CirclePart.CENTER)
                        .setValue(com.mushokucraft.block.MagicCircleBlock.SHEARED, false);
                level.setBlock(placePos, centerState, 3);

                // Place 8 surrounding blocks to form continuous 3x3 hitbox and collision
                for (com.mushokucraft.block.MagicCircleBlock.CirclePart p : com.mushokucraft.block.MagicCircleBlock.CirclePart.values()) {
                    if (p == com.mushokucraft.block.MagicCircleBlock.CirclePart.CENTER) continue;
                    BlockPos pPos = placePos.offset(p.getDx(), 0, p.getDz());
                    BlockState pState = ModBlocks.MAGIC_CIRCLE.get().defaultBlockState()
                            .setValue(com.mushokucraft.block.MagicCircleBlock.SIZE, 3)
                            .setValue(com.mushokucraft.block.MagicCircleBlock.PART, p)
                            .setValue(com.mushokucraft.block.MagicCircleBlock.SHEARED, false);
                    level.setBlock(pPos, pState, 3);
                }

                if (level.getBlockEntity(placePos) instanceof MagicCircleBlockEntity circleBE) {
                    circleBE.setSize(3);
                    circleBE.setSheared(false);
                    MagicCirclePattern pattern = getPattern(stack);
                    ResourceLocation circleType = getCircleTypeId(stack);
                    if (circleType != null && level instanceof ServerLevel sl) {
                        MagicCirclePattern worldPattern = MagicCirclePatterns.getPatternForSeed(sl.getSeed(), circleType);
                        if (worldPattern != null) {
                            pattern = worldPattern;
                        }
                    }
                    BlockPos linkedPos = getLinkedPos(stack);
                    ResourceLocation linkedDim = getLinkedDim(stack);

                    circleBE.setPattern(pattern);
                    circleBE.setCircleTypeId(circleType);
                    if (player != null) {
                        circleBE.setOwnerUUID(player.getUUID());
                    }

                    // Transfer captured mob soul into newly placed circle
                    if (hasCapturedMob(stack)) {
                        circleBE.setCapturedMob(
                                getCapturedEntityId(stack),
                                getCapturedEntityTag(stack),
                                getCapturedEntityName(stack),
                                getCapturedEntityMaxHp(stack)
                        );
                        if (circleBE.getCircleType() != null) {
                            circleBE.setRequiredMana(circleBE.getCircleType().calculateRequiredMana(level, placePos, null));
                        }
                    }

                    if (linkedPos != null && isTeleportType(circleType)) {
                        circleBE.setLinked(linkedPos, linkedDim);

                        if (linkedDim == null || linkedDim.equals(level.dimension().location())) {
                            if (level.getBlockEntity(linkedPos) instanceof MagicCircleBlockEntity otherBE) {
                                otherBE.setLinked(placePos, level.dimension().location());
                                otherBE.setChanged();
                                level.sendBlockUpdated(linkedPos, otherBE.getBlockState(), otherBE.getBlockState(), 3);
                            }
                        }
                    }
                    circleBE.setChanged();
                    level.sendBlockUpdated(placePos, centerState, centerState, 3);
                }
            } else {
                // 1x1 circle
                BlockState stateToPlace = ModBlocks.MAGIC_CIRCLE.get().defaultBlockState()
                        .setValue(com.mushokucraft.block.MagicCircleBlock.SIZE, 1)
                        .setValue(com.mushokucraft.block.MagicCircleBlock.PART, com.mushokucraft.block.MagicCircleBlock.CirclePart.CENTER)
                        .setValue(com.mushokucraft.block.MagicCircleBlock.SHEARED, false);
                level.setBlock(placePos, stateToPlace, 3);
                if (level.getBlockEntity(placePos) instanceof MagicCircleBlockEntity circleBE) {
                    circleBE.setSize(1);
                    circleBE.setSheared(false);
                    MagicCirclePattern pattern = getPattern(stack);
                    ResourceLocation circleType = getCircleTypeId(stack);
                    if (circleType != null && level instanceof ServerLevel sl) {
                        MagicCirclePattern worldPattern = MagicCirclePatterns.getPatternForSeed(sl.getSeed(), circleType);
                        if (worldPattern != null) {
                            pattern = worldPattern;
                        }
                    }
                    BlockPos linkedPos = getLinkedPos(stack);
                    ResourceLocation linkedDim = getLinkedDim(stack);

                    circleBE.setPattern(pattern);
                    circleBE.setCircleTypeId(circleType);
                    if (player != null) {
                        circleBE.setOwnerUUID(player.getUUID());
                    }

                    // Transfer captured mob soul into newly placed circle
                    if (hasCapturedMob(stack)) {
                        circleBE.setCapturedMob(
                                getCapturedEntityId(stack),
                                getCapturedEntityTag(stack),
                                getCapturedEntityName(stack),
                                getCapturedEntityMaxHp(stack)
                        );
                        if (circleBE.getCircleType() != null) {
                            circleBE.setRequiredMana(circleBE.getCircleType().calculateRequiredMana(level, placePos, null));
                        }
                    }

                    if (linkedPos != null && isTeleportType(circleType)) {
                        circleBE.setLinked(linkedPos, linkedDim);

                        if (linkedDim == null || linkedDim.equals(level.dimension().location())) {
                            if (level.getBlockEntity(linkedPos) instanceof MagicCircleBlockEntity otherBE) {
                                otherBE.setLinked(placePos, level.dimension().location());
                                otherBE.setChanged();
                                level.sendBlockUpdated(linkedPos, otherBE.getBlockState(), otherBE.getBlockState(), 3);
                            }
                        }
                    }
                    circleBE.setChanged();
                    level.sendBlockUpdated(placePos, stateToPlace, stateToPlace, 3);
                }
            }

            level.playSound(null, placePos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 0.8f);
            level.playSound(null, placePos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8f, 1.2f);
            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation circleTypeId = getCircleTypeId(stack);
        if (circleTypeId != null) {
            boolean isStudied = false;
            if (dev.architectury.platform.Platform.getEnv() == net.fabricmc.api.EnvType.CLIENT) {
                Player clientPlayer = net.minecraft.client.Minecraft.getInstance().player;
                if (clientPlayer != null) {
                    if (clientPlayer.isCreative()) {
                        isStudied = true;
                    } else {
                        com.mushokucraft.data.PlayerMasteryData data = com.mushokucraft.data.PlayerMasteryProvider.get(clientPlayer);
                        if (data != null && data.isCircleStudied(circleTypeId)) {
                            isStudied = true;
                        }
                    }
                }
            }

            if (isStudied) {
                MagicCircleType circleType = MagicCircleRegistry.get(circleTypeId);
                Component name = circleType != null ? circleType.getDisplayName() : Component.literal(circleTypeId.toString());
                tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.type", name).withStyle(ChatFormatting.GOLD));
            } else {
                tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.unstudied",
                        Component.translatable("item.mushokucraft.inscribed_manuscript.unstudied_glyph")).withStyle(ChatFormatting.DARK_PURPLE));
            }
        } else {
            tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.unknown").withStyle(ChatFormatting.GRAY));
        }

        int size = getSize(stack);
        if (size == 3) {
            tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.size_3x3").withStyle(ChatFormatting.GOLD));
        }

        if (isTeleportType(circleTypeId)) {
            BlockPos linked = getLinkedPos(stack);
            if (linked != null) {
                tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.linked", linked.getX(), linked.getY(), linked.getZ()).withStyle(ChatFormatting.AQUA));
            } else {
                tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.unlinked").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (hasCapturedMob(stack)) {
            String name = getCapturedEntityName(stack);
            float hp = getCapturedEntityMaxHp(stack);
            tooltipComponents.add(Component.translatable("tooltip.mushokucraft.captured_soul", name != null ? name : "Существо").withStyle(ChatFormatting.DARK_PURPLE));
            tooltipComponents.add(Component.translatable("tooltip.mushokucraft.captured_hp", String.format("%.0f", hp)).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.mushokucraft.captured_hint").withStyle(ChatFormatting.AQUA));
        }

        if (com.mushokucraft.magic.circle.MagicCreationCircleType.ID.equals(circleTypeId)) {
            tooltipComponents.add(Component.translatable("tooltip.mushokucraft.creation_hint").withStyle(ChatFormatting.AQUA));
        }

        tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.help1").withStyle(ChatFormatting.DARK_PURPLE));
        if (isTeleportType(circleTypeId)) {
            tooltipComponents.add(Component.translatable("item.mushokucraft.inscribed_manuscript.help2").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
