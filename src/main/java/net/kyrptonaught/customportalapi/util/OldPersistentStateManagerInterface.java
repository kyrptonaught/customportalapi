package net.kyrptonaught.customportalapi.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.util.function.Function;
import java.util.function.Supplier;

public interface OldPersistentStateManagerInterface {
    default  <T extends PersistentState> T computeIfAbsent(Function<NbtCompound, T> nbtCompoundTFunction, Supplier<T> tSupplier, String string) {
        return null;
    }
}
