package com.mushokucraft.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create("mushokucraft", Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<com.mushokucraft.block.entity.AirCushionBlockEntity>> AIR_CUSHION_BE = BLOCK_ENTITIES.register("air_cushion_be", () ->
            BlockEntityType.Builder.of(com.mushokucraft.block.entity.AirCushionBlockEntity::new, ModBlocks.AIR_CUSHION.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<com.mushokucraft.block.entity.MagicCircleBlockEntity>> MAGIC_CIRCLE_BE = BLOCK_ENTITIES.register("magic_circle_be", () ->
            BlockEntityType.Builder.of(com.mushokucraft.block.entity.MagicCircleBlockEntity::new, ModBlocks.MAGIC_CIRCLE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<com.mushokucraft.block.entity.BarrierWallBlockEntity>> BARRIER_WALL_BE = BLOCK_ENTITIES.register("barrier_wall_be", () ->
            BlockEntityType.Builder.of(com.mushokucraft.block.entity.BarrierWallBlockEntity::new, ModBlocks.BARRIER_WALL.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
