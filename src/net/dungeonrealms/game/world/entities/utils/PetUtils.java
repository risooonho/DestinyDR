package net.dungeonrealms.game.world.entities.utils;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.pets.*;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class PetUtils implements GenericMechanic{

    private static PetUtils instance = null;

    public static PetUtils getInstance() {
        if (instance == null) {
            return new PetUtils();
        }
        return instance;
    }

    private static Field gsa;
    private static Field goalSelector;
    private static Field targetSelector;

    static {
        try {
            gsa = PathfinderGoalSelector.class.getDeclaredField("b");
            gsa.setAccessible(true);
            goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);
            targetSelector = EntityInsentient.class.getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
	public void startInitialization() {
        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Entity> entry : Entities.PLAYER_PETS.entrySet()) {
                Entity pet = entry.getValue();
                Player player = Bukkit.getPlayer(entry.getKey());
                org.bukkit.World world = pet.getBukkitEntity().getWorld();
                Location location = new Location(world, pet.lastX, pet.lastY, pet.lastZ);
                if (player.getLocation().distance(location) > 20) {
                    if (!(player.isFlying())) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> pet.getBukkitEntity().teleport(player), 0L);
                    }
                }
            }
        }, 100L, 100L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     *
     * @param e
     * @param toFollow
     * @since 1.0
     */
    private static void makePet(EntityLiving e, UUID toFollow) {
        try {
            Object nms_entity = e;
            if (nms_entity instanceof EntityInsentient) {
                PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(nms_entity);
                PathfinderGoalSelector target = (PathfinderGoalSelector) targetSelector.get(nms_entity);
                gsa.set(goal, new UnsafeList<>());
                gsa.set(target, new UnsafeList<>());
                goal.a(0, new PathfinderGoalFloat((EntityInsentient) nms_entity));
                goal.a(1, new PathfinderGoalWalktoTile((EntityInsentient) nms_entity, toFollow));
            } else {
                throw new IllegalArgumentException(e.getCustomName() + " is not an instance of an EntityInsentient.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static class PathfinderGoalWalktoTile extends PathfinderGoal {
        private EntityInsentient entity;
        private PathEntity path;
        private UUID p;

        public PathfinderGoalWalktoTile(EntityInsentient entitycreature, UUID p) {
            this.entity = entitycreature;
            this.p = p;
        }

        @Override
        public boolean a() {
            if (Bukkit.getPlayer(p) == null) {
                return path != null;
            }
            Location targetLocation = Bukkit.getPlayer(p).getLocation();
            this.entity.getNavigation();
            this.path = this.entity.getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ());
            this.entity.getNavigation();
            if (this.path != null) {
                this.c();
            }
            return this.path != null;
        }

        @Override
        public void c() {
            this.entity.getNavigation().a(this.path, 1.2D);
        }
    }

    /**
     *
     * @param uuid
     * @param petType
     * @param particleType
     * @since 1.0
     */
    public static void spawnPet(UUID uuid, String petType, String particleType) {
        Player player = Bukkit.getPlayer(uuid);
        World world = ((CraftWorld) player.getWorld()).getHandle();
        EnumPets enumPets = EnumPets.getByName(petType.toUpperCase());
        ParticleAPI.ParticleEffect particleEffect = null;
        if (!particleType.equalsIgnoreCase("")) {
            particleEffect = ParticleAPI.ParticleEffect.getByName(particleType);
        }
        if (!API.isStringPet(petType)) {
            player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [PetType]");
            return;
        }
        switch (enumPets) {
            case CAVE_SPIDER: {
                String customPetName = "Spider";
                CaveSpider petCaveSpider = new CaveSpider(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petCaveSpider.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petCaveSpider, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petCaveSpider.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.SPIDER_IDLE, 1F, 1F);
                makePet(petCaveSpider, player.getUniqueId());
                if (particleEffect != null) {
                    switch (particleEffect) {
                        case FLAME:
                            customPetName = ChatColor.RED + "Flaming Spider";
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.put(petCaveSpider, ParticleAPI.ParticleEffect.FLAME);
                            break;
                        case HAPPY_VILLAGER:
                            customPetName = ChatColor.DARK_GREEN + "Poisonous Spider";
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.put(petCaveSpider, ParticleAPI.ParticleEffect.HAPPY_VILLAGER);
                            break;
                    }
                }
                petCaveSpider.setCustomName(customPetName);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petCaveSpider);
                player.closeInventory();
                break;
            }
            case BABY_ZOMBIE: {
                BabyZombie petBabyZombie = new BabyZombie(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petBabyZombie.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petBabyZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petBabyZombie.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ZOMBIE_IDLE, 1F, 1F);
                makePet(petBabyZombie, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petBabyZombie);
                player.closeInventory();
                break;
            }
            case BABY_PIGZOMBIE: {
                BabyZombiePig petBabyZombiePig = new BabyZombiePig(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petBabyZombiePig.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petBabyZombiePig, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petBabyZombiePig.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petBabyZombiePig.setBaby(true);
                petBabyZombiePig.angerLevel = 0;
                player.playSound(player.getLocation(), Sound.ZOMBIE_PIG_IDLE, 1F, 1F);
                makePet(petBabyZombiePig, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petBabyZombiePig);
                player.closeInventory();
                break;
            }
            case WOLF: {
                Wolf petWolf = new Wolf(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petWolf.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petWolf, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petWolf.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petWolf.setAngry(false);
                petWolf.setTamed(true);
                petWolf.ageLocked = true;
                petWolf.setAge(0);
                player.playSound(player.getLocation(), Sound.WOLF_BARK, 1F, 1F);
                makePet(petWolf, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petWolf);
                player.closeInventory();
                break;
            }
            case CHICKEN: {
                Chicken petChicken = new Chicken(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petChicken.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petChicken, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petChicken.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petChicken.setAge(0);
                petChicken.ageLocked = true;
                player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
                makePet(petChicken, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petChicken);
                player.closeInventory();
                break;
            }
            case OCELOT: {
                Ocelot petOcelot = new Ocelot(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petOcelot.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petOcelot, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petOcelot.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petOcelot.setAge(0);
                petOcelot.ageLocked = true;
                petOcelot.setTamed(true);
                player.playSound(player.getLocation(), Sound.CAT_MEOW, 1F, 1F);
                makePet(petOcelot, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petOcelot);
                player.closeInventory();
                break;
            }
            case RABBIT: {
                Rabbit petRabbit = new Rabbit(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petRabbit.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petRabbit, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petRabbit.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petRabbit.setAge(0);
                petRabbit.ageLocked = true;
                player.playSound(player.getLocation(), Sound.DIG_GRASS, 1F, 1F);
                makePet(petRabbit, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petRabbit);
                player.closeInventory();
                break;
            }
            case SILVERFISH: {
                Silverfish petSilverfish = new Silverfish(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petSilverfish.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petSilverfish, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petSilverfish.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 1F, 1F);
                makePet(petSilverfish, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petSilverfish);
                player.closeInventory();
                break;
            }
            case ENDERMITE: {
                Endermite petEndermite = new Endermite(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petEndermite.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petEndermite, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petEndermite.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENDERMAN_IDLE, 1F, 1F);
                makePet(petEndermite, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petEndermite);
                player.closeInventory();
                break;
            }
            case SNOWMAN: {
                Snowman petSnowman = new Snowman(world, player.getName() + "'s Pet", player.getUniqueId(), EnumEntityType.PET);
                petSnowman.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petSnowman, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petSnowman.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.STEP_SNOW, 1F, 1F);
                makePet(petSnowman, player.getUniqueId());
                EntityAPI.addPlayerPetList(player.getUniqueId(), petSnowman);
                player.closeInventory();
                break;
            }
        }
    }
}
