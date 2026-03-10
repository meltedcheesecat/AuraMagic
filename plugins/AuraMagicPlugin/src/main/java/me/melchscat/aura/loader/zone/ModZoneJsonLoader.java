package me.melchscat.aura.loader.zone;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneJsonLoader;
import me.melchscat.aura.loader.generator.ModCaveGeneratorJsonLoader;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.nio.file.Path;

public class ModZoneJsonLoader extends ZoneJsonLoader {


    public ModZoneJsonLoader(@NonNullDecl SeedString<SeedStringResource> seed, @NonNullDecl Path dataFolder, @NonNullDecl JsonElement json, @NonNullDecl ZoneFileContext zoneContext) {
        super(seed, dataFolder, json, zoneContext);
    }

    /*@Override
    @Nullable
    protected CaveGenerator loadCaveGenerator() {

        try {
            return new ModCaveGeneratorJsonLoader(this.seed, this.dataFolder, this.json, this.zoneContext.getPath().resolve("Cave"), this.zoneContext).load();
        } catch (Throwable var2) {
            throw new Error("Error while loading cave generator.", var2);
        }
    }*/
}
