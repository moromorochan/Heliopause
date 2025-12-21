package com.moromoro.heliopause.EnumProperty;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum WrittenBoardDrawType implements StringRepresentable {
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
            case BLANK_CIRCLE, CROSS_CIRCLE, SQUARE, DIAMOND, DIAMOND_STAR -> 13f/16f;
            case CHILD_NODE -> 7f/16f;
        };
    }

    public static boolean isChildNode(WrittenBoardDrawType type){
        return switch (type){
            case BLANK_CIRCLE, CROSS_CIRCLE, SQUARE, DIAMOND, DIAMOND_STAR -> false;
            case CHILD_NODE -> true;
        };
    }
}
