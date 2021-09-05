package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class CustomPortalHelper {
    public static boolean isInstanceOfCustomPortal(BlockView world, BlockPos pos) {
        return isInstanceOfCustomPortal(world.getBlockState(pos));
    }

    public static boolean isInstanceOfCustomPortal(BlockState state) {
        return state.getBlock() instanceof CustomPortalBlock;
    }

    public static Block getPortalBase(BlockView world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos)) {
            Direction.Axis axis = getAxisFrom(world.getBlockState(pos));

            if (!isInstanceOfCustomPortal(world, moveTowardsFrame(pos, axis, false)))
                return world.getBlockState(moveTowardsFrame(pos, axis, false)).getBlock();
            if (!isInstanceOfCustomPortal(world, moveTowardsFrame(pos, axis, true)))
                return world.getBlockState(moveTowardsFrame(pos, axis, true)).getBlock();

            if (axis == Direction.Axis.Y) axis = Direction.Axis.Z;

            if (!isInstanceOfCustomPortal(world, pos.offset(axis, 1)))
                return world.getBlockState(pos.offset(axis, 1)).getBlock();
            if (!isInstanceOfCustomPortal(world, pos.offset(axis, -1)))
                return world.getBlockState(pos.offset(axis, -1)).getBlock();
        }
        if (pos.getY() < 0 || world.getBlockState(pos).isAir()) {
            return null;
        }
        Direction.Axis axis = getAxisFrom(world.getBlockState(pos));
        return getPortalBase(world, moveTowardsFrame(pos, axis, false));
    }

    private static BlockPos moveTowardsFrame(BlockPos pos, Direction.Axis portalAxis, boolean positiveMove) {
        if (portalAxis.isHorizontal())
            return pos.offset(positiveMove ? Direction.UP : Direction.DOWN);
        return pos.offset(positiveMove ? Direction.EAST : Direction.WEST);
    }

    public static Direction.Axis getAxisFrom(BlockState state) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.get(CustomPortalBlock.AXIS);
        if (state.getBlock() instanceof NetherPortalBlock)
            return state.get(NetherPortalBlock.AXIS);
        return Direction.Axis.X;
    }

    public static BlockState blockWithAxis(BlockState state, Direction.Axis axis) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.with(CustomPortalBlock.AXIS, axis);
        return state;
    }
}
