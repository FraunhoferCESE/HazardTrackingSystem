package org.fraunhofer.plugins.hts.db.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;

import com.atlassian.activeobjects.external.ActiveObjects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

public class SubsystemServiceImpl implements SubsystemService {
	private final ActiveObjects ao;

	public SubsystemServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	@Override
	public Subsystems add(String label, String subsysDesc) {
		final Subsystems subsys = ao.create(Subsystems.class);
		subsys.setLabel(label);
		subsys.setDescription(subsysDesc);
		subsys.save();
		return null;
	}

	@Override
	public Subsystems getSubsystemByID(String id) {
		final Subsystems[] subsys = ao.find(Subsystems.class, Query.select().where("ID=?", id));
		return subsys.length > 0 ? subsys[0] : null;
	}

	@Override
	public List<Subsystems> all() {
		return newArrayList(ao.find(Subsystems.class));
	}

	@Override
	public Subsystems update(Subsystems subsystemToUpdate, String label) {
		subsystemToUpdate.setLabel(label);
		subsystemToUpdate.save();
		return null;
	}

	@Override
	public Subsystems[] getSubsystemsByID(String[] id) {
		ArrayList<Subsystems> subsyslist = new ArrayList<Subsystems>();
		for(String theID : id) {
			if(theID != null) {
				subsyslist.add(getSubsystemByID(theID));
			}
		}
		//TODO validate
		return (Subsystems[]) subsyslist.toArray();
	}

}
