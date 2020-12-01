package net.kyrptonaught.customportalapi.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BucketItem.class)
public interface BucketMixin {
    @Accessor("fluid")
    Fluid getFluidType();

    @Invoker("getEmptiedStack")
    ItemStack invokegetEmptiedStack(ItemStack stack, PlayerEntity player);
}
