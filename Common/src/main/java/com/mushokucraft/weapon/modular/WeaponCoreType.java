package com.mushokucraft.weapon.modular;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WeaponCoreType {
    private final String id;
    private final String translationKey;
    private final int color;
    private final String perkNameKey;
    private final String perkDescKey;

    public WeaponCoreType(String id, String translationKey, int color, String perkNameKey, String perkDescKey) {
        this.id = id;
        this.translationKey = translationKey;
        this.color = color;
        this.perkNameKey = perkNameKey;
        this.perkDescKey = perkDescKey;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getColor() {
        return color;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public Component getPerkName() {
        return Component.translatable(perkNameKey);
    }

    public Component getPerkDescription() {
        return Component.translatable(perkDescKey);
    }

    public void onHit(ItemStack weapon, LivingEntity target, LivingEntity attacker, float damageDealt) {
        if ("sabertooth_wolf".equals(id)) {
            // Savage Rend: Execute extra damage on targets with < 50% HP
            float healthRatio = target.getHealth() / target.getMaxHealth();
            if (healthRatio <= 0.5f) {
                float executeBonus = Math.max(2.0f, damageDealt * 0.25f);
                target.hurt(target.damageSources().mobAttack(attacker), executeBonus);

                // Inflict Slowness / Crippling Bleed
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));

                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 12, 0.3, 0.3, 0.3, 0.15);
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
                }
                attacker.level().playSound(null, target.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1.2f, 0.9f);
            }

            // On hit or kill: Grant brief predator speed burst to wielder
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, true));
        }
    }
}
