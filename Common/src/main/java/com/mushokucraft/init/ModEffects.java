package com.mushokucraft.init;

import com.mushokucraft.effect.BleedingEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create("mushokucraft", Registries.MOB_EFFECT);
    public static final RegistrySupplier<MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding", () -> new BleedingEffect(MobEffectCategory.HARMFUL, 0x8B0000));

    public static void register() {
        MOB_EFFECTS.register();
    }
}


