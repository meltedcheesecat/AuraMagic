package me.melchscat.aura.page;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AuraStartNpcPage extends InteractiveCustomUIPage<AuraStartNpcPage.filterEventData> {
    public AuraStartNpcPage(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, filterEventData.CODEC);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref,
                      @NonNullDecl UICommandBuilder uiCommandBuilder,
                      @NonNullDecl UIEventBuilder uiEventBuilder,
                      @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/AuraStartNPCPage.ui");
        String testTitle = "Test Aura Magic";
        uiCommandBuilder.set("#StartNPCPageTitle.Text", Message.translation(testTitle));
        String testImageTitle = "Test Image";
        uiCommandBuilder.set("#StartNPCImageTitle.Text", Message.translation(testImageTitle));
        String imagePath = "UI/Custom/Pages/StuckInPotStartNPC.png";
        uiCommandBuilder.set("#StartNPCImage.AssetPath", imagePath);
        //uiCommandBuilder.setNull("#StartNPCImage.AssetPath");
        //String testText = "Hello" + System.lineSeparator() + "my name is";
        /*String testStoryText = "This is a test piece of text1." + System.lineSeparator() +
                               "This is a test piece of text2." + System.lineSeparator() +
                               "This is a test piece of text3." + System.lineSeparator() +
                               "This is a test piece of text4.";*/
        String testStoryText = "This is a long continuance line of text to see if the text wraps correctly so if I write a story it is all in screen.";
        uiCommandBuilder.set("#StartNPCStoryText.Text", Message.translation(testStoryText));
        String testButtonText = "Test Button";
        uiCommandBuilder.set("#ButtonAura50.Text", Message.translation(testButtonText));

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ButtonAura50");
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
                BuilderCodec.<filterEventData>builder(filterEventData.class, filterEventData::new)
                        .build();
    }
}
