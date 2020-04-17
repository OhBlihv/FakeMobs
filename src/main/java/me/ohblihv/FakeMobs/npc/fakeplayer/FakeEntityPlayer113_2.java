package me.ohblihv.FakeMobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.PlayerInteractManager;
import net.minecraft.server.v1_13_R2.WorldServer;
import net.minecraft.server.v1_13_R2.EntityPlayer;

public class FakeEntityPlayer113_2 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer113_2(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bx, Byte.MAX_VALUE); //All Skin Parts Enabled
	}

	@Override
	public Object getDatawWatcher()
	{
		return super.getDataWatcher();
	}

}
