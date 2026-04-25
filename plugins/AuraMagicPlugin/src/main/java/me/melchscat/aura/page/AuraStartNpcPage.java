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
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.myNPC.AuraStartNpc;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.List;

public class AuraStartNpcPage extends InteractiveCustomUIPage<AuraStartNpcPage.filterEventData> {
    private final int STORY_LINE_COUNT = 5;
    private String titleStr;
    private String imageTitleStr;
    private String imageFileNameStr;
    private List<String> storyStrList;
    private Boolean button1Visible;
    private String button1Str;
    private Boolean button2Visible;
    private String button2Str;
    private Ref<EntityStore> ourPageRef;
    private Boolean sendResponse;
    private Boolean hasResponded = false;

    public AuraStartNpcPage(@NonNullDecl PlayerRef playerRef, String titleStr, String imageTitleStr, String imageFileNameStr,
                            List<String> storyStrList, Boolean button1Visible, String button1Str, Boolean button2Visible, String button2Str,
                            Boolean sendResponse) {
        this.titleStr = titleStr;
        this.imageTitleStr = imageTitleStr;
        this.imageFileNameStr = imageFileNameStr;
        this.storyStrList = storyStrList;
        this.button1Visible = button1Visible;
        this.button1Str = button1Str;
        this.button2Visible = button2Visible;
        this.button2Str = button2Str;
        this.sendResponse = sendResponse;

        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, filterEventData.CODEC);
    }

    private String wrapText(String text, int maxWidth) {
        StringBuilder sb = new StringBuilder(text);
        int curIndex = 0;
        int startIndex = 0;
        int prevIndex = 1;
        while ((curIndex = sb.indexOf(" ", curIndex+1)) != -1) {
            if ((curIndex - startIndex) >= maxWidth) {
                sb.replace(prevIndex, prevIndex + 1, "\n");
                startIndex = prevIndex;
            }
            prevIndex = curIndex;
        }

        // end of line has no spaces
        if (((text.length()-1) - startIndex) >= maxWidth) {
            sb.replace(prevIndex, prevIndex + 1, "\n");
        }

        return sb.toString();
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref,
                      @NonNullDecl UICommandBuilder uiCommandBuilder,
                      @NonNullDecl UIEventBuilder uiEventBuilder,
                      @NonNullDecl Store<EntityStore> store) {
        this.ourPageRef = ref;
        uiCommandBuilder.append("Pages/AuraStartNPCPage.ui");
        uiCommandBuilder.set("#StartNPCPageTitle.Text", Message.translation(titleStr));
        uiCommandBuilder.set("#StartNPCImageTitle.Text", Message.translation(imageTitleStr));
        String imagePath = "UI/Custom/Pages/" + imageFileNameStr;
        uiCommandBuilder.set("#StartNPCImage.AssetPath", imagePath);

        String itemStr = "";
        int storyIndex = 0;
        for (int storyLine = 1; storyLine <= STORY_LINE_COUNT; storyLine++) {
            if (storyIndex < storyStrList.size()) {
                itemStr = wrapText(storyStrList.get(storyIndex), 55);
                uiCommandBuilder.set("#StartNPCStoryText" + storyLine + ".Text", Message.translation(itemStr));
                uiCommandBuilder.set("#StartNPCStoryText" + storyLine + ".Visible", true);
            } else {
                uiCommandBuilder.set("#StartNPCStoryText" + storyLine + ".Visible", false);
            }
            storyIndex++;
            if (storyIndex < storyStrList.size()) {
                imagePath = "UI/Custom/Pages/" + storyStrList.get(storyIndex);
                uiCommandBuilder.set("#StartNPCStoryImage" + storyLine + ".AssetPath", imagePath);
                uiCommandBuilder.set("#StartNPCStoryImage" + storyLine + ".Visible", true);
            } else {
                uiCommandBuilder.set("#StartNPCStoryImage" + storyLine + ".Visible", false);
            }
            storyIndex++;
        }
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

        if (sendResponse) {
            AuraStartNpc startNPC = AuraMagicPlugin.getInstance().getStartNPC();
            if (startNPC != null) {
                startNPC.hasPageResponse = true;
                startNPC.pageResponse = data.index;
                hasResponded = true;
            }
        }

        player.getPageManager().setPage(ref, store, Page.None);
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        if (hasResponded) return;

        if (sendResponse) {
            AuraStartNpc startNPC = AuraMagicPlugin.getInstance().getStartNPC();
            if (startNPC != null) {
                startNPC.hasPageResponse = true;
                startNPC.pageResponse = 0;
            }
        }
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
