package net.kyrptonaught.customportalapi;

import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PerWorldPortals {
    private static final Set<Block> worldPortals = ConcurrentHashMap.newKeySet();

    public static void removeOldPortalsFromRegistry() {
        for (Block block : worldPortals) {
            CustomPortalApiRegistry.portals.remove(block);
        }
        worldPortals.clear();
    }

    public static void registerWorldPortal(PortalLink portalLink) {
        if (!CustomPortalApiRegistry.portals.containsKey(Registry.BLOCK.get(portalLink.block))) {
            Block blockId = Registry.BLOCK.get(portalLink.block);
            worldPortals.add(blockId);
            CustomPortalApiRegistry.addPortal(blockId, portalLink);
        }
    }
}
