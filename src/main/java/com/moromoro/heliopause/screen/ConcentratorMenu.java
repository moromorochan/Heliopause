package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class ConcentratorMenu extends AbstractContainerMenu {

    public final ConcentratorBlockEntity blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerData data;
    private int tabId = 0;
    public static final int UNIDENTIFIED_COORDINATE = 511;
    public static final int COORDINATE_BUTTON_INDEX = 511;

    private static final int RESULT_SLOT_INDEX = 0;

    private static final int invOffset = 126;
    private static final int hotBarOffset = 184;

    public ConcentratorMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()),
            new SimpleContainerData(ConcentratorBlockEntity.DATA_ACCESS_LENGTH));
    }

    public ConcentratorMenu(int containerId, Inventory playerInv, BlockEntity entity, ContainerData data) {
        super(MenuTypeRegistry.CONCENTRATOR_MENU.get(), containerId);

        blockEntity = (ConcentratorBlockEntity) entity;
        this.player = playerInv.player;
        this.level = player.level();
        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
                // バケツ入力x3と出力
                this.addSlot(new SlotItemHandler(iItemHandler, 0, 103, 102){
                    //プレイヤーがアイテムを置けないようにする
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return false;
                    }
                });

                this.addSlot(new SlotItemHandler(iItemHandler, 1, 20, 102));
            this.addSlot(new SlotItemHandler(iItemHandler, 2, 48, 102));
            this.addSlot(new SlotItemHandler(iItemHandler, 3, 76, 102));
        });

        //バニラのスロットを表示
        addPlayerInventory(playerInv);//index = 4~30
        addPlayerHotBar(playerInv);//index = 31~39

        addDataSlots(data);
    }

    @Override
    //シフトクリックでの移動に対応させる
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotId) {
        final int invStart = 4;
        final int hotStart = 31;
        final int hotEnd = 39;
        //スロットの中身を格納するアイテムスタックを宣言
        ItemStack itemstack = ItemStack.EMPTY;
        //スロットを取得
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            //中身を取得してコピーを作成
            ItemStack tempItemStack = slot.getItem();
            itemstack = tempItemStack.copy();

            // スロットがホットバーのみのときなら
            if(tabId != 1){
                if (!this.moveItemStackTo(tempItemStack, hotStart, hotEnd, false)) {
                    return ItemStack.EMPTY;
                }
                return itemstack;
            }

            //操作スロットが結果スロットなら
            if (slotId == RESULT_SLOT_INDEX) {
                tempItemStack.getItem().onCraftedBy(tempItemStack, level, player);
                //インベントリかホットバーに入れようとする
                if (!this.moveItemStackTo(tempItemStack, invStart, hotEnd, true)) {
                    //失敗したらスキップ
                    return ItemStack.EMPTY;
                }
            }
            //操作スロットがプレイヤーのスロットなら
            else if (slotId >= invStart && slotId < hotEnd) {
                //材料スロットに入れようとする
                boolean moveToInsert = false;
                for (int insertSlotId = 1; insertSlotId < 4; insertSlotId++) {
                    moveToInsert |= this.moveItemStackTo(tempItemStack, insertSlotId, invStart, false);
                }
                if (!moveToInsert) {
                    //失敗した場合
                    //操作スロットがインベントリスロットなら
                    if (slotId < hotStart) {
                        //ホットバーに入れようとする
                        if (!this.moveItemStackTo(tempItemStack, hotStart, hotEnd, false)) {
                            //失敗したらスキップ
                            return ItemStack.EMPTY;
                        }
                    }
                    //操作スロットがホットバーならインベントリに入れようとする
                    else if (!this.moveItemStackTo(tempItemStack, invStart, hotStart, false)) {
                        //失敗したらスキップ
                        return ItemStack.EMPTY;
                    }
                }
            }
            //操作スロットがクラフトスロットでない(ブロックのスロットあるいはその他の追加スロット)なら、インベントリかホットバーに入れようとする
            else if (!this.moveItemStackTo(tempItemStack, invStart, hotEnd, false)) {
                //失敗したらスキップ
                return ItemStack.EMPTY;
            }

            if (tempItemStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (tempItemStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, tempItemStack);
            if (slotId == 0) {
                player.drop(tempItemStack, false);
            }
        }

        return itemstack;
    }

    // 分割したlongを戻す
    private static long combineIntegersToLong(int low, int high) {
        return ((long) high << 32) | ((long) low & 0xFFFFFFFFL);
    }

    public @Nullable Vector2i getTrackingStarCoordinate() {
        return decodeStarCoordinate(data.get(ConcentratorBlockEntity.COORDINATE));
    }

    public static int encodeStarCoordinate(@Nullable Vector2i coordinate){
        int x = UNIDENTIFIED_COORDINATE;
        if (coordinate != null) {
            if (coordinate.x() < 0 || coordinate.x() >= 360) {
                return -1;
            }
            x = coordinate.x();
        }
        int y = UNIDENTIFIED_COORDINATE;
        if (coordinate != null) {
            if (coordinate.y() < -90 || coordinate.y() > 90) {
                return -1;
            }
            y = coordinate.y() + 90;
        }
        // 座標エンコード
        return (x & 0xFFFF) | ((y & 0xFFFF) << 16);

        //this.clicked(COORDINATE_BUTTON_INDEX, encodedCoordinate, ClickType.PICKUP, player);
    }

    public static @Nullable Vector2i decodeStarCoordinate(int encodedCoordinate){
        // 座標デコード
        int x = encodedCoordinate & 0xFFFF;
        int y = (encodedCoordinate >>> 16) & 0xFFFF;
        if(x == UNIDENTIFIED_COORDINATE && y == UNIDENTIFIED_COORDINATE){
            return null;
        }
        return new Vector2i(x, y - 90);
    }

    /*@Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if(slotId == COORDINATE_BUTTON_INDEX){

        }
        super.clicked(slotId, dragType, clickType, player);
    }*/

    @Override
    public boolean clickMenuButton(@NotNull Player player, int slotId) {
        // マイナス領域は座標送信用
        if(slotId < 0){
            // サーバー側でのクリック処理
            if (stillValid(player) && player instanceof ServerPlayer serverPlayer) {// ブロックエンティティに反映
                Level serverPlayerLevel = serverPlayer.level();
                if (serverPlayerLevel.getBlockEntity(blockEntity.getBlockPos()) instanceof ConcentratorBlockEntity entity) {

                    entity.setTargetStarCoordinate(decodeStarCoordinate(-slotId));
                    entity.setChanged();
                    serverPlayerLevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
                    return true;
                }
            }
        }
        if(slotId >=0 && slotId <= ConcentratorScreen.TAB_COUNT){
            tabId = slotId;
        }
        return super.clickMenuButton(player, slotId);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
            player, BlockRegistry.CONCENTRATOR.get());
    }

    //GUIにプレイヤーのインベントリを表示
    private void addPlayerInventory(Inventory playerInv){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv,j+i*9+9,36+j*18,invOffset+i*18));
            }
        }
    }

    //GUIにプレイヤーのホットバーを表示
    private void addPlayerHotBar(Inventory playerInv){
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv,i,36+i*18,hotBarOffset));
        }
    }

    public long getDate() {
        return combineIntegersToLong(data.get(ConcentratorBlockEntity.DATE_LOW), data.get(ConcentratorBlockEntity.DATE_HIGH));
    }

    public long getTime() {
        return combineIntegersToLong(data.get(ConcentratorBlockEntity.TIME_LOW), data.get(ConcentratorBlockEntity.TIME_HIGH));
    }

    public long getLevelSeed(){
        return combineIntegersToLong(data.get(ConcentratorBlockEntity.LEVEL_SEED_LOW), data.get(ConcentratorBlockEntity.LEVEL_SEED_HIGH));
    }

    public double getBarrelAccuracy() {
        return (double) data.get(ConcentratorBlockEntity.ACCURACY)/ConcentratorBlockEntity.ACCURACY_DIVIDE;
    }

    public boolean isSyncedToStar() {
        return data.get(ConcentratorBlockEntity.IS_SYNCED_TO_STAR) == 1;
    }

    /*public int getTabId(){
        if(tabId < 0 || tabId > ConcentratorScreen.TAB_COUNT){
            tabId = 0;
            return 0;
        }
        return tabId;
    }*/
}
