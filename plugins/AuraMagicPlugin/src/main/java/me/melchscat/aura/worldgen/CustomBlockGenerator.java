package me.melchscat.aura.worldgen;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunkSection;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import me.melchscat.aura.AuraPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class CustomBlockGenerator extends ChunkGenerator {
    private static int WOOD_SOUND_SET = -1;
    private static int AURA_CRYSTAL_BLOCK_ID = -1;
    private static int AURA_CRYSTAL_LARGE_ID = -1;
    private static int AURA_CRYSTAL_MEDIUM_ID = -1;
    private static int AURA_CRYSTAL_SMALL_ID = -1;
    private final int windCrystalMinHeight = 100;

    private final ChunkGenerator original;

    public CustomBlockGenerator(ChunkGenerator original) {
        super(original.getZonePatternProvider(), original.getDataFolder());
        this.original = original;
    }

    @Override
    public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, LongPredicate stillNeeded) {
        // Run Hytale's generation first
        return original.generate(seed, index, x, z, stillNeeded).thenApply(chunk -> {
            if (chunk != null) {
                generateAuraSpecificBlocks(chunk);
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

        BlockType type = (BlockType)BlockType.getAssetMap().getAsset(blockId);
        if (type == null) return false;
        if (type.getDrawType() != DrawType.Cube) return false;
        if (type.getMaterial() != BlockMaterial.Solid) return false;
        if (type.getBlockSoundSetIndex() == WOOD_SOUND_SET) return false;

        return true;
    }

    private Boolean isBlockIdaEmptyCrystalSpawn(int blockId) {
        if (blockId == 0) return true;

        BlockType type = (BlockType)BlockType.getAssetMap().getAsset(blockId);
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

    private void placeWindCrystals(GeneratedBlockChunk blockChunk, Vector3i blockLocation) {
        if (findValidStartPlacePoint(blockChunk, blockLocation)) {
            blockChunk.setBlock(blockLocation.x, blockLocation.y, blockLocation.z, AURA_CRYSTAL_BLOCK_ID, 0,0);
        }
    }

    // This is temp for Gen V1 waiting for Gen V2 to be implemented then I will used that
    private void generateAuraSpecificBlocks(GeneratedChunk chunk) {
        GeneratedBlockChunk blockChunk = chunk.getBlockChunk();

        // Get Block IDs for Crystals, this should only run once
        if (AURA_CRYSTAL_BLOCK_ID == -1) AURA_CRYSTAL_BLOCK_ID = getBlockId(AuraPlugin.getInstance().WindCrystalBlock);
        if (AURA_CRYSTAL_LARGE_ID == -1) AURA_CRYSTAL_LARGE_ID = getBlockId(AuraPlugin.getInstance().WindCrystalLarge);
        if (AURA_CRYSTAL_MEDIUM_ID == -1) AURA_CRYSTAL_MEDIUM_ID = getBlockId(AuraPlugin.getInstance().WindCrystalMedium);
        if (AURA_CRYSTAL_SMALL_ID == -1) AURA_CRYSTAL_SMALL_ID = getBlockId(AuraPlugin.getInstance().WindCrystalSmall);

        // This is a hack to check for wood block so I can be fast,
        if (WOOD_SOUND_SET == -1) {
            BlockType wood = BlockType.fromString("Wood_Oak_Trunk");
            WOOD_SOUND_SET = (wood != null) ? wood.getBlockSoundSetIndex() : 999;
        }

        // Ray trace down trying to find valid block
        Vector3i blockLocation = new Vector3i(15 , 319 , 15);
        if (isChunkValidForCrystalSpawn(blockChunk, blockLocation)) {
            placeWindCrystals(blockChunk, blockLocation);
        }
    }
}
