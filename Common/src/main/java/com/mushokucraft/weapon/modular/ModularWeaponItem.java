package com.mushokucraft.weapon.modular;

import com.mushokucraft.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ModularWeaponItem extends SwordItem {
    private final WeaponForm form;

    public ModularWeaponItem(WeaponForm form, Properties properties) {
        super(Tiers.IRON, properties.attributes(createDefaultAttributes(form)));
        this.form = form;
    }

    public WeaponForm getForm() {
        return form;
    }

    private static ItemAttributeModifiers createDefaultAttributes(WeaponForm form) {
        return createAttributes(form.getBaseDamage() + 2.0f, form.getBaseSpeed(), form.getReachBonus());
    }

    public static ItemAttributeModifiers createAttributes(float damage, float speed, float reach) {
        var builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                        Item.BASE_ATTACK_DAMAGE_ID,
                        (double) damage - 1.0,
                        AttributeModifier.Operation.ADD_VALUE
                ), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                        Item.BASE_ATTACK_SPEED_ID,
                        (double) speed,
                        AttributeModifier.Operation.ADD_VALUE
                ), EquipmentSlotGroup.MAINHAND);

        if (reach != 0.0f) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("mushokucraft", "modular_reach"),
                    (double) reach,
                    AttributeModifier.Operation.ADD_VALUE
            ), EquipmentSlotGroup.MAINHAND);
        }

        return builder.build();
    }

    public static WeaponForm getForm(ItemStack stack) {
        if (stack.getItem() instanceof ModularWeaponItem mwi) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            var tag = customData.copyTag();
            if (tag.contains("Form")) {
                return WeaponForm.byId(tag.getString("Form"));
            }
            return mwi.form;
        }
        return WeaponForm.SWORD;
    }

    public static WeaponMaterial getBladeMaterial(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return WeaponMaterialRegistry.get(tag.getString("BladeMaterial"));
    }

    public static WeaponMaterial getGuardMaterial(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return WeaponMaterialRegistry.get(tag.getString("GuardMaterial"));
    }

    public static WeaponMaterial getHandleMaterial(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return WeaponMaterialRegistry.get(tag.getString("HandleMaterial"));
    }

    public static WeaponCoreType getCore(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        if (tag.contains("Core")) {
            return WeaponCoreRegistry.get(tag.getString("Core"));
        }
        return null;
    }

    public static String getCrafter(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return tag.contains("Crafter") ? tag.getString("Crafter") : "Неизвестный кузнец";
    }

    public static float getQuality(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        return tag.contains("Quality") ? tag.getFloat("Quality") : 1.0f;
    }

    public static WeaponQuality getQualityTier(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();
        if (tag.contains("QualityTier")) {
            return WeaponQuality.byId(tag.getString("QualityTier"));
        }
        if (tag.contains("Quality")) {
            return WeaponQuality.fromMultiplier(tag.getFloat("Quality"));
        }
        return WeaponQuality.STANDARD;
    }

    public static float calculateDamage(WeaponForm form, WeaponMaterial blade, WeaponMaterial guard, float quality) {
        float raw = form.getBaseDamage() + (blade != null ? blade.damageBonus() : 2.0f) + (guard != null ? guard.damageBonus() * 0.2f : 0.0f);
        return raw * quality;
    }

    public static float calculateDamage(WeaponForm form, WeaponMaterial blade, WeaponMaterial guard, WeaponQuality quality) {
        return calculateDamage(form, blade, guard, quality != null ? quality.getDamageMultiplier() : 1.0f);
    }

    public static float getWeaponDamage(ItemStack stack) {
        return calculateDamage(getForm(stack), getBladeMaterial(stack), getGuardMaterial(stack), getQuality(stack));
    }

    public static float calculateSpeed(WeaponForm form, WeaponMaterial handle, WeaponMaterial blade) {
        return calculateSpeed(form, handle, blade, WeaponQuality.STANDARD);
    }

    public static float calculateSpeed(WeaponForm form, WeaponMaterial handle, WeaponMaterial blade, WeaponQuality quality) {
        float speed = form.getBaseSpeed();
        if (handle != null) speed += handle.speedModifier();
        if (blade != null) speed += blade.speedModifier() * 0.5f;
        if (quality != null) speed += quality.getSpeedBonus();
        return Math.clamp(speed, -3.6f, -0.4f);
    }

    public static float calculateReach(WeaponForm form, WeaponMaterial blade) {
        return calculateReach(form, blade, WeaponQuality.STANDARD);
    }

    public static float calculateReach(WeaponForm form, WeaponMaterial blade, WeaponQuality quality) {
        float reach = form.getReachBonus();
        if (blade != null) reach += blade.reachBonus();
        if (quality != null) reach += quality.getReachBonus();
        return reach;
    }

    public static int calculateDurability(WeaponForm form, WeaponMaterial blade, WeaponMaterial guard, WeaponMaterial handle, float quality) {
        return calculateDurability(form, blade, guard, handle, WeaponQuality.fromMultiplier(quality));
    }

    public static int calculateDurability(WeaponForm form, WeaponMaterial blade, WeaponMaterial guard, WeaponMaterial handle, WeaponQuality quality) {
        float base = 0;
        if (blade != null) base += blade.durability() * 1.5f;
        if (guard != null) base += guard.durability() * 0.5f;
        if (handle != null) base += handle.durability() * 0.3f;
        float mult = quality != null ? (1.0f + quality.getDurabilityBonus()) : 1.0f;
        return (int) Math.max(40, base * form.getDurabilityMultiplier() * mult);
    }

    public static Item getItemForForm(WeaponForm form) {
        return switch (form) {
            case GREATSWORD -> ModItems.MODULAR_GREATSWORD.get();
            case SCYTHE -> ModItems.MODULAR_SCYTHE.get();
            case DAGGER -> ModItems.MODULAR_DAGGER.get();
            case KATANA -> ModItems.MODULAR_KATANA.get();
            case RAPIER -> ModItems.MODULAR_RAPIER.get();
            case SWORD -> ModItems.MODULAR_SWORD.get();
        };
    }

    public static ItemStack createWeapon(
            WeaponForm form,
            WeaponMaterial blade,
            WeaponMaterial guard,
            WeaponMaterial handle,
            WeaponCoreType core,
            String crafter,
            float quality,
            String customName
    ) {
        return createWeapon(form, blade, guard, handle, core, crafter, WeaponQuality.fromMultiplier(quality), quality, customName);
    }

    public static ItemStack createWeapon(
            WeaponForm form,
            WeaponMaterial blade,
            WeaponMaterial guard,
            WeaponMaterial handle,
            WeaponCoreType core,
            String crafter,
            WeaponQuality quality,
            float qualityScore,
            String customName
    ) {
        Item item = getItemForForm(form);
        ItemStack stack = new ItemStack(item);

        float damage = calculateDamage(form, blade, guard, quality);
        float speed = calculateSpeed(form, handle, blade, quality);
        float reach = calculateReach(form, blade, quality);
        int durability = calculateDurability(form, blade, guard, handle, quality);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("Form", form.getId());
            tag.putString("BladeMaterial", blade != null ? blade.id() : "iron");
            tag.putString("GuardMaterial", guard != null ? guard.id() : "iron");
            tag.putString("HandleMaterial", handle != null ? handle.id() : "oak");
            if (core != null) {
                tag.putString("Core", core.getId());
            }
            tag.putString("Crafter", crafter != null && !crafter.isBlank() ? crafter : "Кузнец");
            tag.putString("QualityTier", quality.getId());
            tag.putFloat("QualityScore", qualityScore);
            tag.putFloat("Quality", quality.getDamageMultiplier());
            if (customName != null && !customName.isBlank()) {
                tag.putString("ArtifactName", customName);
            }
        });

        stack.set(DataComponents.MAX_DAMAGE, durability);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(damage, speed, reach));

        if (quality == WeaponQuality.MASTERPIECE) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if (customName != null && !customName.isBlank()) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(customName).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }

        return stack;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        WeaponForm weaponForm = getForm(stack);
        WeaponCoreType core = getCore(stack);

        // 1. Form Special Abilities
        if (!attacker.level().isClientSide()) {
            switch (weaponForm) {
                case SCYTHE -> {
                    // Cleaving sweep hitting nearby enemies in 3.5 block radius
                    AABB cleaveBox = target.getBoundingBox().inflate(2.5, 1.0, 2.5);
                    List<LivingEntity> nearby = attacker.level().getEntitiesOfClass(LivingEntity.class, cleaveBox,
                            e -> e != attacker && e != target && e.isAlive() && !attacker.isAlliedTo(e));

                    float cleaveDmg = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.65f;
                    for (LivingEntity nearbyEntity : nearby) {
                        nearbyEntity.hurt(target.damageSources().mobAttack(attacker), cleaveDmg);
                    }

                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.5, target.getZ(), 3, 0.5, 0.2, 0.5, 0.0);
                    }
                    attacker.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
                }
                case GREATSWORD -> {
                    // Staggering heavy blow: extra knockback + Slowness
                    target.knockback(0.8, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));

                    // Jump crit bonus
                    if (attacker.fallDistance > 0.0f) {
                        target.hurt(target.damageSources().mobAttack(attacker), 4.0f);
                        attacker.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 1.6f);
                    }
                }
                case DAGGER -> {
                    // Backstab: if attacker is facing roughly the same direction as the target
                    Vec3 targetLook = target.getLookAngle().normalize();
                    Vec3 attackerLook = attacker.getLookAngle().normalize();
                    if (targetLook.dot(attackerLook) > 0.5) {
                        float backstabBonus = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f;
                        target.hurt(target.damageSources().mobAttack(attacker), backstabBonus);
                        attacker.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.2f, 1.4f);
                        if (attacker.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 15, 0.2, 0.2, 0.2, 0.2);
                        }
                    }
                }
                case KATANA -> {
                    // Bleed laceration
                    if (attacker.getRandom().nextFloat() < 0.40f) {
                        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0, false, false, true));
                        attacker.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 1.3f);
                    }
                }
                case RAPIER -> {
                    // Armor Piercing: Extra true damage based on target armor
                    int armor = target.getArmorValue();
                    if (armor > 0) {
                        float pierce = Math.min(6.0f, armor * 0.25f);
                        target.hurt(target.damageSources().magic(), pierce);
                    }
                }
                case SWORD -> {
                    // Balanced stance: small parry window boost or stamina recovery
                }
            }

            // 2. Beast Core Special Effect
            if (core != null) {
                float damageDealt = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                core.onHit(stack, target, attacker, damageDealt);
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = customData.copyTag();

        WeaponForm weaponForm = getForm(stack);
        WeaponMaterial blade = getBladeMaterial(stack);
        WeaponMaterial guard = getGuardMaterial(stack);
        WeaponMaterial handle = getHandleMaterial(stack);
        WeaponCoreType core = getCore(stack);
        String crafter = getCrafter(stack);
        float quality = getQuality(stack);

        // Header: Crafter & Quality Tier
        tooltip.add(Component.translatable("tooltip.mushokucraft.modular_weapon.crafter", crafter).withStyle(ChatFormatting.GOLD));

        WeaponQuality qualityTier = getQualityTier(stack);
        tooltip.add(qualityTier.getTitleComponent());
        tooltip.add(Component.literal("  ").append(qualityTier.getDescComponent()));

        tooltip.add(Component.literal("---------------------------").withStyle(ChatFormatting.DARK_GRAY));

        // Form & Form Perk
        tooltip.add(Component.translatable("tooltip.mushokucraft.modular_weapon.form")
                .append(Component.literal(": "))
                .append(weaponForm.getDisplayName().copy().withStyle(ChatFormatting.WHITE)));
        tooltip.add(Component.literal("  ✦ ").withStyle(ChatFormatting.AQUA)
                .append(weaponForm.getPerkDescription().copy().withStyle(ChatFormatting.GRAY)));

        // Aspects (Blade, Guard, Handle)
        if (blade != null) {
            tooltip.add(Component.literal("  ❖ ").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.translatable("tooltip.mushokucraft.modular_weapon.blade"))
                    .append(": ")
                    .append(blade.getDisplayName().copy().withColor(blade.color())));
        }
        if (guard != null) {
            tooltip.add(Component.literal("  ❖ ").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.translatable("tooltip.mushokucraft.modular_weapon.guard"))
                    .append(": ")
                    .append(guard.getDisplayName().copy().withColor(guard.color())));
        }
        if (handle != null) {
            tooltip.add(Component.literal("  ❖ ").withStyle(ChatFormatting.DARK_AQUA)
                    .append(Component.translatable("tooltip.mushokucraft.modular_weapon.handle"))
                    .append(": ")
                    .append(handle.getDisplayName().copy().withColor(handle.color())));
        }

        // Beast Core Perk
        if (core != null) {
            tooltip.add(Component.literal("---------------------------").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("◈ ").withColor(core.getColor())
                    .append(Component.translatable("tooltip.mushokucraft.modular_weapon.core"))
                    .append(": ")
                    .append(core.getDisplayName().copy().withColor(core.getColor())));
            tooltip.add(Component.literal("  └ ").withStyle(ChatFormatting.GOLD)
                    .append(core.getPerkName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(": ")
                    .append(core.getPerkDescription().copy().withStyle(ChatFormatting.YELLOW)));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
