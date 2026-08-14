package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import static com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity.*;

public class ConcentratorMenu extends AbstractContainerMenu {

    public final ConcentratorBlockEntity blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerData data;
    
    private static final int invOffset = 84;
    private static final int hotBarOffset = 142;
    
    // アイテム出し入れスロット
    private final ItemStackHandler fluidIOSlotHandler = new ItemStackHandler(4);
    public static final int SLOT_FLUID_IN = 2;
    public static final int SLOT_FLUID_IN_RESULT = 3;
    public static final int SLOT_FLUID_OUT = 4;
    public static final int SLOT_FLUID_OUT_RESULT = 5;

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

        // スロットの位置設定
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            // 入力スロット
            this.addSlot(new SlotItemHandler(iItemHandler, SLOT_INPUT_ITEM, 50, 17));
            // 出力スロット
            this.addSlot(new SlotItemHandler(iItemHandler, SLOT_OUTPUT_ITEM, 110, 17){
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {return false;}
            });
        });
        
        // 液体出し入れ用一時スロット
        this.addSlot(new SlotItemHandler(fluidIOSlotHandler, 0, 28, 17));
        this.addSlot(new SlotItemHandler(fluidIOSlotHandler, 1, 28, 53) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        this.addSlot(new SlotItemHandler(fluidIOSlotHandler, 2, 132, 17));
        this.addSlot(new SlotItemHandler(fluidIOSlotHandler, 3, 132, 53) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        
        //バニラのスロットを表示
        addPlayerInventory(playerInv);//index = 4~30
        addPlayerHotBar(playerInv);//index = 31~39

        addDataSlots(data);
    }
    
    @Override
    public void removed(Player player) {
        super.removed(player);
        // GUI 内のアイテムをプレイヤーに戻す
        if (!player.level().isClientSide) {
            for (int i = 0; i < fluidIOSlotHandler.getSlots(); i++) {
                ItemStack stack = fluidIOSlotHandler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    ItemStack copy = stack.copy();
                    if (!player.getInventory().add(copy)) {
                        player.drop(copy, false);
                    }
                    fluidIOSlotHandler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }
    
    // 液体出し入れスロットの処理
    public ItemStack getGuiStack(int guiIndex) {
        return fluidIOSlotHandler.getStackInSlot(guiIndex);
    }
    
    public void setGuiStack(int guiIndex, ItemStack stack) {
        fluidIOSlotHandler.setStackInSlot(guiIndex, stack);
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

            //操作スロットが結果スロットなら
            if (
                slotId == SLOT_INPUT_ITEM
             || slotId == SLOT_OUTPUT_ITEM
             || slotId == SLOT_FLUID_IN
             || slotId == SLOT_FLUID_OUT
             || slotId == SLOT_FLUID_IN_RESULT
             || slotId == SLOT_FLUID_OUT_RESULT
            ) {
                tempItemStack.getItem().onCraftedBy(tempItemStack, level, player);
                //インベントリかホットバーに入れようとする
                if (!this.moveItemStackTo(tempItemStack, invStart, hotEnd, true)) {
                    //失敗したらスキップ
                    return ItemStack.EMPTY;
                }
            }
            //操作スロットがプレイヤーのスロットなら
            else if (slotId >= invStart && slotId < hotEnd) {
                boolean moveToInsert = false;
                // アイテムが液体保持か確認
                if(FluidUtil.getFluidHandler(tempItemStack).isPresent()){
                    // 液体取り出し用スロットに入れようとする
                    moveToInsert |= this.moveItemStackTo(tempItemStack, SLOT_FLUID_IN, invStart, false);
                    moveToInsert |= this.moveItemStackTo(tempItemStack, SLOT_FLUID_OUT, invStart, false);
                }else{
                    //材料スロットに入れようとする
                    moveToInsert |= this.moveItemStackTo(tempItemStack, SLOT_INPUT_ITEM, invStart, false);
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

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
            player, BlockRegistry.CONCENTRATOR.get());
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

    public long getDate() {
        return combineIntegersToLong(data.get(ConcentratorBlockEntity.DATE_LOW), data.get(ConcentratorBlockEntity.DATE_HIGH));
    }

    public long getTime() {
        return combineIntegersToLong(data.get(ConcentratorBlockEntity.TIME_LOW), data.get(ConcentratorBlockEntity.TIME_HIGH));
    }
    
    public FluidStack getInputFluid() {
        return blockEntity.getInputFluid();
    }
    
    public FluidStack getOutputFluid() {
        return blockEntity.getOutputFluid();
    }

    public boolean isSyncedToStar() {
        return data.get(CAN_SEE_SKY) == 1;
    }
    
    public int getRecipeProgress() {
        return data.get(PROGRESS);
    }
    
    public int getMaxProgress() {
        return data.get(MAX_PROGRESS);
    }
}