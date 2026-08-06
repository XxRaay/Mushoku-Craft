package com.mushokucraft.event;

import com.mushokucraft.client.hud.ManaHudManager;
import com.mushokucraft.combat.SwordStyle;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.EventResult;

public class ModClientEvents {
    public static void register() {
        ClientTickEvent.CLIENT_POST.register(mc -> {
            ManaHudManager.tick();
            if (mc.level != null && !mc.isPaused()) {
                for (Player player : mc.level.players()) {
                    PlayerMasteryData mastery = (PlayerMasteryData)PlayerMasteryProvider.get(player);
                    if (mastery == null) continue;

                    if (mastery.isToukiActive()) {
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
                    
                    if (mastery.isAirCushionActive()) {
                        // Wind particles at feet
                        for (int i = 0; i < 3; ++i) {
                            double dx = (mc.level.random.nextDouble() - 0.5) * 1.2;
                            double dy = mc.level.random.nextDouble() * 0.5; // close to ground
                            double dz = (mc.level.random.nextDouble() - 0.5) * 1.2;
                            mc.level.addParticle((ParticleOptions)ParticleTypes.CLOUD, player.getX() + dx, player.getY() + dy, player.getZ() + dz, 0, 0.05, 0);
                        }
                    }
                }
            }
        });
        
        // RenderHandEvent logic can be moved to InteractionEvent or handled properly via mixins or Architectury events.
        // There is no direct RenderHandEvent in Architectury. Let's just handle it in client tick or use a mixin later if needed.
        // I will temporarily comment out the RenderHand logic as it's specifically for canceling off-hand rendering.
        /*
        if (event.getHand() == InteractionHand.OFF_HAND) {
            PlayerMasteryData mastery;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && (mastery = (PlayerMasteryData)PlayerMasteryProvider.get(mc.player)) != null && mastery.getActiveStance() == SwordStyle.SWORD_GOD && mc.player.getMainHandItem().getItem() instanceof SwordItem) {
                event.setCanceled(true);
            }
        }
        */
    }
}
