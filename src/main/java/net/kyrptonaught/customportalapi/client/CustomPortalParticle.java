package net.kyrptonaught.customportalapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;

public class CustomPortalParticle extends PortalParticle {
    protected CustomPortalParticle(ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
        super(clientWorld, d, e, f, g, h, i);
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<BlockStateParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(BlockStateParticleEffect blockStateParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            // Particle portalParticle = (new PortalParticle.Factory(spriteProvider).createParticle(null,clientWorld,d,e,f,g,h,i));
            CustomPortalParticle portalParticle = new CustomPortalParticle(clientWorld, d, e, f, g, h, i);
            portalParticle.setSprite(this.spriteProvider);
            Block block = blockStateParticleEffect.getBlockState().getBlock();
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null) {
                float[] rgb = ColorUtil.getColorForBlock(link.colorID);
                portalParticle.setColor(rgb[0], rgb[1], rgb[2]);
            }
            return portalParticle;
        }
    }
}
