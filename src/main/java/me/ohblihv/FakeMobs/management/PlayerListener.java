package me.ohblihv.FakeMobs.management;

import com.skytonia.SkyCore.util.RunnableShorthand;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.NPCEntity;
import me.ohblihv.FakeMobs.util.PacketUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{

	private final EntityHandler entityHandler;

	public PlayerListener(EntityHandler entityHandler)
	{
		this.entityHandler = entityHandler;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		final Player player = event.getPlayer();
		for(BaseEntity mob : entityHandler.getMobs())
		{
			if(mob instanceof NPCEntity/* && ((NPCEntity) mob).isPlayerInitialized(player)*/)
			{
				((NPCEntity) mob).removeInitializedPlayer(player);
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

	public void handleIntermediatePlayer(Player player, int delay)
	{
		if(entityHandler.isUsedWorld(player.getWorld().getName()))
		{
			entityHandler.addIgnoredPlayer(player.getName());

			RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
			{
				entityHandler.removeIgnoredPlayer(player.getName());
			}).runTaskLater(delay);

			//

			RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
			{
				PacketUtil.getInitialNPCTeam().sendPacket(player);
			}).runTaskASync(delay / 2);
		}
	}

}
