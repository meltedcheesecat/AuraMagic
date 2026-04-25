package me.melchscat.aura.myNPC;

public enum AuraStartNpcSubStatus {
    AURA_START_NPC_INIT(false),
    AURA_START_NPC_START(false),
    AURA_START_NPC_TALK_1(true),
    AURA_START_NPC_DECLINE_QUEST(false),
    AURA_START_NPC_START_2(false),
    AURA_START_NPC_TALK_2(true),
    AURA_START_NPC_ACCEPT_QUEST(false),
    AURA_START_NPC_TALK_3(true),
    AURA_START_NPC_TALK_4(true),
    AURA_START_NPC_BUSY(false);

    public final Boolean isTalk;

    AuraStartNpcSubStatus(Boolean isTalk) {
        this.isTalk = isTalk;
    }
}
