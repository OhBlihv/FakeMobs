package net.auscraft.fakemobs.mobs;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.server.v1_15_R1.VillagerProfession;
import net.minecraft.server.v1_15_R1.VillagerType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

public class VillagerMob extends SimpleMob
{

	private String villagerTypeString, villagerProfessionString;

	//private WrappedVillagerData.Type villagerType;
	private VillagerType villagerType;
	//private WrappedVillagerData.Profession villagerProfession;
	private VillagerProfession villagerProfession;

	private boolean isDancing = false,
					isBaby = false;

	public VillagerMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);

		setEntityType(EntityType.VINDICATOR);

		if(configurationSection.contains("options"))
		{
			isDancing = configurationSection.getBoolean("options.is-dancing", false);
			isBaby = configurationSection.getBoolean("options.is-baby", false);

			villagerTypeString = configurationSection.getString("options.villager-type", "DESERT").toUpperCase();
			//villagerType = WrappedVillagerData.Type.valueOf(villagerTypeString);

			/*switch(villagerTypeString)
			{
				case "JUNGLE": villagerTypeInt = 1; break;
				case "PLAINS": villagerTypeInt = 2; break;
				case "SAVANNA": villagerTypeInt = 3; break;
				case "SNOW": villagerTypeInt = 4; break;
				case "SWAMP": villagerTypeInt = 5; break;
				case "TAIGA": villagerTypeInt = 6; break;
				case "DESERT":
				default:
					villagerTypeString = "DESERT";
					villagerTypeInt = 0;
					break;
			}*/

			//villagerType = VillagerType.a(villagerTypeString.toLowerCase());

			//

			//villagerProfessionString = configurationSection.getString("options.villager-job", "NONE").toUpperCase();
			//villagerProfession = WrappedVillagerData.Profession.valueOf(villagerProfessionString);

			/*switch(villagerProfessionString)
			{
				case "ARMORER": villagerProfessionInt = 1; break;
				case "BUTCHER": villagerProfessionInt = 2; break;
				case "CARTOGRAPHER": villagerProfessionInt = 3; break;
				case "CLERIC": villagerProfessionInt = 4; break;
				case "FARMER": villagerProfessionInt = 5; break;
				case "FISHERMAN": villagerProfessionInt = 6; break;
				case "FLETCHER": villagerProfessionInt = 7; break;
				case "LEATHERWORKER": villagerProfessionInt = 8; break;
				case "LIBRARIAN": villagerProfessionInt = 9; break;
				case "MASON": villagerProfessionInt = 10; break;
				case "NITWIT": villagerProfessionInt = 11; break;
				case "SHEPHERD": villagerProfessionInt = 12; break;
				case "TOOLSMITH": villagerProfessionInt = 13; break;
				case "WEAPONSMITH": villagerProfessionInt = 14; break;
				case "NONE":
				default:
					villagerProfessionString = "NONE";
					villagerProfessionInt = 0;
					break;
			}*/

			/*try
			{
				villagerProfession = (VillagerProfession) VillagerProfession.class.getField(villagerProfessionString).get(null);
			}
			catch (NoSuchFieldException | IllegalAccessException e)
			{
				e.printStackTrace();
			}*/
		}
	}

	@Override
	public void setMetadata(WrappedDataWatcher watcher)
	{
		super.setMetadata(watcher);

		watcher.setObject(15, true);
	}
}
