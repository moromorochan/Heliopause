package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

//タンク容量は持たず、液体を移送するブロックエンティティ
public abstract class AbstractFluidTransferBlockEntity extends FluidHandlerBlockEntity implements IFluidHandler {
    public BlockPos operationBlockPos = this.getBlockPos();//操作を送る対象の位置
    public EnumProperty<AttachFace> operationBlockFace = BlockStateProperties.ATTACH_FACE;//操作を送る対象の接続面
    public AbstractFluidTransferBlockEntity(@NotNull BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    //タンクへの操作を担う側のブロックエンティティの液体ハンドラを取得
    private IFluidHandler getOperationBlockFluidHandler(){
        if(this.level!=null && operationBlockPos!=null) {
            BlockEntity entity = this.level.getBlockEntity(operationBlockPos);
            if(entity==null)return null;
            if(entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()){
                return (IFluidHandler) entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
            }
        }
        return null;
    }

    @Override
    public int getTanks() {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.getTanks();
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.getFluidInTank(tank);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.getTankCapacity(tank);
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.isFluidValid(tank,stack);
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.fill(resource,action);
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.drain(resource,action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        IFluidHandler opHandler = getOperationBlockFluidHandler();
        if(opHandler!=null){
            return opHandler.drain(maxDrain,action);
        }
        return FluidStack.EMPTY;
    }
}
