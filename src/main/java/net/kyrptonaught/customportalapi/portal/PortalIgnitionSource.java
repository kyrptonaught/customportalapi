package net.kyrptonaught.customportalapi.portal;

import net.kyrptonaught.customportalapi.mixin.BucketMixin;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;

public class PortalIgnitionSource {
    public static PortalIgnitionSource FIRE = new PortalIgnitionSource(SourceType.BLOCKPLACED, Registry.BLOCK.getId(Blocks.FIRE));

    private enum SourceType {
        USEITEM, BLOCKPLACED, FLUID, CUSTOM
    }
    private static HashSet<Item> USEITEMS = new HashSet<>();
    public SourceType sourceType;
    public Identifier ignitionSourceID;

    private PortalIgnitionSource(SourceType sourceType, Identifier ignitionSourceID) {
        this.sourceType = sourceType;
        this.ignitionSourceID = ignitionSourceID;
    }

    public static PortalIgnitionSource ItemUseSource(Item item) {
        USEITEMS.add(item);
        if (item instanceof BucketItem)
            return new PortalIgnitionSource(SourceType.FLUID, Registry.FLUID.getId(((BucketMixin) item).getFluidType()));
        return new PortalIgnitionSource(SourceType.USEITEM, Registry.ITEM.getId(item));
    }

    public static PortalIgnitionSource FluidSource(Fluid fluid) {
        return new PortalIgnitionSource(SourceType.FLUID, Registry.FLUID.getId(fluid));
    }

    public static PortalIgnitionSource CustomSource(Identifier ignitionSourceID) {
        return new PortalIgnitionSource(SourceType.CUSTOM, ignitionSourceID);
    }

    public static boolean isRegisteredIgnitionSourceWith(Item item) {
        return item instanceof BucketItem || USEITEMS.contains(item);
    }

}
