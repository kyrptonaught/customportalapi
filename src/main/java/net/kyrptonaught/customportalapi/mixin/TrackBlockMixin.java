package net.kyrptonaught.customportalapi.mixin;

import java.util.Random;

import com.google.common.base.Predicates;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.text.MutableText;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.NetherPortalBlock;

import net.minecraft.block.entity.BlockEntity;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.TeleportTarget;
import net.minecraft.util.math.Box;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.trains.track.TrackPropagator;
import com.simibubi.create.content.trains.track.TrackShape;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.EnumProperty;


/**
 * Mixin applied to the TrackBlock class in the Create mod so that it can recognize and use customportalapi portals.
 */
@Mixin(TrackBlock.class)
public abstract class TrackBlockMixin {


    @Shadow
    protected abstract void connectToNether(ServerWorld level, BlockPos pos, BlockState state);

    @Shadow
    @Final
    public static final EnumProperty<TrackShape> SHAPE = EnumProperty.of("shape", TrackShape.class);

    @Shadow
    @Final
    public static final BooleanProperty HAS_BE = BooleanProperty.of("turn");

    protected void connectToOtherDimension(ServerWorld level, BlockPos pos, BlockState state) {
        TrackShape shape = state.get(TrackBlock.SHAPE);
        Axis portalTest = shape == TrackShape.XO ? Axis.X : shape == TrackShape.ZO ? Axis.Z : null;
        if (portalTest == null)
            return;

        boolean pop = false;
        String fail = null;
        BlockPos failPos = null;

        for(Direction d : Iterate.directionsInAxis(portalTest)) {
            BlockPos portalPos = pos.offset(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!(portalState.getBlock() instanceof NetherPortalBlock) && !(portalState.getBlock() instanceof CustomPortalBlock))
                continue;

            if(portalState.getBlock() instanceof NetherPortalBlock) {
                connectToNether(level, pos, state);
            }
            if(portalState.getBlock() instanceof CustomPortalBlock) {
                connectToCustomPortal(level, pos, state);
            }
        }
    }

    protected void connectToCustomPortal(ServerWorld level, BlockPos pos, BlockState state) {
        TrackShape shape = state.get(TrackBlock.SHAPE);
        Axis portalTest = shape == TrackShape.XO ? Axis.X : shape == TrackShape.ZO ? Axis.Z : null;
        if (portalTest == null)
            return;

        boolean pop = false;
        String fail = null;
        BlockPos failPos = null;

        for(Direction d : Iterate.directionsInAxis(portalTest)) {
            BlockPos portalPos = pos.offset(d);
            BlockState portalState = level.getBlockState(portalPos);
            if (!(portalState.getBlock() instanceof CustomPortalBlock))
                continue;

            pop = true;
            Pair<ServerWorld, BlockFace> otherSide = getOtherSide(level, new BlockFace(pos, d));
            if (otherSide == null) {
                fail = "missing";
                continue;
            }

            ServerWorld otherLevel = otherSide.getFirst();
            BlockFace otherTrack = otherSide.getSecond();
            BlockPos otherTrackPos = otherTrack.getPos();
            BlockState existing = otherLevel.getBlockState(otherTrackPos);
            if(!existing.getMaterial()
                    .isReplaceable()) {
                fail = "blocked";
                failPos = otherTrackPos;
                continue;
            }

            level.setBlockState(pos, state.with(SHAPE, TrackShape.asPortal(d))
                    .with(HAS_BE, true), 3);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TrackBlockEntity tbe)
                tbe.bind(otherLevel.getRegistryKey(), otherTrackPos);

            otherLevel.setBlockState(otherTrackPos, state.with(SHAPE, TrackShape.asPortal(otherTrack.getFace()))
                    .with(HAS_BE, true), 3);
            BlockEntity otherBE = otherLevel.getBlockEntity(otherTrackPos);
            if (otherBE instanceof TrackBlockEntity tbe)
                tbe.bind(level.getRegistryKey(), pos);

            pop = false;
        }

        if (!pop)
            return;

        level.breakBlock(pos, true);

        if (fail == null)
            return;
        PlayerEntity player = level.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 10, Predicates.alwaysTrue());
        if (player == null)
            return;
        player.sendMessage(Components.literal("<!> ").append(Lang.translateDirect("portal_track.failed"))
                .formatted(Formatting.GOLD), false);
        MutableText component =
                failPos != null ? Lang.translateDirect("portal_track." + fail, failPos.getX(), failPos.getY(), failPos.getZ())
                        : Lang.translateDirect("portal_track." + fail);
        player.sendMessage(Components.literal(" - ").formatted(Formatting.GRAY)
                .append(component.styled(st -> st.withColor(0xFFD3B4))), false);
    }

    @Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    private void CPAtrackBlockTick(BlockState state, ServerWorld level, BlockPos pos, Random p_60465_, CallbackInfo ci) {
        TrackPropagator.onRailAdded(level, pos, state);
        if (!state.get(SHAPE)
                .isPortal())
            connectToOtherDimension(level, pos, state);
        ci.cancel();
    }

    protected Pair<ServerWorld, BlockFace> getOtherSide(ServerWorld level, BlockFace inboundTrack) {
        BlockPos portalPos = inboundTrack.getConnectedPos();
        BlockState portalState = level.getBlockState(portalPos);
        if (!(portalState.getBlock() instanceof NetherPortalBlock) && !(portalState.getBlock() instanceof CustomPortalBlock))
            return null;

        MinecraftServer minecraftserver = level.getServer();
        ServerWorld otherLevel = null;
        if(portalState.getBlock() instanceof NetherPortalBlock) {
            RegistryKey<World> resourcekey = level.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
            otherLevel = minecraftserver.getWorld(resourcekey);
        } else {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock)portalState.getBlock()).getPortalBase(level, portalPos));
            RegistryKey<World> resourcekey = level.getRegistryKey() == CustomPortalsMod.dims.get(link.dimID) ? CustomPortalsMod.dims.get(link.returnDimID) : CustomPortalsMod.dims.get(link.dimID);;
            otherLevel = minecraftserver.getWorld(resourcekey);
        }
        if (otherLevel == null)
            return null;

        PortalForcer teleporter = otherLevel.getPortalForcer();
        TeleportTarget portalinfo = null;
        SuperGlueEntity probe = new SuperGlueEntity(level, new Box(portalPos));
        probe.setYaw(inboundTrack.getFace()
                .asRotation());
        if(portalState.getBlock() instanceof NetherPortalBlock) {
            portalinfo = probe.getTeleportTarget(otherLevel);
        } else {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock)portalState.getBlock()).getPortalBase(level, portalPos));
            portalinfo = CustomTeleporter.customTPTarget(otherLevel, probe, portalPos, ((CustomPortalBlock)portalState.getBlock()).getPortalBase(level, portalPos), link.getFrameTester());
        }
        if (portalinfo == null)
            return null;

        BlockPos otherPortalPos = new BlockPos(portalinfo.position);
        BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
        if (!(otherPortalState.getBlock() instanceof NetherPortalBlock) && !(otherPortalState.getBlock() instanceof CustomPortalBlock))
            return null;

        Direction targetDirection = inboundTrack.getFace();
        if (targetDirection.getAxis() == CustomPortalHelper.getAxisFrom(otherPortalState)) {
            targetDirection = targetDirection.rotateYClockwise();
        }
        BlockPos otherPos = otherPortalPos.offset(targetDirection);
        return Pair.of(otherLevel, new BlockFace(otherPos, targetDirection.getOpposite()));
    }
}