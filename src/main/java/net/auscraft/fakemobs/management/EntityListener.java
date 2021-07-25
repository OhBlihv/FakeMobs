package net.auscraft.fakemobs.management;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import net.auscraft.fakemobs.FakeMobs;
import net.auscraft.fakemobs.mobs.IFakeMob;
import net.auscraft.skycore.util.BUtil;
import net.auscraft.skycore.util.RunnableShorthand;
import org.bukkit.Location;
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

	//Hey, Welcome to the dodgiest solution ever!
	private static boolean doublePacketToggle = false;

	public static void init()
	{
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

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
					if (!MobManager.isMobActive())
					{
						return;
					}

					PacketContainer packetContainer = event.getPacket();

					boolean isAttack;
					final StructureModifier<WrappedEnumEntityUseAction> useActionStructure = packetContainer.getEnumEntityUseActions();
					if (useActionStructure.size() > 0)
					{
						List<WrappedEnumEntityUseAction> useActions = useActionStructure.getValues();
						if (useActions == null || useActions.isEmpty())
						{
							return;
						}

						EnumWrappers.EntityUseAction useAction = useActions.get(0).getAction();
						if (useAction == EnumWrappers.EntityUseAction.INTERACT)
						{
							return;
						}

						isAttack = useActions.get(0).getAction() == EnumWrappers.EntityUseAction.ATTACK;
					}
					else
					{
						isAttack = false;
					}

					try
					{
						//Attempt to avoid handling any off-hand interacts
						StructureModifier<EnumWrappers.Hand> handStructure = packetContainer.getHands();
						if (handStructure.size() > 0)
						{
							List<?> handValues = handStructure.getValues();
							BUtil.log(handValues.toString());
							if (!handValues.isEmpty() && handValues.get(0) == EnumWrappers.Hand.OFF_HAND)
							{
								return;
							}
						}
					}
					catch (Exception e)
					{
						//Ignore.
					}

					int entityId = packetContainer.getIntegers().read(0);
					IFakeMob baseMob = MobManager.getMob(entityId);
					if (baseMob == null)
					{
						// Attempt to retrieve a delegate mob for this mob
						baseMob = MobManager.getDelegateMob(entityId);
						if (baseMob == null)
						{
							//BUtil.log("Could not find mob for id=" + entityId);
							return;
						}
					}

					//Debug
					{
						final Location playerAt = event.getPlayer().getLocation();
						int chunkX = (int) playerAt.getX() >> 4, chunkZ = (int) playerAt.getZ() >> 4;

						if (!baseMob.isAtLocation(chunkX, chunkZ))
						{
							//BUtil.logError("FakeMob id '" + entityId + "' was blocking damage outside its region.");
							return;
						}
					}

					//Can't be cancelled/quit from here on in
					event.setCancelled(true);

					/*
					 * Pretty much a flip-flop switch that denies one of the two duplicate
					 * packets that come with a right-click on an entity.
					 */
					if (!isAttack)
					{
						if (doublePacketToggle)
						{
							doublePacketToggle = false;
							return;
						}

						doublePacketToggle = true;
					}

					//Make sure any sub-action functions on the main thread as to not
					//cause more issues than it's worth.
					final IFakeMob baseMobFinal = baseMob;
					RunnableShorthand.forPlugin(FakeMobs.getInstance()).with(() ->
					{
						if (isAttack)
						{
							baseMobFinal.onAttack(event.getPlayer());
						}
						else
						{
							baseMobFinal.onRightClick(event.getPlayer());
						}
					}).ensureSync();
				} catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		});
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
