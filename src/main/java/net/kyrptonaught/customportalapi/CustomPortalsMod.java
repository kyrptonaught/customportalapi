package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.CustomAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.util.CustomPortalFluidProvider;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashMap;

public class CustomPortalsMod implements ModInitializer {
    public static final String MOD_ID = "customportalapi";
    public static CustomPortalBlock portalBlock;
    public static HashMap<Identifier, RegistryKey<World>> dims = new HashMap<>();
    public static Identifier VANILLA_NETHERPORTAL_FRAMETESTER = new Identifier(MOD_ID, "vanillanether");
    public static Identifier FLATPORTAL_FRAMETESTER = new Identifier(MOD_ID, "flat");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (RegistryKey<World> registryKey : server.getWorldRegistryKeys()) {
                dims.put(registryKey.getValue(), registryKey);
            }
        });
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLA_NETHERPORTAL_FRAMETESTER, CustomAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
        UseItemCallback.EVENT.register(((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient) {
                Item item = stack.getItem();
                if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                    HitResult hit = player.raycast(6, 1, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        BlockPos usedBlockPos = blockHit.getBlockPos();
                        if (PortalPlacer.attemptPortalLight(world, usedBlockPos.offset(blockHit.getSide()), usedBlockPos, PortalIgnitionSource.ItemUseSource(item))) {
                            if (item instanceof CustomPortalFluidProvider)
                                player.setStackInHand(hand, ((CustomPortalFluidProvider) item).emptyContents(player.getStackInHand(hand), player));
                            return TypedActionResult.success(stack);
                        }
                    }
                }
            }
            return TypedActionResult.pass(stack);
        }));
        //CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new Identifier("the_end")).lightWithWater().tintColor(46, 5, 25).registerPortal();
        //CustomPortalBuilder.beginPortal().frameBlock(Blocks.DIAMOND_BLOCK).destDimID(new Identifier("the_end")).tintColor(66, 135, 245).registerPortal();
        //CustomPortalBuilder.beginPortal().frameBlock(Blocks.COBBLESTONE).lightWithItem(Items.STICK).destDimID(new Identifier("the_end")).tintColor(45, 24, 45).flatPortal().registerPortal();

    }

    public static void logError(String message) {
        System.out.println("[" + MOD_ID + "]ERROR: " + message);
    }


    public static boolean isInstanceOfCustomPortal(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof CustomPortalBlock || (NetworkManager.isServerSideOnlyMode() && world.getBlockState(pos).getBlock() instanceof NetherPortalBlock);
    }

    public static Block getDefaultPortalBlock() {
        return NetworkManager.isServerSideOnlyMode() ? Blocks.NETHER_PORTAL : portalBlock;
    }

    public static Block getPortalBase(BlockView world, BlockPos pos) {
        if (isInstanceOfCustomPortal(world, pos))
            if (NetworkManager.isServerSideOnlyMode())
                return defaultPortalBaseFinder(world, pos);
            else
                return ((CustomPortalBlock) world.getBlockState(pos).getBlock()).getPortalBase(world, pos);
        else return null;
    }

    public static Block defaultPortalBaseFinder(BlockView world, BlockPos pos) {
        if (CustomPortalsMod.isInstanceOfCustomPortal(world, pos)) {
            Direction.Axis axis = getAxisFrom(world.getBlockState(pos));

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
        Direction.Axis axis = getAxisFrom(world.getBlockState(pos));
        return CustomPortalsMod.getPortalBase(world, moveTowardsFrame(pos, axis, false));
    }

    private static BlockPos moveTowardsFrame(BlockPos pos, Direction.Axis portalAxis, boolean positiveMove) {
        if (portalAxis.isHorizontal())
            return pos.offset(positiveMove ? Direction.UP : Direction.DOWN);
        return pos.offset(positiveMove ? Direction.EAST : Direction.WEST);
    }

    public static Direction.Axis getAxisFrom(BlockState state) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.get(CustomPortalBlock.AXIS);
        if (state.getBlock() instanceof NetherPortalBlock)
            return state.get(NetherPortalBlock.AXIS);
        if (state.getBlock() instanceof EndPortalBlock)
            return Direction.Axis.Y;
        return Direction.Axis.X;
    }

    public static BlockState blockWithAxis(BlockState state, Direction.Axis axis) {
        if (state.getBlock() instanceof CustomPortalBlock)
            return state.with(CustomPortalBlock.AXIS, axis);
        if (state.getBlock() instanceof NetherPortalBlock)
            return state.with(NetherPortalBlock.AXIS, axis);
        return state;
    }

    // to guarantee block exists before use, unsure how safe this is but works for now. Don't want to switch to using a custom entrypoint to break compatibility with existing mods just yet
    //todo fix this with CustomPortalBuilder?
    static {
        portalBlock = new CustomPortalBlock(Block.Settings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance(state -> 11));
        Registry.register(Registry.BLOCK, new Identifier(CustomPortalsMod.MOD_ID, "customportalblock"), portalBlock);
    }
}