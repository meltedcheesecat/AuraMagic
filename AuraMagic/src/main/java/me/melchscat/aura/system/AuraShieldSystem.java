package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagic;
import me.melchscat.aura.component.AuraShieldComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.Set;

public class AuraShieldSystem extends EntityTickingSystem<EntityStore> {
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;

    @Override
    public Query<EntityStore> getQuery() {
        if (auraShieldComponentType == null)
          auraShieldComponentType = AuraMagic.getInstance().getAuraShieldComponentType();

        return auraShieldComponentType;
    }

    @Override
    public void tick(float dt,
                     int index,
                     @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                     @NonNullDecl Store<EntityStore> store,
                     @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        if (auraShieldComponentType == null)
            auraShieldComponentType = AuraMagic.getInstance().getAuraShieldComponentType();

        AuraShieldComponent auraShield = archetypeChunk.getComponent(index, auraShieldComponentType);

        if (auraShield == null)
            return;

        if (auraShield.ourPlayer == null)
            return;

        Float oldHealth = auraShield.health;
        Boolean firstShow = false;

        if (auraShield.addedHealth > 0.0F){
            // if our health is zero and we are adding health reset the fade timer
            if (auraShield.health <= 0.0F){
                auraShield.healthFadeTimer = 0.0F;
                firstShow = true;
            }

            auraShield.health += (auraShield.addedHealth - (auraShield.health / 10));
            auraShield.addedHealth = 0.0F;
        }

        if (auraShield.health > 0.0F){
            // add the delta time to the fade timer
            auraShield.healthFadeTimer += dt;

            // decrease the health by 1 each second
            if ((auraShield.healthFadeTimer >= 1.0F) || (firstShow)){

                if (!firstShow) {
                    auraShield.health -= 1.0F;
                    auraShield.healthFadeTimer = 0.0F;
                }

                // checks if there is a new type of model, if yes saves the particles to shieldParticles
                if (auraShield.hasNewModelId == true) {
                    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(auraShield.modelId);
                    if (modelAsset != null) {
                        com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle[] modelAssetParticles = modelAsset.getParticles();

                        if ((modelAssetParticles != null) && (modelAssetParticles.length > 0)){
                            auraShield.shieldParticles[0] = modelAssetParticles[0].toPacket();
                            auraShield.invalidModelId = false;
                        } else {
                            auraShield.invalidModelId = true;
                        }
                    } else {
                        auraShield.shieldParticles = null;
                        auraShield.invalidModelId = true;
                    }
                    auraShield.hasNewModelId = false;
                }

                if (auraShield.invalidModelId == true)
                    return;

                // using the player to get the NetworkID and Packet Handler so we can send the SpawnModelParticles message
                Ref<EntityStore> ourPlayerReference = auraShield.ourPlayer.getReference();
                if (ourPlayerReference == null)
                    return;


                Ref<EntityStore> playerRef = auraShield.ourPlayer.getReference();
                NetworkId networkIdComponent = store.getComponent(playerRef, NetworkId.getComponentType());

                if (networkIdComponent == null)
                    return;

                int playerNetworkId = networkIdComponent.getId();

                PlayerRef playerRefComponent = (PlayerRef)commandBuffer.getComponent(ourPlayerReference, PlayerRef.getComponentType());

                if (playerRefComponent == null)
                    return;

                //auraShield.shieldParticles[0].scale = auraShield.health / 10;

                // Creates the packet with the shieldParticles in it and then sends it
                SpawnModelParticles packet = new SpawnModelParticles(playerNetworkId, auraShield.shieldParticles);

                playerRefComponent.getPacketHandler().writeNoCache(packet);
            }
        }

        if (oldHealth != auraShield.health) {
            DynamicLight shieldDynamicLight = archetypeChunk.getComponent(index, DynamicLight.getComponentType());

            if (shieldDynamicLight == null)
                return;

            ColorLight colorLight = new ColorLight();

            if (auraShield.health >= 15.0f)
            {
                colorLight.radius = (byte) 1;
                colorLight.red = (byte) 8;
                colorLight.green = (byte) 15;
                colorLight.blue = (byte) 13;
            } else if (auraShield.health <= 0.0f ) {
                colorLight.radius = (byte) 0;
                colorLight.red = (byte) 0;
                colorLight.green = (byte) 0;
                colorLight.blue = (byte) 0;
            } else {
                byte byteHealth = (byte) auraShield.health;
                if (byteHealth > 7) {
                    colorLight.red = (byte) (byteHealth - 7);
                } else {
                    colorLight.red = (byte) 0;
                }
                colorLight.green = byteHealth;
                if (byteHealth > 2) {
                    colorLight.blue = (byte) (byteHealth - 2);
                } else {
                    colorLight.blue = (byte) 0;
                }
            }
            shieldDynamicLight.setColorLight(colorLight);
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

    public static class OnDamageReceived extends DamageEventSystem {
        @Nonnull
        private final Query<EntityStore> query = AuraMagic.getInstance().getAuraShieldComponentType();

        @Nonnull
        public Query<EntityStore> getQuery() {
            return this.query;
        }

        public void handle(int index,
                           @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull CommandBuffer<EntityStore> commandBuffer,
                           @Nonnull Damage damage) {
            AuraShieldComponent auraShield = archetypeChunk.getComponent(index, AuraMagic.getInstance().getAuraShieldComponentType());

            if (auraShield == null)
                return;

            if (auraShield.health <= 0.0f)
                return;

            float damageAmount = damage.getAmount();

            if (damageAmount <= auraShield.health){
                damage.setCancelled(true);

                auraShield.health -= damageAmount;
            } else {
                damageAmount -= auraShield.health;

                auraShield.health = 0.0f;
                damage.setAmount(damageAmount);
            }

            //getLogger().at(Level.INFO).log(outStr);
        }
    }
}