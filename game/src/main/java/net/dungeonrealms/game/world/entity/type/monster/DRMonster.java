package net.dungeonrealms.game.world.entity.type.monster;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.AuraType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonus;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemLootAura;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollItem;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueScrollType;
import net.dungeonrealms.game.item.items.functional.cluescrolls.ClueUtils;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.DropRate;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public interface DRMonster {

    public static final TeleportLocation[][] TELEPORT_DROPS = new TeleportLocation[][]{
            {TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD},
            {TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.TRIPOLI},
            {TeleportLocation.CYRENNICA, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.GLOOMY_HOLLOWS, TeleportLocation.CRESTGUARD},
            {TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS},
            {TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS}};

    public static final Class<?>[] untargettable = new Class[]{EntityZombie.class, EntitySkeleton.class, EntitySilverfish.class, EntitySpider.class, EntityCaveSpider.class, DRMonster.class};

    //This is for attribute calculations.

    default void onMonsterAttack(Player p) {

    }

    default void onMonsterDeath(Player killer) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> checkItemDrop(killer));
    }

    default void setupMonster(int tier) {
        setTier(tier);
        setGear();
        setSkullTexture();

        if (getEnum() != null) {
            String displayName = getEnum().getPrefix() + getEnum().getName() + getEnum().getSuffix();
            Metadata.CUSTOM_NAME.set(getBukkit(), displayName);
            getBukkit().setCustomName(displayName);
            getBukkit().setCustomNameVisible(true);
        }

        //  SET NMS DATA  //
        setupNMS();
    }

    default void setupNMS() {
//    	getNMS().getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        getNMS().getAttributeInstance(GenericAttributes.c).setValue(.75D);
        getNMS().noDamageTicks = 0;
        getNMS().maxNoDamageTicks = 0;
    }

    default void setGear() {
        ItemStack[] armor = GameAPI.getTierArmor(getTier());
        Random random = ThreadLocalRandom.current();
        boolean forcePlace = false;
        EntityEquipment e = getBukkit().getEquipment();

        int chance = 6 + getTier();

        ItemStack[] entityArmor = e.getArmorContents();
        for (int i = 0; i <= 2; i++) { //Chestplate, boots, leggings. No helmet.
            if (forcePlace || getTier() >= 3 || random.nextInt(10) <= chance) {
                entityArmor[i] = armor[i];

                if (i == 1) //Reset force place for low tiers at leggings.
                    forcePlace = false;
            } else {
                forcePlace = true;
            }
        }
        e.setArmorContents(entityArmor);
        e.setItemInMainHand(getWeapon());
    }

    default void setMonster(EnumMonster m) {
    }

    EnumMonster getEnum();

    default EntityLiving getNMS() {
        return (EntityLiving) this;
    }

    default AttributeList getAttributes() {
        return EntityAPI.getEntityAttributes().get(this);
    }

    default LivingEntity getBukkit() {
        return (LivingEntity) ((net.minecraft.server.v1_9_R2.Entity) this).getBukkitEntity();
    }

    default ItemStack getWeapon() {
        return makeItem(new ItemWeaponMelee());
    }

    default ItemStack makeItem(ItemGear gear) {
        return gear.setTier(ItemTier.getByTier(getTier())).generateItem();
    }

    default void setSkullTexture() {
        if (getEnum() != null && getEnum().getSkullItem() != null) {
            //No helms, they dont render.
            if (getBukkit().getType() == EntityType.IRON_GOLEM || getBukkit().getType() == EntityType.BLAZE || getBukkit().getType() == EntityType.WITCH)
                return;
            ItemStack helmet = getEnum().getSkullItem();
            getBukkit().getEquipment().setHelmet(helmet);
            getNMS().setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
        }
    }

    default void setTier(int tier) {
        Metadata.TIER.set(getBukkit(), tier);
    }

    default int getTier() {
        return Metadata.TIER.get(getBukkit()).asInt();
    }

    default void checkItemDrop(Player killer) {
        if (killer != null) ClueUtils.handleMobKilled(killer, this);
        LivingEntity ent = getBukkit();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(killer);

        //No normal drops in dungeons.
        if (DungeonManager.isDungeon(ent))
            return;

        //Boss will handle this.
        if (Metadata.BOSS.has(ent) || (Metadata.RIFT_MOB.has(ent) && Metadata.ELITE.has(ent)))
            return;

        //combat log npcs have special drop mechanics
        if (EnumEntityType.COMBATLOG_NPC.isType(ent))
            return;

        int tier = getTier();
        Random random = ThreadLocalRandom.current();
        PlayerWrapper pw = PlayerWrapper.getWrapper(killer);

        ModifierRange gemFinder = pw.getAttributes().getAttribute(ArmorAttributeType.GEM_FIND);
        double gemFind = (gemFinder.getValHigh() / 100) + 1;
        int killerItemFind = pw.getAttributes().getAttribute(ArmorAttributeType.ITEM_FIND).getValHigh();

        Location loc = ent.getLocation();
        World world = ((CraftWorld) loc.getWorld()).getHandle();

        int gemRoll = random.nextInt(100);
        DropRate dr = DropRate.getRate(tier);
        int gemChance = dr.getMobGemChance();
        boolean elite = Metadata.ELITE.has(ent);
        double chance = elite ? dr.getEliteDropChance() : dr.getNormalDropChance();


        // If it's a named elite, bring the drop chances down.
        if (Metadata.NAMED_ELITE.has(ent))
            chance /= 3;

        if (DonationEffects.getInstance().hasBuff(EnumBuff.LOOT))
            chance += chance * (DonationEffects.getInstance().getBuff(EnumBuff.LOOT).getBonusAmount() / 100f);

        int clueRoll = random.nextInt(10000);
        boolean isClueDrop = clueRoll <= (tier == 5 ? 10 : tier == 4 ? 8 : tier == 3 ? 5 : tier == 2 ? 3 : 1);
        if (isClueDrop) {
            ClueScrollItem clue = new ClueScrollItem(ClueScrollType.COMBAT);
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), clue.generateItem()));
        }


        if (killer != null) {
            Party party = Affair.getParty(killer);
            if (party != null) {
                int nearby = party.getNearbyMembers(killer, 50).size() + 1;
                if (nearby >= 4 && nearby < 8) {
                    chance += chance * 0.005;
                }

                if (nearby >= 8) {
                    chance += chance * 0.01;
                }
            }
        }

        if (gemRoll < (gemChance * gemFind)) {
            if (gemRoll >= gemChance)
                wrapper.sendDebug(ChatColor.GREEN + "Your " + gemFinder.getValHigh() + "% Gem Find has resulted in a drop.");

            double gemsDropped = Utils.randInt(dr.getGemDropMin(), dr.getGemDropMax());
            gemsDropped *= gemFind;
            if (elite)
                gemsDropped *= 1.5;

            while (gemsDropped > 0) {
                int drop = Math.min((int) gemsDropped, 64);
                gemsDropped -= drop;
                if (drop <= 0) break;
                ItemGem gem = new ItemGem(drop);
                ItemStack gemStack = gem.generateItem();
                ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), gemStack));
            }
        }

        double dropRoll = ThreadLocalRandom.current().nextDouble(1000D);

        List<ItemStack> toDrop = new ArrayList<>();
        for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents())
            if (stack != null && stack.getType() != Material.AIR && stack.getType() != Material.SKULL && stack.getType() != Material.SKULL_ITEM)
                toDrop.add(stack);

        if (!elite)
            toDrop.add(new ItemArmor(ItemType.HELMET).setTier(ItemTier.getByTier(getTier())).generateItem());

        double mult = ItemLootAura.getDropMultiplier(ent.getLocation(), AuraType.LOOT);
        if (mult > 0)
            chance += chance * mult * .01;

        ItemTier t = ItemTier.getByTier(getTier());
        Integer dry = pw.getDryLoot().get(t);

        double itemFindIncrease = (chance * killerItemFind / 100);

        if (dry != null && dry > 300 && !elite && !Metadata.NAMED_ELITE.has(ent)) {
            //Garuntee a drop?
            //1000 dry = 50% increase.
            int increase = Math.min(((dry - 150) / 20) * 10, 500);

            chance = Math.max(chance, increase + chance);

            System.out.println("Adding " + increase + " to drop for " + pw.getUsername());
        }

        //Random drop choice, as opposed dropping in the same order (boots>legs>chest>head)
        Collections.shuffle(toDrop);
        if (dropRoll < chance + itemFindIncrease) {
            if (dropRoll >= chance)
                wrapper.sendDebug(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");

            ItemStack drop = toDrop.get(random.nextInt(toDrop.size()));
            if (ThreadLocalRandom.current().nextInt(2) == 0) { // 50% chance for weapon, 50% for armor
                ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInMainHand();
                if (weapon != null && weapon.getType() != Material.AIR)
                    drop = weapon;
            }

            //  DROP ITEM  //
            if (drop != null && drop.getType() != Material.AIR) {
                PersistentItem persis = PersistentItem.constructItem(drop);
                if (persis instanceof ItemGear) {
                    ItemGear gear = (ItemGear) PersistentItem.constructItem(drop);
                    gear.setGlowing(false);
                    gear.damageItem(null, Utils.randInt(0, ItemGear.MAX_DURABILITY - (int) (ItemGear.MAX_DURABILITY / 7.5)));
                    //drop the gear peice.
                    ItemManager.whitelistItemDrop(killer, loc, gear.generateItem());
                }
            }
            pw.getDryLoot().put(t, 0);
        } else {
            //No drop..
            pw.getDryLoot().put(t, dry == null ? 1 : dry + 1);
        }

        // Drop teleport book.
        int bookChance = SetBonus.hasSetBonus(killer, SetBonuses.LIBRARIAN) ? (int) (dr.getTeleportBookChance() * .5D) : 0;
        int rand = random.nextInt(100);
        if (dr.getTeleportBookChance() + bookChance >= rand) {
            if (rand > dr.getTeleportBookChance()) {
                //Wasnt going to get it.. bonus came in clutch
                ent.getLocation().getWorld().playSound(ent.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.4F);
            }
            ItemManager.whitelistItemDrop(killer, ent.getLocation(), new ItemTeleportBook(
                    TELEPORT_DROPS[tier - 1][random.nextInt(TELEPORT_DROPS[tier - 1].length)]).generateItem());
        }
    }

    default ItemStack getHeld() {
        return getBukkit().getEquipment().getItemInMainHand();
    }

    default void calculateAttributes() {
        EntityAPI.calculateAttributes(this);
    }

    default int getHP() {
        return HealthHandler.getHP(getBukkit());
    }

    default void setHP(int hp) {
        HealthHandler.setHP(getBukkit(), hp);
    }

    default int getMaxHP() {
        return HealthHandler.getMaxHP(getBukkit());
    }

    default void setMaxHP(int max) {
        HealthHandler.setMaxHP(getBukkit(), max);
    }

    default double getPercentHP() {
        return HealthHandler.getHPPercent(getBukkit());
    }

}
