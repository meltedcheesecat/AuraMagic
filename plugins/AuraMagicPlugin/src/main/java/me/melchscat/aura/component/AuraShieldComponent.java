package me.melchscat.aura.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraPlugin;

import javax.annotation.Nonnull;

public class AuraShieldComponent implements Component<EntityStore> {
    private float currentHealth = 0f;

    public static ComponentType<EntityStore, AuraShieldComponent> getAuraComponentType() {
        return AuraPlugin.getInstance().getAuraShieldComponentType();
    }

    public AuraShieldComponent() {
    }

    private AuraShieldComponent(@Nonnull AuraShieldComponent other) {
        this.currentHealth = other.currentHealth;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = currentHealth;
    }

    @Nonnull
    public Component<EntityStore> clone() {return new AuraShieldComponent(this); }
}
