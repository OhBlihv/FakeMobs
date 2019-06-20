package me.ohblihv.FakeMobs.mobs.loader;

import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.LocationUtil;
import me.ohblihv.FakeMobs.mobs.actions.ActionFactory;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import me.ohblihv.FakeMobs.mobs.loader.exception.EntityLoaderException;
import me.ohblihv.FakeMobs.mobs.loader.meta.BaseEntityLoaderMeta;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

public abstract class BaseEntityLoader<T extends BaseEntityLoaderMeta> implements EntityLoader<T>
{

	@Override
	public T loadConfiguration(ConfigurationSection configurationSection) throws EntityLoaderException
	{
		final Location mobLocation = LocationUtil.getLocation(configurationSection.getConfigurationSection("location"));
		final EntityType entityType;
		try
		{
			entityType = EntityType.valueOf(configurationSection.getString("options.mob-type"));
		}
		catch(IllegalArgumentException e)
		{
			throw new EntityLoaderException("No entity type as '" + configurationSection.getString("options.mob-type") + "'");
		}

		final Map<InteractionType, Deque<BaseAction>> interactActions = new EnumMap<>(InteractionType.class);

		for(InteractionType interactionType : InteractionType.values())
		{
			ConfigurationSection interactionSection;
			if(configurationSection.contains(interactionType.name()) && configurationSection.isConfigurationSection(interactionType.name()) &&
				(interactionSection = configurationSection.getConfigurationSection(interactionType.name())) != null)
			{
				interactActions.put(interactionType, loadActions(interactionSection));
			}
		}

		return (T) new BaseEntityLoaderMeta(entityType, mobLocation, interactActions);
	}

	@Override
	public Deque<BaseAction> loadActions(ConfigurationSection configurationSection)
	{
		//This click type is not defined
		if(configurationSection == null)
		{
			return null;
		}

		Deque<BaseAction> actionDeque = new ArrayDeque<>();
		for(String actionName : configurationSection.getKeys(false))
		{
			BaseAction action;
			try
			{
				action = ActionFactory.loadAction(configurationSection.getConfigurationSection(actionName), actionName);
			}
			catch(IllegalArgumentException e)
			{
				BUtil.log(e.getMessage());
				continue;
			}

			actionDeque.add(action);
		}

		return actionDeque;
	}

}
