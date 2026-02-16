package com.moromoro.heliopause.entity;

import com.mojang.blaze3d.platform.Window;
import com.moromoro.ConfigHolder;
import com.moromoro.heliopause.block.ConcentratorBlock;
import com.moromoro.heliopause.block.LensBarrelBlock;
import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.instance.IhasHoverDrawEntity;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.EntityRegistry;
import com.moromoro.heliopause.screen.ConcentratorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.moromoro.heliopause.block.LensBarrelBlock.renderAccuracyGUI;
import static com.moromoro.heliopause.screen.ConcentratorScreen.isStellarValidOnTime;

public class LensBarrelEntity extends Entity implements IhasHoverDrawEntity {

    private final List<BlockState> barrels = new ArrayList<>();

    private float XRotLast = -90;
    private float YRotLast = 180;
    private float XRotToward = -90;
    private float YRotToward = 180;
    private boolean isSyncedToStar = false;

    //private int updateTimer = 0;
    private long lastUpdateTime = 0;
    public static final int COUNT_UPDATE = 100;
    public static final float ROT_SPEED = 0.3f * COUNT_UPDATE;

    private static final double RAY_MAX_DISTANCE = ConfigHolder.MAX_VIEW_CONCENTRATOR.get();

    private static final EntityDataAccessor<CompoundTag> syncTag =
        SynchedEntityData.defineId(LensBarrelEntity.class, EntityDataSerializers.COMPOUND_TAG);

    public LensBarrelEntity(EntityType<? extends Entity> entityType, Level level) {
        super(EntityRegistry.LENS_BARREL_E.get(), level);
    }

