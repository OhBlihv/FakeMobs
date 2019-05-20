package me.ohblihv.FakeMobs.mobs.loader;

import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.SimpleEntity;
import me.ohblihv.FakeMobs.mobs.loader.configuration.InteractionType;
import me.ohblihv.FakeMobs.mobs.loader.meta.BaseEntityLoaderMeta;
import me.ohblihv.FakeMobs.util.PacketUtil;

public class SimpleEntityLoader extends BaseEntityLoader
{

	@Override
	public BaseEntity createEntityContainer(BaseEntityLoaderMeta entityLoaderMeta)
	{
		//Trigger the DataWatcher cache for this entity type
		PacketUtil.getDefaultWatcher(entityLoaderMeta.getLocation().getWorld(), entityLoaderMeta.getEntityType());

		return new SimpleEntity(entityLoaderMeta.getEntityId(), entityLoaderMeta.getLocation(),
			entityLoaderMeta.getInteractActions().get(InteractionType.LEFT_CLICK),
			entityLoaderMeta.getInteractActions().get(InteractionType.RIGHT_CLICK), entityLoaderMeta.getEntityType());
	}

}
