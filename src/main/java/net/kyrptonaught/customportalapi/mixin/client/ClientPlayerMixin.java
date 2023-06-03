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
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
    @Final
    protected MinecraftClient client;

    public ClientPlayerMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }


    @Shadow
    public abstract void closeHandledScreen();

    @Shadow public float prevNauseaIntensity;
    @Shadow public float nauseaIntensity;
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
            PortalLink link = this.getInPortalPos() != null ? CustomPortalApiRegistry.getPortalLinkFromBase(CustomPortalHelper.getPortalBase(this.getWorld(), this.getInPortalPos())) : null;
            if (link != null)
                setLastUsedPortalColor(link.colorID);
            updateCustomNausea(previousColor);
            ci.cancel();
        }
    }

    @Unique
    private void updateCustomNausea(int previousColor) {
        this.prevNauseaIntensity = this.nauseaIntensity;
        if (this.getTimeInPortal() > 0) {
            if (this.client.currentScreen != null && !this.client.currentScreen.shouldPause() && !(this.client.currentScreen instanceof DeathScreen) && !(this.client.currentScreen instanceof DownloadingTerrainScreen)) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }
                this.client.setScreen(null);
            }

            if (this.nauseaIntensity == 0.0F && previousColor != -999) { //previous color prevents this from playing after a teleport. A tp sets the previousColor to -999
                PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(CustomPortalHelper.getPortalBase(getWorld(), getInPortalPos()));
                if (link != null && link.getInPortalAmbienceEvent().hasEvent()) {
                    this.client.getSoundManager().play(link.getInPortalAmbienceEvent().execute(this).getInstance());
                } else
                    this.client.getSoundManager().play(PositionedSoundInstance.ambient(SoundEvents.BLOCK_PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            this.nauseaIntensity += 0.0125F;
            if (this.nauseaIntensity >= 1.0F) {
                this.nauseaIntensity = 1.0F;
            }
        } else if (this.hasStatusEffect(StatusEffects.NAUSEA) && this.getStatusEffect(StatusEffects.NAUSEA).getDuration() > 60) {
            this.nauseaIntensity += 0.006666667F;
            if (this.nauseaIntensity > 1.0F) {
                this.nauseaIntensity = 1.0F;
            }
        } else {
            if (this.nauseaIntensity > 0.0F) {
                this.nauseaIntensity -= 0.05F;
            }

            if (this.nauseaIntensity < 0.0F) {
                this.nauseaIntensity = 0.0F;
            }
        }
    }
}
