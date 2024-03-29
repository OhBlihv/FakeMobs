package net.auscraft.fakemobs.management;

import lombok.Getter;
import lombok.Setter;
import net.auscraft.fakemobs.mobs.BaseMob;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class MobRunnable implements Runnable
{
	
	private static final String prefix = "[FakeMobs-Thread] ";
	
	protected CopyOnWriteArraySet<Integer> mobIdSet = new CopyOnWriteArraySet<>();
	
	private int currentTick = 0;
	
	@Getter
	@Setter
	private int taskId = -1;
	
	@Override
	//Run is called every 1/4 seconds or 5 ticks
	public void run()
	{
		if(mobIdSet.isEmpty()) //No Mobs
		{
			return; //Wait til next execution
		}
		
		//Update players every two seconds
		if(currentTick % 8 == 0)
		{
			for(BaseMob baseBoss : MobManager.mobMap.values())
			{
				baseBoss.updateNearbyPlayers();
			}
		}

		for(BaseMob baseMob : MobManager.mobMap.values())
		{
			baseMob.onTick(currentTick);
		}

		//Handle overflow. Avoid using long.
		if(++currentTick < 0)
		{
			currentTick = 0;
		}
	}
	
}
