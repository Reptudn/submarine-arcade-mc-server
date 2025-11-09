package de.reptudn;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import de.reptudn.commands.GamemodeCommand;
import de.reptudn.commands.TestCommand;
import de.reptudn.config.GameConfig;
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

        JNoise noise = JNoise.newBuilder()
                .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
                .scale(0.01)
                .build();

        instanceContainer.setGenerator(unit -> {
            Point start = unit.absoluteStart();
            for (int x = 0; x < unit.size().x(); x++) {
                for (int z = 0; z < unit.size().z(); z++) {
                    Point bottom = start.add(x, 0, z);

                    synchronized (noise) { // Synchronization is necessary for JNoise
                        // Generate mountain height using GameConfig values
                        double heightRange = GameConfig.MAX_MOUNTAIN_HEIGHT - GameConfig.MIN_MOUNTAIN_HEIGHT;
                        double height = noise.evaluateNoise(bottom.x(), bottom.z()) * (heightRange / 2) + (heightRange / 2) + GameConfig.MIN_MOUNTAIN_HEIGHT;
                        height = Math.max(GameConfig.MIN_MOUNTAIN_HEIGHT, Math.min(GameConfig.MAX_MOUNTAIN_HEIGHT, height));

                        // Place bedrock at y=0 (bottom layer)
                        unit.modifier().setBlock(bottom, Block.BEDROCK);

                        // Fill stone mountains from min height to calculated height
                        if (height > GameConfig.MIN_MOUNTAIN_HEIGHT) {
                            unit.modifier().fill(bottom.add(0, GameConfig.MIN_MOUNTAIN_HEIGHT, 0), bottom.add(1, height, 1), Block.STONE);
                        }

                        // Fill with water from mountain surface to configured sea height
                        if (height < GameConfig.SEA_HEIGHT) {
                            unit.modifier().fill(bottom.add(0, height, 0), bottom.add(1, GameConfig.SEA_HEIGHT, 1), Block.WATER);
                        }
                    }
                }
            }
        });

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

        cmdManager.register(new TestCommand(), new GamemodeCommand());
    }
}

