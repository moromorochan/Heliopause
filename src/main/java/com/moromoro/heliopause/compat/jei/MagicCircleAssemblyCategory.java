package com.moromoro.heliopause.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.recipe.MagicCircleAssemblyRecipe;
import com.moromoro.heliopause.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.moromoro.heliopause.recipe.MagicCircleAssemblyRecipe.*;

public class MagicCircleAssemblyCategory  implements IRecipeCategory<MagicCircleAssemblyRecipe> {
    public static final RecipeType<MagicCircleAssemblyRecipe> MAGIC_CIRCLE_ASSEMBLY_TYPE =
        new RecipeType<>(Serializer.ID, MagicCircleAssemblyRecipe.class);
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Heliopause.MODID, "textures/gui/container/magic_circle_assembly.png");
    private static final int imageWidth = 176;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable symbol;
    private final IDrawable node;
    private final IDrawable dot;
    private static final float DOT_SCALE = 4;

    private record nodeWithPos(Node node, Vector2d pos){}
   // private List<nodeWithPos> nodeWithPosList = new ArrayList<>();

    public MagicCircleAssemblyCategory(IGuiHelper helper) {
        // アイコン設定
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.WRITTEN_BOARD.get()));
        // 背景
        this.background = helper.createDrawable(TEXTURE, 3,10, 173-3,134);
        this.symbol = helper.createDrawable(TEXTURE, imageWidth, 0, 11, 12);
        this.node = helper.createDrawable(TEXTURE, imageWidth + 11, 0,7,8);
        this.dot = helper.createDrawable(TEXTURE, imageWidth + 18,0,2,2);
    }

    @Override
    public void draw(MagicCircleAssemblyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        icon.draw(guiGraphics,140, 21);
        Font font = Minecraft.getInstance().font;
        Component tooltip =
        switch (recipe.getTrigger().type()) {
            case "place_on" -> Component.translatable("recipe.heliopause.magic_circle_assembly_trigger_place_on");
            case "use" -> Component.translatable("recipe.heliopause.magic_circle_assembly_trigger_use");
            case "consume" -> Component.translatable("recipe.heliopause.magic_circle_assembly_trigger_consume");
            default -> Component.empty();
        };
        double tooltipLength = font.width(tooltip.getString());
        guiGraphics.drawString(font, tooltip, 136 - (int)tooltipLength, 5, 0x808080,false);
        drawNetwork(recipe, guiGraphics);
    }

    record drawCircleData(List<String> keys, double radius){}

    private void drawNetwork(MagicCircleAssemblyRecipe recipe, GuiGraphics guiGraphics) {
        //List<MagicCircleAssemblyRecipe.Part> parts = recipe.getParts();
        List<Node> nodes = recipe.getNodes();
        List<Circle> circles = recipe.getCircles();
        // ルートの円を取得
        List<Circle> rootCircles = circles.stream().filter(Circle -> Circle.center().equals(ORIGIN_KEY)).toList();

        symbol.draw(guiGraphics, 60,65);

        Set<nodeWithPos> nodeWithPosList = new HashSet<>();

        List<drawCircleData> drawCircleList = new ArrayList<>();
        for (int i = 0; i < rootCircles.size(); i++) {
            Circle circle = rootCircles.get(i);
            // 円周上のノードを取得
            List<String> containsKey = circle.contains();
            // 半径を用意
            double radius = 20+(1-((double)i/rootCircles.size()))*40;
            drawCircleList.add(new drawCircleData(containsKey, radius));
        }
        // 円を描画
        nodeWithPosList.addAll(drawCircle(nodes, circles, drawCircleList, nodeWithPosList, guiGraphics, new Vector2d(65,70)));
        if(!nodeWithPosList.isEmpty()) {
            // 線を描画
            for (nodeWithPos nodeWithPos : nodeWithPosList) {
                Vector2d pos = nodeWithPos.pos();
                node.draw(guiGraphics, (int)pos.x()-3, (int)pos.y()-3);
                for (String key : nodeWithPos.node().connects()) {
                    Vector2d pairPos = nodeWithPosList.stream()
                        .filter(withPos -> withPos.node().key().equals(key)).map(MagicCircleAssemblyCategory.nodeWithPos::pos).findFirst().orElse(new Vector2d(-1,-1));
                    if(pos.angle(pairPos) >= 0){
                        drawLine(guiGraphics, pos, pairPos);
                    }
                }
            }
            // ノードを描画
            for (nodeWithPos nodeWithPos : nodeWithPosList) {
                Vector2d pos = nodeWithPos.pos();
                node.draw(guiGraphics, (int)pos.x()-3, (int)pos.y()-3);
            }
        }
    }

    private Set<nodeWithPos> drawCircle(List<Node> recipeNodes, List<Circle> recipeCircles, List<drawCircleData> drawCircleList, Set<nodeWithPos> checkedWithPosList, GuiGraphics guiGraphics, Vector2d center) {
        Set<nodeWithPos> nodeWithPosList = new HashSet<>();
        PoseStack poseStack = guiGraphics.pose();

        for (drawCircleData drawCircleData : drawCircleList) {
            double radius = drawCircleData.radius();
            List<String> containsKey = drawCircleData.keys();
            // 円周長でドット数を決める
            float length = (float) ((radius - 0.5) * Math.PI);
            int circleDiv = (int) (length/DOT_SCALE);
            float dotScale = length / circleDiv + 0.5f;
            poseStack.pushPose();
            poseStack.translate(center.x() + 0.5, center.y() + 0.5, 0);
            for (double dotId = 0; dotId < circleDiv; dotId++) {
                poseStack.mulPose(new Quaternionf().rotateZ((float) ((360f/circleDiv) * Math.PI / 180)));
                poseStack.translate(-dotScale, (radius - 0.5), 0);
                poseStack.scale(dotScale, 1f, 1f);
                //int posX = (int)(center.x() + radius * Math.cos(dotId));
                //int posY = (int)(center.y() + radius * Math.sin(dotId));
                dot.draw(guiGraphics, 0, 0);
                poseStack.scale(1f/dotScale, 1f, 1f);
                poseStack.translate(dotScale, -(radius - 0.5), 0);
            }
            poseStack.popPose();
            // 円周上のノードを描画
            for (int nodeId = 0; nodeId < containsKey.size(); nodeId++) {
                double angle = ((float)nodeId/containsKey.size())*2*Math.PI;
                int posX = (int)(center.x() + (radius) * Math.cos(angle));
                int posY = (int)(center.y() + radius * Math.sin(angle));
                String key = containsKey.get(nodeId);
                Node posNode = recipeNodes.stream().filter(Node-> Node.key().equals(key)).findFirst().orElse(null);
                if(posNode!=null) {
                    if(checkedWithPosList.stream().filter(nodeWithPos -> nodeWithPos.node().key().equals(posNode.key())).toList().isEmpty()){
                        nodeWithPosList.add(new nodeWithPos(posNode,new Vector2d(posX, posY)));
                    }
                }
            }
        }

        Set<nodeWithPos> localList = new HashSet<>();
        for (nodeWithPos nodeWithPos : nodeWithPosList) {
            //Node posNode = nodeWithPos.node();
            String key = nodeWithPos.node().key();
            Vector2d pos = nodeWithPos.pos();
            // ノード上に円があれば描く
            List<drawCircleData> localCircleList = new ArrayList<>();
            for (Circle circle : recipeCircles.stream().filter(circle -> circle.center().equals(key)).toList()) {
                List<String> contains = circle.contains();
                Vector2d containsPos = nodeWithPosList.stream()
                    .filter(posList -> contains.contains(posList.node().key()))
                    .map(MagicCircleAssemblyCategory.nodeWithPos::pos).findFirst().orElse(pos);//TODO: 複数のノードを円周に含んでいる場合、ノードの親を移動して円周にすべてを収める
                double localRadius = pos.distance(containsPos);
                if(localRadius > 0.1){
                    localCircleList.add(new drawCircleData(contains, localRadius));
                }
            }
            localList.addAll(drawCircle(recipeNodes, recipeCircles, localCircleList, nodeWithPosList, guiGraphics, pos));
        }
        nodeWithPosList.addAll(localList);

        return nodeWithPosList;
    }

    private void drawLine(GuiGraphics guiGraphics, Vector2d start, Vector2d end) {
        PoseStack poseStack = guiGraphics.pose();
        float length = (float) start.distance(end) /2;
        float angle = (float) (Math.atan2(end.y() - start.y(), end.x() - start.x()));
        poseStack.pushPose();
        poseStack.translate(start.x() + 0.5, start.y() + 0.5, 0);
        poseStack.mulPose(new Quaternionf().rotateZ(angle));
        poseStack.translate(0, -1, 0);
        poseStack.scale(length, 1f, 1f);
        dot.draw(guiGraphics, 0, 0);
        poseStack.popPose();
    }

    @Override
    public RecipeType<MagicCircleAssemblyRecipe> getRecipeType() {
        return MAGIC_CIRCLE_ASSEMBLY_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe.heliopause.magic_circle_assembly");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MagicCircleAssemblyRecipe recipe, IFocusGroup focuses) {
        // 材料スロット
        //builder.addSlot(RecipeIngredientRole.INPUT, 80-13, 21-1).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 140, 2).addIngredients(recipe.getIngredients().get(0));
        // 結果スロット
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140,65).addItemStack(recipe.getResultItem(null));
    }
}
