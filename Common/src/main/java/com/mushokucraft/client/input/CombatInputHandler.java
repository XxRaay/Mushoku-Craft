package com.mushokucraft.client.input;

import com.mushokucraft.MushokuCraftCommon;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.ClientSpellState;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.network.CancelCastPacket;
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
    private static com.mushokucraft.magic.entity.IcicleBreakTargetEntity previewTargetEntity = null;
    private static int lastSelectedSlot = -1;

    public static boolean isCharging() {
        return isCharging;
    }

    public static com.mushokucraft.magic.entity.IcicleBreakTargetEntity getPreviewEntity() {
        return previewTargetEntity;
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
            if (action != GLFW.GLFW_PRESS) {
                return EventResult.pass();
            }
            boolean isAttack = mc.options.keyAttack.matchesMouse(button);
            boolean isUseItem = mc.options.keyUse.matchesMouse(button);
            
            if (isAttack && ClientSpellState.selectedSpell != null) {
                if (ClientCastState.isCasting) {
                    return EventResult.interruptFalse();
                }
                String spellName = ClientSpellState.selectedSpell.getPath();
                if (spellName.equals("longsword_light") || spellName.equals("longsword_of_silence")) {
                    return EventResult.pass();
                }
                if (!isCharging) {
                    isCharging = true;
                    NetworkManager.sendToServer(new StartChargePacket(ClientSpellState.selectedSpell));
                    if (spellName.equals("cumulonimbus")) {
                        CombatInputHandler.startPlayerAnimation("cumulonimbus_charge");
                    } else {
                        CombatInputHandler.startPlayerAnimation("waterball_charge");
                    }
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
                lastSelectedSlot = -1;
                ClientCastState.cancelAll();
                return;
            }

            int currentSlot = mc.player.getInventory().selected;
            if (lastSelectedSlot != -1 && currentSlot != lastSelectedSlot) {
                if (ClientCastState.isCasting) {
                    ClientCastState.cancelAll();
                    NetworkManager.sendToServer(new CancelCastPacket());
                }
                if (isCharging) {
                    isCharging = false;
                    NetworkManager.sendToServer(new ReleaseChargePacket());
                    CombatInputHandler.stopPlayerAnimation();
                    lastStance = null;
                }
            }
            lastSelectedSlot = currentSlot;

            if (ClientSpellState.selectedSpell == null && ClientCastState.isCasting && !ClientCastState.isLearningCast) {
                ClientCastState.cancelAll();
                NetworkManager.sendToServer(new CancelCastPacket());
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
            if (ClientSpellState.selectedSpell != null && ClientSpellState.selectedSpell.getPath().equals("icicle_break")) {
                if (mc.level != null && mc.player != null) {
                    com.mushokucraft.data.PlayerMasteryData mastery = (com.mushokucraft.data.PlayerMasteryData) com.mushokucraft.data.PlayerMasteryProvider.get(mc.player);
                    float spellMastery = mastery != null ? mastery.getSpellMastery(ClientSpellState.selectedSpell) : 0f;
                    
                    if (isCharging && spellMastery >= 1.0f) {
                        // Silent Cast is charging, server handles the render. Discard client preview.
                        if (previewTargetEntity != null) {
                            previewTargetEntity.discard();
                            previewTargetEntity = null;
                        }
                    } else {
                        // Not charging, OR Regular Cast is charging (server hasn't spawned yet)
                        net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition();
                        net.minecraft.world.phys.Vec3 look = mc.player.getLookAngle();
                        net.minecraft.world.phys.Vec3 endPos = eyePos.add(look.scale(7.0));
                        net.minecraft.world.phys.HitResult result = mc.level.clip(new net.minecraft.world.level.ClipContext(eyePos, endPos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
                        net.minecraft.world.phys.Vec3 hitPos = result.getLocation();
                        
                        net.minecraft.world.phys.HitResult groundResult = mc.level.clip(new net.minecraft.world.level.ClipContext(hitPos, hitPos.add(0, -64, 0), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player));
                        net.minecraft.world.phys.Vec3 groundPos = groundResult.getLocation();

                        if (previewTargetEntity == null) {
                            previewTargetEntity = new com.mushokucraft.magic.entity.IcicleBreakTargetEntity(mc.level, mc.player);
                            previewTargetEntity.setPos(groundPos.x, groundPos.y + 0.05, groundPos.z);
                            previewTargetEntity.setSnappedToGround(true);
                            mc.level.addEntity(previewTargetEntity);
                        } else {
                            // Lerp position
                            double lerpFactor = 0.3; // Adjust for smoothness vs responsiveness
                            double newX = previewTargetEntity.getX() + (groundPos.x - previewTargetEntity.getX()) * lerpFactor;
                            double newZ = previewTargetEntity.getZ() + (groundPos.z - previewTargetEntity.getZ()) * lerpFactor;
                            
                            previewTargetEntity.xOld = previewTargetEntity.getX();
                            previewTargetEntity.yOld = previewTargetEntity.getY();
                            previewTargetEntity.zOld = previewTargetEntity.getZ();
                            previewTargetEntity.setPos(newX, groundPos.y + 0.05, newZ);
                        }
                    }
                }
            } else {
                if (previewTargetEntity != null) {
                    previewTargetEntity.discard();
                    previewTargetEntity = null;
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
