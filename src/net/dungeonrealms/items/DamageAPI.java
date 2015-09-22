package net.dungeonrealms.items;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    /**
     * Calculates the weapon damage based on the nbt tag of an item, the attacker and receiver
     * @param attacker
     * @param reciever
     * @param tag
     * @since 1.0
     */
    public static double calculateWeaponDamage(Player attacker, Entity reciever, NBTTagCompound tag) {
        ItemStack ourItem = attacker.getItemInHand();
        int weaponTier = new Attribute(ourItem).getItemTier().getId();
        double damage = tag.getDouble("damage");
        boolean isHitCrit = false;
        if (reciever instanceof Player) {
            if (tag.getInt("vsPlayers") != 0) {
                damage += tag.getInt("vsPlayers");
                //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                //TODO: PROBABLY CHANGE
            }
        } else {
            if (reciever.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                if (tag.getInt("vsMonsters") != 0) {
                    damage += tag.getInt("vsMonsters");
                    //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                    // TODO: PROBABLY CHANGE
                }
            }
        }

        if (tag.getInt("pureDamage") != 0) {
            damage += tag.getInt("pureDamage");
        }

        if (tag.getInt("armorPenetration") != 0) {
            damage += tag.getInt("armorPenetration");
        }

        if (tag.getInt("accuracy") != 0) {
            damage += tag.getInt("accuracy");
        }

        LivingEntity leReceiver = (LivingEntity) reciever;
        //TODO: THIS WAS BEING USED IN DR BUT THE TIER OF THE ITEM WAS HARDCODED TO 0. WHY? NO CLUE. SHOULD WE KEEP OR REMOVE?
        if (tag.getInt("fireDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    leReceiver.setFireTicks(15);
                    break;
                case 1:
                    leReceiver.setFireTicks(25);
                    break;
                case 2:
                    leReceiver.setFireTicks(30);
                    break;
                case 3:
                    leReceiver.setFireTicks(35);
                    break;
                case 4:
                    leReceiver.setFireTicks(40);
                    break;
            }
            damage += tag.getInt("fireDamage");
        }

        if (tag.getInt("iceDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage += tag.getInt("iceDamage");
        }

        if (tag.getInt("poisonDamage") != 0) {
            switch (weaponTier) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage += tag.getInt("poisonDamage");
        }

        if (tag.getInt("criticalHit") != 0) {
            if (new Random().nextInt(99) < tag.getInt("criticalHit")) {
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, reciever.getLocation(),
                            new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 50);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isHitCrit = true;
            }
        }

        if (tag.getInt("lifesteal") != 0) {
            //TODO: LIFESTEAL WHEN WE HAVE OUR CUSTOM HP SHIT DONE
        }

        if (tag.getInt("blind") != 0) {
            boolean canTargetBeBlinded = false;
            if (new Random().nextInt(99) < tag.getInt("blind")) {
                if (!leReceiver.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    canTargetBeBlinded = true;
                } else if (leReceiver.hasMetadata("blind")) {
                    long last_blind = leReceiver.getMetadata("blind").get(0).asLong();
                    if ((System.currentTimeMillis() - last_blind) <= (10 * 1000)) {
                        // Less than 10 seconds, do nothing.
                        canTargetBeBlinded = false;
                    }
                }

                if (canTargetBeBlinded) {
                    switch (weaponTier) {
                        case 0:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                            break;
                        case 1:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                            break;
                        case 2:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                            break;
                        case 3:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                        case 4:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                    }
                    leReceiver.setMetadata("blind", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                }
            }
        }

        if (attacker.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int potionTier = 0;
            for (PotionEffect potionEffect : attacker.getActivePotionEffects()) {
                if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                    potionTier = potionEffect.getAmplifier();
                    break;
                }
            }
            switch (potionTier) {
                case 0:
                    damage *= 1.1;
                    break;
                case 1:
                    damage *= 1.3;
                    break;
                case 2:
                    damage *= 1.5;
                    break;
            }
        }
        if (isHitCrit) {
            damage = damage * 1.5;
        }
        Bukkit.broadcastMessage("Final Attack damage: " + damage);
        return damage;
    }

    /**
     * Calculates the weapon damage based on the metadata of the projectile, the attacker and receiver
     * @param attacker
     * @param reciever
     * @param projectile
     * @since 1.0
     */
    public static double calculateProjectileDamage(Player attacker, Entity reciever, Projectile projectile) {
        double damage = projectile.getMetadata("damage").get(0).asDouble();
        boolean isHitCrit = false;
        if (reciever instanceof Player) {
            if (projectile.getMetadata("vsPlayers").get(0).asInt() != 0) {
                damage += projectile.getMetadata("vsPlayers").get(0).asInt();
                //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                //TODO: PROBABLY CHANGE
            }
        } else {
            if (reciever.getMetadata("type").get(0).asString().equalsIgnoreCase("hostile")) {
                if (projectile.getMetadata("vsMonsters").get(0).asInt() != 0) {
                    damage += projectile.getMetadata("vsMonsters").get(0).asInt();
                    //THIS IS DR'S FORMULA. I'M NOT SURE WHY THEY ALWAYS DIVIDE BY 100 SURELY YOU'D JUST SET THE DATA TO 80 INSTEAD OF 800 ETC.
                    // TODO: PROBABLY CHANGE
                }
            }
        }

        if (reciever.getMetadata("pureDamage").get(0).asInt() != 0) {
            damage += reciever.getMetadata("pureDamage").get(0).asInt();
        }

        if (reciever.getMetadata("armorPenetration").get(0).asInt() != 0) {
            damage += reciever.getMetadata("armorPenetration").get(0).asInt();
        }

        if (reciever.getMetadata("accuracy").get(0).asInt() != 0) {
            damage += reciever.getMetadata("accuracy").get(0).asInt();
        }

        LivingEntity leReceiver = (LivingEntity) reciever;
        //TODO: THIS WAS BEING USED IN DR BUT THE TIER OF THE ITEM WAS HARDCODED TO 0. WHY? NO CLUE. SHOULD WE KEEP OR REMOVE?
        if (projectile.getMetadata("fireDamage").get(0).asInt() != 0) {
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.setFireTicks(15);
                    break;
                case 1:
                    leReceiver.setFireTicks(25);
                    break;
                case 2:
                    leReceiver.setFireTicks(30);
                    break;
                case 3:
                    leReceiver.setFireTicks(35);
                    break;
                case 4:
                    leReceiver.setFireTicks(40);
                    break;
            }
            damage += projectile.getMetadata("fireDamage").get(0).asInt();
        }

        if (projectile.getMetadata("iceDamage").get(0).asInt() != 0) {
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                    break;
            }
            damage +=  projectile.getMetadata("iceDamage").get(0).asInt();
        }

        if (projectile.getMetadata("poisonDamage").get(0).asInt() != 0) {
            switch (projectile.getMetadata("tier").get(0).asInt()) {
                case 0:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
                    break;
                case 1:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                    break;
                case 2:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
                    break;
                case 3:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                    break;
                case 4:
                    leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                    break;
            }
            damage +=  projectile.getMetadata("poisonDamage").get(0).asInt();
        }

        if (projectile.getMetadata("criticalHit").get(0).asInt() != 0) {
            if (new Random().nextInt(99) < projectile.getMetadata("criticalHit").get(0).asInt()) {
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.MAGIC_CRIT, reciever.getLocation(),
                            new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 50);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                isHitCrit = true;
            }
        }

        if (projectile.getMetadata("lifesteal").get(0).asInt() != 0) {
            //TODO: LIFESTEAL WHEN WE HAVE OUR CUSTOM HP SHIT DONE
        }

        if (projectile.getMetadata("blind").get(0).asInt() != 0) {
            boolean canTargetBeBlinded = false;
            if (new Random().nextInt(99) < projectile.getMetadata("blind").get(0).asInt()) {
                if (!leReceiver.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    canTargetBeBlinded = true;
                } else if (leReceiver.hasMetadata("blind")) {
                    long last_blind = leReceiver.getMetadata("blind").get(0).asLong();
                    if ((System.currentTimeMillis() - last_blind) <= (10 * 1000)) {
                        // Less than 10 seconds, do nothing.
                        canTargetBeBlinded = false;
                    }
                }

                if (canTargetBeBlinded) {
                    switch (projectile.getMetadata("tier").get(0).asInt()) {
                        case 0:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
                            break;
                        case 1:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
                            break;
                        case 2:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                            break;
                        case 3:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                        case 4:
                            leReceiver.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                            break;
                    }
                    leReceiver.setMetadata("blind", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
                }
            }
        }

        if (attacker.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int potionTier = 0;
            for (PotionEffect potionEffect : attacker.getActivePotionEffects()) {
                if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                    potionTier = potionEffect.getAmplifier();
                    break;
                }
            }
            switch (potionTier) {
                case 0:
                    damage *= 1.1;
                    break;
                case 1:
                    damage *= 1.3;
                    break;
                case 2:
                    damage *= 1.5;
                    break;
            }
        }
        if (isHitCrit) {
            damage = damage * 1.5;
        }
        Bukkit.broadcastMessage("Final Attack damage: " + damage);
        return damage;
    }
}
