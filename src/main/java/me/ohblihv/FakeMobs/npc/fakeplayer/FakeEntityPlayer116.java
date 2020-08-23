package me.ohblihv.FakeMobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.PlayerInteractManager;
import net.minecraft.server.v1_16_R2.WorldServer;

public class FakeEntityPlayer116 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer116(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact)
	{
		super(srv, world, game, interact);

		this.datawatcher.set(bj, (byte) 0xFF); //All Skin Parts Enabled
	}

	@Override
	public Object getDatawWatcher()
	{
		return super.getDataWatcher();
	}
}
