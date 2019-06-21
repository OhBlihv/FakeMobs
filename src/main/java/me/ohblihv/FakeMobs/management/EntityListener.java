package me.ohblihv.FakeMobs.management;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.skytonia.SkyCore.util.BUtil;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Created by Chris Brown (OhBlihv) on 19/05/2016.
 */
public class EntityListener implements Listener
{
	
	private static ProtocolManager protocolManager;
	
	//Hey, Welcome to the dodgiest solution ever!
	private static boolean doublePacketToggle = false;

	public EntityListener(EntityHandler entityHandler)
	{
		protocolManager = ProtocolLibrary.getProtocolManager();

		protocolManager.addPacketListener(
				new PacketAdapter(FakeMobs.getInstance(), ListenerPriority.LOWEST, PacketType.Play.Client.USE_ENTITY)
		{
			
			@Override
			public void onPacketReceiving(PacketEvent event)
			{
				try
				{
					if(!entityHandler.isMobActive())
					{
						return;
					}
					
					PacketContainer packetContainer = event.getPacket();
					
					//Only handle boss IDs
					final StructureModifier<Integer> integers = packetContainer.getIntegers();

					int entityId;
					if(integers.size() == 0)
					{
						return;
					}

					try
					{
						//Attempt to avoid handling any off-hand interacts
						List<?> handValues;
						if(packetContainer.getHands().size() > 0 &&
							!(handValues = packetContainer.getHands().getValues()).isEmpty() &&
							handValues.get(0) == EnumWrappers.Hand.OFF_HAND)
						{
							return;
						}
					}
					catch(Exception e)
					{
						//Ignore.
					}
					
					entityId = packetContainer.getIntegers().read(0);
					BaseEntity baseEntity = entityHandler.getEntity(entityId);
					//if(!EntityHandler.isMobId(entityId))
					if(baseEntity == null)
					{
						return;
					}
					
					//Debug
					{
						Chunk playerAt = event.getPlayer().getLocation().getChunk();
						int chunkX = playerAt.getX(), chunkZ = playerAt.getZ();
						
						if(!baseEntity.isAtLocation(chunkX, chunkZ))
						{
							return;
						}
					}
					
					//Can't be cancelled/quit from here on in
					event.setCancelled(true);
					
					boolean isAttack;
					if(packetContainer.getEntityUseActions().size() > 0)
					{
						try
						{
							packetContainer.getEntityUseActions().getValues().isEmpty();
						}
						catch(NullPointerException e)
						{
							//Ignore.
							BUtil.log("Ignored second interact packet.");
							return;
						}
						
						isAttack = packetContainer.getEntityUseActions().getValues().get(0) == EnumWrappers.EntityUseAction.ATTACK;
					}
					else
					{
						isAttack = false;
					}
				
					/*
					 * Pretty much a flip-flop switch that denies one of the two duplicate
					 * packets that come with a right-click on an entity.
					 */
					if(!isAttack)
					{
						if(doublePacketToggle)
						{
							doublePacketToggle = false;
							return;
						}
						
						doublePacketToggle = true;
					}

					//Make sure any sub-action functions on the main thread as to not
					//cause more issues than it's worth.
					Bukkit.getScheduler().runTask(FakeMobs.getInstance(), () ->
					{
						if(isAttack)
						{
							baseEntity.onAttack(event.getPlayer());
						}
						else
						{
							baseEntity.onRightClick(event.getPlayer());
						}
					});
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			
		});
	}

	public static void destruct()
	{
		//
	}

	public static int getDamage(Player player)
	{
		ItemStack itemStack = player.getItemInHand();

		int damage = 2; //Default?

		if(itemStack == null || itemStack.getType() == Material.AIR)
		{
			return damage; //Cannot check the rest on an empty item
		}

		switch(itemStack.getType())
		{
			case WOOD_SWORD:
			case GOLD_SWORD:
				damage = 4; break;
			case STONE_SWORD: damage = 5; break;
			case IRON_SWORD: damage = 6; break;
			case DIAMOND_SWORD: damage = 7; break;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		//TODO: Consider other enchantments?
		if(itemMeta.hasEnchant(Enchantment.DAMAGE_ALL))
		{
			//As per the Sharpness definition
			//1 damage for the first level, 0.5 for every subsequent level
			damage += 1 + ((itemMeta.getEnchantLevel(Enchantment.DAMAGE_ALL) - 1) * 0.5D);
		}

		return damage;
	}

}
