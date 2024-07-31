package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Math;

public abstract class AbstractFluidTankEntity extends AbstractFluidConcealBlockEntity {

    //直前の描画時刻を格納
    private long lastFrameTime;

    //タンクの滑らかに変化する貯蔵量を格納
    private float smoothedTankAmount = 0;

    public AbstractFluidTankEntity(BlockEntityType<?> blockEntityType,BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state,capacity);
        this.mainTank = new FluidTank(capacity){
            //内容が更新されたときの挙動
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
                if (level != null && !level.isClientSide) {
                    //Heliopause.LOGGER.debug("contentChanged_abstract");
                    updateRenderData();
                }
            }
        };
    }

    protected abstract void updateRenderData();
    protected abstract void removeRenderData();

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            removeRenderData();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            removeRenderData();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            updateRenderData();
        }
        smoothedTankAmount = getFluidInTank(0).getAmount();
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        lastFrameTime = System.nanoTime();
        if (level != null && !level.isClientSide) {
            updateRenderData();
        }
        setSmoothedTankAmount(getFluidInTank(0).getAmount());
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    public float getSmoothedTankAmount() {
        return smoothedTankAmount;
    }

    public void setSmoothedTankAmount(float smoothedTankAmount) {
        this.smoothedTankAmount = Math.clamp(0,this.getTankCapacity(0),smoothedTankAmount);
    }
}
