package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import me.melchscat.aura.component.AuraBlockLifetimeComponent;

import javax.annotation.Nonnull;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.block.AuraBlocks.AURA_AIR_BLOCK;

public class AuraBlockLifetimeSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, AuraBlockLifetimeComponent> lifetimeType;

    public AuraBlockLifetimeSystem(ComponentType<ChunkStore, AuraBlockLifetimeComponent> lifetimeType) {
        this.lifetimeType = lifetimeType;
    }

    @Override
    public void tick(float dt,
                     int index,
                     @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                     @Nonnull Store<ChunkStore> store,
                     @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        AuraBlockLifetimeComponent lifetime = archetypeChunk.getComponent(index, this.lifetimeType);
        if (lifetime == null) return;

        BlockModule.BlockStateInfo blockStateInfo = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());
        if (blockStateInfo == null) return;

        Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
        if (!chunkRef.isValid()) return;

        BlockChunk blockChunkComponent = (BlockChunk)commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunkComponent == null) return;

        World world = store.getExternalData().getWorld();
        if (world.getTick() < (lifetime.startTick + lifetime.lifeTickLength)) return;

        int blockStateInfoIndex = blockStateInfo.getIndex();

        // This gets the x and z local coords and uses the below methods to get the global coords
        int x = ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getX(),
                                                   ChunkUtil.xFromBlockInColumn(blockStateInfoIndex));
        int y = ChunkUtil.yFromBlockInColumn(blockStateInfoIndex);
        int z = ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getZ(),
                                                   ChunkUtil.zFromBlockInColumn(blockStateInfoIndex));

        long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);

        if (worldChunk == null) {
            getLogger().at(Level.INFO).log("Aura worldChunk not found");
            return;
        }

        world.execute(() -> {
            worldChunk.setTicking(x, y, z, false);
            worldChunk.setBlock(x, y, z, AURA_AIR_BLOCK.id());
        });
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.lifetimeType, BlockModule.BlockStateInfo.getComponentType());
    }
}