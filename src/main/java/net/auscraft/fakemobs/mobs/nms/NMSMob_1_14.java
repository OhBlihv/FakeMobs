package net.auscraft.fakemobs.mobs.nms;

import net.minecraft.server.v1_14_R1.EntityTypes;
import org.bukkit.entity.EntityType;

public class NMSMob_1_14 implements INMSMob
{

	@Override
	public float getEntityHeight(EntityType entityType)
	{
		return EntityTypes.a(entityType.name().toLowerCase()).get().i();
	}
}
