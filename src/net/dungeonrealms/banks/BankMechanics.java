/**
 *
 */
package net.dungeonrealms.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics {

	public static ItemStack gem;
	public static ItemStack banknote;
	public static HashMap<UUID, Storage> storage = new HashMap<>();

	public static void init() {
		loadCurrency();
	}

	public static void handleLogout(UUID uuid) {
		if (storage.containsKey(uuid)) {
			Inventory inv = storage.get(uuid).inv;
			if (inv != null) {
			ItemStack[] listItems = inv.getContents();
			ArrayList<ItemStack> list = new ArrayList<>();
			for (int i = 0; i < listItems.length; i++) {
				ItemStack item = listItems[i];
				if(item != null)
				list.add(item);
			}
			try {
				String serializedInv = ItemSerialization.serialize(list);
				storage.remove(uuid);
				DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "inventory.storage", serializedInv);
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		}
		try {
			PlayerInventory inv = Bukkit.getPlayer(uuid).getInventory();
			ItemStack[] listItems = inv.getContents();
			ArrayList<ItemStack> list = new ArrayList<>();
			for (int i = 0; i < listItems.length; i++) {
			ItemStack item = listItems[i];
			if (item != null && item.getType() != Material.AIR)
				list.add(item);
			}
			if (!list.isEmpty())
			DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, "inventory.player",
					ItemSerialization.serialize(list));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void handleLogin(UUID uuid) {
		Bukkit.broadcastMessage("player logged in");
		String playerInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY, uuid);
		if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
			try {
			List<ItemStack> items = ItemSerialization.deserialize(playerInv);
			items.stream().filter(item -> item != null && item.getType() != Material.AIR)
					.forEach(item -> Bukkit.getPlayer(uuid).getInventory().addItem(item));
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
		String source = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_STORAGE, uuid);
		if (source != null && source.length() > 0) {
			List<ItemStack> items;
			try {
			items = ItemSerialization.deserialize(source);
			Storage storageTemp = new Storage(uuid, items);
			storage.put(uuid, storageTemp);
			} catch (IOException e) {
			e.printStackTrace();
			}
		} else {
			Storage storageTemp = new Storage(uuid);
			storage.put(uuid, storageTemp);
		}
	}

	/**
	 * @return
	 */
	private static void loadCurrency() {
		ItemStack item = new ItemStack(Material.EMERALD, 1);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add("The currency of Andalucia");
		meta.setLore(lore);
		meta.setDisplayName("Gem");
		item.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
		tag.setString("type", "money");
		nms.setTag(tag);
		gem = CraftItemStack.asBukkitCopy(nms);

		ItemStack item2 = new ItemStack(Material.PAPER, 1);
		ItemMeta meta2 = item2.getItemMeta();
		List<String> lore2 = new ArrayList<>();
		lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
		meta2.setLore(lore2);
		meta2.setDisplayName(ChatColor.GREEN.toString() + "Bank Note");
		item2.setItemMeta(meta2);
		net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
		NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
		tag2.setString("type", "money");
		tag2.setInt("worth", 0);
		nms2.setTag(tag2);
		banknote = CraftItemStack.asBukkitCopy(nms2);
	}

	public static ItemStack createBankNote(int amount) {
		ItemStack item2 = new ItemStack(Material.PAPER, 1);
		ItemMeta meta2 = item2.getItemMeta();
		List<String> lore2 = new ArrayList<>();
		lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
		meta2.setLore(lore2);
		meta2.setDisplayName(ChatColor.GREEN.toString() + "Bank Note");
		item2.setItemMeta(meta2);
		net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
		NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
		tag2.setString("type", "money");
		tag2.setInt("worth", amount);
		nms2.setTag(tag2);
		return CraftItemStack.asBukkitCopy(nms2);
	}

	/**
	 * @param uuid
	 * @param num
	 */
	public static void addGemsToPlayer(UUID uuid, int num) {
		DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, "info.gems", num);
	}

	/**
	 * @param uniqueId
	 */
	public static Storage getStorage(UUID uniqueId) {
		return storage.get(uniqueId);
	}

}
