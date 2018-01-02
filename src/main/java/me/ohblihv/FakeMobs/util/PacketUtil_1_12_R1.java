package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 8/20/2017.
 */
public class PacketUtil_1_12_R1 implements IPacketUtil
{
	
	public void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		WrapperPlayServerSpawnEntityLiving spawnPacket = new WrapperPlayServerSpawnEntityLiving();
		
		spawnPacket.setEntityID(baseMob.getEntityId());
		spawnPacket.setType(baseMob.getEntityType());
		
		Location spawnLocation = baseMob.getLocation();
		spawnPacket.setX((int) spawnLocation.getX());
		spawnPacket.setY((int) spawnLocation.getY());
		spawnPacket.setZ((int) spawnLocation.getZ());
		spawnPacket.setPitch(spawnLocation.getPitch());
		spawnPacket.setYaw(spawnLocation.getYaw());
		//BUtil.logInfo("Spawning at " + spawnLocation.getX() + " " + spawnLocation.getY() + " " + spawnLocation.getZ() + "| " +
		//		              spawnLocation.getYaw() + " " + spawnLocation.getPitch() + " as id=" + baseMob.getEntityId());
		
		//Reverse these values since yaw == pitch and vice-versa
		spawnPacket.setYaw(spawnLocation.getPitch());
		spawnPacket.setHeadPitch(spawnLocation.getYaw());
		
		WrappedDataWatcher watcher = PacketUtil.getDefaultWatcher(spawnLocation.getWorld(), baseMob.getEntityType());
		watcher.setObject(0, (byte) 0);
		spawnPacket.setMetadata(watcher);
		
		//BUtil.logInfo("Spawning mob as " + baseMob.getEntityType() + " with id " + baseMob.getEntityId());
		spawnPacket.sendPacket(player);
	}

	@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{

	}

	public void sendDestroyPacket(Player player, int entityId)
	{
		WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
		
		destroyPacket.setEntityIds(new int[] {entityId});
		
		destroyPacket.sendPacket(player);
	}
	
}
