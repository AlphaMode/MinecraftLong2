package ca.spottedleaf.starlight.common.util;

import net.minecraft.world.level.LevelHeightAccessor;

public final class WorldUtil {

    // min, max are inclusive

    public static long getMaxSection(final LevelHeightAccessor world) {
        return world.getMaxSection() - 1; // getMaxSection() is exclusive
    }

    public static long getMinSection(final LevelHeightAccessor world) {
        return world.getMinSection();
    }

    public static long getMaxLightSection(final LevelHeightAccessor world) {
        return getMaxSection(world) + 1;
    }

    public static long getMinLightSection(final LevelHeightAccessor world) {
        return getMinSection(world) - 1;
    }



    public static long getTotalSections(final LevelHeightAccessor world) {
        return getMaxSection(world) - getMinSection(world) + 1;
    }

    public static long getTotalLightSections(final LevelHeightAccessor world) {
        return getMaxLightSection(world) - getMinLightSection(world) + 1;
    }

    public static long getMinBlockY(final LevelHeightAccessor world) {
        return getMinSection(world) << 4;
    }

    public static long getMaxBlockY(final LevelHeightAccessor world) {
        return (getMaxSection(world) << 4) | 15;
    }

    private WorldUtil() {
        throw new RuntimeException();
    }

}