    public LensBarrelEntity(Level level, Vec3 pos, List<BlockState> initBarrels){
        super(EntityRegistry.LENS_BARREL_E.get(), level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPos(pos);
        this.setRot(180, -90);
        this.setYRotLast(180);
        this.setYRotToward(180);
        this.setXRotLast(-90);
        this.setXRotToward(-90);
        //this.setUpdateTimer(COUNT_UPDATE);
        this.setLastUpdateTime(level.getGameTime() - COUNT_UPDATE);
        assemble(initBarrels);
    }

    public void assemble(List<BlockState> validBarrels) {
        if(!level().isClientSide()){
            this.barrels.clear();
            this.barrels.addAll(validBarrels);
            CompoundTag nbt = new CompoundTag();
            addAdditionalSaveData(nbt);
            this.entityData.set(syncTag, nbt);
        }
    }

    @Override public boolean canBeCollidedWith() {
        return true;
    }

    @Override public boolean isPickable() {
        return true;
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {

        InteractionResult sidedSuccess = disassemble();
        if (sidedSuccess != null) return sidedSuccess;

        return super.interact(player, hand);
    }

    @Nullable
    public InteractionResult disassemble() {
        Level level = this.level();
        BlockPos blockPos = this.getOnPos().below();
        if(level.isClientSide){
            return null;
        }
        // ドロップ用
        LootParams.Builder lootBuilder = new LootParams.Builder((ServerLevel) level)
            .withParameter(LootContextParams.ORIGIN, blockPos.getCenter())
            .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        // 鏡筒をブロックに戻す 設置できない場合はドロップ
        for (int barrelPos = 0; barrelPos < barrels.size(); barrelPos++) {
            BlockState barrelState = barrels.get(barrelPos);
            BlockPos placePos = blockPos.above(barrelPos + 1);
            if(level.getBlockState(placePos).canBeReplaced()) {
                level.setBlock(placePos, barrelState, 3);
            }else {
                List<ItemStack> drops = barrelState.getDrops(lootBuilder);
                SoundEvent breakSound = barrelState.getSoundType().getBreakSound();
                level.playSound(null, blockPos, breakSound, SoundSource.BLOCKS);
                for (ItemStack drop : drops) {
                    ItemEntity itemEntity = new ItemEntity(level, placePos.getX() + 0.5, placePos.getY() + 0.5, placePos.getZ() + 0.5, drop);
                    level.addFreshEntity(itemEntity);
                }
            }
        }
        if(level.getBlockState(blockPos).is(BlockRegistry.CONCENTRATOR.get())){
            BlockState newState =  level.getBlockState(blockPos).setValue(ConcentratorBlock.ENABLED, false);
            level.setBlock(blockPos,newState, 3);
            level.sendBlockUpdated(blockPos, newState, newState, 3);
        }
        barrels.clear();

        level.playSound(null, blockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0f, 0.8f);

        this.discard();

        return InteractionResult.sidedSuccess(!level().isClientSide());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(syncTag, new CompoundTag());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.entityData.set(syncTag, nbt);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if(key.equals(syncTag)){
            readAdditionalSaveData(this.entityData.get(syncTag));
            createBoundingBox();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        Level level = level();
        barrels.clear();
        ListTag barrelTags = nbt.getList("barrels", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < barrelTags.size(); i++) {
            CompoundTag barrelTag = barrelTags.getCompound(i);
            this.barrels.add(NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), barrelTag));
        }
        //setUpdateTimer(nbt.getInt("timer"));
        setLastUpdateTime(nbt.getLong("lastUpdate"));
        setYRotToward(nbt.getFloat("yRotToward"));
        setXRotToward(nbt.getFloat("xRotToward"));
        setYRotLast(nbt.getFloat("yRotLast"));
        setXRotLast(nbt.getFloat("xRotLast"));
        setSyncedToStar(nbt.getBoolean("isSyncedToStar"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        ListTag barrelTags = new ListTag();
        for (BlockState barrel : this.barrels) {
            barrelTags.add(NbtUtils.writeBlockState(barrel));
        }
        nbt.put("barrels", barrelTags);
        //nbt.putInt("timer", getUpdateTimer());
        nbt.putLong("lastUpdate", getLastUpdateTime());
        nbt.putFloat("yRotToward", getYRotToward());
        nbt.putFloat("xRotToward", getXRotToward());
        nbt.putFloat("yRotLast", getYRotLast());
        nbt.putFloat("xRotLast", getXRotLast());
        nbt.putBoolean("isSyncedToStar", isSyncedToStar());
    }

    @Override
    public void baseTick() {
        Level level = level();
        if(!level.isClientSide){
            if(level.getGameTime() - getLastUpdateTime() >= COUNT_UPDATE){
                setLastUpdateTime(level.getGameTime());
                BlockPos blockEntityPos = BlockPos.containing(getPosition(0)).below();

                this.setRot(180, -90);

                // 真下が収斂器か確認
                if (level.getBlockEntity(blockEntityPos) instanceof ConcentratorBlockEntity blockEntity) {
                    Vector2i trackingCoordinate = blockEntity.getTargetStarCoordinate();
                    if(trackingCoordinate != null && level instanceof ServerLevel serverLevel){
                        long date = Season.getDayInYear(serverLevel);
                        long dayTime = serverLevel.getDayTime();
                        float partialDate = date + (dayTime % 24000)/24000f;
                        Vector2d anglePos = ConcentratorScreen.equatorialToHorizontal(trackingCoordinate, partialDate, dayTime, true);
                        Vector2f targetPos = new Vector2f((float)-anglePos.y(), (float)anglePos.x() + 90);
                        // 前回の値を仮保持
                        float tempYLast = this.getYRotToward();
                        float tempXLast = this.getXRotToward();
                        // 値を適用
                        boolean isSynced;
                        isSynced  = this.setYRotToward(targetPos.y());
                        isSynced |= this.setXRotToward(targetPos.x());
                        this.setYRotLast(tempYLast);
                        this.setXRotLast(tempXLast);

                        boolean isStellarValid = isStellarValidOnTime(dayTime % 24000, anglePos);

                        if(isStellarValid && isSynced){
                            this.setSyncedToStar(canSeeSky());
                        }else{
                            this.setSyncedToStar(false);
                        }

                    }
                }else{
                    this.disassemble();
                }

                CompoundTag nbt = new CompoundTag();
                addAdditionalSaveData(nbt);
                this.entityData.set(syncTag, nbt);
            }
        }
        createBoundingBox();
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        createBoundingBox();
        return getBoundingBox();
    }

    private void createBoundingBox() {
        // 角度からバウンディングボックス作成
        double barrelLength = barrels != null ? getBarrels().size() - 1 : 1;
        double partialXRot = Math.toRadians(90 + getPartialXRot(0));
        double partialYRot = Math.toRadians( - getPartialYRot(0));
        double barrelWidth = barrelLength * Math.sin(partialXRot);
        double barrelHeight = barrelLength * Math.cos(partialXRot) - 8d/16;
        Vec3 startPos = this.position().add(0,16d/16,0);
        Vec3 endPos = new Vec3(Math.sin(partialYRot) * barrelWidth, barrelHeight, Math.cos(partialYRot) * barrelWidth).add(startPos);
        AABB box = new AABB(startPos, endPos).inflate(0.5);
        setBoundingBox(box);
    }

    public float getPartialXRot(float partialTicks){
        double deltaXRot = getXRotToward() - getXRotLast();
        double updateTime = level().getGameTime() - getLastUpdateTime();
        double updatePartialTicks = Math.min(1f, (updateTime + partialTicks)/COUNT_UPDATE);
        return (float) (getXRotLast() + deltaXRot * updatePartialTicks);
    }

    public float getPartialYRot(float partialTicks){
        double deltaYRot = getYRotToward() - getYRotLast();
        deltaYRot = (360 + deltaYRot % 360) % 360;
        if(deltaYRot > 180){
            deltaYRot -= 360;
        }
        double updateTime = level().getGameTime() - getLastUpdateTime();
        double updatePartialTicks = Math.min(1f, (updateTime + partialTicks)/COUNT_UPDATE);
        return (float) (getYRotLast() + deltaYRot * updatePartialTicks);
    }

    public Vec3 getPartialLookVec(float partialTicks){
        double XRot = Math.toRadians(getPartialXRot(partialTicks));
        double YRot = Math.toRadians(getPartialYRot(partialTicks));
        double normalW = Math.cos(XRot);
        double normalY = -Math.sin(XRot);
        double normalX = -Math.sin(YRot) * normalW;
        double normalZ = Math.cos(YRot) * normalW;
        return new Vec3(normalX, normalY, normalZ);
    }

    public List<BlockState> getBarrels() {
        return barrels;
    }

    public double getWholeAccuracy(){
        double wholeAccuracy = 1.0;
        for (BlockState barrel : barrels) {
            wholeAccuracy *= LensBarrelBlock.getBarrelAccuracy(LensBarrelBlock.getBarrelType(barrel));
        }
        return wholeAccuracy * barrels.size();
    }

    public float getXRotToward() {
        return XRotToward;
    }

    public float getYRotToward() {
        return (360+YRotToward)%360;
    }

    public boolean setXRotToward(float XRotToward) {
        float deltaXRot = (XRotToward - this.XRotToward);
        deltaXRot = Math.min(Math.max(deltaXRot, -ROT_SPEED), ROT_SPEED);
        this.XRotToward = Math.min(this.XRotToward + deltaXRot,0);
        return Math.abs(deltaXRot) <= ROT_SPEED;
    }

    public boolean setYRotToward(float YRotToward) {
        float deltaYRot = (360 + YRotToward - this.YRotToward)% 360;
        if(deltaYRot > 180){
            deltaYRot -= 360;
        }
        deltaYRot = Math.min(Math.max(deltaYRot, -ROT_SPEED), ROT_SPEED);
        this.YRotToward += deltaYRot;
        return Math.abs(deltaYRot) <= ROT_SPEED;
    }

    public boolean isSyncedToStar() {
        return isSyncedToStar;
    }

    private boolean canSeeSky() {
        // 視界が有効か確認
        Vec3 lookVec = getPartialLookVec(0);

        Vec3 clipStart = lookVec.scale(0.7).add(getPosition(0));
        Vec3 clipEnd = lookVec.scale(RAY_MAX_DISTANCE).add(getPosition(0));

        // 遮蔽判定
        ClipContext context = new ClipContext(clipStart, clipEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null);
        BlockHitResult result = level().clip(context);

        return !result.getType().equals(HitResult.Type.BLOCK);
    }

    public void setSyncedToStar(boolean syncedToStar) {
        this.isSyncedToStar = syncedToStar;
    }

    public float getXRotLast() {
        return XRotLast;
    }

    public void setXRotLast(float XRotLast) {
        this.XRotLast = XRotLast;
    }

    public float getYRotLast() {
        return YRotLast;
    }

    public void setYRotLast(float YRotLast) {
        this.YRotLast = YRotLast;
    }

    @Override
    public boolean renderHoverGraphicWithEntity(RenderGuiOverlayEvent event, ClientLevel level) {
        Minecraft instance = Minecraft.getInstance();
        LocalPlayer player = instance.player;
        // プレイヤー確認
        if(player == null){
            return false;
        }
        if (!instance.options.renderDebug) {
            List<BlockState> invertBarrels = new ArrayList<>(barrels);
            Collections.reverse(invertBarrels);
            renderAccuracyGUI(event, instance, invertBarrels, true, true);
        } else {
            // デバッグ用
            Window window = event.getWindow();
            GuiGraphics graphics = event.getGuiGraphics();
            final int x = (window.getGuiScaledWidth())/2;
            final int y = (window.getGuiScaledHeight())/2;

            graphics.drawString(instance.font, "rotY: " + String.format("%.1f",getPartialYRot(0)) + ", "
                + String.format("%.1f",getYRotLast()) + "->"+ String.format("%.1f",getYRotToward()), x + 10, y, 0xFF8080);
            graphics.drawString(instance.font, "rotX: " + String.format("%.1f",getPartialXRot(0)) + ", "
                + String.format("%.1f",getXRotLast()) + "->"+ String.format("%.1f",getXRotToward()), x + 10, y + 10, 0x80FF80);
            Vec3 lookVec = getPartialLookVec(0);
            graphics.drawString(instance.font, "lookVector: "
                + String.format("%.1f",lookVec.x()) +", "+ String.format("%.1f",lookVec.y()) + ", "+ String.format("%.1f",lookVec.z()),
                x + 10, y + 20, 0xFFFFFF);
            graphics.drawString(instance.font, "updateTime: " + (level().getGameTime() - getLastUpdateTime()), x + 10, y + 30, 0x8080FF);
            graphics.drawString(instance.font, "isSyncedToStar: " + isSyncedToStar, x + 10, y + 40, 0x8080FF);
            graphics.drawString(instance.font, "canSeeSky: " + canSeeSky(), x + 10, y + 50, 0x8080FF);
        }

        return true;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
