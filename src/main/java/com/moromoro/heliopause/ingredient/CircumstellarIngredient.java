package com.moromoro.heliopause.ingredient;

import com.moromoro.heliopause.recipe.OrreryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Math;

//星周天体を表現するクラス
public class CircumstellarIngredient {
    private static final float REFERENCE_RADIUS = 1.0f;    //ワールド座標でのスロット間の距離

    boolean disk_shaped = true;          // false:オーブ true:星周円盤
    int orbitalSlotId = 0;              // 軌道スロット位置
    float revolutionOffset = 0;          // 公転による位置
    int rotationRatio = 5;            // 自転による位置(見た目用)
    //FluidStack fluidStack = FluidStack.EMPTY;   // 内容の液体
    //ItemStack itemStack = ItemStack.EMPTY;      // 内容のアイテム
    OrreryIngredient ingredient = OrreryIngredient.empty();
    //int fractableItemAmount = 0;                // 輪になる漸減アイテムの量

    public CircumstellarIngredient(int orbitalSlotId, float revolutionOffset, int rotationRatio, OrreryIngredient ingredient/*FluidStack fluidStack, ItemStack itemStack*//*, int fractableItemAmount*/, boolean disk_shaped) {
        this.setOrbitalSlot(orbitalSlotId);
        this.setRevolutionOffset(revolutionOffset);
        this.setRotationRatio(rotationRatio);
        this.setIngredient(ingredient);
        this.setDisk_shaped(disk_shaped);

        //this.setFluidStack(fluidStack);
        //this.setItemStack(itemStack);
        //this.setFractableItemAmount(fractableItemAmount);
    }

    public static CircumstellarIngredient empty(int orbitalSlotId) {
        return new CircumstellarIngredient(orbitalSlotId,0,0,OrreryIngredient.empty(),false);
    }
    /*public CircumstellarIngredient(int orbitalSlotId, float revolutionOffset, int rotationRatio, FluidStack fluidStack, boolean disk_shaped){
        this(orbitalSlotId,revolutionOffset, rotationRatio,new OrreryIngredient(fluidStack), disk_shaped);
    }
    public CircumstellarIngredient(int orbitalSlotId, float revolutionOffset, int rotationRatio, FluidStack fluidStack){
        this(orbitalSlotId,revolutionOffset, rotationRatio,fluidStack,ItemStack.EMPTY*//*, 0*//*, false);
    }
    public CircumstellarIngredient(int orbitalSlotId, float revolutionOffset, int rotationRatio, ItemStack itemStack*//*, int fractableItemAmount*//*, boolean disk_shaped){
        this(orbitalSlotId,revolutionOffset, rotationRatio,FluidStack.EMPTY,itemStack*//*, fractableItemAmount*itemStack.getCount()*//*,disk_shaped);
    }
    public CircumstellarIngredient(int orbitalSlotId, float revolutionOffset, int rotationRatio, ItemStack itemStack){
        this(orbitalSlotId,revolutionOffset, rotationRatio,FluidStack.EMPTY,itemStack*//*, 0*//*, true);
    }*/

    public boolean isValid() {
        return ((!this.ingredient.getFluidStack().isEmpty()) || (!this.ingredient.getItemStack().isEmpty()/* && this.fractableItemAmount!=0*/));
    }

    public void setDisk_shaped(boolean newShape){
        this.disk_shaped = newShape;
    }

    public static int getSlotFromRadius(float newOrbitalRadius) {
        if(newOrbitalRadius<0) {
            return 0;
        }
        return Math.roundHalfUp(newOrbitalRadius / REFERENCE_RADIUS) - 1;
    }

    /*public float setOrbitalSlotFromRadius(float newOrbitalRadius) {
        if(newOrbitalRadius<0) {
            return -1;
        }
        this.orbitalSlotId = Math.roundHalfUp(newOrbitalRadius / REFERENCE_RADIUS) - 1;
        return (float) orbitalSlotId + REFERENCE_RADIUS;
    }*/

    public void setOrbitalSlot(int newOrbitalSlot){
        if(newOrbitalSlot < 0){return;}
        this.orbitalSlotId = newOrbitalSlot;
    }

    public void setRevolutionOffset(float newRevolutionOffset) {
        this.revolutionOffset = (newRevolutionOffset)%360;
    }

    public void setRotationRatio(int newRotationOffset) {
        this.rotationRatio = newRotationOffset;
    }

    public void setIngredient(OrreryIngredient ingredient) {
        this.ingredient = ingredient;
    }

    /*public void setFluidStack(FluidStack newFluidStack) {
        this.fluidStack = newFluidStack;
    }

    public void setItemStack(ItemStack newItemStack) {
        this.itemStack = newItemStack;
    }*/

