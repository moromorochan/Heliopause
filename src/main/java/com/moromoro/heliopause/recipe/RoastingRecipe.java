package com.moromoro.heliopause.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoastingRecipe implements Recipe<CraftingContainer> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack result;
    private final ResourceLocation recipeId;

    private static final int INPUT_SLOTS = 4;

    public RoastingRecipe(NonNullList<Ingredient> inputItems, ItemStack result, ResourceLocation recipeId){
        this.inputItems = inputItems;
        this.result = result;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<RoastingRecipe>{
        public static final Type INSTANCE = new Type();
        public static final String ID = "roasting";
    }

    public  static class Serializer implements RecipeSerializer<RoastingRecipe>{
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID,Type.ID);

        //jsonからレシピを読み込み
        @Override
        public RoastingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe,"result"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(serializedRecipe,"ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(),Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i,Ingredient.fromJson(ingredients.get(i)));
            }
            return new RoastingRecipe(inputs, result, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable RoastingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buffer.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i,Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            return new RoastingRecipe(inputs,result,recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RoastingRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());

            for(Ingredient ingredient : recipe.getIngredients()){
                ingredient.toNetwork(buffer);
            }

            buffer.writeItemStack(recipe.getResultItem(null), false);
        }
    }
    //タグに対応するようにマッチング
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if(level.isClientSide()){
            return false;
        }
        List<ItemStack> availableItems = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty()) {
                availableItems.add(itemStack);
            }
        }

        if (availableItems.size() != this.inputItems.size()) {
            return false;
        }

        for (Ingredient ingredient : this.inputItems) {
            boolean found = false;
            for (Iterator<ItemStack> iterator = availableItems.iterator(); iterator.hasNext();) {
                ItemStack itemStack = iterator.next();
                if (ingredient.test(itemStack)) {
                    found = true;
                    iterator.remove();
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

}
