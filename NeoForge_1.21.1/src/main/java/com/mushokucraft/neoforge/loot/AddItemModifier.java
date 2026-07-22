package com.mushokucraft.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemModifier
extends LootModifier {
    public static final MapCodec<AddItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> 
        codecStart(inst).and(
            ItemStack.CODEC.fieldOf("item").forGetter((AddItemModifier m) -> m.item)
        ).apply(inst, AddItemModifier::new)
    );
    private final ItemStack item;

    public AddItemModifier(LootItemCondition[] conditionsIn, ItemStack item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(this.item.copy());
        return generatedLoot;
    }

    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

