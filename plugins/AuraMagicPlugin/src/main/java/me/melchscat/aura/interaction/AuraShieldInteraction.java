package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AuraShieldInteraction extends Interaction {

    // This matches "CurrentHealth": 0 in your JSON
    protected int currentHealth;

    public static final BuilderCodec<AuraShieldInteraction> CODEC =
            BuilderCodec.builder(
                            AuraShieldInteraction.class,
                            AuraShieldInteraction::new,
                            Interaction.ABSTRACT_CODEC
                    )
                    .<Integer>appendInherited(
                            new KeyedCodec<>("CurrentHealth", Codec.INTEGER),
                            (i, v) -> i.currentHealth = v, // Setter
                            i -> i.currentHealth,          // Getter
                            (i, p) -> i.currentHealth = p.currentHealth
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
        EventTitleUtil.showEventTitleToWorld(
                Message.raw("Shield Clicked"),
                Message.raw("Sub Title"),
                false,
                (String)null,
                4.0F,
                1.5F,
                1.5F,
                context.getEntity().getStore());

        if (firstRun) {
            // THIS IS YOUR EVENT FIRE
            System.out.println("Shield Interaction Started with Health: " + currentHealth);

            // Logic to spawn the visual shield or apply buffs
            applyShieldLogic(context);
        }

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
        return WaitForDataFrom.None; // Don't wait for client confirmation
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

    private void applyShieldLogic(InteractionContext context) {
    }
}
