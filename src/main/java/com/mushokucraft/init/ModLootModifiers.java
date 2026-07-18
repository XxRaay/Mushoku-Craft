package com.mushokucraft.init;

import com.mojang.serialization.MapCodec;
import com.mushokucraft.loot.AddItemModifier;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create((Registry)NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"mushokucraft");
    public static final Supplier<MapCodec<AddItemModifier>> ADD_ITEM = LOOT_MODIFIER_SERIALIZERS.register("add_item", () -> AddItemModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}


