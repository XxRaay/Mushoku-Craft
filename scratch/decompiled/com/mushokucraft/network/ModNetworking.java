/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.fml.common.EventBusSubscriber$Bus
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.mushokucraft.network;

import com.mushokucraft.client.network.ClientPayloadHandler;
import com.mushokucraft.network.CastSpellPacket;
import com.mushokucraft.network.CastStartedPacket;
import com.mushokucraft.network.ChangeStancePacket;
import com.mushokucraft.network.LearnSpellResultPacket;
import com.mushokucraft.network.LearnSpellSyncPacket;
import com.mushokucraft.network.MasteryGainedPacket;
import com.mushokucraft.network.QteResultPacket;
import com.mushokucraft.network.QteTriggerPacket;
import com.mushokucraft.network.ReleaseChargePacket;
import com.mushokucraft.network.StartChargePacket;
import com.mushokucraft.network.StartLearnSpellPacket;
import com.mushokucraft.network.SyncFullMasteryPacket;
import com.mushokucraft.network.SyncManaPacket;
import com.mushokucraft.network.SyncToukiPacket;
import com.mushokucraft.network.ToggleToukiPacket;
import com.mushokucraft.network.TriggerParryPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid="mushokucraft", bus=EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("mushokucraft").versioned("1.0");
        registrar.playToClient(SyncManaPacket.TYPE, SyncManaPacket.STREAM_CODEC, ModNetworking::handleSyncMana);
        registrar.playToClient(CastStartedPacket.TYPE, CastStartedPacket.STREAM_CODEC, CastStartedPacket::handle);
        registrar.playToServer(CastSpellPacket.TYPE, CastSpellPacket.STREAM_CODEC, CastSpellPacket::handle);
        registrar.playToServer(StartChargePacket.TYPE, StartChargePacket.STREAM_CODEC, StartChargePacket::handle);
        registrar.playToServer(ReleaseChargePacket.TYPE, ReleaseChargePacket.STREAM_CODEC, ReleaseChargePacket::handle);
        registrar.playToServer(StartLearnSpellPacket.TYPE, StartLearnSpellPacket.STREAM_CODEC, StartLearnSpellPacket::handle);
        registrar.playToClient(LearnSpellSyncPacket.TYPE, LearnSpellSyncPacket.STREAM_CODEC, LearnSpellSyncPacket::handle);
        registrar.playToClient(QteTriggerPacket.TYPE, QteTriggerPacket.STREAM_CODEC, QteTriggerPacket::handle);
        registrar.playToServer(QteResultPacket.TYPE, QteResultPacket.STREAM_CODEC, QteResultPacket::handle);
        registrar.playToClient(LearnSpellResultPacket.TYPE, LearnSpellResultPacket.STREAM_CODEC, LearnSpellResultPacket::handle);
        registrar.playToServer(ChangeStancePacket.TYPE, ChangeStancePacket.STREAM_CODEC, ChangeStancePacket::handle);
        registrar.playToClient(TriggerParryPacket.TYPE, TriggerParryPacket.STREAM_CODEC, TriggerParryPacket::handle);
        registrar.playToServer(ToggleToukiPacket.TYPE, ToggleToukiPacket.STREAM_CODEC, ToggleToukiPacket::handle);
        registrar.playToClient(SyncToukiPacket.TYPE, SyncToukiPacket.STREAM_CODEC, SyncToukiPacket::handle);
        registrar.playToClient(SyncFullMasteryPacket.TYPE, SyncFullMasteryPacket.STREAM_CODEC, SyncFullMasteryPacket::handle);
        registrar.playToClient(MasteryGainedPacket.TYPE, MasteryGainedPacket.STREAM_CODEC, MasteryGainedPacket::handle);
    }

    private static void handleSyncMana(SyncManaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPayloadHandler.handleSyncMana(packet));
    }
}

