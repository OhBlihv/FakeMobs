package me.ohblihv.FakeMobs.management;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

		/*protocolManager.addPacketListener(
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
					int entityId;
					if(packetContainer.getIntegers().size() == 0)
					{
						return;
					}
					
					entityId = packetContainer.getIntegers().read(0);
					BaseMob baseMob = MobManager.getMob(entityId);
					//if(!MobManager.isMobId(entityId))
					if(baseMob == null)
					{
						return;
					}
					
					//Debug
					{
						*//*Entity entity = protocolManager.getEntityFromID(event.getPlayer().getWorld(), entityId);
						if(entity != null)
						{
							BUtil.logError("FakeMob id '" + entityId + "' was blocking damage outside its region.");
							return;
						}*//*
						
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
					
					boolean isAttack = true;
					
					if(packetContainer.getEntityUseActions().size() > 0)
					{
						try
						{
							packetContainer.getEntityUseActions().getValues().isEmpty();
						}
						catch(NullPointerException e)
						{
							//Ignore.
							BUtil.logInfo("Ignored second interact packet.");
							return;
						}
						
						isAttack = packetContainer.getEntityUseActions().getValues().get(0) == EnumWrappers.EntityUseAction.ATTACK;
					}
				
				*//*
				 * Pretty much a flip-flop switch that denies one of the two duplicate
				 * packets that come with a right-click on an entity.
				 *//*
					if(!isAttack)
					{
						if(doublePacketToggle)
						{
							doublePacketToggle = false;
							return;
						}
						
						doublePacketToggle = true;
					}
					
					final boolean isAttackFinal = isAttack;
					//Make sure any sub-action functions on the main thread as to not
					//cause more issues than it's worth.
					Bukkit.getScheduler().runTask(FakeMobs.getInstance(), () ->
					{
						if(isAttackFinal)
						{
							baseMob.onAttack(event.getPlayer());
						}
						else
						{
							baseMob.onRightClick(event.getPlayer());
						}
					});
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			
		});*/

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
