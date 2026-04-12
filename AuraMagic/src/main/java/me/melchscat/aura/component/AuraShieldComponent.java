package me.melchscat.aura.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AuraShieldComponent implements Component<EntityStore> {
    // We use a Codec so we can save/load or sync this data
    public static final BuilderCodec<AuraShieldComponent> CODEC =
            BuilderCodec.builder(AuraShieldComponent.class, AuraShieldComponent::new)
                    .append(new KeyedCodec<>("Health", Codec.FLOAT),
                            (comp,
                             value) -> comp.health = value,
                            comp -> comp.health)
                    .add()
                    .build();

    public float oldHealth = 0.0f;
    public float health = 0.0f;
    public com.hypixel.hytale.protocol.ModelParticle[] shieldParticles = new com.hypixel.hytale.protocol.ModelParticle[1];
    public String modelId = "";
    public Boolean invalidModelId = true;

    public final float HEALTH_FADE_TIMER_DELAY = 3.0F;
    public float healthFadeTimerValue = 0.0f;
    public float shieldParticleTimerValue = 0.0f;
    public Boolean checkChargeAuraShield = false;
    public float chargedHealth = 0.0f;
    public String chargedModelId = "";
    public Boolean checkOnDamageHealthChange = false;

    public AuraShieldComponent() {}

    @Override
    public Component<EntityStore> clone() {
        AuraShieldComponent copy = new AuraShieldComponent();
        copy.health = this.health;
        return copy;
    }
}
