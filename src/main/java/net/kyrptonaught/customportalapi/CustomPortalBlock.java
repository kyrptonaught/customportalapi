package net.kyrptonaught.customportalapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.customportalapi.client.CustomPortalsModClient;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Random;

public class CustomPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    protected static final VoxelShape Y_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public CustomPortalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(AXIS)) {
            case Z -> Z_SHAPE;
            case Y -> Y_SHAPE;
            default -> X_SHAPE;
        };
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        Block block = getPortalBase(world, pos);
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester().createInstanceOfPortalFrameTester().init(world, pos, state.get(AXIS), block);
            if (portalFrameTester.wasAlreadyValid())
                return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        }
        //todo handle unknown portallink

        return Blocks.AIR.getDefaultState();
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; ++i) {
            double d = (double) pos.getX() + random.nextDouble();
            double e = (double) pos.getY() + random.nextDouble();
            double f = (double) pos.getZ() + random.nextDouble();
            double g = ((double) random.nextFloat() - 0.5D) * 0.5D;
            double h = ((double) random.nextFloat() - 0.5D) * 0.5D;
            double j = ((double) random.nextFloat() - 0.5D) * 0.5D;
            int k = random.nextInt(2) * 2 - 1;
            if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
                d = (double) pos.getX() + 0.5D + 0.25D * (double) k;
                g = random.nextFloat() * 2.0F * (float) k;
            } else {
                f = (double) pos.getZ() + 0.5D + 0.25D * (double) k;
                j = random.nextFloat() * 2.0F * (float) k;
            }
            world.addParticle(new BlockStateParticleEffect(CustomPortalsModClient.CUSTOMPORTALPARTICLE, getPortalBase(world, pos).getDefaultState()), d, e, f, g, h, j);
        }
    }


    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        EntityInCustomPortal entityInPortal = (EntityInCustomPortal) entity;
        if (entity instanceof PlayerEntity) {
            if (!entityInPortal.didTeleport()) {
                entityInPortal.setInPortal(true);
                if (entityInPortal.getTimeInPortal() >= entity.getMaxNetherPortalTime()) {
                    entityInPortal.teleported();
                    if (!world.isClient)
                        CustomTeleporter.TPToDim(world, entity, getPortalBase(world, pos), pos);
                }
            } else entityInPortal.increaseCooldown();
        } else if (!world.isClient) {
            if (!entityInPortal.didTeleport()) {
                entityInPortal.teleported();
                CustomTeleporter.TPToDim(world, entity, getPortalBase(world, pos), pos);
            } else entityInPortal.increaseCooldown();

        }
    }

    public Block getPortalBase(BlockView world, BlockPos pos) {
        if (CustomPortalsMod.isInstanceOfCustomPortal(world, pos)) {
            Direction.Axis axis = world.getBlockState(pos).get(AXIS);

            if (!CustomPortalsMod.isInstanceOfCustomPortal(world, moveTowardsFrame(pos, axis, false)))
                return world.getBlockState(moveTowardsFrame(pos, axis, false)).getBlock();
            if (!CustomPortalsMod.isInstanceOfCustomPortal(world, moveTowardsFrame(pos, axis, true)))
                return world.getBlockState(moveTowardsFrame(pos, axis, true)).getBlock();

            if (axis == Direction.Axis.Y) axis = Direction.Axis.Z;

            if (!CustomPortalsMod.isInstanceOfCustomPortal(world, pos.offset(axis, 1)))
                return world.getBlockState(pos.offset(axis, 1)).getBlock();
            if (!CustomPortalsMod.isInstanceOfCustomPortal(world, pos.offset(axis, -1)))
                return world.getBlockState(pos.offset(axis, -1)).getBlock();
        }
        if (pos.getY() < 0 || world.getBlockState(pos).isAir()) {
            return null;
        }
        Direction.Axis axis = world.getBlockState(pos).get(AXIS);
        return CustomPortalsMod.getPortalBase(world, moveTowardsFrame(pos, axis, false));
    }

    public BlockPos moveTowardsFrame(BlockPos pos, Direction.Axis portalAxis, boolean positiveMove) {
        if (portalAxis.isHorizontal())
            return pos.offset(positiveMove ? Direction.UP : Direction.DOWN);
        return pos.offset(positiveMove ? Direction.EAST : Direction.WEST);
    }
}
