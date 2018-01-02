package me.ohblihv.FakeMobs.util.skins;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.file.FlatFile;
import me.ohblihv.FakeMobs.FakeMobs;

import java.io.*;
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

		if(cfg.getSave().contains("skins"))
		{
			for(String skinName : cfg.getConfigurationSection("skins").getKeys(false))
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

		BUtil.log("Loaded " + textureCache.size() + " textures and " + idToNameCache.size() + " name associations from file");
	}

	public static void save()
	{
		File textureFile = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/textures.json");

		try(Writer writer = new FileWriter(textureFile))
		{
			gson.toJson(textureCache, ConcurrentHashMap.class, writer);
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
		return textureCache.get(idToNameCache.get(uuid));
	}

	public static String getUUIDForSkin(String skinName)
	{
		return idToNameCache.inverse().get(skinName);
	}

}
