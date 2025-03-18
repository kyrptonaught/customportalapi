package net.kyrptonaught.customportalapi.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Portal;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @WrapOperation(method = "tickNausea", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
    public void playCustomPortalAmbiance(SoundManager instance, SoundInstance sound, Operation<Void> original) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        PortalLink link = isCustomPortal(player);
        if (link != null && link.getInPortalAmbienceEvent().hasEvent()) {
            original.call(instance, link.getInPortalAmbienceEvent().execute(player).getInstance());
            return;
        }
        original.call(instance, sound);
    }

    @Unique
    private PortalLink isCustomPortal(ClientPlayerEntity player) {
        PortalManager portalManager = player.portalManager;
        Portal portalBlock = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPortal() : null;
        BlockPos portalPos = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPos() : null;

        if (portalBlock instanceof CustomPortalBlock)
            return CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalBlock).getPortalBase(player.clientWorld, portalPos));

        return null;
    }
}
