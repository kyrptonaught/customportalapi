package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.minecraft.block.Block;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin implements EntityInCustomPortal {
    int portalFrameBlockID;

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;", ordinal = 0))
    public RegistryKey<World> CPApreventEndCredits(ServerWorld serverWorld) {
        if (this.didTeleport()) {
            Block portalFrame = CustomPortalHelper.getPortalBase(serverWorld, getInPortalPos());
            portalFrameBlockID = Registry.BLOCK.getRawId(portalFrame);
            return RegistryKey.of(Registry.WORLD_KEY, new Identifier(CustomPortalsMod.MOD_ID, "nullworld"));
        }
        return serverWorld.getRegistryKey();
    }

    @Inject(method = "createEndSpawnPlatform", at = @At("HEAD"), cancellable = true)
    public void CPAcancelEndPlatformSpawn(ServerWorld world, BlockPos centerPos, CallbackInfo ci) {
        if (this.didTeleport()) ci.cancel();
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void CPAmodifyWorldEventPacket(ServerPlayNetworkHandler instance, Packet<?> packet) {
        if (packet instanceof WorldEventS2CPacket && portalFrameBlockID != 0) {
            instance.sendPacket(new WorldEventS2CPacket(1032, BlockPos.ORIGIN, portalFrameBlockID, false));
            portalFrameBlockID = 0;
        } else
            instance.sendPacket(packet);
    }
}
