package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.RoastingTableBlockEntity;
import com.moromoro.heliopause.recipe.RoastingRecipe;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.MenuTypeRegistry;
import com.moromoro.heliopause.registry.RecipeTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

import static com.moromoro.heliopause.blockEntity.RoastingTableBlockEntity.durationMultiply;

public class RoastingTableMenu extends AbstractContainerMenu {

    public final RoastingTableBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private static final int LIT_TIME_INDEX = 0;
    private static final int LIT_DURATION_INDEX = 1;

    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer resultSlots = new ResultContainer();
    private final Player player;
    private static final int RESULT_SLOT_INDEX = 0;

    public RoastingTableMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }
    public RoastingTableMenu(int containerId, Inventory playerInv, BlockEntity entity, ContainerData data){
        super(MenuTypeRegistry.ROASTING_TABLE_MENU.get(), containerId);
        player = playerInv.player;
        blockEntity = (RoastingTableBlockEntity) entity;
        this.level = player.level();
        this.data = data;

        //GUIにリザルトスロットを追加 index = 0
        this.addSlot(new Slot(this.resultSlots,0,124,35){
            //アイテムを取り出したときの処理をオーバーライド
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);

                // クラフト結果が取り出されたときにクラフトグリッドのアイテムを消費
                for (int i = 0; i < RoastingTableMenu.this.craftSlots.getContainerSize(); ++i) {
                    RoastingTableMenu.this.craftSlots.removeItem(i, 1);
                }
                //燃焼状態にする
                blockEntity.setLit();
            }

            //プレイヤーがアイテムを置けないようにする
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        //GUIに燃料スロットを表示 index = 1
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler,0,26,58));
        });

        //GUIにクラフトグリッドを追加 index = 2~5
        for(int i = 0; i < 2; ++i) {
            for(int j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 2, 48 + j * 18, 26 + i * 18));
            }
        }

        //バニラのスロットを表示
        addPlayerInventory(playerInv);//index = 6~32
        addPlayerHotBar(playerInv);//index = 33~41

        addDataSlots(data);
    }

    //GUIにプレイヤーのインベントリを表示
    private void addPlayerInventory(Inventory playerInv){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv,j+i*9+9,8+j*18,84+i*18));
            }
        }
    }

    //GUIにプレイヤーのホットバーを表示
    private void addPlayerHotBar(Inventory playerInv){
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv,i,8+i*18,142));
        }
    }

    //シフトクリックでの移動に対応させる
    public ItemStack quickMoveStack(Player player, int slotId) {
        final int invStart = 6;
        final int hotStart = 33;
        final int hotEnd = 41;
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
    //ブロックが壊されていないか確認
    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, BlockRegistry.ROASTING_TABLE.get());
    }

    public boolean isBurning() {
        return data.get(LIT_TIME_INDEX) > 0;
    }

    public int getLitTime() {
        return data.get(LIT_TIME_INDEX);
    }

    public int getLitDuration() {
        return data.get(LIT_DURATION_INDEX);
    }

    //スロットが変更される毎にレシピを確認
    @Override
    public void slotsChanged(Container container) {
        //Heliopause.LOGGER.debug("slotsChanged called in Menu");
        //サーバーサイドで実行
        if (!this.level.isClientSide) {
            //プレイヤーを取得
            ServerPlayer serverplayer = (ServerPlayer) this.player;
            //スロットの中身を格納するアイテムスタックを宣言
            ItemStack itemstack = ItemStack.EMPTY;
            //ブロックが点灯可能なら
            if (canBeBurn()) {
                //レシピがあるかどうか確認
                Optional<RoastingRecipe> optional = this.level.getServer().getRecipeManager().getRecipeFor(RecipeTypeRegistry.ROASTING.get(), craftSlots, this.level);
                if (optional.isPresent()) {
                    //レシピを取得
                    RoastingRecipe roastingrecipe = optional.get();

                    //クラフト結果を生成
                    if (resultSlots.setRecipeUsed(this.level, serverplayer, roastingrecipe)) {
                        ItemStack tempItemstack = roastingrecipe.assemble(craftSlots, this.level.registryAccess());
                        if (tempItemstack.isItemEnabled(this.level.enabledFeatures())) {
                            //結果を入れる
                            itemstack = tempItemstack;
                        }
                    }
                }

            }
            //結果スロットにアイテムを適用
            resultSlots.setItem(0, itemstack);
            this.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, itemstack));
        }
    }

    public boolean canBeBurn() {
        return (isBurning()||hasFuel());
    }

    private boolean hasFuel() {
        ItemStack fuelItem = this.slots.get(1).getItem();
        boolean hasFuel = !fuelItem.isEmpty();
        int fuelDuration = getBurnDuration(fuelItem);
        return hasFuel && fuelDuration > 0;
    }

    protected int getBurnDuration(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        } else {
            //アイテムを取得
            Item item = itemStack.getItem();
            //かまどレシピでの燃焼時間を取得して、倍率を掛ける
            return net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack, RecipeType.SMELTING) * durationMultiply;
        }
    }

    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.craftSlots);
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    public boolean shouldMoveToInventory(int p_150553_) {
        return p_150553_ != 0;
    }
}
