package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.kyrptonaught.customportalapi.networking.ForcePlacePacket;
import net.kyrptonaught.customportalapi.networking.LinkSyncPacket;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;


public class CustomPortalsMod implements ModInitializer {
    public static final String MOD_ID = "customportalapi";
    public static CustomPortalBlock portalBlock;
    public static Identifier VANILLAPORTAL_FRAMETESTER = Identifier.of(MOD_ID, "vanillanether");
    public static Identifier FLATPORTAL_FRAMETESTER = Identifier.of(MOD_ID, "flat");
    public static PortalLinkingStorage portalLinkingStorage;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
            portalLinkingStorage = persistentStateManager.getOrCreate(PortalLinkingStorage.getPersistentStateType());
        });
        CustomPortalApiRegistry.registerPortalFrameTester(VANILLAPORTAL_FRAMETESTER, VanillaPortalAreaHelper::new);
        CustomPortalApiRegistry.registerPortalFrameTester(FLATPORTAL_FRAMETESTER, FlatPortalAreaHelper::new);
        UseItemCallback.EVENT.register(((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient()) {
                Item item = stack.getItem();
                if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                    HitResult hit = player.raycast(6, 1, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        BlockPos usedBlockPos = blockHit.getBlockPos();
                        if (PortalPlacer.attemptPortalLight(world, usedBlockPos.offset(blockHit.getSide()), PortalIgnitionSource.ItemUseSource(item).withPlayer(player))) {
                            return ActionResult.SUCCESS_SERVER;
                        }
                    }
                }
            }
            return ActionResult.PASS;
        }));

        PayloadTypeRegistry.playS2C().register(LinkSyncPacket.PACKET_ID, LinkSyncPacket.codec);
        PayloadTypeRegistry.playS2C().register(ForcePlacePacket.PACKET_ID, ForcePlacePacket.codec);

        /*
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.GLOWSTONE).destDimID(Identifier.of("the_nether")).lightWithWater().setPortalSearchYRange(126, 256).tintColor(125, 20, 20).registerPortal();
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.OBSIDIAN).destDimID(Identifier.of("the_end")).tintColor(66, 135, 245).registerPortalForced();
        CustomPortalBuilder.beginPortal().frameBlock(Blocks.COBBLESTONE).lightWithItem(Items.STICK).destDimID(Identifier.of("the_end")).tintColor(45, 24, 45).flatPortal().registerPortal();


        CustomPortalBuilder.beginPortal()
                .frameBlock(Blocks.EMERALD_BLOCK)
                .lightWithWater()
                .destDimID(Identifier.of("the_end"))
                .tintColor(25, 76, 156)
                .flatPortal()
                .registerInPortalAmbienceSound(player -> new CPASoundEventData(SoundEvents.BLOCK_ANVIL_LAND, player.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F))
                .registerPostTPPortalAmbience(player -> new CPASoundEventData(SoundEvents.BLOCK_ANVIL_LAND, player.getRandom().nextFloat() * 0.4F + 0.8F, 0.25F))
                .registerPortal();
*/
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
        portalBlock = (CustomPortalBlock) Blocks.register(
                RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(CustomPortalsMod.MOD_ID, "customportalblock")),
                CustomPortalBlock::new,
                AbstractBlock.Settings.create()
                        .noCollision()
                        .ticksRandomly()
                        .strength(-1.0f)
                        .sounds(BlockSoundGroup.GLASS)
                        .luminance(state -> 11)
                        .pistonBehavior(PistonBehavior.BLOCK)
        );

    }
}
