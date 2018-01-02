package me.ohblihv.FakeMobs.mobs.actions;

import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Created by Chris Brown (OhBlihv) on 9/09/2016.
 */
public abstract class BaseAction
{
	
	static final Random random = new Random();
	
	public abstract void doAction(Player player);
	
}
