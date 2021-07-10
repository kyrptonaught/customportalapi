package net.kyrptonaught.customportalapi.api;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CustomPortalBuilder {
    PortalLink portalLink;
    private CustomPortalBuilder() {
        portalLink = new PortalLink();
    }
    public static CustomPortalBuilder beginPortal(){
        return new CustomPortalBuilder();
    }
    public void registerPortal() {
        CustomPortalApiRegistry.addPortal(Registry.BLOCK.get(portalLink.block), portalLink);
    }

    public CustomPortalBuilder frameBlock(Identifier blockID) {
        portalLink.block = blockID;
        return this;
    }

    public CustomPortalBuilder frameBlock(Block block) {
        portalLink.block = Registry.BLOCK.getId(block);
        return this;
    }

    public CustomPortalBuilder destDimID(Identifier dimID) {
        portalLink.dimID = dimID;
        return this;
    }

    public CustomPortalBuilder tintColor(int color) {
        portalLink.colorID = color;
        return this;
    }

    public CustomPortalBuilder tintColor(int r, int g, int b) {
        portalLink.colorID = ColorUtil.getColorFromRGB(r, g, b);
        return this;
    }

    public CustomPortalBuilder ignitionSource(PortalIgnitionSource ignitionSource) {
        portalLink.portalIgnitionSource = ignitionSource;
        return this;
    }

    public CustomPortalBuilder lightWithWater() {
        portalLink.portalIgnitionSource = PortalIgnitionSource.WATER;
        return this;
    }

    public CustomPortalBuilder lightWithItem(Item item) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.ItemUseSource(item);
        return this;
    }

    public CustomPortalBuilder lightWithFluid(Fluid fluid) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.FluidSource(fluid);
        return this;
    }

    public CustomPortalBuilder customIgnitionSource(Identifier customSourceID) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.CustomSource(customSourceID);
        return this;
    }

    public CustomPortalBuilder forcedSize(int width, int height) {
        portalLink.forcedWidth = width;
        portalLink.forcedHeight = height;
        return this;
    }

    public CustomPortalBuilder customPortalBlock(CustomPortalBlock portalBlock) {
        portalLink.setPortalBlock(portalBlock);
        return this;
    }

    public CustomPortalBuilder returnDim(Identifier dimID, boolean onlyIgnitableInReturnDim) {
        portalLink.returnDimID = dimID;
        portalLink.onlyIgnitableInReturnDim = onlyIgnitableInReturnDim;
        return this;
    }
    public CustomPortalBuilder onlyLightInOverworld(){
        portalLink.onlyIgnitableInReturnDim = true;
        return this;
    }
}
