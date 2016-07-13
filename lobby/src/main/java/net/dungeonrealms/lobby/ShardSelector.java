package net.dungeonrealms.lobby;


import net.dungeonrealms.game.AbstractMenu;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.menu.GUIButtonClickEvent;
import net.dungeonrealms.game.menu.item.GUIButton;
import net.dungeonrealms.network.ShardInfo;
import net.dungeonrealms.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/18/2016
 */

public class ShardSelector extends AbstractMenu {

    public ShardSelector(Player player) {
        super(Lobby.getInstance(), "DungeonRealms Shards", AbstractMenu.round(getFilteredServers().size()), player.getUniqueId());
        setDestroyOnExit(true);

        List<BungeeServerInfo> servers = new ArrayList<>(getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            return o1num - o2num;
        });

        // DISPLAY AVAILABLE SHARDS //
        for (BungeeServerInfo info : servers) {
            String bungeeName = info.getServerName();
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();

            // Do not show YT / CS shards unless they've got the appropriate permission to see them.
            if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player)) || (shardID.equalsIgnoreCase("US-0") && !Rank.isGM(player)))
                continue;

            GUIButton button = new GUIButton(getShardItem(shardID)) {

                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    Player player = event.getWhoClicked();
                    player.closeInventory();


                    if (shardID.contains("SUB") && !Rank.isSubscriber(player)) {
                        player.sendMessage(new String[]{
                                ChatColor.RED + "This is a " + ChatColor.BOLD + ChatColor.UNDERLINE + "SUBSCRIBER ONLY" + ChatColor.RED + " shard!",
                                ChatColor.RED + "You can subscribe at: " + ChatColor.UNDERLINE + "http://www.dungeonrealms.net/shop"
                        });
                        return;
                    } else if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player))) {
                        player.sendMessage(ChatColor.RED + "You are " + ChatColor.BOLD + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " authorized to connect to this shard.");
                        return;
                    }

                    BungeeUtils.sendToServer(player.getName(), info.getServerName());
                }
            };

            List<String> lore = new ArrayList<>();

            if (!getServerType(shardID).equals(""))
                lore.add(ChatColor.RED.toString() + ChatColor.ITALIC + getServerType(shardID));


            //final int slot = getServerType(shardID).equals("") ? getSize() : Math.min(getInventorySize(), getInventorySize() - 1) - (getSize() - getNormalServers());

            lore.add(ChatColor.GREEN + "This shard is online!");
            lore.add(ChatColor.WHITE + "Click here to load your");
            lore.add(ChatColor.WHITE + "character onto this shard.");
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Online: " + info.getOnlinePlayers() + "/" + info.getMaxPlayers());

            button.setDisplayName(getShardColour(shardID) + ChatColor.BOLD.toString() + shardID);
            button.setLore(lore);

            set(getSize(), button);
        }
    }


    private int getNormalServers() {
        int count = 0;

        for (String bungeeName : getFilteredServers().keySet()) {
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();
            if (getServerType(shardID).equals(""))
                count++;
        }

        return count;
    }

    /**
     * Returns the material associated with a shard.
     *
     * @param shardID
     * @return Material
     */
    private ItemStack getShardItem(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return new ItemStack(Material.DIAMOND);
        else if (shardID.startsWith("CS-")) return new ItemStack(Material.PRISMARINE_SHARD);
        else if (shardID.startsWith("YT-")) return new ItemStack(Material.GOLD_NUGGET);
        else if (shardID.startsWith("BR-")) return new ItemStack(Material.SAPLING, 1, (byte) 3);
        else if (shardID.startsWith("SUB-")) return new ItemStack(Material.EMERALD);

        return new ItemStack(Material.END_CRYSTAL);
    }

    /**
     * Returns the chat colour associated with a shard.
     *
     * @param shardID
     * @return ChatColor
     */
    private ChatColor getShardColour(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return ChatColor.AQUA;
        else if (shardID.startsWith("CS-")) return ChatColor.BLUE;
        else if (shardID.startsWith("YT-")) return ChatColor.RED;
        else if (shardID.startsWith("SUB-")) return ChatColor.GREEN;

        return ChatColor.YELLOW;
    }

    private static Map<String, BungeeServerInfo> getFilteredServers() {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Map.Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {
            String bungeeName = e.getKey();
            if (ShardInfo.getByPseudoName(bungeeName) == null) continue;
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || info.getOnlinePlayers() >= info.getMaxPlayers() || info.getMotd1().equals("offline"))
                continue;

            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }

    public String getServerType(String shardID) {
        if (shardID.contains("SUB")) return "Subscribers Only";
        if (shardID.contains("YT")) return "YouTubers Only";
        if (shardID.contains("BR")) return "Brazilian Shard";
        if (shardID.contains("RP")) return "Role-playing Shard";
        if (shardID.contains("CS")) return "Support Agents Only";
        return "";
    }

    @Override
    public void open(Player player) {
        if (getSize() == 0) {
            player.sendMessage(ChatColor.RED + "Unable to find an available shard for you.");
            return;
        }

        player.openInventory(inventory);
    }

}