package net.auscraft.fakemobs.hologram;

import net.auscraft.skycore.clientside.hologram.PlayerHoverText;
import net.auscraft.skycore.clientside.movement.BobMovement;
import net.auscraft.skycore.util.EntityTrio;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerMobHologram extends PlayerHoverText
{

	private final List<String> content;

	public PlayerMobHologram(int entityId, Player player, EntityTrio initialLocation, List<String> content)
	{
		super(entityId, player, initialLocation);

		this.content = content;

		addMovementHandler(new BobMovement(0.2D, 24D));
	}

	@Override
	public String getText(int tick)
	{
		return content.get((tick / 20) % content.size());
	}
}
