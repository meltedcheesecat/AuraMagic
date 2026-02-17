package me.melchscat.aura;

import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.asset.AuraShieldAsset;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.interaction.AuraShieldInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AuraPlugin extends JavaPlugin {
    private static AuraPlugin instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;

    public static AuraPlugin getInstance() {
        return instance;
    }

    public AuraPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.auraShieldComponentType = EntityStore.REGISTRY.registerComponent(AuraShieldComponent.class, AuraShieldComponent::new);

        /*// 1. Create the specialized AssetCodec using the 'wrap' method
        AssetCodec<String, AuraShieldAsset> assetCodec = AssetBuilderCodec.wrap(
                AuraShieldAsset.CODEC,        // Your existing BuilderCodec
                Codec.STRING,                 // The Key type (String)
                AuraShieldAsset::setId,       // Setter for the ID
                AuraShieldAsset::getId,       // Getter for the ID
                (asset, data) -> {},          // Data setter (can be empty if not using extra info)
                (asset) -> null               // Data getter (can return null if not using extra info)
        );

        // 2. Register with the registry
        getAssetRegistry().register(
                HytaleAssetStore.builder(String.class, AuraShieldAsset.class, new IndexedLookupTableAssetMap<>(AuraShieldAsset[]::new))
                        .setPath("MyAssets")
                        .setCodec(assetCodec)
                        .setKeyFunction(AuraShieldAsset::getId)
                        .setReplaceOnRemove(key -> null)
                        .build()
        );*/

        Interaction.CODEC.register(
                "AuraMagicShield",  // This MUST match the "Type" in your JSON
                AuraShieldInteraction.class,
                AuraShieldInteraction.CODEC
        );
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComponentType;
    }
}
