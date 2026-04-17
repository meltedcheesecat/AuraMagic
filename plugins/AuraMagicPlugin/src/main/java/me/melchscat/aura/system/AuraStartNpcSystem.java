package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.block.AuraBlocks.AURA_START_NPC_BLOCK;
import static me.melchscat.aura.myNPC.AuraStartNpcSubStatus.AURA_START_NPC_ANIMATION;
import static me.melchscat.aura.myNPC.AuraStartNpcSubStatus.AURA_START_NPC_IDLE;

public class AuraStartNpcSystem extends EntityTickingSystem<ChunkStore> {
    private static final String IN_POT_IDLE = "InPotIdle";
    private static final String IN_POT_SHAKE = "InPotShake";
    private final ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType;
    private BlockType startNPCBlockType;

    public AuraStartNpcSystem(ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType) {
        this.auraStartNpcComponentType = auraStartNpcComponentType;
    }

    private List<PlayerRef> playersInRange(World world, AuraStartNpc startNPC) {
        Collection<PlayerRef> playerRefs = world.getPlayerRefs();

        List<PlayerRef> selPlayerRefs = new ArrayList<>();
        for (PlayerRef playerRef : playerRefs) {
            if (playerRef == null) continue;
            if (!playerRef.isValid()) continue;

            Vector3d playerPosition = playerRef.getTransform().getPosition();
            Vector3i playerPos = new Vector3i(playerPosition.toVector3i());

            double distToPlayer = playerPos.distanceTo(startNPC.ourCoord);

            if (distToPlayer <= (double)startNPC.jsonProps.maxActiveRange) {
                selPlayerRefs.add(playerRef);
            }
        }

        return selPlayerRefs;
    }

    private void npcStuckInPot(AuraStartNpc startNPC, World world){
        if (startNPC.statusTick + startNPC.statusDelay > world.getTick()) return;

        List<PlayerRef> selPlayerRefs = playersInRange(world, startNPC);
        if (selPlayerRefs.isEmpty()) return;

        long chunkIndex = ChunkUtil.indexChunkFromBlock(startNPC.ourCoord.getX(), startNPC.ourCoord.getZ());
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
        if (worldChunk == null) return;

        if (startNPCBlockType == null) {
            startNPCBlockType = (BlockType)BlockType.getAssetMap().getAsset(AURA_START_NPC_BLOCK.id());
            if (startNPCBlockType == null) return;
        }

        switch (startNPC.SubStatus) {
            case AURA_START_NPC_IDLE: {
                // Sound was not playing on state change but everything else was working
                // even when I activated the item using an interaction the sound would play fine.
                // So this is a hack to get it working
                for (PlayerRef playerRef : selPlayerRefs) {
                    int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Shake");
                    if (soundEventIndex == 0) return;
                    SoundUtil.playSoundEvent2dToPlayer(playerRef, soundEventIndex, SoundCategory.SFX);
                }

                startNPC.SubStatus = AURA_START_NPC_ANIMATION;
                startNPC.statusTick = world.getTick();
                startNPC.statusDelay = (long)world.getTps(); // 1 second
                world.execute(() -> {
                    worldChunk.setBlockInteractionState(startNPC.ourCoord, startNPCBlockType, IN_POT_SHAKE);
                });
                break;
            }
            case AURA_START_NPC_ANIMATION: {
                startNPC.SubStatus = AURA_START_NPC_IDLE;
                startNPC.statusTick = world.getTick();
                // 1/2 a second plus 0 to 2 seconds longer
                startNPC.statusDelay = (long)(world.getTps()/(long)2) +
                                       ThreadLocalRandom.current().nextLong(world.getTps()*(long)2);
                world.execute(() -> {
                    worldChunk.setBlockInteractionState(startNPC.ourCoord, startNPCBlockType, IN_POT_IDLE);
                });
                break;
            }
        }
    }

    @Override
    public void tick(float dt,
                     int index,
                     @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
                     @Nonnull Store<ChunkStore> store,
                     @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
        AuraStartNpcComponent startNpcComponent = archetypeChunk.getComponent(index, this.auraStartNpcComponentType);
        if (startNpcComponent == null) return;
        if (!startNpcComponent.enabled) return;

        BlockModule.BlockStateInfo blockStateInfo = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());
        if (blockStateInfo == null) return;

        Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
        if (!chunkRef.isValid()) return;

        BlockChunk blockChunkComponent = (BlockChunk)commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunkComponent == null) return;

        if (startNpcComponent.auraStartNpc == null) {
            startNpcComponent.auraStartNpc = AuraMagicPlugin.getInstance().getStartNPC();
            if (startNpcComponent.auraStartNpc == null) return;
        }
        AuraStartNpc startNPC = startNpcComponent.auraStartNpc;

        // the npc block coordinates never change so get the once
        if (!startNPC.hasOurCoord) {
            int blockStateInfoIndex = blockStateInfo.getIndex();
            // This gets the x and z local coords and uses the below methods to get the global coords
            int x = ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getX(),
                    ChunkUtil.xFromBlockInColumn(blockStateInfoIndex));
            int y = ChunkUtil.yFromBlockInColumn(blockStateInfoIndex);
            int z = ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getZ(),
                    ChunkUtil.zFromBlockInColumn(blockStateInfoIndex));

            startNPC.ourCoord = new Vector3i(x,y,z);
            startNPC.hasOurCoord = true;
        }

        World world = store.getExternalData().getWorld();

        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT : {
                npcStuckInPot(startNPC, world);
                break;
            }
        }
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.auraStartNpcComponentType, BlockModule.BlockStateInfo.getComponentType());
    }
}
