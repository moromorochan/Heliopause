package com.moromoro.heliopause.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.moromoro.heliopause.recipe.OrreryTransferenceRecipe.FluidStackFromJson;

public class StarlightConcentrationRecipe implements Recipe<Container> {

    public final static int FEATURE_FIRST = 0;
    public final static int FEATURE_SECOND = 1;
    public final static int FEATURE_THIRD = 2;

    // レシピパラメータ
    private final FluidStack fluidStack;
    private final int featureId;
    private final Conditions conditions;
    private final ResourceLocation recipeId;

    public record Conditions(String dimension, int magnitudeMin, int magnitudeMax, double accuracyMin, double accuracyMax, List<String> typeWhitelist, List<LongitudeChance> longitudeChance) {
        public static Conditions parseConditions(JsonObject jsonObject) {
            // conditionがないならデフォルト条件
            if (jsonObject == null) return new Conditions("minecraft:overworld", 1, 1, 0.0, 1.0, new ArrayList<>(), new ArrayList<>());

            // ディメンション取得
            String dimension = jsonObject.has("dimension") ? jsonObject.get("dimension").getAsString() : "minecraft:overworld";
            // 等級範囲取得
            int magnitudeMin = jsonObject.has("magnitude_min") ? jsonObject.get("magnitude_min").getAsInt() : 1;
            int magnitudeMax = jsonObject.has("magnitude_max") ? jsonObject.get("magnitude_max").getAsInt() : 1;
            // 要求精度範囲取得
            double accuracyMin = jsonObject.has("accuracy_min") ? jsonObject.get("accuracy_min").getAsDouble() : 0.0;
            double accuracyMax = jsonObject.has("accuracy_max") ? jsonObject.get("accuracy_max").getAsDouble() : 1.0;

            // 天体の種類条件取得
            List<String> typeWhitelist = new ArrayList<>();
            if (jsonObject.has("type_whitelist")) {
                JsonArray arr = jsonObject.getAsJsonArray("type_whitelist");
                for (JsonElement e : arr) typeWhitelist.add(e.getAsString());
            }

            // 経度ごとの確率取得
            List<LongitudeChance> longitudeChance = new ArrayList<>();
            if (jsonObject.has("longitude_chance")) {
                JsonArray array = jsonObject.getAsJsonArray("longitude_chance");
                for (JsonElement element : array) {
                    JsonObject object = element.getAsJsonObject();
                    int start = object.has("start") ? object.getAsJsonPrimitive("start").getAsInt() : 0;
                    int end = object.has("end") ? object.getAsJsonPrimitive("end").getAsInt() : 359;
                    double chance = object.has("chance") ? object.getAsJsonPrimitive("chance").getAsDouble() : 1.0;
                    longitudeChance.add(new LongitudeChance(start, end, chance));
                }
            }

            return new Conditions(dimension, magnitudeMin, magnitudeMax, accuracyMin, accuracyMax, typeWhitelist, longitudeChance);
        }
    }

    public record LongitudeChance(int start, int end, double chance) {}

