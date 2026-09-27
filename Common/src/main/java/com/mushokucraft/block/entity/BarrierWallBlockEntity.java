package com.mushokucraft.block.entity;

import com.mushokucraft.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarrierWallBlockEntity extends BlockEntity {
    private BlockPos circlePos;
    private UUID ownerUUID;

    public BarrierWallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARRIER_WALL_BE.get(), pos, state);
    }

    public BlockPos getCirclePos() {
        return circlePos;
    }

    public void setCirclePos(BlockPos circlePos) {
        this.circlePos = circlePos;
        this.setChanged();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (circlePos != null) {
            tag.putInt("CircleX", circlePos.getX());
            tag.putInt("CircleY", circlePos.getY());
            tag.putInt("CircleZ", circlePos.getZ());
        }
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CircleX") && tag.contains("CircleY") && tag.contains("CircleZ")) {
            this.circlePos = new BlockPos(tag.getInt("CircleX"), tag.getInt("CircleY"), tag.getInt("CircleZ"));
        } else {
            this.circlePos = null;
        }
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        } else {
            this.ownerUUID = null;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}
