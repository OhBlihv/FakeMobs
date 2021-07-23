package net.auscraft.fakemobs.mobs;

import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.auscraft.fakemobs.npc.NPCProfile;
import net.auscraft.fakemobs.npc.fakeplayer.FakeEntityPlayer;
import net.auscraft.fakemobs.util.PacketUtil;
import net.auscraft.fakemobs.util.skins.SkinHandler;
import net.auscraft.skycore.items.construction.ItemContainerConstructor;
import net.auscraft.skycore.util.BUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NPCMob extends BaseMob
{

	private final Set<String> initializedPlayers = new HashSet<>();

	@Getter
	private final String npcName;

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

		//npcName = RandomStringUtils.randomAlphanumeric(10);
		npcName = getDisplayName();

		profile = new NPCProfile(npcName);

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
				BUtil.log("No Skin defined for " + npcName);

				skinName = getProfile().getId().toString();
			}

			if(skinName == null)
			{
				skinName = npcName;
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

		itemMeta.setUnbreakable(true);

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

		onSpawn(player);
	}

}
