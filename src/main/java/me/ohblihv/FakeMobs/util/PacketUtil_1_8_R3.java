package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
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

	/*@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{
		if(!npcMob.isPlayerInitialized(player))
		{
			final List<PlayerInfoData> playerInfo = Collections.singletonList(new PlayerInfoData(
					WrappedGameProfile.fromHandle(npcMob.getProfile()),
					0,
					EnumWrappers.NativeGameMode.NOT_SET,
					WrappedChatComponent.fromText(npcMob.getDisplayName())));

			WrapperPlayServerPlayerInfo wrappedPacket = new WrapperPlayServerPlayerInfo();
			wrappedPacket.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
			wrappedPacket.setData(playerInfo);

			wrappedPacket.sendPacket(player);

			RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
			{
				WrapperPlayServerPlayerInfo removePacket = new WrapperPlayServerPlayerInfo();
				removePacket.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
				removePacket.setData(playerInfo);
			}).runTaskASync(20L);

			npcMob.addInitializedPlayer(player);
		}

		WrapperPlayServerNamedEntitySpawn18 spawnPacket = new WrapperPlayServerNamedEntitySpawn18();

		*//*spawnPacket.setEntityID(npcMob.getEntityId());

		spawnPacket.setPosition(npcMob.getLocation().toVector());
		//spawnPacket.setMetadata(new WrappedDataWatcher(player));
		spawnPacket.setPlayerUUID(npcMob.getProfile().getId());*//*

		spawnPacket.setEntityID(npcMob.getEntityId());
		spawnPacket.setPlayerUUID(npcMob.getProfile().getId());
		spawnPacket.setPosition(npcMob.getLocation().toVector());
		//spawnPacket.setMetadata(WrappedDataWatcher.getEntityWatcher(player));

		spawnPacket.sendPacket(player);

		BUtil.log("Spawning NPC for " + player.getName() + " at " + npcMob.getLocation().toVector());
	}*/

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
				}

				WrapperPlayServerScoreboardTeam teamPacket = new WrapperPlayServerScoreboardTeam();
				teamPacket.setNameTagVisibility("never");
				teamPacket.setName("NPC-TEAM");
				teamPacket.setMode(3);
				teamPacket.setPrefix("§8[NPC] ");
				teamPacket.getPlayers().add(npcMob.getProfile().getName());

				teamPacket.sendPacket(player);

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
