package me.melchscat.aura.worldgen;

import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;

public class NukeChunkGeneratorWrapper extends ChunkGenerator {
    private final ChunkGenerator original;

    public NukeChunkGeneratorWrapper(ChunkGenerator original) {
        super(original.getZonePatternProvider(), original.getDataFolder());
        this.original = original;
    }

    @Override
    public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, LongPredicate stillNeeded) {
        // Run Hytale's generation first
        return original.generate(seed, index, x, z, stillNeeded).thenApply(chunk -> {
            if (chunk != null) {
                applyNuke(chunk);
            }
            return chunk;
        });
    }

    private void applyNuke(GeneratedChunk chunk) {
        GeneratedBlockChunk blockChunk = chunk.getBlockChunk();

        // GeneratedBlockChunk usually uses 0-15 for X/Z and 0-255 for Y
        for (int ly = 150; ly < 170; ly++) {
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    blockChunk.setBlock(lx, ly, lz, 100, 0, 0);
                }
            }
        }
    }
}
