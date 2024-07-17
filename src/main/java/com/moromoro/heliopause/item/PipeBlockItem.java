package com.moromoro.heliopause.item;

import com.moromoro.Heliopause;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.moromoro.heliopause.block.AbstractNetworkBlock.*;

public class PipeBlockItem extends BlockItem {
    private final int diameter;
    private final String key;

    public PipeBlockItem(Block block, int pipeDiameter, Properties properties,String translationKey) {
        super(block, properties);
        this.diameter = pipeDiameter;
        key = translationKey;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Heliopause.LOGGER.debug("State on Item");
        BlockState variedState = state
                .setValue( NORTH, state.getValue(NORTH)>0 ? this.diameter:0 )
                .setValue( EAST, state.getValue(EAST)>0 ? this.diameter:0 )
                .setValue( SOUTH, state.getValue(SOUTH)>0 ? this.diameter:0 )
                .setValue( WEST, state.getValue(WEST)>0 ? this.diameter:0 )
                .setValue( UP, state.getValue(UP)>0 ? this.diameter:0 )
                .setValue( DOWN, state.getValue(DOWN)>0 ? this.diameter:0 )
                .setValue(CENTER,this.diameter);
        return super.placeBlock(context, variedState);
    }

    @Override
    public String getDescriptionId() {
        return "item." + Heliopause.MODID + "." +this.key;
    }
}
