package net.kyrptonaught.customportalapi.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.WorldEventHandler;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldEventHandler.class)
public class WorldRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(method = "processWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
    public void CPA$postTPSoundEvent(SoundManager instance, SoundInstance sound, Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int eventId, @Local(argsOnly = true, ordinal = 1) int data) {
        if (eventId == 1032 && data != 0) {
            Block block = Registries.BLOCK.get(data);
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null && link.getPostTpPortalAmbienceEvent().hasEvent()) {
                original.call(instance, link.getPostTpPortalAmbienceEvent().execute(client.player).getInstance());
                return;
            }
        }
        original.call(instance, sound);
    }
}
