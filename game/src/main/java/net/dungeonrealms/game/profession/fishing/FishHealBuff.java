package net.dungeonrealms.game.profession.fishing;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.entity.Player;

public class FishHealBuff extends FishBuff {

    public FishHealBuff(NBTTagCompound tag) {
        super(tag, FishBuffType.HEALTH);
    }

    public FishHealBuff(FishingTier tier) {
        super(tier, FishBuffType.HEALTH);
    }

    @Override
    public void applyBuff(Player player) {
        HealthHandler.heal(player, (int) (getValue() / 100D * HealthHandler.getMaxHP(player)), true);
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper != null && (wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC || wrapper.getAlignment() == KarmaHandler.EnumPlayerAlignments.NEUTRAL)) {
            HealthHandler.trackHeal(player);
        }
    }

    @Override
    protected int[] getChances() {
        return new int[]{10, 10, 10, 10, 10};
    }

    @Override
    protected String[] getNamePrefixes() {
        return new String[]{"Lesser", "Normal", "Mighty", "Enhanced", "Medicine"};
    }

    @Override
    protected int[] getDurations() {
        return null;
    }

    @Override
    protected void generateVal() {
        int t = getTier().getTier();
        if (t == 1) {
            setValue(2);
        } else if (t == 2) {
            setValue(4);
        } else if (t == 3) {
            setValue(6);
        } else if (t == 4) {
            setValue(8);
        } else {
            setValue(10);
        }
    }
}
