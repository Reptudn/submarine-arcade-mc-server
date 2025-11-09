package de.reptudn;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import de.reptudn.commands.GamemodeCommand;
import de.reptudn.commands.ReloadCommand;
import de.reptudn.commands.TestCommand;
import de.reptudn.config.GameConfig;
import de.reptudn.game.Weapons.SubmarineGun;
import de.reptudn.game.Weapons.Weapon;
import de.reptudn.game.Weapons.WeaponManager;
import de.reptudn.world.WorldGeneratorUnderwater;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.ping.Status;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.identity.NamedAndIdentified;

public class Main {
    public static void main(String[] args) {
        final String address = args.length >= 2 ? args[1] : "0.0.0.0";
        final int port = args.length >= 3 ? Integer.parseInt(args[2]) : 25565;

        System.out.println("Setting up Server...\n\tAddress: " + address + "\n\tPort: " + port);

        // Init basic minecraft server
        MinecraftServer minecraftServer = MinecraftServer.init();



        // Create an instance container -> Instance = World
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // instanceContainer.viewDistance(100); // das crazy view distance.. egal wir probieren xD

        instanceContainer.setGenerator(new WorldGeneratorUnderwater(System.currentTimeMillis()));

        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        // player spawning
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 120, 0));
        });

        // player UI setup after spawn
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();

            var scoreboardDividerLine = Component.text("----------------", NamedTextColor.DARK_GRAY);

            Sidebar sidebar = new Sidebar(Component.text("SubWars", NamedTextColor.GOLD));
            sidebar.createLine(new Sidebar.ScoreboardLine("divider", scoreboardDividerLine, 0));
            sidebar.createLine(new Sidebar.ScoreboardLine("header", Component.text("Welcome to SubWars!", NamedTextColor.GREEN), 0));
            sidebar.createLine(new Sidebar.ScoreboardLine("description", Component.text("Enjoy your stay!", NamedTextColor.AQUA), 0));
            sidebar.addViewer(player);

            WeaponManager.giveWeapon(player, new SubmarineGun());

            Notification notification = new Notification(
                Component.text("Yo what's up!", NamedTextColor.DARK_RED),
                FrameType.TASK,
                ItemStack.of(Material.BIRCH_TRAPDOOR)
            );

            player.sendPacket(notification.buildAddPacket());

            player.addEffect(new Potion(
                PotionEffect.NIGHT_VISION,
                100, // Level 1 (0-based indexing)
                Integer.MAX_VALUE // Permanent duration
            ));

            player.addEffect(new Potion(
                PotionEffect.WATER_BREATHING, 100, // Level 1
                Integer.MAX_VALUE // Permanent duration
            ));

            player.addEffect(new Potion(
                PotionEffect.CONDUIT_POWER, 100, // Level 1
                Integer.MAX_VALUE // Permanent duration
            ));

            Scheduler scheduler = player.scheduler();
            Task task = scheduler.scheduleNextTick(() -> System.out.println("Hey!"));
            task.cancel();

        });

        // Weapon interaction events
        globalEventHandler.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();

            if (!WeaponManager.hasWeapon(player)) return;

            Weapon weapon = WeaponManager.getWeapon(player);
            if (weapon instanceof SubmarineGun submarineGun) {
                // Check if player is holding the weapon (crossbow)
                if (player.getItemInMainHand().material() == Material.CROSSBOW) {
                    // Cancel the event to prevent item transformation
                    event.setCancelled(true);

                    // Shoot the weapon
                    submarineGun.fireProjectile(player);

                    // Update weapon item immediately and with slight delay to ensure it stays correct
                    WeaponManager.updateWeaponItem(player);
                    player.scheduler().scheduleNextTick(() -> {
                        WeaponManager.updateWeaponItem(player);
                    });
                }
            }
        });

        // Add right-click interaction for reloading
        globalEventHandler.addListener(net.minestom.server.event.player.PlayerEntityInteractEvent.class, event -> {
            Player player = event.getPlayer();

            if (!WeaponManager.hasWeapon(player)) return;

            Weapon weapon = WeaponManager.getWeapon(player);
            if (weapon instanceof SubmarineGun submarineGun && player.getItemInMainHand().material() == Material.CROSSBOW) {
                if (player.isSneaking()) {
                    // Shift + Right click to reload
                    submarineGun.reload();
                    WeaponManager.updateWeaponItem(player);
                    player.sendMessage(Component.text("Waffe nachgeladen! Munition: " +
                        submarineGun.getAmmo() + "/" + submarineGun.getMaxAmmo(), NamedTextColor.GREEN));
                }
            }
        });

        // Server Details on Server List Ping
        globalEventHandler.addListener(ServerListPingEvent.class, event -> {
            event.setStatus(Status.builder()
                    .description(Component.text("SubWars: Steeldiver Revived?!", NamedTextColor.GOLD))
                    .playerInfo(Status.PlayerInfo.builder()
                            .onlinePlayers(420)
                            .maxPlayers(500)
                            .sample(NamedAndIdentified.named(Component.text("Reptudn", NamedTextColor.AQUA)))
                            .build())
                    .playerInfo(420, 500) // simpler alternative to set player count only
                    .versionInfo(new Status.VersionInfo("Skill", 47)) // set some fake version info
                    .build());
        });

        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.scheduleNextTick(() -> System.out.println("Hey!"));
        scheduler.submitTask(() -> {
            instanceContainer.sendMessage(Component.text("1 minute has passed in the server!", NamedTextColor.YELLOW));
            return TaskSchedule.minutes(1);
        });

        System.out.println("Registering Commands...");
        registerCommands();

        System.out.println("Starting Minecraft Server...");
        minecraftServer.start(address, port);
    }

    private static void registerCommands() {
        var cmdManager = MinecraftServer.getCommandManager();

        cmdManager.register(new TestCommand(), new GamemodeCommand(), new ReloadCommand());
    }
}

