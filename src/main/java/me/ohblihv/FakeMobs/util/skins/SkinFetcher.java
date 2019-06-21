package me.ohblihv.FakeMobs.util.skins;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;
import com.skytonia.SkyCore.util.BUtil;
import lombok.Getter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.NPCEntity;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.UUID;

public class SkinFetcher implements Runnable
{

	public interface PostLoadRunnable
	{

		void run(GameProfile gameProfile);

	}

	public static Method MAKE_REQUEST;
	public static SkinThread SKIN_THREAD;
	static
	{
		try
		{
			MAKE_REQUEST = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class,
					Object.class, Class.class);
			MAKE_REQUEST.setAccessible(true);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		if (SKIN_THREAD == null)
		{
			Bukkit.getScheduler().runTaskTimer(FakeMobs.getInstance(), SKIN_THREAD = new SkinThread(), 10, 10);
		}
	}

	@Getter
	private final NPCEntity npc;
	private final String skinKey;

	private final YggdrasilAuthenticationService repo;
	private final String skinId;

	private boolean saveSkin = true;

	private final PostLoadRunnable postRunnable;

	public SkinFetcher(String skinId, YggdrasilAuthenticationService repo, NPCEntity npc)
	{
		//Update Skin
		this(npc.getSkinName(), skinId, repo, npc, (gameprofile) -> npc.respawnMob());
	}

	public SkinFetcher(String skinKey, String skinId, YggdrasilAuthenticationService repo, PostLoadRunnable runnable)
	{
		//Update Skin
		this(skinKey, skinId, repo, null, runnable);
	}

	public SkinFetcher(String skinUUID, YggdrasilAuthenticationService repo, PostLoadRunnable runnable, boolean saveSkin)
	{
		this(skinUUID, skinUUID, repo, null, runnable);

		this.saveSkin = saveSkin;
	}

	public SkinFetcher(String skinKey, String skinId, YggdrasilAuthenticationService repo, NPCEntity npc, PostLoadRunnable postRunnable)
	{
		this.skinKey = skinKey;
		this.skinId = skinId;
		this.repo = repo;
		this.npc = npc;
		this.postRunnable = postRunnable;
	}

	public void start()
	{
		SkinFetcher.SKIN_THREAD.addRunnable(this);
	}

	/*
	 * Yggdrasil's default implementation of this method silently fails instead of throwing an Exception like it should.
	 */
	private GameProfile fillProfileProperties(YggdrasilAuthenticationService auth, GameProfile profile, boolean requireSecure) throws Exception
	{
		URL url = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(profile.getId()));
		url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);

		BUtil.log("Querying as '" + url + "'.");

		MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse) MAKE_REQUEST.invoke(auth, url, null, MinecraftProfilePropertiesResponse.class);

		if (response == null)
		{
			BUtil.log("no response");
			return profile;
		}
		GameProfile result = new GameProfile(response.getId(), response.getName());
		result.getProperties().putAll(response.getProperties());
		profile.getProperties().putAll(response.getProperties());

		BUtil.log("Retrieved profile as " + response.getProperties().toString());

		return result;
	}

	@Override
	public void run()
	{
		GameProfile skinProfile;
		//Skin UUID or Skin Texture
		Property cached = SkinHandler.getSkinByUuid(skinKey);
		if (cached != null)
		{
			BUtil.log("Using cached skin texture for " + skinKey);

			skinProfile = new GameProfile(UUID.fromString(skinId), "");
			skinProfile.getProperties().put("textures", cached);
		}
		else
		{
			try
			{
				skinProfile = fillProfileProperties(repo, new GameProfile(UUID.fromString(skinId), ""), true);
			}
			catch (Exception e)
			{
				BUtil.log(e.getMessage());
				if ((e.getMessage() != null && e.getMessage().contains("too many requests")) || (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("too many requests")))
				{
					SKIN_THREAD.delay();
					SKIN_THREAD.addRunnable(this);
				}
				BUtil.log("Too many requests - delaying...");
				return;
			}

			if (skinProfile == null || !skinProfile.getProperties().containsKey("textures"))
			{
				return;
			}
			Property textures = Iterables.getFirst(skinProfile.getProperties().get("textures"), null);
			if (textures.getValue() == null || textures.getSignature() == null)
			{
				return;
			}

			if(npc != null)
			{
				BUtil.log("Fetched skin texture for UUID " + skinId + " for NPC " + npc.getPlayerListName() + " UUID " + npc.getProfile().getId());
			}
			else
			{
				BUtil.log("Fetched skin texture for Texture " + skinId);
			}

			if(saveSkin)
			{
				SkinHandler.addSkin(skinKey, new Property("textures", textures.getValue(), textures.getSignature()));
			}
		}

		postRunnable.run(skinProfile);
	}
}