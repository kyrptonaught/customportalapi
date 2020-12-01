package net.kyrptonaught.customportalapi.mixin;


import net.kyrptonaught.customportalapi.util.CreatePortal;
import net.kyrptonaught.customportalapi.util.PortalIgnitionSource;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
        if (CreatePortal.createPortal(world, pos, PortalIgnitionSource.BlockSource.FIRE))
            ci.cancel();
    }
}
