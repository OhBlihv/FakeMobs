package net.auscraft.fakemobs.mobs.nms;

import net.auscraft.skycore.util.BUtil;
import org.bukkit.entity.EntityType;

public class NMSMob
{

	private static INMSMob nmsMobHandler = null;

	private static void initImpl()
	{
		switch(BUtil.getNMSVersion())
		{
			case "v1_17_R1": nmsMobHandler = new NMSMob_1_17(); break;
			default:
				throw new IllegalArgumentException("Unsupported NMS Version " + BUtil.getNMSVersion());
		}
	}

	public static float getMobHeight(EntityType entityType)
	{
		initImpl();

		return nmsMobHandler.getEntityHeight(entityType);
	}

}
