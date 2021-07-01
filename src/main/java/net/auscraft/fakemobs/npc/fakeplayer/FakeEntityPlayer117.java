package net.auscraft.fakemobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;

public class FakeEntityPlayer117 extends EntityPlayer implements FakeEntityPlayer
{

	public FakeEntityPlayer117(MinecraftServer srv, WorldServer world, GameProfile game)
	{
		super(srv, world, game);
		//this.Y.set(bj, (byte) 0xFF); //All Skin Parts Enabled
		// TODO: Re-implement the below
		//this.Y.set(DataWatcherRegistry.c.a(1000), bj); // Can I use any ID here?
	}

	@Override
	public Object getDatawWatcher()
	{
		return super.getDataWatcher();
	}
}
