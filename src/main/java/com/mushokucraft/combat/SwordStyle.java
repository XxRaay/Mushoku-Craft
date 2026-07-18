package com.mushokucraft.combat;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.data.MasteryCalculator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Represents the three Sword God styles.
 * Each style encapsulates its unique mechanics and Touki aura attribute modifiers.
 */
public enum SwordStyle {
    SWORD_GOD("sword_god", 0xFF4444) {
        private static final ResourceLocation SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "sword_god_speed");
        private static final ResourceLocation REACH_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "sword_god_reach");
        private static final ResourceLocation TOUKI_DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "touki_damage");

        @Override
        public void applyPassiveModifiers(Player player) {
            var speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
            if (speedAttr != null && speedAttr.getModifier(SPEED_MOD_ID) == null) {
                speedAttr.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, MushokuConfig.SWORD_GOD_SPEED_BONUS.get(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            var reachAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (reachAttr != null && reachAttr.getModifier(REACH_MOD_ID) == null) {
                reachAttr.addTransientModifier(new AttributeModifier(REACH_MOD_ID, MushokuConfig.SWORD_GOD_REACH_BONUS.get(), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removePassiveModifiers(Player player) {
            var speedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
            if (speedAttr != null) speedAttr.removeModifier(SPEED_MOD_ID);
            var reachAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (reachAttr != null) reachAttr.removeModifier(REACH_MOD_ID);
        }

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null && damageAttr.getModifier(TOUKI_DAMAGE_MOD_ID) == null) {
                damageAttr.addTransientModifier(new AttributeModifier(TOUKI_DAMAGE_MOD_ID, MasteryCalculator.calculateToukiDamage(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) damageAttr.removeModifier(TOUKI_DAMAGE_MOD_ID);
        }
    },
    
    WATER_GOD("water_god", 0x4488FF) {
        private static final ResourceLocation TOUKI_KB_RES_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "touki_knockback_res");

        @Override
        public void applyPassiveModifiers(Player player) {}
        @Override
        public void removePassiveModifiers(Player player) {}

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            var kbResAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kbResAttr != null && kbResAttr.getModifier(TOUKI_KB_RES_MOD_ID) == null) {
                kbResAttr.addTransientModifier(new AttributeModifier(TOUKI_KB_RES_MOD_ID, MasteryCalculator.calculateToukiKnockbackResistance(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            var kbResAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kbResAttr != null) kbResAttr.removeModifier(TOUKI_KB_RES_MOD_ID);
        }
    },
    
    NORTH_GOD("north_god", 0xAA44FF) {
        private static final ResourceLocation TOUKI_SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "touki_speed");
        private static final ResourceLocation TOUKI_JUMP_MOD_ID = ResourceLocation.fromNamespaceAndPath("mushokucraft", "touki_jump");

        @Override
        public void applyPassiveModifiers(Player player) {}
        @Override
        public void removePassiveModifiers(Player player) {}

        @Override
        public void applyToukiModifiers(Player player, float mastery) {
            var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeedAttr != null && moveSpeedAttr.getModifier(TOUKI_SPEED_MOD_ID) == null) {
                moveSpeedAttr.addTransientModifier(new AttributeModifier(TOUKI_SPEED_MOD_ID, MasteryCalculator.calculateToukiSpeed(mastery), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            var jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
            if (jumpAttr != null && jumpAttr.getModifier(TOUKI_JUMP_MOD_ID) == null) {
                jumpAttr.addTransientModifier(new AttributeModifier(TOUKI_JUMP_MOD_ID, MasteryCalculator.calculateToukiJump(mastery), AttributeModifier.Operation.ADD_VALUE));
            }
        }

        @Override
        public void removeToukiModifiers(Player player) {
            var moveSpeedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (moveSpeedAttr != null) moveSpeedAttr.removeModifier(TOUKI_SPEED_MOD_ID);
            var jumpAttr = player.getAttribute(Attributes.JUMP_STRENGTH);
            if (jumpAttr != null) jumpAttr.removeModifier(TOUKI_JUMP_MOD_ID);
        }
    };

    private final String id;
    private final int color;

    SwordStyle(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String getId() { return id; }
    public int getColor() { return color; }

    public abstract void applyPassiveModifiers(Player player);
    public abstract void removePassiveModifiers(Player player);
    public abstract void applyToukiModifiers(Player player, float mastery);
    public abstract void removeToukiModifiers(Player player);
}





