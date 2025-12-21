package com.moromoro.heliopause.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class BlackBoardBlock extends Block {
    public static final IntegerProperty CoordinateX = IntegerProperty.create("coordinate_x",0,32);
    public static final IntegerProperty CoordinateZ = IntegerProperty.create("coordinate_z",0,32);
    public BlackBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(CoordinateX,16).setValue(CoordinateZ,16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(CoordinateX,CoordinateZ);
    }

}