    StarlightConcentrationRecipe(FluidStack fluidStack, int featureId, Conditions conditions, ResourceLocation recipeId){
        this.fluidStack = fluidStack;
        this.featureId = featureId;
        this.conditions = conditions;
        this.recipeId = recipeId;
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
            FluidStack fluidStack;
            // 液体取得
            if (json.has("fluid")) {
                fluidStack = FluidStackFromJson(json, 10);
                if (fluidStack.isEmpty()) {
                    Heliopause.LOGGER.warn("unknown 'fluid' in recipe {}. Skipping.", recipeId);
                }
            } else {
                fluidStack = FluidStack.EMPTY;
                Heliopause.LOGGER.warn("must have 'fluid' in recipe {}. Skipping.", recipeId);
            }
            int feature = json.has("feature") ? json.get("feature").getAsInt() : FEATURE_FIRST;

            Conditions conditions;
            if(json.has("starlight_conditions")){
                conditions = Conditions.parseConditions(json.getAsJsonObject("starlight_conditions"));
            }else{
                conditions = Conditions.parseConditions(null);
            }

            return new StarlightConcentrationRecipe(fluidStack, feature, conditions, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable StarlightConcentrationRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer){
            // 液体
            FluidStack fluidStack = FluidStack.readFromPacket(buffer);
            // 特徴スロット
            int feature = buffer.readInt();

            // 条件
            String dimension = buffer.readUtf();
            int magnitudeMin = buffer.readInt();
            int magnitudeMax = buffer.readInt();
            double accuracyMin = buffer.readDouble();
            double accuracyMax = buffer.readDouble();

            int whitelistSize = buffer.readInt();
            List<String> typeWhitelist = new ArrayList<>(whitelistSize);
            for(int i = 0; i < whitelistSize; i++){
                typeWhitelist.add(buffer.readUtf());
            }

            int chanceSize = buffer.readInt();
            List<StarlightConcentrationRecipe.LongitudeChance> longitudeChance = new ArrayList<>(chanceSize);
            for(int i = 0; i < chanceSize; i++){
                int start = buffer.readInt();
                int end = buffer.readInt();
                double chance = buffer.readDouble();
                longitudeChance.add(new StarlightConcentrationRecipe.LongitudeChance(start, end, chance));
            }

            StarlightConcentrationRecipe.Conditions conditions =
                new StarlightConcentrationRecipe.Conditions(dimension, magnitudeMin, magnitudeMax, accuracyMin, accuracyMax, typeWhitelist, longitudeChance);

            return new StarlightConcentrationRecipe(fluidStack, feature, conditions, recipeId);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull StarlightConcentrationRecipe recipe){
            // 液体
            recipe.fluidStack.writeToPacket(buffer);
            // 特徴スロット
            buffer.writeInt(recipe.featureId);

            // 条件
            StarlightConcentrationRecipe.Conditions recipeConditions = recipe.conditions;
            buffer.writeUtf(recipeConditions.dimension);
            buffer.writeInt(recipeConditions.magnitudeMin);
            buffer.writeInt(recipeConditions.magnitudeMax);
            buffer.writeDouble(recipeConditions.accuracyMin);
            buffer.writeDouble(recipeConditions.accuracyMax);

            buffer.writeInt(recipeConditions.typeWhitelist.size());
            for(String stellarType : recipeConditions.typeWhitelist) buffer.writeUtf(stellarType);

            buffer.writeInt(recipeConditions.longitudeChance.size());
            for(StarlightConcentrationRecipe.LongitudeChance chance : recipeConditions.longitudeChance){
                buffer.writeInt(chance.start);
                buffer.writeInt(chance.end);
                buffer.writeDouble(chance.chance);
            }

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
        // 天体生成レシピがあるか確認
        //level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.STELLAR_INSTANTIATION.get(), container, level);
        return currentDim.equals(requiredDim);
    }

    /*public record EtherFeature(FluidStack fluidStack, double requiredAccuracy){}

    public EtherFeature getEtherFeature(long levelSeed, int featureId, StellarInstantiationRecipe.StellarInstance stellarInstance){

        RandomSource randomSource = RandomSource.create(levelSeed);
        double accuracy = conditions.accuracyMin + randomSource.nextDouble()*(conditions.accuracyMax - conditions.accuracyMin);

        return;
    }*/

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public int getFeatureId() {
        return Mth.clamp(featureId, 0, 2);
    }

    public double getChance(StellarInstantiationRecipe.StellarInstance stellarInstance) {
        String type = stellarInstance.stellarType();
        if(!conditions.typeWhitelist.contains(type)){
            return 0;
        }
        int magnitude = stellarInstance.magnitude();
        if(magnitude < conditions.magnitudeMin() || magnitude > conditions.magnitudeMax()){
            return 0;
        }
        int longitude = stellarInstance.coordinate().x();
        for (LongitudeChance chance : conditions.longitudeChance) {
            if(longitude >= chance.start() && longitude <= chance.end()){
                return chance.chance();
            }
        }
        return 0;
    }

    public double getAccuracy(long localSeed){
        RandomSource randomSource = RandomSource.create(localSeed);
        return conditions.accuracyMin + randomSource.nextDouble()*(conditions.accuracyMax - conditions.accuracyMin);
    }

    public FluidStack getResultFluidStack() {
        return fluidStack;
    }

    public record FluidStackChance(FluidStack fluidStack, int maxAmount, double chance, double accuracy){}

    public static Collection<? extends FluidStackChance> pickFluidStack(RandomSource localRandom, List<FluidStackChance>[] stackChancesArray, double[] wholePoolArray) {
        List<FluidStackChance> resultChances = new ArrayList<>();
        if(stackChancesArray.length != wholePoolArray.length){
            return resultChances;
        }
        RandomSource slotRandomSource = RandomSource.create(localRandom.nextLong());
        for (int i = 0; i < stackChancesArray.length; i++) {
            slotRandomSource = RandomSource.create(slotRandomSource.nextLong());
            List<FluidStackChance> stackChances = stackChancesArray[i];
            double wholePool = wholePoolArray[i];

            if (stackChances == null || stackChances.isEmpty()) {
                resultChances.add(new FluidStackChance(FluidStack.EMPTY, 0, 0, 0)); // フォールバック処理
                continue;
            }
            // チャンス合計が0の場合は最初の候補を返す
            if (wholePool <= 0) {
                FluidStackChance first = stackChances.get(0);
                resultChances.add(new FluidStackChance(first.fluidStack().copy(), first.maxAmount(), first.chance(), first.accuracy()));
                continue;
            }

            double randomGetter = slotRandomSource.nextDouble() * wholePool;
            double threshold = 0.0;
            boolean checked = false;
            for (FluidStackChance typeChance : stackChances) {
                threshold += typeChance.chance();
                if (randomGetter <= threshold) {
                    // 内容量をランダム化 20%切り上げ ~ 100%
                    FluidStack chanceStack = typeChance.fluidStack().copy();
                    int amount = chanceStack.getAmount();
                    chanceStack.setAmount((int)Math.ceil(amount * 0.2 + amount * 0.8 * slotRandomSource.nextDouble()));

                    resultChances.add(new FluidStackChance(chanceStack, typeChance.fluidStack().getAmount(), typeChance.chance(), typeChance.accuracy()));
                    checked = true;
                    break;
                }
            }
            if(checked){
                continue;
            }
            FluidStackChance typeChance = stackChances.get(stackChances.size() - 1);
            // 内容量をランダム化 20%切り上げ ~ 100%
            FluidStack chanceStack = typeChance.fluidStack().copy();
            int amount = chanceStack.getAmount();
            chanceStack.setAmount((int)Math.ceil(amount * 0.2 + amount * 0.8 * slotRandomSource.nextDouble()));
            resultChances.add(new FluidStackChance(chanceStack, typeChance.fluidStack().getAmount(), typeChance.chance(), typeChance.accuracy()));
        }
        return resultChances;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
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
