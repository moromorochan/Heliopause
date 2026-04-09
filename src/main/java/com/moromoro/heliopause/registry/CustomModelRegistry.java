package com.moromoro.heliopause.registry;


import com.moromoro.Heliopause;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Heliopause.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CustomModelRegistry {
    // 天体
    public static final ResourceLocation IMI_ROCKY = new ResourceLocation(Heliopause.MODID,"decoration/imitation_star/rocky");
    public static final ResourceLocation IMI_GAS_STRIPES = new ResourceLocation(Heliopause.MODID,"decoration/imitation_star/gas_stripes");
    public static final ResourceLocation IMI_GAS_PLAIN = new ResourceLocation(Heliopause.MODID,"decoration/imitation_star/gas_plain");
    public static final ResourceLocation IMI_FIXED_STAR = new ResourceLocation(Heliopause.MODID,"decoration/imitation_star/fixed_star");
    public static final ResourceLocation IMI_COLLAPSAR = new ResourceLocation(Heliopause.MODID,"decoration/imitation_star/collapsar");
    // パーツ
    public static final ResourceLocation ORRERY_ITEM_SPHERE = new ResourceLocation(Heliopause.MODID,"decoration/orrery/item_sphere");
    public static final ResourceLocation ORRERY_CONCEALED_SPHERE = new ResourceLocation(Heliopause.MODID,"decoration/orrery/concealed_sphere");
    public static final ResourceLocation ORRERY_SUB_SPHERE = new ResourceLocation(Heliopause.MODID,"decoration/orrery/sub_sphere");
    public static final ResourceLocation ORRERY_ORBIT = new ResourceLocation(Heliopause.MODID,"decoration/orrery/orbit");
    public static final ResourceLocation ORRERY_ORBIT_END = new ResourceLocation(Heliopause.MODID,"decoration/orrery/orbit_end");

    public static final ResourceLocation SIDEROSTAT_MOON = new ResourceLocation(Heliopause.MODID, "decoration/siderostat/moon");
    public static final ResourceLocation SIDEROSTAT_SPRING = new ResourceLocation(Heliopause.MODID, "decoration/siderostat/spring");

    // 星明かり収斂器
    //public static final ResourceLocation STARLIGHT_CONCENTRATOR_PITCH = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/pitch");
    //public static final ResourceLocation STARLIGHT_CONCENTRATOR_TURNTABLE = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable");
    //public static final ResourceLocation STARLIGHT_CONCENTRATOR_WEIGHT = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/weight");
        // ターンテーブル側
    public static final ResourceLocation CONCENTRATOR_TURNTABLE_BOTTOM = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable/bottom");
    public static final ResourceLocation CONCENTRATOR_TURNTABLE_MIDDLE = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable/middle");
    public static final ResourceLocation CONCENTRATOR_TURNTABLE_EX_HALF = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable/ex_half");
    public static final ResourceLocation CONCENTRATOR_TURNTABLE_EX_FULL = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable/ex_full");
        // 歯車
    public static final ResourceLocation CONCENTRATOR_TURNTABLE_GEAR = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/turntable/gear");
        // 鏡筒側
    public static final ResourceLocation CONCENTRATOR_CYLINDER_BOTTOM = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/bottom");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_MIDDLE = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/middle");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_TOP = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/top");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_UPPER_EX_HALF = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/upper_ex_half");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_UPPER_EX_FULL = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/upper_ex_full");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_LOWER_EX_HALF = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/lower_ex_half");
    public static final ResourceLocation CONCENTRATOR_CYLINDER_LOWER_EX_FULL = new ResourceLocation(Heliopause.MODID, "decoration/concentrator/cylinder/lower_ex_full");
    // インターフェース陣
    public static final ResourceLocation CIRCLE_DOTTED = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_dotted");
    public static final ResourceLocation LINE_DOTTED = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_dotted");
    public static final ResourceLocation ARROW_DEFAULT = new ResourceLocation(Heliopause.MODID, "decoration/circle/arrow_default");
    // デフォルト陣
    public static final ResourceLocation CIRCLE_DEFAULT = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_default");
    public static final ResourceLocation LINE_DEFAULT = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_default");
    // 遷天模倣陣
    public static final ResourceLocation CIRCLE_ORRERY = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_orrery");
    public static final ResourceLocation LINE_ORRERY = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_orrery");
    // 経緯台陣
    public static final ResourceLocation CIRCLE_ALT_AZIMUTH = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_circle_alt_azimuth");
    public static final ResourceLocation LINE_ALT_AZIMUTH = new ResourceLocation(Heliopause.MODID, "decoration/circle/multi_line_alt_azimuth");

    //カスタムモデルの登録
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // 天体
        event.register(IMI_ROCKY);
        event.register(IMI_GAS_STRIPES);
        event.register(IMI_GAS_PLAIN);
        event.register(IMI_FIXED_STAR);
        event.register(IMI_COLLAPSAR);
        // パーツ
        event.register(ORRERY_ITEM_SPHERE);
        event.register(ORRERY_CONCEALED_SPHERE);
        event.register(ORRERY_SUB_SPHERE);
        event.register(ORRERY_ORBIT);
        event.register(ORRERY_ORBIT_END);
        event.register(SIDEROSTAT_MOON);
        event.register(SIDEROSTAT_SPRING);
        //event.register(STARLIGHT_CONCENTRATOR_PITCH);
        //event.register(STARLIGHT_CONCENTRATOR_TURNTABLE);
        //event.register(STARLIGHT_CONCENTRATOR_WEIGHT);
        event.register(CONCENTRATOR_TURNTABLE_BOTTOM);
        event.register(CONCENTRATOR_TURNTABLE_MIDDLE);
        event.register(CONCENTRATOR_TURNTABLE_EX_HALF);
        event.register(CONCENTRATOR_TURNTABLE_EX_FULL);
        event.register(CONCENTRATOR_TURNTABLE_GEAR);
        event.register(CONCENTRATOR_CYLINDER_BOTTOM);
        event.register(CONCENTRATOR_CYLINDER_MIDDLE);
        event.register(CONCENTRATOR_CYLINDER_TOP);
        event.register(CONCENTRATOR_CYLINDER_UPPER_EX_HALF);
        event.register(CONCENTRATOR_CYLINDER_UPPER_EX_FULL);
        event.register(CONCENTRATOR_CYLINDER_LOWER_EX_HALF);
        event.register(CONCENTRATOR_CYLINDER_LOWER_EX_FULL);
        // インターフェース陣
        event.register(CIRCLE_DOTTED);
        event.register(LINE_DOTTED);
        event.register(ARROW_DEFAULT);
        // デフォルト陣
        event.register(CIRCLE_DEFAULT);
        event.register(LINE_DEFAULT);
        // 遷天模倣陣
        event.register(CIRCLE_ORRERY);
        event.register(LINE_ORRERY);
        // 経緯台陣
        event.register(CIRCLE_ALT_AZIMUTH);
        event.register(LINE_ALT_AZIMUTH);
    }
}
