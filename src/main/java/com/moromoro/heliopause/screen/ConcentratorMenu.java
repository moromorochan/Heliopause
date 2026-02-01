package com.moromoro.heliopause.screen;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class ConcentratorMenu extends AbstractContainerMenu {

    public final ConcentratorBlockEntity blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerData data;
    public static final int UNIDENTIFIED_COORDINATE = 511;
    public static final int COORDINATE_BUTTON_INDEX = 3;
    //private final RandomSource randomSource;
    //private final long date;
    //private final long time;

    private static final int invOffset = 137;
    private static final int hotBarOffset = 195;

    public ConcentratorMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(ConcentratorBlockEntity.DATA_ACCESS_LENGTH));
    }

    public ConcentratorMenu(int containerId, Inventory playerInv, BlockEntity entity, ContainerData data) {
        super(MenuTypeRegistry.CONCENTRATOR_MENU.get(), containerId);

        blockEntity = (ConcentratorBlockEntity) entity;
        //long levelSeed =
        //randomSource = RandomSource.create(levelSeed);
        this.player = playerInv.player;
        this.level = player.level();
        //this.date =
        //this.time = ;
        this.data = data;
        //バニラのスロットを表示
        addPlayerInventory(playerInv);//index = 2~28
        addPlayerHotBar(playerInv);//index = 29~37

        addDataSlots(data);
    }

    // 分割したlongを戻す
    private static long combineIntegersToLong(int low, int high) {
        return ((long) high << 32) | ((long) low & 0xFFFFFFFFL);
    }

    // ランダムから星の位置生成
    public List<Vector2i> getStarCoordinates(){

        List<Vector2i> starCoordinates = new ArrayList<>();
        long localSeed = combineIntegersToLong(data.get(ConcentratorBlockEntity.LEVEL_SEED_LOW), data.get(ConcentratorBlockEntity.LEVEL_SEED_HIGH));//randomSource.nextLong();
        for (int i = 0; i < ConcentratorBlockEntity.STAR_COUNT; i++) {
            RandomSource localRandom = RandomSource.create(localSeed);
            int x = localRandom.nextInt(ConcentratorBlockEntity.STAR_RANGE_X);
            int y = localRandom.nextInt(ConcentratorBlockEntity.STAR_RANGE_MIN_Y, ConcentratorBlockEntity.STAR_RANGE_MAX_Y);
            starCoordinates.add(new Vector2i(x,y));
            localSeed = localRandom.nextLong();
        }
        return starCoordinates;
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

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if(slotId == COORDINATE_BUTTON_INDEX){
            // サーバー側での処理
            if (stillValid(player) && player instanceof ServerPlayer serverPlayer) {// ブロックエンティティに反映
                Level serverPlayerLevel = serverPlayer.level();
                if (serverPlayerLevel.getBlockEntity(blockEntity.getBlockPos()) instanceof ConcentratorBlockEntity entity) {

                    entity.setTargetStarCoordinate(decodeStarCoordinate(dragType));
                    entity.setChanged();
                    serverPlayerLevel.sendBlockUpdated(entity.getBlockPos(), entity.getBlockState(), entity.getBlockState(), 3);
                    return;
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return ItemStack.EMPTY;
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
}
