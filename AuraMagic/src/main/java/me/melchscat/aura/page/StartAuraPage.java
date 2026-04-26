package me.melchscat.aura.page;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class StartAuraPage extends InteractiveCustomUIPage<StartAuraPage.filterEventData> {

    public StartAuraPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, filterEventData.CODEC);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref,
                      @NonNullDecl UICommandBuilder uiCommandBuilder,
                      @NonNullDecl UIEventBuilder uiEventBuilder,
                      @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/StartAuraPage.ui");
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref,
                                @NonNullDecl Store<EntityStore> store,
                                @NonNullDecl filterEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        player.getPageManager().setPage(ref, store, Page.None);
    }

    public static class filterEventData {
        public static final BuilderCodec<filterEventData> CODEC =
                BuilderCodec.<filterEventData>builder(
                        filterEventData.class, filterEventData::new
                ).build();
    }
}
