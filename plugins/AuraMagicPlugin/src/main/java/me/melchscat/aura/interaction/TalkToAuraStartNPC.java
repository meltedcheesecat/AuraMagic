package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.page.AuraStartNpcPage;

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

        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        if (!owningEntityRef.isValid())
            return;

        Store<EntityStore> store = owningEntityRef.getStore();

        Player player = store.getComponent(owningEntityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = player.getWorld();
        if (world == null) return;

        world.execute( () -> {
            AuraStartNpcPage startNpcPage = new AuraStartNpcPage(playerRef,
                    "Chat Dialog", "Voice from the Jar", "StuckInPotStartNPC.png",
                    "You hear a voice coming from the shaking Pot, you walk towards it and say Hello. It responds and tells you it has been stuck in here for ages and it needs help getting out.",
                    false, "NoButton", true, "Accept Quest");

            player.getPageManager().openCustomPage(owningEntityRef, store, startNpcPage);
        });
    }

    public static final BuilderCodec<TalkToAuraStartNPC> CODEC = BuilderCodec.builder(
                    TalkToAuraStartNPC.class, TalkToAuraStartNPC::new, SimpleInteraction.CODEC)
            .build();
}
