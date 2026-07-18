package com.mushokucraft.init;

import com.mushokucraft.data.PlayerMasteryData;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create((ResourceKey)NeoForgeRegistries.Keys.ATTACHMENT_TYPES, (String)"mushokucraft");
    public static final Supplier<AttachmentType<PlayerMasteryData>> PLAYER_MASTERY = ATTACHMENT_TYPES.register("player_mastery", () -> AttachmentType.serializable(PlayerMasteryData::new).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}


