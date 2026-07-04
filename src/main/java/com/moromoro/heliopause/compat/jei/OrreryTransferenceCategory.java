package com.moromoro.heliopause.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.item.ImitationCoreItem;
import com.moromoro.heliopause.recipe.ImitationCoreAssemblyRecipe;
import com.moromoro.heliopause.recipe.OrreryTransferenceRecipe;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Quaternionf;

import java.util.*;

public class OrreryTransferenceCategory implements IRecipeCategory<OrreryTransferenceRecipe> {
    public static final RecipeType<OrreryTransferenceRecipe> ORRERY_TRANSFERENCE_TYPE =
        new RecipeType<>(OrreryTransferenceRecipe.Serializer.ID, OrreryTransferenceRecipe.class);
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Heliopause.MODID, "textures/gui/container/orrery_transference.png");
    private static final int imageWidth = 176;
    private static final int SCALE_START = 21;
    private static final int SCALE_END = 147;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable scale;
    private final IDrawable dot;
    private final IDrawable dotShade;
    private final IDrawable orbitHide;
    private final ITickTimer orbitTimer;
    private static final float DOT_SCALE = 8;

    public OrreryTransferenceCategory(IGuiHelper helper){
        // アイコン設定
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.ORRERY_CIRCLE_BOARD.get()));
        // 背景
        this.background = helper.createDrawable(TEXTURE, 3,10, 173-3,115);
        this.scale = helper.createDrawable(TEXTURE, imageWidth, 0, 2, 7);
        this.dot = helper.createDrawable(TEXTURE, imageWidth + 2, 0, 2, 2);
        this.dotShade = helper.createDrawable(TEXTURE, imageWidth + 2, 2, 2, 2);
        this.orbitHide = helper.createDrawable(TEXTURE, 3, 10, 40, 115);
        this.orbitTimer = helper.createTickTimer(30, 30, false);
    }

    @Override
    public void draw(OrreryTransferenceRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        drawScale(recipe, guiGraphics);
        icon.draw(guiGraphics, 14, 39);
        /*String coreKey = recipe.getCenterStarId();
        ItemStack coreItemStack = ImitationCoreItem.getImitationCoreWithTag(coreKey);
        FluidStack coreFluidStack = getCoreFluidUsage(coreItemStack);
        if(!coreFluidStack.isEmpty()) {
            guiGraphics.drawString(
                Minecraft.getInstance().font, coreFluidStack.getAmount() +"mb/sec", 14, 20, 0xFFFFFF);
            //builder.addSlot(RecipeIngredientRole.OUTPUT, 14,20).addFluidStack(coreFluidStack.getFluid(), coreFluidStack.getAmount(), coreFluidStack.getTag());
        }
        FluidStack resultFluidStack = recipe.getResultStellarStack().fluidStack();
        if(!resultFluidStack.isEmpty()){
            guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                resultFluidStack.getAmount() + "mb",
                22, 82, 0xFFFFFF
            );
        }*/
    }

    @Override
    public RecipeType<OrreryTransferenceRecipe> getRecipeType() {
        return ORRERY_TRANSFERENCE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.heliopause.orrery");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    private void drawScale(OrreryTransferenceRecipe recipe, GuiGraphics guiGraphics){
        // 材料スロット
        List<OrreryTransferenceRecipe.StellarIngredient> stellarIngredients = new ArrayList<>(recipe.getStellarIngredients());

        if(!stellarIngredients.isEmpty()){
            stellarIngredients.sort(Comparator.comparingDouble(OrreryTransferenceRecipe.StellarIngredient::resonanceRatio));
            double minRatio = stellarIngredients.get(0).resonanceRatio();
            double normalizedMaxRatio = stellarIngredients.get(stellarIngredients.size() - 1).resonanceRatio() / minRatio;

            for (OrreryTransferenceRecipe.StellarIngredient ingredient : stellarIngredients) {
                double normalizedRatio = ingredient.resonanceRatio() / minRatio;
                double normalizedLength = (SCALE_END - SCALE_START) * (normalizedRatio / normalizedMaxRatio);
                drawOrbit(normalizedLength, guiGraphics);
                int scalePos = SCALE_START + (int)normalizedLength;
                scale.draw(guiGraphics, scalePos, 57);
                /*guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0,0, 1);
                guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    String.valueOf((Math.floor(normalizedRatio * 10f))/10f),
                    scalePos + 1, 65, 0xFFFFFF
                );
                if(!ingredient.fluidStack().isEmpty()){
                    guiGraphics.drawCenteredString(
                        Minecraft.getInstance().font,
                        ingredient.fluidStack().getAmount() + "mb",
                        scalePos +1, 30, 0xFFFFFF
                    );
                }
                guiGraphics.pose().popPose();*/
            }
        }
    }

    private void drawOrbit(double radius, GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        //double angleEnd = 180 - Math.acos(16/radius) * 180 / Math.PI;
        float length = (float) ((radius - 0.5) * Math.PI);
        int circleDiv = (int) (length/DOT_SCALE);
        float rotOffset = ((orbitTimer.getValue() * (360f/circleDiv) / 30f) * (int)(200f/ radius)) % (360f/circleDiv) - 360f/circleDiv;
        float dotScale = length / circleDiv + 0.5f;
        // 影の描画
        poseStack.pushPose();
        poseStack.translate(SCALE_START, 63, 0);
        poseStack.scale(1, 0.36f, 1);
        poseStack.mulPose(new Quaternionf().rotateZ((float) ((180 + rotOffset) * Math.PI / 180)));
        for (double dotId = 0; dotId < 180 - (180f / circleDiv); dotId+=(360f / circleDiv)) {
            poseStack.mulPose(new Quaternionf().rotateZ((float) ((360f/circleDiv) * Math.PI / 180)));
            poseStack.translate(-dotScale, (radius - 0.5), 0);
            poseStack.scale(dotScale, 1f, 1f);
            dotShade.draw(guiGraphics, 0, 0);
            poseStack.scale(1f/dotScale, 1f, 1f);
            poseStack.translate(dotScale, -(radius - 0.5), 0);
        }
        poseStack.popPose();
        // 線の描画
        poseStack.pushPose();
        poseStack.translate(SCALE_START, 62, 0);
        poseStack.scale(1, 0.36f, 1);
        poseStack.mulPose(new Quaternionf().rotateZ((float) ((180 + rotOffset) * Math.PI / 180)));
        for (double dotId = 0; dotId < 180 - (180f / circleDiv); dotId+=(360f / circleDiv)) {
            poseStack.mulPose(new Quaternionf().rotateZ((float) ((360f/circleDiv) * Math.PI / 180)));
            poseStack.translate(-dotScale, (radius - 0.5), 0);
            poseStack.scale(dotScale, 1f, 1f);
            dot.draw(guiGraphics, 0, 0);
            poseStack.scale(1f/dotScale, 1f, 1f);
            poseStack.translate(dotScale, -(radius - 0.5), 0);
        }
        poseStack.popPose();
        // 隠す
        orbitHide.draw(guiGraphics,0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OrreryTransferenceRecipe recipe, IFocusGroup focuses) {
        // 中心スロット
        String coreKey = recipe.getCenterStarId();
        ItemStack coreItemStack = ImitationCoreItem.getImitationCoreWithTag(coreKey);
        builder.addSlot(RecipeIngredientRole.INPUT, 14,2).addItemStack(coreItemStack);

        FluidStack coreFluidStack = getCoreFluidUsage(coreItemStack);
        if(!coreFluidStack.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 14,20)
                .addFluidStack(coreFluidStack.getFluid(), coreFluidStack.getAmount(), coreFluidStack.getTag())
                .setFluidRenderer(1000, false, 16,16)
                .addTooltipCallback((view, tooltip) -> {
                    String defaultTooltip = tooltip.get(tooltip.size()-1).getString();
                    tooltip.remove(tooltip.size()-1);
                    tooltip.add(Component.literal(defaultTooltip + "/sec").withStyle(ChatFormatting.GRAY));
                });
        }

        // 材料スロット
        List<OrreryTransferenceRecipe.StellarIngredient> stellarIngredients = new ArrayList<>(recipe.getStellarIngredients());

        if(!stellarIngredients.isEmpty()){
            stellarIngredients.sort(Comparator.comparingDouble(OrreryTransferenceRecipe.StellarIngredient::resonanceRatio));
            double minRatio = stellarIngredients.get(0).resonanceRatio();
            double normalizedMaxRatio = stellarIngredients.get(stellarIngredients.size() - 1).resonanceRatio() / minRatio;

            for (OrreryTransferenceRecipe.StellarIngredient ingredient : stellarIngredients) {
                double normalizedRatio = ingredient.resonanceRatio() / minRatio;
                int scalePos = SCALE_START + (int)((SCALE_END - SCALE_START) * (normalizedRatio / normalizedMaxRatio));
                if (!ingredient.ingredient().isEmpty()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, scalePos - 7, 39).addIngredients(ingredient.ingredient());
                } else if (!ingredient.fluidStack().isEmpty()) {
                    FluidStack fluidStack = ingredient.fluidStack();
                    builder.addSlot(RecipeIngredientRole.INPUT, scalePos - 7, 39)
                        .addFluidStack(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag())
                        .setFluidRenderer(1000, false, 16,16);;
                }
            }

        }
        // 結果スロット
        CircumstellarIngredient.StellarStack result = recipe.getResultStellarStack();
        if(!result.itemStack().isEmpty()){
            ItemStack resultItemStack = result.itemStack();
            builder.addSlot(RecipeIngredientRole.OUTPUT, 14,92).addItemStack(resultItemStack);
        }
        else if(!result.fluidStack().isEmpty()){
            FluidStack resultFluidStack = result.fluidStack();
            builder.addSlot(RecipeIngredientRole.OUTPUT, 14,92)
                .addFluidStack(resultFluidStack.getFluid(), resultFluidStack.getAmount(), resultFluidStack.getTag())
                .setFluidRenderer(1000, false, 16,16);
        }
    }

    private FluidStack getCoreFluidUsage(ItemStack coreItemStack) {
        Level level = Minecraft.getInstance().level;
        //icon.draw(guiGraphics, 14, 3);
        if(level!=null) {
            Optional<ImitationCoreAssemblyRecipe> starOptional =
                level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.IMITATION_CORE_ASSEMBLY.get(), new SimpleContainer(coreItemStack.copy()), level);
            if(starOptional.isPresent()){
                ImitationCoreAssemblyRecipe star = starOptional.get();
                //icon.draw(guiGraphics, 14, 21);
                //builder.addSlot(RecipeIngredientRole.OUTPUT, 14,92).addItemStack(resultItemStack);
                return star.getFluidPerSecond();
            }
        }
        return FluidStack.EMPTY;
    }
}
