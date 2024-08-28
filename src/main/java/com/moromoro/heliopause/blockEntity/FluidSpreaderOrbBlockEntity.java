package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.render.FluidSpreaderOrbBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FluidSpreaderOrbBlockEntity extends AbstractFluidOrbBlockEntity{

    //リングのアニメーション変数を格納
    private float ringRotationOffset;
    private float smoothedRingDensity;

    public FluidSpreaderOrbBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), pos, state, 1000);
    }

    @Override
    protected void updateRenderData() {
        FluidSpreaderOrbBlockRenderer.updateData(this.getBlockPos(), mainTank.getFluid());
    }

    @Override
    protected void removeRenderData() {
        FluidSpreaderOrbBlockRenderer.removeData(this.getBlockPos());
    }

    public float getRingRotationOffset() {
        return ringRotationOffset;
    }

    public void setRingRotationOffset(float rotationOffset) {
        this.ringRotationOffset = rotationOffset%360;
    }

    public float getSmoothedRingDensity() {
        return smoothedRingDensity;
    }

    public void setSmoothedRingDensity(float density) {
        this.smoothedRingDensity = Math.max(0,Math.min(1,density));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getFluidInTank(0).isEmpty()) {
            setSmoothedRingDensity(0f);
        } else {
            setSmoothedTankAmount(1f);
        }
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        if (getFluidInTank(0).isEmpty()) {
            setSmoothedRingDensity(0f);
        } else {
            setSmoothedTankAmount(1f);
        }
    }
}
