package me.ohblihv.FakeMobs.management;

import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.file.FlatFile;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import me.ohblihv.FakeMobs.mobs.SimpleMob;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public class MobManager
{

	private static MobRunnable mobRunnable;

	protected static final ConcurrentHashMap<Integer, BaseMob> mobMap = new ConcurrentHashMap<>();
	private static final Set<String> usedWorlds = new HashSet<>();

	//Contains players who cannot currently visualise the Mobs
	private static final Set<String> ignoredPlayers = new HashSet<>();

	public static void reload()
	{
		destruct();
		init();
	}

	public static void init()
	{
		mobRunnable = new MobRunnable();

		FlatFile flatFile = FlatFile.getInstance();
		flatFile.reloadFile();

		ConfigurationSection mobSection = flatFile.getConfigurationSection("mobs");
		if(mobSection != null)
		{
			for(String mobName : mobSection.getKeys(false))
			{
				loadMob(mobSection.getConfigurationSection(mobName));
			}

			BUtil.log("Loaded " + mobRunnable.mobIdSet.size() + " fake mobs!");
		}
		else
		{
			BUtil.log("No Fake Mobs defined in configuration.");
		}

		mobRunnable.setTaskId(Bukkit.getScheduler().runTaskTimerAsynchronously(FakeMobs.getInstance(), mobRunnable, 5L, 5L).getTaskId());
	}

	public static void destruct()
	{
		for(BaseMob baseMob : mobMap.values())
		{
			baseMob.die();
		}

		mobMap.clear();

		Bukkit.getScheduler().cancelTask(mobRunnable.getTaskId());
		mobRunnable.setTaskId(-1);
	}

	public static boolean isMobId(int entityId)
	{
		return mobMap.containsKey(entityId);
	}

	//Returns if at least one boss is active
	public static boolean isMobActive()
	{
		return !mobMap.isEmpty();
	}

	//private static int nextEntityId = Integer.MAX_VALUE;
	private static int nextEntityId = -1;
	
	public static int getEntityId()
	{
		int returnedId = nextEntityId;
		
		nextEntityId--;
		
		return returnedId;
	}

	public static void loadMob(ConfigurationSection configurationSection)
	{
		int entityId = getEntityId();
		
		//TODO: Introduce factory for determine mob type
		BaseMob baseMob;
		try
		{
			switch(EntityType.valueOf(configurationSection.getString("options.mob-type")))
			{
				case PLAYER:
				{
					baseMob = new NPCMob(entityId, configurationSection);
					break;
				}
				default:
				{
					baseMob = new SimpleMob(entityId, configurationSection);
					break;
				}
			}
		}
		catch(Exception e)
		{
			BUtil.log("Unable to load mob - invalid type");
			e.printStackTrace();
			return;
		}

		addMob(baseMob);
	}

	public static void addMob(BaseMob baseMob)
	{
		usedWorlds.add(baseMob.getMobWorld().getName());

		mobMap.put(baseMob.getEntityId(), baseMob);

		mobRunnable.mobIdSet.add(baseMob.getEntityId());

		//Spawns itself within 2 seconds
	}

	public static void removeMob(int entityId)
	{
		mobMap.remove(entityId);
	}
	
	public static BaseMob getMob(int entityId)
	{
		return mobMap.get(entityId);
	}

	public static Collection<BaseMob> getAllMobs()
	{
		return mobMap.values();
	}

	public static boolean isUsedWorld(String worldName)
	{
		return usedWorlds.contains(worldName);
	}

	public static void addIgnoredPlayer(String playerName)
	{
		ignoredPlayers.add(playerName);
	}

	public static boolean isIgnoredPlayer(String playerName)
	{
		return ignoredPlayers.contains(playerName);
	}

	public static void removeIgnoredPlayer(String playerName)
	{
		ignoredPlayers.remove(playerName);
	}

}
