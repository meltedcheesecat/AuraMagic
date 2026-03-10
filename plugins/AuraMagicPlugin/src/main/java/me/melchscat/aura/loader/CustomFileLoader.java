package me.melchscat.aura.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.server.core.asset.AssetModule;
import me.melchscat.aura.AuraPlugin;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustomFileLoader{


    public static JsonElement loadFile(@Nonnull String filePath, Path dataFolder) {


        Path file = dataFolder.resolve(filePath.replace('.', File.separatorChar) + ".json");

        try {
            if (file.normalize().startsWith(dataFolder.normalize()) && Files.exists(file)) {
                // VANILLA JUST READ
                return FileIO.load(file, JsonLoader.JSON_LOADER);
            }
            // MODIFIED (ie. Change Location!)
            AssetPack[] assetPacks = AssetModule.get().getAssetPacks().stream()
                .filter(assetPack -> !assetPack.getName().equals("Hytale:Hytale") && !assetPack.getName().equals(AuraPlugin.getInstance().getName())).toArray(AssetPack[]::new);
            var dataPath = AuraPlugin.getInstance().ZoneDataPath.getParent();


            // Path to every AssetPack (Path pathToCaveMods : pathsToCaveMods)
            for (AssetPack pack : assetPacks) {
                if (pack.isImmutable() && pack.getPackLocation().toFile().isFile()) {
                    // We are in a ZIP FILE
                    try (FileSystem fs = FileSystems.newFileSystem(pack.getPackLocation(), (ClassLoader) null)) {
                        Path manifestPath = fs.getPath(dataPath.resolve(filePath.replace('.', File.separatorChar) + ".json").toString());
                        if (Files.exists(manifestPath)) {
                            try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
                                char[] buffer = RawJsonReader.READ_BUFFER.get();
                                StringBuilder contentBuilder = new StringBuilder();
                                int numCharsRead;
                                while ((numCharsRead = reader.read(buffer)) != -1) {
                                    contentBuilder.append(buffer, 0, numCharsRead);
                                }

                                try (JsonReader jsonReader = new JsonReader(new StringReader(contentBuilder.toString()))) {
                                    return JsonParser.parseReader(jsonReader);
                                }
                            }
                        }
                    }
                } else {
                    var path = pack.getPackLocation().resolve(dataPath.resolve(filePath.replace('.', File.separatorChar) + ".json"));
                    if (Files.exists(path)) {
                        try (JsonReader reader = new JsonReader(Files.newBufferedReader(path))) {
                            return JsonParser.parseReader(reader);
                        }
                    }
                }
            }

            return null;
        } catch (Throwable var8) {
            throw new Error("Error while loading file reference " + file, var8); // ERROR THAT'S BEING THROWN
        }
    }
}
