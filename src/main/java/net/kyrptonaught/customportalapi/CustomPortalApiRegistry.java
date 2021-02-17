package net.kyrptonaught.customportalapi;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.HashMap;


public class CustomPortalApiRegistry {
    public static HashMap<Block, PortalLink> portals = new HashMap<>();
    public static HashMap<Block, PointOfInterestType> PORTAL_POIs = new HashMap<>();

    public static int getColorFromRGB(int r, int g, int b) {
        return ColorUtil.getColorFromRGB(r, g, b);
    }

    public static boolean isCustomPortalPOI(PointOfInterestType poi) {
        return CustomPortalApiRegistry.PORTAL_POIs.containsValue(poi);
    }

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (frameBlock == null) CustomPortalsMod.logError("Frameblock is null");
        if (link.getPortalBlock() == null) CustomPortalsMod.logError("Portal block is null");
        if (link.portalIgnitionSource == null) CustomPortalsMod.logError("Portal ignition source is null");
        if (link.dimID == null) CustomPortalsMod.logError("Dimmension is null");
        if (CustomPortalsMod.getDefaultPortalBlock() == null)
            CustomPortalsMod.logError("Built in CustomPortalBlock is null");

        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            CustomPortalsMod.logError("A portal(or the nether portal) is already registered with a frame of: " + frameBlock);
        } else {
            portals.put(frameBlock, link);
            Identifier POI_ID = new Identifier(CustomPortalsMod.MOD_ID, Registry.BLOCK.getId(link.getPortalBlock()).getPath() + "poi");
            if (!Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(POI_ID).isPresent())//why tf is .containsID client only?
                PORTAL_POIs.putIfAbsent(link.getPortalBlock(), PointOfInterestHelper.register(POI_ID, 0, 1, link.getPortalBlock()));
        }
    }

    @Deprecated
    public static void addPortal(Block frameBlock, Identifier dimID, int portalColor) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalColor);
        addPortal(frameBlock, link);
    }

    @Deprecated
    public static void addPortal(Block frameBlock, Block ignitionBlock, Identifier dimID, int portalColor) {
        PortalIgnitionSource pis = ignitionBlock.equals(Blocks.WATER) ? PortalIgnitionSource.FluidSource(Fluids.WATER) : PortalIgnitionSource.FIRE;
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalColor);
        link.portalIgnitionSource = pis;
        addPortal(frameBlock, link);
    }

    @Deprecated //mostly keeping for the aether
    public static void addPortal(Block frameBlock, Block ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int portalTint) {
        PortalIgnitionSource ignitionSource = ignitionBlock.equals(Blocks.WATER) ? PortalIgnitionSource.FluidSource(Fluids.WATER) : PortalIgnitionSource.FIRE;
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalTint);
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, CustomPortalBlock portalBlock, Identifier dimID, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, CustomPortalBlock portalBlock, Identifier dimID, int forcePortalWidth, int forcePortalHeight, int r, int g, int b) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, getColorFromRGB(r, g, b));
        link.portalIgnitionSource = ignitionSource;
        link.setPortalBlock(portalBlock);
        link.forcedWidth = forcePortalWidth;
        link.forcedHeight = forcePortalHeight;
        addPortal(frameBlock, link);
    }
}
