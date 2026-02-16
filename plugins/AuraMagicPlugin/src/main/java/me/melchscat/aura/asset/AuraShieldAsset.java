package me.melchscat.aura.asset;

import com.hypixel.hytale.assetstore.JsonAsset;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class AuraShieldAsset implements
        JsonAsset<String>,
        JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, AuraShieldAsset>>
{
    // The Codec only handles the inner data fields
    public static final BuilderCodec<AuraShieldAsset> CODEC =
            BuilderCodec.builder(AuraShieldAsset.class, AuraShieldAsset::new)
                    .append(new KeyedCodec<>("CurrentHealth", Codec.FLOAT),
                            (obj, val) -> obj.currentHealth = val,
                            obj -> obj.currentHealth)
                    .add()
                    .build();

    private String id;
    private float currentHealth;

    @Override
    public String getId() { return id; }

    // Required so AssetBuilderCodec can inject the filename as the ID
    public void setId(String id) { this.id = id; }

    public float getCurrentHealth() { return currentHealth; }
}
