package com.moromoro.heliopause.recipe.orreryWhirling;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.icu.impl.Pair;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.recipe.OrreryIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrreryWhirlingRecipe implements Recipe<Container> {
    private final ResourceLocation recipeId;
    private final OrreryIngredient result;
    private final OrreryIngredient solvent;
    private final NonNullList<OrreryIngredient> ingredients;
    private final int time;

    OrreryWhirlingRecipe(ResourceLocation recipeId, OrreryIngredient result, OrreryIngredient solvent, NonNullList<OrreryIngredient> orreryIngredients, int time){
        this.recipeId = recipeId;
        this.result = result;
        this.solvent = solvent;
        this.ingredients = orreryIngredients;
        this.time = time;
    }

    public OrreryIngredient getResult() {
        return result;
    }

    public OrreryIngredient getSolvent() {
        return solvent;
    }

    public NonNullList<OrreryIngredient> getOrreryIngredients(){
        return ingredients;
    }

    public int getTime() {
        return time;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return null;
    }

    public static class Type implements RecipeType<OrreryWhirlingRecipe>{
        public static final OrreryWhirlingRecipe.Type INSTANCE = new OrreryWhirlingRecipe.Type();
        public static final String ID = "orrery_whirling";
    }

    public  static class Serializer implements RecipeSerializer<OrreryWhirlingRecipe>{
        public static final OrreryWhirlingRecipe.Serializer INSTANCE = new OrreryWhirlingRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID,"orrery_whirling");

        @Override
        public OrreryWhirlingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            // result の読み込み（ブロック・アイテム・流体のいずれか）
            OrreryIngredient result = parseOrreryIngredient(json.getAsJsonObject("result"));

            // solvent の読み込み（カスタムデータを参照）
            OrreryIngredient solvent = parseOrreryIngredient(json.getAsJsonObject("solvent"));

            // ingredients の読み込み（リスト形式）
            NonNullList<OrreryIngredient> ingredients = NonNullList.create();
            JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
            for (JsonElement element : ingredientsArray) {
                OrreryIngredient ingredient = parseOrreryIngredient(element.getAsJsonObject());
                ingredients.add(ingredient);
            }

            // time のデフォルト値（時間指定がない場合）
            int time = json.has("processingTime") ? json.get("processingTime").getAsInt() : 100; // デフォルトは 100 ticks (5秒)

            return new OrreryWhirlingRecipe(recipeId, result, solvent, ingredients, time);
        }

        @Override
        public @Nullable OrreryWhirlingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            // レシピの結果となるコンポーネントを読み出す
            OrreryIngredient result = OrreryIngredient.fromNetwork(buffer);

            // 溶媒（solvent）のコンポーネントを読み出す
            OrreryIngredient solvent = OrreryIngredient.fromNetwork(buffer);

            // 材料のコンポーネント数を読み出し、各コンポーネントを取得
            int ingredientCount = buffer.readVarInt();
            NonNullList<OrreryIngredient> ingredients = NonNullList.create();
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.add(OrreryIngredient.fromNetwork(buffer));
            }

            // 処理時間（ticks）を読み出す
            int time = buffer.readVarInt();

            return new OrreryWhirlingRecipe(recipeId, result, solvent, ingredients, time);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OrreryWhirlingRecipe recipe) {
            // レシピの結果コンポーネントを書き出す
            recipe.getResult().toNetwork(buffer);

            // 溶媒のコンポーネントを書き出す
            recipe.getSolvent().toNetwork(buffer);

            // 材料のコンポーネントの数と各コンポーネントの情報を書き出す
            buffer.writeVarInt(recipe.ingredients.size());
            for (OrreryIngredient comp : recipe.ingredients) {
                comp.toNetwork(buffer);
            }

            // 処理時間を書き出す
            buffer.writeVarInt(recipe.getTime());
        }

        private OrreryIngredient parseOrreryIngredient(JsonObject json) {
            String type = json.get("type").getAsString();
            ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
            int amount = json.has("amount") ? json.get("amount").getAsInt() : 1;

            //return new OrreryIngredient(type, id, amount);
            return OrreryIngredient.empty();
        }
    }

    @Override
    public boolean matches(Container container, Level level) {
        Pair<OrreryIngredient, List<CircumstellarIngredient>> data = readDataFromStack(container.getItem(0),level);
        OrreryIngredient centerIngredient = data.first;
        // 中心が一致するか確認
        if (!matchesComponent(solvent, centerIngredient)) {
            return false;
        }
        //circumStellarIngredientのリストをorreryIngredientのリストに変換
        List<CircumstellarIngredient> circumStellarIngredients = data.second;
        List<OrreryIngredient> inputIngredients = new ArrayList<>();
        for (CircumstellarIngredient circumStellarIngredient : circumStellarIngredients) {
            //inputIngredients.add(circumStellarIngredient.getIngredient());
        }
        //レシピの要求を量の多い順にソート
        List<OrreryIngredient> requiredIngredients = new ArrayList<>(this.ingredients);
        requiredIngredients.sort((a, b) -> Integer.compare(b.getAmount(), a.getAmount()));

        //必要なすべてのingredientがあるか確認
        for (OrreryIngredient req : requiredIngredients) {
            boolean found = false;
            for (Iterator<OrreryIngredient> iterator = inputIngredients.iterator(); iterator.hasNext(); ) {
                OrreryIngredient input = iterator.next();
                if (matchesComponent(req, input)) {

                    iterator.remove();
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesComponent(OrreryIngredient recipeIngredient, OrreryIngredient inputIngredient) {
        //インプットが無ければfalse
        if (inputIngredient == null || inputIngredient.getAmount() <= 0) {
            return false;
        }
        //インプットの種類が異なればfalse
        if (!recipeIngredient.getType().equals(inputIngredient.getType())) {
            return false;
        }

        //タイプおよびIdのマッチング
        if (recipeIngredient.getType().equals(inputIngredient.getType()) && !recipeIngredient.getId().equals(inputIngredient.getId())) {
            return false;
        }

        //要求量が足りているか確認
        return inputIngredient.getAmount() >= recipeIngredient.getAmount();
    }

    public static Pair<OrreryIngredient, List<CircumstellarIngredient>> readDataFromStack(ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return Pair.of(OrreryIngredient.empty(), new ArrayList<>());
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CenterIngredient")) {
            return Pair.of(OrreryIngredient.empty(), new ArrayList<>());
        }

        OrreryIngredient centerIngredient = OrreryIngredient.fromNBT(tag.getCompound("CenterIngredient"));

        List<CircumstellarIngredient> circumstellarIngredients = new ArrayList<>();
        if (tag.contains("Ingredients")) {
            ListTag ingredientsTag = tag.getList("Ingredients", 10); // 10: CompoundTag型
            for (int i = 0; i < ingredientsTag.size(); i++) {
                CompoundTag ingredientNbt = ingredientsTag.getCompound(i);
                CircumstellarIngredient ingredient = CircumstellarIngredient.readFromNbt(ingredientNbt);
                if (ingredient.isValid()) {
                    circumstellarIngredients.add(ingredient);
                }
            }
        }
        return Pair.of(centerIngredient, circumstellarIngredients);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        // result が item/block であると仮定
        if ("item".equals(result.getType())) {
            Item item = ForgeRegistries.ITEMS.getValue(result.getId());
            if (item != null) {
                return new ItemStack(item, result.getAmount());
            }
        } else if ("block".equals(result.getType())) {
            Block block = ForgeRegistries.BLOCKS.getValue(result.getId());
            if (block != null) {
                return new ItemStack(block.asItem(), result.getAmount());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return assemble(null, registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
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
