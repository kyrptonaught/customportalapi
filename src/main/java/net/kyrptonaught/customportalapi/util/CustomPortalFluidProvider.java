package net.kyrptonaught.customportalapi.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

public interface CustomPortalFluidProvider {

    Fluid getFluidContent();

    ItemStack emptyContents(ItemStack stack, PlayerEntity player);
}
