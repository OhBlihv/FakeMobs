package me.ohblihv.FakeMobs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.RunnableShorthand;
import lombok.Getter;
import me.ohblihv.FakeMobs.management.EntityListener;
import me.ohblihv.FakeMobs.management.MobManager;
import me.ohblihv.FakeMobs.management.PlayerListener;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class FakeMobs extends JavaPlugin implements Listener
{

	public static final String NPC_TEAM = "NPC_TEAM";
	
	@Getter
	private static FakeMobs instance = null;
	
	@Override
	public void onEnable()
	{
		instance = this;

		SkinHandler.load();

		EntityListener.init();
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		//Initialize mobs when all worlds are initialized
		RunnableShorthand.forPlugin(this).with(MobManager::init).runNextTick();

		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		try
		{
			MobManager.destruct();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			EntityListener.destruct();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			SkinHandler.save();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//

	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if(!player.isOp())
		{
			return;
		}

		if (event.getMessage().startsWith("/gettexture"))
		{
			event.setCancelled(true);

			ItemStack itemInHand = player.getItemInHand();
			if (itemInHand == null || itemInHand.getType() == Material.AIR)
			{
				player.sendMessage("§c§l(!) §cYou're not holding anything!");
				return;
			}

			if (itemInHand.getType() != Material.PLAYER_HEAD)
			{
				player.sendMessage("§c§l(!) §cYou must be holding a player head.");
				return;
			}

			SkullMeta skullMeta = (SkullMeta) itemInHand.getItemMeta();
			GameProfile profile = null;
			{
				try
				{
					Field field = Class.forName("org.bukkit.craftbukkit.v1_16_R1.inventory.CraftMetaSkull").getField("profile");
					field.setAccessible(true);

					profile = (GameProfile) field.get(skullMeta);
				}
				catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e)
				{
					e.printStackTrace();
					return;
				}
			}

			for(Property property : profile.getProperties().values())
			{
				if(property.getName().equals("textures"))
				{
					BUtil.log(property.getValue());
					player.sendMessage("§e§l(!) §eThe base64 of this skin has been printed to console.");
					break;
				}
			}
		}
	}

	private String skinName = null,
			targetSkinId = null;

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerCommand(ServerCommandEvent event)
	{
		final CommandSender sender = event.getSender();

		if(skinName != null)
		{
			if(event.getCommand().equalsIgnoreCase("yes"))
			{
				event.setCancelled(true);
				sender.sendMessage("§cYes Received, Overwriting old skin cache for '" + skinName + "'");

				String skinName = this.skinName,
					   targetSkinId = this.targetSkinId;

				this.skinName = null;
				this.targetSkinId = null;

				handleSkinCommand(sender, skinName, targetSkinId);
				return;
			}
			else //if(event.getCommand().equalsIgnoreCase("no"))
			{
				event.setCancelled(true);
				sender.sendMessage("§cNo or other command received. Cancelling skin request.");
			}

			skinName = null;
			targetSkinId = null;
			return;
		}

		if(event.getCommand().equals("fakemobs reload"))
		{
			MobManager.reload();
			sender.sendMessage("§e§l(!) §eReloaded Mobs.");
		}
		else if(event.getCommand().startsWith("skinsave"))
		{
			event.setCancelled(true);

			SkinHandler.save();
			sender.sendMessage("§c§l(!) §cSaved Skin Cache in textures.json");
		}
		else if(event.getCommand().startsWith("skinreload"))
		{
			event.setCancelled(true);

			SkinHandler.load();
			sender.sendMessage("§c§l(!) §cReloaded Skin Cache from textures.json");
		}
		else if(event.getCommand().startsWith("skinload"))
		{
			event.setCancelled(true);

			String[] args = event.getCommand().replace("skinload ", "").split("[ ]");
			if(args.length < 2)
			{
				sender.sendMessage("§c§l(!) §cskinload <skin-name> <target-skin-uuid>");
				return;
			}

			final String skinName = args[0],
						 targetSkinId = args[1];

			if(SkinHandler.getSkin(skinName) != null)
			{
				this.skinName = skinName;
				this.targetSkinId = targetSkinId;

				sender.sendMessage("§cThe skin id '" + skinName + "' is already used!");
				sender.sendMessage("§cTo Override, type 'yes'. To ignore, type 'no'");
				return;
			}

			handleSkinCommand(sender, skinName, targetSkinId);
		}
	}

	private void handleSkinCommand(CommandSender sender, String skinName, String targetSkinId)
	{
		new SkinFetcher(skinName, targetSkinId, PacketUtil.getAuthenticationService(),
			(gameProfile) ->
			{
				sender.sendMessage("§cUpdated '" + skinName + "' with signed textures in cache.");
			}).start();
	}
	
}
