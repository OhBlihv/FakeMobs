package me.ohblihv.FakeMobs.util;

import com.comphenix.packetwrapper.AbstractPacket;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.NPCEntity;
import me.ohblihv.FakeMobs.npc.fakeplayer.FakeEntityPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 8/20/2017.
 */
public interface IPacketUtil
{

	FakeEntityPlayer getFakeEntityPlayer(World world, GameProfile gameProfile);
	
	void sendSpawnPacket(Player player, BaseEntity baseEntity);

	void sendPlayerSpawnPackets(Player player, NPCEntity npcMob);
	
	void sendDestroyPacket(Player player, int entityId);

	AbstractPacket getNPCTeam();

	AbstractPacket getInitialNPCTeam();

	// NMS Operations

	void sendLookPacket(Player player, float yaw, float pitch, int entityId);

	void initializeSkin(String skinUUID, NPCEntity targetNPC, World world);

	YggdrasilAuthenticationService getAuthenticationService();

}
