package net.kyrptonaught.customportalapi.util;

import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.portal.PortalPlacer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CustomPortalFluidProvider {

    Fluid getFluidContent();

    ItemStack emptyContents(ItemStack stack, PlayerEntity player);

    default void CPAonFluidPlaced(World world, BlockPos fluidPos) {
        PortalPlacer.attemptPortalLight(world, fluidPos, fluidPos.down(), PortalIgnitionSource.FluidSource(getFluidContent()));
    }
}
