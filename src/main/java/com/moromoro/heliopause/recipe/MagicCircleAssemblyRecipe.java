package com.moromoro.heliopause.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*
public class MagicCircleAssemblyRecipe implements Recipe<Container> {
   private final NonNullList<BlockState> inputBlocks;
    private final NonNullList<BlockState> outputBlocks;
    private final ResourceLocation recipeId;

    private static final int INPUT_SLOTS = 80; //最大で4*4*5程度を想定

    public  static class Serializer implements RecipeSerializer<RoastingRecipe>{
        public static final RoastingRecipe.Serializer INSTANCE = new RoastingRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID,"roasting");

        //jsonからレシピを読み込み
        @Override
        public MagicCircleAssemblyRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe,"result"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(serializedRecipe,"ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(INPUT_SLOTS,Ingredient.EMPTY);
            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i,Ingredient.fromJson(ingredients.get(i)));
            }
            return new MagicCircleAssemblyRecipe(inputs, result, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable MagicCircleAssemblyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(buffer.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i,Ingredient.fromNetwork(buffer));
            }

            ItemStack result = buffer.readItem();
            return new MagicCircleAssemblyRecipe(inputs,result,recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MagicCircleAssemblyRecipe recipe) {
            buffer.writeInt(recipe.inputItems.size());

            for(Ingredient ingredient : recipe.getIngredients()){
                ingredient.toNetwork(buffer);
            }

            buffer.writeItemStack(recipe.getResultItem(null), false);
        }
    }

    //タグに対応するようにマッチング
    @Override
    public boolean matches(Container container, Level level) {
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
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return null;
    }
}
*/