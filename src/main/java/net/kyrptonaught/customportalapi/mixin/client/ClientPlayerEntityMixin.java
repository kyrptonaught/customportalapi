package net.kyrptonaught.customportalapi.mixin.client;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Portal;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Redirect(method = "tickNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;"))
    public SoundSystem.PlayResult playCustomPortalAmbiance(SoundManager instance, SoundInstance sound) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        PortalLink link = isCustomPortal(player);
        if (link != null && link.getInPortalAmbienceEvent().hasEvent()) {
            instance.play(link.getInPortalAmbienceEvent().execute(player).getInstance());
            return null;
        }
        instance.play(sound);
        return null;
    }

    @Unique
    private PortalLink isCustomPortal(ClientPlayerEntity player) {
        PortalManager portalManager = player.portalManager;
        Portal portalBlock = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPortal() : null;
        BlockPos portalPos = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPos() : null;

        if (portalBlock instanceof CustomPortalBlock)
            return CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalBlock).getPortalBase(player.getEntityWorld(), portalPos));

        return null;
    }
}
