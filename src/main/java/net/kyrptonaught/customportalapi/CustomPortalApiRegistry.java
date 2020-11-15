package net.kyrptonaught.customportalapi;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.HashMap;


public class CustomPortalApiRegistry {
    public static HashMap<Block, PortalLink> portals = new HashMap<>();
    public static HashMap<Block, PointOfInterestType> PORTAL_POIs = new HashMap<>();
    //public static HashMap<Block, PortalLink> ignitions = new HashMap<>();

    public static void addPortal(Block frameBlock, PortalLink link) {
        if (portals.containsKey(frameBlock)) {
            System.out.println("ERROR: A portal is already registered with a frame of: " + frameBlock);
        } else {
            portals.put(frameBlock, link);
            PORTAL_POIs.putIfAbsent(link.portalBlock, PointOfInterestHelper.register(new Identifier(CustomPortalsMod.MOD_ID, Registry.BLOCK.getId(link.portalBlock).getPath() + "poi"), 0, 1, link.portalBlock));
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
}
