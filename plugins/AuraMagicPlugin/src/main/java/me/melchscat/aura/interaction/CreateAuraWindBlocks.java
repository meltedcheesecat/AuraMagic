package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.block.AuraBlocks;
import me.melchscat.aura.component.AuraBlockLifetimeComponent;

import javax.annotation.Nonnull;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.block.AuraBlocks.*;

public class CreateAuraWindBlocks extends SimpleInteraction {
    protected int level;
    protected int blockType;
    private ComponentType<ChunkStore, AuraBlockLifetimeComponent> auraBlockLifetimeComType = null;

    private int blockCount() {
        return (((level + blockType) * 2) + 1);
    }

    private void setBlock(World world, Vector3i pos, String blockName, @Nonnull Rotation yaw, int timeSec) {
        if (world == null) return;

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.getX(), pos.getZ());
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
        if (worldChunk == null) return;

        if (worldChunk.getBlock(pos) == AURA_AIR_BLOCK.id()) {
            if (!worldChunk.placeBlock(pos.getX(), pos.getY(), pos.getZ(), blockName, yaw, Rotation.None, Rotation.None)) {
                getLogger().at(Level.INFO).log("Aura Block not placed X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", name:" + blockName);
                return;
            }
            getLogger().at(Level.INFO).log("Aura placed Block X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", name:" + blockName);

            Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(pos.getX(), pos.getZ()));
            if (chunkRef == null) {
                getLogger().at(Level.INFO).log("Aura chunkRef is null");
            } else {
                BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStoreStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
                if (blockComponentChunk == null) {
                    getLogger().at(Level.INFO).log("Aura blockComponentChunk is null");
                } else {
                    int blockIndexColumn = ChunkUtil.indexBlockInColumn(pos.getX(), pos.getY(), pos.getZ());
                    Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);

                    if (blockRef == null) {
                        getLogger().at(Level.INFO).log("Aura blockRef is null");
                    } else {
                        AuraBlockLifetimeComponent lifeTime = chunkStoreStore.getComponent(blockRef, auraBlockLifetimeComType);
                        if (lifeTime == null)
                        {
                            getLogger().at(Level.INFO).log("Aura No lifeTime Component Found");
                        } else {
                            lifeTime.startTick = world.getTick();
                            lifeTime.lifeTickLength = (world.getTps() * (long)timeSec);
                            worldChunk.setTicking(pos.getX(), pos.getY(), pos.getZ(), true);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @Nonnull InteractionType type,
                         @Nonnull InteractionContext context,
                         @Nonnull CooldownHandler cooldownHandler) {
        if (auraBlockLifetimeComType == null) {
            auraBlockLifetimeComType = AuraMagicPlugin.getInstance().getAuraBlockLifetimeComponentType();
        }

        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        if (!owningEntityRef.isValid())
            return;

        Store<EntityStore> store = owningEntityRef.getStore();

        Player player = store.getComponent(owningEntityRef, Player.getComponentType());
        if (player == null) return;
        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = player.getWorld();
        if (world == null) return;

        Transform transform = playerRef.getTransform();

        Vector3d playerPos = transform.getPosition();
        Vector3i pos = new Vector3i();
        if (playerPos.getX() >= 0) pos.setX((int) playerPos.getX()); else pos.setX((int)playerPos.getX() - 1);
        if (playerPos.getY() >= 0) pos.setY((int) playerPos.getY()); else pos.setY((int)playerPos.getY() - 1);
        if (playerPos.getZ() >= 0) pos.setZ((int) playerPos.getZ()); else pos.setZ((int)playerPos.getZ() - 1);

        Vector3d playerDir = transform.getDirection();
        Vector3i dir = new Vector3i();

        // block type 0 is flat, 1 is stair, then level 1 to 5
        if ((blockType < 0) || (blockType > 1)) blockType = 0;
        if ((level < 1) || (level > 5)) level = 1;

        int timeSec = 2 + ((level + blockType) * 2);

        if (Math.abs(playerDir.getX()) >= Math.abs(playerDir.getZ())) {
            dir.setY(0);
            dir.setZ(0);
            if (playerDir.getX() >= 0) dir.setX(1); else dir.setX(-1);
        } else {
            dir.setY(0);
            dir.setX(0);
            if (playerDir.getZ() >= 0) dir.setZ(1); else dir.setZ(-1);
        }

        // stairs start at 3 length and flat starts a 5 length and they both go up in steps of 2
        world.execute(() -> {
            Rotation rotation = AuraBlocks.getBlockFacingDir(dir.getX(), dir.getZ());
            // First block is if you are on the edge of a cliff or something similar
            dir.setY(-1);
            pos.add(dir);
            setBlock(world, pos, AURA_WIND_BLOCK_FLAT.name, Rotation.None, timeSec);
            dir.setY(1);
            pos.add(dir);
            setBlock(world, pos, AURA_WIND_BLOCK_STAIR.name, rotation, timeSec);
            if (blockType == 0) dir.setY(1); else dir.setY(0);

            for (int index = 1; index < blockCount(); index++) {
                pos.add(dir);
                if (blockType == 0)
                    setBlock(world, pos, AURA_WIND_BLOCK_STAIR.name, rotation, timeSec);
                else
                    setBlock(world, pos, AURA_WIND_BLOCK_FLAT.name, Rotation.None, timeSec);
            }
        });
    }

    public static final BuilderCodec<CreateAuraWindBlocks> CODEC = BuilderCodec.builder(
                    CreateAuraWindBlocks.class, CreateAuraWindBlocks::new, SimpleInteraction.CODEC)
            .documentation("Creates Wind Blocks")
            .append(new KeyedCodec<>("Level", Codec.INTEGER, true),
                    (windblk, health) -> windblk.level = health,
                    windblk -> windblk.level)
            .add()
            .documentation("Wind Block Level")
            .append(new KeyedCodec<>("BlockType", Codec.INTEGER, true),
                    (windblk, health) -> windblk.blockType = health,
                    windblk -> windblk.blockType)
            .documentation("Flat blocks or stairs going up")
            .add()
            .build();
}