package me.ohblihv.FakeMobs.mobs;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

public class HoglinMob extends BaseMob
{

	public HoglinMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);
		setEntityType(EntityType.HOGLIN);
	}

	@Override
	public void setMetadata(WrappedDataWatcher watcher)
	{
		super.setMetadata(watcher);

		watcher.setObject(15, false); // Not Baby
		watcher.setObject(16, true); // No Shiver
	}

}
