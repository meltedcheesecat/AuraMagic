package me.melchscat.aura.loader.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import me.melchscat.aura.loader.CustomFileLoader;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;

public class ModCaveTypesJsonLoader extends JsonLoader<SeedStringResource, CaveType[]> {

    protected final Path caveFolder;

    protected final ZoneFileContext zoneContext;

    public ModCaveTypesJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, Path caveFolder, ZoneFileContext zoneContext) {

        super(seed, dataFolder, json);
        this.caveFolder = caveFolder;
        this.zoneContext = zoneContext;
    }

    @Nonnull
    public CaveType[] load() {

        if (this.json != null && this.json.isJsonArray()) {
            JsonArray typesArray = this.json.getAsJsonArray();
            ArrayList<CaveType> caveTypes = new ArrayList<>();

            for (int i = 0; i < typesArray.size(); i++) {
                JsonElement entry = this.getOrLoad(typesArray.get(i));
                if (entry == null || !entry.isJsonObject()) {
                    continue;
                }

                JsonObject caveTypeJson = entry.getAsJsonObject();
                try {
                    String name = this.loadName(caveTypeJson);
                    var type = this.loadCaveType(name, caveTypeJson);
                    if (type != null) caveTypes.add(type); // ISSUE
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            return caveTypes.toArray(CaveType[]::new);
        } else {
            throw new IllegalArgumentException("CaveTypes must be a JSON array.");
        }
    }

    @Override
    protected JsonElement loadFile(@NonNullDecl String filePath){
        return CustomFileLoader.loadFile(filePath, this.dataFolder);
    }

    protected CaveType loadCaveType(String name, JsonElement json) {

        try {
            return new ModCaveTypeJsonLoader(this.seed.append(String.format("-%s", name)), this.dataFolder, json, this.caveFolder, name, this.zoneContext).load();
        }catch (Throwable e){
            System.err.println(e.getMessage());
        }
        return null;
    }

    protected String loadName(@Nonnull JsonObject jsonObject) {

        return jsonObject.get("Name").getAsString();
    }
}
