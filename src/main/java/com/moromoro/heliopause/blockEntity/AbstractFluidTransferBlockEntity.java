package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//タンク容量は持たず、液体を移送するブロックエンティティ
public abstract class AbstractFluidTransferBlockEntity extends FluidHandlerBlockEntity implements IFluidHandler {
    private BlockPos operationBlockPos = null;//操作を送る対象の位置
    public EnumProperty<AttachFace> operationBlockFace = BlockStateProperties.ATTACH_FACE;//操作を送る対象の接続面
    public AbstractFluidTransferBlockEntity(@NotNull BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public BlockPos getOperationBlockPos() {
        //Heliopause.LOGGER.debug(operationBlockPos.toString());
        return operationBlockPos;
    }

    public void setOperationBlockPos(BlockPos operationBlockPos) {
        this.operationBlockPos = operationBlockPos;
        //Heliopause.LOGGER.debug(operationBlockPos.toString());
    }

    //タンクへの操作を担う側のブロックエンティティの液体ハンドラを取得
    private IFluidHandler getOperationBlockFluidHandler(){
        BlockPos opPos = getOperationBlockPos();
        if(this.level!=null && opPos!=null) {
            BlockEntity entity = this.level.getBlockEntity(opPos);
            if(entity==null)return null;
            if(entity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()){
                return (IFluidHandler) entity.getCapability(ForgeCapabilities.FLUID_HANDLER);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            BlockPos opPos = getOperationBlockPos();
            if(this.level!=null && opPos!=null) {
                BlockEntity entity = this.level.getBlockEntity(opPos);
                if(entity == level.getBlockEntity(this.getBlockPos())){
                    return super.getCapability(cap, side); //機能ブロックエンティティがこれを呼び出したときにループするのを防ぐ
                }
                else if(entity!=null){
                    return entity.getCapability(ForgeCapabilities.FLUID_HANDLER).cast();
                }
            }
        }
        return super.getCapability(cap, side);
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
