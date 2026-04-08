package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
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

    private void setBlock(World world, Store<ChunkStore> chunkStore, Vector3i pos, int id, int rotation, int filler) {
        if (world == null) return;

        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.getX(), pos.getZ());

        WorldChunk chunk = world.getChunkIfLoaded(chunkIndex);
        if (chunk == null) return;

        BlockChunk blockChunk = chunk.getBlockChunk();
        if (blockChunk == null) return;

        if (chunk.getBlock(pos) == AURA_AIR_BLOCK.id()) {
            getLogger().at(Level.INFO).log("Aura X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", id:" + id + ", rotation:" + rotation + ", filler:" + filler);
            blockChunk.setBlock(pos.getX(), pos.getY(), pos.getZ(), id, rotation, filler);

            Ref<ChunkStore> blockComponentEntity = chunk.getBlockComponentEntity(pos.getX(), pos.getY(), pos.getZ());
            if (blockComponentEntity == null) {
                getLogger().at(Level.INFO).log("Aura No Block Entity Found");
            } else {
                AuraBlockLifetimeComponent lifeTime = chunkStore.getComponent(blockComponentEntity, auraBlockLifetimeComType);
                if (lifeTime == null)
                {
                    getLogger().at(Level.INFO).log("Aura No lifeTime Component Found");
                } else {
                    lifeTime.startTick = world.getTick();
                    lifeTime.lifeTickLength = (world.getTps() * (long)10);
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
        getLogger().at(Level.INFO).log("Aura CreateAuraWindBlocks started");

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

        World playerWorld = player.getWorld();
        if (playerWorld == null) return;

        Transform transform = playerRef.getTransform();

        Vector3d playerPos = transform.getPosition();

        Vector3i pos = new Vector3i();
        if (playerPos.getX() >= 0) pos.setX((int) playerPos.getX()); else pos.setX((int)playerPos.getX() - 1);
        if (playerPos.getY() >= 0) pos.setY((int) playerPos.getY()); else pos.setY((int)playerPos.getY() - 1);
        if (playerPos.getZ() >= 0) pos.setZ((int) playerPos.getZ()); else pos.setZ((int)playerPos.getZ() - 1);

        Vector3d playerDir = transform.getDirection();
        getLogger().at(Level.INFO).log("Aura Player Dir X:" + playerDir.getX() + ", Z:" + playerDir.getZ() + ", (Height)Y:" + playerDir.getY());
        Vector3i dir = new Vector3i();

        if (Math.abs(playerDir.getX()) >= Math.abs(playerDir.getZ())) {
            dir.setY(0);
            dir.setZ(0);
            if (playerDir.getX() >= 0) dir.setX(1); else dir.setX(-1);
        } else {
            dir.setY(0);
            dir.setX(0);
            if (playerDir.getZ() >= 0) dir.setZ(1); else dir.setZ(-1);
        }
        getLogger().at(Level.INFO).log("Aura Player X:" + pos.getX() + ", Z:" + pos.getZ() + ", (Height)Y:" + pos.getY());
        getLogger().at(Level.INFO).log("Aura Player Dir X:" + dir.getX() + ", Z:" + dir.getZ() + ", (Height)Y:" + dir.getY());

        Store<ChunkStore> chunkStore = playerWorld.getChunkStore().getStore();

        playerWorld.execute(() -> {
            int rotation = AuraBlocks.getBlockFacingDir(dir.getX(), dir.getZ());
            pos.add(dir);
            pos.add(dir);
            setBlock(playerWorld, chunkStore, pos, AURA_WIND_BLOCK_STAIR.id(), rotation,0);
            pos.add(dir);
            setBlock(playerWorld, chunkStore, pos, AURA_WIND_BLOCK_FLAT.id(), 0,0);
            pos.add(dir);
            setBlock(playerWorld, chunkStore, pos, AURA_WIND_BLOCK_FLAT.id(), 0,0);
            pos.add(dir);
            setBlock(playerWorld, chunkStore, pos, AURA_WIND_BLOCK_FLAT.id(), 0,0);
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