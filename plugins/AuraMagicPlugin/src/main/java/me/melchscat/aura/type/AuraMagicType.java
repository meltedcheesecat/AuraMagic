package me.melchscat.aura.type;

import java.util.Optional;

public enum AuraMagicType {
    // --- PRIMARY ELEMENTS ---
    WIND(null, null),
    WATER(null, null),
    EARTH(null, null),
    LIGHTNING(null, null),
    FIRE(null, null),
    VOID(null, null),
    INFINITY(null, null),

    // --- SECONDARY MIXTURES (Order: Parent1 + Parent2) ---
    ICE(WIND, WATER),
    LIFE(WATER, EARTH),
    MAGNETIC(EARTH, LIGHTNING),
    PLASMA(LIGHTNING, FIRE),
    RAGE(FIRE, WIND),

    DUST(WIND, EARTH),
    SOUND(WATER, LIGHTNING),
    LAVA(EARTH, FIRE),
    STORM(LIGHTNING, WIND),
    MIST(FIRE, WATER);

    private final AuraMagicType parent1;
    private final AuraMagicType parent2;

    // Constructor
    AuraMagicType(AuraMagicType p1, AuraMagicType p2) {
        this.parent1 = p1;
        this.parent2 = p2;
    }

    // --- LOGIC METHODS ---

    public boolean isMixed() {
        return parent1 != null && parent2 != null;
    }

    public int getUnmixCost() {
        return isMixed() ? 50 : 0;
    }

    public String applyModifier(AuraMagicType modifier) {
        if (modifier == VOID) return "Controlled " + this.name();
        if (modifier == INFINITY) return "Shattered/Crazy " + this.name();
        return this.name();
    }

    public static Optional<AuraMagicType> mix(AuraMagicType a, AuraMagicType b) {
        for (AuraMagicType type : values()) {
            if (type.isMixed()) {
                if ((type.parent1 == a && type.parent2 == b) ||
                        (type.parent1 == b && type.parent2 == a)) {
                    return Optional.of(type);
                }
            }
        }
        return Optional.empty();
    }
}

