package me.ohblihv.FakeMobs.management;

import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		for(BaseMob mob : MobManager.getAllMobs())
		{
			if(mob instanceof NPCMob/* && ((NPCMob) mob).isPlayerInitialized(player)*/)
			{
				((NPCMob) mob).removeInitializedPlayer(player);
			}
		}
	}

}
