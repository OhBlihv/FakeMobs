package me.ohblihv.FakeMobs.mobs;

import com.skytonia.SkyCore.util.BUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class SimpleEntity extends BaseEntity
{
	
	public SimpleEntity(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);
		
		try
		{
			setEntityType(EntityType.valueOf(configurationSection.getString("options.mob-type")));
		}
		catch(IllegalArgumentException e)
		{
			BUtil.log("Unknown entity type '" + configurationSection.getString("options.mob-type") + "'. Defaulting to VILLAGER.");
		}
	}
	
}