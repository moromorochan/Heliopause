package com.moromoro;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.Map;

public class ConfigHolder {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 液体の名前と色情報を格納する辞書配列を作成
    public static final Map<String, ForgeConfigSpec.IntValue> FLUID_COLORS = new HashMap<>();

    static {
        // 液体
        BUILDER.push("fluidColors");

        // 各液体のデフォルト色情報を定義
        addFluidColor("minecraft:water", 0x3F76E4);
        addFluidColor("minecraft:lava", 0xFF6C00);
        // 他の液体...

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static void addFluidColor(String fluidName, int defaultColor) {
        ForgeConfigSpec.IntValue colorValue = BUILDER.comment("Color of " + fluidName + " in RGB. Default: " + defaultColor)
                .defineInRange(fluidName, defaultColor, 0x000000, 0xFFFFFF);
        FLUID_COLORS.put(fluidName, colorValue);
    }
}
