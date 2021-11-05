package net.kyrptonaught.customportalapi.interfaces;

import net.minecraft.util.math.BlockPos;

public interface EntityInCustomPortal {

    int getTimeInPortal();

    boolean didTeleport();

    void setDidTP(boolean didTP);

    void tickInPortal(BlockPos portalPos);

    BlockPos getInPortalPos();
}
