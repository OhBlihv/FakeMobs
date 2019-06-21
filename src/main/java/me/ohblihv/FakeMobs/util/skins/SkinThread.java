package me.ohblihv.FakeMobs.util.skins;

import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class SkinThread implements Runnable
{

	private final BlockingDeque<SkinFetcher> tasks = new LinkedBlockingDeque<>();
	private volatile int delay = 0;

	public void addRunnable(SkinFetcher r)
	{
		Iterator<SkinFetcher> itr = tasks.iterator();
		//Clear any old duplicate skin fetchers before starting this new task
		while (itr.hasNext())
		{
			if (itr.next().getNpc().getProfile().getId().equals(r.getNpc().getProfile().getId()))
			{
				itr.remove();
			}
		}
		tasks.offer(r);
	}

	public void delay()
	{
		delay = 120; // need to wait a minute before Mojang accepts API calls again
	}

	@Override
	public void run()
	{
		if (delay != 0)
		{
			delay--;
			return;
		}
		Runnable r = tasks.pollFirst();
		if (r != null)
		{
			r.run();
		}
	}

}