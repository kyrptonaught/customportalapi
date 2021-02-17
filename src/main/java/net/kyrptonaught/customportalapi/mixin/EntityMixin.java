package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.util.EntityInCustomPortal;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityInCustomPortal {

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

    @Inject(method = "fromTag", at = @At(value = "TAIL"))
    public void readCustomPortalFromTag(CompoundTag tag, CallbackInfo ci) {
        this.didTP = tag.getBoolean("cpadidTP");
        this.coolDown = tag.getInt("cpacooldown");
    }

    @Inject(method = "toTag", at = @At(value = "RETURN"))
    public void writeCustomPortalToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("cpadidTP", didTP);
        cir.getReturnValue().putInt("cpacooldown", coolDown);
    }
}

