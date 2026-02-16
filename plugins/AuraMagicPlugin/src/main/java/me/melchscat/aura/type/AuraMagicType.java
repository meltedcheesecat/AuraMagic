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

    /** Checks if this is a "Base" element or a mixture */
    public boolean isMixed() {
        return parent1 != null && parent2 != null;
    }

    /** Returns the cost to un-mix (Example logic) */
    public int getUnmixCost() {
        return isMixed() ? 50 : 0;
    }

    /** * Logic for Void/Infinity "Splitting"
     * Returns a 'Crazy' version of the spell if Infinity is applied
     */
    public String applyModifier(AuraMagicType modifier) {
        if (modifier == VOID) return "Controlled " + this.name();
        if (modifier == INFINITY) return "Shattered/Crazy " + this.name();
        return this.name();
    }

    /** * The Mixer: Finds a result based on two inputs.
     * Use this in your 'Crafting' or 'Casting' logic.
     */
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

