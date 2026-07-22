package com.mushokucraft.client.input;

import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.item.MagicBookItem;
import com.mushokucraft.network.QteResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.architectury.networking.NetworkManager;
import software.bernie.geckolib.animatable.GeoItem;

public class ClientCastState {
    public static boolean isCasting = false;
    public static ResourceLocation currentSpell = null;
    public static int totalTicks = 0;
    public static int fizzleTick = 0;
    public static int elapsedTicks = 0;
    public static boolean hasFizzled = false;
    public static int fadeOutTimer = 0;
    public static boolean isQteActive = false;
    public static String currentQteKey = "";
    public static float qteSpeedModifier = 1.0f;
    public static float qteTargetSizeModifier = 1.0f;
    public static float qteShrinkingCircleScale = 3.0f;
    public static boolean isLearningCast = false;

    public static void startCast(ResourceLocation spellId, int castTime, int fizzle, boolean isLearning) {
        isCasting = true;
        currentSpell = spellId;
        totalTicks = castTime;
        fizzleTick = fizzle;
        elapsedTicks = 0;
        hasFizzled = false;
        fadeOutTimer = 0;
        isQteActive = false;
        isLearningCast = isLearning;
    }

    public static void startQte(String keyLetter, float speedModifier, float targetSizeModifier) {
        isQteActive = true;
        currentQteKey = keyLetter;
        qteSpeedModifier = speedModifier;
        qteTargetSizeModifier = targetSizeModifier;
        qteShrinkingCircleScale = 3.0f;
    }

    public static void endQte(boolean success) {
        isQteActive = false;
        if (!success) {
            ClientCastState.triggerFizzle();
        }
    }

    public static void triggerFizzle() {
        if (fizzleTick <= 0) {
            fizzleTick = elapsedTicks;
        }
        isCasting = false;
        hasFizzled = true;
        fadeOutTimer = 40;
        isQteActive = false;
        ClientCastState.stopAnimations();
    }

    public static void tick() {
        if (!isCasting && fadeOutTimer > 0) {
            --fadeOutTimer;
            return;
        }
        if (isCasting) {
            if (isQteActive) {
                if ((qteShrinkingCircleScale -= 0.15f * qteSpeedModifier) <= 0.0f) {
                    ClientCastState.endQte(false);
                    NetworkManager.sendToServer((CustomPacketPayload)new QteResultPacket(0, isLearningCast));
                }
                return;
            }
            ++elapsedTicks;
            if (fizzleTick > 0 && elapsedTicks >= fizzleTick) {
                return; // Pause and wait for QTE packet from server
            } else if (elapsedTicks >= totalTicks) {
                isCasting = false;
                hasFizzled = false;
                fadeOutTimer = 40;
                ClientCastState.stopAnimations();
            }
        }
    }

    public static void stopAnimations() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack itemInHand = mc.player.getMainHandItem();
            Item item = itemInHand.getItem();
            if (item instanceof MagicBookItem) {
                MagicBookItem animatable = (MagicBookItem)item;
                long id = GeoItem.getId((ItemStack)itemInHand);
                animatable.triggerAnim((Entity)mc.player, id, "controller", "idle");
            }
            CombatInputHandler.stopPlayerAnimation();
        }
    }

    public static void cancelAll() {
        isCasting = false;
        hasFizzled = false;
        fadeOutTimer = 0;
        isQteActive = false;
        stopAnimations();
    }
}
