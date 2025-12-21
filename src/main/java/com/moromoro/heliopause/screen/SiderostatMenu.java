package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class SiderostatMenu extends AbstractContainerMenu {

    public final SiderostatBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    //private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;
    private static final int RESULT_SLOT_INDEX = 0;

    private static final int invOffset = 84 + 9;
    private static final int hotBarOffset = 142 + 9;

    public SiderostatMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(SiderostatBlockEntity.DATA_ACCESS_LENGTH));
    }

    public SiderostatMenu(int containerId, Inventory playerInv, BlockEntity entity, ContainerData data){
        super(MenuTypeRegistry.SIDEROSTAT_MENU.get(), containerId);
        player = playerInv.player;
        blockEntity = (SiderostatBlockEntity) entity;
        this.level = player.level();

        this.data = data;

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            //結果スロットを設定 index = 0
            this.addSlot(new SlotItemHandler(iItemHandler,0,80,62){
                //アイテムを取り出したときの処理をオーバーライド
                @Override
                public void onTake(Player player, ItemStack stack) {
                    super.onTake(player, stack);
                    // 経験値を出す

                }

                //プレイヤーがアイテムを置けないようにする
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });

            // 材料スロットを設定 1
            this.addSlot(new SlotItemHandler(iItemHandler,1,80,21));
        });

        //バニラのスロットを表示
        addPlayerInventory(playerInv);//index = 2~28
        addPlayerHotBar(playerInv);//index = 29~37

        addDataSlots(data);
    }

    /*public ContainerData getData() {
        return data;
    }*/

    /*public SiderostatBlockEntity getBlockEntity() {
        return blockEntity;
    }*/
    public int getSpringAmount(){
        return data.get(SiderostatBlockEntity.SPRING_AMOUNT_INDEX);
    }
    public int getCraftingProgress(){
        return data.get(SiderostatBlockEntity.CRAFTING_PROGRESS_INDEX);
    }
    public int getCraftingTotalTime(){
        /*if(level.isClientSide())
        {return 0;}*/
        return data.get(SiderostatBlockEntity.CRAFTING_TOTAL_TIME_INDEX);
    }
    public short getCanSeeSkies(){
        return (short) data.get(SiderostatBlockEntity.CAN_SEE_SKIES_INDEX);
    }

    @Override
    //シフトクリックでの移動に対応させる
    public ItemStack quickMoveStack(Player player, int slotId) {
        final int invStart = 2;
        final int hotStart = 29;
        final int hotEnd = 37;
        //スロットの中身を格納するアイテムスタックを宣言
        ItemStack itemstack = ItemStack.EMPTY;
        //スロットを取得
        Slot slot = this.slots.get(slotId);
        if (slot != null && slot.hasItem()) {
            //中身を取得してコピーを作成
            ItemStack tempItemStack = slot.getItem();
            itemstack = tempItemStack.copy();

            //操作スロットが結果スロットなら
            if (slotId == RESULT_SLOT_INDEX) {
                tempItemStack.getItem().onCraftedBy(tempItemStack, level, player);
                //インベントリかホットバーに入れようとする
                if (!this.moveItemStackTo(tempItemStack, invStart, hotEnd, true)) {
                    //失敗したらスキップ
                    return ItemStack.EMPTY;
                }
                //一括クラフトを実行する
                slot.onQuickCraft(tempItemStack, itemstack);
            }
            //操作スロットがプレイヤーのスロットなら
            else if (slotId >= invStart && slotId < hotEnd) {
                //材料スロットに入れようとする
                if (!this.moveItemStackTo(tempItemStack, 1, invStart, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos().above()),
            player, BlockRegistry.SIDEROSTAT_TOP.get());
    }

    //GUIにプレイヤーのインベントリを表示
    private void addPlayerInventory(Inventory playerInv){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv,j+i*9+9,8+j*18,invOffset+i*18));
            }
        }
    }

    //GUIにプレイヤーのホットバーを表示
    private void addPlayerHotBar(Inventory playerInv){
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv,i,8+i*18,hotBarOffset));
        }
    }
}
