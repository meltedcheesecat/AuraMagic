package me.melchscat.aura.loader.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.procedurallib.file.AssetPath;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import me.melchscat.aura.AuraPlugin;
import me.melchscat.aura.loader.type.ModCaveTypesJsonLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModCaveGeneratorJsonLoader extends JsonLoader<SeedStringResource, CaveGenerator> {

    protected final Path caveFolder;

    protected final ZoneFileContext zoneContext;

    public ModCaveGeneratorJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, ZoneFileContext zoneContext) {

        super(seed.append(".CaveGenerator"), dataFolder, json);
        this.caveFolder = caveFolder;
        this.zoneContext = zoneContext;
    }

    private static String getZoneName(String zoneName) {

        int start = zoneName.indexOf("Zones/") + "Zones/".length();
        int end = zoneName.lastIndexOf("/");
        return zoneName.substring(start, end);
    }

    // CAVEMODIFICATION.JSON
    @Nullable
    public CaveGenerator load() {

        CaveGenerator caveGenerator = null;
        if (this.caveFolder == null) return null;

        AssetPath assetPath = FileIO.resolve(this.caveFolder.resolve("Caves.json"));
        if (!FileIO.exists(assetPath)) {
            return null;
        }

        try {
            JsonObject cavesJson = FileIO.load(assetPath, JsonLoader.JSON_OBJ_LOADER);


            String zoneName = getZoneName(this.caveFolder.toString());

            var modPath = AuraPlugin.getInstance().CaveModificationsPath;

            AssetPack[] assetPacks = AssetModule.get().getAssetPacks().stream()
                .filter(assetPack -> !assetPack.getName().equals("Hytale:Hytale") && !assetPack.getName().equals(AuraPlugin.getInstance().getName())).toArray(AssetPack[]::new);


            JsonObject modifiedCaves;

            // Process each asset pack
            for (AssetPack pack : assetPacks) {
                if (pack.isImmutable() && pack.getPackLocation().getFileName().toString().toLowerCase().endsWith(".zip")) {
                    try (FileSystem fs = FileSystems.newFileSystem(pack.getPackLocation(), (ClassLoader) null)) {
                        Path manifestPath = fs.getPath(modPath.resolve("CaveModifications.json").toString());
                        if (Files.exists(manifestPath)) {
                            try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
                                char[] buffer = RawJsonReader.READ_BUFFER.get();
                                StringBuilder contentBuilder = new StringBuilder();
                                int numCharsRead;
                                while ((numCharsRead = reader.read(buffer)) != -1) {
                                    contentBuilder.append(buffer, 0, numCharsRead);
                                }

                                try (JsonReader jsonReader = new JsonReader(new StringReader(contentBuilder.toString()))) {
                                    modifiedCaves = JsonParser.parseReader(jsonReader).getAsJsonObject();
                                }

                                if (modifiedCaves.get(zoneName) != null) {
                                    JsonArray oreModificationFileList = modifiedCaves.get(zoneName).getAsJsonArray();

                                    for (JsonElement element : oreModificationFileList) {

                                        cavesJson.get("Types").getAsJsonArray().add(element);
                                    }
                                }

                            }
                        }
                    }
                } else {
                    // Normal Way (NON ZIP)
                    var path = pack.getPackLocation()
                        .resolve(modPath.resolve("CaveModifications.json").toString());
                    if (path.toFile().exists()) {
                        try (JsonReader reader = new JsonReader(Files.newBufferedReader(path))) {
                            modifiedCaves = JsonParser.parseReader(reader).getAsJsonObject();

                            if (modifiedCaves.get(zoneName) != null) {
                                JsonArray oreModificationFileList = modifiedCaves.get(zoneName).getAsJsonArray();

                                for (JsonElement element : oreModificationFileList) {
                                    cavesJson.get("Types").getAsJsonArray().add(element);
                                }
                            }
                        }
                    }
                }

            }

                caveGenerator = new CaveGenerator(this.loadCaveTypes(cavesJson));
            } catch (Throwable var9) {
                throw new Error(String.format("Error while loading caves for world generator from %s", assetPath.toString()), var9);
            }


        return caveGenerator;
    }

    @Nonnull
    protected CaveType[] loadCaveTypes(@Nonnull JsonObject jsonObject) {

        return new ModCaveTypesJsonLoader(this.seed, this.dataFolder, jsonObject.get("Types"), this.caveFolder, this.zoneContext).load();
    }
}
