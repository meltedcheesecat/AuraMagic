package me.melchscat.aura.block;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public enum AuraBlocks {
    AURA_AIR_BLOCK("Air_Block"),
    AURA_START_BLOCK("Mannequin_Aura_Magic"),
    AURA_WIND_BLOCK_FLAT("Aura_Wind_Block_Flat"),
    AURA_WIND_BLOCK_STAIR("Aura_Wind_Block_Stair"),
    AURA_WIND_GEM("Rock_Gem_Aquamarine"),
    AURA_WIND_CRYSTAL_SPAWN("Spawner_Wind_Sprite"),
    AURA_WIND_CRYSTAL_BLOCK("Rock_Crystal_Wind_Block"),
    AURA_WIND_CRYSTAL_LARGE("Rock_Crystal_Wind_Large"),
    AURA_WIND_CRYSTAL_MEDIUM("Rock_Crystal_Wind_Medium"),
    AURA_WIND_CRYSTAL_SMALL("Rock_Crystal_Wind_Small");

    public static Rotation getBlockFacingDir(int dirX, int dirZ) {
        if (dirZ == -1) return Rotation.None;
        if (dirX == -1) return Rotation.Ninety;
        if (dirZ == 1) return Rotation.OneEighty;
        return Rotation.TwoSeventy;
    }

    public final String name;
    private boolean initDone = false;
    private int blockId;

    AuraBlocks(String name) {
        this.name = name;
        this.blockId = -1;
    }

    public int id() {
        if (!initDone)
        {
            if (!init()) {
                String errStr = "Aura Blocks not Found:" + AuraBlocks.getErrStr();
                getLogger().at(Level.SEVERE).log(errStr);
                throw new IllegalStateException(errStr);
            }
            initDone = true;
        }

        return this.blockId;
    }

    public static boolean init() {
        boolean noError = true;

        for (AuraBlocks block : values()) {
            if (block.name.compareTo("Air_Block") == 0) {
                block.blockId = 0;
            } else {
                BlockType type = BlockType.fromString(block.name);
                if (type == null) {
                    block.blockId = -2;
                    noError = false;
                    continue;
                }
                block.blockId = BlockType.getAssetMap().getIndex(type.getId());
            }
        }
        return noError;
    }

    public static String getErrStr() {
        String errStr = "(";
        int count = 0;
        for (AuraBlocks block : values()) {
            if (block.blockId < 0) {
                if (count > 0) errStr += ",";
                errStr += block.name;
                count++;
            }
        }
        errStr += ")";
        return errStr;
    }
}
