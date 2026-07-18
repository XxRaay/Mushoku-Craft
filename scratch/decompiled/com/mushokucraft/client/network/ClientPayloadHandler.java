/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 */
package com.mushokucraft.client.network;

import com.mushokucraft.client.gui.MasteryOverlay;
import com.mushokucraft.client.hud.ManaHudManager;
import com.mushokucraft.client.input.ClientCastState;
import com.mushokucraft.client.input.CombatInputHandler;
import com.mushokucraft.data.PlayerMasteryData;
import com.mushokucraft.init.ModAttachments;
import com.mushokucraft.init.ModSpells;
import com.mushokucraft.item.IMagicBook;
import com.mushokucraft.magic.Spell;
import com.mushokucraft.network.CastStartedPacket;
import com.mushokucraft.network.LearnSpellResultPacket;
import com.mushokucraft.network.LearnSpellSyncPacket;
import com.mushokucraft.network.MasteryGainedPacket;
import com.mushokucraft.network.QteTriggerPacket;
import com.mushokucraft.network.SyncManaPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(value=Dist.CLIENT)
public class ClientPayloadHandler {
    public static void handleCastStarted(CastStartedPacket packet) {
        ClientCastState.startCast(packet.spellId(), packet.castTimeTicks(), packet.fizzleTick(), false);
    }

    public static void handleLearnSpellSync(LearnSpellSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (mc.player != null) {
            ItemStack itemInHand;
            Item item;
            Spell spell = ModSpells.SPELLS.get(packet.spellId());
            if (spell != null) {
                ClientCastState.startCast(spell.getId(), (int)spell.getBaseCastTimeTicks(), packet.fizzleTick(), true);
            }
            if ((item = (itemInHand = mc.player.getMainHandItem()).getItem()) instanceof IMagicBook) {
                IMagicBook book = (IMagicBook)item;
                book.triggerCastAnimation((Player)mc.player, itemInHand);
            }
            CombatInputHandler.startPlayerAnimation("waterball_cast");
        }
    }

    public static void handleMasteryGained(MasteryGainedPacket packet) {
        MasteryOverlay.showMasteryGain(packet.amount());
    }

    public static void handleQteTrigger(QteTriggerPacket packet) {
        ClientCastState.startQte(packet.keyLetter(), packet.speedModifier(), packet.targetSizeModifier());
    }

    public static void handleLearnSpellResult(LearnSpellResultPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        if (mc.player != null) {
            if (packet.success()) {
                PlayerMasteryData mastery = (PlayerMasteryData)mc.player.getData(ModAttachments.PLAYER_MASTERY);
                mastery.addSpellMastery(packet.spellId(), 0.1f);
            } else {
                ItemStack item = mc.player.getMainHandItem();
                Item item2 = item.getItem();
                if (item2 instanceof IMagicBook) {
                    IMagicBook book = (IMagicBook)item2;
                    mc.setScreen(book.getLearningScreen());
                }
            }
        }
    }

    public static void handleSyncMana(SyncManaPacket packet) {
        ManaHudManager.ANIMATION_STATE.updateMana(packet.currentMana(), packet.maxMana());
    }
}

