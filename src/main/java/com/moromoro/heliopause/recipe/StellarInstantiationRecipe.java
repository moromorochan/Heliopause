package com.moromoro.heliopause.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class StellarInstantiationRecipe implements Recipe<Container> {

    // レシピパラメータ
    private final String dimension;
    private final List<Instancer> instancerList;
    private final ResourceLocation recipeId;

    public record Instancer(int magnitude, int count, List<TypeChance> typeList){}

    public record TypeChance(String stellarType, double chance){}

    public StellarInstantiationRecipe(String dimension, List<Instancer> instancerList, ResourceLocation recipeId){
        this.dimension = dimension;
        this.instancerList = instancerList;
        this.recipeId = recipeId;
    }

    public static class Type implements RecipeType<StellarInstantiationRecipe>{
        public static final StellarInstantiationRecipe.Type INSTANCE = new StellarInstantiationRecipe.Type();
        public static final String ID = "stellar_instantiation";
    }

    public static class Serializer implements RecipeSerializer<StellarInstantiationRecipe>{
        public static final StellarInstantiationRecipe.Serializer INSTANCE = new StellarInstantiationRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, StellarInstantiationRecipe.Type.ID);

        // jsonレシピ読み込み
        @Override
        public @NotNull StellarInstantiationRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json){
            // ディメンション
            String dimension = json.has("dimension") ? json.get("dimension").getAsString() : "minecraft:overworld";

            // 天体インスタンス
            List<Instancer> instances = new ArrayList<>();
            if (json.has("stellar_instances")) {
                JsonArray arr = json.getAsJsonArray("stellar_instances");
                for (JsonElement elem : arr) {
                    JsonObject obj = elem.getAsJsonObject();
                    int magnitude = obj.has("magnitude") ? obj.get("magnitude").getAsInt() : 1;
                    int count = obj.has("count") ? obj.get("count").getAsInt() : 0;

                    List<TypeChance> typeList = new ArrayList<>();
                    if (obj.has("type")) {
                        JsonArray types = obj.getAsJsonArray("type");
                        for (JsonElement tElem : types) {
                            JsonObject tObj = tElem.getAsJsonObject();
                            String name = tObj.has("name") ? tObj.get("name").getAsString() : "";
                            double chance = tObj.has("chance") ? tObj.get("chance").getAsDouble() : 0.0;
                            typeList.add(new TypeChance(name, chance));
                        }
                    }

                    instances.add(new Instancer(magnitude, count, typeList));
                }
            }

            return new StellarInstantiationRecipe(dimension, instances, recipeId);
        }

        //サーバー・クライアント間のやりとり
        @Override
        public @Nullable StellarInstantiationRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer){
            String dimension = buffer.readUtf();

            int instancesSize = buffer.readInt();
            List<Instancer> instances = new ArrayList<>(instancesSize);
            for (int i = 0; i < instancesSize; i++) {
                int magnitude = buffer.readInt();
                int count = buffer.readInt();

                int typeSize = buffer.readInt();
                List<TypeChance> typeList = new ArrayList<>(typeSize);
                for (int j = 0; j < typeSize; j++) {
                    String name = buffer.readUtf();
                    double chance = buffer.readDouble();
                    typeList.add(new TypeChance(name, chance));
                }

                instances.add(new Instancer(magnitude, count, typeList));
            }

            return new StellarInstantiationRecipe(dimension, instances, recipeId);

        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull StellarInstantiationRecipe recipe){
            buffer.writeUtf(recipe.dimension);

            List<Instancer> instances = recipe.instancerList;
            buffer.writeInt(instances.size());
            for (Instancer inst : instances) {
                buffer.writeInt(inst.magnitude());
                buffer.writeInt(inst.count());

                List<TypeChance> types = inst.typeList();
                buffer.writeInt(types.size());
                for (TypeChance typeChance : types) {
                    buffer.writeUtf(typeChance.stellarType());
                    buffer.writeDouble(typeChance.chance());
                }
            }

        }
    }

    @Override
    public boolean matches(@NotNull Container container, Level level) {
        // ディメンション一致確認
        ResourceLocation requiredDim = new ResourceLocation(this.dimension);
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

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }
    
    public record StellarInstance(String stellarType, int magnitude, Vector2i coordinate, long localSeed){}

    // ランダムから星の位置生成
    public static List<StellarInstance> getStars(StellarInstantiationRecipe recipe, long levelSeed){
        // レベルのシード値から
        long localSeed = levelSeed;
        List<StellarInstance> list = new ArrayList<>();
        // 定義のまとまり毎に生成
        for (Instancer instancer : recipe.instancerList) {
            int count = instancer.count();
            int magnitude = instancer.magnitude();
            // 天体の種類プール
            double wholePool = 0;
            for (TypeChance typeChance : instancer.typeList) {
                wholePool += typeChance.chance();
            }

            for (int i = 0; i < count; i++) {
                RandomSource localRandom = RandomSource.create(localSeed);
                int x = localRandom.nextInt(ConcentratorBlockEntity.STAR_RANGE_X);
                int y = localRandom.nextInt(ConcentratorBlockEntity.STAR_RANGE_MIN_Y, ConcentratorBlockEntity.STAR_RANGE_MAX_Y);
                // 種類プールからランダム生成
                String stellarType = pickType(localRandom, instancer.typeList, wholePool);

                list.add(new StellarInstance(stellarType, magnitude, new Vector2i(x,y), localSeed));
                localSeed = localRandom.nextLong();
            }
        }
        return list;
    }

    private static String pickType(RandomSource localRandom, List<TypeChance> types, double wholePool) {
        if (types == null || types.isEmpty()) return "fixed_star"; // フォールバック処理
        // チャンス合計が0の場合は最初の候補を返す
        if (wholePool <= 0) {
            return types.get(0).stellarType();
        }

        double randomDouble = localRandom.nextDouble() * wholePool;
        double threshold = 0.0;
        for (TypeChance typeChance : types) {
            threshold += typeChance.chance();
            if (randomDouble <= threshold) {
                return typeChance.stellarType();
            }
        }
        return types.get(types.size() - 1).stellarType();
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
