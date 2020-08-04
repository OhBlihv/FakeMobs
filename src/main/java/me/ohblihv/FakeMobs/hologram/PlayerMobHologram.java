package me.ohblihv.FakeMobs.hologram;

import com.skytonia.SkyCore.clientside.hologram.PlayerHoverText;
import com.skytonia.SkyCore.clientside.movement.BobMovement;
import com.skytonia.SkyCore.util.EntityTrio;
import org.bukkit.entity.Player;

public class PlayerMobHologram extends PlayerHoverText
{

	private final String content;

	public PlayerMobHologram(int entityId, Player player, EntityTrio initialLocation, String content)
	{
		super(entityId, player, initialLocation);

		this.content = content;

		addMovementHandler(new BobMovement(0.2D, 24D));
	}

	@Override
	public String getText(int tick)
	{
		return content;
	}
}
