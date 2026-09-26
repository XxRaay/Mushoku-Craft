package com.mushokucraft.magic.circle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MagicCircleRegistry {
    private static final Map<ResourceLocation, MagicCircleType> CIRCLE_TYPES = new LinkedHashMap<>();

    public static final MagicCircleType TELEPORTATION = register(new TeleportCircleType());
    public static final MagicCircleType BARRIER = register(new BarrierCircleType());
    public static final MagicCircleType CAPTURE = register(new CaptureCircleType());
    public static final MagicCircleType SUMMONING = register(new SummoningCircleType());
    public static final MagicCircleType CRYSTALLIZATION = register(new CrystallizationCircleType());
    public static final MagicCircleType SANCTUARY = register(new SanctuaryCircleType());
    public static final MagicCircleType OVERGROWTH = register(new OvergrowthCircleType());
    public static final MagicCircleType DIMENSIONAL_GATE = register(new DimensionalGateCircleType());
    public static final MagicCircleType SOUL_ANCHOR = register(new SoulAnchorCircleType());

    public static <T extends MagicCircleType> T register(T type) {
        CIRCLE_TYPES.put(type.getId(), type);
        return type;
    }

    public static MagicCircleType get(ResourceLocation id) {
        return CIRCLE_TYPES.get(id);
    }

    public static Collection<MagicCircleType> getAll() {
        return Collections.unmodifiableCollection(CIRCLE_TYPES.values());
    }

    public static List<ResourceLocation> getAllIdsSorted() {
        List<ResourceLocation> list = new ArrayList<>(CIRCLE_TYPES.keySet());
        list.sort(Comparator.comparing(ResourceLocation::toString));
        return Collections.unmodifiableList(list);
    }

    /**
     * Resolves which magic circle type corresponds to this drawn pattern in this world.
     * Iterates over all registered circle types, resolving each type's unique seed-assigned pattern.
     * Unassigned patterns in the seed pool act as inactive duds (return null).
     */
    public static MagicCircleType identify(Level level, MagicCirclePattern pattern) {
        if (pattern == null || pattern.countFilled() == 0) return null;

        long seed = level instanceof ServerLevel sl ? sl.getSeed() : ClientMagicCircleState.getClientSeed();

        for (MagicCircleType type : getAll()) {
            MagicCirclePattern expected = MagicCirclePatterns.getPatternForSeed(seed, type.getId());
            if (expected != null && pattern.matches(expected)) {
                return type;
            }
        }

        return null;
    }
}
