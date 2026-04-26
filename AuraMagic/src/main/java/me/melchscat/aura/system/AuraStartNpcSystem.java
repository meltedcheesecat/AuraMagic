package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.Page;
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
import me.melchscat.aura.AuraMagic;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;
import me.melchscat.aura.myNPC.AuraStartNpcAnimationStatus;
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
    private static final String IN_POT_TALK = "InPotTalk";
    private static final String IN_POT_SULK = "InPotSulk";
    private static final String IN_POT_ACCEPT = "InPotAccept";
    private static final String IN_POT_CALL = "InPotCall";
    private static final String IN_POT_RELEASE_1 = "InPotRelease1";
    private static final String IN_POT_RELEASE_2 = "InPotRelease2";
    private static final String IN_POT_RELEASE_3 = "InPotRelease3";
    private static final String IN_POT_RELEASE_4 = "InPotRelease4";
    private static final String IN_POT_RELEASE_5 = "InPotRelease5";
    private static final String IN_POT_RELEASE_6 = "InPotRelease6";
    private static final String IN_POT_IDLE_RELEASE_1 = "InPotIdleRelease1";
    private static final String IN_POT_IDLE_RELEASE_2 = "InPotIdleRelease2";

    private final ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType;
    private BlockType startNPCBlockType;

    public AuraStartNpcSystem(ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcComponentType) {
        this.auraStartNpcComponentType = auraStartNpcComponentType;
    }

    private List<PlayerRef> playersInRange(World world, AuraStartNpc startNPC) {
        Collection<PlayerRef> playerRefs = world.getPlayerRefs();
        startNPC.nearestPlayerDist = 9999;

        List<PlayerRef> selPlayerRefs = new ArrayList<>();
        for (PlayerRef playerRef : playerRefs) {
            if (playerRef == null) continue;
            if (!playerRef.isValid()) continue;

            Vector3d playerPosition = playerRef.getTransform().getPosition();
            Vector3i playerPos = new Vector3i(playerPosition.toVector3i());

            double distToPlayer = playerPos.distanceTo(startNPC.ourCoord);

            if (startNPC.nearestPlayerDist > (int)distToPlayer) {
                startNPC.nearestPlayerDist = (int)distToPlayer;
            }

            if (distToPlayer <= (double)startNPC.jsonProps.maxActiveRange) {
                selPlayerRefs.add(playerRef);
            }
        }

        return selPlayerRefs;
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

    private void sendNPCInPotPart2(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
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
            if (index == 1) {
                storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part2.StoryStr" + index).getAnsiMessage());
            } else {
                storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part1.StoryStr" + index).getAnsiMessage());
            }
            storyStrLst.add("Story_Image_StuckInPot_Part1_Story" + index + ".png");
        }

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    true, "Decline Quest", true, "Accept Quest", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void sendNPCInPotPart3(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.StuckInPot.ImageTitle").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_StuckInPot.png";

        List<String> storyStrLst = new ArrayList<>();
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part3.StoryStr1").getAnsiMessage());
        storyStrLst.add("Story_Image_StuckInPot_Part3_Story1.png");
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part3.StoryStr2").getAnsiMessage());
        storyStrLst.add("Story_Image_StuckInPot_Part1_Story4.png");

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    false, "NoButton", true, "Ok", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void sendNPCInPotPart4(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.StuckInPot.ImageTitle").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_StuckInPot.png";

        List<String> storyStrLst = new ArrayList<>();
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part4.StoryStr1").getAnsiMessage());
        storyStrLst.add("Story_Image_StuckInPot_Part4_Story1.png");
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.StuckInPot.Part4.StoryStr2").getAnsiMessage());

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    false, "NoButton", true, "Ok", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void sendNPCOutPotPart1(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.OutPot.ImageTitle1").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_Ghost.png";
        List<String> storyStrLst = new ArrayList<>();

        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.OutPot.Part1.StoryStr1").getAnsiMessage());
        storyStrLst.add("Story_Image_OutPot_Part1_Story1.png");
        storyStrLst.add(Message.translation("server.auraMagic.StartNPC.OutPot.Part1.StoryStr2").getAnsiMessage());

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    false, "NoButton", true, "Next", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void sendNPCOutPotPart2(World world, Store<EntityStore> store, PlayerRef playerRef, Player player, Ref<EntityStore> ref) {
        if (world == null) return;
        if (store == null) return;
        if (playerRef == null) return;
        if (player == null) return;
        if (ref == null) return;

        String titleStr = Message.translation("server.auraMagic.StartNPC.Dialog.Title").getAnsiMessage();
        String imageTitleStr = Message.translation("server.auraMagic.StartNPC.OutPot.ImageTitle2").getAnsiMessage();
        String mainImgForNPC = "NPC_Image_Ghost.png";
        List<String> storyStrLst = new ArrayList<>();

        for (int index = 1; index <= 5; index++) {
            storyStrLst.add(Message.translation("server.auraMagic.StartNPC.OutPot.Part2.WindMagic" + index).getAnsiMessage());
            storyStrLst.add("Story_Image_OutPot_Part2_WindMagic" + index + ".png");
        }

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new
                    AuraStartNpcPage(playerRef, titleStr, imageTitleStr, mainImgForNPC, storyStrLst,
                    false, "NoButton", true, "Close", true);

            player.getPageManager().openCustomPage(ref, store, startNpcPage);
        });
    }

    private void resetAnimation(World world, AuraStartNpc startNPC) {
        startNPC.aniState = AURA_START_NPC_IDLE_POT;
        startNPC.statusTick = world.getTick();
        startNPC.statusDelay = world.getTps() / (long)2;
    }

    private void setPageCloseDelay(World world, AuraStartNpc startNPC, int secDelay) {
        startNPC.pageTick = world.getTick();
        startNPC.pageDelay = world.getTps() * (long)secDelay;
    }

    private void updateNPCStatusFromResponse(World world, AuraStartNpc startNPC) {
        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_TALK_1: {
                        // First time we are talking to NPC, 0-esc do nothing, 1-decline, 2-accept
                        if (startNPC.pageResponse == 1) {
                            startNPC.jsonProps.subState = AURA_START_NPC_DECLINE_QUEST;
                            /* When starting a page from another page you can do this and set the pres status before
                            if (startNPC.npcTalkingToPlayerRef.isValid()) {
                                startNPC.requestTalk(startNPC.npcTalkingToPlayerRef);
                            }*/
                        } else if (startNPC.pageResponse == 2) {
                            startNPC.jsonProps.subState = AURA_START_NPC_ACCEPT_QUEST;
                            startNPC.doOnceOffAnimation = true;
                            startNPC.greetTick = world.getTick();
                            startNPC.greetDelay = world.getTps() * (long)300;
                            startNPC.removeItemFromPlayerInventory = true;
                            startNPC.itemIdToRemove = "Potion_Mana_Large";
                            startNPC.itemToRemoveQuantity = 1;
                            startNPC.removedItemSuccessfully = false;
                        } else {
                            startNPC.jsonProps.subState = AURA_START_NPC_START;
                        }
                        break;
                    }
                    case AURA_START_NPC_TALK_2: {
                        // We previously declined the quest, 0-esc do nothing, 1-decline, 2-accept
                        if (startNPC.pageResponse == 1) {
                            startNPC.jsonProps.subState = AURA_START_NPC_DECLINE_QUEST;
                        } else if (startNPC.pageResponse == 2) {
                            startNPC.jsonProps.subState = AURA_START_NPC_ACCEPT_QUEST;
                            startNPC.doOnceOffAnimation = true;
                            startNPC.greetTick = world.getTick();
                            startNPC.greetDelay = world.getTps() * (long)300;
                            startNPC.removeItemFromPlayerInventory = true;
                            startNPC.itemIdToRemove = "Potion_Mana_Large";
                            startNPC.itemToRemoveQuantity = 1;
                            startNPC.removedItemSuccessfully = false;
                        } else {
                            startNPC.jsonProps.subState = AURA_START_NPC_DECLINE_QUEST;
                        }
                        break;
                    }
                    case AURA_START_NPC_TALK_3: {
                        // We previously accepted the quest but didn't bring back the item
                        // ignore response go back to accept quest
                        startNPC.jsonProps.subState = AURA_START_NPC_ACCEPT_QUEST;
                        break;
                    }
                    case AURA_START_NPC_TALK_4: {
                        // We previously accepted the quest and did bring back the item
                        // 0-esc do nothing, 1-decline, 2-accept
                        startNPC.jsonProps.state = AURA_START_NPC_OUT_OF_POT;
                        startNPC.jsonProps.subState = AURA_START_NPC_INIT_1;
                        break;
                    }
                }
                break;
            }
            case AURA_START_NPC_OUT_OF_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_TALK_1: {
                        startNPC.jsonProps.subState = AURA_START_NPC_START_2;
                        if (startNPC.npcTalkingToPlayerRef.isValid()) {
                            startNPC.requestTalk(startNPC.npcTalkingToPlayerRef);
                        }
                        break;
                    }
                    case AURA_START_NPC_TALK_2: {
                        startNPC.jsonProps.subState = AURA_START_NPC_START_2;
                        break;
                    }
                }
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

        // stores the current player so we can force close the dialog after timeout
        startNPC.npcTalkingToPlayerRef = playerRef;

        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_START: {
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_1;
                        setPageCloseDelay(world, startNPC, 120); // 2 minutes before dialog closes
                        sendNPCInPotPart1(world, store, playerRef, player, ref);
                        break;
                    }
                    case AURA_START_NPC_DECLINE_QUEST: {
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_2;
                        setPageCloseDelay(world, startNPC, 120); // 2 minutes before dialog closes
                        sendNPCInPotPart2(world, store, playerRef, player, ref);
                        break;
                    }
                    case AURA_START_NPC_ACCEPT_QUEST: {
                        if ((!startNPC.removeItemFromPlayerInventory) || (!startNPC.removedItemSuccessfully)) {
                            // failed to get mana potion from player
                            startNPC.removeItemFromPlayerInventory = true;
                            startNPC.itemIdToRemove = "Potion_Mana_Large";
                            startNPC.itemToRemoveQuantity = 1;
                            startNPC.removedItemSuccessfully = false;
                            startNPC.jsonProps.subState = AURA_START_NPC_TALK_3;
                            setPageCloseDelay(world, startNPC, 90); // 1/2 minutes before dialog closes
                            sendNPCInPotPart3(world, store, playerRef, player, ref);
                            break;
                        }
                        //got mana potion from player
                        startNPC.removeItemFromPlayerInventory = false;
                        startNPC.removedItemSuccessfully = false;
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_4;
                        setPageCloseDelay(world, startNPC, 90); // 1/2 minutes before dialog closes
                        sendNPCInPotPart4(world, store, playerRef, player, ref);
                        break;
                    }
                }
                break;
            }
            case AURA_START_NPC_OUT_OF_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_START: {
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_1;
                        setPageCloseDelay(world, startNPC, 120); // 2 minutes before dialog closes
                        sendNPCOutPotPart1(world, store, playerRef, player, ref);
                        break;
                    }
                    case AURA_START_NPC_START_2: {
                        startNPC.jsonProps.subState = AURA_START_NPC_TALK_2;
                        setPageCloseDelay(world, startNPC, 120); // 2 minutes before dialog closes
                        sendNPCOutPotPart2(world, store, playerRef, player, ref);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateNPCBlock(AuraStartNpc startNPC, World world, WorldChunk worldChunk, AuraStartNpcAnimationStatus aniState, String strStart, long millis) {
        startNPC.aniState = aniState;
        startNPC.statusTick = world.getTick();
        startNPC.statusDelay = (long)((float)world.getTps()*((float)millis/1000.0F)); // convert millis to ticks per second
        world.execute(() -> {
            worldChunk.setBlockInteractionState(startNPC.ourCoord, startNPCBlockType, strStart);
        });
    }

    private void handleAnimation(AuraStartNpc startNPC, World world, List<PlayerRef> selPlayerRefs, WorldChunk worldChunk){
        if (startNPC.statusTick + startNPC.statusDelay > world.getTick()) return;

        switch (startNPC.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_START: {
                        if (startNPC.aniState == AURA_START_NPC_IDLE_POT) {
                            // Sound was not playing on state change but everything else was working
                            // even when I activated the item using an interaction the sound would play fine.
                            // So this is a hack to get it working
                            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Shake");
                            if (soundEventIndex == 0) return;
                            SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                            soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Voice_Call");
                            if (soundEventIndex == 0) return;
                            SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());

                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_SHAKE_POT, IN_POT_SHAKE, (long)1000);
                        } else if (startNPC.aniState == AURA_START_NPC_SHAKE_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_POT, IN_POT_IDLE,
                                    ThreadLocalRandom.current().nextLong((long)3000) + (long)1000);
                        }
                        break;
                    }
                    case AURA_START_NPC_TALK_1:
                    case AURA_START_NPC_TALK_2:
                    case AURA_START_NPC_TALK_3:
                    case AURA_START_NPC_TALK_4:
                    {
                        // All Talking does the same thing
                        if (startNPC.aniState == AURA_START_NPC_IDLE_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_TALK_POT, IN_POT_TALK, (long)1000);
                        } else if (startNPC.aniState == AURA_START_NPC_TALK_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_POT, IN_POT_IDLE, (long)500);
                        }
                        break;
                    }
                    case AURA_START_NPC_DECLINE_QUEST: {
                        // sulks every now and again
                        if (startNPC.aniState == AURA_START_NPC_IDLE_POT) {
                            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Sulk");
                            if (soundEventIndex == 0) return;
                            SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                            soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Voice_Sulk");
                            if (soundEventIndex == 0) return;
                            SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_SULK_POT, IN_POT_SULK, (long)2000);
                        } else if (startNPC.aniState == AURA_START_NPC_SULK_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_POT, IN_POT_IDLE,
                                     ThreadLocalRandom.current().nextLong((long)3000) + (long)3000);
                        }
                        break;
                    }
                    case AURA_START_NPC_ACCEPT_QUEST: {
                        // Jumps for joy once and calls after not seeing you for more than 5 minutes
                        if (startNPC.aniState == AURA_START_NPC_IDLE_POT) {
                            if (startNPC.doOnceOffAnimation) {
                                startNPC.doOnceOffAnimation = false;
                                int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Accept");
                                if (soundEventIndex == 0) return;
                                SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                                soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Voice_Accept");
                                if (soundEventIndex == 0) return;
                                SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                                startNPC.greetTick = world.getTick();
                                startNPC.greetDelay = world.getTps() * (long)180; //3 minutes
                                updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_ACCEPT_POT, IN_POT_ACCEPT, (long)2000);
                            } else {
                                if (startNPC.greetTick + startNPC.greetDelay <= world.getTick()) {
                                    if (startNPC.nearestPlayerDist < 7) {
                                        int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Call");
                                        if (soundEventIndex == 0) return;
                                        SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                                        soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Aura_Start_NPC_Voice_Call");
                                        if (soundEventIndex == 0) return;
                                        SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                                        startNPC.greetTick = world.getTick();
                                        startNPC.greetDelay = world.getTps() * (long)180; //3 minutes
                                        updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_CALL_POT, IN_POT_CALL, (long)2000);
                                    }
                                }

                                // redo Idle timer nothing else
                                startNPC.statusTick = world.getTick();
                                startNPC.statusDelay = (long)world.getTps() * (long)2; // 2 second s
                            }
                        } else if ((startNPC.aniState == AURA_START_NPC_ACCEPT_POT) ||
                                   (startNPC.aniState == AURA_START_NPC_CALL_POT)){
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_POT, IN_POT_IDLE, (long)2000);
                        }
                        break;
                    }
                }
                break;
            }
            case AURA_START_NPC_OUT_OF_POT: {
                switch (startNPC.jsonProps.subState) {
                    case AURA_START_NPC_INIT_1: {
                        if (startNPC.aniState == AURA_START_NPC_IDLE_POT) {
                            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Staff_Charged_Loop");
                            if (soundEventIndex == 0) return;
                            SoundUtil.playSoundEvent3d(soundEventIndex, SoundCategory.SFX, startNPC.ourCoordd, world.getEntityStore().getStore());
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE1_POT, IN_POT_RELEASE_1, (long)250);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE1_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE2_POT, IN_POT_RELEASE_2, (long)250);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE2_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE3_POT, IN_POT_RELEASE_3, (long)250);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE3_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE4_POT, IN_POT_RELEASE_4, (long)1000);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE4_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE5_POT, IN_POT_RELEASE_5, (long)1000);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE5_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_RELEASE6_POT, IN_POT_RELEASE_6, (long)250);
                        } else if (startNPC.aniState == AURA_START_NPC_RELEASE6_POT) {
                            startNPC.jsonProps.subState = AURA_START_NPC_START;
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_RELEASE1_POT, IN_POT_IDLE_RELEASE_1, (long)1000);
                        }
                        break;
                    }
                    case AURA_START_NPC_START :
                    case AURA_START_NPC_START_2 : {
                        if (startNPC.aniState == AURA_START_NPC_IDLE_RELEASE1_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_RELEASE2_POT, IN_POT_IDLE_RELEASE_2, (long)1000);
                        } else if (startNPC.aniState == AURA_START_NPC_IDLE_RELEASE2_POT) {
                            updateNPCBlock(startNPC, world, worldChunk, AURA_START_NPC_IDLE_RELEASE1_POT, IN_POT_IDLE_RELEASE_1, (long)1000);
                        }
                        break;
                    }
                }
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
        // is our Start NPC Component loaded
        AuraStartNpcComponent startNpcComponent = archetypeChunk.getComponent(index, this.auraStartNpcComponentType);
        if (startNpcComponent == null) return;
        if (!startNpcComponent.enabled) return;

        // is our attached start npc instance loaded
        if (startNpcComponent.auraStartNpc == null) {
            startNpcComponent.auraStartNpc = AuraMagic.getInstance().getStartNPC();
            if (startNpcComponent.auraStartNpc == null) return;
        }
        AuraStartNpc startNPC = startNpcComponent.auraStartNpc;

        // Get the Chunk objects and load our blocks coord once
        BlockModule.BlockStateInfo blockStateInfo = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());
        if (blockStateInfo == null) return;

        Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
        if (!chunkRef.isValid()) return;

        BlockChunk blockChunkComponent = (BlockChunk)commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunkComponent == null) return;

        World world = store.getExternalData().getWorld();
        if (!world.isStarted()) return;
        if (world.getPlayerCount() == 0) return;

        if (startNPCBlockType == null) {
            startNPCBlockType = (BlockType)BlockType.getAssetMap().getAsset(AURA_START_NPC_BLOCK.id());
            if (startNPCBlockType == null) return;
        }

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
            startNPC.ourCoordd = new Vector3d(x,y,z);
            startNPC.hasOurCoord = true;
        }

        long chunkIndex = ChunkUtil.indexChunkFromBlock(startNPC.ourCoord.getX(), startNPC.ourCoord.getZ());
        WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
        if (worldChunk == null) return;

        // Are there any players that are ready
        List<PlayerRef> selPlayerRefs = playersInRange(world, startNPC);
        if (selPlayerRefs.isEmpty()) return;
        boolean foundAnyReadyPlayer = false;
        for (PlayerRef playerRef : selPlayerRefs) {
            if (playerRef.isValid()) {
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref == null) continue;
                Store<EntityStore> entityStore = ref.getStore();
                Player player = entityStore.getComponent(ref, Player.getComponentType());
                if (player == null) continue;
                if (!player.isWaitingForClientReady()) {
                    foundAnyReadyPlayer = true;
                    break;
                }
            }
        }
        if (!foundAnyReadyPlayer) return;

        // NPC Dialog that we sent out previous came back with response
        if (startNPC.hasPageResponse) {
            resetAnimation(world, startNPC);
            updateNPCStatusFromResponse(world, startNPC);
            startNPC.hasPageResponse = false;
            return;
        }

        // check if the disconnected player was talking to the NPC
        if (startNPC.checkDisconnectPlayerRef) {
            if (startNPC.disconnectPlayerRef.equals(startNPC.npcTalkingToPlayerRef)) {
                resetAnimation(world, startNPC);
                startNPC.updateValidSaveSubState();
            }
            startNPC.checkDisconnectPlayerRef = false;
        }

        // check if the open talk form has timed out and close form
        if (startNPC.jsonProps.subState.isTalk) {
            if (startNPC.pageTick + startNPC.pageDelay <= world.getTick()) {
                resetAnimation(world, startNPC);
                startNPC.updateValidSaveSubState();
                if (startNPC.npcTalkingToPlayerRef.isValid()) {
                    Ref<EntityStore> npcTalkPlyrRef = startNPC.npcTalkingToPlayerRef.getReference();
                    if (npcTalkPlyrRef != null) {
                        Store<EntityStore> npcTalkEntityStore = npcTalkPlyrRef.getStore();
                        Player npcTalkPlayer = npcTalkEntityStore.getComponent(npcTalkPlyrRef, Player.getComponentType());

                        if (npcTalkPlayer != null) {
                            world.execute( () -> {
                                npcTalkPlayer.getPageManager().setPage(npcTalkPlyrRef, npcTalkEntityStore, Page.None);
                            });
                        }
                    }
                }
            }
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
                resetAnimation(world, startNPC);
                sendNPCInPotMsg(world, entityStore, talkReqPlyrRef, player, ref, startNPC);
            }
        }

        // Handle Animation
        handleAnimation(startNPC, world, selPlayerRefs, worldChunk);
    }

    @Nonnull
    @Override
    public Query<ChunkStore> getQuery() {
        return Query.and(this.auraStartNpcComponentType, BlockModule.BlockStateInfo.getComponentType());
    }
}