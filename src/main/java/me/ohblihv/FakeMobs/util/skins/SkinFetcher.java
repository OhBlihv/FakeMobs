package me.ohblihv.FakeMobs.util.skins;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.util.UUIDTypeAdapter;
import com.skytonia.SkyCore.util.BUtil;
import lombok.Getter;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;

public class SkinFetcher implements Runnable
{

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
	private final NPCMob npc;
	private final MinecraftSessionService repo;
	private final Callable<String> uuid;

	public SkinFetcher(Callable<String> uuid, MinecraftSessionService repo, NPCMob npc)
	{
		this.uuid = uuid;
		this.repo = repo;
		this.npc = npc;
	}

	/*
	 * Yggdrasil's default implementation of this method silently fails instead of throwing an Exception like it should.
	 */
	private GameProfile fillProfileProperties(YggdrasilAuthenticationService auth, GameProfile profile, boolean requireSecure) throws Exception
	{
		URL url = HttpAuthenticationService.constantURL(new StringBuilder().append("https://sessionserver.mojang.com/session/minecraft/profile/").append(UUIDTypeAdapter.fromUUID(profile.getId())).toString());
		url = HttpAuthenticationService.concatenateURL(url, new StringBuilder().append("unsigned=").append(!requireSecure).toString());
		MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse) MAKE_REQUEST.invoke(auth, url, null, MinecraftProfilePropertiesResponse.class);
		if (response == null)
		{
			return profile;
		}
		GameProfile result = new GameProfile(response.getId(), response.getName());
		result.getProperties().putAll(response.getProperties());
		profile.getProperties().putAll(response.getProperties());
		return result;
	}

	@Override
	public void run()
	{
		String realUUID;
		try
		{
			realUUID = uuid.call();
		}
		catch (Exception e)
		{
			return;
		}
		GameProfile skinProfile;
		Property cached = SkinHandler.getSkin(realUUID);
		if (cached != null)
		{
			BUtil.log("Using cached skin texture for NPC " + npc.getDisplayName() + " UUID " + npc.getProfile().getId());
			skinProfile = new GameProfile(UUID.fromString(realUUID), "");
			skinProfile.getProperties().put("textures", cached);
		}
		else
		{
			try
			{
				skinProfile = fillProfileProperties(((YggdrasilMinecraftSessionService) repo).getAuthenticationService(), new GameProfile(UUID.fromString(realUUID), ""), true);
			}
			catch (Exception e)
			{
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

			BUtil.log("Fetched skin texture for UUID " + realUUID + " for NPC " + npc.getDisplayName() + " UUID " + npc.getProfile().getId());
			SkinHandler.addSkin(realUUID, new Property("textures", textures.getValue(), textures.getSignature()));
		}

		//Update Skin
		npc.respawnMob();
	}
}