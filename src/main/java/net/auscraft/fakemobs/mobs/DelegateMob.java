package net.auscraft.fakemobs.mobs;

import org.bukkit.entity.Player;

public class DelegateMob implements IFakeMob
{

	private final BaseMob delegateMob;

	public DelegateMob(BaseMob delegateMob)
	{
		this.delegateMob = delegateMob;
	}

	@Override
	public void spawnMob(Player player)
	{
		throw new UnsupportedOperationException("Cannot spawn a delegate mob - Delegating for " + delegateMob.getDisplayName() + " (" + delegateMob.getEntityId() + ")");
	}

	@Override
	public void respawnMob()
	{
		throw new UnsupportedOperationException("Cannot spawn a delegate mob - Delegating for " + delegateMob.getDisplayName() + " (" + delegateMob.getEntityId() + ")");
	}

	@Override
	public boolean isAtLocation(int chunkX, int chunkZ)
	{
		return delegateMob.isAtLocation(chunkX, chunkZ);
	}

	@Override
	public void despawn()
	{
		throw new UnsupportedOperationException("Cannot despawn a delegate mob - Delegating for " + delegateMob.getDisplayName() + " (" + delegateMob.getEntityId() + ")");
	}

	@Override
	public void die()
	{
		throw new UnsupportedOperationException("Cannot despawn a delegate mob - Delegating for " + delegateMob.getDisplayName() + " (" + delegateMob.getEntityId() + ")");
	}

	@Override
	public void updateNearbyPlayers()
	{
		throw new UnsupportedOperationException("Cannot update a delegate mob - Delegating for " + delegateMob.getDisplayName() + " (" + delegateMob.getEntityId() + ")");
	}

	@Override
	public void onAttack(Player player)
	{
		delegateMob.onAttack(player);
	}

	@Override
	public void onRightClick(Player player)
	{
		delegateMob.onRightClick(player);
	}
}
