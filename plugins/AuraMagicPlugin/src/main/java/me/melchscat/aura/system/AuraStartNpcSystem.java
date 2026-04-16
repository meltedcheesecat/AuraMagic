package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AuraStartNpcSystem extends EntityTickingSystem<ChunkStore> {
    private final ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType;

    public AuraStartNpcSystem(ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType) {
        this.auraStartNpcComponentType = auraStartNpcComponentType;
    }

    private List<PlayerRef> playersInRange(World world, AuraStartNpc startNPC) {
        Collection<PlayerRef> playerRefs = world.getPlayerRefs();

        List<PlayerRef> selPlayerRefs = new ArrayList<>(playerRefs);
        for (PlayerRef playerRef : playerRefs) {
            if (playerRef == null) continue;
            if (!playerRef.isValid()) continue;

            Vector3i playerPos = new Vector3i();
            Vector3d playerPosition = playerRef.getTransform().getPosition();
            playerPosition.assign(playerPos);

            double distToPlayer = playerPos.distanceTo(startNPC.ourCoord);
            if (distToPlayer <= (double)startNPC.jsonProps.maxActiveRange) {
                selPlayerRefs.add(playerRef);
            }
        }

        return selPlayerRefs;
    }

    private void npcStuckInPot(){

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

            startNPC.ourCoord.setX(x);
            startNPC.ourCoord.setY(y);
            startNPC.ourCoord.setZ(z);

            startNPC.hasOurCoord = true;
        }

        World world = store.getExternalData().getWorld();
        List<PlayerRef> selPlayerRefs = playersInRange(world, startNPC);
        if (selPlayerRefs.isEmpty()) return;

        switch (startNpcComponent.auraStartNpc.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT : {
                npcStuckInPot();
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
