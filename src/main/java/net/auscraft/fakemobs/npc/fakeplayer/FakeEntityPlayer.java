package net.auscraft.fakemobs.npc.fakeplayer;

public interface FakeEntityPlayer
{

	void setLocation(double x, double y, double z, float yaw, float pitch);

	int getId();

	Object getDatawWatcher();

}
