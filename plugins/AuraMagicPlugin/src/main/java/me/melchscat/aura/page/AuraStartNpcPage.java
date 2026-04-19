package me.melchscat.aura.page;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraStartNpcPage extends InteractiveCustomUIPage<AuraStartNpcPage.filterEventData> {
    private String titleStr;
    private String imageTitleStr;
    private String imageFileNameStr;
    private String storyStr;
    private Boolean button1Visible;
    private String button1Str;
    private Boolean button2Visible;
    private String button2Str;

    public AuraStartNpcPage(@NonNullDecl PlayerRef playerRef, String titleStr, String imageTitleStr, String imageFileNameStr,
                            String storyStr, Boolean button1Visible, String button1Str, Boolean button2Visible, String button2Str) {
        this.titleStr = titleStr;
        this.imageTitleStr = imageTitleStr;
        this.imageFileNameStr = imageFileNameStr;
        this.storyStr = storyStr;
        this.button1Visible = button1Visible;
        this.button1Str = button1Str;
        this.button2Visible = button2Visible;
        this.button2Str = button2Str;
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, filterEventData.CODEC);
    }

    private String wrapText(String text, int width) {
        StringBuilder sb = new StringBuilder(text);
        int i = 0;
        while ((i = sb.indexOf(" ", i + width)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref,
                      @NonNullDecl UICommandBuilder uiCommandBuilder,
                      @NonNullDecl UIEventBuilder uiEventBuilder,
                      @NonNullDecl Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/AuraStartNPCPage.ui");
        uiCommandBuilder.set("#StartNPCPageTitle.Text", Message.translation(titleStr));
        uiCommandBuilder.set("#StartNPCImageTitle.Text", Message.translation(imageTitleStr));
        String imagePath = "UI/Custom/Pages/" + imageFileNameStr;
        uiCommandBuilder.set("#StartNPCImage.AssetPath", imagePath);
        String testStoryText = wrapText(storyStr, 50);
        uiCommandBuilder.set("#StartNPCStoryText.Text", Message.translation(testStoryText));
        uiCommandBuilder.set("#ButtonAura1.Visible", button1Visible);
        uiCommandBuilder.set("#ButtonAura1.Text", Message.translation(button1Str));
        uiCommandBuilder.set("#ButtonAura2.Visible", button2Visible);
        uiCommandBuilder.set("#ButtonAura2.Text", Message.translation(button2Str));

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ButtonAura1", EventData.of("Index", "1"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ButtonAura2", EventData.of("Index", "2"), false);
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref,
                                @NonNullDecl Store<EntityStore> store,
                                @NonNullDecl filterEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        getLogger().at(Level.INFO).log("AuraLog handleDataEvent button index:" + data.indexStr + ", player:" + playerRef.getUsername());

        player.getPageManager().setPage(ref, store, Page.None);
    }

    public static class filterEventData {
        static final String ELEMENT_INDEX = "Index";
        private String indexStr;
        public int index;

        public static final BuilderCodec<filterEventData> CODEC =
                BuilderCodec.<filterEventData>builder(filterEventData.class, filterEventData::new)
                        .append(
                                // "Index" = read from button ID input
                                new KeyedCodec<>("Index", Codec.STRING),
                                // Setter: put the value into obj.indexStr and obj.index
                                (filterEventData obj, String val) -> {
                                    obj.indexStr = val;
                                    obj.index = Integer.parseInt(val);
                                    },
                                // Getter: read the value from obj.indexStr
                                (filterEventData obj) -> obj.indexStr
                        )
                        .add()
                        .build();
    }
}
