package com.moromoro.heliopause.registry.enumProperty;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ImitationStellarModelValue implements StringRepresentable {
    ROCKY,
    GAS_STRIPES,
    GAS_PLAIN,
    FIXED_STAR,
    COLLAPSAR;

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }

    public static ImitationStellarModelValue fromString(String string) {
        if (string == null) return null;

        for (ImitationStellarModelValue value : values()) {
            if (value.getSerializedName().equals(string)) {
                return value;
            }
        }
        return null;
    }

}
