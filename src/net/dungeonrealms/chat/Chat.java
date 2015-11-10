package net.dungeonrealms.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

    static Chat instance = null;

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    List<String> TERRIBLE_WORDS = Arrays.asList(
            "shit",
            "nigger",
            "bitch",
            "xwaffle",
            "xwaffle the developer",
            "kayaba",
            "dungeonrealms.us",
            "minecade",
            "xwaffle the br",
            "FUCK THE BRS",
            "ITS A BR",
            "RUN ITS A BR",
            "chewedmarkers"
    );

    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        if (event.getMessage().startsWith("@")) {
            String playerName = event.getMessage().replace("@", "").split(" ")[0];
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).limit(1).forEach(player1 -> {
                player1.sendMessage(ChatColor.GRAY + event.getPlayer().getName() + ": " + event.getMessage().replace("@" + playerName, ""));
                event.getPlayer().sendMessage((ChatColor.GRAY + playerName + " -> " + event.getMessage().replace("@" + playerName, "")));
            });
            event.setCancelled(true);
            return;
        }

        TERRIBLE_WORDS.stream().filter(s -> event.getMessage().contains(s.toLowerCase())).forEach(s1 -> {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Wow! You have used a terrible word.. Please rethink your sentence!");
        });

        UUID uuid = event.getPlayer().getUniqueId();
        StringBuilder prefix = new StringBuilder();

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        prefix.append(gChat ?
                ChatColor.GREEN + "<" + ChatColor.AQUA.toString() + ChatColor.BOLD + "G" + ChatColor.GREEN + ">" + ChatColor.RESET + ""
                :
                ChatColor.GREEN + "<" + ChatColor.BOLD + "L" + ChatColor.GREEN + ">" + ChatColor.RESET + "");

        Rank.RankBlob r = Rank.getInstance().getRank(uuid);
        if (r != null && !r.getPrefix().equals("null")) {
            if (r.getName().equalsIgnoreCase("DEFAULT")) {
                prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + ""));
            } else if (!r.getName().equalsIgnoreCase("DEFAULT")) {
                prefix.append(ChatColor.translateAlternateColorCodes('&', " " + r.getPrefix() + ChatColor.RESET));
            }
        } else {
            Utils.log.warning("Rank is null for player: " + event.getPlayer().getName());
        }

        if (!Guild.getInstance().isGuildNull(uuid)) {
            String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
            prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + " [" + clanTag + ChatColor.RESET + "]"));
        }

        if (gChat) {
            if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, event.getPlayer().getUniqueId())) {
                event.setFormat(prefix.toString().trim() + " " + event.getPlayer().getName() + ChatColor.GRAY + ": " + event.getMessage());
            }
        } else {
            event.setCancelled(true);
            API.getNearbyPlayers(event.getPlayer().getLocation(), 100).stream().forEach(player -> player.sendMessage(prefix.toString().trim() + " " + event.getPlayer().getName() + ChatColor.GRAY + ": " + event.getMessage()));
        }
    }

}
