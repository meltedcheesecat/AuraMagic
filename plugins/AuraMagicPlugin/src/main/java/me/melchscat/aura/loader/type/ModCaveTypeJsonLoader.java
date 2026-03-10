package me.melchscat.aura.loader.type;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.cave.CaveTypeJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import me.melchscat.aura.loader.CustomFileLoader;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class ModCaveTypeJsonLoader extends CaveTypeJsonLoader {

    public ModCaveTypeJsonLoader(@NonNullDecl SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, String name, ZoneFileContext zoneContext) {
        super(seed, dataFolder, json, caveFolder, name, zoneContext);
    }

    @Override
    protected JsonElement loadFile(@Nonnull String filePath) {
        return CustomFileLoader.loadFile(filePath, this.dataFolder);
    }

}
