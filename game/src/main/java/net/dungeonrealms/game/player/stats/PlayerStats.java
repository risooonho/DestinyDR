package net.dungeonrealms.game.player.stats;

import lombok.SneakyThrows;
import net.dungeonrealms.database.LoadableData;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.SaveableData;
import net.dungeonrealms.game.mastery.Stats;
import net.dungeonrealms.game.player.inventory.menus.guis.StatGUI;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redone by Kneesnap in early 2017.
 */
public class PlayerStats implements LoadableData, SaveableData {
    private UUID playerUUID;
    public int freeResets;
    public int resetAmounts;
    public boolean reset = true;
    private Map<Stats, Integer> statMap = new HashMap<>();
    private Map<Stats, Integer> tempStatMap = new HashMap<>();
    private int characterID;

    public final static int POINTS_PER_LEVEL = 3;

    public PlayerStats(UUID playerUUID, int characterID) {
        this.playerUUID = playerUUID;
        this.characterID = characterID;

    }


    public void setTempStat(Stats s, int val) {
        tempStatMap.put(s, val);
    }

    public int getTempStat(Stats s) {
        Integer stored = tempStatMap.get(s);
        if (stored != null) return stored;
        return 0;
    }

    public void setStat(Stats s, int val) {
        statMap.put(s, val);
    }

    public int getStat(Stats s) {
        return statMap.get(s);
    }

    public int getFreePoints() {
        int usedPoints = 0;
        for (Stats s : Stats.values())
            usedPoints += getTempStat(s) + getStat(s);
        return (POINTS_PER_LEVEL * (getLevel() + 2)) - usedPoints;
    }

    public void openMenu(Player player) {
        new StatGUI(player).open(player, null);
    }

    public void allocatePoint(Stats s) {
        if (getFreePoints() > 0)
            setTempStat(s, getTempStat(s) + 1);
    }


    public void removePoint(Stats s) {
        int val = getTempStat(s);
        if (val > 0)
            setTempStat(s, val - 1);
    }

    public double getHPPercent(boolean temp) {
        return ((temp ? getTempStat(Stats.VITALITY) : getStat(Stats.VITALITY)) * 0.034);
    }

    public double getSwordDMG(boolean temp) {
        return ((temp ? getTempStat(Stats.VITALITY) : getStat(Stats.VITALITY)) * 0.03);
    }

    public double getBlock(boolean temp) {
        return ((temp ? getTempStat(Stats.STRENGTH) : getStat(Stats.STRENGTH)) * 0.017);
    }

    public double getAxeDMG(boolean temp) {
        return ((temp ? getTempStat(Stats.STRENGTH) : getStat(Stats.STRENGTH)) * 0.03);
    }

    public double getPolearmDMG(boolean temp) {
        return ((temp ? getTempStat(Stats.STRENGTH) : getStat(Stats.STRENGTH)) * 0.02);
    }

    public double getBowDMG(boolean temp) {
        return ((temp ? getTempStat(Stats.DEXTERITY) : getStat(Stats.DEXTERITY)) * 0.015);
    }

    public double getDPS(boolean temp) {
        return ((temp ? getTempStat(Stats.DEXTERITY) : getStat(Stats.DEXTERITY)) * 0.03);
    }

     public double getStaffDMG(boolean temp) {
        return ((temp ? getTempStat(Stats.INTELLECT) : getStat(Stats.INTELLECT)) * 0.02);
    }

    public double getEnergyRegen(boolean temp) {
        return ((temp ? getTempStat(Stats.INTELLECT) : getStat(Stats.INTELLECT)) * 0.015);
    }

    public double getArmor(boolean temp) {
        return ((temp ? getTempStat(Stats.STRENGTH) : getStat(Stats.STRENGTH)) * 0.03);
    }

//     public double getArmorPen(boolean temp) {
//         return ((temp ? getTempStat(Stats.DEXTERITY) : getStat(Stats.DEXTERITY)) * 0.009);
//     }
    
    public double getVSMonsters (boolean temp) {
        return ((temp ? getTempStat(Stats.DEXTERITY) : getStat(Stats.DEXTERITY)) * 0.02);
    }

    public double getDodge(boolean temp) {
        return ((temp ? getTempStat(Stats.DEXTERITY) : getStat(Stats.DEXTERITY)) * 0.017);
    }


    public double getCriticalDamage(boolean temp) {
        return (temp ? getTempStat(Stats.INTELLECT) : getStat(Stats.INTELLECT)) * 0.001;
    }

    public double getElementalResist(boolean temp) {
        return (temp ? getTempStat(Stats.VITALITY) : getStat(Stats.VITALITY)) * 0.04;
    }
    public double getElementalDamage(boolean temp) {
        return (temp ? getTempStat(Stats.INTELLECT) : getStat(Stats.INTELLECT)) * 0.05;
    }

    public double getHealthRegen(boolean temp) {
        return (temp ? getTempStat(Stats.VITALITY) : getStat(Stats.VITALITY)) * 0.3;
    }

    public void lvlUp() {
        //int lvl = getLevel() + 1;
        /*if (lvl == 10 || lvl == 50)
            addReset();*/
//        setPlayerLevel(lvl);
    }

    /**
     * Resets temp stats
     */
    public void resetTemp() {
        for (Stats s : Stats.values())
            setTempStat(s, 0);
    }

    public boolean confirmStats() {
        boolean valid = false;
        for (Stats s : Stats.values()) {
            int tempStats = getTempStat(s);
            if (tempStats > 0) {
                setStat(s, getStat(s) + tempStats);
                setTempStat(s, 0);
                valid = true;
            }
        }
        PlayerWrapper.getPlayerWrapper(playerUUID).calculateAllAttributes();
        return valid;
    }

    /**
     * Resets the player stats.
     */
    public void unallocateAllPoints() {
        resetTemp();
        for(Stats stat : Stats.values())
            setStat(stat, 0);
        PlayerWrapper.getPlayerWrapper(playerUUID).calculateAllAttributes();
    }

    public int getLevel() {
        return PlayerWrapper.getPlayerWrapper(playerUUID).getLevel();
    }

    public void addReset() {
        resetAmounts++;
    }

    @Override
    @SneakyThrows
    public void extractData(ResultSet resultSet) {
        for (Stats s : Stats.values())
            setStat(s, resultSet.getInt(s.getDBField()));
        this.freeResets = resultSet.getInt("attributes.freeResets");
        this.resetAmounts = resultSet.getInt("attributes.resets_available");
    }

    @Override
    public String getUpdateStatement() {
        String sql = "UPDATE attributes SET ";
        for (Stats s : Stats.values())
            sql += s.name().toLowerCase() + " = '" + getStat(s) + "', ";

        sql += "resets_available = '%s', freeResets = '%s' WHERE character_id = '%s';";
        return String.format(sql, resetAmounts, freeResets, this.characterID);
    }

    public double getEnergyRegen() {
        return 1 + (getStat(Stats.INTELLECT) * 0.00015);
    }

    public double getRegen() {
        return getHPRegen();
    }

    public double getHPRegen() {
        return getStat(Stats.VITALITY) * 0.03;
    }

    public double getDPS() {
        return getStat(Stats.DEXTERITY) * 0.03;
    }
}
