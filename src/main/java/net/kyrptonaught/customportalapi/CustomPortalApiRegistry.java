package net.kyrptonaught.customportalapi;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (frameBlock == null) CustomPortalsMod.logError("frameblock is null");
        if (link.portalBlock == null) CustomPortalsMod.logError("portal block is null");
        if (link.ignitionBlock == null) CustomPortalsMod.logError("ignition block is null");
        if (CustomPortalsMod.portalBlock == null) CustomPortalsMod.logError("Built in CustomPortalBlock is null");
        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            CustomPortalsMod.logError("A portal(or the nether portal) is already registered with a frame of: " + frameBlock);
        } else {
            portals.put(frameBlock, link);
            Identifier POI_ID = new Identifier(CustomPortalsMod.MOD_ID, Registry.BLOCK.getId(link.portalBlock).getPath() + "poi");

            if (!Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(POI_ID).isPresent())//why tf is .containsID client only?
                PORTAL_POIs.putIfAbsent(link.portalBlock, PointOfInterestHelper.register(POI_ID, 0, 1, link.portalBlock));
        }
    }

    public static void addPortal(Block frameBlock, Identifier dimID, int portalColor) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), dimID, portalColor);
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, Block ignitionBlock, Identifier dimID, int portalColor) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), Registry.BLOCK.getId(ignitionBlock), dimID, portalColor);
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, Block ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int portalColor) {
        PortalLink link = new PortalLink(Registry.BLOCK.getId(frameBlock), Registry.BLOCK.getId(ignitionBlock), portalBlock, dimID, portalColor);
        addPortal(frameBlock, link);
    }

    public static void addPortal(Block frameBlock, Identifier dimID, int r, int g, int b) {
        addPortal(frameBlock, dimID, getColorFromRGB(r, g, b));
    }

    public static void addPortal(Block frameBlock, Block ignitionBlock, Identifier dimID, int r, int g, int b) {
        addPortal(frameBlock, ignitionBlock, dimID, getColorFromRGB(r, g, b));
    }

    public static void addPortal(Block frameBlock, Block ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int r, int g, int b) {
        addPortal(frameBlock, ignitionBlock, portalBlock, dimID, getColorFromRGB(r, g, b));
    }
}
