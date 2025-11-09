package com.moromoro.heliopause.blockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

//液体を扱う、液体を液球として描画するブロックエンティティ
public abstract class AbstractFluidOrbBlockEntity extends AbstractFluidTankEntity {
    //回転の進捗を格納
    protected float rotationOffset;
    //浮き沈みの進捗を格納
    private float waveOffset;

    public AbstractFluidOrbBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state,capacity);
    }

    //レンダリング時のオフセットを設定
    public Vec3 centerOffset() {
        return new Vec3(0,0,0);
    }

    public float getRotationOffset() {
        return rotationOffset;
    }

    public void setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset%360;
    }

    public float getWaveOffset() {
        return waveOffset;
    }

    public void setWaveOffset(float waveOffset) {
        this.waveOffset = waveOffset%360;
    }
}