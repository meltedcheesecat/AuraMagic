package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagic;
import me.melchscat.aura.component.AuraShieldComponent;

import javax.annotation.Nonnull;

public class ChargeAuraShield extends SimpleInteraction {
    protected float healthAdded;
    protected String modelId = "";
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @Nonnull InteractionType type,
                         @Nonnull InteractionContext context,
                         @Nonnull CooldownHandler cooldownHandler) {
        if (firstRun) {
            auraShieldComponentType = AuraMagic.getInstance().getAuraShieldComponentType();
        }

        // the owning Entity and entity seem to be the same thing, but owning Entity
        // is used in the code and entity isn't. In this case I hope it is the player
        Ref<EntityStore> owningEntityRef = context.getOwningEntity();

        if (!owningEntityRef.isValid())
            return;

        // if not exists then add to owningEntityRef, then return;
        Store<EntityStore> entityStore = owningEntityRef.getStore();

        DynamicLight shieldDynamicLight = entityStore.getComponent(owningEntityRef, DynamicLight.getComponentType());
        if (shieldDynamicLight == null) {
            shieldDynamicLight = new DynamicLight();

            // you can't write to the store while in a tick and the commandbuffer
            // allows you to buffer these commands for later
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

            if (commandBuffer == null)
                return;

            commandBuffer.putComponent(owningEntityRef, DynamicLight.getComponentType(), shieldDynamicLight);
        }


        AuraShieldComponent auraShieldComponent = entityStore.getComponent(owningEntityRef, auraShieldComponentType);

        if (auraShieldComponent == null) {
            auraShieldComponent = new AuraShieldComponent();

            // you can't write to the store while in a tick and the commandbuffer
            // allows you to buffer these commands for later
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

            if (commandBuffer == null)
                return;

            commandBuffer.putComponent(owningEntityRef, auraShieldComponentType, auraShieldComponent);
        }

        if (auraShieldComponent.ourPlayer == null)
            auraShieldComponent.ourPlayer = entityStore.getComponent(owningEntityRef, Player.getComponentType());

        if (auraShieldComponent.ourPlayer == null)
            return;

        // Add the passed health to the shield Component
        auraShieldComponent.addedHealth = healthAdded;

        // update new particle Id
        if ((modelId != null) && (modelId.compareTo(auraShieldComponent.modelId) != 0))
        {
            auraShieldComponent.hasNewModelId = true;
            auraShieldComponent.modelId = modelId;
        }

        // Keep the interaction alive or finish it
        context.getState().state = InteractionState.Finished;
    }

    public static final BuilderCodec<ChargeAuraShield> CODEC = BuilderCodec.builder(
                    ChargeAuraShield.class, ChargeAuraShield::new, SimpleInteraction.CODEC)
            .documentation("Adds a shield to the Player")
            .append(new KeyedCodec<>("HealthAdded", Codec.FLOAT, true),
                    (shield, health) -> shield.healthAdded = health,
                    shield -> shield.healthAdded)
            .documentation("The health to bed added to the shield.")
            .add()
            .<String>appendInherited(
                    new KeyedCodec<>("ModelId", Codec.STRING),
                    (shield, id) -> shield.modelId = id,
                    shield -> shield.modelId,
                    (shield1, shield2) -> shield1.modelId = shield2.modelId)
            .documentation("The test particle.")
            .add()
            .build();
}
