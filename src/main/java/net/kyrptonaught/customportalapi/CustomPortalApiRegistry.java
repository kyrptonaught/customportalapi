package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


public class CustomPortalApiRegistry {
    protected static ConcurrentHashMap<Block, PortalLink> portals = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Identifier, PortalFrameTester.PortalFrameTesterFactory> PortalFrameTesters = new ConcurrentHashMap<>();

    public static PortalLink getPortalLinkFromBase(Block baseBlock) {
        if (baseBlock == null) return null;
        if (portals.containsKey(baseBlock)) return portals.get(baseBlock);
        return null;
    }

    public static Collection<PortalLink> getAllPortalLinks() {
        return portals.values();
    }

    public static int getColorFromRGB(int r, int g, int b) {
        return ColorUtil.getColorFromRGB(r, g, b);
    }


    public static void registerPortalFrameTester(Identifier frameTesterID, PortalFrameTester.PortalFrameTesterFactory createPortalFrameTester) {
        PortalFrameTesters.put(frameTesterID, createPortalFrameTester);
    }

    public static PortalFrameTester.PortalFrameTesterFactory getPortalFrameTester(Identifier frameTesterID) {
        return PortalFrameTesters.getOrDefault(frameTesterID, null);
    }

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (frameBlock == null) CustomPortalsMod.logError("Frameblock is null");
        if (link.getPortalBlock() == null) CustomPortalsMod.logError("Portal block is null");
        if (link.portalIgnitionSource == null) CustomPortalsMod.logError("Portal ignition source is null");
        if (link.dimID == null) CustomPortalsMod.logError("Dimension is null");
        if (CustomPortalsMod.dims.size() > 0 && !CustomPortalsMod.dims.containsKey(link.dimID))
            CustomPortalsMod.logError("Dimension not found");
        CustomPortalsMod.dims.keySet().forEach(System.out::println);
        if (CustomPortalsMod.getDefaultPortalBlock() == null)
            CustomPortalsMod.logError("Built in CustomPortalBlock is null");

        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            CustomPortalsMod.logError("A portal(or the nether portal) is already registered with a frame of: " + frameBlock);
        } else {
            portals.put(frameBlock, link);
        }
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, Identifier dimID, int portalColor) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalColor);
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, Block ignitionBlock, Identifier dimID, int portalColor) {
        PortalIgnitionSource pis = ignitionBlock.equals(Blocks.WATER) ? PortalIgnitionSource.FluidSource(Fluids.WATER) : PortalIgnitionSource.FIRE;
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalColor);
        link.portalIgnitionSource = pis;
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, Block ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int portalTint) {
        PortalIgnitionSource ignitionSource = ignitionBlock.equals(Blocks.WATER) ? PortalIgnitionSource.FluidSource(Fluids.WATER) : PortalIgnitionSource.FIRE;
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalTint);
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, CustomPortalBlock portalBlock, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        addPortal(frameBlock, link);
    }

    /**
     * @deprecated CustomPortalApiRegistry is being phased out and replaced with {@link net.kyrptonaught.customportalapi.api.CustomPortalBuilder} instead for more flexibility
     */
    @Deprecated
    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, CustomPortalBlock portalBlock, Identifier dimID, int forcePortalWidth, int forcePortalHeight, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        link.forcedWidth = forcePortalWidth;
        link.forcedHeight = forcePortalHeight;
        addPortal(frameBlock, link);
    }
}
