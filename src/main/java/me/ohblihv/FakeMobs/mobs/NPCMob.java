package me.ohblihv.FakeMobs.mobs;

import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ohblihv.FakeMobs.npc.FakeEntityPlayer;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import net.minecraft.server.v1_8_R3.*;
import org.arkhamnetwork.Arkkit.patches.chunkgc.PlayerMoveTask;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NPCMob extends BaseMob
{

	private final Set<String> initializedPlayers = new HashSet<>();

	@Getter
	private final String displayName;

	@Getter
	private final String skinUUID;

	@Getter
	private final String skinName;

	@Getter
	private final NPCProfile profile;

	@Getter
	private final FakeEntityPlayer fakeEntityPlayer;

	public NPCMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);

		if(configurationSection.isString("options.displayname"))
		{
			displayName = BUtil.translateColours(configurationSection.getString("options.displayname"));
		}
		else
		{
			displayName = "NPC" + Math.abs(entityId);
		}

		profile = new NPCProfile(displayName);

		{
			String skinUUID = null;
			String skinName = null;
			if(configurationSection.isString("options.skin"))
			{
				skinName = configurationSection.getString("options.skin");
				skinUUID = SkinHandler.getUUIDForSkin(skinName);

				if(skinUUID == null)
				{
					BUtil.log("Skin '" + configurationSection.getString("options.skin", "default") + "' not found.");
				}
			}

			if(skinUUID == null)
			{
				BUtil.log("No Skin defined for " + displayName);

				skinName = getProfile().getId().toString();
			}

			if(skinName == null)
			{
				skinName = displayName;
			}

			this.skinName = skinName;
			this.skinUUID = skinUUID;
		}

		setEntityType(EntityType.PLAYER);

		WorldServer worldServer = ((CraftWorld) getMobLocation().getWorld()).getHandle();
		fakeEntityPlayer = new FakeEntityPlayer(MinecraftServer.getServer(), worldServer,
				profile, new PlayerInteractManager(worldServer));
		fakeEntityPlayer.setLocation(getMobLocation().getX(), getMobLocation().getY(), getMobLocation().getZ(),
				getMobLocation().getYaw(), getMobLocation().getPitch());

		this.setEntityId(fakeEntityPlayer.getId());
	}

	public boolean isPlayerInitialized(Player player)
	{
		return initializedPlayers.contains(player.getName());
	}

	public void addInitializedPlayer(Player player)
	{
		initializedPlayers.add(player.getName());
	}

	public void removeInitializedPlayer(Player player)
	{
		initializedPlayers.remove(player.getName());
	}

	@Override
	public void spawnMob(Player player)
	{
		Property cached = SkinHandler.getSkin(skinName);
		if (cached != null)
		{
			BUtil.log("Adding skin properties for " + profile.getId());
			profile.getProperties().put("textures", cached);

			BUtil.log("Comparing added properties with entity properties => " +
					(profile.getProperties().get("textures").equals(fakeEntityPlayer.getProfile().getProperties().get("textures"))));
		}
		else
		{
			BUtil.log("Retrieving skin for " + profile.getId());
			SkinFetcher.SKIN_THREAD.addRunnable(new SkinFetcher(skinUUID,
					((CraftWorld) getMobWorld()).getHandle().getMinecraftServer().aD(), this));
		}

		PacketUtil.sendPlayerSpawnPacket(player, this);
	}

	private final Map<String, LastLookDirection> currentlyLookingAt = new HashMap<>();
	@AllArgsConstructor
	private class LastLookDirection
	{

		//Default to invalid numbers
		public int yaw, pitch;

	}

	@Override
	public void onTick(int tick)
	{
		Location currentLocation = getMobLocation();

		for(Player player : getNearbyPlayers())
		{
			if(!player.isOnline())
			{
				continue; //Will be cleaned up later
			}

			Location playerLocation = PlayerMoveTask.async_player_locations.get(player.getName());
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

				PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
					getEntityId(), (byte) MathHelper.d(yaw * 256.0F / 360.0F), (byte) MathHelper.d(pitch * 256.0F / 360.0F), false
				);

				((CraftPlayer) player).getHandle().playerConnection.sendPacket(lookPacket);

				WrapperPlayServerEntityHeadRotation headRotationPacket = new WrapperPlayServerEntityHeadRotation();

				headRotationPacket.setEntityID(getEntityId());
				headRotationPacket.setHeadYaw((byte) MathHelper.d(yaw * 256.0F / 360.0F));

				headRotationPacket.sendPacket(player);

				//BUtil.log("Looking at player Yaw:" + yaw + " Pitch:" + pitch);
			}
			else if(currentlyLookingAt.remove(player.getName()) != null)
			{
				//BUtil.log("Not looking at player anymore");

				//Reset location
				PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
					getEntityId(),
					(byte) MathHelper.d(currentLocation.getYaw() * 256.0F / 360.0F),
					(byte) MathHelper.d(currentLocation.getPitch() * 256.0F / 360.0F), false
				);

				((CraftPlayer) player).getHandle().playerConnection.sendPacket(lookPacket);

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

}
