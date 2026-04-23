package me.melchscat.aura.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.melchscat.aura.AuraMagicPlugin;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.myNPC.AuraStartNpc;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

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
        Ref<EntityStore> ref = context.getOwningEntity();
        if (!ref.isValid())
            return;

        Store<EntityStore> store = ref.getStore();

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        AuraStartNpc startNPC = AuraMagicPlugin.getInstance().getStartNPC();
        if (startNPC == null) return;

        if ((startNPC.removeItemFromPlayerInventory) && (!startNPC.removedItemSuccessfully)) {
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
            if (commandBuffer != null) {
                ItemStack itemToRemove = new ItemStack(startNPC.itemIdToRemove, startNPC.itemToRemoveQuantity);
                CombinedItemContainer itemContainer = InventoryComponent.getCombined(commandBuffer, ref, InventoryComponent.HOTBAR_STORAGE_BACKPACK);
                if (itemContainer.canRemoveItemStack(itemToRemove)) {
                    ItemStackTransaction transaction = itemContainer.removeItemStack(itemToRemove, false, false);
                    startNPC.removedItemSuccessfully = transaction.succeeded();
                } else {
                    startNPC.removedItemSuccessfully = false;
                }
            }
        }

        startNPC.requestTalk(playerRef);
    }

    public static final BuilderCodec<TalkToAuraStartNPC> CODEC = BuilderCodec.builder(
                    TalkToAuraStartNPC.class, TalkToAuraStartNPC::new, SimpleInteraction.CODEC)
            .build();
}
