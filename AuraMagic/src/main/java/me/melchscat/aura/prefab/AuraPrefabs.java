package me.melchscat.aura.prefab;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraPrefabs {
    private final String ourPackName = "me.melchscat.aura:AuraMagic";
    private final int prefabTypeCount = 9; // not the amount of prefabs since we spawn more than 2 sometimes
    public Path path;
    public AssetPack assetPack;
    public Map<Path, BlockSelection> windPrefabs;
    public boolean loaded = false;

    public void init() {
        PrefabStore store = PrefabStore.get();
        if (store == null) {
            getLogger().at(Level.INFO).log("Aura AuraPrefabs store is null");
            return;
        } else {
            List<PrefabStore.AssetPackPrefabPath> assetPrefabs = store.getAllAssetPrefabPaths();
            for (PrefabStore.AssetPackPrefabPath assetPrefab : assetPrefabs) {
                if (assetPrefab.getPackName().compareTo(ourPackName) == 0) {
                    path = assetPrefab.prefabsPath();
                    assetPack = assetPrefab.pack();
                    // wind
                    Path windPath = path.resolve("Aura").resolve("Wind");
                    windPrefabs = store.getPrefabDir(windPath);
                    loaded = true;
                    return;
                }
            }
        }
        getLogger().at(Level.INFO).log("Aura AuraPrefabs pack not found");
    }

    private Path getWindPrefabPath(String fileName) {
        for (Map.Entry<Path, BlockSelection> entry : windPrefabs.entrySet()) {
            Path windPrefabPath = entry.getKey();
            if (fileName.compareTo(windPrefabPath.getFileName().toString()) == 0) {
                return windPrefabPath;
            }
        }
        getLogger().at(Level.INFO).log("Aura getWindPrefab file:" + fileName +" not found");
        return null;
    }

    private void placeWindPrefab(@Nonnull World world, @Nonnull Vector3i position, Path windPrefabPath) {
        Store<EntityStore> store = world.getEntityStore().getStore();

        if (store == null) return;

        world.execute(() -> {
            PrefabBuffer prefabBuffer = PrefabBufferUtil.loadBuffer(windPrefabPath);
            PrefabBuffer.PrefabBufferAccessor prefabBufferAccessor = prefabBuffer.newAccess();
            PrefabUtil.paste(prefabBufferAccessor, world, position, Rotation.None, true, new Random(), store);
            prefabBufferAccessor.release();
        });
    }

    private void placeWindDoublePrefab(@Nonnull World world, @Nonnull Vector3i position, int yPosAddFor2, Path windPrefabPath1, Path windPrefabPath2) {
        Store<EntityStore> store = world.getEntityStore().getStore();

        if (store == null) return;

        world.execute(() -> {
            PrefabBuffer prefabBuffer1 = PrefabBufferUtil.loadBuffer(windPrefabPath1);
            PrefabBuffer.PrefabBufferAccessor prefabBufferAccessor1 = prefabBuffer1.newAccess();
            PrefabUtil.paste(prefabBufferAccessor1, world, position, Rotation.None, true, new Random(), store);
            prefabBufferAccessor1.release();

            if (position.getY() < 270) {
                int newY = position.getY() + yPosAddFor2;
                if (newY > 290) newY = 290;

                Vector3i pos2 = new Vector3i(position.getX(), newY, position.getZ());

                PrefabBuffer prefabBuffer2 = PrefabBufferUtil.loadBuffer(windPrefabPath2);
                PrefabBuffer.PrefabBufferAccessor prefabBufferAccessor2 = prefabBuffer2.newAccess();
                PrefabUtil.paste(prefabBufferAccessor2, world, pos2, Rotation.None, true, new Random(), store);
                prefabBufferAccessor2.release();
            }
        });
    }

    public void spawnWindPrefab(@Nonnull World world, @Nonnull Vector3i position) {
        if (!loaded) init();

        Path windPrefabPath;
        switch (ThreadLocalRandom.current().nextInt(prefabTypeCount)) {
            case 0 :
            case 1 : {
                // basically windAngel points to temple above
                Path windPrefabPath1 = getWindPrefabPath("WindAngel.prefab.json");
                Path windPrefabPath2 = getWindPrefabPath("WindAngelTemple.prefab.json");

                placeWindDoublePrefab(world, position, 80, windPrefabPath1, windPrefabPath2);
                break;
            }
            case 2 : {
                windPrefabPath = getWindPrefabPath("WindFourAngels.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 3 : {
                windPrefabPath = getWindPrefabPath("WindFlower.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 4 : {
                windPrefabPath = getWindPrefabPath("WindLargeTemple.prefab.json");
                position.setY(position.getY() + 20);
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 5 : {
                windPrefabPath = getWindPrefabPath("WindFlatTemple.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 6 : {
                windPrefabPath = getWindPrefabPath("WindMiniTemple1.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 7 : {
                windPrefabPath = getWindPrefabPath("WindMiniTemple2.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
            case 8 : {
                windPrefabPath = getWindPrefabPath("WindMiniTemple3.prefab.json");
                placeWindPrefab(world, position, windPrefabPath);
                break;
            }
        }
    }
}
