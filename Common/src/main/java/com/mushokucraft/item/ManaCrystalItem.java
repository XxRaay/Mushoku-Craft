package com.mushokucraft.item;

import com.mushokucraft.block.MagicCircleBlock;
import com.mushokucraft.block.entity.MagicCircleBlockEntity;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.init.ModItems;
import com.mushokucraft.magic.circle.DimensionalGateCircleType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ManaCrystalItem extends Item {
    private final float manaRestored;

    public ManaCrystalItem(Properties properties, float manaRestored) {
        super(properties);
        this.manaRestored = manaRestored;
    }

    public float getManaRestored() {
        return manaRestored;
    }

    public static boolean isGateAttuned(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().contains("GateX");
    }

    public static BlockPos getLinkedGatePos(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("GateX") && tag.contains("GateY") && tag.contains("GateZ")) {
            return new BlockPos(tag.getInt("GateX"), tag.getInt("GateY"), tag.getInt("GateZ"));
        }
        return null;
    }

    public static ResourceLocation getLinkedGateDim(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("GateDim")) {
            return ResourceLocation.tryParse(tag.getString("GateDim"));
        }
        return null;
    }

    public static void setLinkedGate(ItemStack stack, BlockPos pos, ResourceLocation dim) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (pos != null) {
                tag.putInt("GateX", pos.getX());
                tag.putInt("GateY", pos.getY());
                tag.putInt("GateZ", pos.getZ());
            } else {
                tag.remove("GateX");
                tag.remove("GateY");
                tag.remove("GateZ");
            }
            if (dim != null) {
                tag.putString("GateDim", dim.toString());
            } else {
                tag.remove("GateDim");
            }
        });
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isGateAttuned(stack) || super.isFoil(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.MAGIC_CIRCLE.get())) {
            return super.useOn(context);
        }

        MagicCircleBlock.CirclePart part = state.hasProperty(MagicCircleBlock.PART) ?
                state.getValue(MagicCircleBlock.PART) : MagicCircleBlock.CirclePart.CENTER;
        BlockPos centerPos = pos.offset(-part.getDx(), 0, -part.getDz());

        if (!(level.getBlockEntity(centerPos) instanceof MagicCircleBlockEntity circleBE)) {
            return super.useOn(context);
        }

        // 1. Large Mana Crystal: Soul Anchor binding
        if (stack.is(ModItems.LARGE_MANA_CRYSTAL.get())) {
            if (!circleBE.hasSoulAnchorType()) {
                return super.useOn(context);
            }

            if (circleBE.getSize() < 3) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_3x3"), true);
                }
                return InteractionResult.FAIL;
            }

            if (circleBE.getAdditionalLayers().size() < 2) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_three_layers"), true);
                }
                return InteractionResult.FAIL;
            }

            if (!circleBE.allLayersExpanded()) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_layers_3x3"), true);
                }
                return InteractionResult.FAIL;
            }

            if (!circleBE.isSoulAnchor()) {
                if (!level.isClientSide() && player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_requires_sealing_layer"), true);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide() && player != null) {
                if (circleBE.isBoundTo(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_already_bound_self"), true);
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }

                // Bind player's soul
                stack.shrink(1);
                circleBE.bindPlayer(player);

                PlayerMasteryData mastery = PlayerMasteryProvider.get(player);
                if (mastery != null) {
                    mastery.setSoulAnchor(centerPos, level.dimension().location());
                }

                level.playSound(null, centerPos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.2f);
                level.playSound(null, centerPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.5f);
                level.playSound(null, centerPos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 60, 1.2, 0.4, 1.2, 0.15);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 35, 0.9, 0.3, 0.9, 0.05);
                    sl.sendParticles(ParticleTypes.FLASH, centerPos.getX() + 0.5, centerPos.getY() + 0.6, centerPos.getZ() + 0.5, 2, 0, 0, 0, 0);
                }

                String dimName = level.dimension().location().toString();
                player.displayClientMessage(Component.translatable("message.mushokucraft.soul_anchor_attuned",
                        centerPos.getX(), centerPos.getY(), centerPos.getZ(), dimName), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // 2. Medium mana crystals: Interdimensional Gates
        if (!stack.is(ModItems.MEDIUM_MANA_CRYSTAL.get())) {
            return super.useOn(context);
        }

        if (!circleBE.hasDimensionalGateType()) {
            return super.useOn(context);
        }

        if (circleBE.getSize() < 3) {
            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_3x3"), true);
            }
            return InteractionResult.FAIL;
        }

        if (circleBE.getAdditionalLayers().isEmpty()) {
            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!circleBE.allLayersExpanded()) {
            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers_3x3"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!circleBE.isDimensionalGate()) {
            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_only_self"), true);
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            if (!isGateAttuned(stack)) {
                // Attune crystal to this origin gate
                if (stack.getCount() > 1) {
                    ItemStack attuned = stack.copyWithCount(1);
                    setLinkedGate(attuned, centerPos, level.dimension().location());
                    stack.shrink(1);
                    if (player != null && !player.getInventory().add(attuned)) {
                        player.drop(attuned, false);
                    }
                } else {
                    setLinkedGate(stack, centerPos, level.dimension().location());
                }

                level.playSound(null, centerPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.6f);
                level.playSound(null, centerPos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0f, 1.4f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.PORTAL, centerPos.getX() + 0.5, centerPos.getY() + 0.3, centerPos.getZ() + 0.5, 30, 0.6, 0.3, 0.6, 0.1);
                    sl.sendParticles(ParticleTypes.GLOW, centerPos.getX() + 0.5, centerPos.getY() + 0.4, centerPos.getZ() + 0.5, 15, 0.4, 0.3, 0.4, 0.05);
                }

                if (player != null) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.crystal_attuned_to_gate",
                            centerPos.getX(), centerPos.getY(), centerPos.getZ(), level.dimension().location().toString()), true);
                }
                return InteractionResult.SUCCESS;
            } else {
                // Attuned crystal used on destination gate!
                BlockPos sourcePos = getLinkedGatePos(stack);
                ResourceLocation sourceDim = getLinkedGateDim(stack);

                if (sourcePos != null && sourcePos.equals(centerPos) && sourceDim != null && sourceDim.equals(level.dimension().location())) {
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.crystal_already_attuned_same"), true);
                    }
                    return InteractionResult.FAIL;
                }

                if (sourcePos != null && sourceDim != null) {
                    // Check remote source gate validity & multi-layer status
                    ServerLevel sourceLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, sourceDim));
                    if (sourceLevel == null) {
                        if (player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
                        }
                        return InteractionResult.FAIL;
                    }
                    if (!sourceLevel.hasChunk(sourcePos.getX() >> 4, sourcePos.getZ() >> 4)) {
                        sourceLevel.getChunk(sourcePos.getX() >> 4, sourcePos.getZ() >> 4);
                    }
                    if (!(sourceLevel.getBlockEntity(sourcePos) instanceof MagicCircleBlockEntity sourceBE) || !sourceBE.isDimensionalGate()) {
                        if (player != null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_target_not_multilayer"), true);
                        }
                        return InteractionResult.FAIL;
                    }

                    // 1. Link destination gate to source gate
                    circleBE.setLinked(sourcePos, sourceDim);
                    circleBE.setCurrentMana(0.0f);
                    circleBE.setChanged();
                    level.sendBlockUpdated(centerPos, circleBE.getBlockState(), circleBE.getBlockState(), 3);

                    // 2. Link source gate to destination gate (cross-dimension supported!)
                    sourceBE.setLinked(centerPos, level.dimension().location());
                    sourceBE.setCurrentMana(0.0f);
                    sourceBE.setChanged();
                    sourceLevel.sendBlockUpdated(sourcePos, sourceBE.getBlockState(), sourceBE.getBlockState(), 3);

                    sourceLevel.playSound(null, sourcePos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.2f);
                    sourceLevel.playSound(null, sourcePos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8f, 1.4f);
                    sourceLevel.sendParticles(ParticleTypes.FLASH, sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.0);
                    sourceLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5, 50, 1.2, 0.4, 1.2, 0.2);

                    // Consume attuned medium mana crystal
                    stack.shrink(1);

                    level.playSound(null, centerPos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0f, 1.2f);
                    level.playSound(null, centerPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8f, 1.4f);
                    level.playSound(null, centerPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.6f);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.FLASH, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.0);
                        sl.sendParticles(ParticleTypes.REVERSE_PORTAL, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 50, 1.2, 0.4, 1.2, 0.2);
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5, 30, 0.8, 0.4, 0.8, 0.1);
                    }

                    if (player != null) {
                        player.displayClientMessage(Component.translatable("message.mushokucraft.gate_linked_bidirectional"), true);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isGateAttuned(stack)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.crystal_cannot_consume_attuned"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide()) {
            PlayerMasteryData data = PlayerMasteryProvider.get(player);
            if (data != null) {
                float effectiveMax = data.getMaxMana() + com.mushokucraft.accessory.AccessoryHelper.getMaxManaBonus(player);
                if (data.getMana() >= effectiveMax) {
                    player.displayClientMessage(Component.translatable("message.mushokucraft.mana_already_full"), true);
                    return InteractionResultHolder.fail(stack);
                }

                data.regenMana(this.manaRestored, effectiveMax);
                PlayerMasteryProvider.sync(player);

                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                            20, 0.35, 0.5, 0.35, 0.05);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(),
                            10, 0.25, 0.4, 0.25, 0.03);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
                    sl.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                }

                player.displayClientMessage(Component.translatable("message.mushokucraft.mana_crystal_consumed",
                        String.format("%.0f", this.manaRestored)), true);
                stack.shrink(1);
                return InteractionResultHolder.consume(stack);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (isGateAttuned(stack)) {
            BlockPos p = getLinkedGatePos(stack);
            ResourceLocation d = getLinkedGateDim(stack);
            if (p != null && d != null) {
                tooltipComponents.add(Component.translatable("tooltip.mushokucraft.gate_attuned_pos",
                        p.getX(), p.getY(), p.getZ(), d.toString()).withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltipComponents.add(Component.translatable("tooltip.mushokucraft.gate_attuned_hint").withStyle(ChatFormatting.AQUA));
            }
        }

        tooltipComponents.add(Component.translatable("tooltip.mushokucraft.mana_crystal_restore",
                String.format("%.0f", this.manaRestored)).withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.translatable("tooltip.mushokucraft.mana_crystal_desc").withStyle(ChatFormatting.GRAY));
    }
}
