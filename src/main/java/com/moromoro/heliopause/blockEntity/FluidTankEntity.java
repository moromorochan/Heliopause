package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidTankEntity extends FluidHandlerBlockEntity {
    private FluidTank tank;

    public FluidTankEntity(BlockEntityType<?> entityType,BlockPos pos, BlockState state, int capacity){
        super(entityType,pos,state);
        this.tank = new FluidTank(capacity);
    }

    public FluidTank getTank(){
        return this.tank;
    }

    //液体を入れる
    public int addFluid(FluidStack fluid){
        return this.tank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
    }

    //液体を取り出す
    public FluidStack removeFluid(int amount){
        return this.tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
    }
}
