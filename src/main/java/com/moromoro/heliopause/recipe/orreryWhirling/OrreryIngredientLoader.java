package com.moromoro.heliopause.recipe.orreryWhirling;

import com.google.gson.*;
import com.moromoro.heliopause.recipe.OrreryIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class OrreryIngredientLoader extends SimpleJsonResourceReloadListener {
    public static final Map<ResourceLocation, OrreryIngredient> ORRERY_INGREDIENTS = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().create();
    private static final String DIRECTORY = "orrery_ingredients";

    public OrreryIngredientLoader() {
        super(GSON, DIRECTORY);
    }

    public static OrreryIngredient getMaterialProperties(ResourceLocation id) {
        return ORRERY_INGREDIENTS.getOrDefault(id, new OrreryIngredient("undef", id,0));
    }

    //ディレクトリの読み込み
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        ORRERY_INGREDIENTS.clear(); //古いデータをクリア
        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            JsonObject json = entry.getValue().getAsJsonObject();
            OrreryIngredient ingredient = processMaterial(json);
            ORRERY_INGREDIENTS.put(ingredient.getId(), ingredient);
        }
    }

    //jsonの読み取り
    private OrreryIngredient processMaterial(JsonObject json){
        //種類
        String type = json.get("type").getAsString();
        //id
        ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
        //耐性
        float resistance = json.get("resistance").getAsFloat();

        //共鳴特性
        float[] innerProperties;
        if (json.has("inner_properties")) {
            innerProperties = parseFloatArray(json.getAsJsonArray("inner_properties"));
        } else {
            innerProperties = new float[]{0.0F};
        }

        float[] outerProperties;
        if (json.has("outer_properties")) {
            outerProperties = parseFloatArray(json.getAsJsonArray("outer_properties"));
        } else {
            outerProperties = new float[]{0.0F};
        }

        OrreryIngredient ingredient = new OrreryIngredient(type,id,0);
        ingredient.setProperties(innerProperties,outerProperties);
        ingredient.setResistance(resistance);

        return ingredient;
    }

    //可変長配列の読み込み
    private float[] parseFloatArray(JsonArray jsonArray) {
        int size = jsonArray.size();
        float[] result = new float[size];

        for (int i = 0; i < size; i++) {
            result[i] = jsonArray.get(i).getAsFloat();
        }

        return result;
    }
}
