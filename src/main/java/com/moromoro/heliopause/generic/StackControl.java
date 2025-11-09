package com.moromoro.heliopause.generic;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class StackControl {
    //液体の移動
    public static FluidStack transferFluid(IFluidHandler fillStack, IFluidHandler drainStack, int maxTransfer){
        // fillStackにどれだけ流し入れられるか確認 0なら動作を終わる
        int fillAllowance = fillStack.fill(drainStack.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.SIMULATE);
        if (fillAllowance > 0) {
            // drainStackから液体を取り出す
            FluidStack drainAllowance = drainStack.drain(fillAllowance, IFluidHandler.FluidAction.EXECUTE);
            if (!drainAllowance.isEmpty()) {
                // fillStackの液体を増やす
                fillStack.fill(drainAllowance, IFluidHandler.FluidAction.EXECUTE);
            }
            return drainAllowance;
        }
        return FluidStack.EMPTY;
    }

    //2値間でどれだけ移送できるかの確認
    public  static int checkTransferAmount(int fillAmount, int fillLimit, int drainAmount, int maxTransfer){
        return Math.min(Math.min(fillLimit - fillAmount, drainAmount), maxTransfer);
    }
}
