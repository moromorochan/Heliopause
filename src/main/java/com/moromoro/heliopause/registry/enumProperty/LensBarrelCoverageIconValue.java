package com.moromoro.heliopause.registry.enumProperty;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum LensBarrelCoverageIconValue implements StringRepresentable {
    CIRCLE,
    DOUBLE_CIRCLE,
    DIAMOND;

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static LensBarrelCoverageIconValue fromString(String string) {
        if (string == null) return null;

        for (LensBarrelCoverageIconValue value : values()) {
            if (value.getSerializedName().equals(string)) {
                return value;
            }
        }
        return null;
    }

}