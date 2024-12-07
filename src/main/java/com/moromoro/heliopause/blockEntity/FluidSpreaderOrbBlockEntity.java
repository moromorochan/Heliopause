package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.particle.FluidSpreadParticles;
import com.moromoro.heliopause.particle.WhirlRingParticles;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import com.moromoro.heliopause.render.FluidSpreaderOrbBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidSpreaderOrbBlockEntity extends AbstractFluidOrbBlockEntity{

    //アニメーションの滑らかな描画用変数
    //private float smoothedRingDensity;
    //リングの外径(幅は内側ギリギリまでで自動生成)
    private float ringRadius=4.0f;
    //内側どこまで寄せるか
    protected final float innerRadius=Math.max(0.6f,ringRadius-1.8f);

    public FluidSpreaderOrbBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_ORB_BE.get(), pos, state, 1024);
    }

    @Override
    protected void updateRenderData() {
        FluidSpreaderOrbBlockRenderer.updateData(this.getBlockPos(), mainTank.getFluid());
    }

    @Override
    protected void removeRenderData() {
        FluidSpreaderOrbBlockRenderer.removeData(this.getBlockPos());
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidSpreaderOrbBlockEntity blockEntity) {
        boolean active = hasFluid() && !redStonePowered();
        if (active) {
            //smoothedRingDensity+=0.1f;
            RandomSource randomSource = RandomSource.createNewThreadLocalInstance();
            createParticle(blockState, level, pos, randomSource);
            //降水量25mmイメージ 20平方メートル・マイクラ内2時間で1B消費する確率
            if (randomSource.nextDouble() < 10 / 100f) {
                //this.mainTank.drain(5, FluidAction.EXECUTE);
            }
        }else {
            //smoothedRingDensity-=0.1f;
        }
    }

    private boolean redStonePowered() {
        return false;
    }

    private boolean hasFluid() {
        return !this.getFluidInTank(0).isEmpty();
    }

    private void createParticle(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        //ブロックエンティティを取得
        BlockEntity blockEntity= level.getBlockEntity(pos);
        if(blockEntity instanceof FluidSpreaderOrbBlockEntity){

            //パーティクルの数を生成
            int particleAmount = randomSource.nextInt( (int)(0.5*ringRadius),  (int)(2*ringRadius));

            for (int i = 0; i < particleAmount; i++) {
                //位置を用意
                float centerPosX =(float) pos.getCenter().x;
                float centerPosY = (float) pos.getCenter().y + 1f/16f;//+randomSource.nextFloat()*0.04f;//( pos.getY() +(8f/16f)-orbRadius + (Math.pow(randomSource.nextFloat(),0.25f)*2*orbRadius));
                float centerPosZ =(float) pos.getCenter().z;

                //パーティクルに適用する回転オフセットをランダムから用意
                float randAngle = (float) Math.toRadians(randomSource.nextFloat()*360);
                //パーティクルに適用する半径をランダムから用意
                float orbitWidth = ringRadius-innerRadius;
                float orbitError =
                        ((float)Math.pow(randomSource.nextDouble(),0.8d)/2 * (randomSource.nextBoolean()?1:-1))
                                *(1)
                                * orbitWidth;
                float orbitRadius = innerRadius + (ringRadius/2) + orbitError;

                //パーティクルに適用する色を液体から用意
                int[] color = getFluidRingColor(getFluidInTank(0).getFluid(),orbitRadius,pos);//getFluidColor(getFluidInTank(0).getFluid());

                //パーティクルを生成
                WhirlRingParticles ringParticle = (WhirlRingParticles) Minecraft.getInstance().particleEngine.createParticle(
                        ParticleRegistry.WHIRL_RING_PARTICLES.get(),
                        centerPosX , centerPosY, centerPosZ , 0, 0, 0
                );

                //パーティクルに値を入れる
                if (ringParticle != null) {

                    if(this.level!=null&&!this.level.isClientSide())
                    {
                        ringParticle.setLightIntensity(getFluidInTank(0).getFluid().getFluidType().getLightLevel());
                    }

                    ringParticle.setOrbitalElements(new Vec3(centerPosX,centerPosY,centerPosZ),(float) Math.toRadians(-90),0,randAngle,orbitRadius,1);
                    //パーティクルの大きさをリングの端からの距離に合わせる
                    ringParticle.setPixelBasedSize(orbitWidth*0.5f-Math.abs(orbitError));

                    ringParticle.setColor(color[0],color[1],color[2]);
                }

                //信号が入っていない場合、散布パーティクルを生成する
                if(!redStonePowered()){
                    //軌道上の位置を取得
                    float orbitPosX = (float) (centerPosX + Math.cos(randAngle) * orbitRadius);
                    float orbitPosZ = (float) (centerPosZ + Math.sin(randAngle) * orbitRadius);
                    //軌道速度(パーティクルの初速度にするもの)を取得
                    float centripetalForce = 0.1f; //TODO パーティクルと向心力の値を揃える仕組みを用意する
                    float orbitSpeed = Mth.sqrt(centripetalForce/orbitRadius)/orbitRadius;
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
                }
            }
        }
    }

    private int[] getFluidRingColor(Fluid fluid, float orbitRadius, BlockPos pos) {

        //位置からシード固定のランダムソースを作成
        RandomSource randomSource = RandomSource.create(pos.asLong());

        //液体からスプライトを取得
        TextureAtlasSprite sprite = getFluidSprite(fluid);

        //テクスチャのピクセルの色を取得
        int texColor = sprite.getPixelRGBA(0,randomSource.nextInt(0,15),(int)Math.floor(orbitRadius*8f)%16);

        //ティントカラーを適用
        int color = FastColor.ARGB32.multiply(
                FastColor.ABGR32.color(
                        255,
                        FastColor.ABGR32.red(texColor),
                        FastColor.ABGR32.green(texColor),
                        FastColor.ABGR32.blue(texColor)
                ),
                IClientFluidTypeExtensions.of(fluid).getTintColor()
        );

        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);

        return new int[]{red,green,blue};
    }

    //液体の色を取得
    private int[] getFluidColor(Fluid fluid) {
        //液体のidを取得
        String fluidName = ForgeRegistries.FLUIDS.getKey(fluid).toString();
        if(ConfigHolder.FLUID_COLORS.containsKey(fluidName)){
            int cfgColor = ConfigHolder.FLUID_COLORS.get(fluidName).get();
            float[] cfgRGB =
                    new float[]{
                            FastColor.ARGB32.red(cfgColor),
                            FastColor.ARGB32.green(cfgColor),
                            FastColor.ARGB32.blue(cfgColor)
                    };
            return new int[]{(int)(cfgRGB[0]*255),(int)(cfgRGB[1]*255),(int)(cfgRGB[2]*255)};
        }
        //configに無ければ、テクスチャから生成

        //液体からスプライトを取得
        TextureAtlasSprite sprite = getFluidSprite(fluid);

        //テクスチャのピクセルの色とティントカラーを取得
        int texColor = sprite.getPixelRGBA(0, 7, 7);
        float[] texRGB = new float[]{FastColor.ABGR32.red(texColor),FastColor.ABGR32.green(texColor),FastColor.ABGR32.blue(texColor)};

        //乗算
        float red = texRGB[0];
        float green = texRGB[1];
        float blue = texRGB[2];

        return new int[]{(int)(red*255),(int)(green*255),(int)(blue*255)};
    }

    private TextureAtlasSprite getFluidSprite(Fluid fluid) {
        ResourceLocation fluidTexture = IClientFluidTypeExtensions.of(fluid).getStillTexture(new FluidStack(fluid,10));
        //例外処理
        if (fluidTexture == null) {
            Heliopause.LOGGER.debug("Rendering failure on getting Fluid Sprite.");
            // テクスチャが存在しない場合はエラーテクスチャを返す
            return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(new ResourceLocation("minecraft", "missing_texture"));
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(fluidTexture);
    }
/*
    @Override
    public void onLoad() {
        super.onLoad();
        if (getFluidInTank(0).isEmpty()) {
            setSmoothedRingDensity(0f);
        } else {
            setSmoothedRingDensity(1f);
        }
    }

    //データの読み込み
    @Override
    public void load(@NonNull CompoundTag nbt){
        super.load(nbt);
        if (getFluidInTank(0).isEmpty()) {
            setSmoothedRingDensity(0f);
        } else {
            setSmoothedRingDensity(1f);
        }
    }

    public void setSmoothedRingDensity(float density) {
        smoothedRingDensity = Mth.clamp(0f,1f,density);
    }
    public float getSmoothedRingDensity() {
        return smoothedRingDensity;
    }*/
}