import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
public class Test implements INBTSerializable<CompoundTag> {
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) { return new CompoundTag(); }
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {}
}
