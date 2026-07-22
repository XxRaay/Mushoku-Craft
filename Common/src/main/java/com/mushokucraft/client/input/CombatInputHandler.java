package com.mushokucraft.client.input;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.ClientSpellState;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.network.ReleaseChargePacket;
import com.mushokucraft.network.StartChargePacket;
import com.mushokucraft.network.TriggerParryPacket;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.networking.NetworkManager;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.lwjgl.glfw.GLFW;

public class CombatInputHandler {
    private static boolean isCharging = false;
    private static KeyframeAnimationPlayer animationPlayer = null;
    private static SwordStyle lastStance = null;
    private static KeyframeAnimationPlayer stanceAnimationPlayer = null;

    public static boolean isCharging() {
        return isCharging;
    }

    public static void register() {
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register((client, button, action, mods) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                return EventResult.pass();
            }
            if (ClientCastState.isQteActive) {
                return EventResult.interruptFalse();
            }
            boolean isAttack = mc.options.keyAttack.matchesMouse(button);
            boolean isUseItem = mc.options.keyUse.matchesMouse(button);
            
            if (isAttack && ClientSpellState.selectedSpell != null) {
                String spellName = ClientSpellState.selectedSpell.getPath();
                if (spellName.equals("longsword_light") || spellName.equals("longsword_of_silence")) {
                    return EventResult.pass();
                }
                if (!isCharging) {
                    isCharging = true;
                    NetworkManager.sendToServer(new StartChargePacket(ClientSpellState.selectedSpell));
                    CombatInputHandler.startPlayerAnimation("waterball_charge");
                }
                mc.player.swing(InteractionHand.MAIN_HAND);
                return EventResult.interruptFalse();
            } else if (isUseItem) {
                if (ClientSpellState.selectedSpell != null) {
                    String spellName = ClientSpellState.selectedSpell.getPath();
                    if (spellName.equals("longsword_light")) {
                        NetworkManager.sendToServer(new StartChargePacket(ClientSpellState.selectedSpell));
                        return EventResult.interruptFalse();
                    }
                    if (spellName.equals("longsword_of_silence")) {
                        NetworkManager.sendToServer(new StartChargePacket(ClientSpellState.selectedSpell));
                        CombatInputHandler.startPlayerAnimation("longsword_of_silence");
                        return EventResult.interruptFalse();
                    }
                }
                SwordStyle stance = ClientStanceState.selectedStance;
                ItemStack mainHand = mc.player.getMainHandItem();
                if (stance == SwordStyle.WATER_GOD && mainHand.getItem() instanceof SwordItem && !mc.player.getCooldowns().isOnCooldown(mainHand.getItem())) {
                    mc.player.getCooldowns().addCooldown(mainHand.getItem(), 40);
                    CombatInputHandler.playParryAnimation();
                    NetworkManager.sendToServer(new TriggerParryPacket());
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        ClientTickEvent.CLIENT_POST.register(mc -> {
            boolean holdingSword;
            SwordStyle currentStance;
            ClientCastState.tick();
            if (mc.player == null || mc.player.isDeadOrDying()) {
                if (isCharging) {
                    NetworkManager.sendToServer(new ReleaseChargePacket());
                }
                isCharging = false;
                lastStance = null;
                ClientCastState.cancelAll();
                return;
            }
            if (animationPlayer != null && !animationPlayer.isActive()) {
                animationPlayer = null;
                lastStance = null;
            }
            SwordStyle swordStyle = currentStance = (holdingSword = mc.player.getMainHandItem().getItem() instanceof SwordItem) ? ClientStanceState.selectedStance : null;
            if (currentStance != lastStance) {
                lastStance = currentStance;
                if (currentStance == null) {
                    CombatInputHandler.stopStanceAnimation();
                } else if (currentStance == SwordStyle.SWORD_GOD) {
                    CombatInputHandler.startStanceAnimation("swordgod");
                } else if (currentStance == SwordStyle.WATER_GOD) {
                    CombatInputHandler.startStanceAnimation("watergod");
                } else if (currentStance == SwordStyle.NORTH_GOD) {
                    CombatInputHandler.startStanceAnimation("northgod");
                }
            }
            if (isCharging) {
                long window = mc.getWindow().getWindow();
                boolean isMouseDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS 
                                   || org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                if (!isMouseDown) {
                    isCharging = false;
                    NetworkManager.sendToServer(new ReleaseChargePacket());
                    CombatInputHandler.stopPlayerAnimation();
                    lastStance = null;
                }
            }
        });
    }

    public static void startPlayerAnimation(String animationName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        try {
            IPlayable anim;
            ModifierLayer animationLayer = (ModifierLayer)PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer)mc.player).get(ResourceLocation.fromNamespaceAndPath("mushokucraft", "animation"));
            if (animationLayer != null && (anim = PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath("mushokucraft", animationName))) != null) {
                animationPlayer = new KeyframeAnimationPlayer((KeyframeAnimation)anim);
                animationLayer.setAnimation((IAnimation)animationPlayer);
            }
        }
        catch (Exception e) {
            MushokuCraftCommon.LOGGER.error("Failed to play animation", (Throwable)e);
        }
    }

    public static void stopPlayerAnimation() {
        if (animationPlayer != null) {
            animationPlayer.stop();
            animationPlayer = null;
        }
    }

    public static void playParryAnimation() {
        CombatInputHandler.startPlayerAnimation("parry");
    }

    private static void startStanceAnimation(String animationName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        try {
            ModifierLayer animationLayer = (ModifierLayer)PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer)mc.player).get(ResourceLocation.fromNamespaceAndPath("mushokucraft", "animation"));
            if (animationLayer != null) {
                IPlayable anim;
                if (stanceAnimationPlayer != null) {
                    stanceAnimationPlayer.stop();
                }
                if ((anim = PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath("mushokucraft", animationName))) != null) {
                    stanceAnimationPlayer = new KeyframeAnimationPlayer((KeyframeAnimation)anim);
                    animationLayer.setAnimation((IAnimation)stanceAnimationPlayer);
                }
            }
        }
        catch (Exception e) {
            MushokuCraftCommon.LOGGER.error("Failed to play stance animation", (Throwable)e);
        }
    }

    private static void stopStanceAnimation() {
        if (stanceAnimationPlayer != null) {
            stanceAnimationPlayer.stop();
            stanceAnimationPlayer = null;
        }
    }
}
