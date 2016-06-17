package net.dungeonrealms.game.mongo;

/**
 * Created by Nick on 8/29/2015.
 */
public enum EnumData {

    USERNAME("info.username"),
    HEALTH("info.health"),
    FIRST_LOGIN("info.firstLogin"),
    LAST_LOGIN("info.lastLogin"),
    LAST_LOGOUT("info.lastLogout"),
    LEVEL("info.netLevel"),
    IS_PLAYING("info.isPlaying"),
    EXPERIENCE("info.experience"),
    GEMS("info.gems"),
    HEARTHSTONE("info.hearthstone"),
    ECASH("info.ecash"),
    FRIENDS("info.friends"),
    ALIGNMENT("info.alignment"),
    ALIGNMENT_TIME("info.alignmentTime"),
    CURRENT_LOCATION("info.currentLocation"),
    HASSHOP("info.shopOpen"),
    SHOPLEVEL("info.shopLevel"),
    MULELEVEL("info.muleLevel"),
    CURRENT_FOOD("info.foodLevel"),
    LOGGERDIED("info.loggerdied"),
    ENTERINGREALM("info.enteringrealm"),
    CURRENTSERVER("info.current"),
    ACTIVE_MOUNT("info.activemount"),
    ACTIVE_PET("info.activepet"),
    ACTIVE_TRAIL("info.activetrail"),
    ACTIVE_MOUNT_SKIN("info.activemountskin"),

    ACHIEVEMENTS("collectibles.achievements"),

    RANK("rank.rank"),
    RANK_SUB_EXPIRATION("rank.expiration_date"),
    PURCHASE_HISTORY("rank.purchaseHistory"),

    INVENTORY_COLLECTION_BIN("inventory.collection_bin"),
    INVENTORY_MULE("inventory.mule"),
    INVENTORY_STORAGE("inventory.storage"),
    INVENTORY("inventory.player"),
    INVENTORY_LEVEL("inventory.level"),
    ARMOR("inventory.armor"),

    GUILD("info.guild"),

    GUILD_INVITATION("notices.guildInvitation"),
    FRIEND_REQUSTS("notices.friendRequest"),

    MOUNTS("collectibles.mounts"),
    PETS("collectibles.pets"),
    PARTICLES("collectibles.particles"),
    MOUNT_SKINS("collectibles.mountskins"),

    TOGGLE_DEBUG("toggles.debug"),
    TOGGLE_TRADE("toggles.trade"),
    TOGGLE_TRADE_CHAT("toggles.tradeChat"),
    TOGGLE_GLOBAL_CHAT("toggles.globalChat"),
    TOGGLE_RECEIVE_MESSAGE("toggles.receiveMessage"),
    TOGGLE_PVP("toggles.pvp"),
    TOGGLE_DUEL("toggles.duel"),
    TOGGLE_CHAOTIC_PREVENTION("toggles.chaoticPrevention"),

    PORTAL_SHARDS_T1("portalKeyShards.tier1"),
    PORTAL_SHARDS_T2("portalKeyShards.tier2"),
    PORTAL_SHARDS_T3("portalKeyShards.tier3"),
    PORTAL_SHARDS_T4("portalKeyShards.tier4"),
    PORTAL_SHARDS_T5("portalKeyShards.tier5"),


    MAILBOX("notices.mailbox"),


    /*
    Player Attributes
     */
    //Adds Armor, Block Chance, Axe Damage and Polearm Damage
    STRENGTH("attributes.strength"),
    //Add DPS%, Dodge Chance, Armor Penetration and Bow Damage
    DEXTERITY("attributes.dexterity"),
    //Adds Energy Regeneration, elemental damage, critical hit chance and staff damamge.
    INTELLECT("attributes.intellect"),
    //Adds Health, hp regen, elemental resistance, and sword damage.
    VITALITY("attributes.vitality"),

    RESETS("attributes.resets"),
    
    FREERESETS("attributes.freeresets"),
    
    BUFFER_POINTS("attributes.bufferPoints");

	
	private String key;
	EnumData(String key){
		this.key = key;
	}
	
	public String getKey(){
		return key;
	}

}
