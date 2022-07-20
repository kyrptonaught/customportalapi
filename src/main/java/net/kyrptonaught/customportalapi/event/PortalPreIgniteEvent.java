package net.kyrptonaught.customportalapi.event;

import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface PortalPreIgniteEvent {
    boolean attemptLight(PlayerEntity player, World world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource portalIgnitionSource);
}
