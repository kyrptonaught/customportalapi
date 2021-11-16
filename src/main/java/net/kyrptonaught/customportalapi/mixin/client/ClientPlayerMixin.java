package net.kyrptonaught.customportalapi.mixin.client;


import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.interfaces.ClientPlayerInColoredPortal;
import net.kyrptonaught.customportalapi.interfaces.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin extends PlayerEntity implements EntityInCustomPortal, ClientPlayerInColoredPortal {

    @Shadow
    public float lastNauseaStrength;

    @Shadow
    public float nextNauseaStrength;

    @Shadow
    @Final
    protected MinecraftClient client;

    public ClientPlayerMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void closeHandledScreen();

    int portalColor;

    @Override
    public void setLastUsedPortalColor(int color) {
        this.portalColor = color;

    }

    @Override
    public int getLastUsedPortalColor() {
        return portalColor;
    }


    @Inject(method = "updateNausea", at = @At(value = "HEAD"), cancellable = true)
    public void injectCustomNausea(CallbackInfo ci) {
        if (this.inNetherPortal) {
            setLastUsedPortalColor(-1);
        } else if (this.getTimeInPortal() > 0) {
            int previousColor = getLastUsedPortalColor();
            PortalLink link = this.getInPortalPos() != null ? CustomPortalApiRegistry.getPortalLinkFromBase(CustomPortalHelper.getPortalBase(this.world, this.getInPortalPos())) : null;
            if (link != null)
                setLastUsedPortalColor(link.colorID);
            updateCustomNausea(previousColor);
            ci.cancel();
        }
    }

    @Unique
    private void updateCustomNausea(int previousColor) {
        this.lastNauseaStrength = this.nextNauseaStrength;
        if (this.getTimeInPortal() > 0) {
            if (this.client.currentScreen != null && !this.client.currentScreen.isPauseScreen()) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }
                this.client.setScreen(null);
            }

            if (this.nextNauseaStrength == 0.0F && previousColor != -999) { //previous color prevents this from playing after a teleport. A tp sets the previousColor to -999
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(CustomPortalHelper.getPortalBase(world, getInPortalPos()));
                if (link != null && link.getInPortalAmbienceEvent().hasEvent()) {
                    this.client.getSoundManager().play(link.getInPortalAmbienceEvent().execute(this).getInstance());
                } else
                    this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            this.nextNauseaStrength += 0.0125F;
            if (this.nextNauseaStrength >= 1.0F) {
                this.nextNauseaStrength = 1.0F;
            }
        } else if (this.hasStatusEffect(StatusEffects.NAUSEA) && this.getStatusEffect(StatusEffects.NAUSEA).getDuration() > 60) {
            this.nextNauseaStrength += 0.006666667F;
            if (this.nextNauseaStrength > 1.0F) {
                this.nextNauseaStrength = 1.0F;
            }
        } else {
            if (this.nextNauseaStrength > 0.0F) {
                this.nextNauseaStrength -= 0.05F;
            }

            if (this.nextNauseaStrength < 0.0F) {
                this.nextNauseaStrength = 0.0F;
            }
        }
    }
}
