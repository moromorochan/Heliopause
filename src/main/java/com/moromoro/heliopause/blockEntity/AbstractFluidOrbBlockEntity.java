package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class AbstractFluidOrbBlockEntity extends AbstractFluidTankEntity {

    //直前のフレーム描画時刻を格納
    public long lastFrameTime;
    //回転の進捗を格納
    public float rotationOffset;
    //浮き沈みの進捗を格納
    public float waveOffset;
    //タンクの滑らかに変化する貯蔵量を格納
    public float smoothedTankAmount = 0;

    public AbstractFluidOrbBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state,capacity);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        fluidCapability = LazyOptional.of(() -> mainTank);
        smoothedTankAmount = getFluidInTank(0).getAmount();
    }

    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        smoothedTankAmount = getFluidInTank(0).getAmount();
    }

    @Override
    protected abstract void updateRenderData();
    @Override
    protected abstract void removeRenderData();

    //レンダリング時のオフセットを設定
    public Vec3 centerOffset() {
        return new Vec3(0,0,0);
    }
}