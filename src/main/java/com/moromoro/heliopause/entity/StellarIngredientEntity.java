package com.moromoro.heliopause.entity;

import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class StellarIngredientEntity extends Entity {
    public static final float SIZE = 10f/16f;
    private static final EntityDataAccessor<CompoundTag> syncTag =
        SynchedEntityData.defineId(StellarIngredientEntity.class, EntityDataSerializers.COMPOUND_TAG);

    protected FluidTank fluidHandler = new FluidTank(FluidType.BUCKET_VOLUME){
        // 内容更新毎にセーブ
        /*@Override
        protected void onContentsChanged()
        {
            super.onContentsChanged();
            StellarIngredientBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }*/

        @Override
        public int fill(FluidStack resource, FluidAction action)
        {
            // 搬入不可
            return 0;
        }
    };

    protected ItemStackHandler itemHandler = new ItemStackHandler(1){
        // 内容更新毎にセーブ
        /*@Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            StellarIngredientBlockEntity.this.setChanged();
            if(level != null){
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }*/

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // 搬入不可
            return stack;
        }
    };

    private static final int LIFETIME = 6000;
    private int age; // 5分でエンティティを削除
    private int health = 5;
    Vec3 velocity = Vec3.ZERO;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public StellarIngredientEntity(EntityType<? extends Entity> entityType, Level level) {
        super(EntityRegistry.STELLAR_INGREDIENT_E.get(), level);
        fluidHandler.setFluid(FluidStack.EMPTY);
        itemHandler.setStackInSlot(0,ItemStack.EMPTY);

        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(()-> fluidHandler);
    }

    public StellarIngredientEntity(Level level, Vec3 pos, Vec3 velocity, CircumstellarIngredient ingredient) {
        super(EntityRegistry.STELLAR_INGREDIENT_E.get(), level);
        this.setPos(pos.x(),pos.y(),pos.z());
        this.velocity = velocity;
        setDeltaMovement(velocity);
        this.setStellarStack(ingredient.getStellarStack());
        this.noPhysics = false;
        this.setNoGravity(true);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return lazyFluidHandler.cast();

        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    public void setStellarStack(CircumstellarIngredient.StellarStack stellarStack){
        if(!level().isClientSide()) {
            this.itemHandler.setStackInSlot(0,stellarStack.itemStack().copy());
            this.fluidHandler.setFluid(stellarStack.fluidStack().copy());
            CompoundTag nbt = new CompoundTag();
            addAdditionalSaveData(nbt);
            this.entityData.set(syncTag, nbt);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(syncTag, new CompoundTag());
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if(key.equals(syncTag)){
            readAdditionalSaveData(this.entityData.get(syncTag));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        fluidHandler.readFromNBT(nbt.getCompound("Tank"));
        age = nbt.getInt("age");
        CompoundTag velocityTag = nbt.getCompound("velocity");
        velocity = new Vec3(velocityTag.getFloat("x"), velocityTag.getFloat("y"), velocityTag.getFloat("z"));

        this.entityData.set(syncTag, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.put("Tank", fluidHandler.writeToNBT(new CompoundTag()));
        nbt.putInt("age", age);

        CompoundTag velocityTag = new CompoundTag();
        velocityTag.putFloat("x",(float) velocity.x);
        velocityTag.putFloat("y",(float) velocity.y);
        velocityTag.putFloat("z",(float) velocity.z);
        nbt.put("velocity", velocityTag);
    }

    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level(),this, inventory);
    }

    @Override public boolean canBeCollidedWith() {
        return super.canBeCollidedWith();
    }

    @Override public boolean isPickable() { return true; }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand){
        if (!this.level().isClientSide) {
            //右クリックされたとき
            // 入力を取得
            ItemStack inputStack = player.getItemInHand(hand);
            // 液体取り出し
            inputStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(itemFluidCap -> {
                FluidStack available = fluidHandler.getFluidInTank(0);
                if (!available.isEmpty()) {
                    // 入れられる量確認
                    int canFill = itemFluidCap.fill(available, IFluidHandler.FluidAction.SIMULATE);
                    if (canFill > 0) {
                        // 操作を実行
                        FluidStack toDrain = available.copy();
                        toDrain.setAmount(canFill);
                        fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                        itemFluidCap.fill(toDrain, IFluidHandler.FluidAction.EXECUTE);

                        // 効果音を再生
                        SoundEvent sound = toDrain.getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                        if(sound != null) {
                            this.playSound(sound, 1, 1);
                        }
                        // アイテムを更新
                        player.setItemInHand(hand, itemFluidCap.getContainer());
                    }
                }
            });

            // アイテム取り出し
            ItemStack stackInSlot = itemHandler.getStackInSlot(0);
            if (!stackInSlot.isEmpty()) {
                // 操作を実行
                ItemStack extracted = itemHandler.extractItem(0, stackInSlot.getCount(), false);
                if (!extracted.isEmpty()) {
                    player.addItem(extracted);
                }
                // 効果音を再生
                this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1, 1);
            }
            checkRemove();
        }
        return InteractionResult.sidedSuccess(!level().isClientSide());
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount){
        if (!this.level().isClientSide) {
            this.health = (int)((float)this.health - amount);
            if(health <= 0){
                this.drops();
                this.discard();
            }
        }
        return true;
    }

    public void checkRemove() {
        if (level().isClientSide) {
            return;
        }

        // 内容物がないならエンティティ削除
        if (this.itemHandler.getStackInSlot(0).isEmpty() && this.fluidHandler.getFluidInTank(0).isEmpty()) {
            this.discard();
            //level.removeBlock(this.worldPosition, false);
        }
    }

    @Override
    public void tick() {
        super.tick();
        countAge();
        updateVelocity();
    }
    
    public void countAge(){
        if (!this.level().isClientSide) {
            // 寿命
            age++;
            if (age >= LIFETIME) {
                this.discard();
            }
        }
    }
    
    public void updateVelocity(){
        if(velocity.length() > 0){
            velocity = velocity.scale(0.90);
            this.setDeltaMovement(velocity);
            this.move(MoverType.SELF, getDeltaMovement());
            velocity = this.getDeltaMovement();
        }else{
            this.velocity = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }
}
