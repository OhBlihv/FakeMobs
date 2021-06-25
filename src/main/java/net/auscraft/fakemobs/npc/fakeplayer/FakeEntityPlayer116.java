package net.auscraft.fakemobs.npc.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.MinecraftServer;

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
