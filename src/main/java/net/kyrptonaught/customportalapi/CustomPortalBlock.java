package net.kyrptonaught.customportalapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.customportalapi.client.CustomPortalsModClient;
import net.kyrptonaught.customportalapi.portal.frame.PortalFrameTester;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.CustomTeleporter;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class CustomPortalBlock extends Block implements Portal {
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

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        Block block = getPortalBase((World) world, pos);
        PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
        if (link != null) {
            PortalFrameTester portalFrameTester = link.getFrameTester().createInstanceOfPortalFrameTester().init((WorldAccess) world, pos, CustomPortalHelper.getAxisFrom(state), block);
            if (portalFrameTester.isAlreadyLitPortalFrame())
                return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
        //todo handle unknown portallink

        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(100) == 0) {
            world.playSound((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }

        for (int i = 0; i < 4; ++i) {
            double d = (double) pos.getX() + random.nextDouble();
            double e = (double) pos.getY() + random.nextDouble();
            double f = (double) pos.getZ() + random.nextDouble();
            double g = ((double) random.nextFloat() - 0.5) * 0.5;
            double h = ((double) random.nextFloat() - 0.5) * 0.5;
            double j = ((double) random.nextFloat() - 0.5) * 0.5;
            int k = random.nextInt(2) * 2 - 1;

            if (state.get(AXIS) == Direction.Axis.Y) {
                h = random.nextFloat() * 2.0f * (float) k;
            } else {
                if (world.getBlockState(pos.west()).isOf(this) || world.getBlockState(pos.east()).isOf(this)) {
                    f = (double) pos.getZ() + 0.5 + 0.25 * (double) k;
                    j = random.nextFloat() * 2.0f * (float) k;
                } else {
                    d = (double) pos.getX() + 0.5 + 0.25 * (double) k;
                    g = random.nextFloat() * 2.0f * (float) k;
                }
            }
            world.addParticle(new BlockStateParticleEffect(CustomPortalsModClient.CUSTOMPORTALPARTICLE, getPortalBase(world, pos).getDefaultState()), d, e, f, g, h, j);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.canUsePortals(false)) {
            entity.tryUsePortal(this, pos);
        }
    }

    public Block getPortalBase(World world, BlockPos pos) {
        return CustomPortalHelper.getPortalBaseDefault(world, pos);
    }

    @Override
    public int getPortalDelay(ServerWorld world, Entity entity) {
        if (entity instanceof PlayerEntity playerEntity) {
            return Math.max(1, world.getGameRules().getInt(playerEntity.getAbilities().invulnerable ? GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        } else {
            return 0;
        }
    }

    @Override
    public @Nullable TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
        return CustomTeleporter.createTeleportTarget(world, entity, getPortalBase(world, pos), pos);
    }

    @Override
    public Effect getPortalEffect() {
        return Effect.CONFUSION;
    }
}