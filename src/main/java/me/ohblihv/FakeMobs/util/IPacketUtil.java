package me.ohblihv.FakeMobs.util;

import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 8/20/2017.
 */
public interface IPacketUtil
{
	
	void sendSpawnPacket(Player player, BaseMob baseMob);

	void sendPlayerSpawnPackets(Player player, NPCMob npcMob);
	
	void sendDestroyPacket(Player player, int entityId);
	
}
