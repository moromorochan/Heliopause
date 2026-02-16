package com.moromoro.heliopause.fluid;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.EmptyFluid;
import org.jetbrains.annotations.NotNull;

public abstract class NoBlockFluidSource extends EmptyFluid {
    @Override
    public abstract @NotNull Item getBucket();
    protected boolean isEmpty() {
        return false;
    }
}
