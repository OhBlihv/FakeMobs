package me.ohblihv.FakeMobs.mobs;

import com.skytonia.SkyCore.util.RunnableShorthand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public abstract class BaseEntity
{

	@Getter
    private final Set<Player> nearbyPlayers = new HashSet<>();

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
	@Getter private final Location entityLocation;
	@Getter private final World     mobWorld;
	@Getter private final int       chunkX, chunkZ;
	
	private final Deque<BaseAction> leftClickActions, rightClickActions;

	public BaseEntity(int entityId, Location entityLocation, Deque<BaseAction> leftClickActions, Deque<BaseAction> rightClickActions)
	{
		this.entityLocation = entityLocation;
		this.mobWorld = entityLocation.getWorld();

		Chunk chunk = this.entityLocation.getChunk();
		this.chunkX = chunk.getX();
		this.chunkZ = chunk.getZ();
		this.entityId = entityId;

		if(leftClickActions == null)
		{
			leftClickActions = new ArrayDeque<>();
		}

		if(rightClickActions == null)
		{
			rightClickActions = new ArrayDeque<>();
		}

		this.leftClickActions = leftClickActions;
		this.rightClickActions = rightClickActions;
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
	
	public void remove()
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
	}
	
	public void onAttack(Player player)
	{
		for(BaseAction action : leftClickActions)
		{
			action.doAction(player);
		}
	}
	
	public void onRightClick(Player player)
	{
		for(BaseAction action : rightClickActions)
		{
			action.doAction(player);
		}
	}

	public void onTick(int tick)
	{
		//
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

		leftClickActions.add(action);
	}

	public void addInteractHandler(BaseAction action)
	{
		if(action == null)
		{
			throw new IllegalArgumentException("Action cannot be null!");
		}

		rightClickActions.add(action);
	}

}
