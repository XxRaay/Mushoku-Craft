package com.mushokucraft.magic.circle;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CreationCircleRecipe {
    private final ResourceLocation id;
    private final Supplier<ItemStack> outputSupplier;
    private final Map<Item, Integer> requiredIngredients;
    private final float requiredMana;
    private final int minCircleSize;
    private final Component displayName;

    public CreationCircleRecipe(ResourceLocation id, Supplier<ItemStack> outputSupplier, Map<Item, Integer> requiredIngredients, float requiredMana, int minCircleSize, Component displayName) {
        this.id = id;
        this.outputSupplier = outputSupplier;
        this.requiredIngredients = requiredIngredients;
        this.requiredMana = requiredMana;
        this.minCircleSize = minCircleSize;
        this.displayName = displayName;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public ItemStack createOutput() {
        return this.outputSupplier.get().copy();
    }

    public Map<Item, Integer> getRequiredIngredients() {
        return this.requiredIngredients;
    }

    public float getRequiredMana() {
        return this.requiredMana;
    }

    public int getMinCircleSize() {
        return this.minCircleSize;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public boolean matches(Map<Item, Integer> presentItems, int circleSize) {
        if (circleSize < this.minCircleSize) return false;

        for (Map.Entry<Item, Integer> req : this.requiredIngredients.entrySet()) {
            int available = presentItems.getOrDefault(req.getKey(), 0);
            if (available < req.getValue()) {
                return false;
            }
        }
        return true;
    }

    public void consume(List<ItemEntity> itemEntities) {
        Map<Item, Integer> toConsume = new HashMap<>(this.requiredIngredients);

        for (ItemEntity entity : itemEntities) {
            if (!entity.isAlive()) continue;
            ItemStack stack = entity.getItem();
            Item item = stack.getItem();

            if (toConsume.containsKey(item)) {
                int needed = toConsume.get(item);
                int inStack = stack.getCount();

                if (inStack <= needed) {
                    toConsume.put(item, needed - inStack);
                    entity.discard();
                } else {
                    stack.shrink(needed);
                    entity.setItem(stack);
                    toConsume.remove(item);
                }

                if (toConsume.getOrDefault(item, 0) <= 0) {
                    toConsume.remove(item);
                }

                if (toConsume.isEmpty()) {
                    break;
                }
            }
        }
    }
}
