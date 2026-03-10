package me.melchscat.aura.providers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.WorldGenConfig;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import me.melchscat.aura.loader.generator.ModChunkGeneratorJsonLoader;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomWorldGenProvider implements IWorldGenProvider {

    public static final BuilderCodec<CustomWorldGenProvider> CODEC = BuilderCodec.builder(CustomWorldGenProvider.class, CustomWorldGenProvider::new)
        .documentation("The standard generator for Hytale.")
        .append(new KeyedCodec<>("Name", Codec.STRING), (config, s) -> config.name = s, config -> config.name)
        .documentation("The name of the generator to use. \"*Default*\" if not provided.")
        .add()
        .<Semver>append(new KeyedCodec<>("Version", Semver.CODEC), (config, v) -> config.version = v, config -> config.version)
        .documentation("The version of the generator to use. \"0.0.0\" if not provided.")
        .add()
        .append(new KeyedCodec<>("Path", Codec.STRING), (config, s) -> config.path = s, config -> config.path)
        .documentation("The path to the world generation configuration. \n\nDefaults to the server provided world generation folder if not set.")
        .add()
        .build();

    private String name = "Default";
    private Semver version = HytaleWorldGenProvider.MIN_VERSION;
    private String path;

    @Nonnull
    @Override
    public IWorldGen getGenerator() throws WorldGenLoadException {

        Path worldGenPath;
        worldGenPath = Universe.getWorldGenPath();

        if (!"Default".equals(this.name) || !Files.exists(worldGenPath.resolve("World.json"))) {
            worldGenPath = worldGenPath.resolve(this.name);
        }

        try {
            WorldGenConfig config = new WorldGenConfig(worldGenPath, this.name, this.version);
            return new ModChunkGeneratorJsonLoader(new SeedString<>("ChunkGenerator", new SeedStringResource(PrefabStoreRoot.DEFAULT, config)), config)
                .load();
        } catch (Error var3) {
            throw new WorldGenLoadException("Failed to load world gen!", var3);
        }
    }

    @Nonnull
    @Override
    public String toString() {
        return "HytaleWorldGenProvider{name='" + this.name + "', version=" + this.version + ", path='" + this.path + "'}";
    }
}
