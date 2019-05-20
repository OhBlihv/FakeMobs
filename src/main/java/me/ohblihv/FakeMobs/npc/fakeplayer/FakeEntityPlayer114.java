package me.ohblihv.FakeMobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.WorldServer;

public class FakeEntityPlayer114 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer114(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bt, Byte.MAX_VALUE); //All Skin Parts Enabled
	}

}
