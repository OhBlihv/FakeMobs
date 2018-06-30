package me.ohblihv.FakeMobs.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.PlayerInteractManager;
import net.minecraft.server.v1_9_R2.WorldServer;

import java.util.UUID;

public class FakeEntityPlayer extends EntityPlayer
{

	public FakeEntityPlayer(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact,
	                        UUID uuid)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bq, Byte.MAX_VALUE); //All Skin Parts Enabled*/
		//> 1.8 this.datawatcher.a(10, Byte.MAX_VALUE); //All Skin Parts Enabled*/

		//BUtil.log("Initialized at " + new CraftPlayer((CraftServer) Bukkit.getServer(), this).getLocation());
	}

}