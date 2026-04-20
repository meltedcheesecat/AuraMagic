package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;

import javax.annotation.Nonnull;

public class TalkToAuraStartNPC extends SimpleInteraction {
    private ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcCompType = null;

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @Nonnull InteractionType type,
                         @Nonnull InteractionContext context,
                         @Nonnull CooldownHandler cooldownHandler) {
        if (auraStartNpcCompType == null) {
            auraStartNpcCompType = AuraMagicPlugin.getInstance().getAuraStartNpcCompType();
        }

        // very simple gets the playerRef and gets the startNPC and asks to talk
        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        if (!owningEntityRef.isValid())
            return;

        Store<EntityStore> store = owningEntityRef.getStore();

        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        AuraStartNpc startNPC = AuraMagicPlugin.getInstance().getStartNPC();
        if (startNPC == null) return;

        startNPC.requestTalk(playerRef);
    }

    public static final BuilderCodec<TalkToAuraStartNPC> CODEC = BuilderCodec.builder(
                    TalkToAuraStartNPC.class, TalkToAuraStartNPC::new, SimpleInteraction.CODEC)
            .build();
}
