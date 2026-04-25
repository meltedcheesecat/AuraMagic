package me.melchscat.aura.myNPC;

import com.google.gson.Gson;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import me.melchscat.aura.main.AuraMain;
import static me.melchscat.aura.myNPC.AuraStartNpcStatus.*;
import static me.melchscat.aura.myNPC.AuraStartNpcSubStatus.*;
import static me.melchscat.aura.myNPC.AuraStartNpcAnimationStatus.*;

public class AuraStartNpc {
    public static class JsonProps {
        public AuraStartNpcStatus state = AURA_START_NPC_STUCK_IN_POT;
        public AuraStartNpcSubStatus subState = AURA_START_NPC_START;
        public int maxActiveRange = 10;
    }

    private final ConcurrentLinkedQueue<PlayerRef> talkRequestQueue = new ConcurrentLinkedQueue<>();
    private AuraMain auraMain;
    private final String fileName = "StartNPCConfig.json";
    private Path fullFilePath;

    // main status is saved in file, substatus is not.
    // NPC will always start off as idle
    public JsonProps jsonProps;
    public Boolean hasOurCoord = false;
    public Vector3i ourCoord;
    public AuraStartNpcAnimationStatus aniState = AURA_START_NPC_IDLE_POT;
    public Long statusTick;
    public Long statusDelay;
    public Long pageTick;
    public Long pageDelay;
    public Boolean hasPageResponse = false;
    public int pageResponse = 0;
    public PlayerRef npcTalkingToPlayerRef = null;
    public PlayerRef disconnectPlayerRef = null;
    public Boolean checkDisconnectPlayerRef = false;
    public Boolean doOnceOffAnimation = false;
    public Long greetTick;
    public Long greetDelay;
    public int nearestPlayerDist;
    public Boolean removeItemFromPlayerInventory = false;
    public String itemIdToRemove;
    public int itemToRemoveQuantity;
    public Boolean removedItemSuccessfully = false;
    public Vector3f posOffset = new Vector3f(0.0f, 0.0f, 0.0f);

    public AuraStartNpc (AuraMain auraMain) {
        this.auraMain = auraMain;
        fullFilePath = auraMain.auraDataPath.resolve(fileName);

        jsonProps = new JsonProps();
    }

    private boolean readJsonProps() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(fullFilePath.toFile())) {
            jsonProps = gson.fromJson(reader, JsonProps.class);
        } catch (IOException e) {
            getLogger().at(Level.SEVERE).log("StartNPC readJsonProps Error:" + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean writeJsonProps() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(fullFilePath.toFile())) {
            gson.toJson(jsonProps, writer);
        } catch (IOException e) {
            getLogger().at(Level.SEVERE).log("StartNPC writeJsonProps Error:" + e.getMessage());
            return false;
        }
        return true;
    }

    public void updateValidSaveSubState() {
        switch (this.jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (this.jsonProps.subState) {
                    case AURA_START_NPC_TALK_1: {
                        this.jsonProps.subState = AURA_START_NPC_START;
                        break;
                    }
                    case AURA_START_NPC_TALK_2: {
                        this.jsonProps.subState = AURA_START_NPC_DECLINE_QUEST;
                        break;
                    }
                    case AURA_START_NPC_TALK_3: {
                        this.jsonProps.subState = AURA_START_NPC_ACCEPT_QUEST;
                        break;
                    }
                    case AURA_START_NPC_TALK_4: {
                        this.jsonProps.state = AURA_START_NPC_OUT_OF_POT;
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

    public boolean readData() {
        // writes defaults if file does not exist
        if (!Files.exists(fullFilePath)) {
            if (!writeJsonProps()) {
                return false;
            }
        }

        return readJsonProps();
    }

    public boolean writeData() {
        // if talking when exiting go to previous state
        if (this.jsonProps.subState.isTalk) {
            updateValidSaveSubState();
        }

        return writeJsonProps();
    }

    public void init(AuraMain auraMain) {
        hasPageResponse = false;
        statusTick = auraMain.world.getTick();
        statusDelay = auraMain.world.getTps() * (long) 2; // 2 second delay

        switch (jsonProps.state) {
            case AURA_START_NPC_STUCK_IN_POT: {
                switch (jsonProps.subState) {
                    case AURA_START_NPC_START: {
                        aniState = AURA_START_NPC_IDLE_POT;
                        break;
                    }
                    case AURA_START_NPC_DECLINE_QUEST: {
                        aniState = AURA_START_NPC_IDLE_POT;
                        break;
                    }
                    case AURA_START_NPC_ACCEPT_QUEST: {
                        aniState = AURA_START_NPC_IDLE_POT;
                        greetTick = statusTick;
                        greetDelay = (long)0; // on start set to zero so always greets when seen
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

    public void requestTalk(PlayerRef playerRef) {
        // player already in the queue ignore request
        if (talkRequestQueue.contains(playerRef)) return;

        talkRequestQueue.add(playerRef);
    }

    public PlayerRef nextTalkRequest() {
        return talkRequestQueue.poll(); // Pops the head of the queue, returns null if empty
    }
}