package me.ohblihv.FakeMobs.mobs.nms;

import com.skytonia.SkyCore.util.BUtil;
import org.bukkit.entity.EntityType;

public class NMSMob
{

	private static INMSMob nmsMobHandler = null;

	private static void initImpl()
	{
		switch(BUtil.getNMSVersion())
		{
			case "v1_14_R1": nmsMobHandler = new NMSMob_1_14(); break;
			case "v1_15_R1": nmsMobHandler = new NMSMob_1_15(); break;
			case "v1_16_R2": nmsMobHandler = new NMSMob_1_16(); break;
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
