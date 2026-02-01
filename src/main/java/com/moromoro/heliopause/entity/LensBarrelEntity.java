package com.moromoro.heliopause.entity;

import com.moromoro.heliopause.blockEntity.ConcentratorBlockEntity;
import com.moromoro.heliopause.generic.Season;
import com.moromoro.heliopause.registry.EntityRegistry;
import com.moromoro.heliopause.screen.ConcentratorScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class LensBarrelEntity extends Entity {

    private final List<BlockState> barrels = new ArrayList<>();

    private float XRotLast = 0;
    private float YRotLast = 0;
    private float XRotToward = 0;
    private float YRotToward = 0;
    private boolean isSyncedToStar = false;

    private int updateTimer = 0;
    public static final int COUNT_UPDATE = 100;
    public static final float ROT_SPEED = 0.3f * COUNT_UPDATE;

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
        this.setUpdateTimer(COUNT_UPDATE);
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

        BlockPos blockEntityPos = BlockPos.containing(getPosition(0)).below();
        // 真下が収斂器か確認
        if (level().getBlockEntity(blockEntityPos) instanceof ConcentratorBlockEntity blockEntity) {
            if(!level().isClientSide()) {
                blockEntity.disAssemble(barrels);
                this.discard();
            }
            return InteractionResult.sidedSuccess(!level().isClientSide());
        }

        return super.interact(player, hand);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(syncTag, new CompoundTag());
    }

    @Override
    public void load(CompoundTag nbt) {
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
        setUpdateTimer(nbt.getInt("timer"));
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
        nbt.putInt("timer", getUpdateTimer());
        nbt.putFloat("yRotToward", getYRotToward());
        nbt.putFloat("xRotToward", getXRotToward());
        nbt.putFloat("yRotLast", getYRotLast());
        nbt.putFloat("xRotLast", getXRotLast());
        nbt.putBoolean("isSyncedToStar", isSyncedToStar());
    }

    @Override
    public void baseTick() {
        if(getUpdateTimer() < COUNT_UPDATE){
            setUpdateTimer(getUpdateTimer() + 1);
        }
        else if (!level().isClientSide()) {
            setUpdateTimer(0);

            Level level = level();
            BlockPos blockEntityPos = BlockPos.containing(getPosition(0)).below();

            this.setRot(180, -90);
            this.setYRotLast(this.getYRotToward());
            this.setXRotLast(this.getXRotToward());

            // 真下が収斂器か確認
            if (level.getBlockEntity(blockEntityPos) instanceof ConcentratorBlockEntity blockEntity) {
                Vector2i trackingCoordinate = blockEntity.getTargetStarCoordinate();
                if(trackingCoordinate != null && level instanceof ServerLevel serverLevel){
                    long date = Season.getDayInYear(serverLevel);
                    long dayTime = serverLevel.getDayTime();
                    float partialDate = date + (dayTime % 24000)/24000f;
                    Vector2d anglePos = ConcentratorScreen.equatorialToHorizontal(trackingCoordinate, partialDate, dayTime);
                    this.setYRotToward((float)anglePos.x() + 90);
                    this.setXRotToward((float)-anglePos.y());
                    this.setSyncedToStar((anglePos.equals(this.getXRotToward(), this.getYRotToward())));
                }
                blockEntity.setSyncedToStar(this.isSyncedToStar());
            }

            CompoundTag nbt = new CompoundTag();
            addAdditionalSaveData(nbt);
            this.entityData.set(syncTag, nbt);
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
        double deltaXRot = (getXRotToward() - getXRotLast());
        double updatePartialTicks = Math.min(1f, (getUpdateTimer() + partialTicks)/COUNT_UPDATE);
        return (float) (getXRotLast() + deltaXRot * updatePartialTicks);
    }

    public float getPartialYRot(float partialTicks){
        double deltaYRot = getYRotToward() - getYRotLast();/*(360 + getYRotToward() - getYRotLast())% 360;
        if(deltaYRot > 180){
            deltaYRot -= 360;
        }*/
        double updatePartialTicks = Math.min(1f, (getUpdateTimer() + partialTicks)/COUNT_UPDATE);
        return (float) (getYRotLast() + deltaYRot * updatePartialTicks);
    }

    public List<BlockState> getBarrels() {
        return barrels;
    }

    private void setUpdateTimer(int updateTimer) {
        this.updateTimer = updateTimer;
    }

    public int getUpdateTimer() {
        return updateTimer;
    }

    public float getXRotToward() {
        return XRotToward;
    }

    public void setXRotToward(float XRotToward) {
        float deltaXRot = (XRotToward - getXRotLast());
        deltaXRot = Math.min(Math.max(deltaXRot, -ROT_SPEED), ROT_SPEED);
        this.XRotToward = Math.min(getXRotLast() + deltaXRot, 0);
    }

    public float getYRotToward() {
        return YRotToward;
    }

    public void setYRotToward(float YRotToward) {
        float deltaYRot = (360 + YRotToward - getYRotLast())% 360;
        if(deltaYRot > 180){
            deltaYRot -= 360;
        }
        deltaYRot = Math.min(Math.max(deltaYRot, -ROT_SPEED), ROT_SPEED);
        this.YRotToward = getYRotLast() + deltaYRot;
    }

    public boolean isSyncedToStar() {
        return isSyncedToStar;
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
}
