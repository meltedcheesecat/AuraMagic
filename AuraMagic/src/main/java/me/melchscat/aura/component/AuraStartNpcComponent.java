package me.melchscat.aura.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import me.melchscat.aura.myNPC.AuraStartNpc;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AuraStartNpcComponent implements Component<ChunkStore> {
    public static final BuilderCodec<AuraStartNpcComponent> CODEC =
            BuilderCodec.builder(AuraStartNpcComponent.class, AuraStartNpcComponent::new)
                    .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                            (c, v) -> c.enabled = v, c -> c.enabled)
                    .add()
                    .build();

    public boolean enabled = true;
    public AuraStartNpc auraStartNpc;

    @NullableDecl
    @Override
    public Component<ChunkStore> clone() {
        AuraStartNpcComponent auraStartNpcComponent = new AuraStartNpcComponent();
        auraStartNpcComponent.enabled = this.enabled;
        auraStartNpcComponent.auraStartNpc = this.auraStartNpc;
        return auraStartNpcComponent;
    }

    @NullableDecl
    @Override
    public Component<ChunkStore> cloneSerializable() {
        return Component.super.cloneSerializable();
    }
}
