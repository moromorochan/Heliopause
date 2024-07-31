package com.moromoro.heliopause.blockEntity;

import com.moromoro.ConfigHolder;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.registry.BlockEntityRegistry;
import com.moromoro.heliopause.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

public class FluidSpreaderBlockEntity extends AbstractFluidConcealBlockEntity{
    FluidStack lastFluid=FluidStack.EMPTY;
    public FluidSpreaderBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_SPREADER_BE.get(), pos, state,1000);
        setLastFluid(this.getFluidInTank(0));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        setLastFluid(this.getFluidInTank(0));
    }

    public void setLastFluid(FluidStack lastFluid) {
        this.lastFluid = lastFluid;
    }

    @Override
    public void load(@NonNull CompoundTag nbt) {
        super.load(nbt);
        setLastFluid(this.getFluidInTank(0));
    }

    @NonNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if((side == Direction.UP || side == Direction.DOWN) || side == null){
                return fluidCapability.cast();
            }else{
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCapability.cast();
        }
        return super.getCapability(cap);
    }

    public void tick(Level level, BlockPos pos, BlockState blockState, FluidSpreaderBlockEntity blockEntity) {
        boolean active = hasFluid() && !redStonePowered();
        if(active){
            RandomSource randomSource = RandomSource.createNewThreadLocalInstance();

            createParticle(blockState,level,pos,randomSource);
        }
    }

    private boolean redStonePowered() {
        return false;
    }

    private FluidStack getLastFluid() {
        FluidStack tankFluid = getFluidInTank(0);
        if(!tankFluid.isEmpty()){
            setLastFluid(tankFluid);
            return tankFluid;
        }
        return lastFluid;
    }

    private boolean hasFluid() {
        return !this.getFluidInTank(0).isEmpty();
    }

    //パーティクルの発生
    public void createParticle(BlockState blockState, Level level, BlockPos pos, RandomSource randomSource) {
        //ブロックエンティティを取得
        BlockEntity blockEntity=level.getBlockEntity(pos);
        if(blockEntity instanceof FluidSpreaderBlockEntity){

            //パーティクルの数を生成
            int particleAmount = randomSource.nextInt(1,4);

            for (int i = 0; i < particleAmount; i++) {
                //位置を用意
                float posX =(float) pos.getCenter().x;
                float posY = (float) ( pos.getY() +(4f/16f)+(Math.pow(randomSource.nextFloat(),0.25f)*(8f/16f)));
                float posZ =(float) pos.getCenter().z;
                //パーティクルを発生させるベクトルを用意
                double randAngle = Math.toRadians(randomSource.nextFloat() * 360f);
                double squareRange = (8f/16f);
                //最終的に到達する半径
                double maxReach = 2f;
                //パーティクルの性質から初速を逆算
                double randRange = Math.pow(randomSource.nextFloat(),0.35f) * 0.145d* (maxReach+0.68d-squareRange);
                double randPitch = randomSource.nextFloat() * 0.125d;
                double randSin = Math.sin(randAngle);
                double randCos = Math.cos(randAngle);
                Vec3 randVec = new Vec3(randRange * randSin, randPitch, randRange * randCos);

                //スポーン位置を円形から四角形に成形
                double randX, randZ;
                if (Math.abs(randCos) > Math.abs(randSin)) {
                    randX = squareRange * randSin / Math.abs(randCos);
                    randZ = squareRange * Math.signum(randCos);
                } else {
                    randX = squareRange * Math.signum(randSin);
                    randZ = squareRange * randCos / Math.abs(randSin);
                }
                posX+= (float) randX;
                posZ+= (float) randZ;

                //パーティクルを生成
                //パーティクルに適用する色を液体から用意
                int[] color = getFluidColor(getFluidInTank(0).getFluid());

                Particle particle = Minecraft.getInstance().particleEngine.createParticle(getSpreaderParticle(), posX, posY, posZ, randVec.x, randVec.y, randVec.z);
                particle.setColor(color[0],color[1],color[2]);
            }
        }
    }

    private ParticleOptions getSpreaderParticle() {
        return ParticleRegistry.FLUID_SPREAD_PARTICLES.get();
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
}