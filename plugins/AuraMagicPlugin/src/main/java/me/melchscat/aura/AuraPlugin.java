package me.melchscat.aura;

import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.interaction.ChargeAuraShield;
import me.melchscat.aura.system.AuraShieldSystem;
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
