package me.ohblihv.FakeMobs.mobs;

import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Deque;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class SimpleEntity extends BaseEntity
{

	public SimpleEntity(int entityId, Location mobLocation, Deque<BaseAction> leftClickActions, Deque<BaseAction> rightClickActions, EntityType entityType)
	{
		super(entityId, mobLocation, leftClickActions, rightClickActions);

		setEntityType(entityType);
	}
	
}
