package com.mushokucraft.client.gui;

import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.data.PlayerMasteryProvider;
import com.mushokucraft.magic.circle.MagicCirclePattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientScreenOpener {

    public static void openCanvasScreen(boolean isMainHand, MagicCirclePattern draft, int spent) {
        Minecraft.getInstance().setScreen(new MagicCanvasScreen(isMainHand, draft, spent));
    }

    public static void openAncientManuscriptScreen(ResourceLocation circleTypeId) {
        Minecraft.getInstance().setScreen(new AncientManuscriptScreen(circleTypeId));
    }

    public static void openMagicBookScreen(String elementPrefix, List<String> spellIds) {
        Minecraft.getInstance().setScreen(new MagicBookScreen(elementPrefix, spellIds));
    }

    public static boolean isCircleStudiedClient(ResourceLocation circleTypeId) {
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            if (clientPlayer.isCreative()) {
                return true;
            }
            PlayerMasteryData data = PlayerMasteryProvider.get(clientPlayer);
            return data != null && data.isCircleStudied(circleTypeId);
        }
        return false;
    }
}
