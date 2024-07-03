package com.moromoro.heliopause.blockEntity;

import com.moromoro.Heliopause;
import com.moromoro.heliopause.block.RoastingTableBlock;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.screen.RoastingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoastingTableBlockEntity extends BlockEntity implements MenuProvider {
    //メニューを保持するフィールドを宣言
    private RoastingTableMenu menu;
    //消灯時にクラフトを更新するためのboolean
    //private boolean afterExtinguishCall = false;
    //アイテムハンドラを宣言
    private final ItemStackHandler itemHandler = new ItemStackHandler(1){
        //クラフト時に燃料を考慮できるようにする
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            // アイテムが変更されたときにslotsChangedを呼び出す
            if (level != null && !level.isClientSide) {
                if (menu != null) {
                    menu.slotsChanged(new SimpleContainer(itemHandler.getStackInSlot(0)));
                    //Heliopause.LOGGER.debug("slotChanged called in BE");
                }
            }
        }
        //アイテム搬入できるかどうか制御
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack existingStack = this.getStackInSlot(slot);
            //燃料ではない場合
            if(getBurnDuration(stack) == 0){return stack;}
            // 既存のスタックが空、または同種のアイテムでスタックが満杯でない場合
            if (
                    existingStack.isEmpty() ||
                    (
                            (existingStack.getItem() == stack.getItem()) &&
                            (existingStack.getCount() < existingStack.getMaxStackSize())
                    )
            ) {
                return super.insertItem(slot, stack, simulate);
            } else {
                // 種類が一致しないか、スタックが満杯の場合
                return stack;
            }
        }

    };

    //呼びだされたときに用意しておく
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    //燃料の持続がかまどの何倍になるか
    public static final int durationMultiply = 2;

    //データ格納用
    public final ContainerData data;
    //燃料の持続
    private int litDuration = 0;
    //火が尽きるまでの残り
    private int litTime = 0;

    public RoastingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ROASTING_TABLE_BE.get(), pos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index){
                    case 0 -> RoastingTableBlockEntity.this.litTime;
                    case 1 -> RoastingTableBlockEntity.this.litDuration;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index){
                    case 0 -> RoastingTableBlockEntity.this.litTime = value;
                    case 1 -> RoastingTableBlockEntity.this.litDuration = value;
                };
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops(){
        SimpleContainer inventory =new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.heliopause.alchemy_roasting_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        menu = new RoastingTableMenu(containerId,playerInv, this, this.data);
        return menu;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("Slot", itemHandler.serializeNBT());
        nbt.putInt("BurnTime", this.litTime);
        nbt.putInt("BurnDuration", this.litDuration);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Slot"));
        litTime = nbt.getInt("BurnTime");
        litDuration = nbt.getInt("BurnDuration");
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, RoastingTableBlockEntity blockEntity) {
        //火が残っているなら残り時間を減らす
        boolean lit = isLit();
        if(lit){
            blockEntity.litTime--;
            //afterExtinguishCall = false;
        }
        //消灯・点灯が切り替わった場合
        if(lit != blockState.getValue(RoastingTableBlock.LIT)){
            blockState = blockState.setValue(RoastingTableBlock.LIT,lit);
            if (menu != null) {
            menu.slotsChanged(new SimpleContainer(itemHandler.getStackInSlot(0)));
            }
            level.setBlock(pos,blockState,0b111);
            setChanged(level,pos,blockState);
        }
    }

    private void burnFuel() {
        //燃料を取得
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        this.litDuration = this.litTime = getBurnDuration(fuelStack);
        // 燃料として使用した後に残るアイテムを取得
        if(fuelStack.hasCraftingRemainingItem()){
            ItemStack containerItem = fuelStack.getCraftingRemainingItem();
            itemHandler.setStackInSlot(0, containerItem);
        }
        else
        {
            // 燃料を消費
            itemHandler.extractItem(0, 1, false);
        }
    }

    protected int getBurnDuration(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        } else {
            //かまどレシピでの燃焼時間を取得して、倍率を掛ける
            return net.minecraftforge.common.ForgeHooks.getBurnTime(itemStack, RecipeType.SMELTING) * durationMultiply;
        }
    }

    public void setLit() {
        if(!isLit()){
            burnFuel();
        }
    }
}
