package me.ohblihv.FakeMobs;

import com.skytonia.SkyCore.util.RunnableShorthand;
import lombok.Getter;
import me.ohblihv.FakeMobs.management.EntityListener;
import me.ohblihv.FakeMobs.management.MobManager;
import me.ohblihv.FakeMobs.management.PlayerListener;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class FakeMobs extends JavaPlugin
{
	
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
	
}
