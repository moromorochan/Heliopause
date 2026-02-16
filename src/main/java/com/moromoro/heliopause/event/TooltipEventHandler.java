package com.moromoro.heliopause.event;

import com.moromoro.heliopause.entity.LensBarrelEntity;
import com.moromoro.heliopause.instance.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class TooltipEventHandler {
    // オーバーレイレンダリングに追加する
    @SubscribeEvent
    public void onRenderGuiOverlayPost(RenderGuiOverlayEvent event) {
        Minecraft instance = Minecraft.getInstance();
        Player player = instance.player;
        if (player != null ) {
            HitResult hitResult = instance.hitResult;
            if (hitResult != null ) {
                //メインハンドアイテムを取得
                ItemStack mainHandItem = player.getMainHandItem();
                //オフハンドアイテムを取得
                ItemStack offHandItem = player.getOffhandItem();
                //両方とも空の場合スキップ
                /*if(mainHandItem.isEmpty() && offHandItem.isEmpty()){
                    return;
                }*/
                // アイテム描画成功判定
                boolean itemDrawn = false;

                //メインハンドをチェック
                if(!mainHandItem.isEmpty()){
                    itemDrawn |= drawTextTooltip(event, mainHandItem, hitResult);
                    itemDrawn |= drawGraphicTooltip(event, mainHandItem, hitResult);
                }
                //オフハンドをチェック
                else if (!offHandItem.isEmpty()) {
                    itemDrawn |= drawTextTooltip(event, mainHandItem, hitResult);
                    itemDrawn |= drawGraphicTooltip(event, mainHandItem, hitResult);
                }

                if(itemDrawn || hitResult.getType().equals(HitResult.Type.MISS)){
                    return;
                }

                ClientLevel level = instance.level;
                if (level != null ) {


                    // ブロック描画成功判定
                    boolean blockDrawn = false;

                    // ブロックをチェック
                    if(hitResult.getType().equals(HitResult.Type.BLOCK)){
                        Vec3 angle = player.getLookAngle().normalize().scale(0.5);
                        BlockPos pos = BlockPos.containing(hitResult.getLocation().add(angle));
                        Block block = level.getBlockState(pos).getBlock();
                        if(block instanceof IhasHoverDrawBlock iBlock){
                            blockDrawn |= iBlock.renderHoverGraphic(event, instance.level, pos);
                        }

                    }
                    if(blockDrawn){
                        return;
                    }

                    // エンティティをチェック
                    if(hitResult.getType().equals(HitResult.Type.ENTITY)){
                        List<Entity> entities =
                            level.getEntitiesOfClass(Entity.class, new AABB(hitResult.getLocation(),hitResult.getLocation()).inflate(0.3), e -> true);
                        for (Entity entity : entities) {
                            if(entity instanceof IhasHoverDrawEntity iEntity){
                                iEntity.renderHoverGraphicWithEntity(event, level);
                            }
                        }
                    }
                }
            }
        }
    }

    /*// 移動操作処理に追加する
    @SubscribeEvent
    public void onInputUpdate(InputEvent.MouseScrollingEvent event){
        Minecraft instance = Minecraft.getInstance();
        Player player = instance.player;
        if (player instanceof LocalPlayer localPlayer) {
            HitResult hitResult = instance.hitResult;
            if (hitResult != null ) {
                //メインハンドアイテムを取得
                ItemStack mainHandItem = player.getMainHandItem();
                //オフハンドアイテムを取得
                ItemStack offHandItem = player.getOffhandItem();
                //両方とも空の場合スキップ
                if(mainHandItem.isEmpty() && offHandItem.isEmpty()){
                    return;
                }
                //メインハンドをチェック
                if(!mainHandItem.isEmpty()){
                    stopMoveInput(event,localPlayer,mainHandItem,hitResult);
                }
                //オフハンドをチェック
                else if (!offHandItem.isEmpty()) {
                    stopMoveInput(event,localPlayer,offHandItem,hitResult);
                }
            }
        }
    }

    private void stopMoveInput(InputEvent.MouseScrollingEvent event, LocalPlayer player, ItemStack itemStack, HitResult hitResult){
        // キーが押されているとき
        if(KeyMapRegistry.CIRCLE_SELECT.isPressed()){
            // アイテムを確認
            if(itemStack.getItem() instanceof IhasHoverDraw iItem){
                Minecraft instance = Minecraft.getInstance();
                // プレイヤーを確認
                if(instance.player instanceof LocalPlayer){
                    // ホイールのデータをアイテムへ
                    iItem.setWheelInput(event.getScrollDelta());
                    // ホイールをリセット
                    event = new InputEvent.MouseScrollingEvent(0,event.isLeftDown(),event.isMiddleDown(),event.isRightDown(),event.getMouseX(),event.getMouseY());
                }
            }
        }
    }*/

    private boolean drawTextTooltip(RenderGuiOverlayEvent event, ItemStack itemStack, HitResult hitResult){
        // アイテムのツールチップをオーバーレイに表示
        if(itemStack.getItem() instanceof IhasHoverTexts iItem){
            Minecraft instance = Minecraft.getInstance();
            List<Component> tooltipLists = (iItem.getBlockHoverTexts(instance.level, itemStack, hitResult));

            for (int i = 0; i < tooltipLists.size(); i++) {
                event.getGuiGraphics().drawString(
                    instance.font, tooltipLists.get(i),
                    instance.getWindow().getGuiScaledWidth()/2 +10,
                    instance.getWindow().getGuiScaledHeight()/2 -4 + i*10,
                    0xFFFFFF,true);
            }
            return true;
        }
        return false;
    }

    // アイテムのホバーGUIをオーバーレイに表示
    private boolean drawGraphicTooltip(RenderGuiOverlayEvent event, ItemStack itemStack, HitResult hitResult){
        if(itemStack.getItem() instanceof IhasHoverDraw iItem){
            Minecraft instance = Minecraft.getInstance();
            // アイテムの関数にeventを渡してレンダラを回す
            return iItem.renderHoverGraphic(event, instance.level, itemStack, hitResult);
        }
        return false;
    }

    // ワールド座標系にオーバーレイを描画
    @SubscribeEvent
    public boolean onRenderLevelStage(RenderLevelStageEvent event){
        Minecraft instance = Minecraft.getInstance();
        if(instance.player==null || instance.level == null){
            return false;
        }
        //プレイヤーが手に持っているアイテムを確認
        ItemStack itemStack = instance.player.getMainHandItem();
        if(itemStack.getItem() instanceof IhasLevelDraw iItem){
            return iItem.renderLevelGraphic(event, instance.level, itemStack);
        }
        return false;
    }
}
