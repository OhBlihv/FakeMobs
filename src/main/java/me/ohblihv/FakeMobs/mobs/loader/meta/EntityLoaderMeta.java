package me.ohblihv.FakeMobs.mobs.loader.meta;

import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Deque;
import java.util.Map;

public interface EntityLoaderMeta
{

	int getEntityId();

	void setEntityId(int entityId);

	EntityType getEntityType();

	Location getLocation();

	Map<InteractionType, Deque<BaseAction>> getInteractActions();

}
