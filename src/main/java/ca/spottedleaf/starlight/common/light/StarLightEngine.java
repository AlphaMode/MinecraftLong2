package ca.spottedleaf.starlight.common.light;

import ca.spottedleaf.starlight.common.blockstate.ExtendedAbstractBlockState;
import ca.spottedleaf.starlight.common.util.CoordinateUtils;
import ca.spottedleaf.starlight.common.util.IntegerUtil;
import ca.spottedleaf.starlight.common.util.WorldUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public abstract class StarLightEngine {

    protected static final BlockState AIR_BLOCK_STATE = Blocks.AIR.defaultBlockState();

    protected static final AxisDirection[] DIRECTIONS = AxisDirection.values();
    protected static final AxisDirection[] AXIS_DIRECTIONS = DIRECTIONS;
    protected static final AxisDirection[] ONLY_HORIZONTAL_DIRECTIONS = new AxisDirection[] {
            AxisDirection.POSITIVE_X, AxisDirection.NEGATIVE_X,
            AxisDirection.POSITIVE_Z, AxisDirection.NEGATIVE_Z
    };

    protected static enum AxisDirection {

        // Declaration order is important and relied upon. Do not change without modifying propagation code.
        POSITIVE_X(1, 0, 0), NEGATIVE_X(-1, 0, 0),
        POSITIVE_Z(0, 0, 1), NEGATIVE_Z(0, 0, -1),
        POSITIVE_Y(0, 1, 0), NEGATIVE_Y(0, -1, 0);

        static {
            POSITIVE_X.opposite = NEGATIVE_X; NEGATIVE_X.opposite = POSITIVE_X;
            POSITIVE_Z.opposite = NEGATIVE_Z; NEGATIVE_Z.opposite = POSITIVE_Z;
            POSITIVE_Y.opposite = NEGATIVE_Y; NEGATIVE_Y.opposite = POSITIVE_Y;
        }

        protected AxisDirection opposite;

        public final int x;
        public final int y;
        public final int z;
        public final Direction nms;
        public final long everythingButThisDirection;
        public final long everythingButTheOppositeDirection;

        AxisDirection(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nms = Direction.fromNormal(x, y, z);
            this.everythingButThisDirection = (long)(ALL_DIRECTIONS_BITSET ^ (1 << this.ordinal()));
            // positive is always even, negative is always odd. Flip the 1 bit to get the negative direction.
            this.everythingButTheOppositeDirection = (long)(ALL_DIRECTIONS_BITSET ^ (1 << (this.ordinal() ^ 1)));
        }

        public AxisDirection getOpposite() {
            return this.opposite;
        }
    }

    // I'd like to thank https://www.seedofandromeda.com/blogs/29-fast-flood-fill-lighting-in-a-blocky-voxel-game-pt-1
    // for explaining how light propagates via breadth-first search

    // While the above is a good start to understanding the general idea of what the general principles are, it's not
    // exactly how the vanilla light engine should behave for minecraft.

    // similar to the above, except the chunk section indices vary from [-1, 1], or [0, 2]
    // for the y chunk section it's from [minLightSection, maxLightSection] or [0, maxLightSection - minLightSection]
    // index = x + (z * 5) + (y * 25)
    // null index indicates the chunk section doesn't exist (empty or out of bounds)
    protected final Long2ObjectMap<LevelChunkSection> sectionCache;

    // the exact same as above, except for storing fast access to SWMRNibbleArray
    // for the y chunk section it's from [minLightSection, maxLightSection] or [0, maxLightSection - minLightSection]
    // index = x + (z * 5) + (y * 25)
    protected final Long2ObjectMap<SWMRNibbleArray> nibbleCache;

    // the exact same as above, except for storing fast access to nibbles to call change callbacks for
    // for the y chunk section it's from [minLightSection, maxLightSection] or [0, maxLightSection - minLightSection]
    // index = x + (z * 5) + (y * 25)
    protected final Long2ObjectMap<Boolean> notifyUpdateCache;

    // always initialsed during start of lighting.
    // index = x + (z * 5)
    protected final Long2ObjectMap<ChunkAccess> chunkCache = new Long2ObjectOpenHashMap<>(5 * 5);

    // index = x + (z * 5)
    protected final Long2ObjectMap<boolean[]> emptinessMapCache = new Long2ObjectOpenHashMap<>(5 * 5);

    protected final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
    protected final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();
    protected final BlockPos.MutableBlockPos mutablePos3 = new BlockPos.MutableBlockPos();

    protected long encodeOffsetX;
    protected long encodeOffsetY;
    protected long encodeOffsetZ;

    protected long coordinateOffset;

    protected long chunkOffsetX;
    protected long chunkOffsetY;
    protected long chunkOffsetZ;

    protected long chunkIndexOffset;
    protected long chunkSectionIndexOffset;

    protected final boolean skylightPropagator;
    protected final int emittedLightMask;
    protected final boolean isClientSide;

    protected final Level world;
    protected final int minLightSection;
    protected final int maxLightSection;
    protected final int minSection;
    protected final int maxSection;

    protected StarLightEngine(final boolean skylightPropagator, final Level world) {
        this.skylightPropagator = skylightPropagator;
        this.emittedLightMask = skylightPropagator ? 0 : 0xF;
        this.isClientSide = world.isClientSide;
        this.world = world;
        this.minLightSection = (int) WorldUtil.getMinLightSection(world);
        this.maxLightSection = (int) WorldUtil.getMaxLightSection(world);
        this.minSection = (int) WorldUtil.getMinSection(world);
        this.maxSection = (int) WorldUtil.getMaxSection(world);

        this.sectionCache = new Long2ObjectOpenHashMap<>(5 * 5 * ((this.maxLightSection - this.minLightSection + 1) + 2)); // add two extra sections for buffer
        this.nibbleCache = new Long2ObjectOpenHashMap<>(5 * 5 * ((this.maxLightSection - this.minLightSection + 1) + 2)); // add two extra sections for buffer
        this.notifyUpdateCache = new Long2ObjectOpenHashMap<>(5 * 5 * ((this.maxLightSection - this.minLightSection + 1) + 2)); // add two extra sections for buffer
        this.notifyUpdateCache.defaultReturnValue(false);
    }

    protected final void setupEncodeOffset(final long centerX, final long centerY, final long centerZ) {
        // 31 = center + encodeOffset
        this.encodeOffsetX = 31 - centerX;
        this.encodeOffsetY = (-(this.minLightSection - 1) << 4); // we want 0 to be the smallest encoded value
        this.encodeOffsetZ = 31 - centerZ;

        // coordinateIndex = x | (z << 6) | (y << 12)
        this.coordinateOffset = this.encodeOffsetX + (this.encodeOffsetZ << 6) + (this.encodeOffsetY << 12);

        // 2 = (centerX >> 4) + chunkOffset
        this.chunkOffsetX = 2 - (centerX >> 4);
        this.chunkOffsetY = -(this.minLightSection - 1); // lowest should be 0
        this.chunkOffsetZ = 2 - (centerZ >> 4);

        // chunk index = x + (5 * z)
        this.chunkIndexOffset = this.chunkOffsetX + (5 * this.chunkOffsetZ);

        // chunk section index = x + (5 * z) + ((5*5) * y)
        this.chunkSectionIndexOffset = this.chunkIndexOffset + ((5 * 5) * this.chunkOffsetY);
    }

    protected final void setupCaches(final LightChunkGetter chunkProvider, final long centerX, final long centerY, final long centerZ,
                                     final boolean relaxed, final boolean tryToLoadChunksFor2Radius) {
        final long centerChunkX = centerX >> 4;
        final long centerChunkY = centerY >> 4;
        final long centerChunkZ = centerZ >> 4;

        this.setupEncodeOffset(centerChunkX * 16 + 7, centerChunkY * 16 + 7, centerChunkZ * 16 + 7);

        final int radius = tryToLoadChunksFor2Radius ? 2 : 1;

        for (int dz = -radius; dz <= radius; ++dz) {
            for (int dx = -radius; dx <= radius; ++dx) {
                final long cx = centerChunkX + dx;
                final long cz = centerChunkZ + dz;
                final boolean isTwoRadius = Math.max(IntegerUtil.branchlessAbs(dx), IntegerUtil.branchlessAbs(dz)) == 2;
                final ChunkAccess chunk = (ChunkAccess)chunkProvider.getChunkForLighting(cx, cz);

                if (chunk == null) {
                    if (relaxed | isTwoRadius) {
                        continue;
                    }
                    throw new IllegalArgumentException("Trying to propagate light update before 1 radius neighbours ready");
                }

                if (!this.canUseChunk(chunk)) {
                    continue;
                }

                this.setChunkInCache(cx, cz, chunk);
                this.setEmptinessMapCache(cx, cz, this.getEmptinessMap(chunk));
                if (!isTwoRadius) {
                    this.setBlocksForChunkInCache(cx, cz, chunk.getSections());
                    this.setNibblesForChunkInCache(cx, cz, this.getNibblesOnChunk(chunk));
                }
            }
        }
    }

    protected final ChunkAccess getChunkInCache(final long chunkX, final long chunkZ) {
        return this.chunkCache.get(chunkX + 5*chunkZ + this.chunkIndexOffset);
    }

    protected final void setChunkInCache(final long chunkX, final long chunkZ, final ChunkAccess chunk) {
        this.chunkCache.put(chunkX + 5*chunkZ + this.chunkIndexOffset, chunk);
    }

    protected final LevelChunkSection getChunkSection(final long chunkX, final long chunkY, final long chunkZ) {
        return this.sectionCache.get(chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset);
    }

    protected final void setChunkSectionInCache(final long chunkX, final long chunkY, final long chunkZ, final LevelChunkSection section) {
        this.sectionCache.put(chunkX + 5*chunkZ + 5*5*chunkY + this.chunkSectionIndexOffset, section);
    }

    protected final void setBlocksForChunkInCache(final long chunkX, final long chunkZ, final LevelChunkSection[] sections) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setChunkSectionInCache(chunkX, cy, chunkZ,
                    sections == null ? null : (cy >= this.minSection && cy <= this.maxSection ? sections[cy - this.minSection] : null));
        }
    }

    protected final SWMRNibbleArray getNibbleFromCache(final long chunkX, final long chunkY, final long chunkZ) {
        return this.nibbleCache.get(chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset);
    }

    protected final SWMRNibbleArray[] getNibblesForChunkFromCache(final int chunkX, final int chunkZ) {
        final SWMRNibbleArray[] ret = new SWMRNibbleArray[this.maxLightSection - this.minLightSection + 1];

        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            ret[cy - this.minLightSection] = this.nibbleCache.get(chunkX + 5*chunkZ + (cy * (5 * 5)) + this.chunkSectionIndexOffset);
        }

        return ret;
    }

    protected final void setNibbleInCache(final long chunkX, final long chunkY, final long chunkZ, final SWMRNibbleArray nibble) {
        this.nibbleCache.put(chunkX + 5*chunkZ + (5 * 5) * chunkY + this.chunkSectionIndexOffset, nibble);
    }

    protected final void setNibblesForChunkInCache(final long chunkX, final long chunkZ, final SWMRNibbleArray[] nibbles) {
        for (int cy = this.minLightSection; cy <= this.maxLightSection; ++cy) {
            this.setNibbleInCache(chunkX, cy, chunkZ, nibbles == null ? null : nibbles[cy - this.minLightSection]);
        }
    }

    protected final void updateVisible(final LightChunkGetter lightAccess) {
        for (int index = 0, max = this.nibbleCache.size(); index < max; ++index) {
            final SWMRNibbleArray nibble = this.nibbleCache.get(index);
            if (!this.notifyUpdateCache.get(index) && (nibble == null || !nibble.isDirty())) {
                continue;
            }

            final long chunkX = (index % 5) - this.chunkOffsetX;
            final long chunkZ = ((index / 5) % 5) - this.chunkOffsetZ;
            final int ySections = this.maxSection - this.minSection + 1;
            final long chunkY = ((index / (5*5)) % (ySections + 2 + 2)) - this.chunkOffsetY;
            if ((nibble != null && nibble.updateVisible()) || this.notifyUpdateCache.get(index)) {
                lightAccess.onLightUpdate(this.skylightPropagator ? LightLayer.SKY : LightLayer.BLOCK, SectionPos.of(chunkX, chunkY, chunkZ));
            }
        }
    }

    protected final void destroyCaches() {
        this.sectionCache.clear();
        this.nibbleCache.clear();
        this.chunkCache.clear();
        this.emptinessMapCache.clear();
        if (this.isClientSide) {
            this.notifyUpdateCache.clear();
        }
    }

    protected final BlockState getBlockState(final long worldX, final long worldY, final long worldZ) {
        final LevelChunkSection section = this.sectionCache.get((worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset);

        if (section != null) {
            return section.hasOnlyAir() ? AIR_BLOCK_STATE : section.getBlockState(worldX & 15, worldY & 15, worldZ & 15);
        }

        return AIR_BLOCK_STATE;
    }

    protected final BlockState getBlockState(final long sectionIndex, final long localIndex) {
        final LevelChunkSection section = this.sectionCache.get(sectionIndex);

        if (section != null) {
            return section.hasOnlyAir() ? AIR_BLOCK_STATE : section.getStates().get(localIndex);
        }

        return AIR_BLOCK_STATE;
    }

    protected final int getLightLevel(final long worldX, final long worldY, final long worldZ) {
        final SWMRNibbleArray nibble = this.nibbleCache.get((worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset);

        return nibble == null ? 0 : nibble.getUpdating((int) ((worldX & 15) | ((worldZ & 15) << 4) | ((worldY & 15) << 8)));
    }

    protected final int getLightLevel(final long sectionIndex, final long localIndex) {
        final SWMRNibbleArray nibble = this.nibbleCache.get(sectionIndex);

        return nibble == null ? 0 : nibble.getUpdating((int) localIndex);
    }

    protected final void setLightLevel(final long worldX, final long worldY, final long worldZ, final int level) {
        final long sectionIndex = (worldX >> 4) + 5 * (worldZ >> 4) + (5 * 5) * (worldY >> 4) + this.chunkSectionIndexOffset;
        final SWMRNibbleArray nibble = this.nibbleCache.get(sectionIndex);

        if (nibble != null) {
            nibble.set((int) ((worldX & 15) | ((worldZ & 15) << 4) | ((worldY & 15) << 8)), level);
            if (this.isClientSide) {
                long cx1 = (worldX - 1) >> 4;
                long cx2 = (worldX + 1) >> 4;
                long cy1 = (worldY - 1) >> 4;
                long cy2 = (worldY + 1) >> 4;
                long cz1 = (worldZ - 1) >> 4;
                long cz2 = (worldZ + 1) >> 4;
                for (long x = cx1; x <= cx2; ++x) {
                    for (long y = cy1; y <= cy2; ++y) {
                        for (long z = cz1; z <= cz2; ++z) {
                            this.notifyUpdateCache.put(x + 5 * z + (5 * 5) * y + this.chunkSectionIndexOffset, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }

    protected final void postLightUpdate(final long worldX, final long worldY, final long worldZ) {
        if (this.isClientSide) {
            long cx1 = (worldX - 1) >> 4;
            long cx2 = (worldX + 1) >> 4;
            long cy1 = (worldY - 1) >> 4;
            long cy2 = (worldY + 1) >> 4;
            long cz1 = (worldZ - 1) >> 4;
            long cz2 = (worldZ + 1) >> 4;
            for (long x = cx1; x <= cx2; ++x) {
                for (long y = cy1; y <= cy2; ++y) {
                    for (long z = cz1; z <= cz2; ++z) {
                        this.notifyUpdateCache.put(x + (5 * z) + (5 * 5 * y) + this.chunkSectionIndexOffset, Boolean.TRUE);
                    }
                }
            }
        }
    }

    protected final void setLightLevel(final long sectionIndex, final int localIndex, final int worldX, final int worldY, final int worldZ, final int level) {
        final SWMRNibbleArray nibble = this.nibbleCache.get(sectionIndex);

        if (nibble != null) {
            nibble.set(localIndex, level);
            if (this.isClientSide) {
                int cx1 = (worldX - 1) >> 4;
                int cx2 = (worldX + 1) >> 4;
                int cy1 = (worldY - 1) >> 4;
                int cy2 = (worldY + 1) >> 4;
                int cz1 = (worldZ - 1) >> 4;
                int cz2 = (worldZ + 1) >> 4;
                for (int x = cx1; x <= cx2; ++x) {
                    for (int y = cy1; y <= cy2; ++y) {
                        for (int z = cz1; z <= cz2; ++z) {
                            this.notifyUpdateCache.put(x + (5 * z) + (5 * 5 * y) + this.chunkSectionIndexOffset, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }

    protected final boolean[] getEmptinessMap(final long chunkX, final long chunkZ) {
        return this.emptinessMapCache.get(chunkX + 5*chunkZ + this.chunkIndexOffset);
    }

    protected final void setEmptinessMapCache(final long chunkX, final long chunkZ, final boolean[] emptinessMap) {
        this.emptinessMapCache.put(chunkX + 5*chunkZ + this.chunkIndexOffset, emptinessMap);
    }

    public static SWMRNibbleArray[] getFilledEmptyLight(final LevelHeightAccessor world) {
        return getFilledEmptyLight((int) WorldUtil.getTotalLightSections(world));
    }

    private static SWMRNibbleArray[] getFilledEmptyLight(final int totalLightSections) {
        final SWMRNibbleArray[] ret = new SWMRNibbleArray[totalLightSections];

        for (int i = 0, len = ret.length; i < len; ++i) {
            ret[i] = new SWMRNibbleArray(null, true);
        }

        return ret;
    }

    protected abstract boolean[] getEmptinessMap(final ChunkAccess chunk);

    protected abstract void setEmptinessMap(final ChunkAccess chunk, final boolean[] to);

    protected abstract SWMRNibbleArray[] getNibblesOnChunk(final ChunkAccess chunk);

    protected abstract void setNibbles(final ChunkAccess chunk, final SWMRNibbleArray[] to);

    protected abstract boolean canUseChunk(final ChunkAccess chunk);

    public final void blocksChangedInChunk(final LightChunkGetter lightAccess, final int chunkX, final int chunkZ,
                                           final Set<BlockPos> positions, final Boolean[] changedSections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, true);
        try {
            final ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            if (changedSections != null) {
                final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, changedSections, false);
                if (ret != null) {
                    this.setEmptinessMap(chunk, ret);
                }
            }
            if (!positions.isEmpty()) {
                this.propagateBlockChanges(lightAccess, chunk, positions);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    protected abstract void propagateBlockChanges(final LightChunkGetter lightAccess, final ChunkAccess atChunk, final Set<BlockPos> positions);

    protected abstract void checkBlock(final LightChunkGetter lightAccess, final long worldX, final long worldY, final long worldZ);

    // if ret > expect, then the real value is at least ret (early returns if ret > expect, rather than calculating actual)
    // if ret == expect, then expect is the correct light value for pos
    // if ret < expect, then ret is the real light value
    protected abstract int calculateLightValue(final LightChunkGetter lightAccess, final long worldX, final long worldY, final long worldZ,
                                               final int expect);

    protected final long[] chunkCheckDelayedUpdatesCenter = new long[16 * 16];
    protected final long[] chunkCheckDelayedUpdatesNeighbour = new long[16 * 16];

    protected void checkChunkEdge(final LightChunkGetter lightAccess, final ChunkAccess chunk,
                                  final long chunkX, final long chunkY, final long chunkZ) {
        final SWMRNibbleArray currNibble = this.getNibbleFromCache(chunkX, chunkY, chunkZ);
        if (currNibble == null) {
            return;
        }

        for (final AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
            final int neighbourOffX = direction.x;
            final int neighbourOffZ = direction.z;

            final SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX,
                    chunkY, chunkZ + neighbourOffZ);

            if (neighbourNibble == null) {
                continue;
            }

            if (!currNibble.isInitialisedUpdating() && !neighbourNibble.isInitialisedUpdating()) {
                // both are zero, nothing to check.
                continue;
            }

            // this chunk
            final int incX;
            final int incZ;
            final long startX;
            final long startZ;

            if (neighbourOffX != 0) {
                // x direction
                incX = 0;
                incZ = 1;

                if (direction.x < 0) {
                    // negative
                    startX = chunkX << 4;
                } else {
                    startX = chunkX << 4 | 15;
                }
                startZ = chunkZ << 4;
            } else {
                // z direction
                incX = 1;
                incZ = 0;

                if (neighbourOffZ < 0) {
                    // negative
                    startZ = chunkZ << 4;
                } else {
                    startZ = chunkZ << 4 | 15;
                }
                startX = chunkX << 4;
            }

            int centerDelayedChecks = 0;
            int neighbourDelayedChecks = 0;
            for (long currY = chunkY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                for (long i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                    final long neighbourX = currX + neighbourOffX;
                    final long neighbourZ = currZ + neighbourOffZ;

                    final long currentIndex = (currX & 15) |
                            ((currZ & 15)) << 4 |
                            ((currY & 15) << 8);
                    final int currentLevel = currNibble.getUpdating((int) currentIndex);

                    final long neighbourIndex =
                            (neighbourX & 15) |
                            ((neighbourZ & 15)) << 4 |
                            ((currY & 15) << 8);
                    final int neighbourLevel = neighbourNibble.getUpdating((int) neighbourIndex);

                    // the checks are delayed because the checkBlock method clobbers light values - which then
                    // affect later calculate light value operations. While they don't affect it in a behaviourly significant
                    // way, they do have a negative performance impact due to simply queueing more values

                    if (this.calculateLightValue(lightAccess, currX, currY, currZ, currentLevel) != currentLevel) {
                        this.chunkCheckDelayedUpdatesCenter[centerDelayedChecks++] = currentIndex;
                    }

                    if (this.calculateLightValue(lightAccess, neighbourX, currY, neighbourZ, neighbourLevel) != neighbourLevel) {
                        this.chunkCheckDelayedUpdatesNeighbour[neighbourDelayedChecks++] = neighbourIndex;
                    }
                }
            }

            final long currentChunkOffX = chunkX << 4;
            final long currentChunkOffZ = chunkZ << 4;
            final long neighbourChunkOffX = (chunkX + direction.x) << 4;
            final long neighbourChunkOffZ = (chunkZ + direction.z) << 4;
            final long chunkOffY = chunkY << 4;
            for (int i = 0, len = Math.max(centerDelayedChecks, neighbourDelayedChecks); i < len; ++i) {
                // try to queue neighbouring data together
                // index = x | (z << 4) | (y << 8)
                if (i < centerDelayedChecks) {
                    final long value = this.chunkCheckDelayedUpdatesCenter[i];
                    this.checkBlock(lightAccess, currentChunkOffX | (value & 15),
                            chunkOffY | (value >>> 8),
                            currentChunkOffZ | ((value >>> 4) & 0xF));
                }
                if (i < neighbourDelayedChecks) {
                    final long value = this.chunkCheckDelayedUpdatesNeighbour[i];
                    this.checkBlock(lightAccess, neighbourChunkOffX | (value & 15),
                            chunkOffY | (value >>> 8),
                            neighbourChunkOffZ | ((value >>> 4) & 0xF));
                }
            }
        }
    }

    protected void checkChunkEdges(final LightChunkGetter lightAccess, final ChunkAccess chunk, final ShortCollection sections) {
        final ChunkPos chunkPos = chunk.getPos();
        final long chunkX = chunkPos.x;
        final long chunkZ = chunkPos.z;

        for (final ShortIterator iterator = sections.iterator(); iterator.hasNext();) {
            this.checkChunkEdge(lightAccess, chunk, chunkX, iterator.nextShort(), chunkZ);
        }

        this.performLightDecrease(lightAccess);
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // verifies that light levels on this chunks edges are consistent with this chunk's neighbours
    // edges. if they are not, they are decreased (effectively performing the logic in checkBlock).
    // This does not resolve skylight source problems.
    protected void checkChunkEdges(final LightChunkGetter lightAccess, final ChunkAccess chunk, final int fromSection, final int toSection) {
        final ChunkPos chunkPos = chunk.getPos();
        final long chunkX = chunkPos.x;
        final long chunkZ = chunkPos.z;

        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            this.checkChunkEdge(lightAccess, chunk, chunkX, currSectionY, chunkZ);
        }

        this.performLightDecrease(lightAccess);
    }

    // pulls light from neighbours, and adds them into the increase queue. does not actually propagate.
    protected final void propagateNeighbourLevels(final LightChunkGetter lightAccess, final ChunkAccess chunk, final int fromSection, final int toSection) {
        final ChunkPos chunkPos = chunk.getPos();
        final long chunkX = chunkPos.x;
        final long chunkZ = chunkPos.z;

        for (int currSectionY = toSection; currSectionY >= fromSection; --currSectionY) {
            final SWMRNibbleArray currNibble = this.getNibbleFromCache(chunkX, currSectionY, chunkZ);
            if (currNibble == null) {
                continue;
            }
            for (final AxisDirection direction : ONLY_HORIZONTAL_DIRECTIONS) {
                final int neighbourOffX = direction.x;
                final int neighbourOffZ = direction.z;

                final SWMRNibbleArray neighbourNibble = this.getNibbleFromCache(chunkX + neighbourOffX,
                        currSectionY, chunkZ + neighbourOffZ);

                if (neighbourNibble == null || !neighbourNibble.isInitialisedUpdating()) {
                    // can't pull from 0
                    continue;
                }

                // neighbour chunk
                final int incX;
                final int incZ;
                final long startX;
                final long startZ;

                if (neighbourOffX != 0) {
                    // x direction
                    incX = 0;
                    incZ = 1;

                    if (direction.x < 0) {
                        // negative
                        startX = (chunkX << 4) - 1;
                    } else {
                        startX = (chunkX << 4) + 16;
                    }
                    startZ = chunkZ << 4;
                } else {
                    // z direction
                    incX = 1;
                    incZ = 0;

                    if (neighbourOffZ < 0) {
                        // negative
                        startZ = (chunkZ << 4) - 1;
                    } else {
                        startZ = (chunkZ << 4) + 16;
                    }
                    startX = chunkX << 4;
                }

                final long propagateDirection = 1L << direction.getOpposite().ordinal(); // we only want to check in this direction towards this chunk
                final long encodeOffset = this.coordinateOffset;

                for (int currY = currSectionY << 4, maxY = currY | 15; currY <= maxY; ++currY) {
                    for (long i = 0, currX = startX, currZ = startZ; i < 16; ++i, currX += incX, currZ += incZ) {
                        final int level = neighbourNibble.getUpdating(
                                (int) ((currX & 15)
                                                                        | ((currZ & 15) << 4)
                                                                        | ((currY & 15) << 8))
                        );

                        if (level <= 1) {
                            // nothing to propagate
                            continue;
                        }

                        this.appendToIncreaseQueue(
                                ((currX + (currZ << 6) + (currY << (6 + 6)) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                        | ((level & 0xFL) << (6 + 6 + 16))
                                        | (propagateDirection << (6 + 6 + 16 + 4))
                                        | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS // don't know if the current block is transparent, must check.
                        );
                    }
                }
            }
        }
    }

    public static Boolean[] getEmptySectionsForChunk(final ChunkAccess chunk) {
        final LevelChunkSection[] sections = chunk.getSections();
        final Boolean[] ret = new Boolean[sections.length];

        for (int i = 0; i < sections.length; ++i) {
            if (sections[i] == null || sections[i].hasOnlyAir()) {
                ret[i] = Boolean.TRUE;
            } else {
                ret[i] = Boolean.FALSE;
            }
        }

        return ret;
    }

    public final void forceHandleEmptySectionChanges(final LightChunkGetter lightAccess, final ChunkAccess chunk, final Boolean[] emptinessChanges) {
        final long chunkX = chunk.getPos().x;
        final long chunkZ = chunk.getPos().z;
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, true);
        try {
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, this.getNibblesOnChunk(chunk));
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));

            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptinessChanges, false);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void handleEmptySectionChanges(final LightChunkGetter lightAccess, final int chunkX, final int chunkZ,
                                                final Boolean[] emptinessChanges) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, true);
        try {
            final ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptinessChanges, false);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    protected abstract void initNibble(final long chunkX, final long chunkY, final long chunkZ, final boolean extrude, final boolean initRemovedNibbles);

    protected abstract void setNibbleNull(final long chunkX, final long chunkY, final long chunkZ);

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // subclasses are guaranteed that this is always called before a changed block set
    // newChunk specifies whether the changes describe a "first load" of a chunk or changes to existing, already loaded chunks
    // rets non-null when the emptiness map changed and needs to be updated
    protected final boolean[] handleEmptySectionChanges(final LightChunkGetter lightAccess, final ChunkAccess chunk,
                                                        final Boolean[] emptinessChanges, final boolean unlit) {
        final Level world = (Level)lightAccess.getLevel();
        final long chunkX = chunk.getPos().x;
        final long chunkZ = chunk.getPos().z;

        boolean[] chunkEmptinessMap = this.getEmptinessMap(chunkX, chunkZ);
        boolean[] ret = null;
        final boolean needsInit = unlit || chunkEmptinessMap == null;
        if (needsInit) {
            this.setEmptinessMapCache(chunkX, chunkZ, ret = chunkEmptinessMap = new boolean[(int) WorldUtil.getTotalSections(world)]);
        }

        // update emptiness map
        for (int sectionIndex = (emptinessChanges.length - 1); sectionIndex >= 0; --sectionIndex) {
            Boolean valueBoxed = emptinessChanges[sectionIndex];
            if (valueBoxed == null) {
                if (!needsInit) {
                    continue;
                }
                final LevelChunkSection section = this.getChunkSection(chunkX, sectionIndex + this.minSection, chunkZ);
                emptinessChanges[sectionIndex] = valueBoxed = section == null || section.hasOnlyAir() ? Boolean.TRUE : Boolean.FALSE;
            }
            chunkEmptinessMap[sectionIndex] = valueBoxed.booleanValue();
        }

        // now init neighbour nibbles
        for (int sectionIndex = (emptinessChanges.length - 1); sectionIndex >= 0; --sectionIndex) {
            final Boolean valueBoxed = emptinessChanges[sectionIndex];
            final int sectionY = sectionIndex + this.minSection;
            if (valueBoxed == null) {
                continue;
            }

            final boolean empty = valueBoxed.booleanValue();

            if (empty) {
                continue;
            }

            for (int dz = -1; dz <= 1; ++dz) {
                for (int dx = -1; dx <= 1; ++dx) {
                    // if we're not empty, we also need to initialise nibbles
                    // note: if we're unlit, we absolutely do not want to extrude, as light data isn't set up
                    final boolean extrude = (dx | dz) != 0 || !unlit;
                    for (int dy = 1; dy >= -1; --dy) {
                        this.initNibble(dx + chunkX, dy + sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }

        // check for de-init and lazy-init
        // lazy init is when chunks are being lit, so at the time they weren't loaded when their neighbours were running
        // init checks.
        for (int dz = -1; dz <= 1; ++dz) {
            for (int dx = -1; dx <= 1; ++dx) {
                // does this neighbour have 1 radius loaded?
                boolean neighboursLoaded = true;
                neighbour_loaded_search:
                for (int dz2 = -1; dz2 <= 1; ++dz2) {
                    for (int dx2 = -1; dx2 <= 1; ++dx2) {
                        if (this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ) == null) {
                            neighboursLoaded = false;
                            break neighbour_loaded_search;
                        }
                    }
                }

                for (int sectionY = this.maxLightSection; sectionY >= this.minLightSection; --sectionY) {
                    // check neighbours to see if we need to de-init this one
                    boolean allEmpty = true;
                    neighbour_search:
                    for (int dy2 = -1; dy2 <= 1; ++dy2) {
                        for (int dz2 = -1; dz2 <= 1; ++dz2) {
                            for (int dx2 = -1; dx2 <= 1; ++dx2) {
                                final int y = sectionY + dy2;
                                if (y < this.minSection || y > this.maxSection) {
                                    // empty
                                    continue;
                                }
                                final boolean[] emptinessMap = this.getEmptinessMap(dx + dx2 + chunkX, dz + dz2 + chunkZ);
                                if (emptinessMap != null) {
                                    if (!emptinessMap[y - this.minSection]) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                } else {
                                    final LevelChunkSection section = this.getChunkSection(dx + dx2 + chunkX, y, dz + dz2 + chunkZ);
                                    if (section != null && !section.hasOnlyAir()) {
                                        allEmpty = false;
                                        break neighbour_search;
                                    }
                                }
                            }
                        }
                    }

                    if (allEmpty & neighboursLoaded) {
                        // can only de-init when neighbours are loaded
                        // de-init is fine to delay, as de-init is just an optimisation - it's not required for lighting
                        // to be correct

                        // all were empty, so de-init
                        this.setNibbleNull(dx + chunkX, sectionY, dz + chunkZ);
                    } else if (!allEmpty) {
                        // must init
                        final boolean extrude = (dx | dz) != 0 || !unlit;
                        this.initNibble(dx + chunkX, sectionY, dz + chunkZ, extrude, false);
                    }
                }
            }
        }

        return ret;
    }

    public final void checkChunkEdges(final LightChunkGetter lightAccess, final long chunkX, final long chunkZ) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, false);
        try {
            final ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, this.minLightSection, this.maxLightSection);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void checkChunkEdges(final LightChunkGetter lightAccess, final long chunkX, final long chunkZ, final ShortCollection sections) {
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, false);
        try {
            final ChunkAccess chunk = this.getChunkInCache(chunkX, chunkZ);
            if (chunk == null) {
                return;
            }
            this.checkChunkEdges(lightAccess, chunk, sections);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    // subclasses should not initialise caches, as this will always be done by the super call
    // subclasses should not invoke updateVisible, as this will always be done by the super call
    // needsEdgeChecks applies when possibly loading vanilla data, which means we need to validate the current
    // chunks light values with respect to neighbours
    // subclasses should note that the emptiness changes are propagated BEFORE this is called, so this function
    // does not need to detect empty chunks itself (and it should do no handling for them either!)
    protected abstract void lightChunk(final LightChunkGetter lightAccess, final ChunkAccess chunk, final boolean needsEdgeChecks);

    public final void light(final LightChunkGetter lightAccess, final ChunkAccess chunk, final Boolean[] emptySections) {
        final long chunkX = chunk.getPos().x;
        final long chunkZ = chunk.getPos().z;
        this.setupCaches(lightAccess, chunkX * 16 + 7, 128, chunkZ * 16 + 7, true, true);

        try {
            final SWMRNibbleArray[] nibbles = getFilledEmptyLight(this.maxLightSection - this.minLightSection + 1);
            // force current chunk into cache
            this.setChunkInCache(chunkX, chunkZ, chunk);
            this.setBlocksForChunkInCache(chunkX, chunkZ, chunk.getSections());
            this.setNibblesForChunkInCache(chunkX, chunkZ, nibbles);
            this.setEmptinessMapCache(chunkX, chunkZ, this.getEmptinessMap(chunk));

            final boolean[] ret = this.handleEmptySectionChanges(lightAccess, chunk, emptySections, true);
            if (ret != null) {
                this.setEmptinessMap(chunk, ret);
            }
            this.lightChunk(lightAccess, chunk, true);
            this.setNibbles(chunk, nibbles);
            this.updateVisible(lightAccess);
        } finally {
            this.destroyCaches();
        }
    }

    public final void relightChunks(final LightChunkGetter lightAccess, final Set<ChunkPos> chunks,
                                    final Consumer<ChunkPos> chunkLightCallback, final IntConsumer onComplete) {
        // it's recommended for maximum performance that the set is ordered according to a BFS from the center of
        // the region of chunks to relight
        // it's required that tickets are added for each chunk to keep them loaded
        final Object2ObjectOpenHashMap<ChunkPos, SWMRNibbleArray[]> nibblesByChunk = new Object2ObjectOpenHashMap<>();
        final Object2ObjectOpenHashMap<ChunkPos, boolean[]> emptinessMapByChunk = new Object2ObjectOpenHashMap<>();

        final int[] neighbourLightOrder = new int[] {
                // d = 0
                0, 0,
                // d = 1
                -1, 0,
                0, -1,
                1, 0,
                0, 1,
                // d = 2
                -1, 1,
                1, 1,
                -1, -1,
                1, -1,
        };

        int lightCalls = 0;

        for (final ChunkPos chunkPos : chunks) {
            final long chunkX = chunkPos.x;
            final long chunkZ = chunkPos.z;
            final ChunkAccess chunk = (ChunkAccess)lightAccess.getChunkForLighting(chunkX, chunkZ);
            if (chunk == null || !this.canUseChunk(chunk)) {
                throw new IllegalStateException();
            }

            for (int i = 0, len = neighbourLightOrder.length; i < len; i += 2) {
                final int dx = neighbourLightOrder[i];
                final int dz = neighbourLightOrder[i + 1];
                final long neighbourX = dx + chunkX;
                final long neighbourZ = dz + chunkZ;

                final ChunkAccess neighbour = (ChunkAccess)lightAccess.getChunkForLighting(neighbourX, neighbourZ);
                if (neighbour == null || !this.canUseChunk(neighbour)) {
                    continue;
                }

                if (nibblesByChunk.get(new ChunkPos(neighbourX, neighbourZ)) != null) {
                    // lit already called for neighbour, no need to light it now
                    continue;
                }

                // light neighbour chunk
                this.setupEncodeOffset(neighbourX * 16 + 7, 128, neighbourZ * 16 + 7);
                try {
                    // insert all neighbouring chunks for this neighbour that we have data for
                    for (int dz2 = -1; dz2 <= 1; ++dz2) {
                        for (int dx2 = -1; dx2 <= 1; ++dx2) {
                            final long neighbourX2 = neighbourX + dx2;
                            final long neighbourZ2 = neighbourZ + dz2;
                            final ChunkPos key = new ChunkPos(neighbourX2, neighbourZ2);
                            final ChunkAccess neighbour2 = (ChunkAccess)lightAccess.getChunkForLighting(neighbourX2, neighbourZ2);
                            if (neighbour2 == null || !this.canUseChunk(neighbour2)) {
                                continue;
                            }

                            final SWMRNibbleArray[] nibbles = nibblesByChunk.get(key);
                            if (nibbles == null) {
                                // we haven't lit this chunk
                                continue;
                            }

                            this.setChunkInCache(neighbourX2, neighbourZ2, neighbour2);
                            this.setBlocksForChunkInCache(neighbourX2, neighbourZ2, neighbour2.getSections());
                            this.setNibblesForChunkInCache(neighbourX2, neighbourZ2, nibbles);
                            this.setEmptinessMapCache(neighbourX2, neighbourZ2, emptinessMapByChunk.get(key));
                        }
                    }

                    final ChunkPos key = new ChunkPos(neighbourX, neighbourZ);

                    // now insert the neighbour chunk and light it
                    final SWMRNibbleArray[] nibbles = getFilledEmptyLight(this.world);
                    nibblesByChunk.put(key, nibbles);

                    this.setChunkInCache(neighbourX, neighbourZ, neighbour);
                    this.setBlocksForChunkInCache(neighbourX, neighbourZ, neighbour.getSections());
                    this.setNibblesForChunkInCache(neighbourX, neighbourZ, nibbles);

                    final boolean[] neighbourEmptiness = this.handleEmptySectionChanges(lightAccess, neighbour, getEmptySectionsForChunk(neighbour), true);
                    emptinessMapByChunk.put(key, neighbourEmptiness);
                    if (chunks.contains(new ChunkPos(neighbourX, neighbourZ))) {
                        this.setEmptinessMap(neighbour, neighbourEmptiness);
                    }

                    this.lightChunk(lightAccess, neighbour, false);
                } finally {
                    this.destroyCaches();
                }
            }

            // done lighting all neighbours, so the chunk is now fully lit

            // make sure nibbles are fully updated before calling back
            final SWMRNibbleArray[] nibbles = nibblesByChunk.get(new ChunkPos(chunkX, chunkZ));
            for (final SWMRNibbleArray nibble : nibbles) {
                nibble.updateVisible();
            }

            this.setNibbles(chunk, nibbles);

            for (int y = this.minLightSection; y <= this.maxLightSection; ++y) {
                lightAccess.onLightUpdate(this.skylightPropagator ? LightLayer.SKY : LightLayer.BLOCK, SectionPos.of(chunkX, y, chunkX));
            }

            // now do callback
            if (chunkLightCallback != null) {
                chunkLightCallback.accept(chunkPos);
            }
            ++lightCalls;
        }

        if (onComplete != null) {
            onComplete.accept(lightCalls);
        }
    }

    // contains:
    // lower (6 + 6 + 16) = 28 bits: encoded coordinate position (x | (z << 6) | (y << (6 + 6))))
    // next 4 bits: propagated light level (0, 15]
    // next 6 bits: propagation direction bitset
    // next 24 bits: unused
    // last 3 bits: state flags
    // state flags:
    // whether the increase propagator needs to write the propagated level to the position, used to avoid cascading light
    // updates for block sources
    protected static final long FLAG_WRITE_LEVEL = Long.MIN_VALUE >>> 2;
    // whether the propagation needs to check if its current level is equal to the expected level
    // used only in increase propagation
    protected static final long FLAG_RECHECK_LEVEL = Long.MIN_VALUE >>> 1;
    // whether the propagation needs to consider if its block is conditionally transparent
    protected static final long FLAG_HAS_SIDED_TRANSPARENT_BLOCKS = Long.MIN_VALUE;

    protected long[] increaseQueue = new long[16 * 16 * 16];
    protected int increaseQueueInitialLength;
    protected long[] decreaseQueue = new long[16 * 16 * 16];
    protected int decreaseQueueInitialLength;

    protected final long[] resizeIncreaseQueue() {
        return this.increaseQueue = Arrays.copyOf(this.increaseQueue, this.increaseQueue.length * 2);
    }

    protected final long[] resizeDecreaseQueue() {
        return this.decreaseQueue = Arrays.copyOf(this.decreaseQueue, this.decreaseQueue.length * 2);
    }

    protected final void appendToIncreaseQueue(final long value) {
        final int idx = this.increaseQueueInitialLength++;
        long[] queue = this.increaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeIncreaseQueue();
            queue[idx] = value;
        } else {
            queue[idx] = value;
        }
    }

    protected final void appendToDecreaseQueue(final long value) {
        final int idx = this.decreaseQueueInitialLength++;
        long[] queue = this.decreaseQueue;
        if (idx >= queue.length) {
            queue = this.resizeDecreaseQueue();
            queue[idx] = value;
        } else {
            queue[idx] = value;
        }
    }

    protected static final AxisDirection[][] OLD_CHECK_DIRECTIONS = new AxisDirection[1 << 6][];
    protected static final int ALL_DIRECTIONS_BITSET = (1 << 6) - 1;
    static {
        for (int i = 0; i < OLD_CHECK_DIRECTIONS.length; ++i) {
            final List<AxisDirection> directions = new ArrayList<>();
            for (int bitset = i, len = Integer.bitCount(i), index = 0; index < len; ++index, bitset ^= IntegerUtil.getTrailingBit(bitset)) {
                directions.add(AXIS_DIRECTIONS[IntegerUtil.trailingZeros(bitset)]);
            }
            OLD_CHECK_DIRECTIONS[i] = directions.toArray(new AxisDirection[0]);
        }
    }

    protected final void performLightIncrease(final LightChunkGetter lightAccess) {
        final BlockGetter world = lightAccess.getLevel();
        long[] queue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.increaseQueueInitialLength;
        this.increaseQueueInitialLength = 0;
        final long decodeOffsetX = -this.encodeOffsetX;
        final long decodeOffsetY = -this.encodeOffsetY;
        final long decodeOffsetZ = -this.encodeOffsetZ;
        final long encodeOffset = this.coordinateOffset;
        final long sectionOffset = this.chunkSectionIndexOffset;

        while (queueReadIndex < queueLength) {
            final long queueValue = queue[queueReadIndex++];

            final long posX = ((int)queueValue & 63) + decodeOffsetX;
            final long posZ = (((int)queueValue >>> 6) & 63) + decodeOffsetZ;
            final long posY = (((int)queueValue >>> 12) & ((1 << 16) - 1)) + decodeOffsetY;
            final int propagatedLightLevel = (int)((queueValue >>> (6 + 6 + 16)) & 0xFL);
            final AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int)((queueValue >>> (6 + 6 + 16 + 4)) & 63L)];

            if ((queueValue & FLAG_RECHECK_LEVEL) != 0L) {
                if (this.getLightLevel(posX, posY, posZ) != propagatedLightLevel) {
                    // not at the level we expect, so something changed.
                    continue;
                }
            } else if ((queueValue & FLAG_WRITE_LEVEL) != 0L) {
                // these are used to restore block sources after a propagation decrease
                this.setLightLevel(posX, posY, posZ, propagatedLightLevel);
            }

            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (final AxisDirection propagate : checkDirections) {
                    final long offX = posX + propagate.x;
                    final long offY = posY + propagate.y;
                    final long offZ = posZ + propagate.z;

                    final long sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final long localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

                    final SWMRNibbleArray currentNibble = this.nibbleCache.get(sectionIndex);
                    final int currentLevel;
                    if (currentNibble == null || (currentLevel = currentNibble.getUpdating((int) localIndex)) >= (propagatedLightLevel - 1)) {
                        continue; // already at the level we want or unloaded
                    }

                    final BlockState blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = ((ExtendedAbstractBlockState)blockState).getOpacityIfCached();
                    if (opacityCached != -1) {
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set((int) localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);

                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                                | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                                | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4));
                                continue;
                            }
                        }
                        continue;
                    } else {
                        this.mutablePos1.set(offX, offY, offZ);
                        long flags = 0;
                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);

                            if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
                                continue;
                            }
                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                        }

                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacity);
                        if (targetLevel <= currentLevel) {
                            continue;
                        }

                        currentNibble.set((int) localIndex, targetLevel);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 1) {
                            if (queueLength >= queue.length) {
                                queue = this.resizeIncreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4))
                                            | (flags);
                        }
                        continue;
                    }
                }
            } else {
                // we actually need to worry about our state here
                final BlockState fromBlock = this.getBlockState(posX, posY, posZ);
                this.mutablePos2.set(posX, posY, posZ);
                for (final AxisDirection propagate : checkDirections) {
                    final long offX = posX + propagate.x;
                    final long offY = posY + propagate.y;
                    final long offZ = posZ + propagate.z;

                    final VoxelShape fromShape = (((ExtendedAbstractBlockState)fromBlock).isConditionallyFullOpaque()) ? fromBlock.getFaceOcclusionShape(world, this.mutablePos2, propagate.nms) : Shapes.empty();

                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                        continue;
                    }

                    final long sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final long localIndex = (offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8);

                    final SWMRNibbleArray currentNibble = this.nibbleCache.get(sectionIndex);
                    final int currentLevel;

                    if (currentNibble == null || (currentLevel = currentNibble.getUpdating((int) localIndex)) >= (propagatedLightLevel - 1)) {
                        continue; // already at the level we want
                    }

                    final BlockState blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = ((ExtendedAbstractBlockState)blockState).getOpacityIfCached();
                    if (opacityCached != -1) {
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacityCached);
                        if (targetLevel > currentLevel) {
                            currentNibble.set((int) localIndex, targetLevel);
                            this.postLightUpdate(offX, offY, offZ);

                            if (targetLevel > 1) {
                                if (queueLength >= queue.length) {
                                    queue = this.resizeIncreaseQueue();
                                }
                                queue[queueLength++] =
                                        ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                                | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                                | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4));
                                continue;
                            }
                        }
                        continue;
                    } else {
                        this.mutablePos1.set(offX, offY, offZ);
                        long flags = 0;
                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);

                            if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                                continue;
                            }
                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                        }

                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
                        final int targetLevel = propagatedLightLevel - Math.max(1, opacity);
                        if (targetLevel <= currentLevel) {
                            continue;
                        }

                        currentNibble.set((int) localIndex, targetLevel);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 1) {
                            if (queueLength >= queue.length) {
                                queue = this.resizeIncreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | (propagate.everythingButTheOppositeDirection << (6 + 6 + 16 + 4))
                                            | (flags);
                        }
                        continue;
                    }
                }
            }
        }
    }

    protected final void performLightDecrease(final LightChunkGetter lightAccess) {
        final BlockGetter world = lightAccess.getLevel();
        long[] queue = this.decreaseQueue;
        long[] increaseQueue = this.increaseQueue;
        int queueReadIndex = 0;
        int queueLength = this.decreaseQueueInitialLength;
        this.decreaseQueueInitialLength = 0;
        int increaseQueueLength = this.increaseQueueInitialLength;
        final long decodeOffsetX = -this.encodeOffsetX;
        final long decodeOffsetY = -this.encodeOffsetY;
        final long decodeOffsetZ = -this.encodeOffsetZ;
        final long encodeOffset = this.coordinateOffset;
        final long sectionOffset = this.chunkSectionIndexOffset;
        final int emittedMask = this.emittedLightMask;

        while (queueReadIndex < queueLength) {
            final long queueValue = queue[queueReadIndex++];

            final long posX = ((int)queueValue & 63) + decodeOffsetX;
            final long posZ = (((int)queueValue >>> 6) & 63) + decodeOffsetZ;
            final long posY = (((int)queueValue >>> 12) & ((1 << 16) - 1)) + decodeOffsetY;
            final int propagatedLightLevel = (int)((queueValue >>> (6 + 6 + 16)) & 0xF);
            final AxisDirection[] checkDirections = OLD_CHECK_DIRECTIONS[(int)((queueValue >>> (6 + 6 + 16 + 4)) & 63)];

            if ((queueValue & FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) == 0L) {
                // we don't need to worry about our state here.
                for (final AxisDirection propagate : checkDirections) {
                    final long offX = posX + propagate.x;
                    final long offY = posY + propagate.y;
                    final long offZ = posZ + propagate.z;

                    final long sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (int) ((offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8));

                    final SWMRNibbleArray currentNibble = this.nibbleCache.get(sectionIndex);
                    final int lightLevel;

                    if (currentNibble == null || (lightLevel = currentNibble.getUpdating(localIndex)) == 0) {
                        // already at lowest (or unloaded), nothing we can do
                        continue;
                    }

                    final BlockState blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = ((ExtendedAbstractBlockState)blockState).getOpacityIfCached();
                    if (opacityCached != -1) {
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        final int emittedLight = blockState.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque() ? (FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) : FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set((int) localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4));
                            continue;
                        }
                        continue;
                    } else {
                        this.mutablePos1.set(offX, offY, offZ);
                        long flags = 0;
                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);

                            if (Shapes.faceShapeOccludes(Shapes.empty(), cullingFace)) {
                                continue;
                            }
                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                        }

                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (FLAG_RECHECK_LEVEL | flags);
                            continue;
                        }
                        final int emittedLight = blockState.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (flags | FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set((int) localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) {
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4))
                                            | flags;
                        }
                        continue;
                    }
                }
            } else {
                // we actually need to worry about our state here
                final BlockState fromBlock = this.getBlockState(posX, posY, posZ);
                this.mutablePos2.set(posX, posY, posZ);
                for (final AxisDirection propagate : checkDirections) {
                    final long offX = posX + propagate.x;
                    final long offY = posY + propagate.y;
                    final long offZ = posZ + propagate.z;

                    final long sectionIndex = (offX >> 4) + 5 * (offZ >> 4) + (5 * 5) * (offY >> 4) + sectionOffset;
                    final int localIndex = (int) ((offX & 15) | ((offZ & 15) << 4) | ((offY & 15) << 8));

                    final VoxelShape fromShape = (((ExtendedAbstractBlockState)fromBlock).isConditionallyFullOpaque()) ? fromBlock.getFaceOcclusionShape(world, this.mutablePos2, propagate.nms) : Shapes.empty();

                    if (fromShape != Shapes.empty() && Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
                        continue;
                    }

                    final SWMRNibbleArray currentNibble = this.nibbleCache.get(sectionIndex);
                    final int lightLevel;

                    if (currentNibble == null || (lightLevel = currentNibble.getUpdating(localIndex)) == 0) {
                        // already at lowest (or unloaded), nothing we can do
                        continue;
                    }

                    final BlockState blockState = this.getBlockState(sectionIndex, localIndex);
                    if (blockState == null) {
                        continue;
                    }
                    final int opacityCached = ((ExtendedAbstractBlockState)blockState).getOpacityIfCached();
                    if (opacityCached != -1) {
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacityCached));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | FLAG_RECHECK_LEVEL;
                            continue;
                        }
                        final int emittedLight = blockState.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque() ? (FLAG_WRITE_LEVEL | FLAG_HAS_SIDED_TRANSPARENT_BLOCKS) : FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set((int) localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4));
                            continue;
                        }
                        continue;
                    } else {
                        this.mutablePos1.set(offX, offY, offZ);
                        long flags = 0;
                        if (((ExtendedAbstractBlockState)blockState).isConditionallyFullOpaque()) {
                            final VoxelShape cullingFace = blockState.getFaceOcclusionShape(world, this.mutablePos1, propagate.getOpposite().nms);

                            if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
                                continue;
                            }
                            flags |= FLAG_HAS_SIDED_TRANSPARENT_BLOCKS;
                        }

                        final int opacity = blockState.getLightBlock(world, this.mutablePos1);
                        final int targetLevel = Math.max(0, propagatedLightLevel - Math.max(1, opacity));
                        if (lightLevel > targetLevel) {
                            // it looks like another source propagated here, so re-propagate it
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((lightLevel & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (FLAG_RECHECK_LEVEL | flags);
                            continue;
                        }
                        final int emittedLight = blockState.getLightEmission() & emittedMask;
                        if (emittedLight != 0) {
                            // re-propagate source
                            // note: do not set recheck level, or else the propagation will fail
                            if (increaseQueueLength >= increaseQueue.length) {
                                increaseQueue = this.resizeIncreaseQueue();
                            }
                            increaseQueue[increaseQueueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((emittedLight & 0xFL) << (6 + 6 + 16))
                                            | (((long)ALL_DIRECTIONS_BITSET) << (6 + 6 + 16 + 4))
                                            | (flags | FLAG_WRITE_LEVEL);
                        }

                        currentNibble.set(localIndex, 0);
                        this.postLightUpdate(offX, offY, offZ);

                        if (targetLevel > 0) { // we actually need to propagate 0 just in case we find a neighbour...
                            if (queueLength >= queue.length) {
                                queue = this.resizeDecreaseQueue();
                            }
                            queue[queueLength++] =
                                    ((offX + (offZ << 6) + (offY << 12) + encodeOffset) & ((1L << (6 + 6 + 16)) - 1))
                                            | ((targetLevel & 0xFL) << (6 + 6 + 16))
                                            | ((propagate.everythingButTheOppositeDirection) << (6 + 6 + 16 + 4))
                                            | flags;
                        }
                        continue;
                    }
                }
            }
        }

        // propagate sources we clobbered
        this.increaseQueueInitialLength = increaseQueueLength;
        this.performLightIncrease(lightAccess);
    }
}
