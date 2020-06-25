package me.ohblihv.FakeMobs.mobs.nms;

import net.minecraft.server.v1_16_R1.EntityTypes;
import org.bukkit.entity.EntityType;

public class NMSMob_1_16 implements INMSMob
{

	@Override
	public float getEntityHeight(EntityType entityType)
	{
		return EntityTypes.a(entityType.name().toLowerCase()).get().l().height;
	}
}
