package de.reptudn.commands;

import net.minestom.server.command.builder.Command;

public class TestCommand extends Command {
    public TestCommand() {
        super("test", "t");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Test command executed!");
        });
    }
}
