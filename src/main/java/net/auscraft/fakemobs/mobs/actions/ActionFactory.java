package net.auscraft.fakemobs.mobs.actions;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class ActionFactory
{
	
	public static BaseAction loadAction(ConfigurationSection configurationSection, String actionName) throws IllegalArgumentException
	{
		switch(actionName)
		{
			case "COMMAND":
			{
				return new CommandAction(configurationSection);
			}
			case "SOUND":
			{
				return new SoundAction(configurationSection);
			}
			case "REWARD":
			{
				return new RewardAction(configurationSection);
			}
		}
		
		throw new IllegalArgumentException("Action specified does not exist: '" + actionName + "'");
	}
	
}
