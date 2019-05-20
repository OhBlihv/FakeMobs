package me.ohblihv.FakeMobs.management;

import me.ohblihv.FakeMobs.mobs.BaseEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class EntityUpdateTask extends BukkitRunnable
{
	
	protected final CopyOnWriteArraySet<Integer> mobIdSet = new CopyOnWriteArraySet<>();

	private EntityHandler entityHandler;
	
	private int currentTick = 0;

	public EntityUpdateTask(EntityHandler entityHandler)
	{
		this.entityHandler = entityHandler;
	}
	
	@Override
	//Run is called every 1/4 seconds or 5 ticks
	public void run()
	{
		if(!mobIdSet.isEmpty())
		{
			//Update players every two seconds
			boolean updateNearby = currentTick % 8 == 0;

			for(BaseEntity entity : entityHandler.getMobs())
			{
				if(updateNearby)
				{
					entityHandler.updateNearbyPlayers(entity);
				}

				entity.onTick(currentTick);
			}

			//Handle overflow. Avoid using long.
			if(++currentTick < 0)
			{
				currentTick = 0;
			}
		}
	}
	
}
