package com.mushokucraft.client.input;

import com.mushokucraft.MushokuCraft;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.ClientSpellState;
import com.mushokucraft.client.input.ClientStanceState;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.network.ReleaseChargePacket;
import com.mushokucraft.network.StartChargePacket;
import com.mushokucraft.network.TriggerParryPacket;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.GAME)
public class CombatInputHandler {
    private static boolean isCharging = false;
    private static KeyframeAnimationPlayer animationPlayer = null;
    private static SwordStyle lastStance = null;
    private static KeyframeAnimationPlayer stanceAnimationPlayer = null;

    public static boolean isCharging() {
        return isCharging;
    }

    @SubscribeEvent
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (ClientCastState.isQteActive) {
            event.setCanceled(true);
            return;
        }
        if (event.isAttack() && ClientSpellState.selectedSpell != null) {
            String spellName = ClientSpellState.selectedSpell.getPath();
            if (spellName.equals("longsword_light") || spellName.equals("longsword_of_silence")) {
                return;
            }
            if (!isCharging) {
                isCharging = true;
                PacketDistributor.sendToServer((CustomPacketPayload)new StartChargePacket(ClientSpellState.selectedSpell), (CustomPacketPayload[])new CustomPacketPayload[0]);
                CombatInputHandler.startPlayerAnimation("waterball_charge");
            }
            event.setCanceled(true);
            mc.player.swing(InteractionHand.MAIN_HAND);
        } else if (event.isUseItem()) {
            if (ClientSpellState.selectedSpell != null) {
                String spellName = ClientSpellState.selectedSpell.getPath();
                if (spellName.equals("longsword_light")) {
                    PacketDistributor.sendToServer((CustomPacketPayload)new StartChargePacket(ClientSpellState.selectedSpell), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    event.setCanceled(true);
                    return;
                }
                if (spellName.equals("longsword_of_silence")) {
                    PacketDistributor.sendToServer((CustomPacketPayload)new StartChargePacket(ClientSpellState.selectedSpell), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    CombatInputHandler.startPlayerAnimation("longsword_of_silence");
                    event.setCanceled(true);
                    return;
                }
            }
            SwordStyle stance = ClientStanceState.selectedStance;
            ItemStack mainHand = mc.player.getMainHandItem();
            if (stance == SwordStyle.WATER_GOD && mainHand.getItem() instanceof SwordItem && !mc.player.getCooldowns().isOnCooldown(mainHand.getItem())) {
                mc.player.getCooldowns().addCooldown(mainHand.getItem(), 40);
                CombatInputHandler.playParryAnimation();
                PacketDistributor.sendToServer((CustomPacketPayload)new TriggerParryPacket(), (CustomPacketPayload[])new CustomPacketPayload[0]);
                event.setCanceled(true);
            }
        }
    }

    public static void startPlayerAnimation(String animationName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        try {
            IPlayable anim;
            ModifierLayer animationLayer = (ModifierLayer)PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer)mc.player).get(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"animation"));
            if (animationLayer != null && (anim = PlayerAnimationRegistry.getAnimation((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)animationName))) != null) {
                animationPlayer = new KeyframeAnimationPlayer((KeyframeAnimation)anim);
                animationLayer.setAnimation((IAnimation)animationPlayer);
            }
        }
        catch (Exception e) {
            MushokuCraft.LOGGER.error("Failed to play animation", (Throwable)e);
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
            ModifierLayer animationLayer = (ModifierLayer)PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer)mc.player).get(ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)"animation"));
            if (animationLayer != null) {
                IPlayable anim;
                if (stanceAnimationPlayer != null) {
                    stanceAnimationPlayer.stop();
                }
                if ((anim = PlayerAnimationRegistry.getAnimation((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"mushokucraft", (String)animationName))) != null) {
                    stanceAnimationPlayer = new KeyframeAnimationPlayer((KeyframeAnimation)anim);
                    animationLayer.setAnimation((IAnimation)stanceAnimationPlayer);
                }
            }
        }
        catch (Exception e) {
            MushokuCraft.LOGGER.error("Failed to play stance animation", (Throwable)e);
        }
    }

    private static void stopStanceAnimation() {
        if (stanceAnimationPlayer != null) {
            stanceAnimationPlayer.stop();
            stanceAnimationPlayer = null;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        boolean holdingSword;
        SwordStyle currentStance;
        ClientCastState.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            isCharging = false;
            lastStance = null;
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
            boolean isMouseDown;
            boolean bl = isMouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
            if (!isMouseDown) {
                isCharging = false;
                PacketDistributor.sendToServer((CustomPacketPayload)new ReleaseChargePacket(), (CustomPacketPayload[])new CustomPacketPayload[0]);
                CombatInputHandler.stopPlayerAnimation();
                lastStance = null;
            }
        }
    }
}


