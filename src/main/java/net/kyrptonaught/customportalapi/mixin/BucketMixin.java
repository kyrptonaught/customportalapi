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
    protected abstract ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player);

    @Override
    public Fluid getFluidContent() {
        return this.fluid;
    }

    @Override
    public ItemStack emptyContents(ItemStack stack, PlayerEntity player) {
        return this.getEmptiedStack(stack, player);
    }
}
