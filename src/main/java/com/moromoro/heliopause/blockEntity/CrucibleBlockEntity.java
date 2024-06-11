package com.moromoro.heliopause.blockEntity;

import com.moromoro.heliopause.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlockEntity extends BlockEntity implements IFluidHandler {

    protected FluidTank mainTank;
    protected LazyOptional<IFluidHandler> holder;
    protected int lightLvl;

    //直前の描画時刻を格納
    public long lastFrameTime;
    //タンクの滑らかに変化する貯蔵量を格納
    public float smoothedTankAmount;
    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CRUCIBLE_BE.get(), pos, state);
        mainTank = new FluidTank(1000/*FluidType.BUCKET_VOLUME*/);
        //mainTank.setFluid(new FluidStack(Fluids.WATER,1000));
        holder = LazyOptional.of(() -> mainTank);
    }

    @Override
    public void load(CompoundTag tag){
        super.load(tag);
        this.mainTank.setFluid(FluidStack.loadFluidStackFromNBT(tag.getCompound("FluidStack")));

        //初期化
        smoothedTankAmount = mainTank.getFluidAmount();
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidStack",mainTank.getFluid().writeToNBT(new CompoundTag()));
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
        int result =mainTank.fill(resource,action);
        if(action.execute())
        {setChanged();}
        return result;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack result=mainTank.drain(resource,action);
        if(action.execute())
        {setChanged();}
        return result;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack result=mainTank.drain(maxDrain,action);
        if(action.execute())
        {setChanged();}
        return result;
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
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

}
