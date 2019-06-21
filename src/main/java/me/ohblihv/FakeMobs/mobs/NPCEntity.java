package me.ohblihv.FakeMobs.mobs;

import com.comphenix.packetwrapper.WrapperPlayServerEntityHeadRotation;
import com.mojang.authlib.properties.Property;
import com.skytonia.SkyCore.util.BUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.npc.NPCProfile;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer;
import me.ohblihv.FakeMobs.util.PacketUtil;
import me.ohblihv.FakeMobs.util.lib.MathHelper;
import me.ohblihv.FakeMobs.util.skins.SkinHandler;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NPCEntity extends BaseEntity
{

	private final Set<String> initializedPlayers = new HashSet<>();

	@Getter
	private final String skinUUID, skinName;

	@Getter
	private final NPCProfile profile;

	@Getter
	private final FakeEntityPlayer fakeEntityPlayer;

	@Getter
	private ItemStack headItem, bodyItem, legsItem, feetItem,
						mainHandItem, offHandItem;

	public NPCEntity(int entityId, Location entityLocation, Deque<BaseAction> leftClickActions, Deque<BaseAction> rightClickActions,
	                 NPCProfile npcProfile, String skinUUID, String skinName,
	                 ItemStack headItem, ItemStack bodyItem, ItemStack legsItem, ItemStack feetItem,
	                 ItemStack mainHandItem, ItemStack offHandItem)
	{
		super(entityId, entityLocation, null, null);

		this.skinUUID = skinUUID;
		this.skinName = skinName;
		this.profile = npcProfile;

		fakeEntityPlayer = PacketUtil.getFakeEntityPlayer(getEntityLocation().getWorld(), npcProfile);
		fakeEntityPlayer.setLocation(getEntityLocation().getX(), getEntityLocation().getY(), getEntityLocation().getZ(),
			getEntityLocation().getYaw(), getEntityLocation().getPitch());

		this.headItem = headItem;
		this.bodyItem = bodyItem;
		this.legsItem = legsItem;
		this.feetItem = feetItem;
		this.mainHandItem = mainHandItem;
		this.offHandItem = offHandItem;

		setEntityType(EntityType.PLAYER);
		setEntityId(fakeEntityPlayer.getId());
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
		Property cached = SkinHandler.getSkinByUuid(skinName);
		if (cached != null)
		{
			profile.getProperties().put("textures", cached);
		}
		else
		{
			BUtil.log("Retrieving skin for " + profile.getId());

			PacketUtil.initializeSkin(skinUUID, this, getMobWorld());
		}

		PacketUtil.sendPlayerSpawnPacket(player, this);
	}

	public String getPlayerListName()
	{
		return profile.getName();
	}

	private final Map<String, LastLookDirection> currentlyLookingAt = new HashMap<>();
	@AllArgsConstructor
	private class LastLookDirection
	{

		//Default to invalid numbers
		public int yaw, pitch;

	}

	@Override
	public void onTick(int tick)
	{
		Location currentLocation = getEntityLocation();

		for(Player player : getNearbyPlayers())
		{
			if(!player.isOnline())
			{
				continue; //Will be cleaned up later
			}

			Location playerLocation = player.getLocation();
			if(playerLocation == null)
			{
				continue; //Not initialized yet
			}

			if(playerLocation.getWorld() == getMobWorld() && playerLocation.distance(currentLocation) < 10)
			{
				//Look at player
				double dx = playerLocation.getX() - currentLocation.getX(),
					   dy = playerLocation.getY() - (currentLocation.getY()),
					   dz = playerLocation.getZ() - currentLocation.getZ();

				double var7 = (double) MathHelper.sqrt(dx * dx + dz * dz);
				float yaw   = (float) (MathHelper.b(dz, dx) * 180.0D / Math.PI) - 90.0F;
				float pitch = (float) (-(MathHelper.b(dy, var7) * 180.0D / Math.PI));

				LastLookDirection lastLookDirection = currentlyLookingAt.get(player.getName());
				if(lastLookDirection == null)
				{
					currentlyLookingAt.put(player.getName(), new LastLookDirection((int) yaw, (int) pitch));
				}
				else if(lastLookDirection.pitch == (int) pitch && lastLookDirection.yaw == (int) yaw)
				{
					continue; //Ignore look. Player has not moved enough.
				}
				else
				{
					lastLookDirection.pitch = (int) pitch;
					lastLookDirection.yaw = (int) yaw;
				}

				PacketUtil.sendLookPacket(player, yaw, pitch, getEntityId());

				WrapperPlayServerEntityHeadRotation headRotationPacket = new WrapperPlayServerEntityHeadRotation();

				headRotationPacket.setEntityID(getEntityId());
				headRotationPacket.setHeadYaw((byte) MathHelper.d(yaw * 256.0F / 360.0F));

				headRotationPacket.sendPacket(player);
			}
			else if(currentlyLookingAt.remove(player.getName()) != null)
			{
				//Reset location
				PacketUtil.sendLookPacket(player, currentLocation.getYaw(), currentLocation.getPitch(), getEntityId());

				WrapperPlayServerEntityHeadRotation headRotationPacket = new WrapperPlayServerEntityHeadRotation();

				headRotationPacket.setEntityID(getEntityId());
				headRotationPacket.setHeadYaw((byte) MathHelper.d(currentLocation.getYaw() * 256.0F / 360.0F));

				headRotationPacket.sendPacket(player);
			}
			/*else
			{
				BUtil.log("No action.");
			}*/
		}
	}

	//Facing Direction Helper
	private float a(float var1, float var2, float var3)
	{
		float var4 = MathHelper.g(var2 - var1);
		if (var4 > var3) {
			var4 = var3;
		}

		if (var4 < -var3) {
			var4 = -var3;
		}

		return var1 + var4;
	}

}
