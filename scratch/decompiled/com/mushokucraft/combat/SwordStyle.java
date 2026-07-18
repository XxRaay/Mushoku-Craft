/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 */
package com.mushokucraft.combat;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public enum SwordStyle {
    SWORD_GOD("sword_god", 0xFF4444){
        private static final ResourceLocation SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sword_god_speed");
        private static final ResourceLocation REACH_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"sword_god_reach");
        private static final ResourceLocation TOUKI_DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"touki_damage");

        @Override
        public void applyPassiveModifiers(Player player) {
            AttributeInstance reachAttr;
            AttributeInstance speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
            if (speedAttr != null && speedAttr.getModifier(SPEED_MOD_ID) == null) {
                speedAttr.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, ((Double)MushokuConfig.SWORD_GOD_SPEED_BONUS.get()).doubleValue(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            if ((reachAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)) != null && reachAttr.getModifier(REACH_MOD_ID) == null) {
                reachAttr.addTransientModifier(new AttributeModifier(REACH_MOD_ID, ((Double)MushokuConfig.SWORD_GOD_REACH_BONUS.get()).doubleValue(), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removePassiveModifiers(Player player) {
            AttributeInstance reachAttr;
            AttributeInstance speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_MOD_ID);
            }
            if ((reachAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)) != null) {
                reachAttr.removeModifier(REACH_MOD_ID);
            }
        }

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null && damageAttr.getModifier(TOUKI_DAMAGE_MOD_ID) == null) {
                damageAttr.addTransientModifier(new AttributeModifier(TOUKI_DAMAGE_MOD_ID, MasteryCalculator.calculateToukiDamage(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.removeModifier(TOUKI_DAMAGE_MOD_ID);
            }
        }
    }
    ,
    WATER_GOD("water_god", 0x4488FF){
        private static final ResourceLocation TOUKI_KB_RES_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"touki_knockback_res");

        @Override
        public void applyPassiveModifiers(Player player) {
        }

        @Override
        public void removePassiveModifiers(Player player) {
        }

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            AttributeInstance kbResAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kbResAttr != null && kbResAttr.getModifier(TOUKI_KB_RES_MOD_ID) == null) {
                kbResAttr.addTransientModifier(new AttributeModifier(TOUKI_KB_RES_MOD_ID, MasteryCalculator.calculateToukiKnockbackResistance(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            AttributeInstance kbResAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kbResAttr != null) {
                kbResAttr.removeModifier(TOUKI_KB_RES_MOD_ID);
            }
        }
    }
    ,
    NORTH_GOD("north_god", 0xAA44FF){
        private static final ResourceLocation TOUKI_SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"touki_speed");
        private static final ResourceLocation TOUKI_JUMP_MOD_ID = ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"touki_jump");

        @Override
        public void applyPassiveModifiers(Player player) {
        }

        @Override
        public void removePassiveModifiers(Player player) {
        }

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            AttributeInstance jumpAttr;
            AttributeInstance moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeedAttr != null && moveSpeedAttr.getModifier(TOUKI_SPEED_MOD_ID) == null) {
                moveSpeedAttr.addTransientModifier(new AttributeModifier(TOUKI_SPEED_MOD_ID, MasteryCalculator.calculateToukiSpeed(mastery), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            if ((jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH)) != null && jumpAttr.getModifier(TOUKI_JUMP_MOD_ID) == null) {
                jumpAttr.addTransientModifier(new AttributeModifier(TOUKI_JUMP_MOD_ID, MasteryCalculator.calculateToukiJump(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            AttributeInstance jumpAttr;
            AttributeInstance moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeedAttr != null) {
                moveSpeedAttr.removeModifier(TOUKI_SPEED_MOD_ID);
            }
            if ((jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH)) != null) {
                jumpAttr.removeModifier(TOUKI_JUMP_MOD_ID);
            }
        }
    };

    private final String id;
    private final int color;

    private SwordStyle(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return this.id;
    }

    public int getColor() {
        return this.color;
    }

    public abstract void applyPassiveModifiers(Player var1);

    public abstract void removePassiveModifiers(Player var1);

    public abstract void applyToukiModifiers(Player var1, float var2);

    public abstract void removeToukiModifiers(Player var1);
}

