package net.auscraft.fakemobs.mobs.actions;

import net.auscraft.skycore.util.BUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class SoundAction extends BaseAction
{
	
	private final Sound sound;
	private final int soundChance;
	
	private final float volume,
						pitch;
	
	public SoundAction(ConfigurationSection configurationSection)
	{
		Sound tempSound;
		try
		{
			tempSound = Sound.valueOf(configurationSection.getString("sound"));
		}
		catch(IllegalArgumentException e)
		{
			BUtil.logError("'" + configurationSection.getString("sound") + "' is not a valid sound effect. Defaulting to " + Sound.values()[0].name());
			tempSound = Sound.values()[0];
		}
		
		this.sound = tempSound;
		this.soundChance = configurationSection.getInt("chance");
		
		this.volume = (float) configurationSection.getDouble("volume", 10);
		this.pitch = (float) configurationSection.getDouble("pitch", 1);
	}
	
	@Override
	public void doAction(Player player)
	{
		if(random.nextInt(100) < soundChance)
		{
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
	
}
