package com.moromoro.heliopause.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.enumProperty.LensBarrelCoverageIconValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix.GSON;

// 鏡筒の収斂帯域幅を登録する
public class LensBarrelCoverageListener extends SimpleJsonResourceReloadListener {

    public static final String TYPE_ID = "lens_barrel_coverage";

    public record BarrelCoverageData(
        List<String> block,
        String icon,
        int[] icon_color,
        String name
    ) {}

    //private static final Map<ResourceLocation, BarrelCoverageData> SERVER_DATA = new HashMap<>();
    public static Map<ResourceLocation, BarrelCoverageData> DATA = new HashMap<>();

    public LensBarrelCoverageListener() {
        super(new Gson(), (TYPE_ID));
    }

    // サーバー側の処理
    public static void clear() {
        DATA.clear();
    }

    public static void put(ResourceLocation id, BarrelCoverageData data) {
        DATA.put(id, data);
    }

    // クライアント側の処理
    public static void replace(Map<ResourceLocation, BarrelCoverageData> newData) {
        DATA = newData;
    }

    public static BarrelCoverageData get(ResourceLocation id) {
        return DATA.get(id);
    }

    // サーバー・クライアント共通
    public static Map<ResourceLocation, BarrelCoverageData> getMap() {
        return DATA;
    }

    // json読み込み
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        clear();
        
        // 統合データの準備
        Map<String, BarrelCoverageData> merged = new HashMap<>();

        jsonMap.forEach((id, json) -> {
            try {
                /*BarrelCoverageData data = GSON.fromJson(json, BarrelCoverageData.class);
                // アイコン設定チェック
                if(LensBarrelCoverageIconValue.fromString(data.icon)==null){
                    Heliopause.LOGGER.warn("Unknown icon '{}' in {}, using circle", data.icon(), id);
                    data = new BarrelCoverageData(data.block(), LensBarrelCoverageIconValue.CIRCLE.getSerializedName(), data.icon_color(), data.name());
                }
                // ブロック指定チェック
                if(data.block().isEmpty()){
                    throw new IllegalArgumentException("block is Empty");
                }
                put(id, data);*/
                JsonObject obj = json.getAsJsonObject();
                
                // name取得
                List<String> names = new ArrayList<>();
                if (obj.get("name").isJsonArray()) {
                    obj.getAsJsonArray("name").forEach(e -> names.add(e.getAsString()));
                } else {
                    names.add(obj.get("name").getAsString());
                }
                
                // block取得
                List<String> blocks = new ArrayList<>();
                if (obj.get("block").isJsonArray()) {
                    obj.getAsJsonArray("block").forEach(e -> blocks.add(e.getAsString()));
                } else {
                    blocks.add(obj.get("block").getAsString());
                }
                
                // icon、iconColor取得
                String icon = obj.has("icon") ? obj.get("icon").getAsString() : null;
                int[] color;
                if (obj.has("icon_color")) {
                    JsonArray arr = obj.getAsJsonArray("icon_color");
                    color = new int[]{arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt()};
                }else{
                    color = null;
                }
                
                // 一致する名前ごとに内容を統合する
                for (String name : names) {
                    BarrelCoverageData presentData = merged.get(name);
                    
                    List<String> newBlocks = new ArrayList<>();
                    String newIcon = icon;
                    int[] newColor = color;
                    
                    // 既存があれば追加
                    if (presentData != null) {
                        // block を追加
                        newBlocks.addAll(presentData.block());
                        newBlocks.addAll(blocks);
                        
                        // icon があれば上書き、なければ old を維持
                        if (newIcon == null) newIcon = presentData.icon();
                        
                        // color があれば上書き、なければ old を維持
                        if (newColor == null) newColor = presentData.icon_color();
                    }
                    // なければ新規登録
                    else {
                        newBlocks.addAll(blocks);
                        
                        // デフォルト値
                        if (newIcon == null) newIcon = "circle";
                        if (newColor == null) newColor = new int[]{255, 255, 255};
                    }
                    
                    merged.put(name, new BarrelCoverageData(newBlocks, newIcon, newColor, name));
                }
                
            } catch (Exception e) {
                Heliopause.LOGGER.error("Failed to parse barrel coverage json: {}", id, e);
            }
        });
        
        // 統合データの登録
        merged.forEach((name, data) -> {
            ResourceLocation dataLoc = new ResourceLocation(Heliopause.MODID, name);
            put(dataLoc, data);
        });

        Heliopause.LOGGER.info("Loaded {} barrel coverage entries", getMap().size());
    }

}
