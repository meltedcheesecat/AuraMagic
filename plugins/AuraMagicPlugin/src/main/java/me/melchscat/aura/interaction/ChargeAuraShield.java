package me.melchscat.aura.interaction;

import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.melchscat.aura.AuraPlugin;
import me.melchscat.aura.component.AuraShieldComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ChargeAuraShield extends Interaction {
    protected float healthAdded;
    protected String particleSystemId;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComponentType;

    public static final BuilderCodec<ChargeAuraShield> CODEC =
            BuilderCodec.builder(
                            ChargeAuraShield.class,
                            ChargeAuraShield::new,
                            Interaction.ABSTRACT_CODEC
                    )
                    .<Float>appendInherited(
                            new KeyedCodec<>("HealthAdded", Codec.FLOAT),
                            (inAct, value) -> inAct.healthAdded = value,
                            inAct -> inAct.healthAdded,
                            (inAct1, inAct2) -> inAct1.healthAdded = inAct2.healthAdded
                    )
                    .add()
                    .<String>appendInherited(
                            new KeyedCodec<>("ParticleSystemId", Codec.STRING),
                            (inAct, value) -> inAct.particleSystemId = value,
                            inAct -> inAct.particleSystemId,
                            (inAct1, inAct2) -> inAct1.particleSystemId = inAct2.particleSystemId
                    )
                    .add()
                    .build();

    @Override
    protected void tick0(
            boolean firstRun,
            float time,
            InteractionType type,
            InteractionContext context,
            CooldownHandler cooldownHandler
    ) {
        if (firstRun) {
            auraShieldComponentType = AuraPlugin.getInstance().getAuraShieldComponentType();
        }

        // the owning Entity and entity seem to be the same thing, but owning Entity
        // is used in the code and entity isn't. In this case I hope it is the player
        Ref<EntityStore> owningEntityRef = context.getOwningEntity();

        if (!owningEntityRef.isValid())
          return;

        // if not exists then add to owningEntityRef, then return;
        Store<EntityStore> entityStore = owningEntityRef.getStore();
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

        // Add the passed health to the shield Component
        auraShieldComponent.addedHealth = healthAdded;

        // Keep the interaction alive or finish it
        context.getState().state = InteractionState.Finished;
    }

    @Override
    protected void simulateTick0(boolean b, float v, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        // Tell the client when to stop the animation
        interactionContext.getState().state = (v > 5.0f) ? InteractionState.Finished : InteractionState.NotFinished;
    }

    @Override
    public boolean walk(@NonNullDecl Collector collector, @NonNullDecl InteractionContext interactionContext) {
        return false; // No child interactions to report
    }

    @Override
    public boolean needsRemoteSync() {
        return true; // Yes, show my cool shield to others
    }

    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Client; // Don't wait for client confirmation
    }

    @NonNullDecl
    @Override
    protected com.hypixel.hytale.protocol.Interaction generatePacket() {
        // Standard packet for a basic interaction
        return new com.hypixel.hytale.protocol.SimpleInteraction();
    }

    // You can likely leave these as super calls unless you have advanced tagging needs
    @Override
    public Int2ObjectMap<IntSet> getTags() {
        return super.getTags();
    }

    @Override
    public Operation getInnerOperation() {
        return super.getInnerOperation();
    }
}
