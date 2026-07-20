package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Math;

//液体を扱う、液体を描画するブロックエンティティ
public abstract class AbstractFluidTankEntity extends AbstractFluidConcealBlockEntity {

    //直前の描画時刻を格納
    private long lastFrameTime;

    //タンクの滑らかに変化する貯蔵量を格納
    private float smoothedTankAmount = 0;

    public AbstractFluidTankEntity(BlockEntityType<?> blockEntityType,BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state,capacity);
        this.tank = new FluidTank(capacity){
            //内容が更新されたときの挙動
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
            }
        };
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        smoothedTankAmount = getFluidInTank(0).getAmount();
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        lastFrameTime = System.nanoTime();
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
