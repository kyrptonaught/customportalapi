package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CustomPortalHelper {
    public static boolean isInstanceOfCustomPortal(World world, BlockPos pos) {
        return isInstanceOfCustomPortal(world.getBlockState(pos));
    }

    public static boolean isInstanceOfCustomPortal(BlockState state) {
        return state.getBlock() instanceof CustomPortalBlock;
    }

    public static boolean isInstanceOfPortalFrame(World world, BlockPos pos) {
        if (world.isInBuildLimit(pos))
            return CustomPortalApiRegistry.isRegisteredFrameBlock(world.getBlockState(pos));
        return false;
    }

    public static Block getPortalBase(World world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos)) {
            return ((CustomPortalBlock) world.getBlockState(pos).getBlock()).getPortalBase(world, pos);
        } else if (isInstanceOfPortalFrame(world, pos))
            return world.getBlockState(pos).getBlock();

        return Blocks.AIR;
    }

    public static Block getPortalBaseDefault(World world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos)) {
            Direction.Axis axis = getAxisFrom(world.getBlockState(pos));

            if (axis != Direction.Axis.Y) {
                if (isInstanceOfPortalFrame(world, pos.down()))
                    return world.getBlockState(pos.down()).getBlock();
                if (isInstanceOfPortalFrame(world, pos.up()))
                    return world.getBlockState(pos.up()).getBlock();
            } else axis = Direction.Axis.Z;

            if (isInstanceOfPortalFrame(world, pos.offset(axis, -1)))
                return world.getBlockState(pos.offset(axis, -1)).getBlock();
            if (isInstanceOfPortalFrame(world, pos.offset(axis, 1)))
                return world.getBlockState(pos.offset(axis, 1)).getBlock();

            return getPortalBaseDefault(world, pos.offset(axis, -1));
        } else if (isInstanceOfPortalFrame(world, pos))
            return world.getBlockState(pos).getBlock();

        return Blocks.AIR;
    }

    public static BlockPos getClosestFrameBlock(World world, BlockPos pos) {
        if (isInstanceOfPortalFrame(world, pos.down()))
            return pos.down();
        if (isInstanceOfPortalFrame(world, pos.east()))
            return pos.east();
        if (isInstanceOfPortalFrame(world, pos.west()))
            return pos.west();
        if (isInstanceOfPortalFrame(world, pos.north()))
            return pos.north();
        if (isInstanceOfPortalFrame(world, pos.south()))
            return pos.south();
        if (isInstanceOfPortalFrame(world, pos.up()))
            return pos.up();
        return pos;
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
