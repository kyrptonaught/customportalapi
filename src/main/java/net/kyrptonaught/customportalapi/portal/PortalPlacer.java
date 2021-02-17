package net.kyrptonaught.customportalapi.portal;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.class_5459;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

public class PortalPlacer {
    public static boolean attemptPortalLight(World world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource ignitionSource) {
        Block foundationBlock = world.getBlockState(framePos).getBlock();
        if (!CustomPortalApiRegistry.portals.containsKey(foundationBlock) || !CustomPortalApiRegistry.portals.get(foundationBlock).doesIgnitionMatch(ignitionSource))
            return false;
        return createPortal(world, portalPos, foundationBlock);
    }

    private static boolean createPortal(World world, BlockPos pos, Block foundationBlock) {
        HashSet<Block> foundations = new HashSet<>();
        foundations.add(foundationBlock);
        Optional<CustomAreaHelper> optional = CustomAreaHelper.method_30485(world, pos, Direction.Axis.X, foundations);
        //is valid frame, and is correct size(if applicable)
        if (optional.isPresent() && CustomPortalApiRegistry.portals.get(foundationBlock).isCorrectForcedSize(optional.get().getWidth(), optional.get().getHeight())) {
            optional.get().createPortal(foundationBlock);
            return true;
        }
        return false;
    }

    public static Optional<class_5459.class_5460> findOrCreatePortal(ServerWorld world, BlockPos blockPos, Block portalFrame, Direction.Axis axis, boolean bl) {
        Optional<class_5459.class_5460> found = findDestinationPortal(world, blockPos, portalFrame, bl);
        if (found.isPresent()) return found;
        return createDestinationPortal(world, blockPos, portalFrame.getDefaultState(), axis);
    }