    /*public void setFractableItemAmount(int fractableItemAmount) {
        this.fractableItemAmount = fractableItemAmount;
    }*/

    public boolean getDisk_shaped(){
        return this.disk_shaped;
    }

    public int getSlotId(){
        return this.orbitalSlotId;
    }

    public float getOrbitalRadius(){
        return (this.orbitalSlotId + 1)* REFERENCE_RADIUS;
    }

    public float getRevolutionOffset(){
        return this.revolutionOffset;
    }

    public int getRotationRatio(){
        return this.rotationRatio;
    }

    public OrreryIngredient getIngredient() {
        return ingredient;
    }

    public FluidStack getFluidStack(){
        /*if(this.fluidStack==null)return FluidStack.EMPTY;
        return this.fluidStack;*/
        return ingredient.getFluidStack();
    }

    public ItemStack getItemStack(){
        /*if(this.itemStack==null)return ItemStack.EMPTY;
        return this.itemStack;*/
        return ingredient.getItemStack();
    }

    /*public int getFractableItemAmount(){
        return fractableItemAmount;
    }*/

    /*public OrreryIngredient getAsOrreryIngredient(){
        //タイプの対応
        String type;
        ResourceLocation id;
        int amount;
        if (!fluidStack.isEmpty()) {
            type = "fluid";
            id = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
            amount = fluidStack.getAmount();
        } else if (!itemStack.isEmpty()) {
            type = "item";
            id = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
            amount = itemStack.getCount();//fractableItemAmount;
        } else {
            type = "item";
            id = ForgeRegistries.ITEMS.getKey(Items.AIR);
            amount = 0;
        }
        return new OrreryIngredient(type, id, amount);
    }*/

    public CompoundTag writeToNBT(CompoundTag nbt){
        nbt.putBoolean("Disk", getDisk_shaped());
        nbt.putInt("Slot", getSlotId());
        nbt.putFloat("Revolution", getRevolutionOffset());
        nbt.putInt("Rotation", getRotationRatio());
        nbt.put("Ingredient",getIngredient().toNBT());

        CompoundTag fluidNbt = new CompoundTag();
        getFluidStack().writeToNBT(fluidNbt);
        //nbt.put("FluidStack", fluidNbt);

        //nbt.put("ItemStack", getItemStack().serializeNBT());
        //nbt.putInt("FractableItemAmount", getFractableItemAmount());

        return nbt;
    }

    public static CircumstellarIngredient readFromNbt(CompoundTag ingredientNbt) {
        boolean nbtDisk = ingredientNbt.getBoolean("Disk");
        int nbtSlot = ingredientNbt.getInt("Slot");
        float nbtRevolution = ingredientNbt.getFloat("Revolution");
        int nbtRotation = ingredientNbt.getInt("Rotation");
        OrreryIngredient nbtIngredient = OrreryIngredient.fromNBT(ingredientNbt.getCompound("Ingredient"));
        /*FluidStack nbtFluidStack = FluidStack.loadFluidStackFromNBT(ingredientNbt.getCompound("FluidStack"));
        ItemStack nbtItemStack = ItemStack.of(ingredientNbt.getCompound("ItemStack"));*/
        //int fractableItemAmount = ingredientNbt.getInt("FractableItemAmount");

        return new CircumstellarIngredient(nbtSlot, nbtRevolution, nbtRotation, nbtIngredient/*nbtFluidStack, nbtItemStack*//*, fractableItemAmount*/, nbtDisk);
    }

    //種類問わず量を取得
    public int getAmount() {
        return Math.max(/*getFractableItemAmount()*/getItemStack().getCount(), getFluidStack().getAmount());
    }

    //衛星のサイズを計算
    public float getSatRadius(int capacity){
        float fillPercentage =  this.getFluidStack().getAmount() /(float) capacity;
        return Math.sqrt(fillPercentage) * 0.35f;
    }

    public void consume(int requiredAmount) {
        FluidStack fluidStack = ingredient.getFluidStack().copy();
        ItemStack itemStack = ingredient.getItemStack().copy();
        if(!fluidStack.isEmpty()){
            fluidStack.shrink(requiredAmount);
            setIngredient(new OrreryIngredient(fluidStack));
        } else if (!itemStack.isEmpty()) {
            itemStack.shrink(requiredAmount);//fractableItemAmount -= requiredAmount;
            setIngredient(new OrreryIngredient(itemStack));
        }
    }

    public void addAmount(int i) {
        ingredient.setAmount(ingredient.getAmount() + i);
    }
}
