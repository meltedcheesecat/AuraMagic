package me.melchscat.aura.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AuraBlockLifetimeComponent implements Component<ChunkStore> {
    public static final BuilderCodec<AuraBlockLifetimeComponent> CODEC =
            BuilderCodec.builder(AuraBlockLifetimeComponent.class, AuraBlockLifetimeComponent::new)
                    .append(new KeyedCodec<>("StartTick", Codec.LONG),
                            (c, v) -> c.startTick = v, c -> c.startTick)
                    .add()
                    .append(new KeyedCodec<>("LifeTickLength", Codec.LONG),
                            (c, v) -> c.lifeTickLength = v, c -> c.lifeTickLength)
                    .add()
                    .build();

    public long startTick;
    public long lifeTickLength;

    @NullableDecl
    @Override
    public Component<ChunkStore> clone() {
        AuraBlockLifetimeComponent auraBlockLifetimeComponent = new AuraBlockLifetimeComponent();
        auraBlockLifetimeComponent.startTick = this.startTick;
        auraBlockLifetimeComponent.lifeTickLength = this.lifeTickLength;
        return auraBlockLifetimeComponent;
    }

    @NullableDecl
    @Override
    public Component<ChunkStore> cloneSerializable() {
        return Component.super.cloneSerializable();
    }
}
