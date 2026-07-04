package com.moromoro.heliopause.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.OrreryCircleBoardBlock;
import com.moromoro.heliopause.blockEntity.OrreryCircleBoardBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.item.ImitationCoreItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.moromoro.heliopause.ingredient.CircumstellarIngredient.*;

public class OrreryTransferenceRecipe implements Recipe<Container> {

    // データクラス定義
    public record StellarIngredient(Ingredient ingredient, FluidStack fluidStack, float resonanceRatio){}

    // レシピパラメータ
    private final String centerStarId;
    private final List<StellarIngredient> ingredients;
    private final StellarStack result;
    private final ResourceLocation recipeId;

    OrreryTransferenceRecipe(String centerStarId, List<StellarIngredient> ingredients, StellarStack result, ResourceLocation recipeId){
        this.centerStarId = centerStarId;
        this.ingredients = ingredients;
        this.result = result;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<OrreryTransferenceRecipe>{
        public static final OrreryTransferenceRecipe.Type INSTANCE = new OrreryTransferenceRecipe.Type();
        public static final String ID = "orrery_transference";
    }

    public static class Serializer implements RecipeSerializer<OrreryTransferenceRecipe>{
        public static final OrreryTransferenceRecipe.Serializer INSTANCE = new OrreryTransferenceRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, OrreryTransferenceRecipe.Type.ID);

        // jsonレシピ読み込み
        @Override
        public @NotNull OrreryTransferenceRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
            // 中心星
            String center = GsonHelper.getAsString(json,"center");
            // 周転材料
            List<StellarIngredient> ingredients = new ArrayList<>();
            if (json.has("ingredients") && json.get("ingredients").isJsonArray()) {
                for (JsonElement jsonElement : json.getAsJsonArray("ingredients")) {
                    JsonObject object = jsonElement.getAsJsonObject();
                    float ratio = GsonHelper.getAsFloat(object, "ratio", 1.0f);
                    // アイテムならアイテム・タグ取得
                    if (object.has("item")||object.has("tag")) {
                        Ingredient ingredient = Ingredient.fromJson(object);
                        ingredients.add(new StellarIngredient(ingredient, net.minecraftforge.fluids.FluidStack.EMPTY, ratio));
                    }
                    // 液体なら液体スタック取得
                    else if (object.has("fluid")) {
                        FluidStack fluidStack = FluidStackFromJson(object, 1000);
                        if (!fluidStack.isEmpty()) {
                            ingredients.add(new StellarIngredient(Ingredient.EMPTY, fluidStack, ratio));
                        }
                    } else {
                        Heliopause.LOGGER.warn("Ingredient must have either 'item' or 'fluid' in recipe {}. Skipping.", recipeId);
                    }
                }
            }
            StellarStack resultStack = new StellarStack(ItemStack.EMPTY, FluidStack.EMPTY);
            if (json.has("result")){
                JsonObject resultJson = json.getAsJsonObject("result");
                if(resultJson.has("item")){
                    resultStack = new StellarStack(
                        ShapedRecipe.itemStackFromJson(resultJson),
                        FluidStack.EMPTY
                    );
                }
                else if(resultJson.has("fluid")){
                    resultStack = new StellarStack(
                        ItemStack.EMPTY,
                        FluidStackFromJson(resultJson, 1000)
                    );
                }
            }
            return new OrreryTransferenceRecipe(center, ingredients, resultStack, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable OrreryTransferenceRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            // 中心星のid
            String center = buffer.readUtf();

            // 周転材料
            List<StellarIngredient> ingredients = new ArrayList<>();
            int ingredientSize = buffer.readInt();
            for (int ingredientId = 0; ingredientId < ingredientSize; ingredientId++) {
                if(buffer.readBoolean()){
                    ingredients.add(new StellarIngredient(Ingredient.fromNetwork(buffer), FluidStack.EMPTY, buffer.readFloat()));
                }else{
                    ingredients.add(new StellarIngredient(Ingredient.EMPTY, FluidStack.readFromPacket(buffer), buffer.readFloat()));
                }
            }

            // 結果
            StellarStack stellarStack;
            if(buffer.readBoolean()){
                stellarStack = new StellarStack(buffer.readItem(), FluidStack.EMPTY);
            }else{
                stellarStack = new StellarStack(ItemStack.EMPTY, FluidStack.readFromPacket(buffer));
            }

            return new OrreryTransferenceRecipe(center, ingredients, stellarStack, recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OrreryTransferenceRecipe recipe) {
            // 中心星のid
            buffer.writeUtf(recipe.centerStarId);

            // 周転材料
            buffer.writeInt(recipe.ingredients.size());
            for (StellarIngredient ingredient : recipe.ingredients) {
                // アイテムか液体かを書き込む
                boolean isItem = !ingredient.ingredient.isEmpty();
                buffer.writeBoolean(isItem);
                if(isItem){
                    ingredient.ingredient.toNetwork(buffer);
                }else {
                    ingredient.fluidStack.writeToPacket(buffer);
                }
                buffer.writeFloat(ingredient.resonanceRatio);
            }

            // 結果
            boolean isResultItem = !recipe.result.itemStack().isEmpty();
            buffer.writeBoolean(isResultItem);
            if(isResultItem){
                buffer.writeItemStack(recipe.result.itemStack(), false);
            }else{
                recipe.result.fluidStack().writeToPacket(buffer);
            }
        }
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        if(level.isClientSide()){
            return false;
        }
        // コンテナが要求する状態か確認
        if(container.getContainerSize() != 2){
            return false;
        }
        // コンテナから中心星を取得
        ItemStack centerItem = container.getItem(0);
        if(!(centerItem.getItem() instanceof ImitationCoreItem coreItem)){
            return false;
        }
        // 中心星の種類を確認
        String starId = ImitationCoreItem.getStarId(centerItem);
        if(starId == null || !starId.equals(this.centerStarId)){
            return false;
        }
        // コンテナからブロックエンティティ情報を取得
        ItemStack dataContainerItem = container.getItem(1);
        if (!(dataContainerItem.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        if (!(blockItem.getBlock() instanceof OrreryCircleBoardBlock)) {
            return false;
        }
        CompoundTag nbt = BlockItem.getBlockEntityData(dataContainerItem);
        if(nbt == null){
            return false;
        }
        // nbtから材料を読みだす
        List<CircumstellarIngredient> provided = OrreryCircleBoardBlockEntity.circumStellarFromNbt(nbt);
        if (provided.isEmpty()) return false;
        // サイズチェック
        if (this.ingredients.size() != provided.size()) return false;

        // 順不同で材料の一致確認
        List<CircumstellarIngredient> remaining = new ArrayList<>(provided);
        for (StellarIngredient require : this.ingredients) {
            boolean matched = false;
            for (int i = 0; i < remaining.size(); i++) {
                if (circumstellarMatchesStellar(remaining.get(i), require)) {
                    remaining.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        return true;
    }

    private static boolean circumstellarMatchesStellar(CircumstellarIngredient ingredient, StellarIngredient require) {
        // レシピがアイテムのとき
        if (require.ingredient() != null && !require.ingredient().isEmpty()) {
            ItemStack providedItem = ingredient.getItemStack();
            if (providedItem == null || providedItem.isEmpty()) {
                return false;
            }
            // アイテムorタグ一致
            return require.ingredient().test(providedItem);
        }
        // レシピが液体のとき
        if (require.fluidStack() != null && !require.fluidStack().isEmpty()) {
            FluidStack providedFluid = ingredient.getFluidStack();
            if (providedFluid == null || providedFluid.isEmpty()) {
                return false;
            }
            // 種類と量を確認
            return require.fluidStack().isFluidEqual(providedFluid)
                && providedFluid.getAmount() >= require.fluidStack().getAmount();
        }

        return false;
    }

    // 軌道共鳴の結果を返すレコードクラス
    public record TransferOrbitsResult(List<CircumstellarIngredient> ingredients, boolean transferCompleted){}

    public TransferOrbitsResult transferOrbits(List<CircumstellarIngredient> circumstellarIngredients, final double centForce, final double innerLimit, final double outerLimit){
        // 1tickごとの総移動量を指定
        final double singleMove = 0.001f; // centForce;
        final double wholeMove = circumstellarIngredients.size() * singleMove;

        // 比の合計を計算
        double wholeRatio = 0.0;
        for (StellarIngredient ingredient : this.ingredients) {
            wholeRatio += ingredient.resonanceRatio();
        }

        // 位置エネルギーの合計を計算
        double wholePotential = 0.0;
        for (CircumstellarIngredient ingredient : circumstellarIngredients) {
            if(ingredient.getOrbitalRadius() > 0) {
                wholePotential += ingredient.getOrbitalRadius();
            }
        }

        // 材料と比をマッチング
        List<CircumstellarIngredient> ingredientTransfer = new ArrayList<>(circumstellarIngredients);
        this.ingredients.sort(Comparator.comparing(StellarIngredient::resonanceRatio));

        List<CircumstellarIngredient> resultTransfer = new ArrayList<>();
        // 移動方向毎に分別
        List<CircumstellarIngredient> innerTransfer = new ArrayList<>();
        List<CircumstellarIngredient> outerTransfer = new ArrayList<>();
        for (StellarIngredient recipeIngredient : this.ingredients) {
            for (int i = 0; i < ingredientTransfer.size(); i++) {
                CircumstellarIngredient ingredient = ingredientTransfer.get(i);
                if (circumstellarMatchesStellar(ingredient, recipeIngredient)) {
                    // 現在の位置エネルギー
                    double currentPotential = ingredient.getOrbitalRadius();
                    // 位置エネルギーの目標値
                    double targetPotential = wholePotential * (recipeIngredient.resonanceRatio()/wholeRatio);
                    // エネルギーが足りない場合
                    if(currentPotential + singleMove < targetPotential){
                        if(currentPotential + singleMove >= outerLimit){
                            innerTransfer.add(ingredient);
                        }else{
                            //外側へ移動する群
                            outerTransfer.add(ingredient);
                        }
                    }
                    // 過剰な場合
                    else if(currentPotential - singleMove > targetPotential){
                        // 内側リミットに入るなら外側へ
                        if(currentPotential - singleMove <= innerLimit){
                            outerTransfer.add(ingredient);
                        }else{
                            // 内側へ移動する群
                            innerTransfer.add(ingredient);
                        }
                    }
                    // 移動量に収まる場合は結果群に追加
                    else{
                        resultTransfer.add(ingredient);
                    }
                    ingredientTransfer.remove(i);
                    break;
                }
            }
        }

        boolean isCompleted = true;
        // 内側へ移動する群と外側へ移動する群でエネルギーを折半
        if(!innerTransfer.isEmpty()){
            isCompleted = false;
            final double innerSingleMove = (wholeMove/2)/innerTransfer.size();
            // 結果の軌道半径を計算して適用
            for (CircumstellarIngredient ingredient : innerTransfer) {
                float resultPotential = (float) Math.max(ingredient.getOrbitalRadius() - innerSingleMove, innerLimit);
                ingredient.setOrbitalRadius(resultPotential);
                resultTransfer.add(ingredient);
            }
        }
        if(!outerTransfer.isEmpty()){
            isCompleted = false;
            final double outerSingleMove = (wholeMove/2)/outerTransfer.size();
            for (CircumstellarIngredient ingredient : outerTransfer) {
                float resultPotential = (float) Math.min(ingredient.getOrbitalRadius() + outerSingleMove, outerLimit);
                ingredient.setOrbitalRadius(resultPotential);
                resultTransfer.add(ingredient);
            }
            resultTransfer.sort(Comparator.comparing(CircumstellarIngredient::getOrbitalRadius));
        }

        return new TransferOrbitsResult(resultTransfer, isCompleted);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return result.itemStack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.itemStack().copy();
    }
    public FluidStack getResultFluid(RegistryAccess registryAccess) {
        return result.fluidStack().copy();
    }

    public List<StellarIngredient> getStellarIngredients() {
        return ingredients;
    }

    public StellarStack getResultStellarStack() {
        return result;
    }

    public String getCenterStarId() {
        return this.centerStarId;
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

    public static FluidStack FluidStackFromJson(JsonObject jsonObject, int defaultAmount) {
        if (jsonObject == null) return FluidStack.EMPTY;
        // idを取得
        String fluidId = GsonHelper.getAsString(jsonObject, "fluid", "");
        if (fluidId.isEmpty()) {
            return FluidStack.EMPTY;
        }
        try {
            // idから液体の種類を取得
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId));
            if (fluid == null) {
                Heliopause.LOGGER.warn("Unknown fluid id '{}' in JSON: {}", fluidId, jsonObject);
                return FluidStack.EMPTY;
            }

            // 液体スタックを作成
            int amount = GsonHelper.getAsInt(jsonObject, "amount", defaultAmount);
            FluidStack fluidStack = new FluidStack(fluid, amount);
            // nbtがあれば適用
            if (jsonObject.has("nbt")) {
                JsonElement nbtEl = jsonObject.get("nbt");
                CompoundTag tag = TagParser.parseTag(nbtEl.getAsString());
                fluidStack.setTag(tag);
            }

            return fluidStack;

        } catch (Exception e) {
            Heliopause.LOGGER.warn("Failed to build FluidStack from '{}': {}", fluidId, e.getMessage());
            return FluidStack.EMPTY;
        }
    }

}
