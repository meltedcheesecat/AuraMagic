package me.melchscat.aura.page;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.logging.Level;

import static com.hypixel.hytale.logger.HytaleLogger.getLogger;

public class AuraShieldHud extends CustomUIHud {
    private String healthStr = "";

    public AuraShieldHud(@NonNullDecl PlayerRef playerRef, float health) {
        super(playerRef);
        this.healthStr = String.valueOf((int)health);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Pages/AuraWindShieldHUD.ui");
        uiCommandBuilder.set("#HealthValue.TextSpans", Message.raw(healthStr));
    }

    public void updateHealth(float health) {
        UICommandBuilder builder = new UICommandBuilder();
        healthStr = String.valueOf((int)health);
        builder.set("#HealthValue.TextSpans", Message.raw(healthStr));
        update(false, builder);  // false = don't clear existing HUD
    }
}
