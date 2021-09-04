package net.kyrptonaught.customportalapi.portal.frame;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

public class FlatPortalAreaHelper extends PortalFrameTester {
    private int xSize = -1, zSize = -1;

    public FlatPortalAreaHelper() {
    }

    public FlatPortalAreaHelper init(WorldAccess world, BlockPos pos, Direction.Axis axis, Block... foundations) {
        VALID_FRAME = Sets.newHashSet(foundations);
        this.world = world;
        detectPortal(pos);
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

    private void detectPortal(BlockPos portalPos) {
        if (findInnerPortals(portalPos)) {
            lowerCorner = null;
            return;
        }

        for (Integer x : X_Cords.values()) {
            if (xSize == -1) xSize = x;
            if (x != xSize) xSize = -999;
        }
        for (Integer z : Z_Cords.values()) {
            if (zSize == -1) zSize = z;
            if (z != zSize) zSize = -999;
        }
        if (xSize <= 0 || zSize <= 0)
            lowerCorner = null;
    }


    private final HashSet<BlockPos> insidePos = new HashSet<>();
    private final HashMap<Integer, Integer> X_Cords = new HashMap<>();
    private final HashMap<Integer, Integer> Z_Cords = new HashMap<>();

    private boolean findInnerPortals(BlockPos portalPos) {
        if (insidePos.contains(portalPos) || insidePos.size() > 36)
            return false;

        if (CustomAreaHelper.validStateInsidePortal(world.getBlockState(portalPos), VALID_FRAME)) {
            if (CustomPortalsMod.isInstanceOfCustomPortal(world, portalPos))
                foundPortalBlocks++;
            if (!checkFramesForPortal(portalPos))
                return true;
            insidePos.add(portalPos);
            X_Cords.put(portalPos.getX(), X_Cords.getOrDefault(portalPos.getX(), 0) + 1);
            Z_Cords.put(portalPos.getZ(), Z_Cords.getOrDefault(portalPos.getZ(), 0) + 1);
            if (lowerCorner == null) lowerCorner = portalPos;
            else if (portalPos.getX() <= lowerCorner.getX() && portalPos.getZ() <= lowerCorner.getZ())
                lowerCorner = portalPos;

            return findInnerPortals(portalPos.north()) ||
                    findInnerPortals(portalPos.east()) ||
                    findInnerPortals(portalPos.south()) ||
                    findInnerPortals(portalPos.west());
        }
        return false;
    }

    private boolean checkFramesForPortal(BlockPos pos) {
        return isValidFrameBlock(world.getBlockState(pos.north())) &&
                isValidFrameBlock(world.getBlockState(pos.east())) &&
                isValidFrameBlock(world.getBlockState(pos.south())) &&
                isValidFrameBlock(world.getBlockState(pos.west()));
    }

    private boolean isValidFrameBlock(BlockState state) {
        return CustomAreaHelper.validStateInsidePortal(state, VALID_FRAME) || VALID_FRAME.contains(state.getBlock());
    }

    public boolean isAlreadyLitPortalFrame() {
        return this.isValidFrame() && this.foundPortalBlocks == this.xSize * this.zSize;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && Math.min(xSize, zSize) >= 2 && Math.max(xSize, zSize) < 10;
    }

    public void createPortal(Block frameBlock) {
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(frameBlock);
        BlockState blockState = CustomPortalsMod.blockWithAxis(link != null ? link.getPortalBlock().getDefaultState() : CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), Direction.Axis.Y);
        BlockPos.iterate(this.lowerCorner, this.lowerCorner.offset(Direction.SOUTH, this.xSize - 1).offset(Direction.EAST, this.zSize - 1)).forEach((blockPos) -> {
            this.world.setBlockState(blockPos, blockState, 18);
        });
    }

    @Override
    public boolean isRequestedSize(int attemptWidth, int attemptHeight) {
        return ((xSize == attemptWidth || attemptHeight == 0) && (zSize == attemptHeight) || attemptWidth == 0) ||
                ((xSize == attemptHeight | attemptHeight == 0) && (zSize == attemptWidth || attemptWidth == 0));
    }

    @Override
    public BlockLocating.Rectangle getRectangle() {
        return null;
    }

    @Override
    public boolean doesPortalFitAt(World world, BlockPos attemptPos, Direction.Axis axis) {
        return false;
    }
}