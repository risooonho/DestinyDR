package net.dungeonrealms.game.world.teleportation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenRealm;
import net.dungeonrealms.game.quests.objectives.ObjectiveUseHearthStone;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Teleportation implements GenericMechanic {

    private static Teleportation instance = null;

    public static Teleportation getInstance() {
        if (instance == null) {
            return new Teleportation();
        }
        return instance;
    }

    public static HashMap<UUID, Integer> PLAYER_TELEPORT_COOLDOWNS = new HashMap<>();
    public static HashMap<UUID, Location> PLAYERS_TELEPORTING = new HashMap<>();

    public static Location Underworld;
    public static Location Overworld;
    //teleport_overworld
    //teleport_underworld

    public enum EnumTeleportType {
        HEARTHSTONE(0, "Hearthstone"),
        TELEPORT_BOOK(1, "Teleport Book");

        private int id;
        private String name;

        EnumTeleportType(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
	public void startInitialization() {
        Underworld = new Location(Bukkit.getWorlds().get(0), -362, 172, -3440, -90F, 1F);
        Overworld = new Location(Bukkit.getWorlds().get(0), -1158, 96, -515, 91F, 1F);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Integer> e : PLAYER_TELEPORT_COOLDOWNS.entrySet()) {
                /*if (e.getValue() == 0) {
                    Player player = Bukkit.getPlayer(e.getKey());
                    if (!player.hasMetadata("hearthstoneReady")) {
                        player.sendMessage(ChatColor.RED + "Your Hearthstone is ready.");
                        player.setMetadata("hearthstoneReady", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    }
                    continue;
                }*/
                TeleportAPI.addPlayerHearthstoneCD(e.getKey(), (e.getValue() - 1));
            }
        }, 20L, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Teleports a player to a location.
     *
     * @param uuid
     * @param teleportType
     * @param nbt
     * @since 1.0
     */
    public void teleportPlayer(UUID uuid, EnumTeleportType teleportType, TeleportLocation location) {
        Player player = Bukkit.getPlayer(uuid);
        if (!(player.getWorld().equals(Bukkit.getWorlds().get(0)))) {
            if (teleportType == EnumTeleportType.HEARTHSTONE) {
                TeleportAPI.addPlayerHearthstoneCD(uuid, 280);
            }
            return;
        }
        TeleportAPI.addPlayerCurrentlyTeleporting(uuid, player.getLocation());
        
        if (teleportType == EnumTeleportType.HEARTHSTONE)
        	location = TeleportLocation.valueOf(TeleportAPI.getLocationFromDatabase(uuid).toUpperCase());

        assert location != null;

        String message = ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING" +  " - " + ChatColor.AQUA + location.getDisplayName();

        player.sendMessage(message);

        ParticleAPI.ParticleEffect[] particleEffect = new ParticleAPI.ParticleEffect[2];
        final int[] taskTimer = {7};
        switch (teleportType) {
            case HEARTHSTONE:
                particleEffect[0] = ParticleAPI.ParticleEffect.SPELL;
                particleEffect[1] = ParticleAPI.ParticleEffect.SPELL;
                taskTimer[0] = 10;
                break;
            case TELEPORT_BOOK:
                particleEffect[0] = ParticleAPI.ParticleEffect.WITCH_MAGIC;
                particleEffect[1] = ParticleAPI.ParticleEffect.PORTAL;
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 220, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 220, 1));
                player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1F, 1F);
                taskTimer[0] = 5;
                break;
        }

        Location startingLocation = player.getLocation();
        final boolean[] hasCancelled = {false};
        final TeleportLocation teleportTo = location;
        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId()) && !hasCancelled[0]) {
                if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    if (player.getLocation().distanceSquared(startingLocation) <= 4 && !CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.WHITE.toString() + ChatColor.BOLD + "TELEPORTING " + ChatColor.RESET + "... " + taskTimer[0] + "s");
                        try {
                            ParticleAPI.sendParticleToLocation(particleEffect[0], player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 250);
                            ParticleAPI.sendParticleToLocation(particleEffect[1], player.getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 4F, 400);
                        } catch (Exception e) {
                            Utils.log.info("[TELEPORT] Tried to send particle to player and failed. Continuing");
                        }
                        if (taskTimer[0] <= 0) {
                            if (CombatLog.isInCombat(player)) {
                                player.sendMessage(ChatColor.RED + "Your teleport has been interrupted by combat!");
                                if (teleportType == EnumTeleportType.HEARTHSTONE) {
                                    TeleportAPI.addPlayerHearthstoneCD(uuid, 280);
                                }
                            } else {
                                player.teleport(teleportTo.getLocation());
                                if (teleportType == EnumTeleportType.HEARTHSTONE)
                                    TeleportAPI.addPlayerHearthstoneCD(uuid, 280);
                            }
                            TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
                        }
                        taskTimer[0]--;
                    } else {
                        hasCancelled[0] = true;
                        if (teleportType == EnumTeleportType.TELEPORT_BOOK) {
                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                            player.removePotionEffect(PotionEffectType.CONFUSION);
                        }
                        player.sendMessage(ChatColor.RED + "Your teleport was canceled!");
                        if (teleportType == EnumTeleportType.HEARTHSTONE) {
                            TeleportAPI.addPlayerHearthstoneCD(uuid, 300);
                        }
                    }
                }
            }
        }, 0, 20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getScheduler().cancelTask(taskID);
            TeleportAPI.removePlayerCurrentlyTeleporting(uuid);
            Quests.getInstance().triggerObjective(player, ObjectiveUseHearthStone.class);
        }, (taskTimer[0] * 20L) + 10L);
    }
}
