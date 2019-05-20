package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.skytonia.SkyCore.util.BUtil;
import me.ohblihv.FakeMobs.FakeMobs;
import me.ohblihv.FakeMobs.mobs.BaseMob;
import me.ohblihv.FakeMobs.mobs.NPCMob;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer114;
import me.ohblihv.FakeMobs.util.packets.WrapperPlayServerScoreboardTeam_1_13;
import me.ohblihv.FakeMobs.util.packets.WrapperPlayServerSpawnEntityLiving_1_13_2;
import me.ohblihv.FakeMobs.util.skins.SkinFetcher;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_14_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_14_R1.PlayerConnection;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PacketUtil_1_14 implements IPacketUtil
{

	@Override
	public FakeEntityPlayer getFakeEntityPlayer(World world, GameProfile gameProfile)
	{
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		return new FakeEntityPlayer114(
			MinecraftServer.getServer(), worldServer,
			gameProfile, new PlayerInteractManager(worldServer)
		);
	}

	public void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		WrapperPlayServerSpawnEntityLiving_1_13_2 spawnPacket = new WrapperPlayServerSpawnEntityLiving_1_13_2();

		spawnPacket.setEntityID(baseMob.getEntityId());

		//typeId is unused as of 1.13
		//spawnPacket.setType(baseMob.getEntityType());
		EntityTypes entityTypes;
		try
		{
			entityTypes = EntityTypes.a(baseMob.getEntityType().getName()).get();
		}
		catch(Exception e)
		{
			BUtil.log("Unable to find Entity '" + baseMob.getEntityType().name() + "'");
			e.printStackTrace();
			return;
		}

		//Set entity type id
		spawnPacket.getHandle().getIntegers().write(1, IRegistry.ENTITY_TYPE.a(entityTypes));
		//BUtil.log("Spawning as ID=" + IRegistry.ENTITY_TYPE.a(entityTypes));

		Location spawnLocation = baseMob.getMobLocation();

		spawnPacket.setX(spawnLocation.getX());
		spawnPacket.setY(spawnLocation.getY());
		spawnPacket.setZ(spawnLocation.getZ());

		spawnPacket.setPitch(spawnLocation.getPitch());
		spawnPacket.setYaw(spawnLocation.getYaw());
		//BUtil.log("Spawning at " + spawnLocation.getX() + " " + spawnLocation.getY() + " " + spawnLocation.getZ() + " | " +
		//		              spawnLocation.getYaw() + " " + spawnLocation.getPitch() + " as id=" + baseMob.getEntityId());

		//Reverse these values since yaw == pitch and vice-versa
		spawnPacket.setYaw(spawnLocation.getPitch());
		spawnPacket.setHeadPitch(spawnLocation.getYaw());

		WrappedDataWatcher watcher = PacketUtil.getDefaultWatcher(spawnLocation.getWorld(), baseMob.getEntityType());
		watcher.setObject(0, (byte) 0);
		spawnPacket.setMetadata(watcher);

		//BUtil.log("Spawning mob as " + baseMob.getEntityType() + " with id " + baseMob.getEntityId());
		spawnPacket.sendPacket(player);
	}

	@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(
			PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) npcMob.getFakeEntityPlayer());

		playerConnection.sendPacket(infoPacket);

		playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn((EntityHuman) npcMob.getFakeEntityPlayer()));

		new BukkitRunnable()
		{

			int tick = 0;

			@Override
			public void run()
			{
				if (tick == 0)
				{
					playerConnection.sendPacket(infoPacket);

					final Location location = npcMob.getMobLocation();

					playerConnection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(npcMob.getEntityId(), (byte) ((location.getX() - ((int) location.getX())) / 32D), (byte) ((location.getX() - ((int) location.getY())) / 32D), (byte) ((location.getZ() - ((int) location.getZ())) / 32D), (byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F)), (byte) (MathHelper.d(location.getPitch() * 256.0F / 360.0F)), true));

					playerConnection.sendPacket(new PacketPlayOutEntityHeadRotation((Entity) npcMob.getFakeEntityPlayer(), (byte) (MathHelper.d(location.getYaw() * 256.0F / 360.0F))));
				}

				WrapperPlayServerScoreboardTeam_1_13 teamPacket = (WrapperPlayServerScoreboardTeam_1_13) getNPCTeam();
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
	public AbstractPacket getNPCTeam()
	{
		WrapperPlayServerScoreboardTeam_1_13 teamPacket = new WrapperPlayServerScoreboardTeam_1_13();
		teamPacket.setName(FakeMobs.NPC_TEAM);
		teamPacket.setMode(3);

		return teamPacket;
	}

	@Override
	public AbstractPacket getInitialNPCTeam()
	{
		WrapperPlayServerScoreboardTeam_1_13 teamPacket = new WrapperPlayServerScoreboardTeam_1_13();
		teamPacket.setNameTagVisibility("never");
		teamPacket.setName(FakeMobs.NPC_TEAM);
		teamPacket.setMode(0);
		teamPacket.setPrefix(WrappedChatComponent.fromText("§8[NPC] "));

		return teamPacket;
	}

	@Override
	public void sendLookPacket(Player player, float yaw, float pitch, int entityId)
	{
		PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket = new PacketPlayOutEntity.PacketPlayOutEntityLook(
			entityId,
			(byte) MathHelper.d(yaw * 256.0F / 360.0F),
			(byte) MathHelper.d(pitch * 256.0F / 360.0F), false
		);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(lookPacket);
	}

	@Override
	public void initializeSkin(String skinUUID, NPCMob targetNPC, World world)
	{
		new SkinFetcher(skinUUID, getAuthenticationService(), targetNPC).start();
	}

	@Override
	public YggdrasilAuthenticationService getAuthenticationService()
	{
		return ((YggdrasilMinecraftSessionService) MinecraftServer.getServer().getMinecraftSessionService()).getAuthenticationService();
	}

}
