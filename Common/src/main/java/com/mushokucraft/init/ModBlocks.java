package com.mushokucraft.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create("mushokucraft", Registries.BLOCK);

    public static final RegistrySupplier<Block> AIR_CUSHION = BLOCKS.register("air_cushion", () ->
            new com.mushokucraft.block.AirCushionBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .noLootTable()
                    .destroyTime(-1.0f) // unbreakable
            ));

    public static final RegistrySupplier<Block> MODULAR_ANVIL = BLOCKS.register("modular_anvil", () ->
            new com.mushokucraft.block.ModularAnvilBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.ANVIL))
    );

    public static void register() {
        BLOCKS.register();
    }
}


