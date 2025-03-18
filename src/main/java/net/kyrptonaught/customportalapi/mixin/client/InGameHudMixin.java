package net.kyrptonaught.customportalapi.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.BlockState;
import net.minecraft.block.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.dimension.PortalManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private int lastColor = -1;

    @WrapOperation(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;getWhite(F)I", ordinal = 0))
    public int changeColor(float alpha, Operation<Integer> original) {
        isCustomPortal(client.player);
        if (lastColor >= 0)
            return ColorHelper.withAlpha(ColorHelper.channelFromFloat(alpha), lastColor);
        return original.call(alpha);
    }

    @WrapOperation(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModelParticleSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
    public Sprite renderCustomPortalOverlay(BlockModels instance, BlockState state, Operation<Sprite> original) {
        if (lastColor >= 0) {
            return original.call(instance, CustomPortalsMod.portalBlock.getDefaultState());
        }
        return original.call(instance, state);
    }


    @Unique
    private void isCustomPortal(ClientPlayerEntity player) {
        PortalManager portalManager = player.portalManager;
        Portal portalBlock = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPortal() : null;
        BlockPos portalPos = portalManager != null && portalManager.isInPortal() ? ((PortalManagerAccessor) portalManager).getPos() : null;

        if (portalBlock == null) {
            return;
        }

        if (portalBlock instanceof CustomPortalBlock) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalBlock).getPortalBase(player.clientWorld, portalPos));
            if (link != null) {
                lastColor = link.colorID;
                return;
            }
        }

        lastColor = -1;
    }
}