    private static Optional<class_5459.class_5460> findDestinationPortal(ServerWorld world, BlockPos blockPos, Block portalFrame, boolean bl) {
        PointOfInterestStorage pointOfInterestStorage = world.getPointOfInterestStorage();
        int i = bl ? 16 : 128;
        pointOfInterestStorage.preloadChunks(world, blockPos, i);
        Optional<PointOfInterest> optional = pointOfInterestStorage.getInSquare(CustomPortalApiRegistry::isCustomPortalPOI, blockPos, i, PointOfInterestStorage.OccupationStatus.ANY).filter(pointOfInterest -> {
            if (CustomPortalsMod.isInstanceOfCustomPortal(world, pointOfInterest.getPos()))
                return CustomPortalBlock.getPortalBase(world, pointOfInterest.getPos()).equals(portalFrame);
            return false;
        }).sorted(Comparator.comparingDouble((pointOfInterest) -> ((PointOfInterest) pointOfInterest).getPos().getSquaredDistance(blockPos))
                .thenComparingInt((pointOfInterest) -> ((PointOfInterest) pointOfInterest).getPos().getY()))
                .filter((pointOfInterest) -> world.getBlockState(pointOfInterest.getPos())
                        .contains(Properties.HORIZONTAL_AXIS)).findFirst();
        return optional.map((pointOfInterest) -> {
            BlockPos blockPos2 = pointOfInterest.getPos();
            world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(blockPos2), 3, blockPos2);
            BlockState blockState = world.getBlockState(blockPos2);
            return class_5459.method_30574(blockPos2, blockState.get(Properties.HORIZONTAL_AXIS), 21, Direction.Axis.Y, 21, (blockPosx) -> world.getBlockState(blockPosx) == blockState);
        });
    }

    private static Optional<class_5459.class_5460> createDestinationPortal(World world, BlockPos blockPos, BlockState frameBlock, Direction.Axis axis) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double d = -1.0D;
        BlockPos blockPos2 = null;
        double e = -1.0D;
        BlockPos blockPos3 = null;
        WorldBorder worldBorder = world.getWorldBorder();
        int i = world.getDimensionHeight() - 1;
        BlockPos.Mutable mutable = blockPos.mutableCopy();
        Iterator var13 = BlockPos.method_30512(blockPos, 16, Direction.EAST, Direction.SOUTH).iterator();

        while (true) {
            BlockPos.Mutable mutable2;
            int p;
            do {
                do {
                    if (!var13.hasNext()) {
                        if (d == -1.0D && e != -1.0D) {
                            blockPos2 = blockPos3;
                            d = e;
                        }

                        int o;
                        if (d == -1.0D) {
                            blockPos2 = (new BlockPos(blockPos.getX(), MathHelper.clamp(blockPos.getY(), 70, world.getDimensionHeight() - 10), blockPos.getZ())).toImmutable();
                            Direction direction2 = direction.rotateYClockwise();
                            if (!worldBorder.contains(blockPos2)) {
                                return Optional.empty();
                            }

                            for (o = -1; o < 2; ++o) {
                                for (p = 0; p < 2; ++p) {
                                    for (int q = -1; q < 3; ++q) {
                                        BlockState blockState = q < 0 ? frameBlock : Blocks.AIR.getDefaultState();
                                        mutable.set(blockPos2, p * direction.getOffsetX() + o * direction2.getOffsetX(), q, p * direction.getOffsetZ() + o * direction2.getOffsetZ());
                                        world.setBlockState(mutable, blockState);
                                    }
                                }
                            }
                        }

                        for (int r = -1; r < 3; ++r) {
                            for (o = -1; o < 4; ++o) {
                                if (r == -1 || r == 2 || o == -1 || o == 3) {
                                    mutable.set(blockPos2, r * direction.getOffsetX(), o, r * direction.getOffsetZ());
                                    world.setBlockState(mutable, frameBlock, 3);
                                }
                            }
                        }

                        BlockState blockState2 = CustomPortalApiRegistry.portals.containsKey(frameBlock.getBlock()) ?
                                CustomPortalApiRegistry.portals.get(frameBlock.getBlock()).getPortalBlock().getDefaultState().with(NetherPortalBlock.AXIS, axis)
                                : CustomPortalsMod.getDefaultPortalBlock().getDefaultState().with(NetherPortalBlock.AXIS, axis);

                        for (o = 0; o < 2; ++o) {
                            for (p = 0; p < 3; ++p) {
                                mutable.set(blockPos2, o * direction.getOffsetX(), p, o * direction.getOffsetZ());
                                world.setBlockState(mutable, blockState2, 18);
                            }
                        }

                        return Optional.of(new class_5459.class_5460(blockPos2.toImmutable(), 2, 3));
                    }

                    mutable2 = (BlockPos.Mutable) var13.next();
                    p = Math.min(i, world.getTopY(Heightmap.Type.MOTION_BLOCKING, mutable2.getX(), mutable2.getZ()));
                } while (!worldBorder.contains(mutable2));
            } while (!worldBorder.contains(mutable2.move(direction, 1)));

            mutable2.move(direction.getOpposite(), 1);

            for (int l = p; l >= 0; --l) {
                mutable2.setY(l);
                if (world.isAir(mutable2)) {
                    int m;
                    for (m = l; l > 0 && world.isAir(mutable2.move(Direction.DOWN)); --l) {
                    }
                    if (l + 4 <= i) {
                        int n = m - l;
                        if (n <= 0 || n >= 3) {
                            mutable2.setY(l);
                            if (canHostFrame(world, mutable2, mutable, direction, 0)) {
                                double f = blockPos.getSquaredDistance(mutable2);
                                if (canHostFrame(world, mutable2, mutable, direction, -1) && canHostFrame(world, mutable2, mutable, direction, 1) && (d == -1.0D || d > f)) {
                                    d = f;
                                    blockPos2 = mutable2.toImmutable();
                                }

                                if (d == -1.0D && (e == -1.0D || e > f)) {
                                    e = f;
                                    blockPos3 = mutable2.toImmutable();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean canHostFrame(World world, BlockPos blockPos, BlockPos.Mutable mutable, Direction direction, int i) {
        Direction direction2 = direction.rotateYClockwise();

        for (int j = -1; j < 3; ++j) {
            for (int k = -1; k < 4; ++k) {
                mutable.set(blockPos, direction.getOffsetX() * j + direction2.getOffsetX() * i, k, direction.getOffsetZ() * j + direction2.getOffsetZ() * i);
                if (k < 0 && !world.getBlockState(mutable).getMaterial().isSolid()) {
                    return false;
                }

                if (k >= 0 && !world.isAir(mutable)) {
                    return false;
                }
            }
        }
        return true;
    }
}
