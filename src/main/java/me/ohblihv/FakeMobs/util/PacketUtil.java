package me.ohblihv.FakeMobs.util;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.skytonia.SkyCore.util.BUtil;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
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
			case "v1_9_R2": packetUtilImpl = new PacketUtil_1_9_R2(); break;
			case "v1_12_R1": packetUtilImpl = new PacketUtil_1_12_R1(); break;
			default:
				throw new IllegalArgumentException("Unsupported NMS Version " + BUtil.getNMSVersion());
		}
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
		packetUtilImpl.sendDestroyPacket(player, entityId);
	}
	
}
