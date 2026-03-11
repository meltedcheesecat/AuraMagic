package me.melchscat.aura.worldgen;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.zone.ZoneJsonLoader;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class CustomZoneLoader extends ZoneJsonLoader {

    public CustomZoneLoader(@NonNullDecl SeedString<SeedStringResource> seed, @NonNullDecl Path dataFolder, @NonNullDecl JsonElement json, @NonNullDecl ZoneFileContext zoneContext) {
        super(seed, dataFolder, json, zoneContext);
    }
}
