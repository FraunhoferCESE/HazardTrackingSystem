package org.fraunhofer.plugins.hts.db.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.Subsystems;
import org.fraunhofer.plugins.hts.db.service.SubsystemService;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;

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
		final Subsystems[] subsystem = ao.find(Subsystems.class, Query.select().where("ID=?", id));
		return subsystem.length > 0 ? subsystem[0] : null;
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
	public Subsystems[] getSubsystemsByID(int[] id) {
		if(id != null) {
			Subsystems[] subsystemArr = new Subsystems[id.length];
			for(int i = 0; i < id.length; i++) {
				subsystemArr[i] = ao.get(Subsystems.class, id[i]);
			}
			return subsystemArr;
		}
		return null;
	}
	
}
