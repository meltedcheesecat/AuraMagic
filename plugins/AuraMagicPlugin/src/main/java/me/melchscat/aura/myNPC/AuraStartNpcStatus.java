package me.melchscat.aura.myNPC;

public enum AuraStartNpcStatus {
    // defined backward so the (nextState) works
    AURA_START_NPC_END(null),
    AURA_START_NPC_READY_TO_TRAIN(AURA_START_NPC_END),
    AURA_START_NPC_GET_OUT_OF_POT(AURA_START_NPC_READY_TO_TRAIN),
    AURA_START_NPC_STUCK_IN_POT_QUEST(AURA_START_NPC_GET_OUT_OF_POT),
    AURA_START_NPC_STUCK_IN_POT(AURA_START_NPC_STUCK_IN_POT_QUEST);

    public final AuraStartNpcStatus nextState;

    AuraStartNpcStatus(AuraStartNpcStatus nextState) {
        this.nextState = nextState;
    }
}
