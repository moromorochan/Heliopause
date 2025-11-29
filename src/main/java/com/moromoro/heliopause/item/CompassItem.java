package com.moromoro.heliopause.item;

import com.mojang.blaze3d.platform.Window;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.WrittenBoardBlock;
import com.moromoro.heliopause.EnumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.KeyMapRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// 錬成陣を開始する道具
public class CompassItem extends Item implements IhasHoverTexts,IhasHoverMenu {
    // 背景
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Heliopause.MODID, "textures/gui/item/circle_select/background.png");
    // アイコン
    //private static final ResourceLocation SYMBOL = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/symbol.png");
    //private static final ResourceLocation CIRCLE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/circle.png");
    //private static final ResourceLocation LINE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/line.png");
    //private static final ResourceLocation ERASE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/erase.png");

    private static final int TEX_WIDTH = 125;
    private static final int TEX_HEIGHT = 51;
    private static final String SELECT = "CustomModelData";
    //private static final String DRAW_PROGRESS = "drawing_progress";
    private int selectIndex = 0;  // 選択中の陣の種類
    private final int selectMax = 4;
    //private int drawingProgress = 0;
    private int drawingTotalTime = 100;

    public CompassItem(Properties properties) {
        super(properties);
        this.selectIndex = 0;
    }

    @Override
    public @NotNull List<Component> getBlockHoverTexts(ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult) {
        List<Component> tooltip = new ArrayList<>();
        Minecraft instance = Minecraft.getInstance();

        if(instance.player != null){
            // 陣の選択ツールチップ
            if(!KeyMapRegistry.CIRCLE_SELECT.isPressed()){
                //操作キーの翻訳を取得
                Component useKey = KeyMapRegistry.CIRCLE_SELECT.getKeyMapping().getTranslatedKeyMessage();
                tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.compass.tooltip.description1", useKey)).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.description3"));
            }else{
                return new ArrayList<>();
            }
            // 陣の書き込みツールチップ
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState blockState = clientLevel.getBlockState(pos);
                if (blockState.is(BlockRegistry.BLACKBOARD.get())) {
                    tooltip.clear();
                    // 既に操作中ならキャンセル
                    if (!instance.options.keyUse.isDown()) {
                        //操作キーの翻訳を取得
                        Component useKey = instance.options.keyUse.getTranslatedKeyMessage();
                        tooltip.add(Component.literal("[").append(useKey).append("] :"));
                        //tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.compass.tooltip.description1", useKey)).append("] :"));
                        tooltip.add(Component.translatable("item.heliopause.compass.tooltip.description2"));
                    }
                }
            }
        }
        return tooltip;
    }

    // 空中でクリックしたとき
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // シフトを押しているなら書くもの変更
        if (KeyMapRegistry.CIRCLE_SELECT.isPressed()) {
            if (!level.isClientSide) {
                changeSelect(stack);
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    //ブロックをクリックしたとき
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if(player == null){
            return super.useOn(context);
        }
        Level level = context.getLevel();
        //Direction direction = context.getClickedFace();
        ItemStack itemStack = context.getItemInHand();
        // シフトを押しているならスキップ
        if(KeyMapRegistry.CIRCLE_SELECT.isPressed()){
            this.use(level,player, context.getHand());
            return InteractionResult.CONSUME;
        }
        //ブロックを取得
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);

        if (itemStack.is(this)) {
            if (blockState.is(BlockRegistry.BLACKBOARD.get()) ||
                blockState.is(BlockRegistry.WRITTEN_BOARD.get())) {

                //player.startUsingItem(context.getHand());
                CompoundTag nbt = itemStack.getOrCreateTag();
                this.selectIndex = nbt.getInt(SELECT);
                switch (this.selectIndex) {
                    case 0:
                        drawSymbol(level, pos, WrittenBoardDrawType.CROSS_CIRCLE);
                        break;
                    case 1:
                        drawCircle(level, pos, blockState, context);
                        break;
                    case 2:
                        drawLine(level, pos, blockState, context);
                        break;
                    case 3:
                        eraseDrawn(level, pos, blockState);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }

        }
        return super.useOn(context);
    }

    private void drawSymbol(Level level, BlockPos pos, WrittenBoardDrawType drawType) {
        // 既に何か描かれているなら記号を入れる
        /*if(blockState.getBlock() instanceof WrittenBoardBlock){

        }*/
        BlockState newBlockState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState().setValue(WrittenBoardBlock.CIRCLE_TYPE, drawType);
        level.setBlock(pos, newBlockState, 3);
    }

    private void drawCircle(Level level, BlockPos pos, BlockState blockState, UseOnContext context) {
        // 一度目の選択なら中心位置を保存 二度目の選択なら範囲をチェック
        BlockPos firstPos = operateDoublePos(context, pos, blockState);
        // 範囲が有効なら描画
        if(firstPos != null){

            BlockEntity blockEntity = level.getBlockEntity(firstPos);
            if(blockEntity instanceof WrittenBoardBlockEntity boardEntity){
                double circleRadius = Math.sqrt(pos.distSqr(firstPos));
                if(boardEntity.setCircleRadius(circleRadius, false)){
                    drawSymbol(level, firstPos, WrittenBoardDrawType.CROSS_CIRCLE);
                }
            }
        }
    }

    private void drawLine(Level level, BlockPos pos, BlockState blockState, UseOnContext context) {
        // 一度目の選択なら中心位置を保存 二度目の選択なら範囲をチェック
        BlockPos firstPos = operateDoublePos(context, pos, blockState);
        // 範囲が有効なら描画
        if(firstPos != null){

            BlockEntity blockEntity = level.getBlockEntity(firstPos);
            if(blockEntity instanceof WrittenBoardBlockEntity boardEntity){
                if(level.getBlockState(pos).is(BlockRegistry.BLACKBOARD.get())){
                    drawSymbol(level, pos, WrittenBoardDrawType.DOUBLE_CIRCLE);
                }
                boardEntity.setLinePairPos(pos, false);
                /*if(){

                }*/
            }
        }
    }

    private void eraseDrawn(Level level, BlockPos pos, BlockState blockState) {
        // エンティティ、かつ描かれているものがひとつなら
        if(blockState.getBlock() instanceof WrittenBoardBlock){
            // 消す
            level.setBlock(pos, BlockRegistry.BLACKBOARD.get().defaultBlockState(), 3);
        }

        // エンティティでないなら、親エンティティを参照できるか確認
            // 範囲に存在しうる描画を探す

            // 存在すれば消す
    }

    private void changeSelect(ItemStack stack){
        Minecraft instance = Minecraft.getInstance();
        if(instance.level == null || !instance.level.isClientSide){
            return;
        }
        CompoundTag nbt = stack.getOrCreateTag();
        this.selectIndex = nbt.getInt(SELECT) + 1;
        //++this.selectIndex;
        if(selectIndex >= selectMax){
            selectIndex -= selectMax;
        }
        nbt.putInt(SELECT,selectIndex);
    }

    // 二つの座標挙動
    private @Nullable BlockPos operateDoublePos(UseOnContext context, BlockPos pos, BlockState blockState){
        if(context.getPlayer() == null){
            return null;
        }
        ItemStack itemStack = context.getItemInHand();
        CompoundTag tag = itemStack.getOrCreateTag();
        //1回目
        if (!tag.contains("firstPosX")) {
            // 円・線は親ノードが必要
            if(blockState.is(BlockRegistry.WRITTEN_BOARD.get())){
                tag.putInt("firstPosX", pos.getX());
                tag.putInt("firstPosY", pos.getY());
                tag.putInt("firstPosZ", pos.getZ());
                itemStack.setTag(tag);
                context.getPlayer().setItemInHand(context.getHand(),itemStack);
            }
            return null;
        } else {
            //2回目
            int x1 = tag.getInt("firstPosX");
            int y1 = tag.getInt("firstPosY");
            int z1 = tag.getInt("firstPosZ");
            tag.remove("firstPosX");
            tag.remove("firstPosY");
            tag.remove("firstPosZ");
            if (tag.isEmpty()) {
                itemStack.setTag(null);
            }
            itemStack.setTag(tag);
            context.getPlayer().setItemInHand(context.getHand(),itemStack);
            return new BlockPos(x1, y1, z1);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int remainTicks) {
        super.onUseTick(level, entity, itemStack, remainTicks);
        if(entity instanceof Player player){
            if (itemStack.is(this)) {
                return;
            }
                /*Minecraft instance = Minecraft.getInstance();
                instance.mouseHandler.grabMouse();*/
        }
    }

    // 長押し終了
    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        super.onStopUsing(stack, entity, count);
        //Minecraft instance = Minecraft.getInstance();
        //instance.mouseHandler.grabMouse();
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return drawingTotalTime;
    }

    @Override
    public void renderHoverMenu(RenderGuiOverlayEvent event, ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult) {
        //new CircleSelectScreen(itemStack, hitResult);
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        // プレイヤーがaltを押しているか確認
        if(player == null || !KeyMapRegistry.CIRCLE_SELECT.isPressed()){
            return;
        }
        /*
        // 対象のブロックを取得
       if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState blockState = clientLevel.getBlockState(pos);
            if(!blockState.is(BlockRegistry.BLACKBOARD.get())){
                return;
            }
            // マウスを解放
            //instance.mouseHandler.releaseMouse();

        }*/
        instance.options.keyInventory.setDown(true);

        Window window = event.getWindow();
        int x = (window.getGuiScaledWidth()-TEX_WIDTH)/2;
        int y = (window.getGuiScaledHeight()-TEX_HEIGHT)/2 + 45;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.blit(BACKGROUND,x,y,0,0f,0f,TEX_WIDTH,TEX_HEIGHT,128,128);
        graphics.drawCenteredString(
            instance.font,Component.translatable("gui.heliopause.compass.description1",instance.options.keyUse.getTranslatedKeyMessage()),
            instance.getWindow().getGuiScaledWidth()/2,
            instance.getWindow().getGuiScaledHeight()/2+10,
            0xFFFFFF
            );
        for (int slot = 0; slot < selectMax; slot++) {
            ItemStack slotStack = new ItemStack(this.asItem());
            CompoundTag nbt = new CompoundTag();
            nbt.putInt(SELECT,slot);
            slotStack.setTag(nbt);
            graphics.blit(BACKGROUND,x + 5 + slot * 30,y + 3, 0, 0, TEX_HEIGHT, 25,25,128,128);
            graphics.renderItem(slotStack,x + 10 + slot * 30,y + 8);
        }
        graphics.blit(BACKGROUND,x + 5 + this.selectIndex * 30,y + 3, 0, 25, TEX_HEIGHT, 25,25,128,128);

        Component description = switch (this.selectIndex) {
            case 0 -> Component.translatable("gui.heliopause.compass.description2");
            case 1 -> Component.translatable("gui.heliopause.compass.description3");
            case 2 -> Component.translatable("gui.heliopause.compass.description4");
            case 3 -> Component.translatable("gui.heliopause.compass.description5");
            default -> Component.literal("");
        };
        graphics.drawCenteredString(
            instance.font,description,
            instance.getWindow().getGuiScaledWidth()/2,
            instance.getWindow().getGuiScaledHeight()/2+56,
            0xFFFFFF
        );
    }
}
