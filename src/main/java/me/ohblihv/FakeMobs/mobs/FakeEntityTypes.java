package me.ohblihv.FakeMobs.mobs;

import me.ohblihv.FakeMobs.mobs.loader.EntityLoader;
import me.ohblihv.FakeMobs.mobs.loader.SimpleEntityLoader;

public enum FakeEntityTypes
{

	SIMPLE(new SimpleEntityLoader()),

	;

	final EntityLoader entityLoader;

	FakeEntityTypes(EntityLoader entityLoader)
	{
		this.entityLoader = entityLoader;
	}

	public EntityLoader getEntityLoader()
	{
		return this.entityLoader;
	}

}
