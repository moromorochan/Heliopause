package com.moromoro.heliopause.recipe;

import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoonlightPouringRecipe implements Recipe<Container> {
    private final int craftTime;
    private final double craftExp;
    private final Ingredient ingredient;
    private final ItemStack result;
    private final ResourceLocation recipeId;

    MoonlightPouringRecipe(int craftTime, double craftExp,Ingredient ingredient, ItemStack result, ResourceLocation recipeId){
        this.craftTime = craftTime;
        this.craftExp = craftExp;
        this.ingredient = ingredient;
        this.result = result;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<MoonlightPouringRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "moonlight_pouring";
    }

    public static class Serializer implements RecipeSerializer<MoonlightPouringRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, Type.ID);

        // jsonレシピ読み込み
        @Override
        public @NotNull MoonlightPouringRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
            int craftTime = json.get("time").getAsInt();
            double craftExp = json.get("experience").getAsDouble();
            Ingredient inputItem = Ingredient.fromJson(GsonHelper.getAsJsonObject(json,"ingredient"));
            ItemStack resultItem = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json,"result"));

            return new MoonlightPouringRecipe(craftTime, craftExp, inputItem, resultItem, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable MoonlightPouringRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int craftTime = buffer.readInt();
            double craftExp = buffer.readDouble();
            Ingredient inputItem = Ingredient.fromNetwork(buffer);
            ItemStack resultItem = buffer.readItem();

            return new MoonlightPouringRecipe(craftTime, craftExp, inputItem, resultItem, recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MoonlightPouringRecipe recipe) {
            buffer.writeInt(recipe.getCraftTime());
            buffer.writeDouble(recipe.getCraftExp());
            recipe.getIngredient().toNetwork(buffer);
            buffer.writeItemStack(recipe.getResultItem(null),false);
        }
    }

    @Override
    public boolean matches(@NotNull Container container, Level level) {
        if(level.isClientSide()){
            return false;
        }
        ItemStack inputItem = container.getItem(1);
        // 材料比較
        return this.ingredient.test(inputItem);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    public int getCraftTime() {
        return craftTime;
    }

    public double getCraftExp() {
        return craftExp;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.withSize(1, this.ingredient);
    }

    private Ingredient getIngredient() {
        return this.ingredient;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }
}
