package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;


public class CustomPortalApiRegistry {
    protected static final ConcurrentHashMap<Block, PortalLink> portals = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Identifier, PortalFrameTester.PortalFrameTesterFactory> PortalFrameTesters = new ConcurrentHashMap<>();

    public static PortalLink getPortalLinkFromBase(Block baseBlock) {
        if (baseBlock == null) return null;
        if (portals.containsKey(baseBlock)) return portals.get(baseBlock);
        return null;
    }

    public static boolean isRegisteredFrameBlock(BlockState blockState) {
        return portals.containsKey(blockState.getBlock());
    }

    public static Collection<PortalLink> getAllPortalLinks() {
        return portals.values();
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
        if (CustomPortalsMod.getDefaultPortalBlock() == null)
            CustomPortalsMod.logError("Built in CustomPortalBlock is null");

        if (portals.containsKey(frameBlock) || frameBlock.equals(Blocks.OBSIDIAN)) {
            CustomPortalsMod.logError("A portal(or the nether portal) is already registered with a frame of: " + frameBlock);
        } else {
            portals.put(frameBlock, link);
        }
    }
}
