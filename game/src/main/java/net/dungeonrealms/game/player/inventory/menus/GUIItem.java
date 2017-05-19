package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.function.Consumer;

public class GUIItem extends ShopItem {

    @Getter
    private Consumer<InventoryClickEvent> clickCallback;

    public GUIItem(ItemStack item) {
        super(item);
        getItem().getItemMeta().addItemFlags(ItemFlag.values());
    }

    public GUIItem(Material material) {
        this(material, (short) 0);
    }

    public GUIItem(Material mat, short s) {
        this(new ItemStack(mat, 1, s));
    }

    @Override
    public ItemStack getStack() {
        return getItem();
    }

    public GUIItem setEnchanted(boolean enchant) {

        if (enchant)
            getItem().addUnsafeEnchantment(EnchantmentAPI.getGlowEnchant(), 1);
        else if (getItem().containsEnchantment(EnchantmentAPI.getGlowEnchant()))
            getItem().removeEnchantment(EnchantmentAPI.getGlowEnchant());

        return this;
    }

    @Override
    public void loadItem() {
        super.loadItem();
        if (getItem().hasItemMeta() && getItem().getItemMeta().hasLore())
            this.lore = getItem().getItemMeta().getLore();
    }

    public GUIItem setClick(Consumer<InventoryClickEvent> event) {
        this.clickCallback = event;
        return this;
    }

    public GUIItem setECashCost(int ecash) {
        this.setECash(ecash);
        ItemMeta im = getItem().getItemMeta();
        List<String> lore = im.getLore() == null ? Lists.newArrayList() : im.getLore();
        lore.add(ChatColor.WHITE.toString() + ecash + ChatColor.GREEN + " E-Cash");
        im.setLore(lore);
        getItem().setItemMeta(im);
        return this;
    }

    public GUIItem setSkullOwner(String name) {
        ItemMeta im = getItem().getItemMeta();
        if (!(im instanceof SkullMeta)) return this;
        SkullMeta sm = (SkullMeta) im;
        sm.setOwner(name);
        getItem().setItemMeta(sm);
        return this;
    }

    public GUIItem setDurability(short data) {
        getItem().setDurability(data);
        return this;
    }

    public GUIItem setName(String name) {
        ItemMeta im = getItem().getItemMeta();
        im.setDisplayName(name);
        getItem().setItemMeta(im);
        return this;
    }

    public GUIItem setLore(String... strings) {
        return setLore(Lists.newArrayList(strings));
    }

    public GUIItem setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }
}
