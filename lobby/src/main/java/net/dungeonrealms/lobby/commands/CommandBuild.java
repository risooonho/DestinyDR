package net.dungeonrealms.lobby.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.Lobby;
import net.md_5.bungee.api.ChatColor;

public class CommandBuild extends BaseCommand {

	public CommandBuild() {
		super("build", "/<command>", "Send yourself to the build server.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || !Lobby.getInstance().isLoggedIn((Player)sender))
			return false;
		
		if (args.length > 0 && args[0].equalsIgnoreCase("kata")) {
			BungeeUtils.sendToServer(sender.getName(), "kata");
			return true;
		}

		if (args.length == 0 || !args[0].equalsIgnoreCase("creative"))
			return false;

		sender.sendMessage(ChatColor.YELLOW + ChatColor.ITALIC.toString() + "Attempting to send you to the build server...");
		BungeeUtils.sendToServer(sender.getName(), "buildserver");
		
		return true;
	}
}
