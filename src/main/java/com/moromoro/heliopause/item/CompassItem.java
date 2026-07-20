package com.moromoro.heliopause.item;

import com.mojang.blaze3d.platform.Window;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.AbstractWrittenBoardBlock;
import com.moromoro.heliopause.block.BlackBoardBlock;
import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.implementable.IHasHoverDraw;
import com.moromoro.heliopause.implementable.IHasHoverTexts;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// 錬成陣を開始する道具
public class CompassItem extends Item implements IHasHoverTexts, IHasHoverDraw {
    // 背景
    private static final ResourceLocation BACKGROUND = new ResourceLocation(Heliopause.MODID, "textures/gui/compass_tool_select.png");
    // アイコン
    //private static final ResourceLocation SYMBOL = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/symbol.png");
    //private static final ResourceLocation CIRCLE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/circle.png");
    //private static final ResourceLocation LINE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/line.png");
    //private static final ResourceLocation ERASE = new ResourceLocation(Heliopause.MODID,"textures/gui/item/circle_select/erase.png");

    private static final int TEX_WIDTH = 125;
    private static final int TEX_HEIGHT = 51;
    private static final String SELECT = "CustomModelData";
    //private static final String DRAW_PROGRESS = "drawing_progress";
    private int selectIndex;  // 選択中の陣の種類
    private final int selectMax = 4;
    private static final int SYMBOL_INDEX = 0;
    private static final int CIRCLE_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int ERASE_INDEX = 3;
    //private int drawingProgress = 0;
    //private int drawingTotalTime = 100;

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
                tooltip.add(Component.literal("[").append(Component.translatable("item.heliopause.compass.tooltip.hold_key", useKey)).append("] :"));
                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.open_select"));
            }else{
                return tooltip;
            }

            // 陣の書き込みツールチップ
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // 選択ブロック情報取得
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState blockState = clientLevel.getBlockState(pos);

                // 操作キーの翻訳を取得
                Component useKey = instance.options.keyUse.getTranslatedKeyMessage();

                // ツールチップ用情報取得
                CompoundTag nbt = itemStack.getTag();
                boolean hasFirstPos = (nbt != null && nbt.contains("firstPosX"));

                // ノードの場合
                if(blockState.getBlock() instanceof AbstractWrittenBoardBlock || hasFirstPos){
                    // ツールがなら紋なら操作なし
                    if(selectIndex == SYMBOL_INDEX){
                        return tooltip;
                    }
                    tooltip.clear();

                    // 操作キー
                    tooltip.add(Component.literal("[").append(useKey).append("] :"));
                    // 操作情報
                    switch (selectIndex){
                        case CIRCLE_INDEX -> {
                            if (!hasFirstPos) {
                                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.select_center"));
                            } else {
                                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.draw_circle"));
                            }
                        }
                        case LINE_INDEX -> {
                            if (!hasFirstPos) {
                                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.select_begin"));
                            } else {
                                tooltip.add(Component.translatable("item.heliopause.compass.tooltip.draw_line"));
                            }
                        }
                        case ERASE_INDEX -> tooltip.add(Component.translatable("item.heliopause.compass.tooltip.erase_drawn"));
                    }
                    return tooltip;
                }

                // 黒板の場合
                if (blockState.is(BlockRegistry.BLACKBOARD.get())) {
                    // ツールが円・線なら操作なし
                    if(selectIndex == CIRCLE_INDEX || selectIndex == LINE_INDEX) {
                        return tooltip;
                    }
                    tooltip.clear();
                    // 操作キー
                    tooltip.add(Component.literal("[").append(useKey).append("] :"));
                    // 操作情報
                    switch (selectIndex){
                        case SYMBOL_INDEX -> tooltip.add(Component.translatable("item.heliopause.compass.tooltip.draw_symbol"));
                        case ERASE_INDEX -> tooltip.add(Component.translatable("item.heliopause.compass.tooltip.erase_drawn"));
                    }
                    return tooltip;
                }
            }
        }
        return tooltip;
    }

    // 空中でクリックしたとき
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        // 変更キーを押しているなら書くもの変更
        if (KeyMapRegistry.CIRCLE_SELECT.isActivated(level, player, hand)) {
            return changeSelect(level, itemStack);
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
        // 選択画面ならスキップ
        if(KeyMapRegistry.CIRCLE_SELECT.isActivated(level, player, context.getHand())){
            changeSelect(level, itemStack);
            return InteractionResult.CONSUME;
        }
        //ブロックを取得
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);

        if (itemStack.is(this)) {
            if (blockState.is(BlockRegistry.BLACKBOARD.get()) ||
                level.getBlockEntity(pos) instanceof AbstractWrittenBoardBlockEntity) {

                //player.startUsingItem(context.getHand());
                CompoundTag nbt = itemStack.getOrCreateTag();
                this.selectIndex = nbt.getInt(SELECT);
                switch (this.selectIndex) {
                    case SYMBOL_INDEX:
                        drawSymbol(level, pos, null);
                        break;
                    case CIRCLE_INDEX:
                        drawCircle(level, pos, blockState, context);
                        break;
                    case LINE_INDEX:
                        drawLine(level, pos, blockState, context);
                        break;
                    case ERASE_INDEX:
                        eraseDrawn(level, pos, blockState, context);
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            }

        }
        return super.useOn(context);
    }

    private void drawSymbol(Level level, BlockPos pos, @Nullable WrittenBoardDrawType drawType) {
        BlockState oldBlockState = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        // シンボルがないならCROSS_CIRCLE
        if(oldBlockState.is(BlockRegistry.BLACKBOARD.get())){
            BlockState newBlockState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState()
                .setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE, Objects.requireNonNullElse(drawType, WrittenBoardDrawType.CROSS_CIRCLE));
            level.setBlock(pos, newBlockState, 3);
        }
        // シンボルがあるなら順に置き換え
        else if(oldBlockState.is(BlockRegistry.WRITTEN_BOARD.get())){
            // ルートノードではない場合は置き換えない
            if (!(blockEntity instanceof AbstractWrittenBoardBlockEntity boardBlockEntity) || !boardBlockEntity.isRoot(level)) {
                return;
            }
            BlockState newBlockState = BlockRegistry.WRITTEN_BOARD.get().defaultBlockState();
            if (drawType != null) {
                newBlockState = newBlockState.setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE, drawType);
            } else {
                WrittenBoardDrawType[] circleTypes = WrittenBoardDrawType.values();
                int poseState = oldBlockState.getValue(AbstractWrittenBoardBlock.CIRCLE_TYPE).ordinal();
                WrittenBoardDrawType circleType = circleTypes[(poseState + circleTypes.length) % circleTypes.length];
                // 子ノード・大きいノードはスキップ
                while(WrittenBoardDrawType.isChildNode(circleType) || WrittenBoardDrawType.isLargeNode(circleType)) {
                    poseState++;
                    circleType = circleTypes[(poseState + circleTypes.length) % circleTypes.length];
                }

                newBlockState = newBlockState.setValue(AbstractWrittenBoardBlock.CIRCLE_TYPE, circleType);
            }
            level.setBlock(pos, newBlockState, 3);
        }
    }

    private void drawCircle(Level level, BlockPos pos, BlockState blockState, UseOnContext context) {
        // 一度目の選択なら中心位置を保存 二度目の選択なら範囲をチェック
        BlockPos firstPos = operateDoublePos(context, pos, blockState);
        // 範囲が有効なら描画
        if(firstPos != null){

            BlockEntity blockEntity = level.getBlockEntity(firstPos);
            if(blockEntity instanceof AbstractWrittenBoardBlockEntity boardEntity){
                double circleRadius = pos.getCenter().distanceTo(firstPos.getCenter());
                boardEntity.drawCircle(circleRadius, false);
                /*if(){
                    drawSymbol(level, firstPos, WrittenBoardDrawType.CROSS_CIRCLE);
                }*/
            }
        }
    }

    private void drawLine(Level level, BlockPos pos, BlockState blockState, UseOnContext context) {
        // 一度目の選択なら中心位置を保存 二度目の選択なら範囲をチェック
        BlockPos firstPos = operateDoublePos(context, pos, blockState);
        // 範囲が有効なら描画
        if(firstPos != null){

            BlockEntity blockEntity = level.getBlockEntity(firstPos);
            if(blockEntity instanceof AbstractWrittenBoardBlockEntity boardEntity){
                /*if(level.getBlockState(pos).is(BlockRegistry.BLACKBOARD.get())){
                    drawSymbol(level, pos, WrittenBoardDrawType.CHILD_NODE);
                }*/
                boardEntity.drawLine(pos, false);
                /*if(){

                }*/
            }/*else{
                //クリック位置からPREVIEW_LIMIT_SIZEまで走査
                for(int localX = -PREVIEW_LIMIT_SIZE; localX < PREVIEW_LIMIT_SIZE; localX++){
                    for(int localZ = -PREVIEW_LIMIT_SIZE; localZ < PREVIEW_LIMIT_SIZE; localZ++){
                        if(level.getBlockEntity(firstPos.offset(localX,0,localZ)) instanceof AbstractWrittenBoardBlockEntity entity){
                            // ヒット判定
                            entity.drawLineFromPos(context.getClickedPos(), AbstractWrittenBoardBlockEntity.CLICK_SIZE, false);
                        }
                    }
                }
            }*/
        }
    }

    private void eraseDrawn(Level level, BlockPos blockPos, BlockState blockState, UseOnContext context) {
        // ブロックエンティティがあり、クリック位置が判定サイズ内なら
        if(level.getBlockEntity(blockPos) instanceof AbstractWrittenBoardBlockEntity entity
            && AbstractWrittenBoardBlock.checkPosInNode(blockState, blockPos, context.getClickLocation())){
            // ノードを消す
            entity.eraseNode(1);
        }
        // 選択がノードでないなら、親ブロックエンティティを参照できるか確認
        else{
            // 候補を取得
            for (AbstractWrittenBoardBlockEntity node : BlackBoardBlock.getNodeList(level, blockPos)) {
                // ヒット判定
                node.eraseFromPos(context.getClickLocation(), AbstractWrittenBoardBlockEntity.CLICK_SIZE);
            }
        }
    }

    private InteractionResultHolder<ItemStack> changeSelect(Level level,ItemStack stack){
        /*Minecraft instance = Minecraft.getInstance();
        if(instance.level == null || instance.level.isClientSide){
            return;
        }*/
        if (!level.isClientSide) {
            CompoundTag nbt = stack.getOrCreateTag();
            this.selectIndex = nbt.getInt(SELECT) + 1;
            //++this.selectIndex;
            if(selectIndex >= selectMax){
                selectIndex -= selectMax;
            }
            nbt.putInt(SELECT,selectIndex);
            stack.setTag(nbt);
        }
        
        // 選択座標をリセット
        removePosTag(stack.getOrCreateTag(), stack);
        return InteractionResultHolder.consume(stack);
        
    }

    public int getSelectIndex(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        return nbt.getInt(SELECT);
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
                //itemStack.setTag(block);
                //context.getPlayer().setItemInHand(context.getHand(),itemStack);
            }
            return null;
        } else {
            //2回目
            BlockPos returnPos = getPosFromTag(tag);
            removePosTag(tag, itemStack);

            // 高さが違うなら除外
            if(returnPos == null || returnPos.getY() != pos.getY()){
                return null;
            }
            return returnPos;

            //itemStack.setTag(block);
            //context.getPlayer().setItemInHand(context.getHand(),itemStack);

        }
    }

    @Nullable
    public BlockPos getPosFromTag(@Nullable CompoundTag tag) {
        if(tag == null){
            return null;
        }
        if(tag.contains("firstPosX")) {
            int x1 = tag.getInt("firstPosX");
            int y1 = tag.getInt("firstPosY");
            int z1 = tag.getInt("firstPosZ");

            return new BlockPos(x1, y1, z1);
        }
        return null;
    }

    private void removePosTag(CompoundTag tag, ItemStack itemStack) {
        if(tag.contains("firstPosX")){
            tag.remove("firstPosX");
            tag.remove("firstPosY");
            tag.remove("firstPosZ");
            if (tag.isEmpty()) {
                itemStack.setTag(null);
            }
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        // プレイヤー以外は処理しない
        if (!(entity instanceof Player)) {
            return;
        }

        // アイテムが選択状態でなければリセット
        if (!selected) {
            removePosTag(itemStack.getOrCreateTag(), itemStack);
        }
    }

    /*@Override
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
    }*/

    // 選択メニュー表示
    @Override
    public boolean renderHoverGraphic(RenderGuiEvent event, ClientLevel clientLevel, ItemStack itemStack, HitResult hitResult) {
        //new CircleSelectScreen(itemStack, hitResult);
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        // プレイヤーがaltを押しているか確認
        if(player == null || !KeyMapRegistry.CIRCLE_SELECT.isPressed()){
            return false;
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

        if(itemStack.getItem() instanceof CompassItem compassItem){
        instance.options.keyInventory.setDown(true);

        Window window = event.getWindow();
        int x = (window.getGuiScaledWidth()-TEX_WIDTH)/2;
        int y = (window.getGuiScaledHeight()-TEX_HEIGHT)/2 + 45;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.blit(BACKGROUND,x,y,0,0f,0f,TEX_WIDTH,TEX_HEIGHT,128,128);
        graphics.drawCenteredString(
            instance.font,Component.translatable("item.heliopause.compass.tooltip.select_tool",instance.options.keyUse.getTranslatedKeyMessage()),
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

        int localSelectIndex = compassItem.getSelectIndex(itemStack);

        graphics.blit(BACKGROUND,x + 5 + localSelectIndex * 30,y + 3, 0, 25, TEX_HEIGHT, 25,25,128,128);

        Component description = switch (localSelectIndex) {
            case 0 -> Component.translatable("item.heliopause.compass.tooltip.draw_symbol");
            case 1 -> Component.translatable("item.heliopause.compass.tooltip.draw_circle");
            case 2 -> Component.translatable("item.heliopause.compass.tooltip.draw_line");
            case 3 -> Component.translatable("item.heliopause.compass.tooltip.erase_drawn");
            default -> Component.literal("");
        };
        graphics.drawCenteredString(
            instance.font,description,
            instance.getWindow().getGuiScaledWidth()/2,
            instance.getWindow().getGuiScaledHeight()/2+56,
            0xFFFFFF
        );
        }
        return true;
    }

    // 書きかけのプレビュー表示
    /*@Override
    public void renderLevelGraphic(RenderLevelStageEvent event, ClientLevel level, ItemStack itemStack){
        // cutoutのタイミング
        if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }
        CompoundTag block = itemStack.getTag();
        if (block == null || !block.contains("firstPosX")) {
            return;
        }
        // 選択済みのブロック位置を取得
        BlockPos firstPos = getPosFromTag(block);
        if(firstPos == null){
            return;
        }
        BlockEntity firstPosEntity = level.getBlockEntity(firstPos);
        if(!(firstPosEntity instanceof AbstractWrittenBoardBlockEntity nodeEntity)){
            return;
        }

        Minecraft instance = Minecraft.getInstance();
        // カメラエンティティを取得
        Entity cameraEntity = instance.getCameraEntity();
        if (!(cameraEntity instanceof Player player)) {
            return;
        }

        // 視点のブロック位置を取得
        Vec3 pointPos = WrittenBoardRenderer.getPointPos(firstPos, player, event.getPartialTick());
        if(pointPos == null){
            return;
        }
        BlockPos secondPos = BlockPos.containing(pointPos).below();
        // 高さが違うなら描画しない
        if(firstPos.getY() != secondPos.getY()){
            return;
        }

        // 描画定数
        int combinedLight = 0xF000F0;
        int combinedOverlay = 0;
        double length = firstPos.getCenter().distanceTo(secondPos.getCenter());
        BlockRenderDispatcher blockRenderer = instance.getBlockRenderer();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = instance.renderBuffers().bufferSource();
        VertexConsumer cutoutBuffer = bufferSource.getBuffer(RenderType.cutout());
        // 描画の種類取得
        int selectIndex = getSelectIndex(itemStack);
        switch (selectIndex) {
            case 1 -> {
                float[] renderColor = new float[]{0.8f,0.8f,0.8f};
                if(!nodeEntity.drawCircle(length, true)){
                    renderColor = new float[]{0.2f,0.2f,1f};
                }
                WrittenBoardRenderer.renderCircle(poseStack, cutoutBuffer, blockRenderer, combinedLight, combinedOverlay, renderColor, instance, firstPos, length);
            }
            case 2 -> {
                float[] renderColor = new float[]{0.8f,0.8f,0.8f};
                if(!nodeEntity.drawLine(secondPos, true)){
                    renderColor = new float[]{0.2f,0.2f,1f};
                }
                // 角度を計算
                float angle = Math.atan2(firstPos.getZ() - secondPos.getZ(), secondPos.getX() - firstPos.getX());
                WrittenBoardRenderer.renderLine(poseStack, cutoutBuffer, blockRenderer, combinedLight, combinedOverlay, renderColor, instance, firstPos, secondPos, angle, DECOR_MODELS.get("line_dotted"), null);
            }
            default -> {
                return;
            }
        }
    }*/
}
