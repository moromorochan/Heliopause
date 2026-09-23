package com.moromoro.heliopause.recipe;

import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MoonlightPouringRecipeBuilder implements RecipeBuilder {
    private static final ResourceLocation TYPE = new ResourceLocation(Heliopause.MODID, MoonlightPouringRecipe.Type.ID);

    private static final RecipeSerializer<MoonlightPouringRecipe> RECIPE_SERIALIZER = MoonlightPouringRecipe.Serializer.INSTANCE;

    private final Ingredient ingredient;
    private final Item result;
    private final int resultCount;
    private final int time;
    private String group;
    private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

    private MoonlightPouringRecipeBuilder(Ingredient ingredient, Item result, int count, int time) {
        this.ingredient = ingredient;
        this.result = result;
        this.resultCount = count;
        this.time = time;
    }

    public static MoonlightPouringRecipeBuilder moonlightPouring(Ingredient ingredient, Item result, int count, int time) {
        return new MoonlightPouringRecipeBuilder(ingredient, result, count, time);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, CriterionTriggerInstance criterion) {
        Objects.requireNonNull(name, "Criterion name");
        Objects.requireNonNull(criterion, "Criterion");
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No criteria for recipe " + id);
        }

        consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                // レシピタイプ
                json.addProperty("type", TYPE.toString());
                // time & experience
                json.addProperty("time", time);

                // 材料
                json.add("ingredient", ingredient.toJson());

                // 結果
                JsonObject resultJson = new JsonObject();
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(result);
                if (itemId == null) {
                    throw new IllegalStateException("Result item is not registered: " + result);
                }
                resultJson.addProperty("item", itemId.toString());
                resultJson.addProperty("count", resultCount);
                json.add("result", resultJson);

                // グループ(任意)
                if (group != null && !group.isEmpty()) {
                    json.addProperty("group", group);
                }
            }

            @Override
            public ResourceLocation getId() {
                return id;
            }

            @Override
            public RecipeSerializer<?> getType() {
                return RECIPE_SERIALIZER;
            }

            @Override
            public JsonObject serializeAdvancement() {
                JsonObject advancement = new JsonObject();
                // 親にルートを設定
                advancement.addProperty("parent", "minecraft:recipes/root");

                // criteria を追加
                JsonObject criteriaJson = new JsonObject();
                for (Map.Entry<String, CriterionTriggerInstance> entry : criteria.entrySet()) {
                    criteriaJson.add(entry.getKey(), entry.getValue().serializeToJson(SerializationContext.INSTANCE));
                }
                advancement.add("criteria", criteriaJson);

                // rewards にレシピIDを入れる
                JsonObject rewards = new JsonObject();
                com.google.gson.JsonArray recipesArray = new com.google.gson.JsonArray();
                recipesArray.add(id.toString());
                rewards.add("recipes", recipesArray);
                advancement.add("rewards", rewards);

                // requirementsをorで設定
                com.google.gson.JsonArray requirements = new com.google.gson.JsonArray();
                for (String name : criteria.keySet()) {
                    com.google.gson.JsonArray single = new com.google.gson.JsonArray();
                    single.add(name);
                    requirements.add(single);
                }
                advancement.add("requirements", requirements);

                return advancement;
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath());
            }
        });
    }

}