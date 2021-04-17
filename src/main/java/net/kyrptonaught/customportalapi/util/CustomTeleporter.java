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
            entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
            ((ServerWorld) world).resetIdleTimeout();
            destination.resetIdleTimeout();
            // entity.remove();
        }
    }

    //copied fromm serverplayerentity
    public static void TPPlayer(ServerWorld destination, ServerPlayerEntity player, Block portalFrame, BlockPos portalPos) {
        ((ServerPlayerEntityTPAccessor) player).setinTeleportationState(true);
        ServerWorld serverWorld = player.getServerWorld();
        WorldProperties worldProperties = destination.getLevelProperties();
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(destination.getDimension(), destination.getRegistryKey(), BiomeAccess.hashSeed(destination.getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), destination.isDebugWorld(), destination.isFlat(), true));
        player.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        PlayerManager playerManager = player.server.getPlayerManager();
        playerManager.sendCommandTree(player);
        serverWorld.removePlayer(player, Entity.RemovalReason.CHANGED_DIMENSION);
        //player.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
        ((EntityInvoker) player).invokeunsetRemoved();
        TeleportTarget teleportTarget = customTPTarget(destination, player, portalFrame, portalPos);


        player.setWorld(destination);
        destination.onPlayerChangeDimension(player);
        player.yaw = teleportTarget.yaw % 360.0F;
        player.pitch = teleportTarget.pitch % 360.0F;
        player.refreshPositionAfterTeleport(teleportTarget.position.x, teleportTarget.position.y, teleportTarget.position.z);

        ((ServerPlayerEntityTPAccessor) player).invokeworldChanged(serverWorld);
        //player.interactionManager.setWorld(destination);
        player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        playerManager.sendWorldInfo(player, destination);
        playerManager.sendPlayerStatus(player);

        for (StatusEffectInstance statusEffectInstance : player.getStatusEffects()) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), statusEffectInstance));
        }

        player.networkHandler.sendPacket(new WorldEventS2CPacket(1032, BlockPos.ORIGIN, 0, false));
        ((ServerPlayerEntityTPAccessor) player).setinsyncedExperience(-1);
        ((ServerPlayerEntityTPAccessor) player).setinsyncedHealth(-1.0f);
        ((ServerPlayerEntityTPAccessor) player).setinsyncedFoodLevel(-1);
    }

    public static TeleportTarget customTPTarget(ServerWorld destination, Entity entity, Block portalFrame, BlockPos portalPos) {
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = Math.max(-2.9999872E7D, worldBorder.getBoundWest() + 16.0D);
        double e = Math.max(-2.9999872E7D, worldBorder.getBoundNorth() + 16.0D);
        double f = Math.min(2.9999872E7D, worldBorder.getBoundEast() - 16.0D);
        double g = Math.min(2.9999872E7D, worldBorder.getBoundSouth() - 16.0D);
        double h = DimensionType.getCoordinateScaleFactor(entity.world.getDimension(), destination.getDimension());
        BlockPos blockPos3 = new BlockPos(MathHelper.clamp(entity.getX() * h, d, f), entity.getY(), MathHelper.clamp(entity.getZ() * h, e, g));
        BlockState blockState = entity.world.getBlockState(portalPos);
        return PortalPlacer.findOrCreatePortal(destination, blockPos3, portalFrame, blockState.get(NetherPortalBlock.AXIS), destination.getRegistryKey() == World.NETHER).map((arg) -> {
            Direction.Axis axis2;
            Vec3d vec3d2;
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis2 = blockState.get(Properties.HORIZONTAL_AXIS);
                PortalUtil.Rectangle lv = PortalUtil.getLargestRectangle(portalPos, axis2, 21, Direction.Axis.Y, 21, (blockPos) ->
                        entity.world.getBlockState(blockPos) == blockState);
                vec3d2 = positionInPortal(axis2, lv, entity);
            } else {
                axis2 = Direction.Axis.X;
                vec3d2 = new Vec3d(0.5D, 0.0D, 0.0D);
            }

            return AreaHelper.getNetherTeleportTarget(destination, arg, axis2, vec3d2, entity.getDimensions(entity.getPose()), entity.getVelocity(), entity.yaw, entity.pitch);
        }).orElse(idkWhereToPutYou(destination, entity, blockPos3));

    }

    protected static Vec3d positionInPortal(Direction.Axis axis, PortalUtil.Rectangle arg, Entity entity) {
        return AreaHelper.entityPosInPortal(arg, axis, entity.getPos(), entity.getDimensions(entity.getPose()));
    }

    protected static TeleportTarget idkWhereToPutYou(ServerWorld world, Entity entity, BlockPos pos) {
        BlockPos destinationPos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
        return new TeleportTarget(new Vec3d(destinationPos.getX() + .5, destinationPos.getY(), destinationPos.getZ() + .5), entity.getVelocity(), entity.yaw, entity.pitch);
    }
}
