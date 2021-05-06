package net.kyrptonaught.customportalapi.mixin.client;


import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.ClientPlayerInColoredPortal;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.EntityInCustomPortal;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin extends LivingEntity implements ClientPlayerInColoredPortal {

    @Shadow
    public float lastNauseaStrength;

    @Shadow
    public float nextNauseaStrength;

    @Shadow
    @Final
    protected MinecraftClient client;

    protected ClientPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
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
        } else if (((EntityInCustomPortal) this).getTimeInPortal() > 0) {
            PortalLink link = CustomPortalApiRegistry.getPortalLinkFromBase(CustomPortalsMod.getPortalBase(this.world, this.getBlockPos()));
            setLastUsedPortalColor(link != null ? link.colorID : 1908001);
            updateCustomNausea();
            ci.cancel();
        }
    }

    @Unique
    private void updateCustomNausea() {
        this.lastNauseaStrength = this.nextNauseaStrength;
        if (((EntityInCustomPortal) this).getTimeInPortal() > 0) {
            if (this.client.currentScreen != null && !this.client.currentScreen.isPauseScreen()) {
                if (this.client.currentScreen instanceof HandledScreen) {
                    this.closeHandledScreen();
                }

                this.client.openScreen(null);
            }

            if (this.nextNauseaStrength == 0.0F) {
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
