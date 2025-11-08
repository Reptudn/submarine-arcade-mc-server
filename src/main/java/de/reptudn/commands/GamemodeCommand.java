package de.reptudn.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /gamemode <mode>");
        });

        var gamemodeTypeArgument = ArgumentType.String("mode");

        addSyntax((sender, context) -> {
           final String mode = context.get(gamemodeTypeArgument);

           if (!(sender instanceof Player player)) {
               sender.sendMessage("Only players can change their gamemode.");
               return;
           }

           switch (mode.toLowerCase()) {
               case "survival", "s" -> {
                   player.setGameMode(GameMode.SURVIVAL);
                   player.sendMessage("Gamemode set to Survival");
                }
                case "creative", "c" -> {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage("Gamemode set to Creative");
                }
                case "adventure", "a" -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.sendMessage("Gamemode set to Adventure");
                }
                case "spectator", "sp" -> {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("Gamemode set to Spectator");
                }
                default -> sender.sendMessage("Unknown gamemode: " + mode);
           }
        }, gamemodeTypeArgument);
    }

}
