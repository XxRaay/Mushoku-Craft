package com.mushokucraft.block.entity;

import com.mushokucraft.block.BarrierWallBlock;
import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.event.ModGameEvents;
import com.mushokucraft.init.ModBlockEntities;
import com.mushokucraft.init.ModBlocks;
import com.mushokucraft.magic.circle.BarrierCircleType;
import com.mushokucraft.magic.circle.CaptureCircleType;
import com.mushokucraft.magic.circle.CrystallizationCircleType;
import com.mushokucraft.magic.circle.DimensionalGateCircleType;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import com.mushokucraft.magic.circle.MagicCircleRegistry;
import com.mushokucraft.magic.circle.MagicCircleType;
import com.mushokucraft.magic.circle.CreationCircleRecipe;
import com.mushokucraft.magic.circle.CreationCircleRecipes;
import com.mushokucraft.magic.circle.MagicCreationCircleType;
import com.mushokucraft.magic.circle.OvergrowthCircleType;
import com.mushokucraft.magic.circle.SanctuaryCircleType;
import com.mushokucraft.magic.circle.SoulAnchorCircleType;
import com.mushokucraft.magic.circle.SummoningCircleType;
import com.mushokucraft.magic.circle.TeleportCircleType;
import net.minecraft.world.entity.item.ItemEntity;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MagicCircleBlockEntity extends BlockEntity {
    public static class CircleLayer {
        private ResourceLocation circleTypeId;
        private MagicCirclePattern pattern;
        private int size; // 1 or 3

        public CircleLayer(ResourceLocation circleTypeId, MagicCirclePattern pattern, int size) {
            this.circleTypeId = circleTypeId;
            this.pattern = pattern != null ? pattern : new MagicCirclePattern();
            this.size = (size == 3) ? 3 : 1;
        }

        public ResourceLocation getCircleTypeId() { return circleTypeId; }
        public MagicCirclePattern getPattern() { return pattern; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = (size == 3) ? 3 : 1; }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            if (circleTypeId != null) tag.putString("Type", circleTypeId.toString());
            if (pattern != null) tag.put("Pattern", pattern.save());
            tag.putInt("Size", size);
            return tag;
        }

        public static CircleLayer load(CompoundTag tag) {
            ResourceLocation type = tag.contains("Type") ? ResourceLocation.tryParse(tag.getString("Type")) : null;
            MagicCirclePattern pat = tag.contains("Pattern") ? MagicCirclePattern.load(tag.getCompound("Pattern")) : new MagicCirclePattern();
            int sz = tag.contains("Size") ? tag.getInt("Size") : 1;
            return new CircleLayer(type, pat, sz);
        }
    }

    public static final int MAX_ADDITIONAL_LAYERS = 2;
    private final List<CircleLayer> additionalLayers = new ArrayList<>();

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

    // Soul Anchor binding state
    private UUID boundPlayerUUID;
    private String boundPlayerName;

    // Client-side cached entity for 3D projection rendering
    private Entity clientRenderEntity;
    private String lastRenderEntityId;

    public MagicCircleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_CIRCLE_BE.get(), pos, state);
    }

    @Nullable
    public UUID getBoundPlayerUUID() {
        return boundPlayerUUID;
    }

    @Nullable
    public String getBoundPlayerName() {
        return boundPlayerName;
    }

    public void bindPlayer(Player player) {
        this.boundPlayerUUID = player.getUUID();
        this.boundPlayerName = player.getGameProfile().getName();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void clearBoundPlayer() {
        this.boundPlayerUUID = null;
        this.boundPlayerName = null;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isBoundTo(UUID uuid) {
        return this.boundPlayerUUID != null && this.boundPlayerUUID.equals(uuid);
    }

    public boolean isBound() {
        return this.boundPlayerUUID != null;
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
        if (circleTypeId == null && pattern != null && pattern.countFilled() > 0 && level != null) {
            MagicCircleType type = MagicCircleRegistry.identify(level, pattern);
            if (type != null) {
                this.circleTypeId = type.getId();
                this.setChanged();
            }
        }
        return circleTypeId;
    }

    public void setCircleTypeId(ResourceLocation id) {
        this.circleTypeId = id;
        this.setChanged();
    }

    public MagicCircleType getCircleType() {
        if (isDimensionalGate()) {
            return MagicCircleRegistry.DIMENSIONAL_GATE;
        }
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

    public List<CircleLayer> getAdditionalLayers() {
        return additionalLayers;
    }

    public boolean hasAdditionalLayers() {
        return !additionalLayers.isEmpty();
    }

    public boolean hasLayerType(ResourceLocation typeId) {
        if (typeId == null) return false;
        for (CircleLayer layer : this.additionalLayers) {
            if (typeId.equals(layer.getCircleTypeId())) {
                return true;
            }
        }
        return false;
    }

    public boolean canAddLayer(@Nullable ResourceLocation layerType) {
        if (this.size != 3 || this.additionalLayers.size() >= MAX_ADDITIONAL_LAYERS) {
            return false;
        }
        if (this.barrierActive) {
            return false;
        }
        if (layerType == null) return true;

        boolean isBaseGate = DimensionalGateCircleType.ID.equals(this.circleTypeId);
        boolean isLayerGate = DimensionalGateCircleType.ID.equals(layerType);

        // Dimensional Gate only works with itself!
        if (isBaseGate && !isLayerGate) {
            return false;
        }
        if (!isBaseGate && isLayerGate) {
            return false;
        }

        boolean isBaseAnchor = SoulAnchorCircleType.ID.equals(this.circleTypeId);
        boolean isLayerAnchor = SoulAnchorCircleType.ID.equals(layerType);
        boolean isLayerSealing = CaptureCircleType.ID.equals(layerType);

        // Soul Anchor accepts only Soul Anchor and Sealing (Capture) layers without duplicates!
        if (isBaseAnchor) {
            if (!isLayerAnchor && !isLayerSealing) {
                return false;
            }
            if (hasLayerType(layerType)) {
                return false;
            }
        }
        if (!isBaseAnchor && isLayerAnchor) {
            return false;
        }

        return true;
    }

    public boolean canAddLayer() {
        return canAddLayer(null);
    }

    public boolean addLayer(ResourceLocation typeId, MagicCirclePattern pattern, int size) {
        if (!canAddLayer(typeId)) return false;
        this.additionalLayers.add(new CircleLayer(typeId, pattern, size));
        this.requiredMana = calculateTotalRequiredMana(this.level);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return true;
    }

    @Nullable
    public CircleLayer popTopLayer() {
        if (this.barrierActive) return null;
        if (this.additionalLayers.isEmpty()) return null;
        CircleLayer removed = this.additionalLayers.remove(this.additionalLayers.size() - 1);
        this.requiredMana = calculateTotalRequiredMana(this.level);
        if (!this.barrierActive) {
            this.currentMana = Math.min(this.currentMana, this.requiredMana);
        }
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
        return removed;
    }

    @Nullable
    public CircleLayer getTopLayer() {
        if (this.additionalLayers.isEmpty()) return null;
        return this.additionalLayers.get(this.additionalLayers.size() - 1);
    }

    public boolean expandTopLayer() {
        if (this.barrierActive) return false;
        CircleLayer top = getTopLayer();
        if (top != null && top.getSize() < 3) {
            top.setSize(3);
            this.requiredMana = calculateTotalRequiredMana(this.level);
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if this circle contains the given circle type either as base or in attached layers.
     */
    public boolean hasCircleType(@Nullable ResourceLocation id) {
        if (id == null) return false;
        if (id.equals(this.circleTypeId)) return true;
        for (CircleLayer layer : this.additionalLayers) {
            if (id.equals(layer.getCircleTypeId())) return true;
        }
        return false;
    }

    /**
     * Returns the size (1 or 3) associated with a given circle type on this circle.
     */
    public int getLayerSizeForType(@Nullable ResourceLocation typeId) {
        if (typeId == null) return this.size;
        if (typeId.equals(this.circleTypeId)) return this.size;
        for (CircleLayer layer : this.additionalLayers) {
            if (typeId.equals(layer.getCircleTypeId())) {
                return layer.getSize();
            }
        }
        return this.size;
    }

    /**
     * Checks if this circle's base type is Dimensional Gate.
     */
    public boolean hasDimensionalGateType() {
        return DimensionalGateCircleType.ID.equals(this.circleTypeId);
    }

    /**
     * Checks if all attached layers have been expanded to size 3 (3x3).
     */
    public boolean allLayersExpanded() {
        if (this.additionalLayers.isEmpty()) return false;
        for (CircleLayer layer : this.additionalLayers) {
            if (layer.getSize() < 3) return false;
        }
        return true;
    }

    /**
     * An Interdimensional Gate MUST be multi-layered with itself!
     * Requirements:
     * 1. Base circle must be 3x3.
     * 2. Base circle MUST be Dimensional Gate type.
     * 3. Must have at least 1 additional layer.
     * 4. All additional layers must also be Dimensional Gate type (only works with itself!).
     * 5. All additional layers must be expanded to 3x3!
     */
    public boolean isDimensionalGate() {
        if (this.size != 3) return false;
        if (!DimensionalGateCircleType.ID.equals(this.circleTypeId)) return false;
        if (this.additionalLayers.isEmpty()) return false;
        for (CircleLayer layer : this.additionalLayers) {
            if (!DimensionalGateCircleType.ID.equals(layer.getCircleTypeId())) return false;
            if (layer.getSize() < 3) return false;
        }
        return true;
    }

    /**
     * Checks if this circle's base type is Barrier.
     */
    public boolean isBaseBarrier() {
        return BarrierCircleType.ID.equals(this.circleTypeId);
    }

    /**
     * Checks if this circle's base type is Soul Anchor.
     */
    public boolean hasSoulAnchorType() {
        return SoulAnchorCircleType.ID.equals(this.circleTypeId);
    }

    /**
     * A Soul Anchor is a sacred 3-layer ritual altar!
     * Requirements:
     * 1. Base circle must be 3x3.
     * 2. Base circle MUST be Soul Anchor type.
     * 3. Must have 2 additional layers (total 3 layers: base + 2 floating layers).
     * 4. One additional layer must be Soul Anchor type.
     * 5. One additional layer must be Sealing / Capture type.
     * 6. All additional layers must be expanded to 3x3!
     */
    public boolean isSoulAnchor() {
        if (this.size != 3) return false;
        if (!SoulAnchorCircleType.ID.equals(this.circleTypeId)) return false;
        if (this.additionalLayers.size() < 2) return false;

        boolean hasAnchorLayer = false;
        boolean hasSealingLayer = false;

        for (CircleLayer layer : this.additionalLayers) {
            if (layer.getSize() < 3) return false;
            if (SoulAnchorCircleType.ID.equals(layer.getCircleTypeId())) {
                hasAnchorLayer = true;
            } else if (CaptureCircleType.ID.equals(layer.getCircleTypeId())) {
                hasSealingLayer = true;
            } else {
                return false;
            }
        }
        return hasAnchorLayer && hasSealingLayer;
    }

    public float calculateLayerRequiredMana(ResourceLocation typeId, int layerSize, @Nullable Level level) {
        if (typeId == null) return 0.0f;
        MagicCircleType type = MagicCircleRegistry.get(typeId);
        if (type == null) return 0.0f;

        String path = typeId.getPath();
        return switch (path) {
            case "dimensional_gate" -> MushokuConfig.MAGIC_CIRCLE_DIMENSIONAL_GATE_MANA.get().floatValue();
            case "soul_anchor" -> MushokuConfig.MAGIC_CIRCLE_SOUL_ANCHOR_MANA.get().floatValue();
            case "barrier" -> (layerSize == 3) ?
                    MushokuConfig.MAGIC_CIRCLE_BARRIER_3X3_MANA.get().floatValue() :
                    MushokuConfig.MAGIC_CIRCLE_BARRIER_1X1_MANA.get().floatValue();
            case "sanctuary" -> (layerSize == 3) ?
                    MushokuConfig.MAGIC_CIRCLE_SANCTUARY_3X3_MANA.get().floatValue() :
                    MushokuConfig.MAGIC_CIRCLE_SANCTUARY_1X1_MANA.get().floatValue();
            case "overgrowth" -> (layerSize == 3) ?
                    MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_3X3_MANA.get().floatValue() :
                    MushokuConfig.MAGIC_CIRCLE_OVERGROWTH_1X1_MANA.get().floatValue();
            case "capture" -> MushokuConfig.MAGIC_CIRCLE_CAPTURE_BASE_MANA.get().floatValue();
            case "summoning" -> {
                if (hasCapturedMob()) {
                    float maxHp = getCapturedEntityMaxHp();
                    float base = MushokuConfig.MAGIC_CIRCLE_SUMMON_BASE_MANA.get().floatValue();
                    float perHp = MushokuConfig.MAGIC_CIRCLE_SUMMON_MANA_PER_HP.get().floatValue();
                    yield base + maxHp * perHp;
                }
                yield 0.0f;
            }
            case "crystallization" -> {
                CrystallizationCircleType.CrystallizationRecipe r = null;
                if (level != null) {
                    net.minecraft.world.entity.item.ItemEntity it = CrystallizationCircleType.findTargetItemEntity(level, worldPosition, layerSize);
                    if (it != null) {
                        r = CrystallizationCircleType.getRecipe(it.getItem().getItem());
                    }
                }
                yield (r != null) ? r.manaCost() : 0.0f;
            }
            case "magic_creation" -> {
                if (level != null) {
                    List<ItemEntity> items = MagicCreationCircleType.findItems(level, worldPosition, layerSize);
                    CreationCircleRecipe recipe = CreationCircleRecipes.findRecipe(items, layerSize);
                    if (recipe != null) {
                        double mult = MushokuConfig.MAGIC_CIRCLE_CREATION_MANA_MULT.get();
                        float mana = recipe.getRequiredMana();
                        if (layerSize == 3) {
                            mana *= 0.75f;
                        }
                        yield (float) (mana * mult);
                    }
                }
                yield 0.0f;
            }
            case "teleportation", "teleport" -> {
                if (linkedPos != null) {
                    double dx = worldPosition.getX() - linkedPos.getX();
                    double dy = worldPosition.getY() - linkedPos.getY();
                    double dz = worldPosition.getZ() - linkedPos.getZ();
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    yield (float) (MushokuConfig.MAGIC_CIRCLE_TELEPORT_BASE_MANA.get() + dist * MushokuConfig.MAGIC_CIRCLE_TELEPORT_MANA_PER_BLOCK.get());
                }
                yield 999999.0f;
            }
            default -> (level != null) ? type.calculateRequiredMana(level, worldPosition, linkedPos) : 0.0f;
        };
    }

    /**
     * Calculates the summed required mana across the base circle and all attached layers!
     */
    public float calculateTotalRequiredMana(@Nullable Level level) {
        float total = 0.0f;
        MagicCircleType baseType = getCircleType();
        if (baseType != null) {
            total += Math.max(0.0f, calculateLayerRequiredMana(baseType.getId(), this.size, level));
        }

        // Sum up every additional attached layer
        for (CircleLayer layer : this.additionalLayers) {
            ResourceLocation lId = layer.getCircleTypeId();
            if (lId == null) continue;

            float layerCost = calculateLayerRequiredMana(lId, layer.getSize(), level);
            total += Math.max(0.0f, layerCost);
        }

        return total;
    }

    /**
     * Triggers all active layers in this multi-layered circle simultaneously!
     */
    public boolean triggerAllLayers(ServerLevel level, BlockPos pos, @Nullable Player player) {
        List<MagicCircleType> instantTypes = new ArrayList<>();
        List<MagicCircleType> transitTypes = new ArrayList<>();
        List<MagicCircleType> persistentTypes = new ArrayList<>();

        Set<ResourceLocation> collected = new LinkedHashSet<>();
        MagicCircleType baseType = getCircleType();
        if (baseType != null) {
            collected.add(baseType.getId());
        }
        for (CircleLayer layer : this.additionalLayers) {
            if (layer.getCircleTypeId() != null) {
                collected.add(layer.getCircleTypeId());
            }
        }

        for (ResourceLocation id : collected) {
            MagicCircleType t = MagicCircleRegistry.get(id);
            if (t == null) continue;
            if (DimensionalGateCircleType.ID.equals(id) || TeleportCircleType.ID.equals(id)) {
                transitTypes.add(t);
            } else if (BarrierCircleType.ID.equals(id) || SanctuaryCircleType.ID.equals(id) || OvergrowthCircleType.ID.equals(id)) {
                persistentTypes.add(t);
            } else {
                instantTypes.add(t);
            }
        }

        float instantManaCost = 0.0f;
        boolean anyTriggered = false;

        if (isBaseBarrier()) {
            // Base is Barrier: the barrier MUST be activated first!
            if (baseType == null) return false;
            boolean barrierOk = baseType.onTrigger(level, pos, this.linkedPos, player);
            if (!barrierOk || !this.barrierActive) {
                // Barrier activation failed -> layers must not activate!
                return false;
            }
            anyTriggered = true;

            // Only AFTER the barrier is active, activate all additional layers!
            for (MagicCircleType t : instantTypes) {
                if (t.getId().equals(baseType.getId())) continue;
                boolean ok = t.onTrigger(level, pos, this.linkedPos, player);
                if (ok) {
                    int layerSize = getLayerSizeForType(t.getId());
                    instantManaCost += calculateLayerRequiredMana(t.getId(), layerSize, level);
                }
            }

            for (MagicCircleType t : persistentTypes) {
                if (t.getId().equals(baseType.getId())) continue;
                t.onTrigger(level, pos, this.linkedPos, player);
            }

            for (MagicCircleType t : transitTypes) {
                if (t.getId().equals(baseType.getId())) continue;
                boolean ok = t.onTrigger(level, pos, this.linkedPos, player);
                if (ok) {
                    int layerSize = getLayerSizeForType(t.getId());
                    instantManaCost += calculateLayerRequiredMana(t.getId(), layerSize, level);
                }
            }

            if (!this.additionalLayers.isEmpty() && player != null) {
                player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_layers_activated", this.additionalLayers.size()), true);
            }
        } else {
            // 1. Trigger instant non-transit layers (Crystallization, Summoning, etc.)
            for (MagicCircleType t : instantTypes) {
                boolean ok = t.onTrigger(level, pos, this.linkedPos, player);
                if (ok) {
                    anyTriggered = true;
                    int layerSize = getLayerSizeForType(t.getId());
                    instantManaCost += calculateLayerRequiredMana(t.getId(), layerSize, level);
                }
            }

            // 2. Trigger persistent layers (Barrier)
            for (MagicCircleType t : persistentTypes) {
                boolean ok = t.onTrigger(level, pos, this.linkedPos, player);
                if (ok) {
                    anyTriggered = true;
                }
            }

            // 3. Trigger transit layers last (Dimensional Gate, Teleportation)
            for (MagicCircleType t : transitTypes) {
                boolean ok = t.onTrigger(level, pos, this.linkedPos, player);
                if (ok) {
                    anyTriggered = true;
                    int layerSize = getLayerSizeForType(t.getId());
                    instantManaCost += calculateLayerRequiredMana(t.getId(), layerSize, level);
                }
            }
        }

        if (anyTriggered) {
            if (persistentTypes.isEmpty()) {
                this.currentMana = 0.0f;
            } else {
                this.currentMana = Math.max(0.0f, this.currentMana - instantManaCost);
            }
            this.triggerCooldown = 40;
            this.setChanged();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            return true;
        }

        return false;
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
                if (this.isDimensionalGate()) {
                    return targetBE.isDimensionalGate();
                }
                if (this.circleTypeId == null || !this.circleTypeId.equals(targetBE.getCircleTypeId())) {
                    return false;
                }
                return true;
            }
            return false;
        }

        if (level.isLoaded(this.linkedPos)) {
            if (level.getBlockEntity(this.linkedPos) instanceof MagicCircleBlockEntity targetBE) {
                if (this.isDimensionalGate()) {
                    return targetBE.isDimensionalGate();
                }
                if (this.circleTypeId == null || !this.circleTypeId.equals(targetBE.getCircleTypeId())) {
                    return false;
                }
                return true;
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
        if (type != null) {
            type.onServerTick((ServerLevel) level, pos, be);
        }

        // Also tick any additional layers!
        // If base is a barrier, all additional layers are activated ONLY after the barrier is active!
        // If base is a soul anchor, the additional layers are dedicated to the altar resonance (skip generic ticking)
        if ((!be.isBaseBarrier() || be.isBarrierActive()) && !be.hasSoulAnchorType()) {
            for (CircleLayer layer : be.getAdditionalLayers()) {
                if (layer.getCircleTypeId() != null) {
                    MagicCircleType layerType = MagicCircleRegistry.get(layer.getCircleTypeId());
                    if (layerType != null) {
                        layerType.onServerTick((ServerLevel) level, pos, be);
                    }
                }
            }
        }

        if (type == null) return;

        // Recalculate required mana across all layers
        if (be.linkedPos != null || be.circleTypeId != null || be.hasAdditionalLayers()) {
            be.requiredMana = be.calculateTotalRequiredMana(level);
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

        boolean isGate = be.isDimensionalGate();
        boolean isGateCandidate = be.hasDimensionalGateType();
        boolean hasTeleport = be.hasCircleType(TeleportCircleType.ID) && !isGate;
        boolean hasSummoning = be.hasCircleType(SummoningCircleType.ID);
        boolean hasCrystallization = be.hasCircleType(CrystallizationCircleType.ID);
        boolean hasCreation = be.hasCircleType(MagicCreationCircleType.ID);
        boolean canInfuse = true;
        if (isGate || hasTeleport) {
            if (be.linkedPos == null) canInfuse = false;
        }
        if (hasSummoning && !be.hasCapturedMob()) {
            canInfuse = false;
        }
        if (hasCrystallization && CrystallizationCircleType.findTargetItemEntity(level, pos, be.size) == null) {
            canInfuse = false;
        }
        if (hasCreation && CreationCircleRecipes.findRecipe(MagicCreationCircleType.findItems(level, pos, be.size), be.size) == null) {
            canInfuse = false;
        }

        for (Player player : players) {
            if (player.isShiftKeyDown()) {
                if (isGateCandidate && !isGate) {
                    if (level.getGameTime() % 40 == 0) {
                        if (be.size < 3) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_3x3"), true);
                        } else if (be.additionalLayers.isEmpty()) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers"), true);
                        } else if (!be.allLayersExpanded()) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_requires_layers_3x3"), true);
                        } else {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_only_self"), true);
                        }
                    }
                    continue;
                }

                if (!canInfuse) {
                    if (level.getGameTime() % 40 == 0) {
                        if (isGate && be.linkedPos == null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.dimensional_gate_unlinked"), true);
                        } else if (hasTeleport && be.linkedPos == null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.circle_not_linked"), true);
                        } else if (hasSummoning && !be.hasCapturedMob()) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.summon_requires_mob"), true);
                        } else if (hasCrystallization && CrystallizationCircleType.findTargetItemEntity(level, pos, be.size) == null) {
                            player.displayClientMessage(Component.translatable("message.mushokucraft.crystallization_requires_mineral"), true);
                        } else if (hasCreation) {
                            List<ItemEntity> items = MagicCreationCircleType.findItems(level, pos, be.size);
                            if (items.isEmpty()) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.creation_waiting_ingredients"), true);
                            } else {
                                CreationCircleRecipe closest = CreationCircleRecipes.findClosestRecipe(items);
                                if (closest != null) {
                                    player.displayClientMessage(Component.translatable("message.mushokucraft.creation_missing_ingredients",
                                            closest.getDisplayName(),
                                            CreationCircleRecipes.getMissingIngredientsDescription(closest, items)), true);
                                } else {
                                    player.displayClientMessage(Component.translatable("message.mushokucraft.creation_requires_ingredients"), true);
                                }
                            }
                        }
                    }
                    continue;
                }
                // If destination is not valid/broken for teleport or gate, reset link immediately!
                if ((hasTeleport || isGate) && !be.isDestinationValid(level)) {
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
                        for (CircleLayer layer : be.additionalLayers) {
                            if (layer.getCircleTypeId() != null) {
                                MagicCircleType lType = MagicCircleRegistry.get(layer.getCircleTypeId());
                                if (lType != null && !lType.getId().equals(type.getId())) {
                                    lType.onChannelTick((ServerLevel) level, pos, player, be.currentMana, be.requiredMana);
                                }
                            }
                        }

                        if (player instanceof ServerPlayer sp) {
                            ModGameEvents.syncMana(sp, masteryData);
                        }

                        if (level.getGameTime() % 4 == 0) {
                            if (be.barrierActive) {
                                player.displayClientMessage(Component.translatable("message.mushokucraft.barrier_status",
                                        String.format("%.0f", be.currentMana),
                                        String.format("%.0f", be.barrierHealth),
                                        String.format("%.0f", be.barrierMaxHealth)), true);
                            } else if (hasCreation) {
                                CreationCircleRecipe recipe = CreationCircleRecipes.findRecipe(MagicCreationCircleType.findItems(level, pos, be.size), be.size);
                                Component rName = recipe != null ? recipe.getDisplayName() : Component.literal("...");
                                int pct = (int) ((be.currentMana / Math.max(1.0f, be.requiredMana)) * 100);
                                player.displayClientMessage(Component.translatable("message.mushokucraft.creation_infusing",
                                        rName,
                                        String.format("%.0f", be.currentMana),
                                        String.format("%.0f", be.requiredMana),
                                        pct), true);
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
                    boolean success = be.triggerAllLayers((ServerLevel) level, pos, player);
                    if (!success) {
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

        // Additional Layers NBT
        ListTag layerList = new ListTag();
        for (CircleLayer layer : this.additionalLayers) {
            layerList.add(layer.save());
        }
        tag.put("AdditionalLayers", layerList);

        // Soul Anchor bound player NBT
        if (this.boundPlayerUUID != null) {
            tag.putUUID("BoundPlayerUUID", this.boundPlayerUUID);
        }
        if (this.boundPlayerName != null) {
            tag.putString("BoundPlayerName", this.boundPlayerName);
        }
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

        // Additional Layers NBT
        this.additionalLayers.clear();
        if (tag.contains("AdditionalLayers", 9)) {
            ListTag lList = tag.getList("AdditionalLayers", 10);
            for (int i = 0; i < lList.size(); i++) {
                this.additionalLayers.add(CircleLayer.load(lList.getCompound(i)));
            }
        }

        // Soul Anchor bound player NBT
        if (tag.hasUUID("BoundPlayerUUID")) {
            this.boundPlayerUUID = tag.getUUID("BoundPlayerUUID");
        } else {
            this.boundPlayerUUID = null;
        }
        if (tag.contains("BoundPlayerName")) {
            this.boundPlayerName = tag.getString("BoundPlayerName");
        } else {
            this.boundPlayerName = null;
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
