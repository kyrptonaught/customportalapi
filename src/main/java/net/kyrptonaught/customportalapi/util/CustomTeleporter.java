package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.interfaces.CustomTeleportingEntity;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.portalLinking.DimensionalBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

public class CustomTeleporter {

    public static void TPToDim(World world, Entity entity, Block portalBase, BlockPos portalPos) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(portalBase);
        if (link == null) return;
        if (link.executeBeforeTPEvent(entity) == SHOULDTP.CANCEL_TP)
            return;
        RegistryKey<World> destKey = world.getRegistryKey() == CustomPortalsMod.dims.get(link.dimID) ? CustomPortalsMod.dims.get(link.returnDimID) : CustomPortalsMod.dims.get(link.dimID);
        ServerWorld destination = ((ServerWorld) world).getServer().getWorld(destKey);
        if (destination == null) return;

        TeleportTarget target = customTPTarget(destination, entity, portalPos, portalBase, link.getFrameTester());

        ((CustomTeleportingEntity) entity).setCustomTeleportTarget(target);
        entity = entity.moveToWorld(destination);
        if(entity != null) {
            entity.setYaw(target.yaw);
            entity.setPitch(target.pitch);
            if (entity instanceof ServerPlayerEntity)
                entity.refreshPositionAfterTeleport(target.position);
            link.executePostTPEvent(entity);
        }
    }


    public static TeleportTarget customTPTarget(ServerWorld destinationWorld, Entity entity, BlockPos enteredPortalPos, Block frameBlock, PortalFrameTester.PortalFrameTesterFactory portalFrameTesterFactory) {
        Direction.Axis portalAxis = CustomPortalsMod.getAxisFrom(entity.getEntityWorld().getBlockState(enteredPortalPos));
        BlockLocating.Rectangle fromPortalRectangle = portalFrameTesterFactory.createInstanceOfPortalFrameTester().init(entity.getEntityWorld(), enteredPortalPos, portalAxis, frameBlock).getRectangle();
        DimensionalBlockPos destinationPos = CustomPortalsMod.portalLinkingStorage.getDestination(fromPortalRectangle.lowerLeft, entity.getEntityWorld().getRegistryKey());

        if (destinationPos != null && destinationPos.dimensionType.equals(destinationWorld.getRegistryKey().getValue())) {
            PortalFrameTester portalFrameTester = portalFrameTesterFactory.createInstanceOfPortalFrameTester().init(destinationWorld, destinationPos.pos, portalAxis, frameBlock);
            if (portalFrameTester.isValid()) {
                if (!portalFrameTester.wasAlreadyValid()) {
                    portalFrameTester.createPortal(frameBlock);
                }
                return buildTPTargetInDestPortal(portalFrameTester.getRectangle(), portalAxis, getOffset(fromPortalRectangle, entity), entity);
            }
        }
        return createDestinationPortal(destinationWorld, entity, portalAxis, fromPortalRectangle, frameBlock.getDefaultState());
    }

    public static TeleportTarget createDestinationPortal(ServerWorld destination, Entity entity, Direction.Axis axis, BlockLocating.Rectangle portalFramePos, BlockState frameBlock) {
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = Math.max(-2.9999872E7D, worldBorder.getBoundWest() + 16.0D);
        double e = Math.max(-2.9999872E7D, worldBorder.getBoundNorth() + 16.0D);
        double f = Math.min(2.9999872E7D, worldBorder.getBoundEast() - 16.0D);
        double g = Math.min(2.9999872E7D, worldBorder.getBoundSouth() - 16.0D);
        double h = DimensionType.getCoordinateScaleFactor(entity.world.getDimension(), destination.getDimension());
        BlockPos blockPos3 = new BlockPos(MathHelper.clamp(entity.getX() * h, d, f), entity.getY(), MathHelper.clamp(entity.getZ() * h, e, g));
        Optional<BlockLocating.Rectangle> portal = PortalPlacer.createDestinationPortal(destination, blockPos3, frameBlock, axis);
        if (portal.isPresent()) {
            CustomPortalsMod.portalLinkingStorage.createLink(portalFramePos.lowerLeft, entity.world.getRegistryKey(), portal.get().lowerLeft, destination.getRegistryKey());
            return buildTPTargetInDestPortal(portal.get(), axis, getOffset(portalFramePos, entity), entity);
        }
        return idkWhereToPutYou(destination, entity, blockPos3);
    }

    protected static Vec3d getOffset(BlockLocating.Rectangle arg, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = arg.width - entityDimensions.width;
        double height = arg.height - entityDimensions.height;

        double deltaX = MathHelper.getLerpProgress(entity.getX(), arg.lowerLeft.getX(), arg.lowerLeft.getX() + width);
        double deltaY = MathHelper.getLerpProgress(entity.getY(), arg.lowerLeft.getY(), arg.lowerLeft.getY() + height);
        double deltaZ = MathHelper.getLerpProgress(entity.getZ(), arg.lowerLeft.getZ(), arg.lowerLeft.getZ() + width);
        return new Vec3d(deltaX, deltaY, deltaZ);
    }

    public static TeleportTarget buildTPTargetInDestPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d offset, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = portalRect.width - entityDimensions.width;
        double height = portalRect.height - entityDimensions.height;
        double x = MathHelper.lerp(offset.x, portalRect.lowerLeft.getX(), portalRect.lowerLeft.getX() + width);
        double y = MathHelper.lerp(offset.y, portalRect.lowerLeft.getY(), portalRect.lowerLeft.getY() + height);
        double z = MathHelper.lerp(offset.z, portalRect.lowerLeft.getZ(), portalRect.lowerLeft.getZ() + width);
        if (portalAxis == Direction.Axis.X)
            z = portalRect.lowerLeft.getZ() + 0.5D;
        else if (portalAxis == Direction.Axis.Z)
            x = portalRect.lowerLeft.getX() + .5D;

        return new TeleportTarget(new Vec3d(x, y, z), entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }

    protected static TeleportTarget idkWhereToPutYou(ServerWorld world, Entity entity, BlockPos pos) {
        CustomPortalsMod.logError("Unable to find find tp location, forced to place on top of world");
        BlockPos destinationPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
        return new TeleportTarget(new Vec3d(destinationPos.getX() + .5, destinationPos.getY(), destinationPos.getZ() + .5), entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }
}