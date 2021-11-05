package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.interfaces.CustomTeleportingEntity;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInCustomPortal, CustomTeleportingEntity {

    @Shadow
    public World world;

    @Unique
    boolean didTP = false;

    @Unique
    int timeInPortal = 0, maxTimeInPortal = 80, cooldown = 0;

    @Unique
    private BlockPos inPortalPos;

    @Unique
    @Override
    public boolean didTeleport() {
        return didTP;
    }

    @Unique
    @Override
    public void setDidTP(boolean didTP) {
        this.didTP = didTP;
        if (didTP) {
            timeInPortal = maxTimeInPortal;
            cooldown = 10;
        } else {
            timeInPortal = 0;
            cooldown = 0;
        }
        //inPortalPos = null;
    }

    @Unique
    @Override
    public int getTimeInPortal() {
        return timeInPortal;
    }

    @Unique
    @Override
    public void tickInPortal(BlockPos portalPos) {
        cooldown = 10;
        inPortalPos = portalPos;
    }

    @Unique
    @Override
    public BlockPos getInPortalPos() {
        return inPortalPos;
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void CPAinCustomPortal(CallbackInfo ci) {
        if (cooldown > 0) {
            cooldown--;
            timeInPortal = Math.min(timeInPortal + 1, maxTimeInPortal);
            if (cooldown <= 0) {
                setDidTP(false);
            }
        }
    }

    private TeleportTarget customTPTarget;

    @Unique
    @Override
    public void setCustomTeleportTarget(TeleportTarget teleportTarget) {
        this.customTPTarget = teleportTarget;
    }

    @Unique
    @Override
    public TeleportTarget getCustomTeleportTarget() {
        return customTPTarget;
    }

    @Inject(method = "getTeleportTarget", at = @At("HEAD"), cancellable = true)
    public void CPAgetCustomTPTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if (this.didTeleport())
            cir.setReturnValue(getCustomTeleportTarget());
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;createEndSpawnPlatform(Lnet/minecraft/server/world/ServerWorld;)V"))
    public void CPAcancelEndPlatformSpawn(ServerWorld world) {
        if (this.didTeleport())
            return;
        ServerWorld.createEndSpawnPlatform(world);
    }

    @Inject(method = "readNbt", at = @At(value = "TAIL"))
    public void CPAreadCustomPortalFromTag(NbtCompound tag, CallbackInfo ci) {
        this.didTP = tag.getBoolean("cpadidTP");
        this.cooldown = tag.getInt("cpaCooldown");
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN"))
    public void CPAwriteCustomPortalToTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        cir.getReturnValue().putBoolean("cpadidTP", didTP);
        cir.getReturnValue().putInt("cpaCooldown", cooldown);
    }
}