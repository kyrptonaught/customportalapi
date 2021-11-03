package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

public class FlatPortalAreaHelper extends PortalFrameTester {
    private int xSize = -1, zSize = -1;
    private final int maxXSize = 21, maxZSize = 21;

    public FlatPortalAreaHelper() {
    }

    public FlatPortalAreaHelper init(WorldAccess world, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        this.lowerCorner = this.getLowerCorner(blockPos, Direction.Axis.X, Direction.Axis.Z);
        this.foundPortalBlocks = 0;
        if (lowerCorner == null) {
            lowerCorner = blockPos;
            xSize = zSize = 1;
        } else {
            this.xSize = this.getSize(Direction.Axis.X, 2, maxXSize);
            if (this.xSize > 0) {
                this.zSize = this.getSize(Direction.Axis.Z, 2, maxZSize);
                if (checkForValidFrame(Direction.Axis.X, Direction.Axis.Z, xSize, zSize)) {
                    countExistingPortalBlocks(Direction.Axis.X, Direction.Axis.Z, xSize, zSize);
                } else {
                    lowerCorner = null;
                    xSize = zSize = 1;
                }
            }
        }
        return this;
    }

    public Optional<PortalFrameTester> getNewPortal(WorldAccess worldAccess, BlockPos blockPos, Direction.Axis axis, Block... foundations) {
        return getOrEmpty(worldAccess, blockPos, (areaHelper) -> {
            return areaHelper.isValidFrame() && areaHelper.foundPortalBlocks == 0;
        }, axis, foundations);
    }

    public Optional<PortalFrameTester> getOrEmpty(WorldAccess worldAccess, BlockPos blockPos, Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... foundations) {
        return Optional.of((PortalFrameTester) new FlatPortalAreaHelper().init(worldAccess, blockPos, axis, foundations)).filter(predicate);
    }


    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.xSize * this.zSize;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && xSize >= 2 && zSize >= 2 && xSize < maxXSize && zSize < maxZSize;
    }

    public void lightPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalHelper.blockWithAxis(link != null ? link.getPortalBlock().getDefaultState() : CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), Direction.Axis.Y);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.Axis.X, this.xSize - 1).offset(Direction.Axis.Z, this.zSize - 1)).forEach((blockPos) -> {
            this.world.setBlockState(blockPos, blockState, 18);
        });
    }

    @Override
    public void createPortal(World world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        for (int i = -1; i < 3; i++) {
            world.setBlockState(pos.offset(Direction.Axis.X, i).offset(Direction.Axis.Z, -1), frameBlock);
            world.setBlockState(pos.offset(Direction.Axis.X, i).offset(Direction.Axis.Z, 2), frameBlock);

            world.setBlockState(pos.offset(Direction.Axis.Z, i).offset(Direction.Axis.X, -1), frameBlock);
            world.setBlockState(pos.offset(Direction.Axis.Z, i).offset(Direction.Axis.X, 2), frameBlock);
        }
        for (int i = 0; i < 2; i++) {
            placeLandingPad(world, pos.offset(Direction.Axis.X, i).down(), frameBlock);
            placeLandingPad(world, pos.offset(Direction.Axis.X, i).offset(Direction.Axis.Z, 1).down(), frameBlock);

            fillAirAroundPortal(world, pos.offset(Direction.Axis.X, i).up());
            fillAirAroundPortal(world, pos.offset(Direction.Axis.X, i).offset(Direction.Axis.Z, 1).up());
            fillAirAroundPortal(world, pos.offset(Direction.Axis.X, i).up(2));
            fillAirAroundPortal(world, pos.offset(Direction.Axis.X, i).offset(Direction.Axis.Z, 1).up(2));
        }
        //inits this instance based off of the newly created portal;
        this.lowerCorner = pos;
        this.xSize = zSize = 2;
        this.world = world;
        this.foundPortalBlocks = 4;
        lightPortal(frameBlock.getBlock());
    }

    private void fillAirAroundPortal(World world, BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isSolid())
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.FORCE_STATE);
    }

    private void placeLandingPad(World world, BlockPos pos, BlockState frameBlock) {
        if (!world.getBlockState(pos).getMaterial().isSolid())
            world.setBlockState(pos, frameBlock);
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((xSize == attemptWidth || attemptHeight == 0) && (zSize == attemptHeight) || attemptWidth == 0) ||
                ((xSize == attemptHeight || attemptHeight == 0) && (zSize == attemptWidth || attemptWidth == 0));
    }

    @Override
    public BlockLocating.Rectangle getRectangle() {
        return new BlockLocating.Rectangle(lowerCorner, xSize, zSize);
    }

    @Override
    public Direction.Axis getAxis1() {
        return Direction.Axis.X;
    }

    @Override
    public Direction.Axis getAxis2() {
        return Direction.Axis.Z;
    }

    @Override
    public BlockPos doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis) {
        BlockLocating.Rectangle rect = BlockLocating.getLargestRectangle(attemptPos.up(), Direction.Axis.X, 4, Direction.Axis.Z, 4, blockPos -> {
            return world.getBlockState(blockPos).getMaterial().isSolid() &&
                    !world.getBlockState(blockPos.up()).getMaterial().isSolid() && !world.getBlockState(blockPos.up()).getMaterial().isLiquid() &&
                    !world.getBlockState(blockPos.up(2)).getMaterial().isSolid() && !world.getBlockState(blockPos.up(2)).getMaterial().isLiquid();
        });
        return rect.width >= 4 && rect.height >= 4 ? rect.lowerLeft : null;
    }

    @Override
    public Vec3d getEntityOffsetInPortal(BlockLocating.Rectangle arg, Entity entity, Direction.Axis portalAxis) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double xSize = arg.width - entityDimensions.width;
        double zSize = arg.height - entityDimensions.width;

        double deltaX = MathHelper.getLerpProgress(entity.getX(), arg.lowerLeft.getX(), arg.lowerLeft.getX() + xSize);
        double deltaY = MathHelper.getLerpProgress(entity.getY(), arg.lowerLeft.getY() - 1, arg.lowerLeft.getY() + 1);
        double deltaZ = MathHelper.getLerpProgress(entity.getZ(), arg.lowerLeft.getZ(), arg.lowerLeft.getZ() + zSize);

        return new Vec3d(deltaX, deltaY, deltaZ);
    }

    @Override
    public TeleportTarget getTPTargetInPortal(BlockLocating.Rectangle portalRect, Direction.Axis portalAxis, Vec3d prevOffset, Entity entity) {
        EntityDimensions entityDimensions = entity.getDimensions(entity.getPose());
        double xSize = portalRect.width - entityDimensions.width;
        double zSize = portalRect.height - entityDimensions.width;

        double x = MathHelper.lerp(prevOffset.x, portalRect.lowerLeft.getX(), portalRect.lowerLeft.getX() + xSize);
        double y = MathHelper.lerp(prevOffset.y, portalRect.lowerLeft.getY() - 1, portalRect.lowerLeft.getY() + 1);
        double z = MathHelper.lerp(prevOffset.z, portalRect.lowerLeft.getZ(), portalRect.lowerLeft.getZ() + zSize);

        y = Math.max(y, portalRect.lowerLeft.getY());
        return new TeleportTarget(new Vec3d(x, y, z), entity.getVelocity(), entity.getYaw(), entity.getPitch());
    }
}