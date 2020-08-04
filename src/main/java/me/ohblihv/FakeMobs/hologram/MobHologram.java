package me.ohblihv.FakeMobs.hologram;

import com.skytonia.SkyCore.clientside.AbstractClientSideEntity;
import com.skytonia.SkyCore.clientside.hologram.HoverText;
import com.skytonia.SkyCore.util.EntityTrio;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MobHologram extends HoverText
{

	@Getter
	private final String content;

	public MobHologram(Location entityLocation, String content)
	{
		super(entityLocation, 30);

		this.content = content;
	}

	@Override
	public AbstractClientSideEntity getNewInstance(int entityId, Player player, EntityTrio location)
	{
		return new PlayerMobHologram(entityId, player, location, content);
	}
}
