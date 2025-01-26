package com.moromoro.heliopause.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

//星周天体を表現するクラス
public class CircumstellarIngredient {

    public boolean disk_shaped = true;          // false:オーブ true:星周円盤
    public float orbitalRadius = 0;             // 軌道半径
    public float revolutionOffset = 0;          // 公転による位置
    public int rotationRatio = 5;            // 自転による位置(見た目用)
    FluidStack fluidStack = FluidStack.EMPTY;   // 内容の液体
    ItemStack itemStack = ItemStack.EMPTY;      // 内容のアイテム
    int fractableItemAmount = 0;                // 輪になる漸減アイテムの量

    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, int rotationRatio, FluidStack fluidStack, ItemStack itemStack, int fractableItemAmount, boolean disk_shaped) {
        this.setDisk_shaped(disk_shaped);
        this.setOrbitalRadius(orbitalRadius);
        this.setRevolutionOffset(revolutionOffset);
        this.setRotationRatio(rotationRatio);
        this.setFluidStack(fluidStack);
        this.setItemStack(itemStack);
        this.setFractableItemAmount(fractableItemAmount);
    }
    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, int rotationRatio, FluidStack fluidStack, boolean disk_shaped){
        this(orbitalRadius,revolutionOffset, rotationRatio,fluidStack,ItemStack.EMPTY, 0, disk_shaped);
    }
    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, int rotationRatio, FluidStack fluidStack){
        this(orbitalRadius,revolutionOffset, rotationRatio,fluidStack,ItemStack.EMPTY, 0, false);
    }
    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, int rotationRatio, ItemStack itemStack, int fractableItemAmount, boolean disk_shaped){
        this(orbitalRadius,revolutionOffset, rotationRatio,FluidStack.EMPTY,itemStack, fractableItemAmount*itemStack.getCount(),disk_shaped);
    }
    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, int rotationRatio, ItemStack itemStack){
        this(orbitalRadius,revolutionOffset, rotationRatio,FluidStack.EMPTY,itemStack, 0, true);
    }

    public void setDisk_shaped(boolean newShape){
        this.disk_shaped = newShape;
    }

    public void setOrbitalRadius(float newOrbitalRadius) {
        if(newOrbitalRadius<0) return;
        this.orbitalRadius = newOrbitalRadius;
    }

    public void setRevolutionOffset(float newRevolutionOffset) {
        this.revolutionOffset = newRevolutionOffset;
    }

    public void setRotationRatio(int newRotationOffset) {
        this.rotationRatio = newRotationOffset;
    }

    public void setFluidStack(FluidStack newFluidStack) {
        this.fluidStack = newFluidStack;
    }

    public void setItemStack(ItemStack newItemStack) {
        this.itemStack = newItemStack;
    }

    public void setFractableItemAmount(int fractableItemAmount) {
        this.fractableItemAmount = fractableItemAmount;
    }

    public boolean getDisk_shaped(){
        return this.disk_shaped;
    }

    public float getOrbitalRadius(){
        return this.orbitalRadius;
    }

    public float getRevolutionOffset(){
        return this.revolutionOffset;
    }

    public int getRotationRatio(){
        return this.rotationRatio;
    }

    public FluidStack getFluidStack(){
        if(this.fluidStack==null)return FluidStack.EMPTY;
        return this.fluidStack;
    }

    public ItemStack getItemStack(){
        if(this.itemStack==null)return ItemStack.EMPTY;
        return this.itemStack;
    }

    public int getFractableItemAmount(){
        return fractableItemAmount;
    }

    public CompoundTag writeToNBT(CompoundTag nbt){
        nbt.putBoolean("Disk", getDisk_shaped());
        nbt.putFloat("Radius", getOrbitalRadius());
        nbt.putFloat("Revolution", getRevolutionOffset());
        nbt.putInt("Rotation", getRotationRatio());

        CompoundTag fluidNbt = new CompoundTag();
        getFluidStack().writeToNBT(fluidNbt);
        nbt.put("FluidStack", fluidNbt);

        nbt.put("ItemStack", getItemStack().serializeNBT());
        nbt.putInt("FractableItemAmount", getFractableItemAmount());

        return nbt;
    }

    public static CircumstellarIngredient readFromNbt(CompoundTag ingredientNbt) {
        boolean nbtDisk = ingredientNbt.getBoolean("Disk");
        float nbtRadius = ingredientNbt.getFloat("Radius");
        float nbtRevolution = ingredientNbt.getFloat("Revolution");
        int nbtRotation = ingredientNbt.getInt("Rotation");
        FluidStack nbtFluidStack = FluidStack.loadFluidStackFromNBT(ingredientNbt.getCompound("FluidStack"));
        ItemStack nbtItemStack = ItemStack.of(ingredientNbt.getCompound("ItemStack"));
        int fractableItemAmount = ingredientNbt.getInt("FractableItemAmount");

        return new CircumstellarIngredient(nbtRadius, nbtRevolution, nbtRotation, nbtFluidStack, nbtItemStack, fractableItemAmount, nbtDisk);
    }
}
