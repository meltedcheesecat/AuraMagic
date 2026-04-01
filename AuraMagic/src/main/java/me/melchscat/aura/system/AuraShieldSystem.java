package me.melchscat.aura.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagic;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.page.AuraShieldHud;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraShieldSystem extends EntityTickingSystem<EntityStore> {
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;

    @Override
    public Query<EntityStore> getQuery() {
        if (auraShieldComponentType == null)
          auraShieldComponentType = AuraMagic.getInstance().getAuraShieldComponentType();

        return auraShieldComponentType;
    }

    private void doSpawnParticles(AuraShieldComponent auraShield, @NonNullDecl Store<EntityStore> store, Ref<EntityStore> ref, Player player, PlayerRef playerRef) {
        if ((auraShield.chargedModelId != null) && (auraShield.chargedModelId.compareTo(auraShield.modelId) != 0)) {
            auraShield.modelId = auraShield.chargedModelId;
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
        }

        if (auraShield.invalidModelId == true)
            return;

        NetworkId networkIdComponent = store.getComponent(ref, NetworkId.getComponentType());
        if (networkIdComponent == null)
            return;

        int playerNetworkId = networkIdComponent.getId();

        SpawnModelParticles packet = new SpawnModelParticles(playerNetworkId, auraShield.shieldParticles);

        World world = player.getWorld();
        if (world == null) return;

        for (PlayerRef worldPlyrRef : world.getPlayerRefs()) {
            worldPlyrRef.getPacketHandler().writeNoCache(packet);
        }

        //playerRef.getPacketHandler().writeNoCache(packet);
    }

    private void doShieldDynamicLight(AuraShieldComponent auraShield, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk) {
        DynamicLight shieldDynamicLight = archetypeChunk.getComponent(index, DynamicLight.getComponentType());
        if (shieldDynamicLight == null) return;

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

    private void doAuraShieldHud(AuraShieldComponent auraShield, Player player, PlayerRef playerRef) {
        HudManager hudManager = player.getHudManager();

        // Just Went from 0 to some Health
        if (auraShield.oldHealth <= 0.0f) {
            AuraShieldHud auraShieldHud = new AuraShieldHud(playerRef, auraShield.health);
            hudManager.setCustomHud(playerRef, auraShieldHud);
        } else if (auraShield.health <= 0.0f) {
            hudManager.setCustomHud(playerRef, null);
        } else {
            CustomUIHud customHud = hudManager.getCustomHud();
            if (customHud instanceof AuraShieldHud auraShieldHud) {
                auraShieldHud.updateHealth(auraShield.health);
            }
        }
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

        if ((auraShield.health <= 0.0F) &&
            (!auraShield.checkChargeAuraShield) &&
            (!auraShield.checkOnDamageHealthChange))
            return;

        boolean spawnParticles = false;

        // Check if the charge shield interaction ran
        if (auraShield.checkChargeAuraShield) {
            auraShield.health += (auraShield.chargedHealth - (auraShield.health / 10));
            auraShield.chargedHealth = 0.0F;
            auraShield.checkChargeAuraShield = false;

            if (auraShield.oldHealth <= 0.0f) {
                spawnParticles = true;
                auraShield.healthFadeTimerValue = 0.0F;
            }
        }

        // check if the onDamage ran and changed the health, this insures that on zero health code still gets here
        if (auraShield.checkOnDamageHealthChange) auraShield.checkOnDamageHealthChange = false;

        if (auraShield.health > 0.0F) {
            // add the delta time to the fade timer
            auraShield.healthFadeTimerValue += dt;

            if (auraShield.healthFadeTimerValue >= 1.0F) {
                auraShield.health -= 1.0F;
                auraShield.healthFadeTimerValue = 0.0F;

                if (auraShield.health > 0.0f)
                    spawnParticles = true;
            }
        }

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRef == null) return;
        if (!playerRef.isValid()) return;
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null) return;

        if (spawnParticles)
            doSpawnParticles(auraShield, store, ref, player, playerRef);

        if (auraShield.oldHealth != auraShield.health) {
            doShieldDynamicLight(auraShield, index, archetypeChunk);

            doAuraShieldHud(auraShield, player, playerRef);

            auraShield.oldHealth = auraShield.health;
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

        @Override
        public SystemGroup<EntityStore> getGroup() {
            return DamageModule.get().getFilterDamageGroup();
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

            if (damageAmount <= 0.0f)
                return;

            if (damageAmount <= auraShield.health){
                damage.setCancelled(true);
                damage.setAmount(0.0f);
                auraShield.health -= damageAmount;
            } else {
                damageAmount -= auraShield.health;
                auraShield.health = 0.0f;
                damage.setAmount(damageAmount);
            }
            auraShield.checkOnDamageHealthChange = true;
        }
    }
}