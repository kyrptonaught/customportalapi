package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.interfaces.CustomTeleportingEntity;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInCustomPortal, CustomTeleportingEntity {

    @Unique
    boolean didTP = false;

    @Unique
    int coolDown = 0, maxCooldown = 10;

    @Unique
    @Override
    public boolean didTeleport() {
        return didTP;
    }

    @Unique
    @Override
    public void teleported() {
        setDidTP(true);
    }

    @Unique
    @Override
    public void setDidTP(boolean didTP) {
        this.didTP = didTP;
        coolDown = maxCooldown;
    }

    @Unique
    @Override
    public void increaseCooldown() {
        coolDown = Math.min(coolDown + 1, maxCooldown);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void inCustomPortal(CallbackInfo ci) {
        if (didTP) {
            coolDown--;
            if (coolDown <= 0)
                didTP = false;
        }
    }

    private TeleportTarget customTPTarget;

    @Override
    public void setCustomTeleportTarget(TeleportTarget teleportTarget) {
        this.customTPTarget = teleportTarget;
    }

    @Override
    public TeleportTarget getCustomTeleportTarget() {
        return customTPTarget;
    }
    @Inject(method = "getTeleportTarget", at = @At("HEAD"), cancellable = true)
    public void getCustomTPTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
        if (this.didTeleport())
            cir.setReturnValue(getCustomTeleportTarget());
    }

    @Inject(method = "readNbt", at = @At(value = "TAIL"))
    public void readCustomPortalFromTag(NbtCompound tag, CallbackInfo ci) {
        this.didTP = tag.getBoolean("cpadidTP");
        this.coolDown = tag.getInt("cpacooldown");
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN"))
    public void writeCustomPortalToTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        cir.getReturnValue().putBoolean("cpadidTP", didTP);
        cir.getReturnValue().putInt("cpacooldown", coolDown);
    }
}