package com.mushokucraft.fabric.trinkets;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mushokucraft.accessory.AccessoryItem;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class FabricTrinketItemWrapper implements Trinket {

    private final AccessoryItem accessory;

    public FabricTrinketItemWrapper(AccessoryItem accessory) {
        this.accessory = accessory;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        accessory.handleCurioTick(entity);
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        accessory.handleEquip(entity);
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        accessory.handleUnequip(entity);
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (slot == null || slot.inventory() == null || slot.inventory().getSlotType() == null) {
            return false;
        }
        String name = slot.inventory().getSlotType().getName();
        com.mushokucraft.accessory.AccessoryType type = accessory.getAccessoryType();
        return switch (type) {
            case RING -> "ring".equals(name);
            case NECKLACE -> "necklace".equals(name);
            case BELT -> "belt".equals(name);
            case CHARM -> "charm".equals(name);
            case HANDS -> "glove".equals(name) || "hands".equals(name);
            case BODY -> "body".equals(name) || "cape".equals(name) || "back".equals(name);
        };
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation slotIdentifier) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = ArrayListMultimap.create();
        for (Map.Entry<Holder<Attribute>, Double> entry : accessory.getAttributeModifiersMap().entrySet()) {
            ResourceLocation modId = ResourceLocation.fromNamespaceAndPath("mushokucraft", "trinket_" + slotIdentifier.getPath().replace("/", "_") + "_" + entry.getKey().getRegisteredName().replace(":", "_"));
            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_VALUE;
            if (entry.getKey().equals(Attributes.MOVEMENT_SPEED) || entry.getKey().equals(Attributes.ATTACK_SPEED)) {
                operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            }
            AttributeModifier modifier = new AttributeModifier(modId, entry.getValue(), operation);
            modifiers.put(entry.getKey(), modifier);
        }
        return modifiers;
    }
}
