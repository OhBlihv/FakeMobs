package me.ohblihv.FakeMobs.management;

import lombok.Getter;
import lombok.Setter;
import me.ohblihv.FakeMobs.mobs.BaseMob;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class MobRunnable implements Runnable
{
	
	private static final String prefix = "[FakeMobs-Thread] ";
	
	protected CopyOnWriteArraySet<Integer> mobIdSet = new CopyOnWriteArraySet<>();
	
	private long currentTick = 0;
	
	@Getter
	@Setter
	private int taskId = -1;
	
	@Override
	//Run is called every 1 second or 20 ticks
	public void run()
	{
		if(mobIdSet.isEmpty()) //No Mobs
		{
			return; //Wait til next execution
		}
		
		//Update players every two seconds
		if(currentTick % 2 == 0)
		{
			for(BaseMob baseBoss : MobManager.mobMap.values())
			{
				baseBoss.updateNearbyPlayers();
			}
		}
		
		++currentTick;
	}
	
}
