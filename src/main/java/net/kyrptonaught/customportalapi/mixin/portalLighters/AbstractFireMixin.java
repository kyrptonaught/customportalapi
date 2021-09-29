package net.kyrptonaught.customportalapi.mixin.portalLighters;


import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFireBlock.class)
public class AbstractFireMixin {

    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    public void detectCustomPortal(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        if (PortalPlacer.attemptPortalLight(world, pos, PortalIgnitionSource.FIRE))
            ci.cancel();
    }
}