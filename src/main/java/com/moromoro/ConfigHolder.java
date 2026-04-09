package com.moromoro;

import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.registry.TagRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class ConfigHolder {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 液体の名前と色情報を格納する辞書配列を作成
    public static final Map<String, ForgeConfigSpec.IntValue> FLUID_COLORS = new HashMap<>();

    // 季節オフセット
    public static final ForgeConfigSpec.IntValue SEASON_OFFSET;

    // 鏡筒関係
    public static final ForgeConfigSpec.IntValue MAX_BARREL_LENGTH;
    public static final ForgeConfigSpec.BooleanValue BARREL_BLOCK_INFO_ALWAYS;
    public static final ForgeConfigSpec.BooleanValue BARREL_ENTITY_INFO_ALWAYS;
    //public static final Map<TagKey<Block>, ForgeConfigSpec.DoubleValue> LENS_BARREL_ACCURACIES = new HashMap<>();

    // シデロスタットの視界確認距離
    public static final ForgeConfigSpec.DoubleValue MAX_VIEW_SIDEROSTAT;
    // 星明かり収斂器の視界確認距離
    public static final ForgeConfigSpec.DoubleValue MAX_VIEW_CONCENTRATOR;

    static {
        // 液体
        /*BUILDER.push("fluidColors");
        // 各液体のデフォルト色情報を定義
        addFluidColor("minecraft:water", 0x3F76E4);
        addFluidColor("minecraft:lava", 0xFF6C00);
        //...
        BUILDER.pop();*/

        // 季節オフセット
        BUILDER.push("season");
            SEASON_OFFSET = BUILDER.comment("Definition of the offset for the start of the season. January 1st(Default) is 0.")
                .defineInRange("offset",0, (int)-Season.YEAR_LENGTH, (int)Season.YEAR_LENGTH);
        BUILDER.pop();

        // 鏡筒関係
        BUILDER.push("lensBarrel");
            MAX_BARREL_LENGTH = BUILDER.comment("Definition of the maximum lens barrel length, including mirrors.")
                .defineInRange("maxLength", 4, 2, 8);

            BARREL_BLOCK_INFO_ALWAYS = BUILDER.comment("Whether to always display information for barrel-blocks. If false, it will only be displayed when the Shift key is down.")
                .define("alwaysDisplayBlockInfo", false);
            BARREL_ENTITY_INFO_ALWAYS = BUILDER.comment("Whether to always display information for barrel-entity. If false, it will only be displayed when the Shift key is down.")
                .define("alwaysDisplayEntityInfo", true);
            /*BUILDER.comment("Definition of lens barrel types and accuracy.");
            addLensBarrelAccuracy(TagRegistry.Blocks.WOODEN_LENS_BARREL, 0.7);
            addLensBarrelAccuracy(TagRegistry.Blocks.ALCHEMY_BIRON_LENS_BARREL, 0.9);*/
        //...
        BUILDER.pop();

        BUILDER.push("visibilityCheck");
        BUILDER.comment("Definition of the distance at which to check if any view is obstructed. A large number may cause lag.");
        // シデロスタットの視界確認距離
        MAX_VIEW_SIDEROSTAT = BUILDER.defineInRange("siderostat", 255.0, 0.7,8191.0);
        // 星明かり収斂器の視界確認距離
        MAX_VIEW_CONCENTRATOR = BUILDER.defineInRange("concentrator", 255.0, 0.7, 8191.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /*private static void addFluidColor(String fluidName, int defaultColor) {
        ForgeConfigSpec.IntValue colorValue = BUILDER.comment("Color of " + fluidName + " in RGB. Default: " + defaultColor)
                .defineInRange(fluidName, defaultColor, 0x000000, 0xFFFFFF);
        FLUID_COLORS.put(fluidName, colorValue);
    }*/

    /*private static void addLensBarrelAccuracy(TagKey<Block> tagKey, double defaultAccuracy){
        ForgeConfigSpec.DoubleValue accuracyValue = BUILDER.defineInRange(tagKey.location().toString(), defaultAccuracy, 0.1, 128.0);
        LENS_BARREL_ACCURACIES.put(tagKey, accuracyValue);
    }*/
}
