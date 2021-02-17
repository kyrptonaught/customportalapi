package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.kyrptonaught.customportalapi.util.CustomPortalFluidProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashMap;

public class CustomPortalsMod implements ModInitializer {
    public static final String MOD_ID = "customportals";
    public static CustomPortalBlock portalBlock;
    public static HashMap<Identifier, RegistryKey<World>> dims = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (RegistryKey<World> registryKey : server.getWorldRegistryKeys()) {
                dims.put(registryKey.getValue(), registryKey);
            }
        });
        UseBlockCallback.EVENT.register((playerEntity, world, hand, hitResult) -> {
            Item item = playerEntity.getStackInHand(hand).getItem();
            if (PortalIgnitionSource.isRegisteredIgnitionSourceWith(item)) {
                if (PortalPlacer.attemptPortalLight(world, hitResult.getBlockPos().offset(hitResult.getSide()), hitResult.getBlockPos(), PortalIgnitionSource.ItemUseSource(item))) {
                    if (item instanceof CustomPortalFluidProvider)
                        playerEntity.setStackInHand(hand, ((CustomPortalFluidProvider) item).emptyContents(playerEntity.getStackInHand(hand), playerEntity));
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        // CustomPortalApiRegistry.addPortal(Blocks.DIAMOND_BLOCK, PortalIgnitionSource.FIRE, new Identifier("the_end"), 66, 135, 245);
        // CustomPortalApiRegistry.addPortal(Blocks.GLOWSTONE, PortalIgnitionSource.WATER, (CustomPortalBlock) portalBlock, new Identifier("the_nether"), 2, 3, 55, 89, 195);
        //CustomPortalApiRegistry.addPortal(Blocks.NETHERITE_BLOCK, PortalIgnitionSource.FluidSource(Fluids.LAVA), new Identifier("the_nether"), 245, 135, 66);
        //CustomPortalApiRegistry.addPortal(Blocks.SNOW_BLOCK, PortalIgnitionSource.ItemUseSource(Items.STICK), new Identifier("the_end"), 247, 250, 255);

    }

    public static void logError(String message) {
        System.out.println("[" + MOD_ID + "]ERROR: " + message);
    }

    public static boolean isInstanceOfCustomPortal(BlockView world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof CustomPortalBlock;
    }

    public static Block getDefaultPortalBlock() {
        return portalBlock;
    }

    // to guarantee block exists before use, unsure how safe this is but works for now. Don't want to switch to using a custom entrypoint to break compatibility with existing mods just yet
    static {
        portalBlock = new CustomPortalBlock(Block.Settings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance(state -> 11));
        Registry.register(Registry.BLOCK, new Identifier(CustomPortalsMod.MOD_ID, "customportalblock"), portalBlock);
    }
}