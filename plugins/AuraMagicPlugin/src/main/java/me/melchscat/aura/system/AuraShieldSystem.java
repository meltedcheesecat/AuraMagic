package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraPlugin;
import me.melchscat.aura.component.AuraShieldComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Set;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraShieldSystem extends EntityTickingSystem<EntityStore> {
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;
    private float healthFadeTimer = 0f;

    @Override
    public Query<EntityStore> getQuery() {
        if (auraShieldComponentType == null)
          auraShieldComponentType = AuraPlugin.getInstance().getAuraShieldComponentType();

        return auraShieldComponentType;
    }

    @Override
    public void tick(float dt,
                     int index,
                     @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store,
                     @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (auraShieldComponentType == null)
            auraShieldComponentType = AuraPlugin.getInstance().getAuraShieldComponentType();

        AuraShieldComponent auraShield = archetypeChunk.getComponent(index, auraShieldComponentType);

        if (auraShield == null)
            return;

        if (auraShield.addedHealth > 0.0F){
            // if our health is zero and we are adding health reset the fade timer
            if (auraShield.health <= 0.0F){
                healthFadeTimer = 0.0F;
            }

            auraShield.health += (auraShield.addedHealth - (auraShield.health / 10));
            auraShield.addedHealth = 0.0F;
        }

        if (auraShield.health > 0.0F){
            // add the delta time to the fade timer
            healthFadeTimer += dt;

            // decrease the health by 1 each second
            if (healthFadeTimer >= 1.0F){
                auraShield.health -= 1.0F;
                healthFadeTimer = 0.0F;
            }
        }

        //getLogger().at(Level.INFO).log("Log String");
    }

    @Override
    public void onSystemRegistered() {
        super.onSystemRegistered();
    }

    @Override
    public void onSystemUnregistered() {
        super.onSystemUnregistered();
    }

    @NullableDecl
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return super.getGroup();
    }

    @NonNullDecl
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return super.getDependencies();
    }
}