package com.moromoro.heliopause.registry;


import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CustomModelRegistry {
    public static final Map<String, ResourceLocation> DECOR_MODELS = Map.ofEntries(
        // 天体
        Map.entry("moon", new ResourceLocation(Heliopause.MODID, "decoration/phantom/moon")),
        Map.entry("siderostat_spring", new ResourceLocation(Heliopause.MODID, "decoration/siderostat_spring")),

        // 陣
        Map.entry("circle_default", new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_default")),
        Map.entry("line_default", new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_default")),
        Map.entry("arrow_default", new ResourceLocation(Heliopause.MODID, "decoration/circle/arrow_default")),

        Map.entry("line_dotted", new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_dotted"))
    );

    //カスタムモデルの登録
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        DECOR_MODELS.values().forEach(event::register);
    }
}
