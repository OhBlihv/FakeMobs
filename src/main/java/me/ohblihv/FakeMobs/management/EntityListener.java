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
import me.ohblihv.FakeMobs.mobs.IFakeMob;
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

	//private static AsyncListenerHandler bossListener;
	
	private static ProtocolManager protocolManager;
	
	//Hey, Welcome to the dodgiest solution ever!
	private static boolean doublePacketToggle = false;

	public static void init()
	{
		protocolManager = ProtocolLibrary.getProtocolManager();

		/*protocolManager.addPacketListener(new PacketAdapter(FakeMobs.getInstance(), ListenerPriority.LOWEST,
			*//*PacketType.Play.Server.SPAWN_ENTITY,
			PacketType.Play.Server.SPAWN_ENTITY_LIVING,
			PacketType.Play.Server.ENTITY_METADATA,
			PacketType.Play.Server.NAMED_ENTITY_SPAWN*//*
			PacketType.Play.Server.ENTITY_METADATA
			)
		{
			@Override
			public void onPacketReceiving(PacketEvent event)
			{
				//
			}

			@Override
			public void onPacketSending(PacketEvent event)
			{
				BUtil.log("Sent " + event.getPacketType().name() + " -> Data ->");
				for(Field packetField : event.getPacket().getModifier().getFields())
				{
					try
					{
						packetField.setAccessible(true);

						final Object value = packetField.get(event.getPacket().getHandle());

						String result = "EMPTY";
						if(value instanceof Collection)
						{
							for(Object object : (Collection) value)
							{
								if(object instanceof DataWatcher.Item)
								{
									DataWatcher.Item item = ((DataWatcher.Item) object);

									result = item.a().a() + "->" + item.b() + (item.b() instanceof Integer ? " -- AS BITS(" + Integer.toBinaryString((Integer) item.b()) + ")" : "") + "\n";
								}
								else
								{
									result = value + "\n";
								}
							}

							if(result.endsWith("\n"))
							{
								result = result.substring(0, result.length() - 1);
							}
						}
						else
						{
							//Implitic null check
							result = value + "";
						}

						BUtil.log(packetField.getName() + "=" + result);
					}
					catch (IllegalAccessException e)
					{
						e.printStackTrace();
					}
				}
			}
		});*/

		protocolManager.addPacketListener(
				new PacketAdapter(FakeMobs.getInstance(), ListenerPriority.LOWEST, PacketType.Play.Client.USE_ENTITY)
		{
			
			@Override
			public void onPacketReceiving(PacketEvent event)
			{
				try
				{
					if(!MobManager.isMobActive())
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
					IFakeMob baseMob = MobManager.getMob(entityId);
					//if(!MobManager.isMobId(entityId))
					if(baseMob == null)
					{
						// Attempt to retrieve a delegate mob for this mob
						baseMob = MobManager.getDelegateMob(entityId);
						if(baseMob == null)
						{
							return;
						}
					}
					
					//Debug
					{
						/*Entity entity = protocolManager.getEntityFromID(event.getPlayer().getWorld(), entityId);
						if(entity != null)
						{
							BUtil.logError("FakeMob id '" + entityId + "' was blocking damage outside its region.");
							return;
						}*/
						
						//Location entityLocation = event.getPlayer().getLocation();
						Chunk playerAt = event.getPlayer().getLocation().getChunk();
						int chunkX = playerAt.getX(), chunkZ = playerAt.getZ();
						
						//if(!(chunkX == 25 && chunkZ == -2) || !(chunkX == 25 && chunkZ == -1))
						if(!baseMob.isAtLocation(chunkX, chunkZ))
						{
							//BUtil.logError("FakeMob id '" + entityId + "' was blocking damage outside its region.");
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
					final IFakeMob baseMobFinal = baseMob;
					Bukkit.getScheduler().runTask(FakeMobs.getInstance(), () ->
					{
						if(isAttack)
						{
							baseMobFinal.onAttack(event.getPlayer());
						}
						else
						{
							baseMobFinal.onRightClick(event.getPlayer());
						}
					});
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			
		});

		//bossListener.start();
	}

	public static void destruct()
	{
		//bossListener.cancel();
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
			case WOODEN_SWORD:
			case GOLDEN_SWORD:
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

	/*
	 * Bukkit Listeners
	 */

	/*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		if(!MobManager.isMobActive() || event.getEntity() == null ||
				   !(event.getEntity() instanceof Arrow) || !(event.getEntity().getShooter() instanceof Player))
		{
			return;
		}

		Projectile projectile = event.getEntity();
		Location projectileLocation = projectile.getLocation();

		//Check proximity to boss, see if its possible
		for(BaseBoss baseBoss : MobManager.mobMap.values())
		{
			Location bossLocation = baseBoss.getLocation();

			if(bossLocation.getWorld().getEnvironment() != projectileLocation.getWorld().getEnvironment() ||
					   bossLocation.distance(projectileLocation) > baseBoss.getViewDistance())
			{
				continue;
			}

			BUtil.logInfo("Adding Projectile");
			baseBoss.addNearbyProjectile(projectile);
		}
	}*/


}
