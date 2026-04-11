package me.melchscat.aura;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import me.melchscat.aura.component.AuraBlockLifetimeComponent;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.interaction.ChargeAuraShield;
import me.melchscat.aura.interaction.CreateAuraWindBlocks;
import me.melchscat.aura.interaction.ShowStartAuraPage;
import me.melchscat.aura.prefab.AuraPrefabs;
import me.melchscat.aura.system.AuraBlockLifetimeSystem;
import me.melchscat.aura.worldgen.CustomWorldGenProvider;
import me.melchscat.aura.system.AuraShieldSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AuraMagic extends JavaPlugin {
    private static AuraMagic instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComType;
    private ComponentType<ChunkStore, AuraBlockLifetimeComponent> auraBlockLifetimeComType;
    private AuraPrefabs auraPrefabs;

    public AuraMagic(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        // replaces the hytale world generator with a custom one
        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", CustomWorldGenProvider.class, CustomWorldGenProvider.CODEC);

        // Magic Block lifetime, currently used for the block spell
        auraBlockLifetimeComType = ChunkStore.REGISTRY.registerComponent(AuraBlockLifetimeComponent.class, "AuraBlockLifetime",  AuraBlockLifetimeComponent.CODEC);
        Interaction.CODEC.register("CreateAuraWindBlocks", CreateAuraWindBlocks.class, CreateAuraWindBlocks.CODEC);

        // Magic Shield, for now this is wind only
        auraShieldComType = EntityStore.REGISTRY.registerComponent(AuraShieldComponent.class, AuraShieldComponent::new);
        Interaction.CODEC.register("ChargeAuraShield", ChargeAuraShield.class, ChargeAuraShield.CODEC);

        // Aura Start Page shown by mannequin
        Interaction.CODEC.register("ShowStartAuraPage", ShowStartAuraPage.class, ShowStartAuraPage.CODEC);
    }

    @Override
    protected void start() {
        // Magic Block lifetime system
        getChunkStoreRegistry().registerSystem(new AuraBlockLifetimeSystem(auraBlockLifetimeComType));

        // Magic Shield system, for now this is wind only
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem(auraShieldComType));
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem.OnDamageReceived(auraShieldComType));
    }

    public static AuraMagic getInstance() {
        return instance;
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComType;
    }

    public ComponentType<ChunkStore, AuraBlockLifetimeComponent> getAuraBlockLifetimeComponentType() {
        return this.auraBlockLifetimeComType;
    }

    public AuraPrefabs getAuraPrefabs() {
        if (this.auraPrefabs == null)
          auraPrefabs = new AuraPrefabs();

        return this.auraPrefabs;
    }
}
