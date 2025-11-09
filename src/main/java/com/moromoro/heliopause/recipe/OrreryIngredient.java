package com.moromoro.heliopause.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class OrreryIngredient {
    private final String type; // "item", "fluid"
    private final ResourceLocation id;
    private int amount;
    private float[] innerProperties = {0.0F}; // デフォルト値
    private float[] outerProperties = {0.0F}; // デフォルト値
    private float resistance = 1.0F; // デフォルト値

    public OrreryIngredient(String type, ResourceLocation id, int amount) {
        this.type = type;
        this.id = id;
        this.amount = amount;
    }

    public OrreryIngredient(Block block){
        new OrreryIngredient(this.type = "block", this.id = ForgeRegistries.BLOCKS.getKey(block),this.amount = 1);
    }

    public OrreryIngredient(ItemStack itemStack){
        new OrreryIngredient(this.type = "item", this.id = ForgeRegistries.ITEMS.getKey(itemStack.getItem()), this.amount = itemStack.getCount());
    }

    public OrreryIngredient(FluidStack fluidStack){
        new OrreryIngredient(this.type = "fluid", this.id = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()), this.amount = fluidStack.getAmount());
    }

    // ゲッター
    public String getType() { return type; }
    public ResourceLocation getId() { return id; }
    public int getAmount() { return amount; }
    public float[] getInnerProperties() { return innerProperties; }
    public float[] getOuterProperties() { return outerProperties; }
    public float getResistance() { return resistance; }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    // カスタムデータを適用
    public void setProperties(float[] inner, float[] outer) {
        this.innerProperties = inner;
        this.outerProperties = outer;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public static OrreryIngredient empty(){
        return new OrreryIngredient("item", ForgeRegistries.ITEMS.getKey(Items.AIR), 0);
    }

    /*public final @NotNull Block getBlock(){
        if(!type.equals("block")) return Blocks.AIR;
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        if(block == null){
            return Blocks.AIR;
        }
        return block;
    }*/

    public final @NotNull ItemStack getItemStack(){
        if(!type.equals("item")) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if(item == null){
            return ItemStack.EMPTY;
        }
        return new ItemStack(item,amount);
    }

    public final @NotNull FluidStack getFluidStack(){
        if(!type.equals("fluid")) return FluidStack.EMPTY;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if(fluid == null){
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid,amount);
    }

    public static OrreryIngredient fromNBT(CompoundTag nbt) {
        String type = nbt.getString("type");
        ResourceLocation id = new ResourceLocation(nbt.getString("id"));
        int amount = nbt.getInt("amount");
        return new OrreryIngredient(type, id, amount);
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", this.type);
        nbt.putString("id", this.id.toString());
        nbt.putInt("amount", this.amount);
        return nbt;
    }

    public static OrreryIngredient fromNetwork(FriendlyByteBuf buffer) {
        // type（String）を読み出す
        String type = buffer.readUtf();

        // id（ResourceLocation）を読み出す
        ResourceLocation id = buffer.readResourceLocation();

        // amount（int、VarInt形式）を読み出す
        int amount = buffer.readVarInt();

        // innerProperties 配列を書き出す際、まず長さを書いているので、読み出す
        int innerLength = buffer.readVarInt();
        float[] innerProperties = new float[innerLength];
        for (int i = 0; i < innerLength; i++) {
            innerProperties[i] = buffer.readFloat();
        }

        // outerProperties 配列も同様に読み出す
        int outerLength = buffer.readVarInt();
        float[] outerProperties = new float[outerLength];
        for (int i = 0; i < outerLength; i++) {
            outerProperties[i] = buffer.readFloat();
        }

        // resistance（float）を読み出す
        float resistance = buffer.readFloat();

        // 基本情報のみでインスタンスを生成し、後からカスタムデータを設定
        OrreryIngredient component = new OrreryIngredient(type, id, amount);
        component.setProperties(innerProperties, outerProperties);
        component.setResistance(resistance);

        return component;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        // type (String) の書き出し
        buffer.writeUtf(this.type);

        // id (ResourceLocation) の書き出し
        buffer.writeResourceLocation(this.id);

        // amount (int) の書き出し（VarIntとして書き出す）
        buffer.writeVarInt(this.amount);

        // innerProperties 配列の書き出し：長さと各要素（float）
        buffer.writeVarInt(this.innerProperties.length);
        for (float value : this.innerProperties) {
            buffer.writeFloat(value);
        }

        // outerProperties 配列の書き出し：長さと各要素（float）
        buffer.writeVarInt(this.outerProperties.length);
        for (float value : this.outerProperties) {
            buffer.writeFloat(value);
        }

        // resistance (float) の書き出し
        buffer.writeFloat(this.resistance);
    }

    public boolean isSame(OrreryIngredient newIngredient) {
        return this.type.equals(newIngredient.getType())&&this.id.equals(newIngredient.getId());
    }

    public boolean isEmpty() {
        return this.amount == 0;
    }
}
