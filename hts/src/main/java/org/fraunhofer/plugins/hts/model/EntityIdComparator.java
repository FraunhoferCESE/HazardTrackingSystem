package org.fraunhofer.plugins.hts.model;

import java.util.Comparator;

import net.java.ao.Entity;

public class EntityIdComparator implements Comparator<Entity> {

	@Override
	public int compare(Entity o1, Entity o2) {
		if (o1.getID() == o2.getID())
			return 0;
		return o1.getID() < o2.getID() ? -1 : 1;
	}
}
