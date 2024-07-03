package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.render.CrucibleBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CrucibleBlockEntity extends AbstractFluidTankEntity {

    //直前の描画時刻を格納
    public long lastFrameTime;
    //タンクの滑らかに変化する貯蔵量を格納
    public float smoothedTankAmount = 0;
    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CRUCIBLE_BE.get(), pos, state,1000);
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
    protected void updateRenderData() {
        CrucibleBlockRenderer.updateData(this.getBlockPos(), mainTank.getFluid());
    }
    @Override
    protected void removeRenderData() {
        CrucibleBlockRenderer.removeData(this.getBlockPos());
    }
}
