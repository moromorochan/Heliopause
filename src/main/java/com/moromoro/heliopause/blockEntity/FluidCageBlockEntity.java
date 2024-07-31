package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.render.FluidCageBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidCageBlockEntity extends AbstractFluidOrbBlockEntity {

    public FluidCageBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_CAGE_BE.get(), pos, state,32000);
    }
    //このブロックエンティティが上側かどうか取得
    private boolean isUpperPart(){
        return getBlockState().getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER;
    }

    //ブロックの設置向きが天井かどうか取得
    private boolean isOnCeiling(){
        return getBlockState().getValue(BlockStateProperties.VERTICAL_DIRECTION) == Direction.DOWN;
    }

    //対になるブロックの位置を取得
    public BlockPos getPairBlockPos(){
        BlockPos pos = this.getBlockPos();
        if(isOnCeiling() ^ isUpperPart()){
            return pos.below();
        }else{
            return pos.above();
        }
    }

    //対になるブロックエンティティを取得
    private FluidCageBlockEntity getPairBlockEntity() {
        BlockPos pos = getPairBlockPos();
        if(this.level!=null && pos!=null){
            BlockEntity pairBlockEntity = this.level.getBlockEntity(pos);
            if(pairBlockEntity instanceof FluidCageBlockEntity){
                return (FluidCageBlockEntity) pairBlockEntity;
            }
        }
        return null;
    }

    protected void updateRenderData() {
        FluidCageBlockRenderer.updateData(this.getBlockPos(), this.mainTank.getFluid());
    }

    protected void removeRenderData() {
        FluidCageBlockRenderer.removeData(this.getBlockPos());
    }

    @Override
    //レンダリング時のオフセットを設定
    public Vec3 centerOffset() {
        if(isOnCeiling()){
            return new Vec3(0,-(18.5f-8f)/16f,0);
        }else{
            return new Vec3(0,(18.5f-8f)/16f,0);
        }
    }

    //タンクへの操作を担う側のブロックエンティティを取得
    private FluidCageBlockEntity getOperationBlockEntity(){
        FluidCageBlockEntity entity = getPairBlockEntity();
        if(entity!=null) {
        return this.isUpperPart() ? entity : this;
        }
        return null;
    }

    /*----------------動作系 上下で分岐*/

    @Override
    public int getTanks() {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.getTanks();
        }
        return -1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.getFluidInTank(tank);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.getTankCapacity(tank);
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.isFluidValid(tank, stack);
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.fill(resource, action);
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.drain(resource,action);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidCageBlockEntity entity = getOperationBlockEntity();
        if(entity!=null) {
            return entity.mainTank.drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    /*----------------通信系 上下で分岐*/

    //データの読み込み

    @Override
    public void load(@NonNull CompoundTag nbt) {
        //Heliopause.LOGGER.debug("load");
        super.load(nbt);
    }

    //Capability関連
    @NonNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            FluidCageBlockEntity entity = getOperationBlockEntity();
            if(entity!=null) {
            return entity.fluidCapability.cast();
            }
        }
        return super.getCapability(cap, side);
    }

}
