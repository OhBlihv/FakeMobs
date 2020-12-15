package net.auscraft.fakemobs.hologram;

import net.auscraft.skycore.clientside.AbstractClientSideEntity;
import net.auscraft.skycore.clientside.hologram.HoverText;
import net.auscraft.skycore.util.EntityTrio;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class BobbingMobHologram extends HoverText
{

	@Getter
	private final List<String> content;

	public BobbingMobHologram(Location entityLocation, List<String> content)
	{
		super(entityLocation, 30);

		this.content = content;
	}

	@Override
	public AbstractClientSideEntity getNewInstance(int entityId, Player player, EntityTrio location)
	{
		return new PlayerBobbingMobHologram(entityId, player, location, content);
	}
}
