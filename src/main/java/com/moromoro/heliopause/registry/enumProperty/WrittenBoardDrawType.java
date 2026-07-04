package com.moromoro.heliopause.registry.enumProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WrittenBoardDrawType implements StringRepresentable {
    LARGE_SYMBOL,
    BLANK_CIRCLE,
    CROSS_CIRCLE,
    SQUARE,
    DIAMOND,
    DIAMOND_STAR,
    CHILD_NODE;

    public static EnumProperty<WrittenBoardDrawType> create(String drawType, Class<WrittenBoardDrawType> writtenBoardDrawTypeClass){
        return EnumProperty.create(drawType, writtenBoardDrawTypeClass);
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static double getNodeSize(WrittenBoardDrawType type){
        return switch (type){
            case LARGE_SYMBOL -> 31f/16f;
            case BLANK_CIRCLE, CROSS_CIRCLE, SQUARE, DIAMOND, DIAMOND_STAR -> 13f/16f;
            case CHILD_NODE -> 7f/16f;
        };
    }

    public static boolean isLargeNode(WrittenBoardDrawType type){
        return (type == LARGE_SYMBOL);
    }

    public static boolean isChildNode(WrittenBoardDrawType type){
        return (type == CHILD_NODE);
    }
}
