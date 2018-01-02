package me.ohblihv.FakeMobs.mobs.actions;

import com.skytonia.SkyCore.items.construction.ItemContainer;
import com.skytonia.SkyCore.items.construction.ItemContainerConstructor;
import com.skytonia.SkyCore.util.BUtil;
import com.skytonia.SkyCore.util.file.FlatFile;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public class RewardAction extends BaseAction
{
	
	@RequiredArgsConstructor
	private class RewardObj
	{
		
		private final double chance;
		
		private final ItemContainer itemContainer;
		
	}
	
	private final Deque<RewardObj> rewardDeque = new ArrayDeque<>();
	
	public RewardAction(ConfigurationSection configurationSection)
	{
		for(Map<String, Object> configurationMap : FlatFile.getInstance().getListMap(configurationSection.getCurrentPath() + ".rewards"))
		{
			rewardDeque.add(new RewardObj((Double) configurationMap.get("chance"), ItemContainerConstructor.buildItemContainer(configurationMap)));
		}
		
		BUtil.logInfo("Loaded " + rewardDeque.size() + " rewards.");
	}
	
	@Override
	public void doAction(Player player)
	{
		//Get this number to 100%
		double rolledNumber = random.nextDouble() * 100D;
		//BUtil.logInfo("Rolled " + rolledNumber);
		
		for(RewardObj reward : rewardDeque)
		{
			/*if(reward.itemContainer.getMaterial() != Material.PORK)
			{
				BUtil.logInfo("Need " + reward.chance + " for " + reward.itemContainer.getMaterial());
			}*/
			
			if(rolledNumber < reward.chance)
			{
				PlayerInventory playerInventory = player.getInventory();
				
				//Ignore any items that do not fit in the inventory
				playerInventory.addItem(reward.itemContainer.toItemStack(player.getName()));
				return;
			}
		}
	}
	
}
