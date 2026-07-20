package com.moromoro.heliopause.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.moromoro.heliopause.recipe.OrreryTransferenceRecipe.FluidStackFromJson;

public class StarlightConcentrationRecipe implements Recipe<Container> {

    // レシピパラメータ
    private final ResourceLocation recipeId;
    
    private final Ingredient ingredientItem;
    private final FluidStack ingredientFluid;
    
    private final ItemStack resultItem;
    private final FluidStack resultFluid;
    
    private final int time;
    private final Conditions conditions;
    
    public StarlightConcentrationRecipe(ResourceLocation recipeId, Ingredient ingredientItem, FluidStack ingredientFluid, ItemStack resultItem, FluidStack resultFluid, int time, Conditions conditions) {
        this.recipeId = recipeId;
        this.ingredientItem = ingredientItem;
        this.ingredientFluid = ingredientFluid;
        this.resultItem = resultItem;
        this.resultFluid = resultFluid;
        this.time = time;
        this.conditions = conditions;
    }
    
    // パラメータのレコードクラス
    public record SeasonRange(int start, int end) {}
    
    public record Conditions(String dimension, Set<String> coverage, SeasonRange seasonRange) {
        static Conditions fromJson(JsonObject obj) {
            String dimension = obj.has("dimension")
                ? obj.get("dimension").getAsString()
                : "minecraft:overworld";
            
            Set<String> coverage = new HashSet<>();
            if (obj.has("coverage")) {
                obj.getAsJsonArray("coverage").forEach(e -> coverage.add(e.getAsString()));
            }
            
            JsonObject o = obj.getAsJsonObject("season_range");
            int start = o.get("start").getAsInt();
            int end = o.get("end").getAsInt();
            SeasonRange ranges = new SeasonRange(start, end);
            
            return new Conditions(dimension, coverage, ranges);
        }
    }

    public static class Type implements RecipeType<StarlightConcentrationRecipe>{
        public static final StarlightConcentrationRecipe.Type INSTANCE = new StarlightConcentrationRecipe.Type();
        public static final String ID = "starlight_concentration";
    }

    public static class Serializer implements RecipeSerializer<StarlightConcentrationRecipe>{
        public static final StarlightConcentrationRecipe.Serializer INSTANCE = new StarlightConcentrationRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, StarlightConcentrationRecipe.Type.ID);

        // jsonレシピ読み込み
        @Override
        public @NotNull StarlightConcentrationRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json){
            // ingredient
            Ingredient ingredientItem = Ingredient.EMPTY;
            FluidStack ingredientFluid = FluidStack.EMPTY;
            
            if (json.has("ingredient")) {
                JsonObject ing = json.getAsJsonObject("ingredient");
                
                if (ing.has("item")) {
                    ingredientItem = Ingredient.fromJson(ing);
                }
                if (ing.has("fluid")) {
                    ingredientFluid = FluidStackFromJson(ing, 1000);
                }
            }
            
            // result
            ItemStack resultItem = ItemStack.EMPTY;
            FluidStack resultFluid = FluidStack.EMPTY;
            
            if (json.has("result")) {
                JsonObject res = json.getAsJsonObject("result");
                
                if (res.has("item")) {
                    resultItem = ShapedRecipe.itemStackFromJson(res);
                }
                if (res.has("fluid")) {
                    resultFluid = FluidStackFromJson(res, 1000);
                }
            }
            
            int time = json.has("time") ? json.get("time").getAsInt() : 0;
            
            // conditions
            StarlightConcentrationRecipe.Conditions conditions =
                json.has("starlight_conditions") ? StarlightConcentrationRecipe.Conditions.fromJson(json.getAsJsonObject("starlight_conditions"))
                    : new StarlightConcentrationRecipe.Conditions("minecraft:overworld", Set.of(), new SeasonRange(0, 359));
            
            return new StarlightConcentrationRecipe(recipeId, ingredientItem, ingredientFluid, resultItem, resultFluid, time, conditions);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable StarlightConcentrationRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer){
            Heliopause.LOGGER.debug("read from network, {}", recipeId);
            Ingredient ingredientItem = Ingredient.fromNetwork(buffer);
            FluidStack ingredientFluid = FluidStack.readFromPacket(buffer);
            
            ItemStack resultItem = buffer.readItem();
            FluidStack resultFluid = FluidStack.readFromPacket(buffer);
            
            int time = buffer.readInt();
            
            // conditions
            String dimension = buffer.readUtf();
            
            int covSize = buffer.readInt();
            Set<String> coverage = new HashSet<>();
            for (int i = 0; i < covSize; i++) coverage.add(buffer.readUtf());
            
            int start = buffer.readInt();
            int end = buffer.readInt();
            SeasonRange range = new SeasonRange(start, end);
            
            StarlightConcentrationRecipe.Conditions conditions = new StarlightConcentrationRecipe.Conditions(dimension, coverage, range);
            
            return new StarlightConcentrationRecipe(recipeId, ingredientItem, ingredientFluid, resultItem, resultFluid, time, conditions);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull StarlightConcentrationRecipe recipe){
            recipe.ingredientItem.toNetwork(buffer);
            recipe.ingredientFluid.writeToPacket(buffer);
            
            buffer.writeItem(recipe.resultItem);
            recipe.resultFluid.writeToPacket(buffer);
            
            buffer.writeInt(recipe.time);
            
            // conditions
            buffer.writeUtf(recipe.conditions.dimension());
            
            buffer.writeInt(recipe.conditions.coverage().size());
            for (String coverage : recipe.conditions.coverage()) buffer.writeUtf(coverage);
            
            SeasonRange range = recipe.conditions.seasonRange();
            buffer.writeInt(range.start());
            buffer.writeInt(range.end());
        }
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        Conditions recipeConditions = this.conditions;
        if (recipeConditions == null) {
            return false;
        }
        // ディメンション一致確認
        ResourceLocation requiredDim = new ResourceLocation(recipeConditions.dimension);
        ResourceLocation currentDim = level.dimension().location();
        return currentDim.equals(requiredDim);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    public Ingredient getIngredientItem() {
        return ingredientItem;
    }
    
    public FluidStack getIngredientFluid() {
        return ingredientFluid;
    }
    
    public Conditions getConditions() {
        return conditions;
    }
    
    public int getTime() {
        return time;
    }
    
    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return resultItem;
    }
    public FluidStack getResultFluid() {
        return resultFluid;
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
