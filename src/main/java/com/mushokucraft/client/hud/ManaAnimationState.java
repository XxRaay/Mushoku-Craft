package com.mushokucraft.client.hud;

import com.mushokucraft.combat.SwordStyle;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;

public class ManaAnimationState {
    private int fizzleTicks;
    private static final int FIZZLE_DURATION = 20;
    private int chargingTicks;
    private boolean isCharging;
    private static final int CHARGE_RAMP_TICKS = 40;
    private boolean toukiActive;
    @Nullable
    private SwordStyle activeSwordStyle;
    private float previousMana;
    private float currentMana;
    private float maxMana = 100.0f;
    private float displayMana;
    private int manaChangedTicks;
    private static final int VISIBILITY_DURATION = 60;
    private static final int FADE_OUT_TICKS = 20;

    public void tick() {
        if (this.fizzleTicks > 0) {
            --this.fizzleTicks;
        }
        if (this.isCharging) {
            this.chargingTicks = Math.min(this.chargingTicks + 1, 40);
        } else if (this.chargingTicks > 0) {
            this.chargingTicks = Math.max(this.chargingTicks - 2, 0);
        }
        if (this.manaChangedTicks > 0) {
            --this.manaChangedTicks;
        }
        this.displayMana = Mth.lerp((float)0.15f, (float)this.displayMana, (float)this.currentMana);
    }

    public void triggerFizzle() {
        this.fizzleTicks = 20;
    }

    public void startCharging() {
        this.isCharging = true;
    }

    public void stopCharging() {
        this.isCharging = false;
    }

    public void setTouki(boolean active, @Nullable SwordStyle style) {
        this.toukiActive = active;
        this.activeSwordStyle = style;
    }

    public void updateMana(float current, float max) {
        if (Math.abs(current - this.currentMana) > 0.01f) {
            this.previousMana = this.currentMana;
            this.manaChangedTicks = 60;
        }
        this.currentMana = current;
        this.maxMana = max;
    }

    public float getFizzleProgress() {
        return (float)this.fizzleTicks / 20.0f;
    }

    public float getChargeIntensity() {
        return (float)this.chargingTicks / 40.0f;
    }

    public boolean isToukiActive() {
        return this.toukiActive;
    }

    @Nullable
    public SwordStyle getActiveSwordStyle() {
        return this.activeSwordStyle;
    }

    public float getCurrentMana() {
        return this.currentMana;
    }

    public float getMaxMana() {
        return this.maxMana;
    }

    public float getDisplayMana() {
        return this.displayMana;
    }

    public float getManaPercent() {
        return this.maxMana > 0.0f ? Mth.clamp((float)(this.displayMana / this.maxMana), (float)0.0f, (float)1.0f) : 0.0f;
    }

    public boolean isManaVisible() {
        return this.manaChangedTicks > 0 || this.currentMana < this.maxMana || this.fizzleTicks > 0 || this.isCharging || this.toukiActive;
    }

    public float getManaFadeAlpha() {
        if (this.currentMana < this.maxMana || this.fizzleTicks > 0 || this.isCharging || this.toukiActive) {
            return 1.0f;
        }
        if (this.manaChangedTicks <= 0) {
            return 0.0f;
        }
        if (this.manaChangedTicks <= 20) {
            return (float)this.manaChangedTicks / 20.0f;
        }
        return 1.0f;
    }
}


