package com.mushokucraft.magic.circle;

public class ClientMagicCircleState {
    private static long clientSeed = 0L;
    private static int activeTeleportPatternIndex = 0;

    public static void setClientWorldInfo(long seed, int patternIndex) {
        clientSeed = seed;
        activeTeleportPatternIndex = patternIndex;
    }

    public static long getClientSeed() {
        return clientSeed;
    }

    public static int getActiveTeleportPatternIndex() {
        return activeTeleportPatternIndex;
    }

    public static MagicCirclePattern getActiveTeleportPattern() {
        return MagicCirclePatterns.getPatternForSeed(clientSeed, TeleportCircleType.ID);
    }

    public static MagicCirclePattern getPatternForType(net.minecraft.resources.ResourceLocation typeId) {
        return MagicCirclePatterns.getPatternForSeed(clientSeed, typeId);
    }
}
