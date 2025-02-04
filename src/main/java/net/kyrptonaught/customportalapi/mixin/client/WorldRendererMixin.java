package net.kyrptonaught.customportalapi.mixin.client;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.WorldEventHandler;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldEventHandler.class)
public class WorldRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(method = "processWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
    public void CPA$postTPSoundEvent(SoundManager instance, SoundInstance sound, int eventId, BlockPos pos, int data) {
        if (eventId == 1032 && data != 0) {
            Block block = Registries.BLOCK.get(data);
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(block);
            if (link != null && link.getPostTpPortalAmbienceEvent().hasEvent()) {
                instance.play(link.getPostTpPortalAmbienceEvent().execute(client.player).getInstance());
                return;
            }
        }
        instance.play(sound);
    }
}
