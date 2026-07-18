package com.mushokucraft.item;

import com.mushokucraft.config.MushokuConfig;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class HuntingKnifeItem extends Item {
    public HuntingKnifeItem(Properties properties) {
        super(properties.durability(25).attributes(createAttributes()));
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                        Item.BASE_ATTACK_DAMAGE_ID, 
                        0.5 - 1.0, // Total 0.5 damage (Player base is 1.0, so modifier is -0.5)
                        AttributeModifier.Operation.ADD_VALUE
                ), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                        Item.BASE_ATTACK_SPEED_ID, 
                        -2.4, 
                        AttributeModifier.Operation.ADD_VALUE
                ), EquipmentSlotGroup.MAINHAND)
                .build();
    }
}





