package me.ohblihv.FakeMobs.mobs;

import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.items.construction.ItemContainerConstructor;
import com.skytonia.SkyCore.util.BUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.lib.MathHelper;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

	@Getter
	private ItemStack
		headItem = null,
		bodyItem = null,
		legsItem = null,
		feetItem = null,
		mainHandItem = null,
		offHandItem = null;

	public NPCMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);

		displayName = RandomStringUtils.randomAlphanumeric(10);

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
					if(SkinHandler.getSkin(skinName) == null)
					{
						BUtil.log("Skin '" + configurationSection.getString("options.skin", "default") + "' not found.");
					}
					else
					{
						//Skin does not have an associated player/UUID. Generate.
						skinUUID = UUID.randomUUID().toString();
					}
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

		fakeEntityPlayer = PacketUtil.getFakeEntityPlayer(getMobLocation().getWorld(), profile);
		fakeEntityPlayer.setLocation(getMobLocation().getX(), getMobLocation().getY(), getMobLocation().getZ(),
				getMobLocation().getYaw(), getMobLocation().getPitch());

		if(configurationSection.contains("options.equipment"))
		{
			if(configurationSection.contains("options.equipment.head"))
			{
				headItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.head")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.body"))
			{
				bodyItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.body")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.legs"))
			{
				legsItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.legs")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.feet"))
			{
				feetItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.feet")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.main-hand"))
			{
				mainHandItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.main-hand")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.off-hand"))
			{
				offHandItem = setUnbreakable(
					ItemContainerConstructor.buildItemContainer(configurationSection.getConfigurationSection("options.equipment.off-hand")).toItemStack());
			}
		}

		this.setEntityId(fakeEntityPlayer.getId());
	}

	private ItemStack setUnbreakable(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return itemStack;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		if(itemMeta == null)
		{
			return itemStack;
		}

		itemMeta.spigot().setUnbreakable(true);

		itemStack.setItemMeta(itemMeta);

		return itemStack;
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
		Property cached = SkinHandler.getSkinByUuid(skinName);
		if (cached != null)
		{
			profile.getProperties().put("textures", cached);
		}
		else
		{
			BUtil.log("Retrieving skin for " + profile.getId());

			PacketUtil.initializeSkin(skinUUID, this, getMobWorld());
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

}
