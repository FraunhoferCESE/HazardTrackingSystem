package org.fraunhofer.plugins.hts.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.model.ControlGroups;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ControlGroupsService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();

	public ControlGroupsService(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}

	private void initializeTable() {
		synchronized (_lock) {
			if (!initialized) {
				if (ao.find(ControlGroups.class).length == 0) {
					add("Design");
					add("Safety devices");
					add("Cautions/warnings");
					add("Procedures/training");
					add("Other");
				}
				initialized = true;
			}
		}
	}

	private ControlGroups add(String label) {
		final ControlGroups cg = ao.create(ControlGroups.class);
		cg.setLabel(label);
		cg.save();
		return cg;
	}

	public ControlGroups getControlGroupByID(String id) {
		initializeTable();

		final ControlGroups[] cg = ao.find(ControlGroups.class, Query.select().where("ID=?", id));
		return cg.length > 0 ? cg[0] : null;
	}

	public List<ControlGroups> all() {
		initializeTable();
		return newArrayList(ao.find(ControlGroups.class));
	}
}
