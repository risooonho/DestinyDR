package net.dungeonrealms.game.command.menu;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.player.inventory.menus.guis.ParticleEffectGUI;
import net.dungeonrealms.game.player.menu.CraftingMenu;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class CommandTrail extends BaseCommand {

    public CommandTrail(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }

        Player player = (Player) sender;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null) return false;
        if (args.length == 0) {
            if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                wrapper.setActiveTrail(null);
                player.sendMessage(ChatColor.GREEN + "Your have disabled your trail.");
                return true;
            }

            ParticleEffect trailType = wrapper.getActiveTrail();
            if (trailType == null || trailType.equals("")) {
                player.sendMessage(ChatColor.RED + "You don't have an active trail, please enter the trails section in your profile to set one.");
                player.closeInventory();
                return true;
            }
            DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, trailType);
            player.sendMessage(ChatColor.GREEN + "Your active trail has been activated.");
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("o") || args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("v")) {
                new ParticleEffectGUI(player, null).open();
                return true;
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("g") || args[0].equalsIgnoreCase("get")) {
            	CraftingMenu.addTrailItem(player);
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
