package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer18;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Chris Brown (OhBlihv) on 8/20/2017.
 */
public class PacketUtil_1_8_R3 implements IPacketUtil
{

	@Override
	public FakeEntityPlayer getFakeEntityPlayer(World world, GameProfile gameProfile)
	{
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		return new FakeEntityPlayer18(
			MinecraftServer.getServer(), worldServer,
			gameProfile, new net.minecraft.server.v1_8_R3.PlayerInteractManager(worldServer)
		);
	}

	public void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		WrapperPlayServerSpawnEntityLiving spawnPacket = new WrapperPlayServerSpawnEntityLiving();

		spawnPacket.setEntityID(baseMob.getEntityId());
		spawnPacket.setType(baseMob.getEntityType());

		Location spawnLocation = baseMob.getMobLocation();
		spawnPacket.setX((int) spawnLocation.getX());
		spawnPacket.setY((int) spawnLocation.getY());
		spawnPacket.setZ((int) spawnLocation.getZ());
		spawnPacket.setYaw(spawnLocation.getYaw());
		//BUtil.logInfo("Spawning at " + spawnLocation.getX() + " " + spawnLocation.getY() + " " + spawnLocation.getZ() + "| " +
		//		              spawnLocation.getYaw() + " " + spawnLocation.getPitch() + " as id=" + baseMob.getEntityId());

		//Reverse these values since yaw == pitch and vice-versa
		//spawnPacket.setYaw(spawnLocation.getPitch());
		spawnPacket.setHeadPitch(spawnLocation.getPitch());

		WrappedDataWatcher watcher = PacketUtil.getDefaultWatcher(spawnLocation.getWorld(), baseMob.getEntityType());
		watcher.setObject(0, (byte) 0);
		spawnPacket.setMetadata(watcher);

		//BUtil.logInfo("Spawning mob as " + baseMob.getEntityType() + " with id " + baseMob.getEntityId());
		spawnPacket.sendPacket(player);
	}

	@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(
			PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) npcMob.getFakeEntityPlayer());

		playerConnection.sendPacket(infoPacket);

		new BukkitRunnable()
		{

			int tick = 0;

			@Override
			public void run()
			{
				if (tick == 0)
				{
					playerConnection.sendPacket(infoPacket);

					playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn((EntityHuman) npcMob.getFakeEntityPlayer()));

					final Location location = npcMob.getMobLocation();

					playerConnection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(npcMob.getEntityId(), (byte) ((location.getX() - ((int) location.getX())) / 32D), (byte) ((location.getX() - ((int) location.getY())) / 32D), (byte) ((location.getZ() - ((int) location.getZ())) / 32D), (byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F)), (byte) (MathHelper.d(location.getPitch() * 256.0F / 360.0F)), true));

					playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation((Entity) npcMob.getFakeEntityPlayer(), (byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F))));
				}

				WrapperPlayServerScoreboardTeam teamPacket = new WrapperPlayServerScoreboardTeam();
				teamPacket.setNameTagVisibility("never");
				teamPacket.setName(FakeMobs.NPC_TEAM);
				teamPacket.setMode(3);
				teamPacket.setPrefix("§8[NPC] ");
				teamPacket.getPlayers().add(npcMob.getProfile().getName());

				teamPacket.sendPacket(player);

				for(EnumWrappers.ItemSlot slot : EnumWrappers.ItemSlot.values())
				{
					WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment();

					equipmentPacket.setEntityID(npcMob.getEntityId());

					ItemStack itemStack = null;
					switch(slot)
					{
						case HEAD: itemStack = npcMob.getHeadItem(); break;
						case CHEST: itemStack = npcMob.getBodyItem(); break;
						case LEGS: itemStack = npcMob.getLegsItem(); break;
						case FEET: itemStack = npcMob.getFeetItem(); break;
						case MAINHAND: itemStack = npcMob.getMainHandItem(); break;
						case OFFHAND: itemStack = npcMob.getOffHandItem(); break;
					}

					if(itemStack == null)
					{
						continue;
					}

					equipmentPacket.setItem(itemStack);

					try
					{
						equipmentPacket.setSlot(slot);

						equipmentPacket.sendPacket(player);
					}
					catch(Exception e)
					{
						//
					}
				}

				if(++tick > 6)
				{
					playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
						PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) npcMob.getFakeEntityPlayer()));

					this.cancel();
				}
			}

		}.runTaskTimerAsynchronously(FakeMobs.getInstance(),  5, 20);
	}

	public void sendDestroyPacket(Player player, int entityId)
	{
		WrapperPlayServerEntityDestroy destroyPacket = new WrapperPlayServerEntityDestroy();

		destroyPacket.setEntityIds(new int[] {entityId});

		destroyPacket.sendPacket(player);
	}

	@Override
	public void sendLookPacket(Player player, float yaw, float pitch, int entityId)
	{
		net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook(
			entityId,
			(byte) MathHelper.d(yaw * 256.0F / 360.0F),
			(byte) MathHelper.d(pitch * 256.0F / 360.0F), false
		);

		((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(lookPacket);
	}

	@Override
	public WrapperPlayServerScoreboardTeam getNPCTeam()
	{
		WrapperPlayServerScoreboardTeam teamPacket = new WrapperPlayServerScoreboardTeam();
		teamPacket.setNameTagVisibility("never");
		teamPacket.setName(FakeMobs.NPC_TEAM);
		teamPacket.setMode(3);
		teamPacket.setPrefix("§8[NPC] ");

		return teamPacket;
	}

	@Override
	public AbstractPacket getInitialNPCTeam()
	{
		WrapperPlayServerScoreboardTeam teamPacket = new WrapperPlayServerScoreboardTeam();
		teamPacket.setNameTagVisibility("never");
		teamPacket.setName(FakeMobs.NPC_TEAM);
		teamPacket.setMode(0);
		teamPacket.setPrefix("§8[NPC] ");

		return teamPacket;
	}

	@Override
	public void initializeSkin(String skinUUID, NPCMob targetNPC, World world)
	{
		new SkinFetcher(skinUUID, getAuthenticationService(), targetNPC).start();
	}

	@Override
	public YggdrasilAuthenticationService getAuthenticationService()
	{
		return ((YggdrasilMinecraftSessionService) MinecraftServer.getServer().aD()).getAuthenticationService();
	}

}
