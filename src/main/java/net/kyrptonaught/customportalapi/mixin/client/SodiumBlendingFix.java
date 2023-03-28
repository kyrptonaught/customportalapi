package net.kyrptonaught.customportalapi.mixin.client;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.ColorSampler;
import me.jellysquid.mods.sodium.client.util.color.ColorARGB;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.model.quad.blender.LinearColorBlender")
public abstract class SodiumBlendingFix {

    @Shadow(remap = false)
    protected abstract <T> int getBlockColor(BlockRenderView world, T state, ColorSampler<T> sampler, int x, int y, int z, int colorIdx);

    @Inject(method = "getVertexColor", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private <T> void onGetVertexColor(BlockRenderView world, BlockPos origin, ModelQuadView quad, ColorSampler<T> sampler, T state, int vertexIdx, CallbackInfoReturnable<Integer> info) {
        // credit to https://github.com/Juuxel/unofficial-sodium-biome-blending-fix
        if (state instanceof BlockState && ((BlockState) state).getBlock() instanceof CustomPortalBlock) {
            int color = getBlockColor(world, state, sampler, origin.getX(), origin.getY(), origin.getZ(), quad.getColorIndex());
            info.setReturnValue(ColorARGB.toABGR(color));
        }
    }
}
