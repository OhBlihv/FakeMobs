package net.auscraft.fakemobs.mobs;

import net.auscraft.skycore.util.BUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class SimpleMob extends BaseMob
{
	
	public SimpleMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);
		
		try
		{
			setEntityType(EntityType.valueOf(configurationSection.getString("options.mob-type")));
		}
		catch(IllegalArgumentException e)
		{
			BUtil.log("Unknown entity type '" + configurationSection.getString("options.mob-type") + "'. Defaulting to VILLAGER.");
			e.printStackTrace();
		}
	}
	
}
