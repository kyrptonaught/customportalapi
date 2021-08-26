package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
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

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;", ordinal = 0))
    public RegistryKey<World> moveToWorld(ServerWorld serverWorld) {
        if (this.didTeleport())
            return RegistryKey.of(Registry.WORLD_KEY, new Identifier(CustomPortalsMod.MOD_ID, "nullworld"));
        return serverWorld.getRegistryKey();
    }

    @Inject(method = "createEndSpawnPlatform", at = @At("HEAD"), cancellable = true)
    public void cancelEndPlatformSpawn(ServerWorld world, BlockPos centerPos, CallbackInfo ci) {
        if (this.didTeleport()) ci.cancel();
    }
}
