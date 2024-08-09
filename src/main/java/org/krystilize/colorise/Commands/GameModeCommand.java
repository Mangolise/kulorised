package org.krystilize.colorise.Commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.krystilize.colorise.Util;

public class GameModeCommand extends Command {
    public GameModeCommand() {
        super("gamemode", "gm", "gmc", "gms", "gma");

        setCondition((sender, s) -> sender instanceof Player player && Util.ADMINS.contains(player.getUsername()));

        addSyntax(this::executeNoArgs);
        addSyntax(this::executeArgs, ArgumentType.Enum("gamemode", GameMode.class));
    }

    private void executeNoArgs(CommandSender sender, CommandContext context) {
        switch (context.getInput()) {
            case "gms", "gma" -> execute(sender, GameMode.ADVENTURE);
            case "gmc" -> execute(sender, GameMode.CREATIVE);
        }
    }

    private void executeArgs(CommandSender sender, CommandContext context) {
        GameMode gamemode = context.get("gamemode");
        execute(sender, gamemode);
    }

    private void execute(CommandSender sender, GameMode gamemode) {
        if (gamemode == GameMode.SURVIVAL) {
            gamemode = GameMode.ADVENTURE;
        }

        if (gamemode != GameMode.ADVENTURE && gamemode != GameMode.CREATIVE) {
            sender.sendMessage("You can only do adventure or creative!");
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("You have to be a player to run this command!");
            return;
        }

        Util.setPlayerGamemode(player, gamemode);
    }
}
