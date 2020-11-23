package net.auscraft.fakemobs.management;

import net.auscraft.fakemobs.FakeMobs;
import net.auscraft.fakemobs.mobs.BaseMob;
import net.auscraft.fakemobs.mobs.NPCMob;
import net.auscraft.fakemobs.util.PacketUtil;
import net.auscraft.skycore.util.RunnableShorthand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		handleIntermediatePlayer(event.getPlayer(), 10);
	}

	@EventHandler
	public void onPlayerScoreboardChange(PlayerChangedWorldEvent event)
	{
		handleIntermediatePlayer(event.getPlayer(), 10);
	}

	public static void handleIntermediatePlayer(Player player, int delay)
	{
		if(MobManager.isUsedWorld(player.getWorld().getName()))
		{
			MobManager.addIgnoredPlayer(player.getName());

			RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
			{
				MobManager.removeIgnoredPlayer(player.getName());
			}).runTaskLater(delay);

			//

			RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
			{
				PacketUtil.getInitialNPCTeam().sendPacket(player);
			}).runTaskASync(delay / 2);
		}
	}

}
