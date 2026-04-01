package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.page.StartAuraPage;

import javax.annotation.Nonnull;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class ShowStartAuraPage extends SimpleInteraction {
    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @Nonnull InteractionType type,
                         @Nonnull InteractionContext context,
                         @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        Store<EntityStore> store = owningEntityRef.getStore();

        Player player = store.getComponent(owningEntityRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        World world = player.getWorld();
        if (world == null) return;

        world.execute( () -> {
            player.getPageManager().openCustomPage(owningEntityRef, store, new StartAuraPage(playerRef));
        });
    }

    public static final BuilderCodec<ShowStartAuraPage> CODEC = BuilderCodec.builder(
                    ShowStartAuraPage.class, ShowStartAuraPage::new, ShowStartAuraPage.CODEC)
            .build();
}
