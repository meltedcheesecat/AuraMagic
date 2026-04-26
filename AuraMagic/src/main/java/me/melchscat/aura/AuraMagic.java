package me.melchscat.aura;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import me.melchscat.aura.component.AuraBlockLifetimeComponent;
import me.melchscat.aura.component.AuraShieldComponent;
import me.melchscat.aura.component.AuraStartNpcComponent;
import me.melchscat.aura.interaction.ChargeAuraShield;
import me.melchscat.aura.interaction.CreateAuraWindBlocks;
import me.melchscat.aura.interaction.ShowStartAuraPage;
import me.melchscat.aura.interaction.TalkToAuraStartNPC;
import me.melchscat.aura.main.AuraMain;
import me.melchscat.aura.myNPC.AuraStartNpc;
import me.melchscat.aura.prefab.AuraPrefabs;
import me.melchscat.aura.system.AuraBlockLifetimeSystem;
import me.melchscat.aura.system.AuraStartNpcSystem;
import me.melchscat.aura.worldgen.CustomWorldGenProvider;
import me.melchscat.aura.system.AuraShieldSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.util.logging.Level;

public class AuraMagic extends JavaPlugin {
    private static AuraMagic instance;
    private ComponentType<EntityStore, AuraShieldComponent> auraShieldComType;
    private ComponentType<ChunkStore, AuraBlockLifetimeComponent> auraBlockLifetimeComType;
    private ComponentType<ChunkStore, AuraStartNpcComponent> auraStartNpcCompType;
    private AuraPrefabs auraPrefabs;
    private AuraMain auraMain;
    private AuraStartNpc startNPC;

    public AuraMagic(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        // replaces the hytale world generator with a custom one
        var defaultProvider = IWorldGenProvider.CODEC.getClassFor("Hytale");
        IWorldGenProvider.CODEC.remove(defaultProvider);
        IWorldGenProvider.CODEC.register(Priority.DEFAULT.before(1), "Hytale", CustomWorldGenProvider.class, CustomWorldGenProvider.CODEC);

        // Magic Block lifetime, currently used for the block spell
        auraBlockLifetimeComType = ChunkStore.REGISTRY.registerComponent(AuraBlockLifetimeComponent.class, "AuraBlockLifetime",  AuraBlockLifetimeComponent.CODEC);
        Interaction.CODEC.register("CreateAuraWindBlocks", CreateAuraWindBlocks.class, CreateAuraWindBlocks.CODEC);

        // Magic Shield, for now this is wind only
        auraShieldComType = EntityStore.REGISTRY.registerComponent(AuraShieldComponent.class, AuraShieldComponent::new);
        Interaction.CODEC.register("ChargeAuraShield", ChargeAuraShield.class, ChargeAuraShield.CODEC);

        // Aura Start NPC
        auraStartNpcCompType = ChunkStore.REGISTRY.registerComponent(AuraStartNpcComponent.class, "AuraBlockStartNpc",  AuraStartNpcComponent.CODEC);
        Interaction.CODEC.register("TalkToAuraStartNPC", TalkToAuraStartNPC.class, TalkToAuraStartNPC.CODEC);

        // Aura Start Page shown by mannequin
        Interaction.CODEC.register("ShowStartAuraPage", ShowStartAuraPage.class, ShowStartAuraPage.CODEC);
        getLogger().at(Level.INFO).log("AuraLog setup");
    }

    @Override
    protected void start() {
        // Magic Block lifetime system
        getChunkStoreRegistry().registerSystem(new AuraBlockLifetimeSystem(auraBlockLifetimeComType));

        // Magic Shield system, for now this is wind only
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem(auraShieldComType));
        getEntityStoreRegistry().registerSystem(new AuraShieldSystem.OnDamageReceived(auraShieldComType));

        // Aura Start NPC
        getChunkStoreRegistry().registerSystem(new AuraStartNpcSystem(auraStartNpcCompType));

        // Events
        getEventRegistry().registerGlobal(StartWorldEvent.class, this::onStartWorld);
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);

        getEventRegistry().registerGlobal(PlayerConnectEvent.class, this::onPlayerConnect);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);

        getLogger().at(Level.INFO).log("AuraLog start");
    }

    @Override
    protected void shutdown() {
        startNPC.writeData();

        getLogger().at(Level.INFO).log("AuraLog shutdown");
    }

    public void onStartWorld(StartWorldEvent event) {
        AuraMain gotAuraMain = getAuraMain();
        if (!gotAuraMain.initialized) {
            gotAuraMain.Initialize(event.getWorld());
        }

        startNPC = new AuraStartNpc(gotAuraMain);
        startNPC.readData();
        startNPC.init(gotAuraMain);
    }

    public void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        getLogger().at(Level.INFO).log("AuraLog onPlayerReady name:" + player.getDisplayName());
    }

    public void onPlayerConnect(PlayerConnectEvent event) {
        getLogger().at(Level.INFO).log("AuraLog onPlayerConnect toString:" + event.toString());
    }

    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        startNPC.checkDisconnectPlayerRef = true;
        startNPC.disconnectPlayerRef = event.getPlayerRef();

        getLogger().at(Level.INFO).log("AuraLog onPlayerDisconnect toString:" + event.toString());
    }

    public static AuraMagic getInstance() {
        return instance;
    }

    public ComponentType<EntityStore, AuraShieldComponent> getAuraShieldComponentType() {
        return this.auraShieldComType;
    }

    public ComponentType<ChunkStore, AuraBlockLifetimeComponent> getAuraBlockLifetimeComponentType() {
        return this.auraBlockLifetimeComType;
    }

    public ComponentType<ChunkStore, AuraStartNpcComponent> getAuraStartNpcCompType() {
        return this.auraStartNpcCompType;
    }

    public AuraPrefabs getAuraPrefabs() {
        if (this.auraPrefabs == null)
          auraPrefabs = new AuraPrefabs();

        return this.auraPrefabs;
    }

    public AuraMain getAuraMain() {
        if (this.auraMain == null)
            auraMain = new AuraMain();

        return this.auraMain;
    }

    public AuraStartNpc getStartNPC() {
        return this.startNPC;
    }
}
