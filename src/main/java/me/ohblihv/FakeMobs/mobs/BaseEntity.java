package me.ohblihv.FakeMobs.mobs;

import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.LocationUtil;
import com.skytonia.SkyCore.util.RunnableShorthand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.management.EntityHandler;
import me.ohblihv.FakeMobs.mobs.actions.ActionFactory;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public abstract class BaseEntity
{

	@Getter
    private final ArrayDeque<Player> nearbyPlayers = new ArrayDeque<>();

	@Getter
	private int viewDistance = 30;

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private int entityId;
	
	@Getter
	private EntityType entityType = EntityType.VILLAGER;
	void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;

		if(entityType != EntityType.PLAYER)
		{
			PacketUtil.getDefaultWatcher(Bukkit.getWorlds().get(0), entityType);
		}
	}

	//Used for judging player proximity
	@Getter private final Location  mobLocation;
	@Getter private final World     mobWorld;
	@Getter private final int       chunkX, chunkZ;
	
	private final Deque<BaseAction> attackActions = new ArrayDeque<>(),
									interactActions = new ArrayDeque<>();

	public BaseEntity(int entityId, ConfigurationSection configurationSection)
	{
		this.mobLocation = LocationUtil.getLocation(configurationSection.getConfigurationSection("location"));
		BUtil.log("Loaded at " + mobLocation);
		this.mobWorld = mobLocation.getWorld();
		Chunk chunk = this.mobLocation.getChunk();
		this.chunkX = chunk.getX();
		this.chunkZ = chunk.getZ();
		this.entityId = entityId;
		
		//Trigger the DataWatcher cache for this entity type
		PacketUtil.getDefaultWatcher(mobLocation.getWorld(), entityType);
		
		loadActions(configurationSection.getConfigurationSection("actions.LEFT_CLICK"), attackActions);
		loadActions(configurationSection.getConfigurationSection("actions.RIGHT_CLICK"), interactActions);
	}
	
	private void loadActions(ConfigurationSection configurationSection, Deque<BaseAction> actionDeque)
	{
		//This click type is not defined
		if(configurationSection == null)
		{
			return;
		}
		
		for(String actionName : configurationSection.getKeys(false))
		{
			BaseAction action;
			try
			{
				action = ActionFactory.loadAction(configurationSection.getConfigurationSection(actionName), actionName);
			}
			catch(IllegalArgumentException e)
			{
				BUtil.logError(e.getMessage());
				continue;
			}
			
			actionDeque.add(action);
		}
	}

	public void updateNearbyPlayers()
	{
		Location bukkitLocation = mobLocation;
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(EntityHandler.isIgnoredPlayer(player.getName()))
			{
				continue; //Don't initialize while the player cannot see.
			}

			boolean inRange = true;

			Location playerLocation = player.getLocation();

			if( !player.isOnline() ||
				playerLocation.getWorld() != bukkitLocation.getWorld() ||
				//If mob is in view distance of the player, make sure to add this player to the nearbyPlayers collection
				bukkitLocation.distance(playerLocation) > viewDistance)
			{
				inRange = false;
			}

			//Create this for insertion and contains checks
			if(inRange)
			{
				//Mob is already spawned for player
				if(nearbyPlayers.contains(player))
				{
					continue;
				}
				
				nearbyPlayers.add(player);

				//Spawn in the mob
				spawnMob(player);
			}
			else
			{
				//Remove the boss if previously in the nearby players collection
				if(nearbyPlayers.contains(player))
				{
					PacketUtil.sendDestroyPacket(player, entityId);

					nearbyPlayers.remove(player);
				}

				//Otherwise, do nothing
			}
		}
	}

	public void spawnMob(Player player)
	{
		PacketUtil.sendSpawnPacket(player, this);
	}

	public void respawnMob()
	{
		despawn();

		RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
		{
			for(Player player : nearbyPlayers)
			{
				spawnMob(player);
			}
		}).runTaskLater(10);
	}
	
	public boolean isAtLocation(int chunkX, int chunkZ)
	{
		//Attempt to allow players to interact within 2 chunks of the mob
		//in-case of using them on chunk boundaries]
		//BUtil.logInfo("(" + this.chunkX + "-" + chunkX + "," + this.chunkZ + "-" + chunkZ + ") x " + Math.abs(this.chunkX - chunkX) + " | z " + Math.abs(this.chunkZ - chunkZ));
		return Math.abs(this.chunkX - chunkX) < 2 && Math.abs(this.chunkZ - chunkZ) < 2;
	}

	public void despawn()
	{
		for(Player player : nearbyPlayers)
		{
			PacketUtil.sendDestroyPacket(player, entityId);
		}
	}
	
	public void die()
	{
		try
		{
			despawn();
		}
		catch(NoClassDefFoundError e)
		{
			//Ignored during despawn.
			//If the player moves far enough away from this mob, it'll despawn anyway
			//and not respawn. This is a minor issue.
		}

		nearbyPlayers.clear();
		
		//Let the rest of the plugin know we've died.
		EntityHandler.removeEntity(entityId);
	}
	
	public void onAttack(Player player)
	{
		for(BaseAction action : attackActions)
		{
			action.doAction(player);
		}
	}
	
	public void onRightClick(Player player)
	{
		for(BaseAction action : interactActions)
		{
			action.doAction(player);
		}
	}

	public void onTick(int tick)
	{

	}

	/*
	 * API
	 */

	public void addAttackHandler(BaseAction action)
	{
		if(action == null)
		{
			throw new IllegalArgumentException("Action cannot be null!");
		}

		attackActions.add(action);
	}

	public void addInteractHandler(BaseAction action)
	{
		if(action == null)
		{
			throw new IllegalArgumentException("Action cannot be null!");
		}

		interactActions.add(action);
	}

}
