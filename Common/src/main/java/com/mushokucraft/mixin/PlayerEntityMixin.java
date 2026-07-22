package com.mushokucraft.mixin;

import com.mushokucraft.data.PlayerMasteryAccessor;
import com.mushokucraft.data.PlayerMasteryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin implements PlayerMasteryAccessor {
    @Unique
    private final PlayerMasteryData mushokucraft$masteryData = new PlayerMasteryData();

    @Override
    public PlayerMasteryData getPlayerMasteryData() {
        return this.mushokucraft$masteryData;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readCustomData(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("MushokuCraftMastery")) {
            this.mushokucraft$masteryData.deserializeNBT(((Player)(Object)this).level().registryAccess(), nbt.getCompound("MushokuCraftMastery"));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeCustomData(CompoundTag nbt, CallbackInfo ci) {
        nbt.put("MushokuCraftMastery", this.mushokucraft$masteryData.serializeNBT(((Player)(Object)this).level().registryAccess()));
    }
}
