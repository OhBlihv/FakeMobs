package me.ohblihv.FakeMobs.util.skins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import me.ohblihv.FakeMobs.FakeMobs;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinHandler
{

	private static Map<String, Property> textureCache = new ConcurrentHashMap<>();
	private static Map<String, String> nameToIdCache = new ConcurrentHashMap<>();
	static
	{
		gson = new GsonBuilder().setPrettyPrinting().create();

		load();
	}

	private static final Gson gson;

	public static void load()
	{
		File textureFile = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/textures.json");
		if(textureFile.exists())
		{
			try(JsonReader jsonReader = new JsonReader(new FileReader(textureFile)))
			{
				textureCache = gson.fromJson(jsonReader, ConcurrentHashMap.class);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		File namesFile = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/names.json");
		if(namesFile.exists())
		{
			try(JsonReader jsonReader = new JsonReader(new FileReader(namesFile)))
			{
				nameToIdCache = gson.fromJson(jsonReader, ConcurrentHashMap.class);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		BUtil.log("Loaded " + textureCache.size() + " textures and " + nameToIdCache.size() + " name associations from file");
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

		File nameToId = new File(FakeMobs.getInstance().getDataFolder().getAbsolutePath() + "/names.json");

		try(Writer writer = new FileWriter(nameToId))
		{
			gson.toJson(nameToId, ConcurrentHashMap.class, writer);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Property getSkin(String uuid)
	{
		return textureCache.get(uuid);
	}

	public static void addSkin(String uuid, Property property)
	{
		textureCache.put(uuid, property);
	}

	public static void addSkinForName(String skinName, String skinUuid)
	{
		nameToIdCache.put(skinName, skinUuid);
	}

	public static Property getSkinByName(String skinName)
	{
		return textureCache.get(nameToIdCache.get(skinName));
	}

}
