package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Chris Brown (OhBlihv) on 8/20/2017.
 */
public class PacketUtil_1_8_R3 implements IPacketUtil
{
	
	public void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		WrapperPlayServerSpawnEntityLiving spawnPacket = new WrapperPlayServerSpawnEntityLiving();
		
		spawnPacket.setEntityID(baseMob.getEntityId());
		spawnPacket.setType(baseMob.getEntityType());
		
		Location spawnLocation = baseMob.getMobLocation();
		spawnPacket.setX((int) spawnLocation.getX());
		spawnPacket.setY((int) spawnLocation.getY());
		spawnPacket.setZ((int) spawnLocation.getZ());
		spawnPacket.setYaw(spawnLocation.getYaw());
		//BUtil.logInfo("Spawning at " + spawnLocation.getX() + " " + spawnLocation.getY() + " " + spawnLocation.getZ() + "| " +
		//		              spawnLocation.getYaw() + " " + spawnLocation.getPitch() + " as id=" + baseMob.getEntityId());
		
		//Reverse these values since yaw == pitch and vice-versa
		//spawnPacket.setYaw(spawnLocation.getPitch());
		spawnPacket.setHeadPitch(spawnLocation.getPitch());
		
		WrappedDataWatcher watcher = PacketUtil.getDefaultWatcher(spawnLocation.getWorld(), baseMob.getEntityType());
		watcher.setObject(0, (byte) 0);
		spawnPacket.setMetadata(watcher);
		
		//BUtil.logInfo("Spawning mob as " + baseMob.getEntityType() + " with id " + baseMob.getEntityId());
		spawnPacket.sendPacket(player);
	}

	@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(
			PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
			npcMob.getFakeEntityPlayer());

		playerConnection.sendPacket(infoPacket);

		playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn(npcMob.getFakeEntityPlayer()));

		new BukkitRunnable()
		{

			int tick = 0;

			@Override
			public void run()
			{
				if(tick == 0)
				{
					playerConnection.sendPacket(infoPacket);

					final Location location = npcMob.getMobLocation();

					playerConnection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
						npcMob.getEntityId(),
						(byte) ((location.getX() - ((int) location.getX())) / 32D),
						(byte) ((location.getX() - ((int) location.getY())) / 32D),
						(byte) ((location.getZ() - ((int) location.getZ())) / 32D),
						(byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F)),
						(byte) (MathHelper.d(location.getPitch() * 256.0F / 360.0F)),
						true
					));

					playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation(
						npcMob.getFakeEntityPlayer(),
						(byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F))
					));
				}

				WrapperPlayServerScoreboardTeam teamPacket = new WrapperPlayServerScoreboardTeam();
				teamPacket.setNameTagVisibility("never");
				teamPacket.setName("NPC-TEAM");
				teamPacket.setMode(3);
				teamPacket.setPrefix("§8[NPC] ");
				teamPacket.getPlayers().add(npcMob.getProfile().getName());

				teamPacket.sendPacket(player);

				WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment();

				equipmentPacket.setEntityID(npcMob.getEntityId());
				equipmentPacket.setItem(npcMob.getHeldItem());
				equipmentPacket.setSlot(EnumWrappers.ItemSlot.MAINHAND);

				equipmentPacket.sendPacket(player);

				if(++tick > 3)
				{
					playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
						PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
						npcMob.getFakeEntityPlayer()));

					this.cancel();
				}
			}

		}.runTaskTimerAsynchronously(FakeMobs.getInstance(),  5, 20);
	}

	public void sendDestroyPacket(Player player, int entityId)
	{
		WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();
		
		destroyPacket.setEntityIds(new int[] {entityId});
		
		destroyPacket.sendPacket(player);
	}
	
}
