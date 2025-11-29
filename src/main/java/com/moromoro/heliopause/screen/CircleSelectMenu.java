package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.SiderostatBlockEntity;
import com.moromoro.heliopause.item.CompassItem;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.MenuTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CircleSelectMenu extends AbstractContainerMenu {

    private final Level level;
    private final ItemStack toolItemStack;
    private final ContainerData data;

    private final Player player;

    public CircleSelectMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.getUseItem(), new SimpleContainerData(SiderostatBlockEntity.DATA_ACCESS_LENGTH));
    }

    public CircleSelectMenu(int containerId, Inventory playerInv, ItemStack itemStack, ContainerData data){
        super(MenuTypeRegistry.CIRCLE_SELECT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.level = player.level();
        this.toolItemStack = itemStack;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        // 操作アイテムがあるか確認
        if(!player.getMainHandItem().is(toolItemStack.getItem())&&
            !player.getOffhandItem().is(toolItemStack.getItem())){
            return false;
        }
        // プレイヤーが右クリックを押し続けているか確認
        if(!player.isUsingItem()){
            return false;
        }
        // ターゲット位置が黒板か確認
        /*if(!level.getBlockState(getTargetBlockPos()).is(BlockRegistry.BLACKBOARD.get()) &&
            !level.getBlockState(getTargetBlockPos()).is(BlockRegistry.WRITTEN_BOARD.get())){
            return false;
        }*/
        return true;
    }

    /*private BlockPos getTargetBlockPos() {
        // dataからブロック位置を取り出す
        return new BlockPos(
            data.get(CompassItem.TARGET_POS_X_INDEX),
            data.get(CompassItem.TARGET_POS_Y_INDEX),
            data.get(CompassItem.TARGET_POS_Z_INDEX)
        );
    }

    public void changeSelect(int delta){
        int next = Math.max(CompassItem.SELECT_MIN,
            Math.min(data.get(CompassItem.SELECT_INDEX)+delta,
                CompassItem.SELECT_MAX));
        data.set(CompassItem.SELECT_INDEX, next);
    }*/
}
