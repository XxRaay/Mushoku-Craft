package com.mushokucraft.block.entity;

import com.mushokucraft.block.BarrierWallBlock;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModBlockEntities;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.magic.circle.BarrierCircleType;
import com.mushokucraft.magic.circle.CrystallizationCircleType;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import com.mushokucraft.magic.circle.SummoningCircleType;
import com.mushokucraft.magic.circle.TeleportCircleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MagicCircleBlockEntity extends BlockEntity {
    private MagicCirclePattern pattern = new MagicCirclePattern();
    private ResourceLocation circleTypeId;
    private BlockPos linkedPos;
    private ResourceLocation linkedDim;
    private float currentMana = 0.0f;
    private float requiredMana = 0.0f;
    private int size = 1;
    private boolean sheared = false;
    private int triggerCooldown = 0;
    private UUID ownerUUID;

    // Barrier state
    private boolean barrierActive = false;
    private float barrierHealth = 0.0f;
    private float barrierMaxHealth = 100.0f;
    private final List<BlockPos> barrierPositions = new ArrayList<>();

    // Captured mob state (for soul trap & summoning projection)
    private String capturedEntityId;
    private CompoundTag capturedEntityTag;
    private String capturedEntityName;
    private float capturedEntityMaxHp;

    // Client-side cached entity for 3D projection rendering
    private Entity clientRenderEntity;
    private String lastRenderEntityId;

    public MagicCircleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_CIRCLE_BE.get(), pos, state);
    }

    public boolean hasCapturedMob() {
        return capturedEntityId != null && !capturedEntityId.isEmpty();
    }

    @Nullable
    public String getCapturedEntityId() {
        return capturedEntityId;
    }

    @Nullable
    public CompoundTag getCapturedEntityTag() {
        return capturedEntityTag;
    }

    @Nullable
    public String getCapturedEntityName() {
        return capturedEntityName;
    }

    public float getCapturedEntityMaxHp() {
        return capturedEntityMaxHp;
    }

    public void setCapturedMob(@Nullable String id, @Nullable CompoundTag tag, @Nullable String name, float maxHp) {
        this.capturedEntityId = id;
        this.capturedEntityTag = tag;
        this.capturedEntityName = name;
        this.capturedEntityMaxHp = maxHp;
        this.clientRenderEntity = null;
        this.lastRenderEntityId = null;
        this.setChanged();
    }

    public void clearCapturedMob() {
        this.capturedEntityId = null;
        this.capturedEntityTag = null;
        this.capturedEntityName = null;
        this.capturedEntityMaxHp = 0.0f;
        this.clientRenderEntity = null;
        this.lastRenderEntityId = null;
        this.setChanged();
    }

    @Nullable
    public Entity getOrCreateRenderEntity(@Nullable Level level) {
        if (capturedEntityId == null || capturedEntityId.isEmpty() || level == null) {
            clientRenderEntity = null;
            lastRenderEntityId = null;
            return null;
        }
        if (clientRenderEntity == null || !capturedEntityId.equals(lastRenderEntityId)) {
            ResourceLocation rl = ResourceLocation.tryParse(capturedEntityId);
            if (rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
                clientRenderEntity = type.create(level);
                if (clientRenderEntity != null && capturedEntityTag != null) {
                    try {
                        clientRenderEntity.load(capturedEntityTag);
                    } catch (Exception ignored) {}
                }
                lastRenderEntityId = capturedEntityId;
            }
        }
        return clientRenderEntity;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }

    public MagicCirclePattern getPattern() {
        return pattern;
    }

    public void setPattern(MagicCirclePattern pattern) {
        this.pattern = pattern != null ? pattern : new MagicCirclePattern();
        this.setChanged();
    }

    public ResourceLocation getCircleTypeId() {
        return circleTypeId;
    }

    public void setCircleTypeId(ResourceLocation id) {
        this.circleTypeId = id;
        this.setChanged();
    }

    public MagicCircleType getCircleType() {
        if (circleTypeId != null) {
            return MagicCircleRegistry.get(circleTypeId);
        }
        return null;
    }

    public BlockPos getLinkedPos() {
        return linkedPos;
    }

    public ResourceLocation getLinkedDim() {
        return linkedDim;
    }

    public void setLinked(BlockPos pos, ResourceLocation dim) {
        this.linkedPos = pos;
        this.linkedDim = dim;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float mana) {
        this.currentMana = mana;
        this.setChanged();
    }

    public float getRequiredMana() {
        return requiredMana;
    }

    public void setRequiredMana(float requiredMana) {
        this.requiredMana = requiredMana;
        this.setChanged();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = (size == 3) ? 3 : 1;
        this.setChanged();
    }

    public boolean isSheared() {
        return sheared;
    }

    public void setSheared(boolean sheared) {
        this.sheared = sheared;
        this.setChanged();
    }

    public boolean isDestinationValid(Level level) {
        if (this.linkedPos == null) return false;

        ServerLevel targetLevel = null;
        if (level instanceof ServerLevel sl) {
            if (this.linkedDim != null && !this.linkedDim.equals(sl.dimension().location())) {
                targetLevel = sl.getServer().getLevel(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION, this.linkedDim));
            } else {
                targetLevel = sl;
            }
        }

        if (targetLevel != null) {
            if (!targetLevel.hasChunk(this.linkedPos.getX() >> 4, this.linkedPos.getZ() >> 4)) {
                targetLevel.getChunk(this.linkedPos.getX() >> 4, this.linkedPos.getZ() >> 4);
            }
            if (targetLevel.getBlockEntity(this.linkedPos) instanceof MagicCircleBlockEntity targetBE) {
                return this.circleTypeId != null && this.circleTypeId.equals(targetBE.getCircleTypeId());
            }
            return false;
        }

        if (level.isLoaded(this.linkedPos)) {
            if (level.getBlockEntity(this.linkedPos) instanceof MagicCircleBlockEntity targetBE) {
                return this.circleTypeId != null && this.circleTypeId.equals(targetBE.getCircleTypeId());
            }
            return false;
        }
        return true;
    }

    public boolean isBarrierActive() {
        return barrierActive;
    }

    public float getBarrierHealth() {
        return barrierHealth;
    }

    public float getBarrierMaxHealth() {
        return barrierMaxHealth;
    }

    public boolean activateBarrier(ServerLevel level) {
        int radius = (this.size == 3) ?
                MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_RADIUS.get() :
                MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_RADIUS.get();
        int height = (this.size == 3) ?
                MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_HEIGHT.get() :
                MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_HEIGHT.get();
        float maxHP = (this.size == 3) ?
                MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_MAX_HP.get().floatValue() :
                MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_MAX_HP.get().floatValue();

        deactivateBarrier(level, false);

        this.barrierActive = true;
        this.barrierMaxHealth = maxHP;
        this.barrierHealth = maxHP;

        int minX = worldPosition.getX() - radius;
        int maxX = worldPosition.getX() + radius;
        int minZ = worldPosition.getZ() - radius;
        int maxZ = worldPosition.getZ() + radius;
        int minY = worldPosition.getY();
        int maxY = worldPosition.getY() + height - 1;

        for (int y = minY; y <= maxY; y++) {
            // North edge
            for (int x = minX; x <= maxX; x++) {
                BlockPos p = new BlockPos(x, y, minZ);
                boolean west = (x == minX);
                boolean east = (x == maxX);
                placeBarrierWall(level, p, true, false, east, west);
            }
            // South edge
            for (int x = minX; x <= maxX; x++) {
                BlockPos p = new BlockPos(x, y, maxZ);
                boolean west = (x == minX);
                boolean east = (x == maxX);
                placeBarrierWall(level, p, false, true, east, west);
            }
            // West edge (excluding corners)
            for (int z = minZ + 1; z < maxZ; z++) {
                BlockPos p = new BlockPos(minX, y, z);
                placeBarrierWall(level, p, false, false, false, true);
            }
            // East edge (excluding corners)
            for (int z = minZ + 1; z < maxZ; z++) {
                BlockPos p = new BlockPos(maxX, y, z);
                placeBarrierWall(level, p, false, false, true, false);
            }
        }

        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
        level.playSound(null, worldPosition, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.4f);
        level.sendParticles(ParticleTypes.FLASH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 1, 0, 0, 0, 0);

        this.setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    private void placeBarrierWall(ServerLevel level, BlockPos p, boolean north, boolean south, boolean east, boolean west) {
        BlockState current = level.getBlockState(p);
        if (current.isAir() || current.canBeReplaced()) {
            BlockState wallState = ModBlocks.BARRIER_WALL.get().defaultBlockState()
                    .setValue(BarrierWallBlock.NORTH, north)
                    .setValue(BarrierWallBlock.SOUTH, south)
                    .setValue(BarrierWallBlock.EAST, east)
                    .setValue(BarrierWallBlock.WEST, west);
            level.setBlock(p, wallState, 3);
            if (level.getBlockEntity(p) instanceof BarrierWallBlockEntity wallBE) {
                wallBE.setCirclePos(worldPosition);
                wallBE.setOwnerUUID(this.ownerUUID);
                level.sendBlockUpdated(p, wallState, wallState, 3);
            }
            barrierPositions.add(p);
        }
    }

    public void deactivateBarrier(Level level, boolean destroyed) {
        for (BlockPos bPos : this.barrierPositions) {
            if (level.getBlockState(bPos).is(ModBlocks.BARRIER_WALL.get())) {
                level.removeBlock(bPos, false);
            }
        }
        this.barrierPositions.clear();
        this.barrierActive = false;
        this.barrierHealth = 0.0f;
        this.setChanged();

        if (destroyed) {
            if (level instanceof ServerLevel sl) {
                sl.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.2f, 0.8f);
                sl.playSound(null, worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.8f, 0.5f);
                sl.sendParticles(ParticleTypes.EXPLOSION, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.1);
            }
            level.destroyBlock(worldPosition, false);
        } else {
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void damageBarrier(float damage, @Nullable Entity attacker) {
        if (!this.barrierActive) return;

        this.barrierHealth -= damage;
        this.setChanged();

        if (attacker instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_health",
                    String.format("%.0f", Math.max(0, barrierHealth)),
                    String.format("%.0f", barrierMaxHealth)), true);
        }

        if (this.barrierHealth <= 0.0f) {
            if (this.level != null) {
                deactivateBarrier(this.level, true);
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicCircleBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.triggerCooldown > 0) {
            be.triggerCooldown--;
        }

        // Auto-identify if not yet resolved
        if (be.circleTypeId == null && be.pattern != null && be.pattern.countFilled() > 0) {
            MagicCircleType type = MagicCircleRegistry.identify(level, be.pattern);
            if (type != null) {
                be.circleTypeId = type.getId();
                be.setChanged();
            }
        }

        MagicCircleType type = be.getCircleType();
        if (type == null) return;

        type.onServerTick((ServerLevel) level, pos, be);

        // Recalculate required mana
        if (be.linkedPos != null || be.circleTypeId != null) {
            be.requiredMana = type.calculateRequiredMana(level, pos, be.linkedPos);
        }

        // Barrier upkeep drain
        if (be.barrierActive) {
            if (level.getGameTime() % 40 == 0) {
                float upkeep = (be.size == 3) ?
                        MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_UPKEEP.get().floatValue() :
                        MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_UPKEEP.get().floatValue();

                if (be.currentMana >= upkeep) {
                    be.currentMana -= upkeep;
                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5, 1, 0.2, 0.05, 0.2, 0.01);
                    }
                } else {
                    // Out of mana -> gentle dissipation
                    if (level instanceof ServerLevel sl) {
                        sl.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
                        be.deactivateBarrier(sl, false);
                    }
                    return;
                }
            }
        }

        // Check players standing on the circle (radius scaled for 1x1 or 3x3)
        double halfSize = (be.size == 3) ? 1.5 : 0.5;
        AABB box = new AABB(pos.getX() + 0.5 - halfSize, pos.getY(), pos.getZ() + 0.5 - halfSize,
                            pos.getX() + 0.5 + halfSize, pos.getY() + 1.0, pos.getZ() + 0.5 + halfSize);
        List<Player> players = level.getEntitiesOfClass(Player.class, box);

        boolean isTeleport = TeleportCircleType.ID.equals(be.circleTypeId);
        boolean isSummoning = SummoningCircleType.ID.equals(be.circleTypeId);
        boolean isCrystallization = CrystallizationCircleType.ID.equals(be.circleTypeId);
        boolean canInfuse = true;
        if (isTeleport) canInfuse = (be.linkedPos != null);
        if (isSummoning) canInfuse = be.hasCapturedMob();
        if (isCrystallization) canInfuse = (be.requiredMana > 0.0f);

        for (Player player : players) {
            if (player.isShiftKeyDown()) {
                if (!canInfuse) {
                    if (level.getGameTime() % 40 == 0) {
                        if (isTeleport) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_not_linked"), true);
                        } else if (isSummoning) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.summon_requires_mob"), true);
                        } else if (isCrystallization) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_requires_mineral"), true);
                        }
                    }
                    continue;
                }
                // If destination is not valid/broken for teleport, reset link immediately!
                if (isTeleport && !be.isDestinationValid(level)) {
                    be.setLinked(null, null);
                    be.setCurrentMana(0.0f);
                    be.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    player.displayClientMessage(Component.translatable("message.mushokucraft.circle_target_missing"), true);
                    continue;
                }

                // Infuse mana
                if (be.ownerUUID == null) {
                    be.ownerUUID = player.getUUID();
                    be.setChanged();
                }
                float rate = MushokuConfig.MAGIC_CIRCLE_INFUSION_RATE_PER_TICK.get().floatValue();
                PlayerMasteryData masteryData = PlayerMasteryProvider.get(player);

                if (masteryData != null && masteryData.getMana() >= rate) {
                    float maxTarget = be.barrierActive ? (be.requiredMana * 2.0f) : be.requiredMana;
                    float needed = Math.max(0.0f, maxTarget - be.currentMana);
                    float drain = Math.min(rate, needed);

                    if (drain > 0.0f && masteryData.consumeMana(drain)) {
                        be.currentMana += drain;
                        if (be.barrierActive) {
                            be.barrierHealth = Math.min(be.barrierMaxHealth, be.barrierHealth + drain * 0.5f);
                        }
                        type.onChannelTick((ServerLevel) level, pos, player, be.currentMana, be.requiredMana);

                        if (player instanceof ServerPlayer sp) {
                            ModGameEvents.syncMana(sp, masteryData);
                        }

                        if (level.getGameTime() % 4 == 0) {
                            if (be.barrierActive) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status",
                                        String.format("%.0f", be.currentMana),
                                        String.format("%.0f", be.barrierHealth),
                                        String.format("%.0f", be.barrierMaxHealth)), true);
                            } else {
                                int pct = (int) ((be.currentMana / Math.max(1.0f, be.requiredMana)) * 100);
                                player.displayClientMessage(Component.translatable("message.mushokucraft.infusing_mana",
                                        String.format("%.0f", be.currentMana),
                                        String.format("%.0f", be.requiredMana),
                                        pct), true);
                            }
                        }
                    }
                }

                // Check activation (only if not already active barrier)
                if (!be.barrierActive && be.triggerCooldown <= 0 && be.currentMana >= be.requiredMana && be.requiredMana > 0.0f) {
                    boolean success = type.onTrigger((ServerLevel) level, pos, be.linkedPos, player);
                    if (success) {
                        if (BarrierCircleType.ID.equals(be.circleTypeId)) {
                            be.triggerCooldown = 20;
                        } else {
                            be.currentMana = 0.0f;
                        }
                        be.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                    } else {
                        be.triggerCooldown = 40; // 2s failure cooldown to avoid sound/chat spam
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.pattern != null) {
            tag.put("Pattern", this.pattern.save());
        }
        if (this.circleTypeId != null) {
            tag.putString("CircleType", this.circleTypeId.toString());
        }
        if (this.linkedPos != null) {
            tag.putInt("LinkedX", this.linkedPos.getX());
            tag.putInt("LinkedY", this.linkedPos.getY());
            tag.putInt("LinkedZ", this.linkedPos.getZ());
        }
        if (this.linkedDim != null) {
            tag.putString("LinkedDim", this.linkedDim.toString());
        }
        tag.putFloat("CurrentMana", this.currentMana);
        tag.putFloat("RequiredMana", this.requiredMana);
        tag.putInt("Size", this.size);
        tag.putBoolean("Sheared", this.sheared);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }

        // Captured mob data
        if (this.capturedEntityId != null) {
            tag.putString("CapturedEntityId", this.capturedEntityId);
        }
        if (this.capturedEntityTag != null) {
            tag.put("CapturedEntityTag", this.capturedEntityTag);
        }
        if (this.capturedEntityName != null) {
            tag.putString("CapturedEntityName", this.capturedEntityName);
        }
        tag.putFloat("CapturedEntityMaxHp", this.capturedEntityMaxHp);

        // Barrier NBT
        tag.putBoolean("BarrierActive", this.barrierActive);
        tag.putFloat("BarrierHealth", this.barrierHealth);
        tag.putFloat("BarrierMaxHealth", this.barrierMaxHealth);
        ListTag bList = new ListTag();
        for (BlockPos bp : this.barrierPositions) {
            CompoundTag bTag = new CompoundTag();
            bTag.putInt("X", bp.getX());
            bTag.putInt("Y", bp.getY());
            bTag.putInt("Z", bp.getZ());
            bList.add(bTag);
        }
        tag.put("BarrierPositions", bList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Pattern")) {
            this.pattern = MagicCirclePattern.load(tag.getCompound("Pattern"));
        }
        if (tag.contains("CircleType")) {
            this.circleTypeId = ResourceLocation.tryParse(tag.getString("CircleType"));
        }
        if (tag.contains("LinkedX") && tag.contains("LinkedY") && tag.contains("LinkedZ")) {
            this.linkedPos = new BlockPos(tag.getInt("LinkedX"), tag.getInt("LinkedY"), tag.getInt("LinkedZ"));
        } else {
            this.linkedPos = null;
        }
        if (tag.contains("LinkedDim")) {
            this.linkedDim = ResourceLocation.tryParse(tag.getString("LinkedDim"));
        } else {
            this.linkedDim = null;
        }
        this.currentMana = tag.getFloat("CurrentMana");
        this.requiredMana = tag.getFloat("RequiredMana");
        if (tag.contains("Size")) {
            this.size = tag.getInt("Size");
        }
        if (tag.contains("Sheared")) {
            this.sheared = tag.getBoolean("Sheared");
        }
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        } else {
            this.ownerUUID = null;
        }

        // Captured mob data
        if (tag.contains("CapturedEntityId")) {
            this.capturedEntityId = tag.getString("CapturedEntityId");
        } else {
            this.capturedEntityId = null;
        }
        if (tag.contains("CapturedEntityTag")) {
            this.capturedEntityTag = tag.getCompound("CapturedEntityTag");
        } else {
            this.capturedEntityTag = null;
        }
        if (tag.contains("CapturedEntityName")) {
            this.capturedEntityName = tag.getString("CapturedEntityName");
        } else {
            this.capturedEntityName = null;
        }
        this.capturedEntityMaxHp = tag.getFloat("CapturedEntityMaxHp");

        // Barrier NBT
        this.barrierActive = tag.getBoolean("BarrierActive");
        this.barrierHealth = tag.getFloat("BarrierHealth");
        this.barrierMaxHealth = tag.contains("BarrierMaxHealth") ? tag.getFloat("BarrierMaxHealth") : 100.0f;
        this.barrierPositions.clear();
        if (tag.contains("BarrierPositions", 9)) {
            ListTag bList = tag.getList("BarrierPositions", 10);
            for (int i = 0; i < bList.size(); i++) {
                CompoundTag bTag = bList.getCompound(i);
                this.barrierPositions.add(new BlockPos(bTag.getInt("X"), bTag.getInt("Y"), bTag.getInt("Z")));
            }
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
