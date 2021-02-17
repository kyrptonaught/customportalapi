package net.kyrptonaught.customportalapi.portal;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldAccess;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public class CustomAreaHelper {
    private final HashSet<Block> VALID_FRAME;

    private final WorldAccess world;
    private final Direction.Axis axis;
    private final Direction negativeDir;
    private int foundPortalBlocks;
    private BlockPos lowerCorner;
    private int height;
    private final int width;

    public CustomAreaHelper(WorldAccess world, BlockPos blockPos, Direction.Axis axis, HashSet<Block> foundations) {
        VALID_FRAME = foundations;
        this.world = world;
        this.axis = axis;
        this.negativeDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.lowerCorner = this.method_30492(blockPos);
        if (this.lowerCorner == null) {
            this.lowerCorner = blockPos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.method_30495();
            if (this.width > 0) {
                this.height = this.method_30496();
            }
        }
    }

    public static Optional<CustomAreaHelper> method_30485(WorldAccess worldAccess, BlockPos blockPos, Direction.Axis axis, HashSet<Block> foundations) {
        return method_30486(worldAccess, blockPos, (CustomAreaHelper) -> {
            return CustomAreaHelper.isValid() && CustomAreaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public static Optional<CustomAreaHelper> method_30486(WorldAccess worldAccess, BlockPos blockPos, Predicate<CustomAreaHelper> predicate, Direction.Axis axis, HashSet<Block> foundations) {
        Optional<CustomAreaHelper> optional = Optional.of(new CustomAreaHelper(worldAccess, blockPos, axis, foundations)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new CustomAreaHelper(worldAccess, blockPos, axis2, foundations)).filter(predicate);
        }
    }

    private BlockPos method_30492(BlockPos blockPos) {
        for (int i = Math.max(0, blockPos.getY() - 21); blockPos.getY() > i && validStateInsidePortal(this.world.getBlockState(blockPos.down()), VALID_FRAME); blockPos = blockPos.down()) {
        }
        Direction direction = this.negativeDir.getOpposite();
        int j = this.method_30493(blockPos, direction) - 1;
        return j < 0 ? null : blockPos.offset(direction, j);
    }

    private int method_30495() {
        int i = this.method_30493(this.lowerCorner, this.negativeDir);
        return i >= 2 && i <= 21 ? i : 0;
    }

    private int method_30493(BlockPos blockPos, Direction direction) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int i = 0; i <= 21; ++i) {
            mutable.set(blockPos).move(direction, i);
            BlockState blockState = this.world.getBlockState(mutable);
            if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                if (VALID_FRAME.contains(blockState.getBlock())) {
                    return i;
                }
                break;
            }

            BlockState blockState2 = this.world.getBlockState(mutable.move(Direction.DOWN));
            if (!VALID_FRAME.contains(blockState2.getBlock())) {
                break;
            }
        }

        return 0;
    }

    private int method_30496() {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i = this.method_30490(mutable);
        return i >= 3 && i <= 21 && this.method_30491(mutable, i) ? i : 0;
    }

    private boolean method_30491(BlockPos.Mutable mutable, int i) {
        for (int j = 0; j < this.width; ++j) {
            BlockPos.Mutable mutable2 = mutable.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, j);
            if (!VALID_FRAME.contains(this.world.getBlockState(mutable2).getBlock())) {
                return false;
            }
        }

        return true;
    }

    private int method_30490(BlockPos.Mutable mutable) {
        for (int i = 0; i < 21; ++i) {
            mutable.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, -1);
            if (!VALID_FRAME.contains(this.world.getBlockState(mutable).getBlock())) {
                return i;
            }

            mutable.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, this.width);
            if (!VALID_FRAME.contains(this.world.getBlockState(mutable).getBlock())) {
                return i;
            }

            for (int j = 0; j < this.width; ++j) {
                mutable.set(this.lowerCorner).move(Direction.UP, i).move(this.negativeDir, j);
                BlockState blockState = this.world.getBlockState(mutable);
                if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                    return i;
                }

                if (blockState.getBlock() instanceof CustomPortalBlock) {
                    ++this.foundPortalBlocks;
                }
            }
        }

        return 21;
    }

    private static boolean validStateInsidePortal(BlockState blockState, HashSet<Block> foundations) {
        PortalIgnitionSource ignitionSource = PortalIgnitionSource.FIRE;
        for (Block block : foundations) {
            if (CustomPortalApiRegistry.portals.containsKey(block)) {
                ignitionSource = CustomPortalApiRegistry.portals.get(block).portalIgnitionSource;
                break;
            }
        }
        if (blockState.isAir() || blockState.getBlock() instanceof CustomPortalBlock)
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

    public boolean wasAlreadyValid() {
        return this.isValid() && this.foundPortalBlocks == this.width * this.height;
    }

    public boolean isValid() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortal(Block frameBlock) {
        BlockState blockState = CustomPortalApiRegistry.portals.containsKey(frameBlock) ?
                CustomPortalApiRegistry.portals.get(frameBlock).getPortalBlock().getDefaultState().with(NetherPortalBlock.AXIS, axis)
                : CustomPortalsMod.getDefaultPortalBlock().getDefaultState().with(NetherPortalBlock.AXIS, axis);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(this.negativeDir, this.width - 1)).forEach((blockPos) -> {
            this.world.setBlockState(blockPos, blockState, 18);
        });
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}