package com.moromoro.heliopause.event;

import com.moromoro.heliopause.item.IhasHoverMenu;
import com.moromoro.heliopause.item.IhasHoverTexts;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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
                if(mainHandItem.isEmpty() && offHandItem.isEmpty()){
                    return;
                }
                //メインハンドをチェック
                if(!mainHandItem.isEmpty()){
                    drawTextTooltip(event, mainHandItem, hitResult);
                    drawGraphicTooltip(event, mainHandItem, hitResult);
                }
                //オフハンドをチェック
                else if (!offHandItem.isEmpty()) {
                    drawTextTooltip(event, mainHandItem, hitResult);
                    drawGraphicTooltip(event, mainHandItem, hitResult);
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
            if(itemStack.getItem() instanceof IhasHoverMenu iItem){
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

    //アイテムのツールチップをオーバーレイに表示
    private void drawTextTooltip(RenderGuiOverlayEvent event, ItemStack itemStack, HitResult hitResult){
        if(itemStack.getItem() instanceof IhasHoverTexts iItem){
            Minecraft instance = Minecraft.getInstance();
            List<Component> tooltipLists = (iItem.getBlockHoverTexts(instance.level, itemStack, hitResult));

            for (int i = 0; i < tooltipLists.size(); i++) {
                //boolean isShadowed = (i == 0);  // 一行目だけ影付きにする
                event.getGuiGraphics().drawString(
                    instance.font, tooltipLists.get(i),
                    instance.getWindow().getGuiScaledWidth()/2 +10,
                    instance.getWindow().getGuiScaledHeight()/2 -4 + i*10,
                    0xFFFFFF,true);
            }
        }
    }

    // アイテムのホバーGUIをオーバーレイに表示
    private void drawGraphicTooltip(RenderGuiOverlayEvent event, ItemStack itemStack, HitResult hitResult){
        if(itemStack.getItem() instanceof IhasHoverMenu iItem){
            Minecraft instance = Minecraft.getInstance();
            // アイテムの関数にeventを渡してレンダラを回す
            iItem.renderHoverMenu(event, instance.level, itemStack, hitResult);
        }
    }
}
