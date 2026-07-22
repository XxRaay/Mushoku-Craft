package com.mushokucraft.init;

import com.mushokucraft.MushokuCraftCommon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> MAGIC = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "magic")
    );
    
    public static final ResourceKey<DamageType> LONGSWORD_SILENCE = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MushokuCraftCommon.MOD_ID, "longsword_silence")
    );
}





