package me.ohblihv.FakeMobs;

import com.skytonia.SkyCore.util.RunnableShorthand;
import lombok.Getter;
import me.ohblihv.FakeMobs.management.EntityListener;
import me.ohblihv.FakeMobs.management.EntityHandler;
import me.ohblihv.FakeMobs.management.PlayerListener;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class FakeMobs extends JavaPlugin implements Listener
{

	public static final String NPC_TEAM = "NPC_TEAM";
	
	@Getter
	private static FakeMobs instance = null;

	private EntityHandler entityHandler = null;
	
	@Override
	public void onEnable()
	{
		instance = this;

		SkinHandler.load();

		EntityListener.init();

		//Initialize entities when all worlds are initialized
		RunnableShorthand.forPlugin(this).with(() ->
		{
			this.entityHandler = new EntityHandler(FakeMobs.this);
			getServer().getPluginManager().registerEvents(new PlayerListener(FakeMobs.this.entityHandler), this);
		}).runNextTick();

		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		try
		{
			this.entityHandler.destruct();
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

	private String  skinName = null,
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

		if(event.getCommand().equalsIgnoreCase("fakemobs reload"))
		{
			this.entityHandler.reload();
			sender.sendMessage("§e§l(!) §eReloaded Mobs.");
		}
		else if(event.getCommand().startsWith("skinsave"))
		{
			event.setCancelled(true);

			SkinHandler.save();
			sender.sendMessage("§c§l(!) §cSaved Skin Cache in textures.json");
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
