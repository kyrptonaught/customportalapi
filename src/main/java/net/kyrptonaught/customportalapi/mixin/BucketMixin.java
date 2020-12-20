package net.kyrptonaught.customportalapi.mixin;

import net.kyrptonaught.customportalapi.util.CustomPortalFluidProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public abstract class BucketMixin implements CustomPortalFluidProvider {
    @Shadow @Final private Fluid fluid;

    @Override
    public Fluid getFluidContent() {
        return this.fluid;
    }

    @Override
    public ItemStack emptyContents(ItemStack stack, PlayerEntity player) {
        return BucketItem.getEmptiedStack(stack, player);
    }
}
