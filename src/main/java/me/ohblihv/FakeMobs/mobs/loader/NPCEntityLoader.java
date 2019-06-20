package me.ohblihv.FakeMobs.mobs.loader;

import com.skytonia.SkyCore.util.BUtil;
import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.NPCEntity;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import me.ohblihv.FakeMobs.mobs.loader.exception.EntityLoaderException;
import me.ohblihv.FakeMobs.mobs.loader.meta.BaseEntityLoaderMeta;
import me.ohblihv.FakeMobs.mobs.loader.meta.NPCEntityLoaderMeta;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import static com.skytonia.SkyCore.items.construction.ItemContainerConstructor.buildItemContainer;

public class NPCEntityLoader extends BaseEntityLoader<NPCEntityLoaderMeta>
{

	@Override
	public NPCEntityLoaderMeta loadConfiguration(ConfigurationSection configurationSection) throws EntityLoaderException
	{
		//Generic NPC Information
		BaseEntityLoaderMeta entityLoaderMeta = super.loadConfiguration(configurationSection);

		String displayName = RandomStringUtils.randomAlphanumeric(10);
		NPCProfile profile = new NPCProfile(displayName);

		String skinUUID = null, skinName = null;
		{
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

				skinName = profile.getId().toString();
			}

			if(skinName == null)
			{
				skinName = displayName;
			}
		}

		ItemStack
			headItem = null,
			bodyItem = null,
			legsItem = null,
			feetItem = null,
			mainHandItem = null,
			offHandItem = null;
		if(configurationSection.contains("options.equipment"))
		{
			if(configurationSection.contains("options.equipment.head"))
			{
				headItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.head")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.body"))
			{
				bodyItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.body")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.legs"))
			{
				legsItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.legs")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.feet"))
			{
				feetItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.feet")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.main-hand"))
			{
				mainHandItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.main-hand")).toItemStack());
			}
			if(configurationSection.contains("options.equipment.off-hand"))
			{
				offHandItem = setUnbreakable(
					buildItemContainer(configurationSection.getConfigurationSection("options.equipment.off-hand")).toItemStack());
			}
		}

		return new NPCEntityLoaderMeta(EntityType.PLAYER, entityLoaderMeta.getLocation(), entityLoaderMeta.getInteractActions(),
			profile, skinUUID, skinName, headItem, bodyItem, legsItem, feetItem, mainHandItem, offHandItem);
	}

	@Override
	public BaseEntity createEntityContainer(NPCEntityLoaderMeta entityLoaderMeta)
	{
		//Trigger the DataWatcher cache for this entity type
		PacketUtil.getDefaultWatcher(entityLoaderMeta.getLocation().getWorld(), entityLoaderMeta.getEntityType());

		return new NPCEntity(entityLoaderMeta.getEntityId(), entityLoaderMeta.getLocation(),
			entityLoaderMeta.getInteractActions().get(InteractionType.LEFT_CLICK),
			entityLoaderMeta.getInteractActions().get(InteractionType.RIGHT_CLICK),
			entityLoaderMeta.getProfile(), entityLoaderMeta.getSkinUUID(), entityLoaderMeta.getSkinName(),
			entityLoaderMeta.getHeadItem(), entityLoaderMeta.getBodyItem(), entityLoaderMeta.getLegsItem(), entityLoaderMeta.getFeetItem(),
			entityLoaderMeta.getMainHandItem(), entityLoaderMeta.getOffHandItem());
	}

	private static ItemStack setUnbreakable(ItemStack itemStack)
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

}
