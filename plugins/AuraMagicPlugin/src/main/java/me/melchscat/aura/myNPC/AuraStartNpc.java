package me.melchscat.aura.myNPC;

import com.google.gson.Gson;
import com.hypixel.hytale.math.vector.Vector3i;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.main.AuraMain;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;
import static me.melchscat.aura.myNPC.AuraStartNpcStatus.AURA_START_NPC_STUCK_IN_POT;
import static me.melchscat.aura.myNPC.AuraStartNpcSubStatus.AURA_START_NPC_IDLE;

public class AuraStartNpc {
    public static class JsonProps {
        public AuraStartNpcStatus state = AURA_START_NPC_STUCK_IN_POT;
        public int maxActiveRange = 10;
    }

    private AuraMain auraMain;
    private final String fileName = "StartNPCConfig.json";
    private Path fullFilePath;

    // main status is saved in file, substatus is not.
    // NPC will always start off as idle
    public JsonProps jsonProps;
    public AuraStartNpcSubStatus SubStatus = AURA_START_NPC_IDLE;
    public Boolean hasOurCoord = false;
    public Vector3i ourCoord;
    public Long statusTick;
    public Long statusDelay;
    public Boolean busyInStatus = false;

    public AuraStartNpc (AuraMain auraMain) {
        this.auraMain = auraMain;
        fullFilePath = auraMain.auraDataPath.resolve(fileName);

        jsonProps = new JsonProps();
        statusTick = auraMain.world.getTick();
        statusDelay = (long)0;
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
        return writeJsonProps();
    }
}