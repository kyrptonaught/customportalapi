package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.util.Identifier;

public class PortalLink {
    public Identifier block;
    public Identifier ignitionBlock = new Identifier("fireblock");
    public CustomPortalBlock portalBlock = CustomPortalsMod.portalBlock;
    public Identifier dimID;
    public int colorID;

    public PortalLink(Identifier blockID, Identifier dimID, int colorID) {
        this.block = blockID;
        this.dimID = dimID;
        this.colorID = colorID;
    }

    public PortalLink(Identifier blockID, Identifier ignitionBlock, Identifier dimID, int colorID) {
        this(blockID, dimID, colorID);
        this.ignitionBlock = ignitionBlock;
    }

    public PortalLink(Identifier blockID, Identifier ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int colorID) {
        this(blockID, ignitionBlock, dimID, colorID);
        this.portalBlock = portalBlock;
    }
}
