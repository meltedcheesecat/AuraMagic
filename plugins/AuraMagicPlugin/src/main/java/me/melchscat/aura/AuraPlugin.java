package me.melchscat.aura;

import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.interaction.ChargeAuraShield;
import me.melchscat.aura.providers.CustomWorldGenProvider;
import me.melchscat.aura.system.AuraShieldSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class AuraPlugin extends JavaPlugin {
    private static AuraPlugin instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;
    private static final String CaveModifactionsPathString = "Server/World/CustomOres/CaveModifications";
    private static final String ZoneDataPathString = "Server/World/CustomOres/Ores";
    public static final Path CaveModificationsPath = Path.of(CaveModifactionsPathString);
    public static final Path ZoneDataPath = Path.of(ZoneDataPathString);

    public static AuraPlugin getInstance() {
        return instance;
    }

    public AuraPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", CustomWorldGenProvider.class, CustomWorldGenProvider.CODEC);

        auraShieldComponentType =
                EntityStore.REGISTRY.registerComponent(AuraShieldComponent.class, AuraShieldComponent::new);

        Interaction.CODEC.register("ChargeAuraShield", ChargeAuraShield.class, ChargeAuraShield.CODEC);

        getEntityStoreRegistry().registerSystem(new AuraShieldSystem());
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem.OnDamageReceived());
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComponentType;
    }
}
