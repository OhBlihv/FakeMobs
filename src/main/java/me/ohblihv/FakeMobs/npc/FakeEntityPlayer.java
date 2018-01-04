package me.ohblihv.FakeMobs.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

public class FakeEntityPlayer extends EntityPlayer
{

	public FakeEntityPlayer(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.watch(10, Byte.MAX_VALUE); //All Skin Parts Enabled*/

		//BUtil.log("Initialized at " + new CraftPlayer((CraftServer) Bukkit.getServer(), this).getLocation());
	}

}