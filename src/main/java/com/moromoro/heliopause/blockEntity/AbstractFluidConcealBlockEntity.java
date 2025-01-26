package com.moromoro.heliopause.blockEntity;

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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//液体を扱う、液体を描画しないブロックエンティティ
public abstract class AbstractFluidConcealBlockEntity  extends FluidHandlerBlockEntity implements IFluidHandler {

    protected LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> this.tank);

    //protected FluidTank mainTank;

    public AbstractFluidConcealBlockEntity(BlockEntityType<?> blockEntityType,BlockPos pos, BlockState state, int capacity) {
        super(blockEntityType, pos, state);
        this.tank.setCapacity(capacity);
        /*this.mainTank = new FluidTank(capacity){
            //内容が更新されたときの挙動
            @Override
            protected void onContentsChanged() {
                super.onContentsChanged();
                setChanged();
                if (level != null && !level.isClientSide) {
                    //Heliopause.LOGGER.debug("contentChanged_abstract");
                    //updateRenderData();
                }
            }
        };*/
    }

    @Override
    public void onLoad() {
        super.onLoad();
        fluidCapability = LazyOptional.of(() -> this.tank);
    }

    @Override
    public int getTanks() {
        return this.tank.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.tank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.tank.isFluidValid(tank,stack);
    }


    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.tank.fill(resource,action);
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.tank.drain(resource,action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.tank.drain(maxDrain,action);
    }

    //変更の保存
    @Override
    public void setChanged() {
        super.setChanged();
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        this.tank.readFromNBT(nbt);
    }
    //データの書き出し
    @Override
    protected void saveAdditional(@NonNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        this.tank.writeToNBT(nbt);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
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
