package me.melchscat.aura.worldgen;

import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.Loader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.FileContext;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.zone.Zone;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map.Entry;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class CustomZonesLoader extends Loader<SeedStringResource, Zone[]> {

    protected final FileLoadingContext loadingContext;

    public CustomZonesLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, FileLoadingContext loadingContext) {

        super(seed.append(".Zones"), dataFolder);
        this.loadingContext = loadingContext;
    }

    @Nonnull
    public Zone[] load() {

        FileContext.Registry<ZoneFileContext> zoneRegistry = this.loadingContext.getZones();
        int index = 0;
        Zone[] zones = new Zone[zoneRegistry.size()];

        for (Entry<String, ZoneFileContext> zoneEntry : zoneRegistry) {
            ZoneFileContext zoneContext = zoneEntry.getValue();

            try {
                JsonObject zoneJson = FileIO.load(zoneContext.getPath().resolve("Zone.json"), JsonLoader.JSON_OBJ_LOADER);
                Zone zone = new CustomZoneLoader(this.seed, this.dataFolder, zoneJson, zoneContext).load();
                zones[index++] = zone;
            } catch (Throwable var9) {
                throw new Error(String.format("Error while loading zone \"%s\" for world generator from file.", zoneContext.getPath().toString()), var9);
            }
        }

        return zones;
    }
}
