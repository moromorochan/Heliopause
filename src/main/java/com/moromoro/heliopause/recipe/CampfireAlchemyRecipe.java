package com.moromoro.heliopause.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CampfireAlchemyRecipe implements Recipe<Container> {
    private final ResourceLocation recipeId;
    private final Block ingredient;
    private final Block result;
    private final CompoundTag resultNBT;

    public CampfireAlchemyRecipe(ResourceLocation recipeId, Block ingredient, Block result, CompoundTag resultNBT) {
        this.recipeId = recipeId;
        this.ingredient = ingredient;
        this.result = result;
        this.resultNBT = resultNBT;
    }

    public Block getIngredient() {
        return ingredient;
    }

    public Block getResult() {
        return result;
    }

    public CompoundTag getResultNBT() {
        return resultNBT;
    }

    public static class Type implements RecipeType<CampfireAlchemyRecipe>{
        public static final CampfireAlchemyRecipe.Type INSTANCE = new CampfireAlchemyRecipe.Type();
    }

    public static class Serializer implements RecipeSerializer<CampfireAlchemyRecipe> {
        public static final CampfireAlchemyRecipe.Serializer INSTANCE = new CampfireAlchemyRecipe.Serializer();

        @Override
        public CampfireAlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Block ingredient = BuiltInRegistries.BLOCK.get(new ResourceLocation(json.get("ingredient").getAsJsonObject().get("block").getAsString()));
            Block result = BuiltInRegistries.BLOCK.get(new ResourceLocation(json.get("result").getAsJsonObject().get("block").getAsString()));

            CompoundTag resultNBT = new CompoundTag();
            try {
                resultNBT= TagParser.parseTag(new Gson().toJson(json.get("result").getAsJsonObject().get("nbt").getAsJsonObject()));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }

            return new CampfireAlchemyRecipe(recipeId, ingredient, result, resultNBT);
        }

        @Override
        public CampfireAlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Block ingredient = BuiltInRegistries.BLOCK.get(buffer.readResourceLocation());
            Block result = BuiltInRegistries.BLOCK.get(buffer.readResourceLocation());
            CompoundTag resultNBT = buffer.readNbt();

            return new CampfireAlchemyRecipe(recipeId, ingredient, result, resultNBT);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CampfireAlchemyRecipe recipe) {
            buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(recipe.getIngredient()));
            buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(recipe.getResult()));
            buffer.writeNbt(recipe.getResultNBT());
        }
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getItem(0).getItem() instanceof BlockItem containerBlock){
            return containerBlock.getBlock() == ingredient;
        }
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.asItem().getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.asItem().getDefaultInstance();
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
