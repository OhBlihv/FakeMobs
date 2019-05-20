package me.ohblihv.FakeMobs.management;

import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.file.FlatFile;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.FakeEntityTypes;
import me.ohblihv.FakeMobs.mobs.loader.EntityLoader;
import me.ohblihv.FakeMobs.mobs.loader.exception.EntityLoaderException;
import me.ohblihv.FakeMobs.mobs.loader.meta.EntityLoaderMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public class EntityHandler
{

	private final FakeMobs plugin;

	private EntityUpdateTask entityUpdateTask;

	private final ConcurrentHashMap<Integer, BaseEntity> mobMap = new ConcurrentHashMap<>();
	private final Set<String> usedWorlds = new HashSet<>();

	//Contains players who cannot currently visualise the Mobs
	private final Set<String> ignoredPlayers = new HashSet<>();

	public void reload()
	{
		destruct();

		this.entityUpdateTask = new EntityUpdateTask(this);

		FlatFile config = FlatFile.getInstance();
		config.reloadFile();
		ConfigurationSection rootSection = config.getSave();

		ConfigurationSection mobSection;
		if(rootSection.contains("mobs") && rootSection.isConfigurationSection ("mobs") &&
			(mobSection = rootSection.getConfigurationSection("mobs")) != null)
		{
			for(String mobName : mobSection.getKeys(false))
			{
				loadMob(mobSection.getConfigurationSection(mobName));
			}

			BUtil.log("Loaded " + entityUpdateTask.mobIdSet.size() + " fake mobs!");
		}
		else
		{
			BUtil.log("No Fake Mobs defined in configuration.");
		}

		entityUpdateTask.runTaskTimerAsynchronously(plugin, 5L, 5L);
	}

	public EntityHandler(FakeMobs plugin)
	{
		this.plugin = plugin;

		reload();
	}

	public void destruct()
	{
		for(BaseEntity baseEntity : mobMap.values())
		{
			baseEntity.die();
		}

		mobMap.clear();

		if(entityUpdateTask != null)
		{
			entityUpdateTask.cancel();
		}
	}

	public boolean isMobId(int entityId)
	{
		return mobMap.containsKey(entityId);
	}

	//Returns if at least one boss is active
	public boolean isMobActive()
	{
		return !mobMap.isEmpty();
	}

	//private static int nextEntityId = Integer.MAX_VALUE;
	private int nextEntityId = -1;
	
	public int getEntityId()
	{
		int returnedId = nextEntityId;
		
		nextEntityId--;
		
		return returnedId;
	}

	public Collection<BaseEntity> getMobs()
	{
		return Collections.unmodifiableCollection(mobMap.values());
	}

	public void loadMob(ConfigurationSection configurationSection)
	{
		FakeEntityTypes fakeType;
		try
		{
			fakeType = FakeEntityTypes.valueOf(configurationSection.getString("type"));
		}
		catch(IllegalArgumentException e)
		{
			BUtil.log("No fake entity type as '" + configurationSection.getString("type") + "'");
			return;
		}

		final EntityLoader entityLoader = fakeType.getEntityLoader();
		final EntityLoaderMeta loaderMeta;
		try
		{
			loaderMeta = entityLoader.loadConfiguration(configurationSection);
		}
		catch (EntityLoaderException e)
		{
			//TODO: Neaten
			e.printStackTrace();
			return;
		}

		//Don't claim an entity ID until this ID has been loaded.
		int entityId = getEntityId();
		loaderMeta.setEntityId(entityId);

		addEntity(entityLoader.createEntityContainer(loaderMeta));
	}

	public void addEntity(BaseEntity baseEntity)
	{
		usedWorlds.add(baseEntity.getMobWorld().getName());

		mobMap.put(baseEntity.getEntityId(), baseEntity);

		entityUpdateTask.mobIdSet.add(baseEntity.getEntityId());

		//Spawns itself within 2 seconds
	}

	public void removeEntity(int entityId)
	{
		BaseEntity removedEntity = mobMap.remove(entityId);

		if(removedEntity != null)
		{
			entityUpdateTask.mobIdSet.remove(removedEntity.getEntityId());
		}

		//No handling for 'usedWorlds' removal.
	}
	
	public BaseEntity getEntity(int entityId)
	{
		return mobMap.get(entityId);
	}

	public boolean isUsedWorld(String worldName)
	{
		return usedWorlds.contains(worldName);
	}

	public void addIgnoredPlayer(String playerName)
	{
		ignoredPlayers.add(playerName);
	}

	public boolean isIgnoredPlayer(String playerName)
	{
		return ignoredPlayers.contains(playerName);
	}

	public void removeIgnoredPlayer(String playerName)
	{
		ignoredPlayers.remove(playerName);
	}

}
