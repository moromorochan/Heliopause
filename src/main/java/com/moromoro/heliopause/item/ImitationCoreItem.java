package com.moromoro.heliopause.item;

import com.moromoro.heliopause.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImitationCoreItem extends Item {
    // nbt
    private static final String STAR_ID = "star_id";
    /*private static final String MODEL_TYPE = "model";
    private static final String SCALE = "scale";
    private static final String COLOR = "color";
    private static final String CENT_FORCE = "cent_force";*/

    public ImitationCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, level, tooltip, flag);
        String starId = getStarId(itemStack);
        if(starId != null){
            tooltip.add(Component.translatable("item.heliopause.imitation_core.description_" + starId).withStyle(ChatFormatting.BLUE));
        }else {
            tooltip.add(Component.translatable("item.heliopause.imitation_core.description_empty").withStyle(ChatFormatting.GRAY));
        }
    }

    public static @NotNull ItemStack getImitationCoreWithTag(String starId) {
        ItemStack itemStack = ItemRegistry.IMITATION_CORE_ITEM.get().getDefaultInstance();
        ImitationCoreItem.setStarId(itemStack, starId);

        return itemStack;
    }

    public static void setStarId(ItemStack itemStack, String id){
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(STAR_ID, id);
    }

    public static @Nullable String getStarId(ItemStack itemStack){
        CompoundTag tag = itemStack.getTag();
        if(tag != null && tag.contains(STAR_ID)){
            return tag.getString(STAR_ID);
        }
        return null;
    }

    /*public static void setModelType(ItemStack itemStack, String modelType){
        CompoundTag block = itemStack.getOrCreateTag();
        block.putString(MODEL_TYPE, modelType);
    }

    public static BakedModel getModelType(ItemStack itemStack){
        CompoundTag block = itemStack.getTag();
        if(block != null && block.contains(MODEL_TYPE)){
            return switch (block.getString(MODEL_TYPE)) {
                // 岩石
                case "rocky" -> Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_ROCKY);
                // ガス縞模様
                case "gas_stripes" ->
                    Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_STRIPES);
                // ガス縞無し
                case "gas_plain" ->
                    Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_PLAIN);
                // 主系列星
                case "fixed_star" ->
                    Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_FIXED_STAR);
                // 崩壊星
                case "collapsar" ->
                    Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_COLLAPSAR);
                default -> Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_PLAIN);
            };
        }
        return Minecraft.getInstance().getModelManager().getModel(CustomModelRegistry.IMI_GAS_PLAIN);
    }

    public static void setScale(ItemStack itemStack, float scale){
        CompoundTag block = itemStack.getOrCreateTag();
        block.putFloat(SCALE, scale);
    }

    public static float getScale(ItemStack itemStack){
        CompoundTag block = itemStack.getTag();
        if(block != null && block.contains(SCALE)){
            return block.getFloat(SCALE);
        }
        return 1;
    }

    public static void setColor(ItemStack itemStack, int r, int g, int b){
        CompoundTag block = itemStack.getOrCreateTag();
        block.putIntArray(COLOR, new int[]{r,g,b});
    }

    public static int[] getColor(ItemStack itemStack){
        CompoundTag block = itemStack.getTag();
        if(block != null && block.contains(COLOR)){
            return block.getIntArray(COLOR);
        }
        return new int[]{255, 255, 255};
    }

    public static void setCentForce(ItemStack itemStack, float force){
        CompoundTag block = itemStack.getOrCreateTag();
        block.putFloat(CENT_FORCE, force);
    }

    public static float getCentForce(ItemStack itemStack){
        CompoundTag block = itemStack.getTag();
        if(block != null && block.contains(CENT_FORCE)){
            return block.getFloat(CENT_FORCE);
        }
        return 0.5f;
    }*/
}
