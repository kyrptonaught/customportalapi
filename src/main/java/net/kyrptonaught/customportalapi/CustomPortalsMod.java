package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.customportalapi.api.CustomPortalBuilder;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.portal.frame.FlatPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.frame.VanillaPortalAreaHelper;
import net.kyrptonaught.customportalapi.portal.linking.PortalLinkingStorage;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class CustomPortalsMod implements ModInitializer {
    public static final String MOD_ID = "customportalapi";
    public static CustomPortalBlock portalBlock;
    public static Identifier VANILLAPORTAL_FRAMETESTER = new Identifier(MOD_ID, "vanillanether");
    public static Identifier FLATPORTAL_FRAMETESTER = new Identifier(MOD_ID, "flat");
    public static PortalLinkingStorage portalLinkingStorage;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            portalLinkingStorage = (PortalLinkingStorage) server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(PortalLinkingStorage::fromNbt, PortalLinkingStorage::new, MOD_ID);
        });
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
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
                        if (PortalPlacer.attemptPortalLight(world, usedBlockPos.offset(blockHit.getSide()), PortalIgnitionSource.ItemUseSource(item).withPlayer(player))) {
                            return TypedActionResult.success(stack);
                        }
                    }
                }
            }
            return TypedActionResult.pass(stack);
        }));

        CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(new Identifier("the_nether")).lightWithWater().setPortalSearchYRange(126, 256).tintColor(125, 20, 20).registerPortal();
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.OBSIDIAN).destDimID(new Identifier("the_end")).tintColor(66, 135, 245).registerPortalForced();
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.COBBLESTONE).lightWithItem(Items.STICK).destDimID(new Identifier("the_end")).tintColor(45, 24, 45).flatPortal().registerPortal();
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.EMERALD_BLOCK).lightWithWater().destDimID(new Identifier("the_end")).tintColor(25, 76, 156).flatPortal().registerPortal();
    }

    public static void logError(String message) {
        System.out.println("[" + MOD_ID + "]ERROR: " + message);
    }

    public static Block getDefaultPortalBlock() {
        return portalBlock;
    }

    // to guarantee block exists before use, unsure how safe this is but works for now. Don't want to switch to using a custom entrypoint to break compatibility with existing mods just yet
    //todo fix this with CustomPortalBuilder?
    static {
        portalBlock = new CustomPortalBlock(AbstractBlock.Settings.create().noCollision().ticksRandomly().strength(-1.0f).sounds(BlockSoundGroup.GLASS).luminance(state -> 11).pistonBehavior(PistonBehavior.BLOCK));
        Registry.register(Registries.BLOCK, new Identifier(CustomPortalsMod.MOD_ID, "customportalblock"), portalBlock);
    }
}