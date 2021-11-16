package net.kyrptonaught.customportalapi.portal.frame;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class PortalFrameTester {
    protected HashSet<Block> VALID_FRAME = null;
    protected int foundPortalBlocks;
    public BlockPos lowerCorner;
    protected WorldAccess world;

    public abstract PortalFrameTester init(WorldAccess world, BlockPos blockPos, Direction.Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getNewPortal(WorldAccess worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations);

    public abstract Optional<PortalFrameTester> getOrEmpty(WorldAccess worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations);

    public abstract boolean isAlreadyLitPortalFrame();

    public abstract boolean isValidFrame();

    public abstract void lightPortal(Block frameBlock);

    public abstract void createPortal(World world, BlockPos pos, BlockState frameBlock, Direction.Axis axis);

    public abstract boolean isRequestedSize(int attemptWidth, int attemptHeight);

    public abstract BlockLocating.Rectangle getRectangle();

    public abstract Direction.Axis getAxis1();

    public abstract Direction.Axis getAxis2();

    public abstract BlockPos doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis);

    public abstract Vec3d getEntityOffsetInPortal(BlockLocating.Rectangle arg, Entity entity, Direction.Axis portalAxis);

    public abstract TeleportTarget getTPTargetInPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d prevOffset, Entity entity);

    protected BlockPos getLowerCorner(BlockPos blockPos, Direction.Axis axis1, Direction.Axis axis2) {
        if (!validStateInsidePortal(world.getBlockState(blockPos), VALID_FRAME))
            return null;
        int offsetX = 1;
        while (validStateInsidePortal(world.getBlockState(blockPos.offset(axis1, -offsetX)), VALID_FRAME)) {
            offsetX++;
            if (offsetX > 20) return null;
        }
        blockPos = blockPos.offset(axis1, -(offsetX - 1));
        int offsetY = 1;
        while (blockPos.getY() - offsetY > 0 && validStateInsidePortal(world.getBlockState(blockPos.offset(axis2, -offsetY)), VALID_FRAME)) {
            offsetY++;
            if (offsetY > 20) return null;
        }
        return blockPos.offset(axis2, -(offsetY - 1));
    }

    protected int getSize(Direction.Axis axis, int minSize, int maxSize) {
        for (int i = 1; i <= maxSize; i++) {
            BlockState blockState = this.world.getBlockState(this.lowerCorner.offset(axis, i));
            if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                if (VALID_FRAME.contains(blockState.getBlock())) {
                    return i >= minSize ? i : 0;

                }
                break;
            }
        }
        return 0;
    }

    protected boolean checkForValidFrame(Direction.Axis axis1, Direction.Axis axis2, int size1, int size2) {
        BlockPos checkPos = lowerCorner.mutableCopy();
        for (int i = 0; i < size1; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.offset(axis2, -1)).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.offset(axis2, size2)).getBlock()))
                return false;
            checkPos = checkPos.offset(axis1, 1);
        }
        checkPos = lowerCorner.mutableCopy();
        for (int i = 0; i < size2; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.offset(axis1, -1)).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.offset(axis1, size1)).getBlock()))
                return false;
            checkPos = checkPos.offset(axis2, 1);
        }
        return true;
    }

    protected void countExistingPortalBlocks(Direction.Axis axis1, Direction.Axis axis2, int size1, int size2) {
        for (int i = 0; i < size1; i++)
            for (int j = 0; j < size2; j++)
                if (CustomPortalHelper.isInstanceOfCustomPortal(world.getBlockState(this.lowerCorner.offset(axis1, i).offset(axis2, j))))
                    foundPortalBlocks++;
    }

    public static boolean validStateInsidePortal(BlockState blockState, HashSet<Block> foundations) {
        PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
        for (Block block : foundations) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null) {
                ignitionSource = link.portalIgnitionSource;
                break;
            }
        }
        if (blockState.isAir() || CustomPortalHelper.isInstanceOfCustomPortal(blockState))
            return true;
        if (ignitionSource == PortalIgnitionSource.FIRE)
            return blockState.isIn(BlockTags.FIRE);
        if (ignitionSource.isWater())
            return blockState.getFluidState().isIn(FluidTags.WATER);
        if (ignitionSource.isLava())
            return blockState.getFluidState().isIn(FluidTags.LAVA);
        if (ignitionSource.sourceType == PortalIgnitionSource.SourceType.FLUID) {
            return Registry.FLUID.getId(blockState.getFluidState().getFluid()).equals(ignitionSource.ignitionSourceID);
        }
        return false;
    }

    @FunctionalInterface
    public interface PortalFrameTesterFactory {
        PortalFrameTester createInstanceOfPortalFrameTester();
    }
}
