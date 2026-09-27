package com.mushokucraft.accessory;

import com.mushokucraft.magic.MagicSchool;
import dev.architectury.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessoryItem extends Item {

    private final AccessoryType accessoryType;
    private final float maxManaBonus;
    private final float manaRegenBonus;
    private final Map<MagicSchool, Float> schoolDamageBonuses;
    private final float allSchoolDamageBonus;
    private final float manaCostDiscount;
    private final float castTimeReduction;
    private final boolean fizzleImmune;
    private final float toukiDrainDiscount;
    private final String passiveId;
    private final Map<Holder<Attribute>, Double> attributeModifiers;

    public AccessoryItem(Properties properties, Builder builder) {
        super(properties);
        this.accessoryType = builder.accessoryType;
        this.maxManaBonus = builder.maxManaBonus;
        this.manaRegenBonus = builder.manaRegenBonus;
        this.schoolDamageBonuses = builder.schoolDamageBonuses;
        this.allSchoolDamageBonus = builder.allSchoolDamageBonus;
        this.manaCostDiscount = builder.manaCostDiscount;
        this.castTimeReduction = builder.castTimeReduction;
        this.fizzleImmune = builder.fizzleImmune;
        this.toukiDrainDiscount = builder.toukiDrainDiscount;
        this.passiveId = builder.passiveId;
        this.attributeModifiers = builder.attributeModifiers;
    }

    public AccessoryType getAccessoryType() {
        return this.accessoryType;
    }

    public float getMaxManaBonus() {
        return this.maxManaBonus;
    }

    public float getManaRegenBonus() {
        return this.manaRegenBonus;
    }

    public float getSchoolDamageBonus(MagicSchool school) {
        if (school == null) return 0f;
        return this.schoolDamageBonuses.getOrDefault(school, 0f);
    }

    public Map<MagicSchool, Float> getSchoolDamageBonuses() {
        return Collections.unmodifiableMap(this.schoolDamageBonuses);
    }

    public float getAllSchoolDamageBonus() {
        return this.allSchoolDamageBonus;
    }

    public float getManaCostDiscount() {
        return this.manaCostDiscount;
    }

    public float getCastTimeReduction() {
        return this.castTimeReduction;
    }

    public boolean isFizzleImmune() {
        return this.fizzleImmune;
    }

    public float getToukiDrainDiscount() {
        return this.toukiDrainDiscount;
    }

    public String getPassiveId() {
        return this.passiveId;
    }

    public Map<Holder<Attribute>, Double> getAttributeModifiersMap() {
        return Collections.unmodifiableMap(this.attributeModifiers);
    }

    public void handleCurioTick(LivingEntity wearer) {
        if (wearer == null || wearer.level().isClientSide) return;

        if ("tempest_vortex".equals(this.passiveId)) {
            // Speed II and water breathing in water or rain
            if (wearer.isInWaterOrRain() || wearer.isInWater()) {
                wearer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, true, false, true));
                wearer.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false, true));
            }
        } else if ("titan_bastion".equals(this.passiveId)) {
            // Resistance I when standing still
            if (wearer.getDeltaMovement().lengthSqr() < 0.002) {
                wearer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false, true));
            }
        } else if ("laplace_sight".equals(this.passiveId)) {
            // Highlights hostile monsters within 24 blocks
            if (wearer.tickCount % 20 == 0) {
                AABB box = wearer.getBoundingBox().inflate(24.0);
                List<Monster> monsters = wearer.level().getEntitiesOfClass(Monster.class, box, LivingEntity::isAlive);
                for (Monster m : monsters) {
                    m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false, false));
                }
            }
        } else if ("volcanic_burn".equals(this.passiveId)) {
            // Passive fire resistance
            wearer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
        }
    }

    public void handleEquip(LivingEntity entity) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            com.mushokucraft.data.PlayerMasteryData data = com.mushokucraft.data.PlayerMasteryProvider.get(player);
            if (data != null) {
                float effectiveMax = data.getMaxMana() + AccessoryHelper.getMaxManaBonus(player);
                data.setLastSyncedEffectiveMax(effectiveMax);
                if (data.getMana() > effectiveMax) {
                    data.setMana(effectiveMax);
                }
                com.mushokucraft.event.ModGameEvents.syncMana(player, data);
            }
        }
    }

    public void handleUnequip(LivingEntity entity) {
        if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
            com.mushokucraft.data.PlayerMasteryData data = com.mushokucraft.data.PlayerMasteryProvider.get(player);
            if (data != null) {
                float effectiveMax = data.getMaxMana() + AccessoryHelper.getMaxManaBonus(player);
                data.setLastSyncedEffectiveMax(effectiveMax);
                if (data.getMana() > effectiveMax) {
                    data.setMana(effectiveMax);
                }
                com.mushokucraft.event.ModGameEvents.syncMana(player, data);
            }
        }
    }

    public List<Component> buildTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        List<Component> result = new ArrayList<>(tooltips);

        // 1. Header: if Curios did not add the header (because vanilla attributes were empty), add it now
        if (result.isEmpty()) {
            result.add(Component.empty());
            result.add(Component.translatable("curios.modifiers." + this.accessoryType.getId()).withStyle(net.minecraft.ChatFormatting.GOLD));
        }

        // 2. Direct vanilla attribute modifiers fallback (in case player is null and Curios skipped applyTextFor)
        if (result.size() <= 2 && !this.attributeModifiers.isEmpty()) {
            for (Map.Entry<Holder<Attribute>, Double> entry : this.attributeModifiers.entrySet()) {
                if (entry.getKey().equals(Attributes.ATTACK_DAMAGE)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.attack_damage", String.format("+%.1f", entry.getValue())).withStyle(net.minecraft.ChatFormatting.BLUE));
                } else if (entry.getKey().equals(Attributes.ARMOR)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.armor", String.format("+%.0f", entry.getValue())).withStyle(net.minecraft.ChatFormatting.BLUE));
                } else if (entry.getKey().equals(Attributes.ARMOR_TOUGHNESS)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.armor_toughness", String.format("+%.0f", entry.getValue())).withStyle(net.minecraft.ChatFormatting.BLUE));
                } else if (entry.getKey().equals(Attributes.MOVEMENT_SPEED)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.movement_speed", String.format("+%.0f%%", entry.getValue() * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
                } else if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.knockback_res", String.format("+%.0f%%", entry.getValue() * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
                } else if (entry.getKey().equals(Attributes.MAX_HEALTH)) {
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.max_health", String.format("+%.0f", entry.getValue())).withStyle(net.minecraft.ChatFormatting.BLUE));
                }
            }
        }

        // 3. Custom Magic & RPG Modifiers (all in ChatFormatting.BLUE matching standard Curios/Minecraft attributes)
        if (this.maxManaBonus > 0) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.max_mana", String.format("+%.0f", this.maxManaBonus)).withStyle(net.minecraft.ChatFormatting.BLUE));
        }
        if (this.manaRegenBonus > 0) {
            String formattedRegen = String.format("+%.1f", this.manaRegenBonus).replace('.', ',');
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.mana_regen", formattedRegen).withStyle(net.minecraft.ChatFormatting.BLUE));
        }
        if (this.allSchoolDamageBonus > 0) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.all_school_damage", String.format("+%.0f%%", this.allSchoolDamageBonus * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
        } else {
            for (Map.Entry<MagicSchool, Float> entry : this.schoolDamageBonuses.entrySet()) {
                if (entry.getValue() > 0) {
                    Component schoolName = Component.translatable("tooltip.mushokucraft.accessory.school_name." + entry.getKey().getId());
                    result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.school_damage", String.format("+%.0f%%", entry.getValue() * 100), schoolName).withStyle(net.minecraft.ChatFormatting.BLUE));
                }
            }
        }
        if (this.manaCostDiscount > 0) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.mana_discount", String.format("-%.0f%%", this.manaCostDiscount * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
        }
        if (this.castTimeReduction > 0) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.cast_reduction", String.format("-%.0f%%", this.castTimeReduction * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
        }
        if (this.fizzleImmune) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.fizzle_immunity").withStyle(net.minecraft.ChatFormatting.BLUE));
        }
        if (this.toukiDrainDiscount > 0) {
            result.add(Component.translatable("tooltip.mushokucraft.accessory.mod.touki_discount", String.format("-%.0f%%", this.toukiDrainDiscount * 100)).withStyle(net.minecraft.ChatFormatting.BLUE));
        }

        // 4. Passive ability description
        if (this.passiveId != null && !this.passiveId.isEmpty()) {
            result.add(Component.empty());
            result.add(Component.translatable("tooltip.mushokucraft.accessory.passive." + this.passiveId + ".title"));
            result.add(Component.translatable("tooltip.mushokucraft.accessory.passive." + this.passiveId + ".desc"));
        }

        // 5. Lore description
        String loreKey = "item.mushokucraft." + stack.getItem().getDescriptionId().substring(stack.getItem().getDescriptionId().lastIndexOf('.') + 1) + ".lore";
        result.add(Component.empty());
        result.add(Component.translatable(loreKey));

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (!Platform.isModLoaded("curios")) {
            tooltip.addAll(buildTooltip(List.of(), context, stack));
        }
    }

    public static class Builder {
        private final AccessoryType accessoryType;
        private float maxManaBonus = 0f;
        private float manaRegenBonus = 0f;
        private final Map<MagicSchool, Float> schoolDamageBonuses = new EnumMap<>(MagicSchool.class);
        private float allSchoolDamageBonus = 0f;
        private float manaCostDiscount = 0f;
        private float castTimeReduction = 0f;
        private boolean fizzleImmune = false;
        private float toukiDrainDiscount = 0f;
        private String passiveId = null;
        private final Map<Holder<Attribute>, Double> attributeModifiers = new HashMap<>();

        public Builder(AccessoryType accessoryType) {
            this.accessoryType = accessoryType;
        }

        public Builder maxMana(float amount) {
            this.maxManaBonus = amount;
            return this;
        }

        public Builder manaRegen(float amount) {
            this.manaRegenBonus = amount;
            return this;
        }

        public Builder schoolDamage(MagicSchool school, float bonus) {
            this.schoolDamageBonuses.put(school, bonus);
            return this;
        }

        public Builder allSchoolDamage(float bonus) {
            this.allSchoolDamageBonus = bonus;
            return this;
        }

        public Builder manaDiscount(float discount) {
            this.manaCostDiscount = discount;
            return this;
        }

        public Builder castReduction(float reduction) {
            this.castTimeReduction = reduction;
            return this;
        }

        public Builder fizzleImmune() {
            this.fizzleImmune = true;
            return this;
        }

        public Builder toukiDiscount(float discount) {
            this.toukiDrainDiscount = discount;
            return this;
        }

        public Builder passive(String id) {
            this.passiveId = id;
            return this;
        }

        public Builder attribute(Holder<Attribute> attribute, double value) {
            this.attributeModifiers.put(attribute, value);
            return this;
        }
    }
}
