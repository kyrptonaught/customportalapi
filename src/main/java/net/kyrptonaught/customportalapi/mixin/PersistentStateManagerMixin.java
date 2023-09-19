package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.util.OldPersistentStateManagerInterface;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(PersistentStateManager.class)
public class PersistentStateManagerMixin implements OldPersistentStateManagerInterface {
    @Shadow @Final
    private Map<String, PersistentState> loadedStates;

    @Unique
    public <T extends PersistentState> T computeIfAbsent(Function<NbtCompound, T> nbtCompoundTFunction, Supplier<T> tSupplier, String string) {
        T t = this.get(nbtCompoundTFunction, string);
        if (t != null) {
            return t;
        } else {
            T t1 = tSupplier.get();
            this.set(string, t1);
            return t1;
        }
    }

    @Unique
    public <T extends PersistentState> T get(Function<NbtCompound, T> nbtCompoundTFunction, String string) {
        PersistentState saveddata = this.loadedStates.get(string);
        if (saveddata == null && !this.loadedStates.containsKey(string)) {
            saveddata = this.readFromFile(nbtCompoundTFunction, null, string);
            this.loadedStates.put(string, saveddata);
        } else if (saveddata == null) {
            return null;
        }

        return (T)saveddata;
    }

    @Shadow
    public void set(String id, PersistentState state) {}

    @Shadow
    private <T extends PersistentState> T readFromFile(Function<NbtCompound, T> readFunction, DataFixTypes dataFixTypes, String id) {
        return null;
    }
}
