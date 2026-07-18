/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.client.event.InputEvent$Key
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.mushokucraft.client.input;

import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.network.QteResultPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="mushokucraft", value={Dist.CLIENT}, bus=EventBusSubscriber.Bus.GAME)
public class QteInputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        int key;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        if (ClientCastState.isQteActive && event.getAction() == 1 && (key = event.getKey()) >= 65 && key <= 90) {
            for (KeyMapping keyMapping : mc.options.keyMappings) {
                if (keyMapping.getKey().getValue() != key) continue;
                while (keyMapping.consumeClick()) {
                }
                keyMapping.setDown(false);
            }
            String pressedKey = String.valueOf((char)(97 + (key - 65)));
            if (pressedKey.equals(ClientCastState.currentQteKey)) {
                float scale = ClientCastState.qteShrinkingCircleScale;
                float targetSize = ClientCastState.qteTargetSizeModifier;
                int result = 0;
                if (scale <= targetSize) {
                    result = scale <= 0.4f * targetSize ? 2 : 1;
                }
                ClientCastState.endQte(result > 0);
                PacketDistributor.sendToServer((CustomPacketPayload)new QteResultPacket(result, ClientCastState.isLearningCast), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else {
                ClientCastState.endQte(false);
                PacketDistributor.sendToServer((CustomPacketPayload)new QteResultPacket(0, ClientCastState.isLearningCast), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
    }
}

