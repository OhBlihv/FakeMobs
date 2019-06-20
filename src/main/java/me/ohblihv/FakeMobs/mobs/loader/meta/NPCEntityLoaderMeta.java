package me.ohblihv.FakeMobs.mobs.loader.meta;

import lombok.Getter;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Deque;
import java.util.Map;

public class NPCEntityLoaderMeta extends BaseEntityLoaderMeta
{

	@Getter
	private final NPCProfile profile;

	@Getter
	private final String skinUUID, skinName;

	@Getter
	private final ItemStack headItem, bodyItem, legsItem, feetItem,
							mainHandItem, offHandItem;

	public NPCEntityLoaderMeta(EntityType entityType, Location entityLocation,
	                           Map<InteractionType, Deque<BaseAction>> interactActions,
	                           NPCProfile profile, String skinUUID, String skinName,
	                           ItemStack headItem, ItemStack bodyItem, ItemStack legsItem, ItemStack feetItem,
	                           ItemStack mainHandItem, ItemStack offHandItem)
	{
		super(entityType, entityLocation, interactActions);

		this.profile = profile;
		this.skinUUID = skinUUID;
		this.skinName = skinName;

		this.headItem = headItem;
		this.bodyItem = bodyItem;
		this.legsItem = legsItem;
		this.feetItem = feetItem;
		this.mainHandItem = mainHandItem;
		this.offHandItem = offHandItem;
	}
}
