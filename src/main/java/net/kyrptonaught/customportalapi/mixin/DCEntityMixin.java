package net.kyrptonaught.customportalapi.mixin;

import com.simibubi.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionalCarriageEntity.class)
public abstract class DCEntityMixin {
    @Inject(method = "dismountPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;resetNetherPortalCooldown()V"))
    public void CPAPortalCooldown(ServerWorld sLevel, ServerPlayerEntity sp, Integer seat, boolean capture, CallbackInfo ci) {
        ((EntityInCustomPortal)(Object)sp).setDidTP(true);
    }
}
