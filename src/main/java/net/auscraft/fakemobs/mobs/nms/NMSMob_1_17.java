package net.auscraft.fakemobs.mobs.nms;

import net.minecraft.world.entity.EntityTypes;
import org.bukkit.entity.EntityType;

public class NMSMob_1_17 implements INMSMob
{

	@Override
	public float getEntityHeight(EntityType entityType)
	{
		return EntityTypes.a(entityType.name().toLowerCase()).get().l();
	}
}
