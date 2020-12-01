package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.customportalapi.mixin.BucketMixin;
import net.kyrptonaught.customportalapi.util.CreatePortal;
import net.kyrptonaught.customportalapi.util.PortalIgnitionSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.gen.placer.BlockPlacer;

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
                if (CreatePortal.createPortal(world, hitResult.getBlockPos().up(), PortalIgnitionSource.ItemUseSource(item))) {
                    if (item instanceof BucketItem)
                        playerEntity.setStackInHand(hand, ((BucketMixin) item).invokegetEmptiedStack(playerEntity.getStackInHand(hand), playerEntity));
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        //CustomPortalApiRegistry.addPortal(Blocks.GLOWSTONE, Blocks.WATER, (CustomPortalBlock)aetherBlock,  new Identifier("the_nether"), DyeColor.LIGHT_BLUE.getMaterialColor().color);
        CustomPortalApiRegistry.addPortal(Blocks.DIAMOND_BLOCK, Blocks.WATER, new Identifier("the_nether"), 66, 135, 245);
        CustomPortalApiRegistry.addPortal(Blocks.GOLD_BLOCK, new Identifier("the_end"), DyeColor.YELLOW.getMaterialColor().color);
    }

    public static void logError(String message) {
        System.out.println("[" + MOD_ID + "]ERROR: " + message);
    }

    // to guarantee block exists before use, unsure how safe this is but works for now. Don't want to switch to using a custom entrypoint to break compatibility with existing mods just yet
    static {
        portalBlock = new CustomPortalBlock(Block.Settings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance(state -> 11));
        Registry.register(Registry.BLOCK, new Identifier(CustomPortalsMod.MOD_ID, "customportalblock"), portalBlock);
    }
}