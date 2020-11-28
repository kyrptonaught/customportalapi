package net.kyrptonaught.customportalapi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;

public class CustomPortalsMod implements ModInitializer {
    public static final String MOD_ID = "customportals";
    public static CustomPortalBlock portalBlock;
    public static HashMap<Identifier, RegistryKey<World>> dims = new HashMap<>();

    @Override
    public void onInitialize() {
        portalBlock = new CustomPortalBlock(Block.Settings.of(Material.PORTAL).noCollision().strength(-1).sounds(BlockSoundGroup.GLASS).luminance(state -> 11));

        Registry.register(Registry.BLOCK, new Identifier(CustomPortalsMod.MOD_ID, "customportalblock"), portalBlock);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (RegistryKey<World> registryKey : server.getWorldRegistryKeys()) {
                dims.put(registryKey.getValue(), registryKey);
            }
        });
      /*  UseItemCallback.EVENT.register(t -> {
            t.getActiveItem()
        });
       */
        //CustomPortalApiRegistry.addPortal(Blocks.GLOWSTONE, Blocks.WATER, (CustomPortalBlock)aetherBlock,  new Identifier("the_nether"), DyeColor.LIGHT_BLUE.getMaterialColor().color);
        //CustomPortalApiRegistry.addPortal(Blocks.GLOWSTONE, Blocks.WATER, new Identifier("the_nether"), rgb);
        //CustomPortalApiRegistry.addPortal(Blocks.GOLD_BLOCK, new Identifier("the_nether"), DyeColor.YELLOW.getMaterialColor().color);
    }
}