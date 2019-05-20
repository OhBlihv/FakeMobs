package me.ohblihv.FakeMobs.mobs.loader;

import me.ohblihv.FakeMobs.mobs.BaseEntity;
import me.ohblihv.FakeMobs.mobs.actions.BaseAction;
import me.ohblihv.FakeMobs.mobs.loader.exception.EntityLoaderException;
import me.ohblihv.FakeMobs.mobs.loader.meta.EntityLoaderMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Deque;

public interface EntityLoader<T extends EntityLoaderMeta>
{

	T loadConfiguration(ConfigurationSection configurationSection) throws EntityLoaderException;

	BaseEntity createEntityContainer(T entityLoaderMeta);

	Deque<BaseAction> loadActions(ConfigurationSection configurationSection);

}
