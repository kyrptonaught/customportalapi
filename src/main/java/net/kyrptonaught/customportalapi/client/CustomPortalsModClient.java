package net.kyrptonaught.customportalapi.client;

import com.mojang.bridge.game.GameSession;
import com.mojang.bridge.launcher.SessionEventListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.impl.client.rendering.ColorProviderRegistryImpl;
import net.kyrptonaught.customportalapi.*;
import net.kyrptonaught.customportalapi.networking.PortalRegistrySync;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class CustomPortalsModClient implements ClientModInitializer {
    public static final ParticleType<BlockStateParticleEffect> CUSTOMPORTALPARTICLE = Registry.register(Registry.PARTICLE_TYPE, CustomPortalsMod.MOD_ID + ":customportalparticle", FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY));

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CustomPortalsMod.portalBlock, RenderLayer.getTranslucent());
        ColorProviderRegistryImpl.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                Block block = CustomPortalBlock.getPortalBase(world, pos);
                if (CustomPortalApiRegistry.portals.containsKey(block))
                    return CustomPortalApiRegistry.portals.get(block).colorID;
            }
            return 1908001;
        }, CustomPortalsMod.portalBlock);
        ParticleFactoryRegistry.getInstance().register(CUSTOMPORTALPARTICLE, CustomPortalParticle.Factory::new);

        PortalRegistrySync.registerReceivePortalData();

        MinecraftClient.getInstance().getGame().setSessionEventListener(new SessionEventListener() {
            @Override
            public void onStartGameSession(GameSession session){}

            @Override
            public void onLeaveGameSession(GameSession session) {
                PerWorldPortals.removeOldPortalsFromRegistry();
            }
        });
    }
}