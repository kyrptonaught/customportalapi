package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.util.CustomPortalFluidProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BucketItem.class)
public abstract class BucketMixin implements CustomPortalFluidProvider {
    @Shadow
    @Final
    private Fluid fluid;

    @Shadow
    public static ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player) {
        return null;
    }

    @Override
    public Fluid getFluidContent() {
        return this.fluid;
    }

    @Override
    public ItemStack emptyContents(ItemStack stack, PlayerEntity player) {
        return getEmptiedStack(stack, player);
    }
}
