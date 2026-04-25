package me.melchscat.aura.worldgen;

import com.google.common.flogger.AbstractLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunkSection;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.prefab.AuraPrefabs;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongPredicate;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.block.AuraBlocks.*;

public class CustomBlockGenerator extends ChunkGenerator {
    private static int WOOD_SOUND_SET = -1;
    private static float AURA_ADD_CHANCE = 0.0f;
    private static float MAX_AURA_ADD_CHANCE = 0.75f;
    private static int AURA_GEM_SPAWN_CHANCE = 15;  // 1 out of AURA_GEM_SPAWN_CHANCE
    private final int windCrystalMinHeight = 129;
    private final int windCrystalMaxChanceHeight = 200;
    private final float minChance = 0.01f;
    private final float maxChance = 0.21f;

    private boolean chuckHasSpawnedGem = false;
    private Transform[] spawnArr = null;
    private int spawnChunkX;
    private int spawnChunkZ;
    private final ChunkGenerator original;
    private int fingerChance;
    private World world;

    public CustomBlockGenerator(ChunkGenerator original, World world) {
        super(original.getZonePatternProvider(), original.getDataFolder());
        this.original = original;
        this.world = world;
    }

    @Override
    public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, LongPredicate stillNeeded) {
        if (spawnArr == null) {
            spawnArr = this.getSpawnPoints(seed);

            if (spawnArr.length > 0) {
                Vector3d pos = spawnArr[0].getPosition();
                spawnChunkX = Math.floorDiv((int) pos.x, 32);
                spawnChunkZ = Math.floorDiv((int) pos.z, 32);
            }
        }

        // Run Hytale's generation first
        return original.generate(seed, index, x, z, stillNeeded).thenApply(chunk -> {
            if (chunk != null) {
                generateAuraSpecificBlocks(chunk, x, z);
            }

            return chunk;
        });
    }

    private int getBlockId(String blockName) {
        BlockType type = BlockType.fromString(blockName);

        if (type != null) {
            return BlockType.getAssetMap().getIndex(type.getId());
        }
        return 0; // Return Air if not found
    }

    private Boolean isBlockIdaSolidCrystalSpawn(int blockId) {
        if (blockId == 0) return false;

        BlockType type = (BlockType) BlockType.getAssetMap().getAsset(blockId);
        if (type == null) return false;
        if (type.getDrawType() != DrawType.Cube) return false;
        if (type.getMaterial() != BlockMaterial.Solid) return false;
        if (type.getBlockSoundSetIndex() == WOOD_SOUND_SET) return false;

        return true;
    }

    private Boolean isBlockIdaEmptyCrystalSpawn(int blockId) {
        if (blockId == 0) return true;

        BlockType type = (BlockType) BlockType.getAssetMap().getAsset(blockId);
        if (type == null) return false;
        if (type.getOpacity() != Opacity.Solid) return true;

        return false;
    }

    private Boolean isChunkValidForCrystalSpawn(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        int sectionIndex = ChunkUtil.indexSection(blockLocation.y);
        GeneratedChunkSection section = blockChunk.getSection(blockLocation.y);

        while (blockLocation.y >= windCrystalMinHeight) {
            // sections are 32 height each so only rerun if we are in a new section
            if (sectionIndex != ChunkUtil.indexSection(blockLocation.y)) {
                section = blockChunk.getSection(blockLocation.y);
            }

            // check if section is empty
            if (section == null) {
                // this should get the first y from the bottom of the current section, -1 will get the next one down
                blockLocation.y = (ChunkUtil.indexSection(blockLocation.y) * 32) - 1;
                continue;
            }

            int blockId = section.getBlock(blockLocation.x, blockLocation.y, blockLocation.z);
            if (isBlockIdaSolidCrystalSpawn(blockId)) {
                return true;
            }

            blockLocation.y--;
        }

        return false;
    }

    private Boolean isPointValid(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        int topBlockId = blockChunk.getBlock(blockLocation.x, blockLocation.y, blockLocation.z);
        int currY = blockLocation.y - 1;
        int bottomBlockId = blockChunk.getBlock(blockLocation.x, currY, blockLocation.z);
        if (isBlockIdaEmptyCrystalSpawn(topBlockId) && isBlockIdaSolidCrystalSpawn(bottomBlockId)) return true;
        return false;
    }

    private Boolean getYValidPlacePoint(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        if (isPointValid(blockChunk, blockLocation)) return true;
        blockLocation.y--;
        if (isPointValid(blockChunk, blockLocation)) return true;
        blockLocation.y = blockLocation.y + 2;
        if (isPointValid(blockChunk, blockLocation)) return true;
        blockLocation.y--;
        return false;
    }

    private Boolean findValidStartPlacePoint(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        // first block just check the current block since the trace down checked the lower block
        blockLocation.y++;
        int blockId = blockChunk.getBlock(blockLocation.x, blockLocation.y, blockLocation.z);
        if (isBlockIdaEmptyCrystalSpawn(blockId)) return true;
        blockLocation.x--;
        if (getYValidPlacePoint(blockChunk, blockLocation)) return true;
        blockLocation.x = blockLocation.x + 2;
        if (getYValidPlacePoint(blockChunk, blockLocation)) return true;
        blockLocation.x--;
        blockLocation.z--;
        if (getYValidPlacePoint(blockChunk, blockLocation)) return true;
        blockLocation.z = blockLocation.z + 2;
        if (getYValidPlacePoint(blockChunk, blockLocation)) return true;
        blockLocation.z--;
        return false;
    }

    private void placeWindCrystalFinger(GeneratedBlockChunk blockChunk, Vector3i blockLocation, int directionX, int directionZ) {
        Vector3i currLocation = new Vector3i(blockLocation);
        int blockSize = 3;
        int fingerLength = 0;
        currLocation.x = currLocation.x + directionX;
        currLocation.z = currLocation.z + directionZ;

        // There is a small chance to spawn 1 gem on each Crystal outcrop
        if (!chuckHasSpawnedGem) {
            if (ThreadLocalRandom.current().nextInt(AURA_GEM_SPAWN_CHANCE) == 0) {
                blockSize = 4;
            }
        }

        while ((blockSize > 0) && (fingerLength < 8)) {
            if (!getYValidPlacePoint(blockChunk, currLocation)) return;

            switch (blockSize) {
                case 1:
                    blockChunk.setBlock(currLocation.x, currLocation.y, currLocation.z, AURA_WIND_CRYSTAL_SMALL.id(), 0, 0);
                    break;
                case 2:
                    blockChunk.setBlock(currLocation.x, currLocation.y, currLocation.z, AURA_WIND_CRYSTAL_MEDIUM.id(), 0, 0);
                    break;
                case 3:
                    blockChunk.setBlock(currLocation.x, currLocation.y, currLocation.z, AURA_WIND_CRYSTAL_LARGE.id(), 0, 0);
                    break;
                case 4: {
                    blockChunk.setBlock(currLocation.x, currLocation.y, currLocation.z, AURA_WIND_GEM.id(), 0, 0);
                    chuckHasSpawnedGem = true;
                    break;
                }
            }
            currLocation.x = currLocation.x + directionX;
            currLocation.z = currLocation.z + directionZ;

            fingerLength++;
            blockSize = blockSize - ThreadLocalRandom.current().nextInt(3);
        }
    }

    private boolean doSpawnFinger() {
        if (ThreadLocalRandom.current().nextInt(fingerChance) == 0) {
            fingerChance++;
            return true;
        }
        if (fingerChance > 1)
            fingerChance--;
        return false;
    }

    private void placeWindCrystals(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        if (findValidStartPlacePoint(blockChunk, blockLocation)) {
            blockChunk.setBlock(blockLocation.x, blockLocation.y, blockLocation.z, AURA_WIND_CRYSTAL_SPAWN.id(), 0, 0);

            fingerChance = 3;

            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, 1, 1);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, 1, 0);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, 1, -1);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, -1, 1);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, -1, 0);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, -1, -1);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, 0, 1);
            if (doSpawnFinger()) placeWindCrystalFinger(blockChunk, blockLocation, 0, -1);
        }
    }

    // This is temp for Gen V1 waiting for Gen V2 to be implemented then I will used that
    private void generateAuraSpecificBlocks(GeneratedChunk chunk, int chunkX, int chunkZ) {
        GeneratedBlockChunk blockChunk = chunk.getBlockChunk();
        // Add Aura Start Block near player spawn
        if (spawnArr.length > 0) {
            getLogger().at(Level.INFO).log("AuraLog ChunkGen spawnArr.length:" + spawnArr.length);
            if ((spawnChunkX == chunkX) && (spawnChunkZ == chunkZ)) {
                Vector3d position = spawnArr[0].getPosition();
                int spawnX = (int) position.x - (chunkX * 32);
                int spawnZ = (int) position.z - (chunkZ * 32);
                int spawnY = (int) position.y;
                // Start Aura Start NPC
                getLogger().at(Level.INFO).log("AuraLog ChunkGen Start NPC x:" + spawnX + ", y:" + (spawnY - 1) + ", z:" + (spawnZ - 3));
                blockChunk.setBlock(spawnX, spawnY - 1, spawnZ - 3, AURA_START_NPC_BLOCK.id(), 0, 0);
            }
        }

        AuraPrefabs auraPrefabs = AuraMagicPlugin.getInstance().getAuraPrefabs();
        if (auraPrefabs == null) return;
        if (!auraPrefabs.loaded) auraPrefabs.init();

        // This is a hack to check for wood block so I can be fast,
        if (WOOD_SOUND_SET == -1) {
            BlockType wood = BlockType.fromString("Wood_Oak_Trunk");
            WOOD_SOUND_SET = (wood != null) ? wood.getBlockSoundSetIndex() : 999;
        }

        int randXPos = ThreadLocalRandom.current().nextInt(8) + 12;
        int randZPos = ThreadLocalRandom.current().nextInt(8) + 12;

        chuckHasSpawnedGem = false;
        boolean donePlace = false;
        // Ray trace down trying to find valid block
        Vector3i blockLocation = new Vector3i(randXPos, 319, randZPos);
        if (isChunkValidForCrystalSpawn(blockChunk, blockLocation)) {

            int currentY = blockLocation.y;
            float spawnChance;

            if (currentY >= windCrystalMaxChanceHeight) {
                spawnChance = maxChance + AURA_ADD_CHANCE;
            } else {
                float range = (float) (windCrystalMaxChanceHeight - windCrystalMinHeight);
                float progress = (float) (currentY - windCrystalMinHeight) / range;

                progress = Math.max(0.0f, progress);
                spawnChance = minChance + AURA_ADD_CHANCE + (progress * (maxChance - minChance));
            }

            if (ThreadLocalRandom.current().nextFloat() < spawnChance) {
                // one out of every 5 should be a prefab rather than crystals
                if (ThreadLocalRandom.current().nextInt(6) == 5) {
                    Vector3i placePos = new Vector3i(16 + (chunkX * 32), currentY, 16 + (chunkZ * 32));
                    auraPrefabs.spawnWindPrefab(world, placePos);
                } else {
                    placeWindCrystals(blockChunk, blockLocation);
                }

                donePlace = true;
                AURA_ADD_CHANCE = 0.0f;
            }
        }
        // Adds a small chance to place everytime it fails
        if (!donePlace) {
            if (AURA_ADD_CHANCE < MAX_AURA_ADD_CHANCE)
                AURA_ADD_CHANCE = AURA_ADD_CHANCE + 0.01f;
        }
    }
}