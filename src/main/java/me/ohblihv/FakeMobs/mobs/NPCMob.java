package me.ohblihv.FakeMobs.mobs;

import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import lombok.Getter;
import me.ohblihv.FakeMobs.npc.FakeEntityPlayer;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class NPCMob extends BaseMob
{

	private final Set<String> initializedPlayers = new HashSet<>();

	@Getter
	private final String displayName;

	@Getter
	private final NPCProfile profile;

	@Getter
	private final FakeEntityPlayer fakeEntityPlayer;

	public NPCMob(int entityId, ConfigurationSection configurationSection)
	{
		super(entityId, configurationSection);

		if(configurationSection.isString("options.displayname"))
		{
			displayName = BUtil.translateColours(configurationSection.getString("options.displayname"));
		}
		else
		{
			displayName = "NPC" + Math.abs(entityId);
		}

		profile = new NPCProfile(displayName);

		setEntityType(EntityType.PLAYER);

		WorldServer worldServer = ((CraftWorld) getMobLocation().getWorld()).getHandle();
		fakeEntityPlayer = new FakeEntityPlayer(MinecraftServer.getServer(), worldServer,
				profile, new PlayerInteractManager(worldServer));
		fakeEntityPlayer.setLocation(getMobLocation().getX(), getMobLocation().getY(), getMobLocation().getZ(),
				getMobLocation().getYaw(), getMobLocation().getPitch());

		this.setEntityId(fakeEntityPlayer.getId());
	}

	public boolean isPlayerInitialized(Player player)
	{
		return initializedPlayers.contains(player.getName());
	}

	public void addInitializedPlayer(Player player)
	{
		initializedPlayers.add(player.getName());
	}

	public void removeInitializedPlayer(Player player)
	{
		initializedPlayers.remove(player.getName());
	}

	@Override
	public void spawnMob(Player player)
	{
		Property cached = SkinHandler.getSkin(profile.getId().toString());
		if (cached != null)
		{
			BUtil.log("Adding skin properties for " + profile.getId());
			profile.getProperties().put("textures", cached);

			BUtil.log("Comparing added properties with entity properties => " +
					(profile.getProperties().get("textures").equals(fakeEntityPlayer.getProfile().getProperties().get("textures"))));
		}
		else
		{
			BUtil.log("Retrieving skin for " + profile.getId());
			SkinFetcher.SKIN_THREAD.addRunnable(new SkinFetcher(() -> profile.getId().toString(),
					((CraftWorld) getMobWorld()).getHandle().getMinecraftServer().aD(), this));
		}

		PacketUtil.sendPlayerSpawnPacket(player, this);
	}

}
