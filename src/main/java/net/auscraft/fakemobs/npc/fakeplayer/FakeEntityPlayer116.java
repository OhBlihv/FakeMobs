package net.auscraft.fakemobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.MinecraftServer;

public class FakeEntityPlayer116 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer116(MinecraftServer srv, WorldServer world, GameProfile game)
	{
		//super(srv, world, game, interact);
		super(srv, world, game);
		//this.Y.set(bj, (byte) 0xFF); //All Skin Parts Enabled
		this.Y.set(DataWatcherRegistry.c.a(1000), bj); // Can I use any ID here?
	}

	@Override
	public Object getDatawWatcher()
	{
		return super.getDataWatcher();
	}
}
