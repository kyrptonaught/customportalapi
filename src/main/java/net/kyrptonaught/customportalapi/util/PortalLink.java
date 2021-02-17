package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

public class PortalLink {
    public Identifier block;
    public PortalIgnitionSource portalIgnitionSource = PortalIgnitionSource.FIRE;
    private CustomPortalBlock portalBlock = CustomPortalsMod.portalBlock;
    public Identifier dimID;
    public Identifier returnDimID = new Identifier("overworld");
    public int colorID;
    public int forcedWidth, forcedHeight;

    public PortalLink(Identifier blockID, Identifier dimID, int colorID) {
        this.block = blockID;
        this.dimID = dimID;
        this.colorID = colorID;
    }

    public Block getPortalBlock() {
        return portalBlock;
    }

    public void setPortalBlock(CustomPortalBlock block) {
        this.portalBlock = block;
    }

    public boolean doesIgnitionMatch(PortalIgnitionSource attemptedSource) {
        return portalIgnitionSource.sourceType == attemptedSource.sourceType && portalIgnitionSource.ignitionSourceID == attemptedSource.ignitionSourceID;
    }

    public boolean isCorrectForcedSize(int attempWidth, int attemptHeight) {
        return ((forcedWidth == 0 || forcedWidth == attempWidth) && (forcedHeight == 0 || forcedHeight == attemptHeight));
    }
}
