package me.ohblihv.FakeMobs.mobs;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.skytonia.SkyCore.SkyCore;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.LocationUtil;
import com.skytonia.SkyCore.util.RunnableShorthand;
import com.skytonia.SkyCore.util.SupportedVersion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.management.MobManager;
import me.ohblihv.FakeMobs.mobs.actions.ActionFactory;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.nms.NMSMob;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.lib.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public abstract class BaseMob implements IFakeMob
{

	@Getter
    private final ArrayDeque<Player> nearbyPlayers = new ArrayDeque<>();

	@Getter
	private int viewDistance = 30;

	@Getter
	@Setter(AccessLevel.PROTECTED)
	private int entityId, nameEntityId;
	
	@Getter
	private EntityType entityType = EntityType.VILLAGER;
	private static final float DEFAULT_PLAYER_HEIGHT = 1.8f;
	@Getter
	private double mobHeight = DEFAULT_PLAYER_HEIGHT;
	void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
		mobHeight = NMSMob.getMobHeight(getEntityType());
		switch(entityType)
		{
			case ENDERMAN: mobHeight = 2.9; break;
			case IRON_GOLEM: mobHeight = 2.7; break;
			case SHEEP: case COW: case PIG: mobHeight = 1.25; break;
			case HORSE: mobHeight = 2.2; break;
			case CHICKEN: mobHeight = 1.0; break;
			case PLAYER: mobHeight = 1.8; break;
		}

		if(entityType != EntityType.PLAYER)
		{
			PacketUtil.getDefaultWatcher(Bukkit.getWorlds().get(0), entityType);
		}
	}

	//Used for judging player proximity
	@Getter private final Location  mobLocation;
	@Getter private final World     mobWorld;
	@Getter private final int       chunkX, chunkZ;

	@Getter private final String displayName;
	
	private final Deque<BaseAction> attackActions = new ArrayDeque<>(),
									interactActions = new ArrayDeque<>();

	private static int armourStandId;
	static
	{
		try
		{
			final String nmsPackage = "net.minecraft.server.v1_" + SkyCore.getCurrentVersion().getVersionNum() + "_R1.";

			Object entityRegistry = Class.forName(nmsPackage + "IRegistry").getDeclaredField("ENTITY_TYPE").get(null);
			// Citizens support
			if(entityRegistry.getClass().getSimpleName().equals("CustomEntityRegistry"))
			{
				Class<?> entityTypesClass =  Class.forName(nmsPackage + "EntityTypes");
				Field armourStandEntityTypeField = entityTypesClass.getDeclaredField("ARMOR_STAND");

				armourStandId = (int) entityRegistry.getClass().getDeclaredMethod("a", Object.class).invoke(entityRegistry, armourStandEntityTypeField.get(null));
			}
			else
			{
				Class<?> entityTypesClass =  Class.forName(nmsPackage + "EntityTypes");
				Field armourStandEntityTypeField = entityTypesClass.getDeclaredField("ARMOR_STAND");

				armourStandId = (int) entityRegistry.getClass().getDeclaredMethod("a", Object.class).invoke(entityRegistry, armourStandEntityTypeField.get(null));
			}

			BUtil.log("Loaded ArmorStand ID as " + armourStandId);
		}
		catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
	}

	public BaseMob(int entityId, ConfigurationSection configurationSection)
	{
		this.mobLocation = LocationUtil.getLocation(configurationSection.getConfigurationSection("location"));
		BUtil.log("Loaded at " + mobLocation);
		this.mobWorld = mobLocation.getWorld();
		Chunk chunk = this.mobLocation.getChunk();
		this.chunkX = chunk.getX();
		this.chunkZ = chunk.getZ();
		this.entityId = entityId;

		// Name for the name hologram
		this.nameEntityId = MobManager.getEntityId();
		this.displayName = BUtil.translateColours(configurationSection.getString("options.displayname", null));
		
		//Trigger the DataWatcher cache for this entity type
		PacketUtil.getDefaultWatcher(mobLocation.getWorld(), entityType);
		PacketUtil.getDefaultWatcher(mobLocation.getWorld(), EntityType.ARMOR_STAND);
		
		loadActions(configurationSection.getConfigurationSection("actions.LEFT_CLICK"), attackActions);
		loadActions(configurationSection.getConfigurationSection("actions.RIGHT_CLICK"), interactActions);
	}

	public List<Integer> getAllDelegateMobsIds()
	{
		return Arrays.asList(nameEntityId);
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
				BUtil.log(e.getMessage());
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
			if(MobManager.isIgnoredPlayer(player.getName()))
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
					onDespawn(player);

					PacketUtil.sendDestroyPacket(player, entityId);

					nearbyPlayers.remove(player);
				}

				//Otherwise, do nothing
			}
		}
	}

	public void spawnMob(Player player)
	{
		onSpawn(player);

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
			onDespawn(player);

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

	private final Map<String, LastLookDirection> currentlyLookingAt = new HashMap<>();
	@AllArgsConstructor
	private class LastLookDirection
	{

		//Default to invalid numbers
		public int yaw, pitch;

	}

	public void onTick(int tick)
	{
		Location currentLocation = getMobLocation();
		if(mobHeight != DEFAULT_PLAYER_HEIGHT)
		{
			currentLocation = currentLocation.clone().add(0, 0 - (DEFAULT_PLAYER_HEIGHT - mobHeight), 0);
		}

		for(Player player : getNearbyPlayers())
		{
			if(!player.isOnline())
			{
				continue; //Will be cleaned up later
			}

			Location playerLocation = player.getLocation();
			if(playerLocation == null)
			{
				continue; //Not initialized yet
			}

			if(playerLocation.getWorld() == getMobWorld() && playerLocation.distance(currentLocation) < 10)
			{
				//Look at player
				double dx = playerLocation.getX() - currentLocation.getX(),
					dy = playerLocation.getY() - (currentLocation.getY()),
					dz = playerLocation.getZ() - currentLocation.getZ();

				double var7 = (double) MathHelper.sqrt(dx * dx + dz * dz);
				float yaw   = (float) (MathHelper.b(dz, dx) * 180.0D / Math.PI) - 90.0F;
				float pitch = (float) (-(MathHelper.b(dy, var7) * 180.0D / Math.PI));

				LastLookDirection lastLookDirection = currentlyLookingAt.get(player.getName());
				if(lastLookDirection == null)
				{
					currentlyLookingAt.put(player.getName(), new LastLookDirection((int) yaw, (int) pitch));
				}
				else if(lastLookDirection.pitch == (int) pitch && lastLookDirection.yaw == (int) yaw)
				{
					continue; //Ignore look. Player has not moved enough.
				}
				else
				{
					lastLookDirection.pitch = (int) pitch;
					lastLookDirection.yaw = (int) yaw;
				}

				PacketUtil.sendLookPacket(player, yaw, pitch, getEntityId());

				WrapperPlayServerEntityHeadRotation headRotationPacket = new WrapperPlayServerEntityHeadRotation();

				headRotationPacket.setEntityID(getEntityId());
				headRotationPacket.setHeadYaw((byte) MathHelper.d(yaw * 256.0F / 360.0F));

				headRotationPacket.sendPacket(player);
			}
			else if(currentlyLookingAt.remove(player.getName()) != null)
			{
				//Reset location
				PacketUtil.sendLookPacket(player, currentLocation.getYaw(), currentLocation.getPitch(), getEntityId());

				WrapperPlayServerEntityHeadRotation headRotationPacket = new WrapperPlayServerEntityHeadRotation();

				headRotationPacket.setEntityID(getEntityId());
				headRotationPacket.setHeadYaw((byte) MathHelper.d(currentLocation.getYaw() * 256.0F / 360.0F));

				headRotationPacket.sendPacket(player);
			}
			/*else
			{
				BUtil.log("No action.");
			}*/
		}
	}

	//Facing Direction Helper
	private float a(float var1, float var2, float var3)
	{
		float var4 = MathHelper.g(var2 - var1);
		if (var4 > var3) {
			var4 = var3;
		}

		if (var4 < -var3) {
			var4 = -var3;
		}

		return var1 + var4;
	}

	/*
	 * API
	 */

	public void setMetadata(WrappedDataWatcher watcher)
	{
		// No fire/sprinting effects etc.
		watcher.setObject(0, (byte) 0);
	}

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

	public void onSpawn(Player player)
	{
		if(displayName != null && !displayName.isEmpty())
		{
			WrapperPlayServerSpawnEntityLiving spawnPacket = new WrapperPlayServerSpawnEntityLiving();

			//Set entity type id
			spawnPacket.getHandle().getIntegers().write(1, armourStandId);

			spawnPacket.setEntityID(nameEntityId);
			spawnPacket.setUniqueId(UUID.randomUUID());
			spawnPacket.setX(mobLocation.getX());
			spawnPacket.setY(mobLocation.getY() - 1.8 + mobHeight - 0.1);
			spawnPacket.setZ(mobLocation.getZ());

			WrappedDataWatcher watcher = PacketUtil.getDefaultWatcher(mobLocation.getWorld(), EntityType.ARMOR_STAND);

			// OptChat from 1.13+
			Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(displayName)[0].getHandle());
			watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);

			// Invisible
			watcher.setObject(0, (byte) 0x20);
			watcher.setObject(3, true);

			// 1.15 does not contain the metadata in the spawn packet.
			WrapperPlayServerEntityMetadata metadataPacket = null;
			if(SkyCore.getCurrentVersion().isAtLeast(SupportedVersion.ONE_FIFTEEN))
			{
				metadataPacket = new WrapperPlayServerEntityMetadata();
				metadataPacket.setMetadata(watcher.getWatchableObjects());
				metadataPacket.setEntityID(nameEntityId);
			}
			else
			{
				spawnPacket.setMetadata(watcher);
			}

			spawnPacket.sendPacket(player);

			if(metadataPacket != null)
			{
				metadataPacket.sendPacket(player);
			}
		}
	}

	public void onDespawn(Player player)
	{
		WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();

		destroyPacket.setEntityIds(new int[] {nameEntityId});

		destroyPacket.sendPacket(player);
	}

}
