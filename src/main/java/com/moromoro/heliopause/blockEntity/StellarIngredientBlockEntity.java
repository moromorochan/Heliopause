package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StellarIngredientBlockEntity extends BlockEntity {

    protected FluidTank fluidHandler = new FluidTank(FluidType.BUCKET_VOLUME){
        // 内容更新毎にセーブ
        @Override
        protected void onContentsChanged()
        {
            super.onContentsChanged();
            StellarIngredientBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int fill(FluidStack resource, FluidAction action)
        {
            // 搬入不可
            return 0;
        }
    };

    protected ItemStackHandler itemHandler = new ItemStackHandler(1){
        // 内容更新毎にセーブ
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            StellarIngredientBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // 搬入不可
            return stack;
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public StellarIngredientBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.STELLAR_INGREDIENT_BE.get(), blockPos, blockState);
    }

    @Override
    public void load(@NotNull CompoundTag nbt)
    {
        super.load(nbt);

        //ItemStack itemStack = nbt.contains("itemStack") ? ItemStack.of(nbt.getCompound("itemStack")): ItemStack.EMPTY;
        //FluidStack fluidStack = nbt.contains("fluidStack") ? FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluidStack")): FluidStack.EMPTY;

        //itemHandler.setStackInSlot(0, itemStack);
        //fluidHandler.setFluid(fluidStack);
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        fluidHandler.readFromNBT(nbt.getCompound("Tank"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.put("Tank", fluidHandler.writeToNBT(new CompoundTag()));
       /* if(!itemHandler.getStackInSlot(0).isEmpty()){
            nbt.put("itemStack", itemHandler.getStackInSlot(0).save(new CompoundTag()));
        }
        if (!fluidHandler.getFluidInTank(0).isEmpty()) {
            nbt.put("fluidStack", fluidHandler.getFluidInTank(0).writeToNBT(new CompoundTag()));
        }*/
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        this.saveAdditional(nbt);
        return nbt;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag nbt) {
        super.handleUpdateTag(nbt);
        this.load(nbt);
    }

    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return lazyFluidHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(()-> fluidHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    public void setStellarStack(CircumstellarIngredient.StellarStack stellarStack){
        if(level!= null && !level.isClientSide()) {
            this.itemHandler.setStackInSlot(0,stellarStack.itemStack().copy());
            this.fluidHandler.setFluid(stellarStack.fluidStack().copy());
        }
    }

    /*public CircumstellarIngredient.StellarStack getStellarStack() {
        return new CircumstellarIngredient.StellarStack(this.itemHandler.getStackInSlot(0), this.tank.getFluidInTank(0));
    }*/

    public void checkRemove() {
        if (level == null || level.isClientSide) {
            return;
        }

        // 内容物がないならブロック削除
        if (this.itemHandler.getStackInSlot(0).isEmpty() && this.fluidHandler.getFluidInTank(0).isEmpty()) {
            level.removeBlock(this.worldPosition, false);
        }
    }
}