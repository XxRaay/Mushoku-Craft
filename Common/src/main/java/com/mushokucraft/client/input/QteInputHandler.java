package com.mushokucraft.client.input;

import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.network.QteResultPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import dev.architectury.networking.NetworkManager;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.EventResult;

public class QteInputHandler {
    public static void register() {
        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            int key;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null || mc.player == null || mc.player.isDeadOrDying()) {
                return EventResult.pass();
            }
            if (ClientCastState.isQteActive && action == 1 && (key = keyCode) >= 65 && key <= 90) {
                for (KeyMapping keyMapping : mc.options.keyMappings) {
                    if (!keyMapping.matches(key, scanCode)) continue;
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
                        result = scale <= ClientCastState.qtePerfectMultiplier * targetSize ? 2 : 1;
                    }
                    ClientCastState.endQte(result > 0);
                    NetworkManager.sendToServer(new QteResultPacket(result, ClientCastState.isLearningCast));
                } else {
                    ClientCastState.endQte(false);
                    NetworkManager.sendToServer(new QteResultPacket(0, ClientCastState.isLearningCast));
                }
            }
            return EventResult.pass();
        });
    }
}
