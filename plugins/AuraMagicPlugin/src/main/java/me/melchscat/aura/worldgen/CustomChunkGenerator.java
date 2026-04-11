package me.melchscat.aura.worldgen;

import com.google.gson.JsonObject;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.WorldGenConfig;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.MaskProvider;
import com.hypixel.hytale.server.worldgen.loader.ChunkGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.FileContextLoader;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import com.hypixel.hytale.server.worldgen.loader.zone.ZonePatternProviderJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import com.hypixel.hytale.server.worldgen.zone.Zone;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class CustomChunkGenerator extends ChunkGeneratorJsonLoader {

    private final WorldGenConfig config;
    private World world;


    public CustomChunkGenerator(SeedString<SeedStringResource> seed, WorldGenConfig config, World world) {
        super(seed,config);
        this.config = config;
        this.world = world;
    }

    @Override
    @Nonnull
    public ChunkGenerator load() {

        Path worldFile = this.dataFolder.resolve("World.json").toAbsolutePath();
        if (!Files.exists(worldFile)) {
            throw new IllegalArgumentException(String.valueOf(worldFile));
        } else if (!Files.isReadable(worldFile)) {
            throw new IllegalArgumentException(String.valueOf(worldFile));
        } else {
            JsonObject worldJson = this.loadWorldJson(worldFile);
            Path overrideDataFolder = this.loadOverrideDataFolderPath(worldJson, config.path());
            WorldGenConfig config = this.config.withOverride(overrideDataFolder);

            Vector2i worldSize = this.loadWorldSize(worldJson);
            Vector2i worldOffset = this.loadWorldOffset(worldJson);
            MaskProvider maskProvider = this.loadMaskProvider(worldJson, worldSize, worldOffset);
            PrefabStoreRoot prefabStore = this.loadPrefabStore(worldJson);
            this.seed.get().setPrefabConfig(config,prefabStore);

            ZonePatternProviderJsonLoader loader = this.loadZonePatternGenerator(maskProvider);
            FileLoadingContext loadingContext = new FileContextLoader(config.name(), overrideDataFolder, loader.loadZoneRequirement()).load();
            Zone[] zones = new CustomZonesLoader(this.seed, overrideDataFolder, loadingContext).load();
            loader.setZones(zones);

            ChunkGenerator baseGenerator = new ChunkGenerator(loader.load(), overrideDataFolder);

            // this runs the block generation, this is GenV1 I am hoping when they move to GenV2 I can just do thing
            // in json/setup files
            return new CustomBlockGenerator(baseGenerator, world);
        }
    }

    @Nonnull
    private Path loadOverrideDataFolderPath(@Nonnull JsonObject worldJson, @Nonnull Path dataFolder) {
        if (worldJson.has("OverrideDataFolder")) {
            Path overrideFolder = dataFolder.resolve(worldJson.get("OverrideDataFolder").getAsString()).normalize();
            Path parent = dataFolder.getParent();
            if (overrideFolder.startsWith(parent) && Files.exists(overrideFolder, new LinkOption[0])) {
                return overrideFolder;
            } else {
                throw new Error(String.format("Override folder '%s' must exist within: '%s'", overrideFolder.getFileName(), parent));
            }
        } else {
            return dataFolder;
        }
    }
}
