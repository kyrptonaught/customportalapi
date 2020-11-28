package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.util.Identifier;

public class PortalLink {
    public Identifier block;
    public Identifier ignitionBlock = new Identifier("fire");
    public CustomPortalBlock portalBlock = CustomPortalsMod.portalBlock;
    public Identifier dimID;
    public Identifier returnDimID = new Identifier("overworld");
    public int colorID;

    public PortalLink(Identifier blockID, Identifier dimID, int colorID) {
        this.block = blockID;
        this.dimID = dimID;
        this.colorID = colorID;
    }

    public PortalLink(Identifier blockID, Identifier dimID, int r, int g, int b) {
        this(blockID, dimID, ColorUtil.getColorFromRGB(r, g, b));
    }

    public PortalLink(Identifier blockID, Identifier ignitionBlock, Identifier dimID, int colorID) {
        this(blockID, dimID, colorID);
        this.ignitionBlock = ignitionBlock;
    }

    public PortalLink(Identifier blockID, Identifier ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int colorID) {
        this(blockID, ignitionBlock, dimID, colorID);
        this.portalBlock = portalBlock;
    }

    public PortalLink(Identifier blockID, Identifier ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, Identifier returnDimId, int colorID) {
        this(blockID, ignitionBlock, dimID, colorID);
        this.portalBlock = portalBlock;
        this.returnDimID = returnDimId;
    }
}
