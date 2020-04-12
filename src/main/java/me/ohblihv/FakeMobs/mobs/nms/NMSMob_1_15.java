package me.ohblihv.FakeMobs.mobs.nms;

import net.minecraft.server.v1_15_R1.EntityTypes;
import org.bukkit.entity.EntityType;

public class NMSMob_1_15 implements INMSMob
{

	@Override
	public float getEntityHeight(EntityType entityType)
	{
		return EntityTypes.a(entityType.name().toLowerCase()).get().k().height;
	}
}
