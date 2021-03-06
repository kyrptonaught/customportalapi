package net.kyrptonaught.customportalapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.ClientPlayerInColoredPortal;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V"))
    public void changeColor(float red, float green, float blue, float alpha) {
        int color = ((ClientPlayerInColoredPortal) client.player).getLastUsedPortalColor();
        if (color >= 0) {
            float[] colors = ColorUtil.getColorForBlock(color);
            RenderSystem.color4f(colors[0], colors[1], colors[2], alpha);
        } else
            RenderSystem.color4f(red, green, blue, alpha);
    }

    @Redirect(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getSprite(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/texture/Sprite;"))
    public Sprite renderCustomPortalOverlay(BlockModels blockModels, BlockState blockState) {
        if (((ClientPlayerInColoredPortal) client.player).getLastUsedPortalColor() >= 0) {
            return blockModels.getSprite(CustomPortalsMod.portalBlock.getDefaultState());
        }
        return blockModels.getSprite(Blocks.NETHER_PORTAL.getDefaultState());
    }
}
