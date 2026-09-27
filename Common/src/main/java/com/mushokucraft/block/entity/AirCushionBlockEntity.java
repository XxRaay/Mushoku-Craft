package com.mushokucraft.block.entity;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class AirCushionBlockEntity extends BlockEntity {
    private UUID ownerId;
    private int ticksLived = 0;

    public AirCushionBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_CUSHION_BE.get(), pos, state);
    }

    public void setOwner(UUID ownerId) {
        this.ownerId = ownerId;
        this.setChanged();
    }

    public UUID getOwner() {
        return ownerId;
    }

    public void resetTicksLived() {
        this.ticksLived = 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AirCushionBlockEntity entity) {
        if (!level.isClientSide) {
            entity.ticksLived++;
            if (entity.ticksLived >= MushokuConfig.AIR_CUSHION_BLOCK_LIFETIME.get()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ownerId != null) {
            tag.putUUID("Owner", this.ownerId);
        }
        tag.putInt("TicksLived", this.ticksLived);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("Owner")) {
            this.ownerId = tag.getUUID("Owner");
        }
        this.ticksLived = tag.getInt("TicksLived");
    }
}
