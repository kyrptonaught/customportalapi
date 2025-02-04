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
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@Environment(EnvType.CLIENT)
public class CustomPortalsModClient implements ClientModInitializer {

    public static ParticleType<BlockStateParticleEffect> CUSTOMPORTALPARTICLE = FabricParticleTypes.complex(BlockStateParticleEffect::createCodec, BlockStateParticleEffect::createPacketCodec);

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(CustomPortalsMod.portalBlock, RenderLayer.getTranslucent());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (pos != null) {
                Block block = CustomPortalHelper.getPortalBase(MinecraftClient.getInstance().world, pos.toImmutable());
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
                if (link != null) return link.colorID;
            }
            return 1908001;
        }, CustomPortalsMod.portalBlock);

        Registry.register(Registries.PARTICLE_TYPE, CustomPortalsMod.MOD_ID + ":customportalparticle", CUSTOMPORTALPARTICLE);
        ParticleFactoryRegistry.getInstance().register(CUSTOMPORTALPARTICLE, CustomPortalParticle.Factory::new);

        NetworkManager.registerPackets();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            PerWorldPortals.removeOldPortalsFromRegistry();
        });
    }
}