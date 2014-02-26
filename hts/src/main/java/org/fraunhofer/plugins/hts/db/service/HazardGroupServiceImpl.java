package org.fraunhofer.plugins.hts.db.service;

import java.util.List;

import org.fraunhofer.plugins.hts.db.Hazard_Group;

import com.atlassian.activeobjects.external.ActiveObjects;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class HazardGroupServiceImpl implements HazardGroupService{
	private final ActiveObjects ao;
	
	public HazardGroupServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	@Override
	public Hazard_Group add(String label) {
		final Hazard_Group group = ao.create(Hazard_Group.class);
		group.setLabel(label);
		return group;
	}

	@Override
	public List<Hazard_Group> all() {
		return newArrayList(ao.find(Hazard_Group.class));
	}

}
