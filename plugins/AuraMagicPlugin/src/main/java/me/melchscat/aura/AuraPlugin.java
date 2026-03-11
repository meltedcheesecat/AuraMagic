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
import me.melchscat.aura.worldgen.CustomWorldGenProvider;
import me.melchscat.aura.system.AuraShieldSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.nio.file.Path;

public class AuraPlugin extends JavaPlugin {
    private static AuraPlugin instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;


    public String WindCrystalBlock = "Rock_Crystal_Wind_Block";
    public String WindCrystalLarge = "Rock_Crystal_Wind_Large";
    public String WindCrystalMedium = "Rock_Crystal_Wind_Medium";
    public String WindCrystalSmall = "Rock_Crystal_Wind_Small";

    /*
    public String WindCrystalBlock = "Rock_Crystal_Pink_Block";
    public String WindCrystalLarge = "Rock_Crystal_Pink_Large";
    public String WindCrystalMedium = "Rock_Crystal_Pink_Medium";
    public String WindCrystalSmall = "Rock_Crystal_Pink_Small";
    */

    public static AuraPlugin getInstance() {
        return instance;
    }

    public AuraPlugin(@NonNullDecl JavaPluginInit init) {
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
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComponentType;
    }
}
