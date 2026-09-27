package com.mushokucraft.neoforge.curios;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mushokucraft.accessory.AccessoryItem;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Map;

public class NeoForgeCurioItemWrapper implements ICurioItem {

    private final AccessoryItem accessory;

    public NeoForgeCurioItemWrapper(AccessoryItem accessory) {
        this.accessory = accessory;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        accessory.handleCurioTick(slotContext.entity());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        accessory.handleEquip(slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        accessory.handleUnequip(slotContext.entity());
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = ArrayListMultimap.create();
        for (Map.Entry<Holder<Attribute>, Double> entry : accessory.getAttributeModifiersMap().entrySet()) {
            ResourceLocation modId = ResourceLocation.fromNamespaceAndPath("mushokucraft", "curio_" + id.getPath() + "_" + entry.getKey().getRegisteredName().replace(":", "_"));
            AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_VALUE;
            if (entry.getKey().equals(Attributes.MOVEMENT_SPEED) || entry.getKey().equals(Attributes.ATTACK_SPEED)) {
                operation = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            }
            AttributeModifier modifier = new AttributeModifier(modId, entry.getValue(), operation);
            modifiers.put(entry.getKey(), modifier);
        }
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        return accessory.buildTooltip(tooltips, context, stack);
    }
}
