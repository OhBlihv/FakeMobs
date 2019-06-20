package me.ohblihv.FakeMobs.mobs.loader;

import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.SimpleEntity;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import me.ohblihv.FakeMobs.mobs.loader.meta.SimpleEntityLoaderMeta;
import me.ohblihv.FakeMobs.util.PacketUtil;

public class SimpleEntityLoader extends BaseEntityLoader<SimpleEntityLoaderMeta>
{

	@Override
	public BaseEntity createEntityContainer(SimpleEntityLoaderMeta entityLoaderMeta)
	{
		//Trigger the DataWatcher cache for this entity type
		PacketUtil.getDefaultWatcher(entityLoaderMeta.getLocation().getWorld(), entityLoaderMeta.getEntityType());

		return new SimpleEntity(entityLoaderMeta.getEntityId(), entityLoaderMeta.getLocation(),
			entityLoaderMeta.getInteractActions().get(InteractionType.LEFT_CLICK),
			entityLoaderMeta.getInteractActions().get(InteractionType.RIGHT_CLICK), entityLoaderMeta.getEntityType());
	}

}
