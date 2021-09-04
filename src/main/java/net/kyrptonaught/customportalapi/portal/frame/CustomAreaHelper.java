package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public class CustomAreaHelper extends PortalFrameTester {
    private Direction.Axis axis;
    private Direction negativeDir;
    private int height;
    private int width;

    private final int maxWidth = 21;
    private final int maxHeight = 21;

    public CustomAreaHelper() {

    }

    public PortalFrameTester init(WorldAccess world, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        this.axis = axis;
        this.negativeDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.lowerCorner = this.getLowerCorner(blockPos);
        this.foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            width = height = 1;
        } else {
            this.width = this.getWidth();
            if (this.width > 0) {
                this.height = this.getHeight();
                if (checkForValidFrame()) {
                    countExistingPortalBlocks();
                } else {
                    lowerCorner = null;
                    width = height = 1;
                }
            }
        }
        return this;
    }

    @Override
    public BlockLocating.Rectangle getRectangle() {
        return new BlockLocating.Rectangle(lowerCorner, width, height);
    }

    @Override
    public boolean doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis) {
        BlockLocating.Rectangle rect = BlockLocating.getLargestRectangle(attemptPos, axis, 4, Direction.Axis.Y, 5, blockPos -> {
            return !world.getBlockState(blockPos).getMaterial().isSolid();
        });
        System.out.println(rect.lowerLeft + " " + rect + width + " " + height);
        return false;
    }

    public Optional<PortalFrameTester> getNewPortal(WorldAccess worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(worldAccess, blockPos, (customAreaHelper) -> {
            return customAreaHelper.isValidFrame() && customAreaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(WorldAccess worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        Optional<PortalFrameTester> optional = Optional.of(new CustomAreaHelper().init(worldAccess, blockPos, axis, foundations)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new CustomAreaHelper().init(worldAccess, blockPos, axis2, foundations)).filter(predicate);
        }
    }

    private BlockPos getLowerCorner(BlockPos blockPos) {
        if (!validStateInsidePortal(world.getBlockState(blockPos), VALID_FRAME))
            return null;
        int offsetX = 1;
        while (validStateInsidePortal(world.getBlockState(blockPos.offset(negativeDir.getOpposite(), offsetX)), VALID_FRAME)) {
            offsetX++;
            if (offsetX > 20) return null;
        }
        blockPos = blockPos.offset(negativeDir.getOpposite(), offsetX - 1);
        int offsetY = 1;
        while (blockPos.getY() - offsetY > 0 && validStateInsidePortal(world.getBlockState(blockPos.down(offsetY)), VALID_FRAME)) {
            offsetY++;
            if (offsetY > 20) return null;
        }
        return blockPos.down(offsetY - 1);
    }

    private int getWidth() {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = 1; i <= maxWidth; i++) {
            mutable.set(this.lowerCorner).move(this.negativeDir, i);
            BlockState blockState = this.world.getBlockState(mutable);
            if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                if (VALID_FRAME.contains(blockState.getBlock())) {
                    return i >= 2 ? i : 0;

                }
                break;
            }
        }
        return 0;
    }

    private int getHeight() {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i = 1; i <= maxHeight; i++) {
            mutable.set(lowerCorner).move(Direction.UP, i);
            BlockState blockState = this.world.getBlockState(mutable);
            if (!validStateInsidePortal(blockState, VALID_FRAME)) {
                if (VALID_FRAME.contains(blockState.getBlock())) {
                    return i >= 3 ? i : 0;
                }
                break;
            }
        }
        return 0;
    }

    private boolean checkForValidFrame() {
        BlockPos checkPos = lowerCorner.mutableCopy();
        for (int i = 0; i < this.width; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.down()).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.up(height)).getBlock()))
                return false;
            checkPos = checkPos.offset(negativeDir);
        }
        checkPos = lowerCorner.mutableCopy();
        for (int i = 0; i < this.height; i++) {
            if (!VALID_FRAME.contains(world.getBlockState(checkPos.offset(negativeDir.getOpposite())).getBlock()) || !VALID_FRAME.contains(world.getBlockState(checkPos.offset(negativeDir, width)).getBlock()))
                return false;
            checkPos = checkPos.up();
        }
        return true;
    }

    private void countExistingPortalBlocks() {
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (CustomPortalsMod.isInstanceOfCustomPortal(world.getBlockState(this.lowerCorner.offset(negativeDir, i).up(j))))
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
        if (blockState.isAir() || CustomPortalsMod.isInstanceOfCustomPortal(blockState))
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

    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.width * this.height;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= maxWidth && this.height >= 3 && this.height <= maxHeight;
    }

    public void createPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalsMod.blockWithAxis(link != null ? link.getPortalBlock().getDefaultState() : CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), axis);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(this.negativeDir, this.width - 1)).forEach((blockPos) -> {
            this.world.setBlockState(blockPos, blockState, 18);
        });
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((attemptWidth == 0 || width == attemptWidth) && (attemptHeight == 0 || this.height == attemptHeight));
    }
}