package net.auscraft.fakemobs.management;

import net.auscraft.fakemobs.FakeMobs;
import net.auscraft.skycore.clientside.ClientSideHandler;
import net.auscraft.skycore.util.BUtil;
import net.auscraft.skycore.util.file.FlatFile;
import net.auscraft.fakemobs.mobs.BaseMob;
import net.auscraft.fakemobs.mobs.DelegateMob;
import net.auscraft.fakemobs.mobs.HoglinMob;
import net.auscraft.fakemobs.mobs.NPCMob;
import net.auscraft.fakemobs.mobs.SimpleMob;
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
	protected static final ConcurrentHashMap<Integer, DelegateMob> delegateMobMap = new ConcurrentHashMap<>();
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

	public static void loadMob(ConfigurationSection configurationSection)
	{
		int entityId = ClientSideHandler.getUniqueEntityId();
		
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
				/*case VILLAGER:
				{
					baseMob = new VillagerMob(entityId, configurationSection);
					break;
				}*/
				case HOGLIN:
				{
					baseMob = new HoglinMob(entityId, configurationSection);
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

		for(int delegateMobId : baseMob.getAllDelegateMobsIds())
		{
			addDelegateMob(delegateMobId, baseMob);
		}

		//Spawns itself within 2 seconds
	}

	public static void addDelegateMob(int entityId, BaseMob hostMob)
	{
		delegateMobMap.put(entityId, new DelegateMob(hostMob));
	}

	public static void removeDelegateMob(int entityId)
	{
		delegateMobMap.remove(entityId);
	}

	public static void removeMob(int entityId)
	{
		BaseMob baseMob = mobMap.remove(entityId);

		for(int delegateMobId : baseMob.getAllDelegateMobsIds())
		{
			removeDelegateMob(delegateMobId);
		}
	}
	
	public static BaseMob getMob(int entityId)
	{
		return mobMap.get(entityId);
	}

	public static DelegateMob getDelegateMob(int entityId)
	{
		return delegateMobMap.get(entityId);
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
