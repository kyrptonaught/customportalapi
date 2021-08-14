package net.kyrptonaught.customportalapi.mixin.client;

import com.google.common.collect.Sets;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.networking.NetworkManager;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(World.class)
public abstract class ClientWorldMixin implements WorldAccess {
    private static final Set<BlockPos> checkedPos = Sets.newConcurrentHashSet();

    @Shadow
    public abstract boolean setBlockState(BlockPos pos, BlockState state);

    @Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
    public void swapPortalBlocks(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (NetworkManager.isServerSideOnlyMode()) {
            BlockState state = cir.getReturnValue();
            if (!checkedPos.contains(pos) && state.getBlock() instanceof NetherPortalBlock) {
                checkedPos.add(pos);
                Block baseBlock = CustomPortalsMod.getPortalBase(this, pos);
                if (baseBlock != null && !(baseBlock instanceof AirBlock)) {
                    PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(baseBlock);
                    if (link != null) {
                        BlockState newState = CustomPortalsMod.blockWithAxis(link.getPortalBlock(false).getDefaultState(), CustomPortalsMod.getAxisFrom(state));
                        checkedPos.remove(pos);
                        this.setBlockState(pos, newState);
                        cir.setReturnValue(newState);
                    }
                }
            }
        }
    }
}
