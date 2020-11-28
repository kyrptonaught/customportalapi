package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.util.CreatePortal;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class BucketMixin {

    @Shadow
    @Final
    private Fluid fluid;

    @Inject(method = "placeFluid", at = @At("HEAD"), cancellable = true)
    public void use(PlayerEntity player, World world, BlockPos pos, BlockHitResult blockHitResult, CallbackInfoReturnable<Boolean> cir) {
        if (this.fluid.isIn(FluidTags.WATER)) {
            if (CreatePortal.createPortal(world, pos, Blocks.WATER)) {
                cir.setReturnValue(true);
            }
        }
    }
}
