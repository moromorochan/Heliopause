package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlockEntity extends BlockEntity implements IFluidHandler {

    protected FluidTank mainTank;
    protected LazyOptional<IFluidHandler> holder;
    protected int lightLvl;
    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(Heliopause.CRUCIBLE_BE.get(), pos, state);
        mainTank = new FluidTank(FluidType.BUCKET_VOLUME);
        //mainTank.setFluid(new FluidStack(Fluids.WATER,1000));
        holder = LazyOptional.of(() -> mainTank);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.mainTank.setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompound("FluidStack")));
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidStack",this.mainTank.getFluid().writeToNBT(new CompoundTag()));
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == ForgeCapabilities.FLUID_HANDLER)
            return holder.cast();
        return super.getCapability(capability, facing);
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
        return mainTank.fill(resource,action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return mainTank.drain(resource,action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return mainTank.drain(maxDrain,action);
    }
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet){
        this.load(packet.getTag());
    }

    public void send2Client(){
        if(!level.isClientSide()){
            level.sendBlockUpdated(getBlockPos(),getBlockState(),getBlockState(),3);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(){
        return saveWithoutMetadata();
    }

}
