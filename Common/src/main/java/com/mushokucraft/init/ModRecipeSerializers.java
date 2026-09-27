package com.mushokucraft.init;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.crafting.ManuscriptCloningRecipe;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MushokuCraftCommon.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final Supplier<RecipeSerializer<ManuscriptCloningRecipe>> MANUSCRIPT_CLONING = RECIPE_SERIALIZERS.register("manuscript_cloning",
            () -> new SimpleCraftingRecipeSerializer<>(ManuscriptCloningRecipe::new));

    public static final Supplier<RecipeSerializer<com.mushokucraft.crafting.ManuscriptExpandRecipe>> MANUSCRIPT_EXPAND = RECIPE_SERIALIZERS.register("manuscript_expand",
            () -> new SimpleCraftingRecipeSerializer<>(com.mushokucraft.crafting.ManuscriptExpandRecipe::new));

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}
