package com.moromoro.heliopause.block;

import com.moromoro.heliopause.registry.enumProperty.WrittenBoardDrawType;
import com.moromoro.heliopause.blockEntity.OrreryCircleBoardBlockEntity;
import com.moromoro.heliopause.ingredient.CircumstellarIngredient;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.BlockRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.render.OrreryCircleBoardRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.max;

public class OrreryCircleBoardBlock extends AbstractWrittenBoardBlock {
    public static final float InnerLimitRadius = 1;
    public OrreryCircleBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CIRCLE_TYPE, WrittenBoardDrawType.LARGE_SYMBOL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new OrreryCircleBoardBlockEntity(blockPos, blockState);
    }

    @Override
    public void onRemove(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState newState, boolean isMoving) {
        if(!newState.is(BlockRegistry.ORRERY_CIRCLE_BOARD.get()) && level.getBlockEntity(blockPos) instanceof OrreryCircleBoardBlockEntity entity){
            entity.breakRecipe();
        }
        super.onRemove(blockState, level, blockPos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()){return null;}
        return createTickerHelper(blockEntityType, BlockEntityRegistry.ORRERY_CIRCLE_BOARD_BE.get(),
            (tickLevel,pos,tickBlockState,blockEntity) -> blockEntity.tick(tickLevel,pos,tickBlockState,blockEntity)
        );
    }

    @Override
    public void animateTick(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource randomSource) {
        super.animateTick(blockState, level, pos, randomSource);

        //パーティクル生成
        if(level.isClientSide()){
            if(level.getBlockEntity(pos) instanceof OrreryCircleBoardBlockEntity entity){
                List<CircumstellarIngredient> ingredients = entity.getCircumstellars();
                if(!ingredients.isEmpty()) {
                    float centForce = (float) entity.getCentForce(1);
                    for (int i = 0; i < ingredients.size(); i++) {
                        CircumstellarIngredient ingredient = ingredients.get(i);
                        float orbitalRadius = ingredient.getOrbitalRadius();
                        // 星周円盤
                        if(ingredient.isDiskShaped()){
                            //円盤
                            //float ringRadius = ingredient.getOrbitalRadius();
                            if(ingredient.isValid()){
                                float ringWidth = calcRingWidth(max(ingredient.getFluidStack().getAmount(), ingredient.getItemStack().getCount() * 1000), orbitalRadius);

                                //公転
                                //ingredient.setRevolutionOffset(OrreryCircleBoardBlockEntity.calcRevProcess(orbitalRadius, ingredient.getRevolutionOffset(), centForce));
                                //setChanged();

                                //パーティクル生成
                                if(!ingredient.getFluidStack().isEmpty()){
                                    createFluidDiskParticle(level, pos.above().getCenter(), ingredient.getFluidStack(), ingredient.getRevolutionOffset(), orbitalRadius, ringWidth, centForce, randomSource);
                                }
                                else if(!ingredient.getItemStack().isEmpty()){
                                    createItemDiskParticle(level, pos.above().getCenter(), ingredient.getItemStack(), ingredient.getRevolutionOffset(), orbitalRadius, ringWidth, centForce, randomSource);
                                }
                            }
                        }
                        // 天体の軌跡
                        /*else if(randomSource.nextInt(i,2 + i)<1 + i){
                            Vector2d orbitalCartLocalPos = PolarCoordinates.getCartesianCoordinates(orbitalRadius, ingredient.getRevolutionOffset());
                            Vec3 orbitalCartPos = pos.getCenter().add(new Vec3(orbitalCartLocalPos.x(), 0, orbitalCartLocalPos.y()));
                            EndRodParticle particle = (EndRodParticle) Minecraft.getInstance().particleEngine.createParticle(
                                ParticleTypes.END_ROD,
                                orbitalCartPos.x(),orbitalCartPos.y() + 1, orbitalCartPos.z(), 0,0,0
                            );
                            if(particle!=null) {
                                particle.setLifetime(100);
                                particle.setPower(0.90f);
                            }
                        }*/
                    }

                    //if(randomSource.nextFloat() < 1f/2) {
                    float offset;
                    if (entity.isCraftingInProgress())
                        offset = 0.5f + randomSource.nextFloat() * (float) max(0, OrreryCircleBoardRenderer.getProgressOffsetY(entity, 0) - 1);
                    else {
                        offset = 0.5f;
                    }
                    float outerCircleSize = (float) (Collections.max(entity.getCircleRadii()) - OrreryCircleBoardBlockEntity.CLICK_SIZE);
                    createAsteroidParticle(level,
                        pos.getCenter().add(0, offset + 0.05, 0),
                        (outerCircleSize + InnerLimitRadius) / 2,
                        outerCircleSize - InnerLimitRadius,
                        centForce, randomSource);
                    //}
                }
            }
        }
    }

    public static float calcRingWidth(int ingredientAmount, float ringRadius){
        return (float) max((1f/16f), Math.sqrt(ingredientAmount*0.01f/Math.PI + ringRadius*ringRadius) - ringRadius);
    }

    // 液体の環
    private void createFluidDiskParticle(Level level, Vec3 pos, FluidStack fluidStack, float revolution, float ringRadius, float ringWidth, float centripetalForce, RandomSource randomSource) {
        //パーティクルの数を生成
        int particleAmount = max(2,(int) (ringWidth*4f));//(int) (Math.pow(ringRadius,0.0));

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.x;
            float centerPosY = (float) pos.y + 1f/16f;
            float centerPosZ =(float) pos.z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float spreadRandom = (float) ((randomSource.nextFloat()-0.5f) * 360);
            float randAngle = revolution + 180 + spreadRandom;
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth * (1 + (2 - Math.abs(spreadRandom/90f)) * (1-(float) 360 /360));
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = max(ringRadius + orbitError, 0);

            //パーティクルに適用する色を液体から用意
            int[] color = getFluidRingColor(fluidStack.getFluid(),orbitRadius,BlockPos.containing(pos));

            //パーティクルを生成
            WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                centerPosX , centerPosY, centerPosZ , 0, 0, 0
            );

            //パーティクルに値を入れる
            if (ringParticle != null) {

                if(level!=null && !level.isClientSide())
                {
                    ringParticle.setLightIntensity(fluidStack.getFluid().getFluidType().getLightLevel());
                }

                ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(90),0,randAngle,orbitRadius,centripetalForce);
                //パーティクルの大きさをリングの端からの距離に合わせる
                ringParticle.setPixelBasedSize(orbitWidth*0.5f-Math.abs(orbitError));

                ringParticle.setColor(color[0],color[1],color[2]);
            }

            //信号が入っていない場合、散布パーティクルを生成する
                /*if(!redStonePowered()){
                    //軌道上の位置を取得
                    float orbitPosX = (float) (centerPosX + Math.cos(randAngle) * orbitRadius);
                    float orbitPosZ = (float) (centerPosZ + Math.sin(randAngle) * orbitRadius);
                    //軌道速度(パーティクルの初速度にするもの)を取得
                    float DEFAULT_CENT_FORCE = 0.1f;
                    float orbitSpeed = Mth.sqrt(DEFAULT_CENT_FORCE/orbitRadius)/orbitRadius;
                    float orbitVelX = (float) (Math.cos(randAngle-Math.toRadians(90)) * orbitSpeed);
                    float orbitVelZ = (float) (Math.sin(randAngle-Math.toRadians(90)) * orbitSpeed);
                    FluidSpreadParticles spreadParticle = (FluidSpreadParticles) Minecraft.getInstance().particleEngine.createParticle(
                            ParticleRegistry.FLUID_SPREAD_PARTICLES.get(),
                            orbitPosX,centerPosY-0.1f,orbitPosZ,orbitVelX,0,orbitVelZ
                    );
                    //パーティクルに値を入れる
                    if (spreadParticle != null) {
                        spreadParticle.setColor(color[0],color[1],color[2]);
                    }
                }*/
        }
    }
    // アイテムの環
    private void createItemDiskParticle(Level level, Vec3 pos, ItemStack itemStack, float revolution, float ringRadius, float ringWidth, float centripetalForce, RandomSource randomSource) {
        //パーティクルの数を生成
        int particleAmount = max(2,(int) (ringWidth * ((float) 360 /360) * 4f));//(int) Math.pow(ringRadius,0.9);

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.x;
            float centerPosY = (float) pos.y + 1f/16f;
            float centerPosZ =(float) pos.z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float spreadRandom = (randomSource.nextFloat()-0.5f) * 360;
            float randAngle = revolution + 180 + spreadRandom;
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth * (1 + (2 - Math.abs(spreadRandom/90f)) * (1-(float) 360 /360));
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = max(ringRadius + orbitError, 0);

            //パーティクルに適用する色を液体から用意
            int[] color = getItemRingColor(itemStack,orbitRadius,BlockPos.containing(pos));
            //透明部分は飛ばす
            if (Arrays.equals(color, new int[]{-1, -1, -1})){
                continue;
            }else{

                //パーティクルを生成
                WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                    ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                    centerPosX , centerPosY, centerPosZ , 0, 0, 0
                );

                //パーティクルに値を入れる
                if (ringParticle != null) {

                    if(level!=null&&!level.isClientSide())
                    {
                        if(itemStack.getItem() instanceof BlockItem blockItem){
                            ringParticle.setLightIntensity(blockItem.getBlock().defaultBlockState().getLightEmission());
                        }else {
                            ringParticle.setLightIntensity(itemStack.getItem().isFoil(itemStack)?15:0);
                        }
                    }

                    ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(90),0,randAngle,orbitRadius,centripetalForce);
                    //パーティクルの大きさをリングの端からの距離に合わせる
                    ringParticle.setPixelBasedSize(orbitWidth*0.5f-Math.abs(orbitError));

                    ringParticle.setColor(color[0],color[1],color[2]);
                }
            }
        }
    }

    // 装飾
    private void createAsteroidParticle(Level level, Vec3 pos, float ringRadius, float ringWidth, float centripetalForce, RandomSource randomSource){
        //パーティクルの数を生成
        int particleAmount = max(2,(int) (ringRadius * ringWidth * 0.4f));

        for (int i = 0; i < particleAmount; i++) {
            //位置を用意
            float centerPosX =(float) pos.x;
            float centerPosY = (float) pos.y + 1f/16f;
            float centerPosZ =(float) pos.z;

            //パーティクルに適用する回転オフセットをランダムから用意
            float spreadRandom = (randomSource.nextFloat()-0.5f) * 360;
            //パーティクルに適用する半径をランダムから用意
            float orbitWidth = ringWidth * (1 + (2 - Math.abs(spreadRandom/90f)) * (1-(float) 360 /360));
            float orbitError =
                ((float)Math.pow(randomSource.nextDouble(),1.2d)/2 * (randomSource.nextBoolean()?1:-1))
                    *(1)
                    * orbitWidth;
            float orbitRadius = max(ringRadius + orbitError, 0);

            //パーティクルに適用する色をランダムに決定
            float ColorBase = randomSource.nextInt(128, 255)/255f;
            float[] color = new float[]{ColorBase,ColorBase,1};

            //パーティクルを生成
            WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                centerPosX , centerPosY, centerPosZ , 0, 0, 0
            );

            //パーティクルに値を入れる
            if (ringParticle != null) {
                ringParticle.setLightIntensity(6+(int)(3f*ColorBase));
                ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(-90),0, spreadRandom,orbitRadius,centripetalForce);
                //パーティクルの大きさは最小
                ringParticle.setPixelBasedSize(0.01f);
                ringParticle.setLengthIntensity(0f);

                ringParticle.setColor(color[0],color[1],color[2]);
            }

        }
    }

    private int[] getFluidRingColor(Fluid fluid, float orbitRadius, BlockPos pos) {

        //位置からシード固定のランダムソースを作成
        RandomSource randomSource = RandomSource.create(pos.asLong());

        //液体からスプライトを取得
        TextureAtlasSprite sprite = OrreryCircleBoardBlockEntity.getFluidSprite(fluid);

        //テクスチャのピクセルの色を取得
        int texColor = sprite.getPixelRGBA(0,randomSource.nextInt(0,16),(int)Math.floor(orbitRadius*19f)%16);

        int tintColor = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if(tintColor == -1){
            tintColor = 0xFFFFFF;
        }
        int tintAlpha= FastColor.ABGR32.alpha(texColor);
        float halfTint = (tintAlpha/255f);
        texColor = FastColor.ARGB32.lerp(halfTint,0xFFFFFF,texColor);
        //ティントカラーを適用
        int color = FastColor.ARGB32.multiply(
            FastColor.ABGR32.color(
                255,
                FastColor.ABGR32.red(texColor),
                FastColor.ABGR32.green(texColor),
                FastColor.ABGR32.blue(texColor)
            ),
            tintColor
        );

        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);

        return new int[]{red,green,blue};
    }

    private int[] getItemRingColor(ItemStack itemStack, float orbitRadius, BlockPos pos) {
        //位置からシード固定のランダムソースを作成
        RandomSource randomSource = RandomSource.create(pos.asLong());

        //アイテムからスプライトを取得
        TextureAtlasSprite sprite = getItemSprite(itemStack);

        //テクスチャのピクセルの色を取得
        int texColor = sprite.getPixelRGBA(0, 4+(int)Math.floor(orbitRadius*19)%8, randomSource.nextInt(4,12));
        //ティントを用意
        int tintColor = Minecraft.getInstance().getItemColors().getColor(itemStack,0);
        if(tintColor == -1){
            tintColor = 0xFFFFFF;
        }
        int tintAlpha=FastColor.ABGR32.alpha(texColor);
        //透明度の高い部分は-1を返す(スキップ)
        if(tintAlpha <= 64){
            return new int[]{-1,-1,-1};
        }
        float halfTint = (tintAlpha/255f);
        texColor = FastColor.ARGB32.lerp(halfTint,0x808080,texColor);
        //ティントカラーを適用
        int color = FastColor.ARGB32.multiply(
            texColor,
            FastColor.ABGR32.color(
                255,
                FastColor.ABGR32.red(tintColor),
                FastColor.ABGR32.green(tintColor),
                FastColor.ABGR32.blue(tintColor)
            )
        );

        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);

        return new int[]{255-red,255-green,255-blue};
    }

    private static TextureAtlasSprite getItemSprite(ItemStack itemStack){
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(itemStack).getParticleIcon();
    }
}
