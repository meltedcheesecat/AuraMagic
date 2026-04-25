package me.melchscat.aura.main;

import com.hypixel.hytale.server.core.universe.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraMain {
    public String AURA_DATA_NAME = "Data";
    public String AURA_FOLDER_NAME = "AuraMagic";
    public String AURA_PLAYER_FOLDER_NAME = "Players";


    public boolean initialized = false;
    public World world;
    public Path worldSavePath;
    public String worldName;
    public Path auraDataPath;
    public Path auraPlayerPath;

    public AuraMain() {

    }

    public void Initialize(World world) {
        this.world = world;
        if (this.world == null) return;

        // this returns universe\worlds\[name] i.e. this should be the save folder name
        this.worldSavePath = world.getSavePath().toAbsolutePath().getParent().getParent().getParent();
        if (this.worldSavePath == null) return;

        worldName = worldSavePath.getFileName().toString();

        auraDataPath = worldSavePath.resolve(AURA_DATA_NAME).resolve(AURA_FOLDER_NAME);

        if (!Files.exists(auraDataPath)) {
            try {
                Files.createDirectories(auraDataPath);
            } catch (IOException e) {
                getLogger().at(Level.SEVERE).log("AuraMain Initialize Error creating:" + auraDataPath);
                throw new RuntimeException(e);
            }
        }

        auraPlayerPath = auraDataPath.resolve(AURA_PLAYER_FOLDER_NAME);
        if (!Files.exists(auraPlayerPath)) {
            try {
                Files.createDirectories(auraPlayerPath);
            } catch (IOException e) {
                getLogger().at(Level.SEVERE).log("AuraMain Initialize Error creating:" + auraPlayerPath);
                throw new RuntimeException(e);
            }
        }

        initialized = true;
    }
}
