package de.reptudn.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Usage: /gamemode <mode>");
        });

        var gamemodeTypeArgument = getArgumentString();

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

    private static @NotNull ArgumentString getArgumentString() {
        var gamemodeTypeArgument = ArgumentType.String("mode");
        gamemodeTypeArgument.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("survival"));
            suggestion.addEntry(new SuggestionEntry("creative"));
            suggestion.addEntry(new SuggestionEntry("adventure"));
            suggestion.addEntry(new SuggestionEntry("spectator"));
            suggestion.addEntry(new SuggestionEntry("s"));
            suggestion.addEntry(new SuggestionEntry("c"));
            suggestion.addEntry(new SuggestionEntry("a"));
            suggestion.addEntry(new SuggestionEntry("sp"));
        });
        return gamemodeTypeArgument;
    }

}
