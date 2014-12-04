package org.fraunhofer.plugins.hts.db.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import net.java.ao.Query;

import org.fraunhofer.plugins.hts.db.ControlGroups;
import org.fraunhofer.plugins.hts.db.service.ControlGroupsService;

import com.atlassian.activeobjects.external.ActiveObjects;

public class ControlGroupsServiceImpl implements ControlGroupsService {

	private final ActiveObjects ao;
	private static boolean initialized = false;
	private static Object _lock = new Object();
	
	public ControlGroupsServiceImpl(ActiveObjects ao) {
		this.ao = checkNotNull(ao);
	}
	
	public void initializeTable() {
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
	
	public static boolean isInitialized() {
		synchronized (_lock) {
			return initialized;
		}
	}
	
	public static void reset() {
		synchronized (_lock) {
			initialized = false;
		}
	}
	
	@Override
	public ControlGroups add(String label) {
		final ControlGroups cg = ao.create(ControlGroups.class);
		cg.setLabel(label);
		cg.save();
		return cg;
	}

	@Override
	public ControlGroups getControlGroupByID(String id) {
		initializeTable();
		
		final ControlGroups[] cg = ao.find(ControlGroups.class, Query.select().where("ID=?", id));
		return cg.length > 0 ? cg[0] : null;
	}

	@Override
	public List<ControlGroups> all() {
		initializeTable();
		return newArrayList(ao.find(ControlGroups.class));
	}
}
