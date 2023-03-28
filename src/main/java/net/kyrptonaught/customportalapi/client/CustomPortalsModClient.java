package net.kyrptonaught.customportalapi.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.mixin.client.ChunkRendererRegionAccessor;
import net.kyrptonaught.customportalapi.networking.ForcePlacePortalPacket;
import net.kyrptonaught.customportalapi.networking.PortalRegistrySync;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@Environment(EnvType.CLIENT)
public class CustomPortalsModClient implements ClientModInitializer {
    public static final ParticleType<BlockStateParticleEffect> CUSTOMPORTALPARTICLE = Registry.register(Registries.PARTICLE_TYPE, CustomPortalsMod.MOD_ID + ":customportalparticle", FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY));

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CustomPortalsMod.portalBlock, RenderLayer.getTranslucent());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (pos != null && world instanceof ChunkRendererRegion) {
                Block block = CustomPortalHelper.getPortalBase(((ChunkRendererRegionAccessor) world).getWorld(), pos);
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
                if (link != null) return link.colorID;
            }
            return 1908001;
        }, CustomPortalsMod.portalBlock);

        ParticleFactoryRegistry.getInstance().register(CUSTOMPORTALPARTICLE, CustomPortalParticle.Factory::new);

        PortalRegistrySync.registerReceivePortalData();
        ForcePlacePortalPacket.registerReceive();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PerWorldPortals.removeOldPortalsFromRegistry();
        });
    }
}