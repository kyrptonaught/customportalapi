package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Optional;
import java.util.function.Predicate;

public class VanillaPortalAreaHelper extends PortalFrameTester {
    private Direction.Axis axis;
    private int height;
    private int width;

    private final int maxWidth = 21;
    private final int maxHeight = 21;

    public VanillaPortalAreaHelper() {

    }

    public PortalFrameTester init(WorldAccess world, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        this.axis = axis;
        this.lowerCorner = this.getLowerCorner(blockPos, axis, Direction.Axis.Y);
        this.foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            width = height = 1;
        } else {
            this.width = this.getSize(axis, 2, maxWidth);
            if (this.width > 0) {
                this.height = this.getSize(Direction.Axis.Y, 3, maxHeight);
                if (checkForValidFrame(axis, Direction.Axis.Y, width, height)) {
                    countExistingPortalBlocks(axis, Direction.Axis.Y, width, height);
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
    public Direction.Axis getAxis1() {
        return axis;
    }

    @Override
    public Direction.Axis getAxis2() {
        return Direction.Axis.Y;
    }

    public Optional<PortalFrameTester> getNewPortal(WorldAccess worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(worldAccess, blockPos, (customAreaHelper) -> {
            return customAreaHelper.isValidFrame() && customAreaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(WorldAccess worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        Optional<PortalFrameTester> optional = Optional.of(new VanillaPortalAreaHelper().init(worldAccess, blockPos, axis, foundations)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        } else {
            Direction.Axis axis2 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new VanillaPortalAreaHelper().init(worldAccess, blockPos, axis2, foundations)).filter(predicate);
        }
    }

    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.width * this.height;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && this.width >= 2 && this.width <= maxWidth && this.height >= 3 && this.height <= maxHeight;
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((attemptWidth == 0 || width == attemptWidth) && (attemptHeight == 0 || this.height == attemptHeight));
    }

    @Override
    public BlockPos doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis) {
        if (isEmptySpace(world.getBlockState(attemptPos)) && isEmptySpace(world.getBlockState(attemptPos.offset(axis, 1))) &&
                isEmptySpace(world.getBlockState(attemptPos.up())) && isEmptySpace(world.getBlockState(attemptPos.offset(axis, 1).up())) &&
                isEmptySpace(world.getBlockState(attemptPos.up(2))) && isEmptySpace(world.getBlockState(attemptPos.offset(axis, 1).up(2))) &&
                canHoldPortal(world, attemptPos.down()) && canHoldPortal(world, attemptPos.offset(axis, 1).down()))
            return attemptPos;

        return null;
    }

    private boolean isEmptySpace(BlockState blockState) {
        return blockState.getMaterial().isReplaceable() && !blockState.getMaterial().isLiquid();
    }

    private boolean canHoldPortal(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.getMaterial().isSolid() && blockState.isSolidBlock(world, pos) && !blockState.getMaterial().isLiquid() && !blockState.getMaterial().equals(Material.LEAVES);
    }

    @Override
    public Vec3d getEntityOffsetInPortal(BlockLocating.Rectangle arg, Entity entity, Direction.Axis portalAxis) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = arg.width - entityDimensions.width;
        double height = arg.height - entityDimensions.height;

        double deltaX = MathHelper.getLerpProgress(entity.getX(), arg.lowerLeft.getX(), arg.lowerLeft.getX() + width);
        double deltaY = MathHelper.getLerpProgress(entity.getY(), arg.lowerLeft.getY(), arg.lowerLeft.getY() + height);
        double deltaZ = MathHelper.getLerpProgress(entity.getZ(), arg.lowerLeft.getZ(), arg.lowerLeft.getZ() + width);


        return new Vec3d(deltaX, deltaY, deltaZ);
    }

    @Override
    public TeleportTarget getTPTargetInPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d prevOffset, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double width = portalRect.width - entityDimensions.width;
        double height = portalRect.height - entityDimensions.height;
        double x = MathHelper.lerp(prevOffset.x, portalRect.lowerLeft.getX(), portalRect.lowerLeft.getX() + width);
        double y = MathHelper.lerp(prevOffset.y, portalRect.lowerLeft.getY(), portalRect.lowerLeft.getY() + height);
        double z = MathHelper.lerp(prevOffset.z, portalRect.lowerLeft.getZ(), portalRect.lowerLeft.getZ() + width);
        if (portalAxis == Direction.Axis.X)
            z = portalRect.lowerLeft.getZ() + 0.5D;
        else if (portalAxis == Direction.Axis.Z)
            x = portalRect.lowerLeft.getX() + .5D;

        return new TeleportTarget(new Vec3d(x, y, z), entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }

    public void lightPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null ? link.getPortalBlock().getDefaultState() : CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), axis);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(this.axis, this.width - 1)).forEach((blockPos) -> {
            this.world.setBlockState(blockPos, blockState, 18);
        });
    }

    public void createPortal(World world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        Direction.Axis rotatedAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        for (int i = -1; i < 4; i++) {
            world.setBlockState(pos.up(i).offset(axis, -1), frameBlock);
            world.setBlockState(pos.up(i).offset(axis, 2), frameBlock);
            if (i >= 0) {
                fillAirAroundPortal(world, pos.up(i).offset(axis, -1).offset(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.up(i).offset(axis, 2).offset(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.up(i).offset(axis, -1).offset(rotatedAxis, -1));
                fillAirAroundPortal(world, pos.up(i).offset(axis, 2).offset(rotatedAxis, -1));
            }
        }
        for (int i = -1; i < 3; i++) {
            world.setBlockState(pos.up(-1).offset(axis, i), frameBlock);
            world.setBlockState(pos.up(3).offset(axis, i), frameBlock);

            fillAirAroundPortal(world, pos.up(3).offset(axis, i).offset(rotatedAxis, 1));
            fillAirAroundPortal(world, pos.up(3).offset(axis, i).offset(rotatedAxis, -1));
        }
        placeLandingPad(world, pos.down().offset(rotatedAxis, 1), frameBlock);
        placeLandingPad(world, pos.down().offset(rotatedAxis, -1), frameBlock);
        placeLandingPad(world, pos.down().offset(axis, 1).offset(rotatedAxis, 1), frameBlock);
        placeLandingPad(world, pos.down().offset(axis, 1).offset(rotatedAxis, -1), frameBlock);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                fillAirAroundPortal(world, pos.offset(axis, i).up(j).offset(rotatedAxis, 1));
                fillAirAroundPortal(world, pos.offset(axis, i).up(j).offset(rotatedAxis, -1));
            }
        }
        //inits this instance based off of the newly created portal;
        this.lowerCorner = pos;
        this.width = 2;
        this.height = 3;
        this.axis = axis;
        this.world = world;
        this.foundPortalBlocks = 6;

        lightPortal(frameBlock.getBlock());
    }

    private void fillAirAroundPortal(World world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isSolid() || world.getBlockState(pos).isSolidBlock(world, pos))
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
    }

    private void placeLandingPad(World world, BlockPos pos, BlockState frameBlock) {
        if (!world.getBlockState(pos).getMaterial().isSolid())
            world.setBlockState(pos, frameBlock);
    }
}