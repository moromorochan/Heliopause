package com.moromoro.heliopause.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

//星周天体を表現するクラス
public class CircumstellarIngredient {

    public record StellarStack(ItemStack itemStack, FluidStack fluidStack){}

    boolean disk_shaped = true;         // false:オーブ true:星周円盤
    //boolean isItem = true;              // 内容の種類
    float orbitalRadius = 0;            // 公転軌道半径
    float revolutionOffset = 0;         // 公転による位置(Radians)
    private StellarStack ingredient;
    //FluidStack fluidStack;              // 内容の液体
    //ItemStack itemStack;                // 内容のアイテム

    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, ItemStack itemStack, boolean disk_shaped) {
        this.setOrbitalRadius(orbitalRadius);
        this.setRevolutionOffset(revolutionOffset);
        this.setItemStack(itemStack);
        //this.setFluidStack(FluidStack.EMPTY);
        //this.isItem = true;
        this.setDisk_shaped(disk_shaped);
    }

    public CircumstellarIngredient(float orbitalRadius, float revolutionOffset, FluidStack fluidStack, boolean disk_shaped){
        this.setOrbitalRadius(orbitalRadius);
        this.setRevolutionOffset(revolutionOffset);
        //this.setItemStack(ItemStack.EMPTY);
        this.setFluidStack(fluidStack);
        //this.isItem = false;
        this.setDisk_shaped(disk_shaped);
    }

    public CircumstellarIngredient copy() {
        if(this.isItem()) {
            return new CircumstellarIngredient(orbitalRadius, revolutionOffset, ingredient.itemStack().copy(), disk_shaped);
        }else{
            return new CircumstellarIngredient(orbitalRadius, revolutionOffset, ingredient.fluidStack().copy(), disk_shaped);
        }
    }

    public boolean isItem() {
        return !this.ingredient.itemStack().isEmpty();
    }

    public boolean isFluid(){
        return !isItem();
    }

    public boolean isValid() {
        return ((!this.getFluidStack().isEmpty()) || (!this.getItemStack().isEmpty()/* && this.fractableItemAmount!=0*/));
    }

    public void setDisk_shaped(boolean newShape){
        this.disk_shaped = newShape;
    }

    public void setOrbitalRadius(float newOrbitalRadius){
        if(newOrbitalRadius < 0){return;}
        this.orbitalRadius = newOrbitalRadius;
    }

    public void setRevolutionOffset(float newRevolutionOffset) {
        this.revolutionOffset = (float) ((newRevolutionOffset + Math.PI) % (2 * Math.PI) - Math.PI);
    }

    public void setFluidStack(@Nullable FluidStack newFluidStack) {
        //this.fluidStack = newFluidStack != null ? newFluidStack : FluidStack.EMPTY;
        this.ingredient = new StellarStack(ItemStack.EMPTY, newFluidStack != null ? newFluidStack : FluidStack.EMPTY);
    }

    public void setItemStack(@Nullable  ItemStack newItemStack) {
        //this.itemStack = newItemStack != null ? newItemStack : ItemStack.EMPTY;
        this.ingredient = new StellarStack(newItemStack != null ? newItemStack : ItemStack.EMPTY, FluidStack.EMPTY);
    }

    public boolean isDiskShaped(){
        return this.disk_shaped;
    }

    public float getOrbitalRadius(){
        return this.orbitalRadius;
        //return (this.orbitalSlotId + 1)* REFERENCE_RADIUS;
    }

    public float getRevolutionOffset(){
        return this.revolutionOffset;
    }

    public StellarStack getStellarStack(){
        return ingredient;
    }

    public FluidStack getFluidStack(){
        return ingredient.fluidStack();
    }

    public ItemStack getItemStack(){
        return ingredient.itemStack();
    }

    public static CompoundTag writeToNBT(CompoundTag nbt, CircumstellarIngredient ingredient){
        nbt.putBoolean("disk", ingredient.isDiskShaped());
        nbt.putFloat("radius", ingredient.getOrbitalRadius());
        nbt.putFloat("revolution", ingredient.getRevolutionOffset());
        if(!ingredient.getItemStack().isEmpty()){
            CompoundTag itemNbt = ingredient.getItemStack().save(new CompoundTag());
            nbt.put("itemStack", itemNbt);
        }
        if(!ingredient.getFluidStack().isEmpty()){
            CompoundTag fluidNbt = ingredient.getFluidStack().writeToNBT(new CompoundTag());
            nbt.put("fluidStack", fluidNbt);
        }
        return nbt;
    }

    public static CircumstellarIngredient readFromNbt(CompoundTag nbt) {
        boolean nbtDisk = nbt.getBoolean("disk");
        float nbtRadius = nbt.getFloat("radius");
        float nbtRevolution = nbt.getFloat("revolution");
        if(nbt.contains("itemStack")){
            ItemStack nbtItemStack = ItemStack.of(nbt.getCompound("itemStack"));
            return new CircumstellarIngredient(nbtRadius, nbtRevolution, nbtItemStack, nbtDisk);
        }else{
            FluidStack nbtFluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluidStack"));
            return new CircumstellarIngredient(nbtRadius, nbtRevolution, nbtFluidStack, nbtDisk);
        }
    }

    public void toNetwork(FriendlyByteBuf buf){
        buf.writeBoolean(isDiskShaped());
        buf.writeFloat(getOrbitalRadius());
        buf.writeFloat(getRevolutionOffset());
        buf.writeItemStack(getItemStack(), false);
        buf.writeFluidStack(getFluidStack());
    }

    public static CircumstellarIngredient fromNetwork(FriendlyByteBuf buf){
        boolean nbtDisk = buf.readBoolean();
        float nbtRadius = buf.readFloat();
        float nbtRevolution = buf.readFloat();
        ItemStack nbtItemStack = buf.readItem();
        FluidStack nbtFluidStack = buf.readFluidStack();
        if(!nbtItemStack.isEmpty()){
            return new CircumstellarIngredient(nbtRadius, nbtRevolution, nbtItemStack, nbtDisk);
        }
        return new CircumstellarIngredient(nbtRadius, nbtRevolution, nbtFluidStack, nbtDisk);
    }

    //種類問わず量を取得
    public int getAmount() {
        return Math.max(getItemStack().getCount(), getFluidStack().getAmount());
    }

    //衛星のサイズを計算
    public static float getSatRadius(StellarStack stellarStack){
        if(!stellarStack.itemStack().isEmpty()){
            return 0.25f;
        }
        float fillPercentage =  stellarStack.fluidStack().getAmount() /(float) 1000;//TODO: 量を決める
        return Math.sqrt(fillPercentage)*0.4f;
    }

    public void consume(int requiredAmount) {
        FluidStack fluidStack = getFluidStack().copy();
        ItemStack itemStack = getItemStack().copy();
        if (fluidStack.isEmpty()) {
            if (!itemStack.isEmpty()) {
                itemStack.shrink(requiredAmount);
                setItemStack(itemStack);
            }
        } else {
            fluidStack.shrink(requiredAmount);
            setFluidStack(fluidStack);
        }
    }

}
