package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFluidTankEntity extends FluidHandlerBlockEntity implements IFluidHandler {

    protected LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> this.mainTank);

    protected FluidTank mainTank;

    //直前の描画時刻を格納
    public long lastFrameTime;

    public AbstractFluidTankEntity(BlockEntityType<?> blockEntityType,BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state);
        this.mainTank = new FluidTank(capacity){
            //内容が更新されたときの挙動
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
                if (level != null && !level.isClientSide) {
                    Heliopause.LOGGER.debug("contentChanged_abstract");
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
            Heliopause.LOGGER.debug("removed_abstract");
            removeRenderData();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            Heliopause.LOGGER.debug("unloaded_abstract");
            removeRenderData();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            updateRenderData();
        }
    }

    @Override
    public int getTanks() {
        return mainTank.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return mainTank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return mainTank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return mainTank.isFluidValid(tank,stack);
    }


    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int result = mainTank.fill(resource,action);
        return result;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack result = mainTank.drain(resource,action);
        return result;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack result = mainTank.drain(maxDrain,action);
        return result;
    }

    //変更の保存
    @Override
    public void setChanged() {
        super.setChanged();
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        //mainTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompound("FluidStack")));
        super.load(nbt);
        mainTank.readFromNBT(nbt);
        lastFrameTime = System.nanoTime();
        if (level != null && !level.isClientSide) {
            updateRenderData();
        }
    }
    //データの書き出し
    @Override
    protected void saveAdditional(@NonNull CompoundTag nbt) {
        //nbt.put("FluidStack", mainTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(nbt);
        mainTank.writeToNBT(nbt);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        //this.load(Objects.requireNonNull(packet.getTag()));
        CompoundTag nbt = packet.getTag();
        load(nbt == null ? new CompoundTag() : nbt);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }
    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        load(nbt);
    }

    //Capability関連
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
    }
    @NonNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        return super.getCapability(cap, side);
    }

}
