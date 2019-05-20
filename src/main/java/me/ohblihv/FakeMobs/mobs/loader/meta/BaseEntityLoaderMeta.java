package me.ohblihv.FakeMobs.mobs.loader.meta;

import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Deque;
import java.util.Map;

public class BaseEntityLoaderMeta implements EntityLoaderMeta
{

	private int entityId = 0;
	private final EntityType entityType;
	private final Location location;
	private final Map<InteractionType, Deque<BaseAction>> interactActions;

	public BaseEntityLoaderMeta(EntityType entityType, Location entityLocation,
	                            Map<InteractionType, Deque<BaseAction>> interactActions)
	{
		this.entityType = entityType;
		this.location = entityLocation;
		this.interactActions = interactActions;
	}

	@Override
	public int getEntityId()
	{
		if(this.entityId == 0)
		{
			throw new IllegalArgumentException("EntityID Not Yet Set!");
		}

		return entityId;
	}

	@Override
	public void setEntityId(int entityId)
	{
		this.entityId = entityId;
	}

	@Override
	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public Location getLocation()
	{
		return location;
	}

	@Override
	public Map<InteractionType, Deque<BaseAction>> getInteractActions()
	{
		return interactActions;
	}

}
