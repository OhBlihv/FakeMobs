package net.auscraft.fakemobs.util;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.auscraft.fakemobs.mobs.BaseMob;
import net.auscraft.fakemobs.mobs.NPCMob;
import net.auscraft.fakemobs.npc.fakeplayer.FakeEntityPlayer;
import net.auscraft.skycore.util.BUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public class PacketUtil
{
	
	private static IPacketUtil packetUtilImpl = null;
	
	private static final Map<EntityType, WrappedDataWatcher> watcherCache = new HashMap<>();
	
	public static WrappedDataWatcher getDefaultWatcher(World world, EntityType type)
	{
		WrappedDataWatcher watcher = watcherCache.get(type);
		if(watcher != null)
		{
			return watcher.deepClone();
		}
		
		Entity entity = world.spawnEntity(new Location(world, 0, 256, 0), type);

		watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
		watcherCache.put(type, watcher);
		
		entity.remove();
		return watcher;
	}
	
	private static void initImpl()
	{
		switch(BUtil.getNMSVersion())
		{
			case "v1_17_R1": packetUtilImpl = new PacketUtil_1_17_R1(); break;
			default:
				throw new IllegalArgumentException("Unsupported NMS Version " + BUtil.getNMSVersion());
		}
	}

	public static FakeEntityPlayer getFakeEntityPlayer(World world, GameProfile gameProfile)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		return packetUtilImpl.getFakeEntityPlayer(world, gameProfile);
	}

	public static void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}
		
		packetUtilImpl.sendSpawnPacket(player, baseMob);
	}

	public static void sendPlayerSpawnPacket(Player player, NPCMob npcMob)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		packetUtilImpl.sendPlayerSpawnPackets(player, npcMob);
	}

	public static void sendDestroyPacket(Player player, int entityId)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		packetUtilImpl.sendDestroyPacket(player, entityId);
	}

	public static void sendLookPacket(Player player, float yaw, float pitch, int entityId)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		packetUtilImpl.sendLookPacket(player, yaw, pitch, entityId);
	}

	public static void initializeSkin(String skinUUID, NPCMob npcMob, World world)
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		packetUtilImpl.initializeSkin(skinUUID, npcMob, world);
	}

	public static AbstractPacket getNPCTeam()
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		return packetUtilImpl.getNPCTeam();
	}

	public static AbstractPacket getInitialNPCTeam()
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		return packetUtilImpl.getInitialNPCTeam();
	}

	public static YggdrasilAuthenticationService getAuthenticationService()
	{
		if(packetUtilImpl == null)
		{
			initImpl();
		}

		return packetUtilImpl.getAuthenticationService();
	}
	
}
