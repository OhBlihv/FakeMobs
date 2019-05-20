package me.ohblihv.FakeMobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.PlayerInteractManager;
import net.minecraft.server.v1_9_R2.WorldServer;

public class FakeEntityPlayer19 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer19(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bq, Byte.MAX_VALUE); //All Skin Parts Enabled*/
		//> 1.8 this.datawatcher.a(10, Byte.MAX_VALUE); //All Skin Parts Enabled*/

		//BUtil.log("Initialized at " + new CraftPlayer((CraftServer) Bukkit.getServer(), this).getLocation());
	}

}