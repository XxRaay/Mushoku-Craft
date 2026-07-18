/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.SwordItem
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 */
package com.mushokucraft.event;

import com.mushokucraft.client.hud.ManaHudManager;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.GAME)
public class ModClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ManaHudManager.tick();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && !mc.isPaused()) {
            for (Player player : mc.level.players()) {
                PlayerMasteryData mastery = (PlayerMasteryData)player.getData(ModAttachments.PLAYER_MASTERY);
                if (mastery == null || !mastery.isToukiActive()) continue;
                SwordStyle stance = mastery.getActiveStance();
                SimpleParticleType particle = ParticleTypes.ENCHANT;
                if (stance == SwordStyle.SWORD_GOD) {
                    particle = ParticleTypes.CRIT;
                } else if (stance == SwordStyle.WATER_GOD) {
                    particle = ParticleTypes.SPLASH;
                } else if (stance == SwordStyle.NORTH_GOD) {
                    particle = ParticleTypes.SQUID_INK;
                }
                for (int i = 0; i < 2; ++i) {
                    double dx = (mc.level.random.nextDouble() - 0.5) * 1.5;
                    double dy = mc.level.random.nextDouble() * 2.0;
                    double dz = (mc.level.random.nextDouble() - 0.5) * 1.5;
                    double vx = 0.0;
                    double vy = 0.1;
                    double vz = 0.0;
                    if (stance == SwordStyle.SWORD_GOD) {
                        vx = (mc.level.random.nextDouble() - 0.5) * 0.2;
                        vy = mc.level.random.nextDouble() * 0.2;
                        vz = (mc.level.random.nextDouble() - 0.5) * 0.2;
                    } else if (stance == SwordStyle.NORTH_GOD) {
                        vx = (mc.level.random.nextDouble() - 0.5) * 0.1;
                        vy = (mc.level.random.nextDouble() - 0.5) * 0.1;
                        vz = (mc.level.random.nextDouble() - 0.5) * 0.1;
                    }
                    mc.level.addParticle((ParticleOptions)particle, player.getX() + dx, player.getY() + dy, player.getZ() + dz, vx, vy, vz);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() == InteractionHand.OFF_HAND) {
            PlayerMasteryData mastery;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && (mastery = (PlayerMasteryData)mc.player.getData(ModAttachments.PLAYER_MASTERY)) != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && mc.player.getMainHandItem().getItem() instanceof SwordItem) {
                event.setCanceled(true);
            }
        }
    }
}

