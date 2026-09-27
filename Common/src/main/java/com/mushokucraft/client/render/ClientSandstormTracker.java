package com.mushokucraft.client.render;

import com.mushokucraft.config.MushokuConfig;
import com.mushokucraft.magic.entity.SandstormEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks active Sandstorm domains on the client side and provides
 * camouflage checks and tinting parameters (0.5 translucency + sand-golden color).
 */
public class ClientSandstormTracker {

    /**
     * 0.52 alpha, warm desert sand golden color.
     * Alpha = 132 (0x84), Red = 228 (0xE4), Green = 188 (0xBC), Blue = 116 (0x74)
     */
    public static final int SAND_CAMOUFLAGE_COLOR = (132 << 24) | (228 << 16) | (188 << 8) | 116;

    private static final Set<SandstormEntity> ACTIVE_STORMS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final ThreadLocal<LivingEntity> CURRENT_RENDERING_ENTITY = new ThreadLocal<>();

    public static void register(SandstormEntity storm) {
        if (storm != null) {
            ACTIVE_STORMS.add(storm);
        }
    }

    public static void unregister(SandstormEntity storm) {
        if (storm != null) {
            ACTIVE_STORMS.remove(storm);
        }
    }

    public static boolean isCamouflaged(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }

        double radius = MushokuConfig.SANDSTORM_RADIUS.get();
        double radiusSq = radius * radius;

        for (SandstormEntity storm : ACTIVE_STORMS) {
            if (storm != null && storm.isAlive() && storm.level() == entity.level()) {
                if (storm.getOwner() == entity) {
                    return true;
                }
                if (entity.distanceToSqr(storm.getX(), entity.getY(), storm.getZ()) <= radiusSq) {
                    if (storm.getOwner() != null && entity.isAlliedTo(storm.getOwner())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void setCurrentEntity(LivingEntity entity) {
        CURRENT_RENDERING_ENTITY.set(entity);
    }

    public static void clearCurrentEntity() {
        CURRENT_RENDERING_ENTITY.remove();
    }

    public static boolean isCurrentEntityCamouflaged() {
        LivingEntity current = CURRENT_RENDERING_ENTITY.get();
        return current != null && isCamouflaged(current);
    }
}
