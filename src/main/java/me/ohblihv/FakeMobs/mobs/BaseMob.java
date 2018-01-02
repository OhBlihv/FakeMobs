package me.ohblihv.FakeMobs.mobs;

import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.RunnableShorthand;
import com.skytonia.SkyCore.util.file.FlatFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.management.MobManager;
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
import java.util.Random;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public abstract class BaseMob
{

	static final Random rand = new Random();

    //private CopyOnWriteArraySet<Player> nearbyPlayers = new CopyOnWriteArraySet<>();
	private ArrayDeque<Player> nearbyPlayers = new ArrayDeque<>();

	//private CopyOnWriteArrayList<Projectile> nearbyProjectiles = new CopyOnWriteArrayList<>();

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

	public BaseMob(int entityId, ConfigurationSection configurationSection)
	{
		this.mobLocation = FlatFile.getInstance().getLocation(configurationSection.getConfigurationSection("location"));
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
			boolean inRange = true;

			Location playerLocation = player.getLocation();

			if(playerLocation.getWorld() != bukkitLocation.getWorld())
			{
				inRange = false;
			}
			//If boss is in view distance of the player, make sure to add this player to the nearbyPlayers collection
			else if(bukkitLocation.distance(playerLocation) > viewDistance)
			{
				inRange = false;
			}

			//Create this for insertion and contains checks
			//CheapPlayer cheapPlayer = StaticNMS.getCheapPlayerFactoryInstance().getCheapPlayer(player);
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

	public Location getLocation()
	{
		return mobLocation;
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
		despawn();

		nearbyPlayers.clear();
		
		//Let the rest of the plugin know we've died.
		MobManager.removeMob(entityId);
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

}
