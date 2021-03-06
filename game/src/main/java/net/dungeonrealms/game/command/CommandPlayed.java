package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPlayed extends BaseCommand {

    public CommandPlayed(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                Player player = (Player) sender;
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                if(wrapper == null)
                    return false;
                
                int minutesPlayed = wrapper.getPlayerGameStats().getStat(StatColumn.TIME_PLAYED);
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.UNDERLINE + ChatColor.BOLD + "Time Played: " + ChatColor.YELLOW + GameAPI.formatTime(minutesPlayed));
                return true;
            }
        }
        return false;
    }
}
