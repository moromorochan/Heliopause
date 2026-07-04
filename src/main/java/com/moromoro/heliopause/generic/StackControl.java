package com.moromoro.heliopause.generic;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;

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
    
    // ホッパー対応アイテムハンドラ作成
    public static @NotNull IItemHandler createFilteredItemHandler(IItemHandler itemHandler, IntArrayList allowedSlots, boolean allowInsert, boolean allowExtract) {
        // 重複を無くす
        List<Integer> whiteList = new IntArrayList(new LinkedHashSet<>(allowedSlots));
        
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return whiteList.size();
            }
            
            // 外部→内部
            private int toInternal(int externalSlot) {
                if (externalSlot < 0 || externalSlot >= whiteList.size()) {
                    return -1;
                }
                return whiteList.get(externalSlot);
            }
            
            // 内部→外部
            private int toExternal(int internalSlot) {
                return whiteList.indexOf(internalSlot);
            }
            
            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                int internal = toInternal(slot);
                if (internal != -1) {
                    return itemHandler.getStackInSlot(internal);
                }
                return ItemStack.EMPTY;
            }
            
            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (allowInsert) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.insertItem(internal, stack, simulate);
                    }
                }
                return stack;
            }
            
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (allowExtract) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.extractItem(internal, amount, simulate);
                    }
                }
                return ItemStack.EMPTY;
            }
            
            @Override
            public int getSlotLimit(int slot) {
                int internal = toInternal(slot);
                if (internal != -1) {
                    return itemHandler.getSlotLimit(internal);
                }
                return 0;
            }
            
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (allowInsert) {
                    int internal = toInternal(slot);
                    if (internal != -1) {
                        return itemHandler.isItemValid(internal, stack);
                    }
                }
                return false;
            }
        };
    }
    
}
