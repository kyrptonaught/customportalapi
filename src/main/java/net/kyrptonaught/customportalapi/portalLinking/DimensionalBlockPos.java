package net.kyrptonaught.customportalapi.portalLinking;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class DimensionalBlockPos {
    public Identifier dimensionType;
    public BlockPos pos;

    public DimensionalBlockPos(Identifier dimension, BlockPos pos) {
        this.pos = pos;
        this.dimensionType = dimension;
    }

    public static DimensionalBlockPos fromTag(NbtCompound tag) {
        return new DimensionalBlockPos(new Identifier(tag.getString("dimID")), BlockPos.fromLong(tag.getLong("pos")));
    }

    public NbtCompound toTag(NbtCompound tag) {
        tag.putString("dimID", this.dimensionType.toString());
        tag.putLong("pos", pos.asLong());
        return tag;
    }
}