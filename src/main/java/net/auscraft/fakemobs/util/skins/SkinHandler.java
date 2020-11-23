package net.auscraft.fakemobs.util.skins;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.properties.Property;
import net.auscraft.fakemobs.FakeMobs;
import net.auscraft.skycore.util.BUtil;
import net.auscraft.skycore.util.file.FlatFile;
import org.bukkit.configuration.ConfigurationSection;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinHandler
{

	//SkinName -> Skin Textures (Property)
	private static Map<String, Property> textureCache = null;

	//UUID -> Name
	private static HashBiMap<String, String> idToNameCache = HashBiMap.create();

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void load()
	{
		if(textureCache != null)
		{
			BUtil.log("Aborting SkinHandler load. Already loaded.");
			return;
		}

		FlatFile cfg = FlatFile.getInstance();

		Map<String, Property> loadedConfigTextures = new HashMap<>();
		if(cfg.getSave().contains("skins") && cfg.getSave().isConfigurationSection("skins"))
		{
			ConfigurationSection skinSection = cfg.getConfigurationSection("skins");
			for(String skinName : skinSection.getKeys(false))
			{
				if(skinSection.isConfigurationSection(skinName))
				{
					ConfigurationSection innerSkinSection = skinSection.getConfigurationSection(skinName);
					if(!innerSkinSection.isString("texture") || !innerSkinSection.isString("signature"))
					{
						BUtil.log("Missing either 'texture' or 'signature' section inside skin configuration for '" + skinName + "'");
						continue;
					}

					Property property = new Property(
						"textures",
						innerSkinSection.getString("texture"),
						innerSkinSection.getString("signature")
					);

					loadedConfigTextures.put(skinName, property);
					BUtil.log("Loaded config texture for '" + skinName + "'");
				}
				else if(skinSection.isString(skinName))
				{
					String associatedUUID = cfg.getConfigurationSection("skins").getString(skinName);
					if(associatedUUID == null)
					{
						BUtil.log("Bad association for Skin Name '" + skinName + "'");
						continue;
					}

					BUtil.log("Loaded skin association '" + associatedUUID + "'->'" + skinName + "'");
					idToNameCache.put(associatedUUID, skinName);
				}
				else
				{
					BUtil.log("Bad Configuration for Skin '" + skinName + "'. Ignored.");
				}
			}

			BUtil.log("Loaded " + idToNameCache.size() + " skin associations.");
		}
		else
		{
			BUtil.log("Unable to read skin names from config. 'skins' section does not exist!");
		}

		File textureFile = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/textures.json");
		if(textureFile.exists())
		{
			try(JsonReader jsonReader = new JsonReader(new FileReader(textureFile)))
			{
				textureCache = gson.fromJson(jsonReader,
					new TypeToken<ConcurrentHashMap<String, Property>>(){}.getType());
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		if(textureCache == null)
		{
			textureCache = new ConcurrentHashMap<>();
		}

		textureCache.putAll(loadedConfigTextures);

		BUtil.log(textureCache.keySet().toString());

		BUtil.log("Loaded " + textureCache.size() + " textures and " + idToNameCache.size() + " name associations from file");
	}

	public static void save()
	{
		File textureFile = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/textures.json");

		try(Writer writer = new FileWriter(textureFile))
		{
			gson.toJson(textureCache, ConcurrentHashMap.class, writer);
			BUtil.log("Saved textures.json at '" + textureFile.getAbsolutePath() + "'");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Property getSkin(String skinName)
	{
		return textureCache.get(skinName);
	}

	public static void addSkin(String skinName, Property property)
	{
		BUtil.log("Adding reference for skin name " + skinName + "=>" + property);
		textureCache.put(skinName, property);
	}

	public static void addSkinForUuid(String skinUuid, String skinName)
	{
		idToNameCache.put(skinUuid, skinName);
	}

	public static Property getSkinByUuid(String uuid)
	{
		String name = idToNameCache.get(uuid);

		if(name == null)
		{
			return textureCache.get(uuid);
		}

		return textureCache.get(name);
	}

	public static String getUUIDForSkin(String skinName)
	{
		return idToNameCache.inverse().get(skinName);
	}

}
