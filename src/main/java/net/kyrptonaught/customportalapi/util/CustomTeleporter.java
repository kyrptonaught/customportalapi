package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.class_5459;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.AreaHelper;
import net.minecraft.world.dimension.DimensionType;

public class CustomTeleporter {

    public static void TPToDim(World world, Entity entity, Block portalBase, BlockPos portalPoss) {
        if (!CustomPortalApiRegistry.portals.containsKey(portalBase)) return;
        PortalLink link = CustomPortalApiRegistry.portals.get(portalBase);
        RegistryKey<World> destKey = world.getRegistryKey() == CustomPortalsMod.dims.get(link.dimID) ? CustomPortalsMod.dims.get(link.returnDimID) : CustomPortalsMod.dims.get(link.dimID);
        ServerWorld destination = ((ServerWorld) world).getServer().getWorld(destKey);
        if (destination == null) return;

        TeleportTarget target = customTPTarget(destination, entity, portalBase, portalPoss);
        if (entity instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity) entity).teleport(destination, target.position.x, target.position.y, target.position.z, target.yaw, target.pitch);
        } else {
            //copied from entity.moveToWorld(destination);
            entity.detach();
            Entity newEntity = entity.getType().create(destination);
            newEntity.copyFrom(entity);
            newEntity.refreshPositionAndAngles(target.position.x, target.position.y, target.position.z, target.yaw, newEntity.pitch);
            newEntity.setVelocity(target.velocity);
            destination.onDimensionChanged(newEntity);
            entity.remove();
        }
    }

    public static TeleportTarget customTPTarget(ServerWorld destination, Entity entity, Block portalFrame, BlockPos portalPos) {
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = Math.max(-2.9999872E7D, worldBorder.getBoundWest() + 16.0D);
        double e = Math.max(-2.9999872E7D, worldBorder.getBoundNorth() + 16.0D);
        double f = Math.min(2.9999872E7D, worldBorder.getBoundEast() - 16.0D);
        double g = Math.min(2.9999872E7D, worldBorder.getBoundSouth() - 16.0D);
        double h = DimensionType.method_31109(entity.world.getDimension(), destination.getDimension());
        BlockPos blockPos3 = new BlockPos(MathHelper.clamp(entity.getX() * h, d, f), entity.getY(), MathHelper.clamp(entity.getZ() * h, e, g));
        BlockState blockState = entity.world.getBlockState(portalPos);
        return PortalPlacer.findOrCreatePortal(destination, blockPos3, portalFrame, blockState.get(NetherPortalBlock.AXIS), destination.getRegistryKey() == World.NETHER).map((arg) -> {
            Direction.Axis axis2;
            Vec3d vec3d2;
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis2 = blockState.get(Properties.HORIZONTAL_AXIS);
                class_5459.class_5460 lv = class_5459.method_30574(portalPos, axis2, 21, Direction.Axis.Y, 21, (blockPos) ->
                        entity.world.getBlockState(blockPos) == blockState);
                vec3d2 = method_30633(axis2, lv, entity);
            } else {
                axis2 = Direction.Axis.X;
                vec3d2 = new Vec3d(0.5D, 0.0D, 0.0D);
            }

            return AreaHelper.method_30484(destination, arg, axis2, vec3d2, entity.getDimensions(entity.getPose()), entity.getVelocity(), entity.yaw, entity.pitch);
        }).orElse(idkWhereToPutYou(destination, entity, blockPos3));

    }

    protected static Vec3d method_30633(Direction.Axis axis, class_5459.class_5460 arg, Entity entity) {
        return AreaHelper.method_30494(arg, axis, entity.getPos(), entity.getDimensions(entity.getPose()));
    }

    protected static TeleportTarget idkWhereToPutYou(ServerWorld world, Entity entity, BlockPos pos) {
        BlockPos destinationPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
        return new TeleportTarget(new Vec3d(destinationPos.getX() + .5, destinationPos.getY(), destinationPos.getZ() + .5), entity.getVelocity(), entity.yaw, entity.pitch);
    }
}