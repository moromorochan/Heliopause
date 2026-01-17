package com.moromoro.heliopause.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OrreryIngredient {
    private final String type; // "item", "fluid"
    //private final ResourceLocation id;
    private final ItemStack itemStack;
    private final FluidStack fluidStack;
    //private int amount;
    private float[] innerProperties = {0.0F}; // デフォルト値
    private float[] outerProperties = {0.0F}; // デフォルト値
    private float resistance = 1.0F; // デフォルト値

    public OrreryIngredient(String type, @Nullable ItemStack itemStack,@Nullable FluidStack fluidStack/*, int amount*/) {
        this.type = type;
        if(type.equals("item")){
            this.itemStack = itemStack != null ? itemStack : ItemStack.EMPTY;
            this.fluidStack = FluidStack.EMPTY;
        } else if (type.equals("fluid")) {
            this.itemStack = ItemStack.EMPTY;
            this.fluidStack = fluidStack != null ? fluidStack : FluidStack.EMPTY;
        }else{
            this.itemStack = ItemStack.EMPTY;
            this.fluidStack = FluidStack.EMPTY;
        }
        //this.id = id;
        //this.amount = amount;
    }

    /*public OrreryIngredient(Block block){
        new OrreryIngredient(this.type = "block", this.id = ForgeRegistries.BLOCKS.getKey(block),this.amount = 1);
    }*/

    public OrreryIngredient(ItemStack itemStack){
        new OrreryIngredient(this.type = "item", this.itemStack = itemStack, this.fluidStack = FluidStack.EMPTY);
    }

    public OrreryIngredient(FluidStack fluidStack){
        new OrreryIngredient(this.type = "fluid", this.itemStack = ItemStack.EMPTY, this.fluidStack = fluidStack);
    }

    // ゲッター
    public String getType() { return type; }
    public @Nullable ResourceLocation getId() {
        return switch (type){
            case "item" -> ForgeRegistries.ITEMS.getKey(itemStack.getItem());
            case "fluid" -> ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid());
            default -> null;
        };
    }
    public int getAmount() {
        return switch (type){
            case "item" -> itemStack.getCount();
            case "fluid" -> fluidStack.getAmount();
            default -> 0;
        };
    }
    public float[] getInnerProperties() { return innerProperties; }
    public float[] getOuterProperties() { return outerProperties; }
    public float getResistance() { return resistance; }

    /*public void setAmount(int amount) {
        this.amount = amount;
    }*/

    // カスタムデータを適用
    public void setProperties(float[] inner, float[] outer) {
        this.innerProperties = inner;
        this.outerProperties = outer;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public static OrreryIngredient empty(){
        return new OrreryIngredient("item", ItemStack.EMPTY, FluidStack.EMPTY);
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
        return this.itemStack;
    }

    public final @NotNull FluidStack getFluidStack(){
        return this.fluidStack;
    }

    public static OrreryIngredient fromNBT(CompoundTag nbt) {
        String type = nbt.getString("type");
        ItemStack nbtItemStack = ItemStack.of(nbt.getCompound("itemStack"));
        FluidStack nbtFluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluidStack"));
        //ResourceLocation id = new ResourceLocation(nbt.getString("id"));
        //int amount = nbt.getInt("amount");
        return new OrreryIngredient(type, nbtItemStack, nbtFluidStack);
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", this.type);
        nbt.put("itemStack", this.itemStack.serializeNBT());
        CompoundTag fluidNbt = this.fluidStack.writeToNBT(new CompoundTag());
        nbt.put("fluidStack", fluidNbt);
        return nbt;
    }

    public static OrreryIngredient fromNetwork(FriendlyByteBuf buffer) {
        // typeを読み出す
        String type = buffer.readUtf();

        // 内容物を読み出す
        ItemStack netItemStack = buffer.readItem();
        FluidStack netFluidStack = buffer.readFluidStack();

        // innerProperties 配列長を読み出す
        int innerLength = buffer.readVarInt();
        float[] innerProperties = new float[innerLength];
        for (int i = 0; i < innerLength; i++) {
            innerProperties[i] = buffer.readFloat();
        }

        // outerProperties 配列長を読み出す
        int outerLength = buffer.readVarInt();
        float[] outerProperties = new float[outerLength];
        for (int i = 0; i < outerLength; i++) {
            outerProperties[i] = buffer.readFloat();
        }

        // resistance（float）を読み出す
        float resistance = buffer.readFloat();

        // 基本情報のみでインスタンスを生成し、後からカスタムデータを設定
        OrreryIngredient component = new OrreryIngredient(type, netItemStack, netFluidStack);
        component.setProperties(innerProperties, outerProperties);
        component.setResistance(resistance);

        return component;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        // typeの書き出し
        buffer.writeUtf(this.type);

        // 内容物の書き出し
        buffer.writeItemStack(this.itemStack, false);
        buffer.writeFluidStack(this.fluidStack);

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

    /*public boolean isSame(OrreryIngredient newIngredient) {
        return this.type.equals(newIngredient.getType())&&this.id.equals(newIngredient.getId());
    }*/

    public boolean isEmpty() {
        return this.getAmount() == 0;
    }
}
