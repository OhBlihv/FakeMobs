package net.auscraft.fakemobs.util;

import com.comphenix.packetwrapper.AbstractPacket;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.datafixers.util.Pair;
import net.auscraft.fakemobs.FakeMobs;
import net.auscraft.fakemobs.mobs.BaseMob;
import net.auscraft.fakemobs.mobs.NPCMob;
import net.auscraft.fakemobs.npc.fakeplayer.FakeEntityPlayer;
import net.auscraft.fakemobs.npc.fakeplayer.FakeEntityPlayer117;
import net.auscraft.fakemobs.util.lib.MathHelper;
import net.auscraft.fakemobs.util.packets.WrapperPlayServerScoreboardTeam_1_17;
import net.auscraft.fakemobs.util.packets.WrapperPlayServerSpawnEntityLiving_1_17;
import net.auscraft.fakemobs.util.skins.SkinFetcher;
import net.auscraft.skycore.util.BUtil;
import net.minecraft.core.IRegistry;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PacketUtil_1_17_R1 implements IPacketUtil
{

	@Override
	public FakeEntityPlayer getFakeEntityPlayer(World world, GameProfile gameProfile)
	{
		WorldServer worldServer = ((CraftWorld) world).getHandle();

		return new FakeEntityPlayer117(MinecraftServer.getServer(), worldServer, gameProfile);
	}

	public void sendSpawnPacket(Player player, BaseMob baseMob)
	{
		WrapperPlayServerSpawnEntityLiving_1_17 spawnPacket = new WrapperPlayServerSpawnEntityLiving_1_17();

		spawnPacket.setEntityID(baseMob.getEntityId());

		//typeId is unused as of 1.13
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
		//spawnPacket.getHandle().getIntegers().write(1, IRegistry.Y.a(entityTypes));
		spawnPacket.getHandle().getIntegers().write(1, IRegistry.Y.getId(entityTypes));
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
		try
		{
			// Do not trust extending classes
			baseMob.setMetadata(watcher);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// 1.15+ does not contain the metadata in the spawn packet.
		WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata();
		metadataPacket.setMetadata(watcher.getWatchableObjects());
		metadataPacket.setEntityID(baseMob.getEntityId());

		//BUtil.log("Spawning mob as " + baseMob.getEntityType() + " with id " + baseMob.getEntityId());
		spawnPacket.sendPacket(player);

		//metadataPacket.sendPacket(player);
	}

	@Override
	public void sendPlayerSpawnPackets(Player player, NPCMob npcMob)
	{
		PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;

		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(
			PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, (EntityPlayer) npcMob.getFakeEntityPlayer());

		playerConnection.sendPacket(infoPacket);

		playerConnection.sendPacket(new PacketPlayOutNamedEntitySpawn((EntityHuman) npcMob.getFakeEntityPlayer()));

		WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(PacketContainer.fromPacket(
			new PacketPlayOutEntityMetadata(npcMob.getEntityId(), (DataWatcher) npcMob.getFakeEntityPlayer().getDatawWatcher(), true)
		));

		WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher(metadataPacket.getMetadata());
		wrappedDataWatcher.setObject(17, (byte) 0xFF);

		metadataPacket.setMetadata(wrappedDataWatcher.getWatchableObjects());
		metadataPacket.sendPacket(player);

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

				WrapperPlayServerScoreboardTeam_1_17 teamPacket = (WrapperPlayServerScoreboardTeam_1_17) getNPCTeam();
				teamPacket.getPlayers().add(npcMob.getProfile().getName());

				teamPacket.sendPacket(player);

				final List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> equipmentList = new ArrayList<>();

				for(EnumItemSlot slot : EnumItemSlot.values())
				{
					ItemStack itemStack = null;
					switch(slot)
					{
						case f: itemStack = npcMob.getHeadItem(); break; // HEAD
						case e: itemStack = npcMob.getBodyItem(); break; // CHEST
						case d: itemStack = npcMob.getLegsItem(); break; // LEGS
						case c: itemStack = npcMob.getFeetItem(); break; // FEET
						case a: itemStack = npcMob.getMainHandItem(); break; // MAINHAND
						case b: itemStack = npcMob.getOffHandItem(); break; // OFFHAND
					}

					if(itemStack == null)
					{
						continue;
					}

					equipmentList.add(new Pair<>(slot, CraftItemStack.asNMSCopy(itemStack)));
				}

				if(!equipmentList.isEmpty())
				{
					try
					{
						PacketPlayOutEntityEquipment equipmentPacket = new PacketPlayOutEntityEquipment(npcMob.getEntityId(), equipmentList);

						((CraftPlayer) player).getHandle().b.sendPacket(equipmentPacket);
					}
					catch(Exception e)
					{
						//
					}
				}

				if(++tick > 6)
				{
					playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
						PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, (EntityPlayer) npcMob.getFakeEntityPlayer()));

					this.cancel();
				}
			}

		}.runTaskTimerAsynchronously(FakeMobs.getInstance(),  5, 20);
	}

	public void sendDestroyPacket(Player player, int entityId)
	{
		// 1.17 removed multi-destroy packets
		((CraftPlayer) player).getHandle().b.sendPacket(new PacketPlayOutEntityDestroy(entityId));
	}

	@Override
	public AbstractPacket getNPCTeam()
	{
		WrapperPlayServerScoreboardTeam_1_17 teamPacket = new WrapperPlayServerScoreboardTeam_1_17();
		teamPacket.setName(FakeMobs.NPC_TEAM);
		teamPacket.setMode(3);

		return teamPacket;
	}

	@Override
	public AbstractPacket getInitialNPCTeam()
	{
		WrapperPlayServerScoreboardTeam_1_17 teamPacket = new WrapperPlayServerScoreboardTeam_1_17();
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
			(byte) MathHelper.d(pitch * 256.0F / 360.0F), true
		);

		((CraftPlayer) player).getHandle().b.sendPacket(lookPacket);
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
