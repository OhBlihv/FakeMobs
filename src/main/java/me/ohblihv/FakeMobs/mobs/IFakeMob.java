package me.ohblihv.FakeMobs.mobs;

import org.bukkit.entity.Player;

public interface IFakeMob
{

	void spawnMob(Player player);
	void respawnMob();
	boolean isAtLocation(int chunkX, int chunkZ);
	void despawn();
	void die();
	void updateNearbyPlayers();

	void onAttack(Player player);
	void onRightClick(Player player);

}
