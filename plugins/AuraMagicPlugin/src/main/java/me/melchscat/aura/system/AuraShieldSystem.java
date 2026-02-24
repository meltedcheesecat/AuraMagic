package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.ModelParticle;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.protocol.packets.world.SpawnParticleSystem;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.melchscat.aura.AuraPlugin;
import me.melchscat.aura.component.AuraShieldComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraShieldSystem extends EntityTickingSystem<EntityStore> {
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;
    private float healthFadeTimer = 0f;
    private com.hypixel.hytale.protocol.ModelParticle[] shieldParticles = new com.hypixel.hytale.protocol.ModelParticle[1];

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

        if (auraShield.ourPlayer == null)
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

                // checks if there is a new type of model, if yes saves the particles to shieldParticles
                if (auraShield.hasNewModelId == true) {
                    ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(auraShield.modelId);
                    if (modelAsset != null) {
                        com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle[] modelAssetParticles = modelAsset.getParticles();

                        if ((modelAssetParticles != null) && (modelAssetParticles.length > 0)){
                            shieldParticles[0] = modelAssetParticles[0].toPacket();
                            auraShield.invalidModelId = false;
                        } else {
                            auraShield.invalidModelId = true;
                        }
                    } else {
                        shieldParticles = null;
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

                NetworkId networkIdComponent = (NetworkId)archetypeChunk.getComponent(ourPlayerReference.getIndex(), NetworkId.getComponentType());

                if (networkIdComponent == null)
                    return;

                int playerNetworkId = networkIdComponent.getId();

                PlayerRef playerRefComponent = (PlayerRef)commandBuffer.getComponent(ourPlayerReference, PlayerRef.getComponentType());

                if (playerRefComponent == null)
                    return;

                // Creates the packet with the shieldParticles in it and then sends it
                SpawnModelParticles packet = new SpawnModelParticles(playerNetworkId, shieldParticles);

                playerRefComponent.getPacketHandler().writeNoCache(packet);

                // send a world particle code - saving this for later
                //TransformComponent transformComponent = (TransformComponent)store.getComponent(ourPlayerReference, TransformComponent.getComponentType());
                //if (transformComponent == null)
                //    return;

                //World ourPlayerWorld = auraShield.ourPlayer.getWorld();
                //if (ourPlayerWorld == null)
                //    return;

                //Vector3d playerPosition = auraShield.ourPlayer.getPlayerConfigData().getPerWorldData(ourPlayerWorld.getName()).getLastPosition().getPosition();
                //ParticleUtil.spawnParticleEffect(auraShield.modelId, transformComponent.getPosition(), commandBuffer);
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