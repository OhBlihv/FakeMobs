package me.ohblihv.FakeMobs.mobs.loader.meta;

import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Deque;
import java.util.Map;

public class SimpleEntityLoaderMeta extends BaseEntityLoaderMeta
{

	public SimpleEntityLoaderMeta(EntityType entityType, Location entityLocation, Map<InteractionType, Deque<BaseAction>> interactActions)
	{
		super(entityType, entityLocation, interactActions);
	}

}
