package net.kyrptonaught.customportalapi.mixin.client;

import net.minecraft.block.Portal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PortalManager.class)
public interface PortalManagerAccessor {

    @Accessor
    Portal getPortal();

    @Accessor
    BlockPos getPos();
}
