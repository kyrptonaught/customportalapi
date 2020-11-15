package net.kyrptonaught.customportalapi.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityTPAccessor {
    @Accessor("inTeleportationState")
    void setinTeleportationState(boolean tpstate);

    @Accessor("syncedExperience")
    void setinsyncedExperience(int xp);

    @Accessor("syncedHealth")
    void setinsyncedHealth(float hp);

    @Accessor("syncedFoodLevel")
    void setinsyncedFoodLevel(int food);

    @Invoker("worldChanged")
    void invokeworldChanged(ServerWorld origin);
}
