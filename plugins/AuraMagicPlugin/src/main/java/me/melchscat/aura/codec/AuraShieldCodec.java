package me.melchscat.aura.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;

public class AuraShieldCodec {
    public String name;
    public float value;
    public boolean enabled;

    public static final BuilderCodec<AuraShieldCodec> CODEC =
            BuilderCodec.builder(AuraShieldCodec.class, AuraShieldCodec::new)
                    .append(new KeyedCodec<>("Name", Codec.STRING),
                            (config, name) -> config.name = name,
                            config -> config.name
                    )
                    .addValidator(Validators.nonNull())
                    .documentation("Configuration name")
                    .add()

                    .append(new KeyedCodec<>("Value", Codec.FLOAT),
                            (config, value) -> config.value = value,
                            config -> config.value
                    )
                    .documentation("Numeric value")
                    .add()

                    .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                            (config, enabled) -> config.enabled = enabled,
                            config -> config.enabled
                    )
                    .add()

                    .build();
}
