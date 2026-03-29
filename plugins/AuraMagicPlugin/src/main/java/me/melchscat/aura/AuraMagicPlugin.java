package me.melchscat.aura;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.interaction.ChargeAuraShield;
import me.melchscat.aura.interaction.ShowStartAuraPage;
import me.melchscat.aura.worldgen.CustomWorldGenProvider;
import me.melchscat.aura.system.AuraShieldSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AuraMagicPlugin extends JavaPlugin {
    private static AuraMagicPlugin instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;


    //This is temporary code, Later on I will change to read from json files
    public String WindGemBlock = "Rock_Gem_Aquamarine";
    public String WindCrystalBlock = "Spawner_Wind_Sprite";
    public String WindCrystalLarge = "Rock_Crystal_Wind_Large";
    public String WindCrystalMedium = "Rock_Crystal_Wind_Medium";
    public String WindCrystalSmall = "Rock_Crystal_Wind_Small";
    public String AuraStartBlock = "Mannequin_Aura_Magic";

    public String SpawnZoneName = "Zone1_Spawn";

    public static AuraMagicPlugin getInstance() {
        return instance;
    }

    public AuraMagicPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        // replaces the hytale world generator with our custom one
        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", CustomWorldGenProvider.class, CustomWorldGenProvider.CODEC);

        // Our magic Shield, for now this is wind only
        auraShieldComponentType =
                EntityStore.REGISTRY.registerComponent(AuraShieldComponent.class, AuraShieldComponent::new);

        Interaction.CODEC.register("ChargeAuraShield", ChargeAuraShield.class, ChargeAuraShield.CODEC);

        getEntityStoreRegistry().registerSystem(new AuraShieldSystem());
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem.OnDamageReceived());

        Interaction.CODEC.register("ShowStartAuraPage", ShowStartAuraPage.class, ShowStartAuraPage.CODEC);
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComponentType;
    }
}
