package me.ohblihv.FakeMobs.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;

public class FakeEntityPlayer extends EntityPlayer
{

	public FakeEntityPlayer(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.watch(10, Byte.MAX_VALUE); //All Skin Parts Enabled*/
	}

}