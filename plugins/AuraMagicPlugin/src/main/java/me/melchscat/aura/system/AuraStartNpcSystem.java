package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;
import me.melchscat.aura.page.AuraStartNpcPage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.melchscat.aura.block.AuraBlocks.*;
import static me.melchscat.aura.myNPC.AuraStartNpcStatus.*;
import static me.melchscat.aura.myNPC.AuraStartNpcSubStatus.*;
import static me.melchscat.aura.myNPC.AuraStartNpcAnimationStatus.*;

public class AuraStartNpcSystem extends EntityTickingSystem<ChunkStore> {
    private static final String IN_POT_IDLE = "InPotIdle";
    private static final String IN_POT_SHAKE = "InPotShake";
    private final ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType;
    private BlockType startNPCBlockType;

    public AuraStartNpcSystem(ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType) {
        this.auraStartNpcComponentType = auraStartNpcComponentType;
    }

    private void sendNPCInPotBusy(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.StuckInPot.ImageTitle").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_StuckInPot.png";
        List<String> storyStrLst = new ArrayList<>();
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Busy.StoryStr1").getAnsiMessage());
        storyStrLst.add("Story_Image_StuckInPot_Busy.png");

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                         false, "NoButton", true, "Ok", false);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void sendNPCInPotPart1(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.StuckInPot.ImageTitle").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_StuckInPot.png";
        List<String> storyStrLst = new ArrayList<>();
        for (int index = 1; index <= 5; index++) {
            storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part1.StoryStr" + index).getAnsiMessage());
            storyStrLst.add("Story_Image_StuckInPot_Part1_Story" + index + ".png");
        }

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    true, "Decline Quest", true, "Accept Quest", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void updateNPCStatusFromResponse(AuraStartNpc startNPC) {
        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_TALK_1: {
                        // First time we are talking to NPC, 0-esc do nothing, 1-decline, 2-accept
                        if (startNPC.pageResponse == 1) {
                            startNPC.jsonProps.subState = AURA_START_NPC_DECLINE_QUEST;
                        } else if (startNPC.pageResponse == 2) {
                            startNPC.jsonProps.subState = AURA_START_NPC_ACCEPT_QUEST;
                        }
                        break;
                    }
                    case AURA_START_NPC_TALK_2: {
                        break;
                    }
                    case AURA_START_NPC_TALK_3: {
                        break;
                    }
                    case AURA_START_NPC_TALK_4: {
                        break;
                    }
                }
                break;
            }
            case AURA_START_NPC_OUT_OF_POT: {
                break;
            }
        }
    }

    private void sendNPCInPotMsg(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref, AuraStartNpc startNPC) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_START: {
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_1;
                        startNPC.pageTick = world.getTick();
                        startNPC.pageDelay = world.getTps() * (long)120; // 2 minutes before dialog closes
                        startNPC.aniState = AURA_START_NPC_IDLE_POT;
                        startNPC.statusTick = world.getTick();
                        startNPC.statusDelay = world.getTps() / (long)2; // 1/2 a second
                        sendNPCInPotPart1(world, store, playerRef, player, ref);
                        break;
                    }
                    case AURA_START_NPC_TALK_2: {
                        break;
                    }
                    case AURA_START_NPC_TALK_3: {
                        break;
                    }
                    case AURA_START_NPC_TALK_4: {
                        break;
                    }
                }
                break;
            }
            case AURA_START_NPC_OUT_OF_POT: {
                break;
            }
        }
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

    private void handleAnimation(AuraStartNpc startNPC, World world){
        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT : {
                npcStuckInPot(startNPC, world);
                break;
            }
        }

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
            case AURA_START_NPC_START: {
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
                startNPC.SubStatus = AURA_START_NPC_START;
                startNPC.statusTick = world.getTick();
                // 1 second plus 0 to 2 seconds longer
                startNPC.statusDelay = (long)(world.getTps()) +
                                       ThreadLocalRandom.current().nextLong(world.getTps()*(long)3);
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

        // NPC Dialog that we sent out previous came back with response
        if (startNPC.hasPageResponse) {
            updateNPCStatusFromResponse(startNPC);
        }

        // Check if anyone is trying to talk to us
        PlayerRef talkReqPlyrRef = startNPC.nextTalkRequest();
        if (talkReqPlyrRef != null) {
            Ref<EntityStore> ref = talkReqPlyrRef.getReference();
            if (ref == null) return;
            if (!ref.isValid()) return;
            Store<EntityStore> entityStore = ref.getStore();
            Player player = entityStore.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            if (startNPC.jsonProps.subState.isTalk) {
                sendNPCInPotBusy(world, entityStore, talkReqPlyrRef, player, ref);
            } else {
                sendNPCInPotMsg(world, entityStore, talkReqPlyrRef, player, ref, startNPC);
            }
        }

        // Handle Animation
        handleAnimation(startNPC, world);
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.auraStartNpcComponentType, BlockModule.BlockStateInfo.getComponentType());
    }
}
/* Code was on interaction
World world = player.getWorld();
        if (world == null) return;

String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();

        world.execute( () -> {
AuraStartNpcPage startNpcPage = new AuraStartNpcPage(playerRef, titleStr,
        "Voice from the Jar", "StuckInPotStartNPC.png",
        "You hear a voice coming from the shaking Pot, you walk towards it and say Hello. It responds and tells you it has been stuck in here for ages and it needs help getting out.",
        false, "NoButton", true, "Accept Quest");

            player.getPageManager().openCustomPage(owningEntityRef, store, startNpcPage);
        });
*/