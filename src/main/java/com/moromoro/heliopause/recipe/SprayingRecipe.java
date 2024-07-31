package com.moromoro.heliopause.recipe;

import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SprayingRecipe implements Recipe<Container> {
    private final BlockState inputBlock;
    private final BlockState resultBlock;
    private final List<ItemStack> resultItems;
    private final ResourceLocation recipeId;

    public SprayingRecipe(BlockState inputBlock, @Nullable BlockState resultBlock, @Nullable List<ItemStack> resultItems, ResourceLocation recipeId){
        this.inputBlock = inputBlock;
        this.resultBlock = resultBlock==null ? inputBlock : resultBlock;
        this.resultItems = resultItems==null ? new ArrayList<>() : resultItems;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<SprayingRecipe>{
        public static final SprayingRecipe.Type INSTANCE = new SprayingRecipe.Type();
        public static final String ID = "spraying";
    }

    public  static class Serializer implements RecipeSerializer<SprayingRecipe> {
        public static final SprayingRecipe.Serializer INSTANCE = new SprayingRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, "spraying");

        //jsonからレシピを読み込み
        @Override
        public SprayingRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            ItemStack resultItem = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe,"resultItem"));
            //BlockState resultBlock = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipe,"resultBlock"))
            return new SprayingRecipe(Blocks.COPPER_BLOCK.defaultBlockState(), BlockRegistry.ALCHEMY_BIRON_BLOCK.get().defaultBlockState(), null,null);
        }

        @Override
        public @Nullable SprayingRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
            return null;
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, SprayingRecipe p_44102_) {

        }
    }

    @Override
    public boolean matches(Container p_44002_, Level p_44003_) {
        return false;
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
