package net.kyrptonaught.customportalapi.portal.frame;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockLocating;
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

    public abstract boolean wasAlreadyValid();

    public abstract boolean isValid();

    public abstract void createPortal(Block frameBlock);

    public abstract boolean isRequestedSize(int attemptWidth, int attemptHeight);

    public abstract BlockLocating.Rectangle getRectangle();
    @FunctionalInterface
    public interface PortalFrameTesterFactory {
        PortalFrameTester createInstanceOfPortalFrameTester();
    }

}
