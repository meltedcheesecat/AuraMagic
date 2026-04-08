package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import me.melchscat.aura.component.AuraBlockLifetimeComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Set;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.block.AuraBlocks.AURA_AIR_BLOCK;

public class AuraBlockLifetimeSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, AuraBlockLifetimeComponent> auraBlockLifetimeComType;

    public AuraBlockLifetimeSystem(ComponentType<ChunkStore, AuraBlockLifetimeComponent> auraBlockLifetimeComType) {
        this.auraBlockLifetimeComType = auraBlockLifetimeComType;
    }

    @NullableDecl
    @Override
    public Query<ChunkStore> getQuery() {
        return auraBlockLifetimeComType;
    }

    @Override
    public void tick(float dt,
                     int index,
                     @NonNullDecl ArchetypeChunk<ChunkStore> archetypeChunk,
                     @NonNullDecl Store<ChunkStore> store,
                     @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
        BlockSection blocks = archetypeChunk.getComponent(index, BlockSection.getComponentType());
        if (blocks == null) return;
        if (blocks.getTickingBlocksCountCopy() == 0) return;

        ChunkSection section = archetypeChunk.getComponent(index, ChunkSection.getComponentType());
        if (section == null) return;

        BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(section.getChunkColumnReference(), BlockComponentChunk.getComponentType());

        // Loops through each block Component Chunk getting block with life components and checking if they need to end
        blocks.forEachTicking(blockComponentChunk,
                              commandBuffer,
                              section.getY(),
                              (blkCptChk,
                               comBuf,
                               localX,
                               localY,
                               localZ,
                               blockID) -> {

            getLogger().at(Level.INFO).log("Aura AuraBlockLifetimeSystem localx:" + localX + ", localy:" + localX + ", localz:" + localZ);
            Ref<ChunkStore> blockRef = blkCptChk.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
            if (blockRef == null) return BlockTickStrategy.IGNORED;

            AuraBlockLifetimeComponent lifeCom = archetypeChunk.getComponent(index, auraBlockLifetimeComType);
            if (lifeCom == null) return BlockTickStrategy.IGNORED;

            WorldChunk worldChunk = comBuf.getComponent(section.getChunkColumnReference(), WorldChunk.getComponentType());
            if (worldChunk == null) return BlockTickStrategy.IGNORED;

            int globalX = localX + (worldChunk.getX() * 32);
            int globalZ = localZ + (worldChunk.getZ() * 32);

            if (lifeCom.startTick + lifeCom.lifeTickLength <= worldChunk.getWorld().getTick()) return BlockTickStrategy.CONTINUE;

            getLogger().at(Level.INFO).log("Aura AuraBlockLifetimeSystem globalX:" + globalX + ", localy:" + localX + ", globalZ:" + globalZ);
            worldChunk.getWorld().execute(() -> {
                worldChunk.setBlock(globalX, localY, globalZ, AURA_AIR_BLOCK.id());
            });
            return BlockTickStrategy.SLEEP;
        });
    }

    @Override
    public void onSystemRegistered() {
        super.onSystemRegistered();
    }

    @Override
    public void onSystemUnregistered() {
        super.onSystemUnregistered();
    }

    @NullableDecl
    @Override
    public SystemGroup<ChunkStore> getGroup() {
        return super.getGroup();
    }

    @NonNullDecl
    @Override
    public Set<Dependency<ChunkStore>> getDependencies() {
        return super.getDependencies();
    }
}
