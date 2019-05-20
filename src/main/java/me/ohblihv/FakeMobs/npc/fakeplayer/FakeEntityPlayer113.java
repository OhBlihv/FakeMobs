package me.ohblihv.FakeMobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.PlayerInteractManager;
import net.minecraft.server.v1_13_R1.WorldServer;

public class FakeEntityPlayer113 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer113(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bx, Byte.MAX_VALUE); //All Skin Parts Enabled
	}

}
